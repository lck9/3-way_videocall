package dagger.com.twilio.video.app.sdk

import android.app.Application
import android.content.SharedPreferences
import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.ApplicationScope
import com.twilio.video.app.data.DataModule
import com.twilio.video.app.sdk.ConnectOptionsFactory
import com.twilio.video.app.sdk.RoomManager
import com.twilio.video.app.sdk.VideoClient
import dagger.Module
import dagger.Provides

@Module(includes = [
    ApplicationModule::class,
    DataModule::class])
class CommunityVideoSdkModule {

    @Provides
    fun providesConnectOptionsFactory(
        application: Application,
        sharedPreferences: SharedPreferences
    ): ConnectOptionsFactory =
            ConnectOptionsFactory(application, sharedPreferences)

    @Provides
    fun providesRoomFactory(
        application: Application,
        connectOptionsFactory: ConnectOptionsFactory
    ): VideoClient =
            VideoClient(application, connectOptionsFactory)

    @Provides
    @ApplicationScope
    fun providesRoomManager(
      application: Application,
      videoClient: VideoClient,
      sharedPreferences: SharedPreferences
    ): RoomManager =
            RoomManager(application, videoClient, sharedPreferences)
}
