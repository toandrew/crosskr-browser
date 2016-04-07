/**
 * Copyright (C) 2013-2015, Infthink (Beijing) Technology Co., Ltd.
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Floating receiver.
 */
public class MyFloatingReceiver extends BroadcastReceiver {
    private static final String TAG = "FlintReceiver";

    private final String ACTION_NOTIFY = "action.flint.notify";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_NOTIFY.equals(intent.getAction())) {
            String url = intent.getStringExtra("url");
            FxService.setVideoUrl(url);
            if (!FxService.isRunning()) {
                Log.e(TAG, "Starting Flint browser's floating service!");
                
                Intent flingIntent = new Intent(context, FxService.class);
                flingIntent.putExtra("url", url);
                context.startService(flingIntent);
            } else {
                Log.e(TAG, "Flint browser's floating service is already running!");
            }
        }
    }
}