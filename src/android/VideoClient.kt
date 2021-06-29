package src.cordova.plugin.videocall.VideoClient

import android.content.Context
import com.twilio.video.Room
import com.twilio.video.Video
import src.cordova.plugin.videocall.ConnectOptionsFactory.ConnectOptionsFactory

class VideoClient(
    private val context: Context,
    private val connectOptionsFactory: ConnectOptionsFactory
) {

    suspend fun connect(
        identity: String,
        roomName: String,
        roomListener: Room.Listener
    ): Room {

            return Video.connect(
                    context,
                    connectOptionsFactory.newInstance(identity, roomName),
                    roomListener)
    }
}
