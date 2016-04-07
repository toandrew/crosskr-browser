/*
 * FlintDiscoveryProvider
 * Connect SDK
 * 
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on 20 Feb 2014
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.connectsdk.discovery.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;

import com.connectsdk.core.Util;
import com.connectsdk.discovery.DiscoveryFilter;
import com.connectsdk.discovery.DiscoveryProvider;
import com.connectsdk.discovery.DiscoveryProviderListener;
import com.connectsdk.service.FlintService;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.config.FlintServiceDescription;
import com.connectsdk.service.config.ServiceDescription;
import tv.matchstick.flint.FlintDevice;
import tv.matchstick.flint.FlintMediaControlIntent;

public class FlintDiscoveryProvider implements DiscoveryProvider {
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    protected MediaRouter.Callback mMediaRouterCallback;

    protected ConcurrentHashMap<String, ServiceDescription> foundServices;
    protected CopyOnWriteArrayList<DiscoveryProviderListener> serviceListeners;

//    private final static int RESCAN_INTERVAL = 3000;
//    private final static int RESCAN_ATTEMPTS = 3;
//    private final static int SSDP_TIMEOUT = RESCAN_INTERVAL * RESCAN_ATTEMPTS;

//    private Timer addCallbackTimer;
    //private Timer removeCallbackTimer;

    boolean isRunning = false;

    public FlintDiscoveryProvider(Context context) {
        mMediaRouter = createMediaRouter(context);
        mMediaRouterCallback = new MediaRouterCallback();

        foundServices = new ConcurrentHashMap<String, ServiceDescription>(8, 0.75f, 2);
        serviceListeners = new CopyOnWriteArrayList<DiscoveryProviderListener>();
    }

    protected MediaRouter createMediaRouter(Context context) {
        return MediaRouter.getInstance(context);
    }

    @Override
    public void start() {
        if (isRunning) 
            return;

        isRunning = true;

        if (mMediaRouteSelector == null) {
            try {
                mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(FlintMediaControlIntent.categoryForFlint(FlintService.getApplicationID()))
                .build();
            } catch (IllegalArgumentException e) {
                Log.w("Connect SDK", "Invalid application ID: " + FlintService.getApplicationID());
                for (DiscoveryProviderListener listener : serviceListeners) {
                    listener.onServiceDiscoveryFailed(this, new ServiceCommandError(0, "Invalid application ID: " + FlintService.getApplicationID(), null));
                }
                return;
            }
        }
        
        // only once
        rescan();
        
//        addCallbackTimer = new Timer();
//        addCallbackTimer.schedule(new TimerTask() {
//
//            @Override
//            public void run() {
//                sendSearch();
//            }
//        }, 100, RESCAN_INTERVAL);

        /*
        removeCallbackTimer = new Timer();
        removeCallbackTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                Util.runOnUI(new Runnable() {

                    @Override
                    public void run() {
                        mMediaRouter.removeCallback(mMediaRouterCallback);
                    }
                });
            }
        }, RESCAN_INTERVAL - 900, RESCAN_INTERVAL);
        */
    }

//    private void sendSearch() {
//        List<String> killKeys = new ArrayList<String>();
//
//        long killPoint = new Date().getTime() - SSDP_TIMEOUT;
//
//        for (String key : foundServices.keySet()) {
//            ServiceDescription service = foundServices.get(key);
//            if (service == null || service.getLastDetection() < killPoint) {
//                killKeys.add(key);
//            }
//        }
//
//        for (String key : killKeys) {
//            final ServiceDescription service = foundServices.get(key);
//
//            if (service != null) {
//                Util.runOnUI(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        for (DiscoveryProviderListener listener : serviceListeners) {
//                            listener.onServiceRemoved(FlintDiscoveryProvider.this, service);
//                        }
//                    }
//                });
//            }
//
//            if (foundServices.containsKey(key))
//                foundServices.remove(key);
//        }
//
//        //rescan();
//    }

    @Override
    public void stop() {
        isRunning = false;

//        if (addCallbackTimer != null) {
//            addCallbackTimer.cancel();
//            addCallbackTimer = null;
//        }
        
//        if (removeCallbackTimer != null) {
//            removeCallbackTimer.cancel();
//            removeCallbackTimer = null;
//        }

        if (mMediaRouter != null) {
            Util.runOnUI(new Runnable() {

                @Override
                public void run() {
                    mMediaRouter.removeCallback(mMediaRouterCallback);
                }
            });
        }
    }

    @Override
    public void restart() {
        stop();
        start();
    }

    @Override
    public void reset() {
        stop();
        foundServices.clear();
    }

    @Override
    public void rescan() {
        Util.runOnUI(new Runnable() {

            @Override
            public void run() {
                mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
            }
        });
    }

    @Override
    public void addListener(DiscoveryProviderListener listener) {
        serviceListeners.add(listener);
    }

    @Override
    public void removeListener(DiscoveryProviderListener listener) {
        serviceListeners.remove(listener);
    }

    @Override
    public void addDeviceFilter(DiscoveryFilter filter) {}

    @Override
    public void removeDeviceFilter(DiscoveryFilter filter) {}

    @Override
    public void setFilters(java.util.List<DiscoveryFilter> filters) {};

    @Override
    public boolean isEmpty() {
        return false;
    }

    private class MediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteAdded(MediaRouter router, RouteInfo route) {
            super.onRouteAdded(router, route);

            FlintDevice flintDevice = FlintDevice.getFromBundle(route.getExtras());
            String uuid = flintDevice.getDeviceId();

            ServiceDescription foundService = foundServices.get(uuid);

            boolean isNew = foundService == null;
            boolean listUpdateFlag = false;

            if (isNew) {
                foundService = new FlintServiceDescription(FlintService.ID, uuid, flintDevice.getIpAddress().getHostAddress(), flintDevice);
                foundService.setFriendlyName(flintDevice.getFriendlyName());
                foundService.setModelName(flintDevice.getModelName());
                foundService.setModelNumber(flintDevice.getDeviceVersion());
                foundService.setModelDescription(route.getDescription());
                foundService.setPort(flintDevice.getServicePort());
                foundService.setServiceID(FlintService.ID);

                listUpdateFlag = true;
            }
            else {
                if (!foundService.getFriendlyName().equals(flintDevice.getFriendlyName())) {
                    foundService.setFriendlyName(flintDevice.getFriendlyName());
                    listUpdateFlag = true;
                }

                ((FlintServiceDescription)foundService).setFlintDevice(flintDevice);
            }

            if (foundService != null)
                foundService.setLastDetection(new Date().getTime());

            foundServices.put(uuid, foundService);

            if (listUpdateFlag) {
                for (DiscoveryProviderListener listenter: serviceListeners) {
                    listenter.onServiceAdded(FlintDiscoveryProvider.this, foundService);
                }
            }
        }

        @Override
        public void onRouteChanged(MediaRouter router, RouteInfo route) {
            super.onRouteChanged(router, route);

            FlintDevice flintDevice = FlintDevice.getFromBundle(route.getExtras());
            String uuid = flintDevice.getDeviceId();

            ServiceDescription foundService = foundServices.get(uuid);

            boolean isNew = foundService == null;
            boolean listUpdateFlag = false;

            if (!isNew) {
                foundService.setIpAddress(flintDevice.getIpAddress().getHostAddress());
                foundService.setModelName(flintDevice.getModelName());
                foundService.setModelNumber(flintDevice.getDeviceVersion());
                foundService.setModelDescription(route.getDescription());
                foundService.setPort(flintDevice.getServicePort());
                ((FlintServiceDescription)foundService).setFlintDevice(flintDevice);

                if (!foundService.getFriendlyName().equals(flintDevice.getFriendlyName())) {
                    foundService.setFriendlyName(flintDevice.getFriendlyName());
                    listUpdateFlag = true;
                }

                foundService.setLastDetection(new Date().getTime());

                foundServices.put(uuid, foundService);

                if (listUpdateFlag) {
                    for (DiscoveryProviderListener listenter: serviceListeners) {
                        listenter.onServiceAdded(FlintDiscoveryProvider.this, foundService);
                    }
                }
            }
        }

        @Override
        public void onRoutePresentationDisplayChanged(MediaRouter router,
                RouteInfo route) {
            Log.d(Util.T, "onRoutePresentationDisplayChanged: [" + route.getName() + "] [" + route.getDescription() + "]");
            super.onRoutePresentationDisplayChanged(router, route);
        }

        @Override
        public void onRouteRemoved(MediaRouter router, RouteInfo route) {
            super.onRouteRemoved(router, route);
            FlintDevice flintDevice = FlintDevice.getFromBundle(route.getExtras());
            String key = flintDevice.getDeviceId();
            
                final ServiceDescription service = foundServices.get(key);

                if (service != null) {
                    Util.runOnUI(new Runnable() {

                        @Override
                        public void run() {
                            for (DiscoveryProviderListener listener : serviceListeners) {
                                listener.onServiceRemoved(FlintDiscoveryProvider.this, service);
                            }
                        }
                    });
                }

                if (foundServices.containsKey(key))
                    foundServices.remove(key);
            
        }

        @Override
        public void onRouteVolumeChanged(MediaRouter router, RouteInfo route) {
            Log.d(Util.T, "onRouteVolumeChanged: [" + route.getName() + "] [" + route.getDescription() + "]");
            super.onRouteVolumeChanged(router, route);
        }

    }
}
