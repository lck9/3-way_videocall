package src.cordova.plugin.videocall.RoomViewModel

import android.Manifest.permission
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.annotation.VisibleForTesting.PROTECTED
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.twilio.audioswitch.AudioSwitch
import com.twilio.video.Participant

import io.uniflow.android.AndroidDataFlow
import io.uniflow.core.flow.data.UIState
import io.uniflow.core.flow.onState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import src.cordova.plugin.videocall.ParticipantManager.ParticipantManager
import src.cordova.plugin.videocall.ParticipantViewState.buildParticipantViewState
import src.cordova.plugin.videocall.PermissionUtil.PermissionUtil
import src.cordova.plugin.videocall.RoomActivity.RoomActivity
import src.cordova.plugin.videocall.RoomEvent.RoomEvent
import src.cordova.plugin.videocall.RoomManager.RoomManager
import src.cordova.plugin.videocall.RoomViewEffect.RoomViewEffect
import src.cordova.plugin.videocall.RoomViewEvent.RoomViewEvent
import src.cordova.plugin.videocall.RoomViewState.RoomViewConfiguration
import src.cordova.plugin.videocall.RoomViewState.RoomViewState
import src.cordova.plugin.videocall.VideoTrackViewState.VideoTrackViewState
import timber.log.Timber

class RoomViewModel(
  private val roomManager: RoomManager,
  private val audioSwitch: AudioSwitch,
  private val permissionUtil: PermissionUtil,
  private val participantManager: ParticipantManager = ParticipantManager(),
  initialViewState: RoomViewState = RoomViewState(participantManager.primaryParticipant)
) : AndroidDataFlow(defaultState = initialViewState) {

    private var permissionCheckRetry = false
    @VisibleForTesting(otherwise = PRIVATE)
    internal var roomManagerJob: Job? = null

    init {
        audioSwitch.start { audioDevices, selectedDevice ->
            updateState { currentState ->
                currentState.copy(
                        selectedDevice = selectedDevice,
                        availableAudioDevices = audioDevices
                )
            }
        }
        subscribeToRoomEvents()
    }

    @VisibleForTesting(otherwise = PROTECTED)
    public override fun onCleared() {
        super.onCleared()
        audioSwitch.stop()
        roomManagerJob?.cancel()
    }

    fun processInput(viewEvent: RoomViewEvent) {
        Timber.d("View Event: $viewEvent")

        when (viewEvent) {
            RoomViewEvent.OnResume -> checkPermissions()
            RoomViewEvent.OnPause -> roomManager.onPause()
            is RoomViewEvent.SelectAudioDevice -> {
                audioSwitch.selectDevice(viewEvent.device)
            }
            RoomViewEvent.ActivateAudioDevice -> { audioSwitch.activate() }
            RoomViewEvent.DeactivateAudioDevice -> { audioSwitch.deactivate() }
            is RoomViewEvent.Connect -> {
                connect(viewEvent.identity, viewEvent.roomName)
            }
            is RoomViewEvent.PinParticipant -> {
                participantManager.changePinnedParticipant(viewEvent.sid)
                updateParticipantViewState()
            }
            RoomViewEvent.ToggleLocalVideo -> roomManager.toggleLocalVideo()
            RoomViewEvent.EnableLocalVideo -> roomManager.enableLocalVideo()
            RoomViewEvent.DisableLocalVideo -> roomManager.disableLocalVideo()
            RoomViewEvent.ToggleLocalAudio -> roomManager.toggleLocalAudio()
            RoomViewEvent.EnableLocalAudio -> roomManager.enableLocalAudio()
            RoomViewEvent.DisableLocalAudio -> roomManager.disableLocalAudio()
            is RoomViewEvent.StartScreenCapture -> roomManager.startScreenCapture(
                    viewEvent.captureResultCode, viewEvent.captureIntent)
            RoomViewEvent.StopScreenCapture -> roomManager.stopScreenCapture()
            RoomViewEvent.SwitchCamera -> roomManager.switchCamera()
            is RoomViewEvent.VideoTrackRemoved -> {
                participantManager.updateParticipantVideoTrack(viewEvent.sid, null)
                updateParticipantViewState()
            }
            is RoomViewEvent.ScreenTrackRemoved -> {
                participantManager.updateParticipantScreenTrack(viewEvent.sid, null)
                updateParticipantViewState()
            }
            RoomViewEvent.Disconnect -> roomManager.disconnect()
        }
    }

    private fun subscribeToRoomEvents() {
        roomManager.roomEvents.let { sharedFlow ->
            roomManagerJob = viewModelScope.launch {
                Timber.d("Listening for RoomEvents")
                sharedFlow.collect { observeRoomEvents(it) }
            }
        }
    }

    private fun checkPermissions() {
        val isCameraEnabled = permissionUtil.isPermissionGranted(permission.CAMERA)
        val isMicEnabled = permissionUtil.isPermissionGranted(permission.RECORD_AUDIO)

        updateState { currentState ->
            currentState.copy(isCameraEnabled = isCameraEnabled, isMicEnabled = isMicEnabled)
        }
        if (isCameraEnabled && isMicEnabled) {
            roomManager.onResume()
        } else {
            if (!permissionCheckRetry) {
                action {
                    sendEvent {
                        permissionCheckRetry = true
                      RoomViewEffect.PermissionsDenied
                    }
                }
            }
        }
    }

    private fun observeRoomEvents(roomEvent: RoomEvent) {
        Timber.d("observeRoomEvents: %s", roomEvent)
        when (roomEvent) {
            is RoomEvent.Connecting -> {
                showConnectingViewState()
            }
            is RoomEvent.Connected -> {
                showConnectedViewState(roomEvent.roomName)
                checkParticipants(roomEvent.participants)
                action { sendEvent { RoomViewEffect.Connected(roomEvent.room) } }
            }
            is RoomEvent.Disconnected -> showLobbyViewState()
            is RoomEvent.DominantSpeakerChanged -> {
                participantManager.changeDominantSpeaker(roomEvent.newDominantSpeakerSid)
                updateParticipantViewState()
            }
            is RoomEvent.ConnectFailure -> action {
                sendEvent {
                    showLobbyViewState()
                  RoomViewEffect.ShowConnectFailureDialog
                }
            }
            is RoomEvent.MaxParticipantFailure -> action {
                sendEvent { RoomViewEffect.ShowMaxParticipantFailureDialog }
                showLobbyViewState()
            }
//            is TokenError -> action {
//                sendEvent {
//                    showLobbyViewState()
//                    ShowTokenErrorDialog(roomEvent.serviceError)
//                }
//            }
            RoomEvent.RecordingStarted -> updateState { currentState -> currentState.copy(isRecording = true) }
            RoomEvent.RecordingStopped -> updateState { currentState -> currentState.copy(isRecording = false) }
            is RoomEvent.RemoteParticipantEvent -> handleRemoteParticipantEvent(roomEvent)
            is RoomEvent.LocalParticipantEvent -> handleLocalParticipantEvent(roomEvent)
            is RoomEvent.StatsUpdate -> updateState { currentState -> currentState.copy(roomStats = roomEvent.roomStats) }
        }
    }

    private fun handleRemoteParticipantEvent(remoteParticipantEvent: RoomEvent.RemoteParticipantEvent) {
        when (remoteParticipantEvent) {
            is RoomEvent.RemoteParticipantEvent.RemoteParticipantConnected -> addParticipant(remoteParticipantEvent.participant)
            is RoomEvent.RemoteParticipantEvent.VideoTrackUpdated -> {
                participantManager.updateParticipantVideoTrack(remoteParticipantEvent.sid,
                        remoteParticipantEvent.videoTrack?.let { VideoTrackViewState(it) })
                updateParticipantViewState()
            }
            is RoomEvent.RemoteParticipantEvent.TrackSwitchOff -> {
                participantManager.updateParticipantVideoTrack(remoteParticipantEvent.sid,
                        VideoTrackViewState(remoteParticipantEvent.videoTrack,
                                remoteParticipantEvent.switchOff))
                updateParticipantViewState()
            }
            is RoomEvent.RemoteParticipantEvent.ScreenTrackUpdated -> {
                participantManager.updateParticipantScreenTrack(remoteParticipantEvent.sid,
                        remoteParticipantEvent.screenTrack?.let { VideoTrackViewState(it) })
                updateParticipantViewState()
            }
            is RoomEvent.RemoteParticipantEvent.MuteRemoteParticipant -> {
                participantManager.muteParticipant(remoteParticipantEvent.sid,
                        remoteParticipantEvent.mute)
                updateParticipantViewState()
            }
            is RoomEvent.RemoteParticipantEvent.NetworkQualityLevelChange -> {
                participantManager.updateNetworkQuality(remoteParticipantEvent.sid,
                        remoteParticipantEvent.networkQualityLevel)
                updateParticipantViewState()
            }
            is RoomEvent.RemoteParticipantEvent.RemoteParticipantDisconnected -> {
                participantManager.removeParticipant(remoteParticipantEvent.sid)
                if(participantManager.participantThumbnails.size < 2){
                    processInput(RoomViewEvent.Disconnect)
                    ActivityCompat.finishAffinity(RoomActivity.activity)
                }
                updateParticipantViewState()
            }
        }
    }

    private fun handleLocalParticipantEvent(localParticipantEvent: RoomEvent.LocalParticipantEvent) {
        when (localParticipantEvent) {
            is RoomEvent.LocalParticipantEvent.VideoTrackUpdated -> {
                participantManager.updateLocalParticipantVideoTrack(
                        localParticipantEvent.videoTrack?.let { VideoTrackViewState(it) })
                updateParticipantViewState()
                updateState { currentState -> currentState.copy(isVideoOff = localParticipantEvent.videoTrack == null) }
            }
            RoomEvent.LocalParticipantEvent.AudioOn -> updateState { currentState -> currentState.copy(isAudioMuted = false) }
            RoomEvent.LocalParticipantEvent.AudioOff -> updateState { currentState -> currentState.copy(isAudioMuted = true) }
            RoomEvent.LocalParticipantEvent.AudioEnabled -> updateState { currentState -> currentState.copy(isAudioEnabled = true) }
            RoomEvent.LocalParticipantEvent.AudioDisabled -> updateState { currentState -> currentState.copy(isAudioEnabled = false) }
            RoomEvent.LocalParticipantEvent.ScreenCaptureOn -> updateState { currentState -> currentState.copy(isScreenCaptureOn = true) }
            RoomEvent.LocalParticipantEvent.ScreenCaptureOff -> updateState { currentState -> currentState.copy(isScreenCaptureOn = false) }
            RoomEvent.LocalParticipantEvent.VideoEnabled -> updateState { currentState -> currentState.copy(isVideoEnabled = true) }
            RoomEvent.LocalParticipantEvent.VideoDisabled -> updateState { currentState -> currentState.copy(isVideoEnabled = false) }
        }
    }

    private fun addParticipant(participant: Participant) {
        val participantViewState = buildParticipantViewState(participant)
        participantManager.addParticipant(participantViewState)
        updateParticipantViewState()
    }

    private fun showLobbyViewState() {
        action { sendEvent { RoomViewEffect.Disconnected } }
        updateState { currentState ->
            currentState.copy(configuration = RoomViewConfiguration.Lobby)
        }
        participantManager.clearRemoteParticipants()
        updateParticipantViewState()
    }

    private fun showConnectingViewState() {
        updateState { currentState ->
            currentState.copy(configuration = RoomViewConfiguration.Connecting)
        }
    }

    private fun showConnectedViewState(roomName: String) {
        updateState { currentState ->
            currentState.copy(configuration = RoomViewConfiguration.Connected, title = roomName)
        }
    }

    private fun checkParticipants(participants: List<Participant>) {
        for ((index, participant) in participants.withIndex()) {
            if (index == 0) { // local participant
                participantManager.updateLocalParticipantSid(participant.sid)
            } else {
                participantManager.addParticipant(buildParticipantViewState(participant))
            }
        }
        updateParticipantViewState()
    }

    private fun updateParticipantViewState() {
        updateState { currentState ->
            currentState.copy(
                    participantThumbnails = participantManager.participantThumbnails,
                    primaryParticipant = participantManager.primaryParticipant
            )
        }
    }

    private fun connect(identity: String, roomName: String) =
            viewModelScope.launch {
                roomManager.connect(
                        identity,
                        roomName)
            }

    private fun updateState(action: (currentState: RoomViewState) -> UIState) =
            action { onState<RoomViewState> { currentState -> setState { action(currentState) } } }

    @Suppress("UNCHECKED_CAST")
    class RoomViewModelFactory(
        private val roomManager: RoomManager,
        private val audioDeviceSelector: AudioSwitch,
        private val permissionUtil: PermissionUtil
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoomViewModel(roomManager, audioDeviceSelector, permissionUtil) as T
        }
    }
}
