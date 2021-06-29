package src.cordova.plugin.videocall.VideoService

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder

import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import src.cordova.plugin.videocall.RoomManager.RoomManager
import src.cordova.plugin.videocall.RoomNotification.ONGOING_NOTIFICATION_ID
import src.cordova.plugin.videocall.RoomNotification.RoomNotification
import javax.inject.Inject
import timber.log.Timber

private const val ROOM_NAME_EXTRA = "ROOM_NAME_EXTRA"

class VideoService(
    private val rxDisposables: CompositeDisposable = CompositeDisposable()
) : Service() {

    companion object {
        fun startService(context: Context, roomName: String) {
            Intent(context, VideoService::class.java).let { intent ->
                intent.putExtra(ROOM_NAME_EXTRA, roomName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stopService(context: Context) {
            Intent(context, VideoService::class.java).let { context.stopService(it) }
        }
    }

    @Inject lateinit var roomManager: RoomManager

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        setupForegroundService(intent)
        Timber.d("VideoService created")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("VideoService destroyed")
        rxDisposables.clear()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun setupForegroundService(intent: Intent?) {
        intent?.let { it.getStringExtra(ROOM_NAME_EXTRA)?.let { roomName ->
            val roomNotification = RoomNotification(this@VideoService)
            startForeground(
                    ONGOING_NOTIFICATION_ID,
                    roomNotification.buildNotification(roomName))
        } }
    }
}
