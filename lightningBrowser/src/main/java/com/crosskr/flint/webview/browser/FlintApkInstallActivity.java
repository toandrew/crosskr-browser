package com.crosskr.flint.webview.browser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.connectsdk.core.Util;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.device.ConnectableDeviceListener;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManagerListener;
import com.connectsdk.discovery.DiscoveryProvider;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.DeviceService.PairingType;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.sessions.WebAppSession;
import com.connectsdk.service.sessions.WebAppSessionListener;
import com.github.amlcurran.showcaseview.ShowcaseView;

public class FlintApkInstallActivity extends FragmentActivity implements
        DiscoveryManagerListener {
    private static final String TAG = "FlintApkInstallActivity";

    private static final String FLINT_DEFAULT_INSTALL_APK_APP_URL = "http://openflint.github.io/install-app/index.html";

    private static final int MSG_UPDATE_DEVICE_LIST = 1;

    private static final String CMD_INSTALL = "install";

    private static final String CMD_CANCEL_INSTALL = "cancel_install";

    private ShowcaseView mShowcaseView;

    ListView mDeviceListView;

    FlintApkInstallDeviceAdapter mDeviceAdapter;

    private ArrayList<ConnectableDevice> mDeviceList = new ArrayList<ConnectableDevice>();

    private Handler mHandler;

    private boolean mQuit = false;

    private ConnectableDevice mCurrentDevice;

    private String mInstallApkUrl = null;

    private String mInstallApkName = "";

    private String mInstallPercent = "";

    private WebAppSession mWebAppSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.install_apk_list);

        mDeviceListView = (ListView) findViewById(R.id.custom_apk_list_view);

        DiscoveryManager.getInstance().addListener(this);

        try {
            DiscoveryManager
                    .getInstance()
                    .registerDeviceService(
                            (Class<DeviceService>) Class
                                    .forName("com.connectsdk.service.FlintService"),
                            (Class<DiscoveryProvider>) Class
                                    .forName("com.connectsdk.discovery.provider.FlintDiscoveryProvider"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        DiscoveryManager.getInstance().start();

        mDeviceAdapter = new FlintApkInstallDeviceAdapter(this, mDeviceList);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_UPDATE_DEVICE_LIST:
                    mDeviceAdapter.notifyDataSetChanged();
                    break;
                }
            }
        };

        mDeviceListView.setAdapter(mDeviceAdapter);
        mDeviceListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                // TODO Auto-generated method stub
                ConnectableDevice selectedDevice = (ConnectableDevice) arg0
                        .getItemAtPosition(arg2);

                if ((selectedDevice == mCurrentDevice)
                        && mWebAppSession != null) {
                    Toast.makeText(FlintApkInstallActivity.this,
                            getResources()
                                    .getString(R.string.cancel_apk_installing),
                            Toast.LENGTH_SHORT).show();
                    
                    sendApkCmd(CMD_CANCEL_INSTALL, mInstallApkUrl);
                    return;
                }

                if (mWebAppSession != null || mCurrentDevice != null) {
                    cleanup();
                }
                
                Toast.makeText(FlintApkInstallActivity.this,
                        getResources()
                                .getString(R.string.apk_installing),
                        Toast.LENGTH_SHORT).show();
                
                mCurrentDevice = selectedDevice;

                mCurrentDevice.addListener(mDeviceListener);
                mCurrentDevice.connect();
            }
        });

        Intent intent = getIntent();
        Log.e(TAG, "intent:" + intent);
        if (intent != null && intent.getAction() != null
                && (intent.getAction().equals(Intent.ACTION_VIEW))) {
            Uri videoURI = intent.getData();
            if (videoURI != null) {
                mInstallApkUrl = videoURI.toString();
                Log.e(TAG, "mInstallApkUrl:" + mInstallApkUrl);
            }

            mInstallApkName = intent.getStringExtra("mediaTitle");
        }
    }

    @Override
    protected void onDestroy() {
        DiscoveryManager.getInstance().stop();

        mQuit = true;

        super.onDestroy();
    }

    public String getInstallApkName() {
        return mInstallApkName != null ? mInstallApkName : "";
    }

    public String getCurrentInstallPercent() {
        return mInstallPercent;
    }

    @Override
    public void onDeviceAdded(DiscoveryManager manager,
            final ConnectableDevice device) {
        // TODO Auto-generated method stub
        Util.runOnUI(new Runnable() {
            @Override
            public void run() {
                int index = -1;

                Log.e(TAG,
                        "count[" + mDeviceList.size() + "]["
                                + device.getFriendlyName() + "]ip["
                                + device.getIpAddress() + "]");

                if (device.getServiceByName("Matchstick") == null
                        || !device.getFriendlyName().startsWith(
                                "MatchStick-Android")
                        || (device.getFriendlyName().startsWith(
                                "MatchStick-Android") && (device
                                .getFriendlyName().endsWith("-DLNA") || device
                                .getFriendlyName().endsWith("-AirPlay")))) {
                    return;
                }

                for (int i = 0; i < mDeviceList.size(); i++) {
                    ConnectableDevice d = mDeviceList.get(i);

                    String newDeviceName = device.getFriendlyName();
                    String dName = d.getFriendlyName();

                    if (newDeviceName == null) {
                        newDeviceName = device.getModelName();
                    }

                    if (dName == null) {
                        dName = d.getModelName();
                    }

                    Log.e(TAG, "onDeviceAdded: newDeviceName[" + newDeviceName
                            + "]dName[" + dName + "]");

                    if (d.getIpAddress().equals(device.getIpAddress())
                            && newDeviceName.equals(dName)) {
                        Log.e(TAG, "dName:" + dName + " newDeviceName:"
                                + newDeviceName);
                        mDeviceList.remove(d);
                        mDeviceList.add(i, device);
                        return;
                    }

                    if (newDeviceName.compareToIgnoreCase(dName) < 0) {
                        index = i;
                        mDeviceList.add(index, device);
                        break;
                    }
                }

                if (index == -1) {
                    mDeviceList.add(device);
                }

                mDeviceAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDeviceUpdated(DiscoveryManager manager,
            ConnectableDevice device) {
        // TODO Auto-generated method stub
        Util.runOnUI(new Runnable() {
            @Override
            public void run() {
                mDeviceAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDeviceRemoved(DiscoveryManager manager,
            final ConnectableDevice device) {
        // TODO Auto-generated method stub
        Util.runOnUI(new Runnable() {
            @Override
            public void run() {
                mDeviceList.remove(device);

                mDeviceAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDiscoveryFailed(DiscoveryManager manager,
            ServiceCommandError error) {
        // TODO Auto-generated method stub

        Util.runOnUI(new Runnable() {
            @Override
            public void run() {
                mDeviceList.clear();

                mDeviceAdapter.notifyDataSetChanged();
            }
        });
    }

    private void sendApkCmd(String cmd, final String url) {
        Log.e("httpd", "sendApkCmd:" + url);
        if (url == null) {
            return;
        }

        JSONObject obj = new JSONObject();
        try {
            obj.put("cmd", cmd);
            obj.put("url", url);

            sendMessages(obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanup() {
        try {
            if (mWebAppSession != null) {
                mWebAppSession.disconnectFromWebApp();
                mWebAppSession = null;
            }

            if (mCurrentDevice != null) {
                mCurrentDevice.removeListener(mDeviceListener);
                mCurrentDevice.disconnect();
                mCurrentDevice = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessages(final String message) {
        mWebAppSession.sendMessage(message, new ResponseListener<Object>() {

            @Override
            public void onError(ServiceCommandError error) {
                Log.e(TAG, "Could not send message, disconnecting...");
                cleanup();
            }

            @Override
            public void onSuccess(Object object) {
                Log.e(TAG, "Message \"" + message + "\" sent successfully.");
            }
        });
    }

    private ConnectableDeviceListener mDeviceListener = new ConnectableDeviceListener() {

        @Override
        public void onPairingRequired(ConnectableDevice device,
                DeviceService service, PairingType pairingType) {
            // since we haven't enabled pairing, we don't need to solve for this
            // case
        }

        @Override
        public void onDeviceReady(ConnectableDevice device) {
            Log.e(TAG, "Connected to device " + device.getFriendlyName());

            String webAppId = null;

            if (device.getServiceByName("Matchstick") != null) {
                webAppId = FLINT_DEFAULT_INSTALL_APK_APP_URL;
            }

            if (webAppId != null) {
                Log.e(TAG, "Launching web app with id " + webAppId);
                device.getWebAppLauncher().launchWebApp(webAppId,
                        mWebAppLaunchListener);
            }
        }

        @Override
        public void onDeviceDisconnected(ConnectableDevice device) {
            Log.e(TAG, "Disconnected from device");
            cleanup();
        }

        @Override
        public void onConnectionFailed(ConnectableDevice device,
                ServiceCommandError error) {
            Log.e(TAG,
                    "Could not connect to device "
                            + error.getLocalizedMessage());
            cleanup();
        }

        @Override
        public void onCapabilityUpdated(ConnectableDevice device,
                List<String> added, List<String> removed) {
            // we can ignore this case
        }
    };

    private WebAppSessionListener mWebAppSessionListener = new WebAppSessionListener() {

        @Override
        public void onReceiveMessage(WebAppSession webAppSession, Object message) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onReceiveMessage(" + message.toString() + ")");

            try {
                JSONObject obj = new JSONObject(message.toString());
                String msg = obj.getString("msg");
                
                mInstallPercent = obj.getString("value");

                // in UI thread?
                mDeviceAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onWebAppSessionDisconnect(WebAppSession webAppSession) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onWebAppSessionDisconnect()");
            
            cleanup();
            
            mDeviceAdapter.notifyDataSetChanged();
        }

    };

    private WebAppSession.LaunchListener mWebAppLaunchListener = new WebAppSession.LaunchListener() {

        @Override
        public void onError(ServiceCommandError error) {
            Log.e(TAG,
                    "Web app could not be launched: "
                            + error.getLocalizedMessage());
            cleanup();
        }

        @Override
        public void onSuccess(WebAppSession webAppSession) {
            Log.e(TAG, "Web app launch successful, connecting... ");

            mWebAppSession = webAppSession;
            mWebAppSession.connect(mWebAppConnectListener);
            mWebAppSession.setWebAppSessionListener(mWebAppSessionListener);
        }
    };

    private ResponseListener<Object> mWebAppConnectListener = new ResponseListener<Object>() {
        public void onSuccess(Object object) {
            Log.e(TAG, "Web app connected!");

            sendApkCmd(CMD_INSTALL, mInstallApkUrl);
        };

        @Override
        public void onError(ServiceCommandError error) {
            Log.e(TAG,
                    "Web app could not be connected: "
                            + error.getLocalizedMessage());
            cleanup();
        }
    };
}
