/*
 * Copyright (C) 2013-2015, The OpenFlint Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.crosskr.flint.webview.browser;

import android.net.Uri;

/**
 * Flint related status.
 * 
 * @author jim
 *
 */
public interface FlintStatusChangeListener {
    void onDeviceSelected(String name);
    void onDeviceUnselected();
    //void onVolumeChanged(double percent, boolean muted);
    //void onApplicationStatusChanged(String status);
    void onApplicationDisconnected();
    void onConnectionFailed();
   // void onConnected();
    //void onNoLongerRunning(boolean isRunning);
    //void onConnectionSuspended();
    //void onMediaStatusUpdated();
    //void onMediaMetadataUpdated(String title, String artist, Uri imageUrl);
    void onApplicationConnectionResult(String applicationStatus);
    //void onLeaveApplication();
    //void onStopApplication();
    void onMediaSeekEnd();
    //void onMediaVolumeEnd();
}
