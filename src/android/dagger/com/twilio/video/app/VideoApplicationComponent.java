package dagger.com.twilio.video.app;/*
 * Copyright (C) 2019 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.twilio.video.app.ApplicationModule;
import com.twilio.video.app.ApplicationScope;
import com.twilio.video.app.AudioSwitchModule;
import com.twilio.video.app.VideoApplication;
import dagger.com.twilio.video.app.sdk.CommunityVideoSdkModule;
import dagger.com.twilio.video.app.security.SecurityModule;

import com.twilio.video.app.ui.room.RoomActivityModule;
import com.twilio.video.app.ui.room.VideoServiceModule;
import com.twilio.video.app.ui.settings.SettingsActivityModule;
import com.twilio.video.app.ui.settings.SettingsFragmentModule;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@ApplicationScope
@Component(
        modules = {
            AndroidInjectionModule.class,
            ApplicationModule.class,
            CommunityTreeModule.class,
            RoomActivityModule.class,
            SettingsActivityModule.class,
            SettingsFragmentModule.class,
            VideoServiceModule.class,
            CommunityVideoSdkModule.class,
            SecurityModule.class,
            AudioSwitchModule.class
        })
public interface VideoApplicationComponent {
    void inject(VideoApplication application);
}
