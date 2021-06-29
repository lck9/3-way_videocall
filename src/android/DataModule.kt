/*
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

package src.cordova.plugin.videocall.DataModule

import android.app.Application
import android.content.SharedPreferences
import cordova.plugin.videocall.ApplicationScope.ApplicationScope

import dagger.Module
import dagger.Provides
import src.cordova.plugin.videocall.SharedPreferencesUtil.getSharedPreferences

@Module
class DataModule {
    @Provides
    @ApplicationScope
    internal fun provideSharedPreferences(app: Application): SharedPreferences {
        return getSharedPreferences(app)
    }
}
