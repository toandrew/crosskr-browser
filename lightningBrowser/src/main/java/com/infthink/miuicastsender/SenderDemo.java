package com.infthink.miuicastsender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.connectsdk.service.capability.MediaControl.PlayStateStatus;
import com.crosskr.flint.webview.browser.BrowserApp;
import com.crosskr.flint.webview.browser.FlintBaseActivity;
import com.crosskr.flint.webview.browser.FlintStatusChangeListener;
import com.crosskr.flint.webview.browser.FlintVideoManager;
import com.crosskr.flint.webview.browser.FxService;
import com.crosskr.flint.webview.browser.R;
import com.nanohttpd.webserver.src.main.java.fi.iki.elonen.SimpleWebServer;

public class SenderDemo extends FlintBaseActivity implements
        FlintStatusChangeListener {
    private final String TAG = "SenderDemo";

    private final static String ACTION_DUOKAN_VIDEOPLAY = "duokan.intent.action.VIDEO_PLAY";
    private static final int REFRESH_INTERVAL_MS = (int) TimeUnit.SECONDS
            .toMillis(1);

    private static final int PLAYER_STATE_NONE = 0;
    private static final int PLAYER_STATE_PLAYING = 1;
    private static final int PLAYER_STATE_PAUSED = 2;
    private static final int PLAYER_STATE_BUFFERING = 3;
    private static final int PLAYER_STATE_FINISHED = 4;

    private int mPlayerState = PLAYER_STATE_NONE;

    private static final int MSG_ID_REFRESH_DEV_LIST = 1;

    private ImageButton mBtnPlay;
    private ImageButton mBtnVolumeUp;
    private ImageButton mBtnVolumeDown;
    private TextView mTextViewName;
    private TextView mTextViewDescription;
    private TextView mTextViewCurtime;
    private TextView mTextViewDuration;
    private ImageView mImageVideo;

    private BrowserApp mApplication;
    private SeekBar mSeekbar;

    private Handler mHandler;

    private Intent mIntent = null;
    private AlertDialog mAlertDialog = null;

    private Animation mAnimation = null;
    LinearInterpolator mLin = new LinearInterpolator();

    private String mVideoId = "";

    private NotificationManager mNotificationManager;
    private String mIpAddress;

    private SimpleWebServer mNanoHTTPD;
    private int port = 8080;
    private String mRootDir = "/";

    private Timer mSearchTimer;

    private boolean mResumePlay;
    private boolean mIsUserSeeking;

    private boolean mSeeking;
    private boolean mFirst = true;

    private Runnable mRefreshRunnable;
    private boolean mWaitingForReconnect;

    private Uri mUri;
    private String mTitle;

    // used to send cust messages.
    // private FlintMsgChannel mFlintMsgChannel;

    private CheckBox mHardwareDecoderCheckbox;

    private boolean mIsHardwareDecoder = true;

    private FlintVideoManager mFlintVideoManager;

    //private String mCurrentVideoUrl;

    private MenuItem mMediaRouteMenuItem;
    
    private boolean mQuit = false;

    private String processLocalVideoUrl(String url) {
        String real_url = url;
        if (url != null && url.startsWith("file://")) {

            initWebserver();
            // remove "file://"
            real_url = url.replaceAll("file://", "");
            return "http://" + mIpAddress + ":8080" + real_url;

        }
        return url;
    }

    private static final String APPLICATION_ID = "~flintplayer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        
        setContentView(R.layout.video_details);

        Intent flingIntent = new Intent(SenderDemo.this, FxService.class);
        stopService(flingIntent);

        mApplication = (BrowserApp) this.getApplicationContext();

        mFlintVideoManager = new FlintVideoManager(this, APPLICATION_ID, this);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_ID_REFRESH_DEV_LIST:
                    refreshDevListButtonStatus();
                    break;
                }
            }
        };

        Intent intent = getIntent();
        Log.e(TAG, "intent:" + intent);
        if (intent != null
                && intent.getAction() != null
                && (intent.getAction().equals(ACTION_DUOKAN_VIDEOPLAY) || intent
                        .getAction().equals(Intent.ACTION_VIEW))) {
            Uri videoURI = intent.getData();
            mVideoId = processLocalVideoUrl(videoURI.toString());
            String curId = mApplication.getVideoId();
            if (mVideoId.equals(curId)) {
                mResumePlay = true;
            }
            android.util.Log.d(TAG, "videoURI = " + mVideoId);
            android.util.Log.d(TAG, "curId = " + curId);

            StringBuffer sb = new StringBuffer();
            String mediaTitle = intent.getStringExtra("mediaTitle");
            
            mTitle = mediaTitle;
            
            sb.append(mediaTitle);
            int availableEpisodeCount = intent.getIntExtra(
                    "available_episode_count", 0);
            if (availableEpisodeCount > 0) {
                int currentEpisode = intent.getIntExtra("current_episode", 1);
                sb.append("第 " + currentEpisode + " 集");
            }
            String name = sb.toString();
            intent.putExtra("vname", name);
            intent.putExtra("type", "net");
            intent.putExtra("url", mVideoId);

            setCurrentVideoUrl(mVideoId);
            setCurrentVideoTitle(mTitle);
        } else {
            mResumePlay = true;
        }

        initView();

        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (mQuit) {
                    return;
                }
                
                onRefreshEvent();
                startRefreshTimer();
            }
        };

        mTextViewName.setText(mIntent.getStringExtra("vname"));
        mTextViewDescription.setText(mIntent.getStringExtra("vdes"));
        // imgvideo.setImageResource(mIntent.getExtras().getInt("vicon"));
        mImageVideo.setVisibility(View.GONE);
        mBtnPlay.setClickable(false);
        mBtnPlay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPlayerState == PLAYER_STATE_FINISHED) {
                    mFlintVideoManager.playVideo(getCurrentVideoUrl(), "");
                } else if (mPlayerState == PLAYER_STATE_PAUSED
                        || mPlayerState == PLAYER_STATE_BUFFERING) {
                    onPlayClicked();
                } else if (mPlayerState == PLAYER_STATE_PLAYING) {
                    onPauseClicked();
                } else {
                    Log.e(TAG, "ignore for player state:" + mPlayerState);
                }
            }
        });

        mBtnVolumeUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setVolumeUp();
            }

        });

        mBtnVolumeDown.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setVolumeDown();
            }

        });

        mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIsUserSeeking = false;
                mSeekbar.setSecondaryProgress(0);
                onSeekBarMoved(TimeUnit.SECONDS.toMillis(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsUserSeeking = true;
                mSeekbar.setSecondaryProgress(seekBar.getProgress());
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                if (fromUser) {
                    mTextViewCurtime.setText(formatTime(progress * 1000));
                }
            }
        });

        mHardwareDecoderCheckbox = (CheckBox) findViewById(R.id.device_hardware_decoder);
        mHardwareDecoderCheckbox.setVisibility(View.GONE);
        mHardwareDecoderCheckbox
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        // TODO Auto-generated method stub

                        Log.e(TAG, "setHardwareDecoder:" + isChecked);
                    }

                });
        
        mFlintVideoManager.onStart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart!");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.e(TAG, "onStop!");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            setVolumeDown();
            return true;
        case KeyEvent.KEYCODE_VOLUME_UP:
            setVolumeUp();
            return true;
        default:
            break;
        }
        return super.dispatchKeyEvent(event);
    }

    private void setVolumeUp() {
        onVolumeChange(0.1);
    }

    private void setVolumeDown() {
        onVolumeChange(-0.1);
    }

    private void initWebserver() {
        startServer(8080);
    }

    private void initView() {

        mIntent = getIntent();

        // btncast = (ImageButton) findViewById(R.id.btncast);
        mBtnPlay = (ImageButton) findViewById(R.id.btnplay);
        mBtnVolumeUp = (ImageButton) findViewById(R.id.btnpre);
        mBtnVolumeDown = (ImageButton) findViewById(R.id.btnnext);
        mTextViewName = (TextView) findViewById(R.id.tvname);
        // tvdescription = (TextView) findViewById(R.id.tvdescription);
        mTextViewDescription = new TextView(this);
        // imgvideo = (ImageView) findViewById(R.id.imgvideo);
        mImageVideo = new ImageView(this);
        mSeekbar = (SeekBar) findViewById(R.id.videoprogress);
        mTextViewCurtime = (TextView) findViewById(R.id.tvcurtime);
        mTextViewDuration = (TextView) findViewById(R.id.tvduration);
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.play_anim);
        mAnimation.setInterpolator(mLin);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "------onCreateOptionsMenu()------");
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        Log.d(TAG, "------onCreatePanelMenu()------");
        getMenuInflater().inflate(R.menu.action_bar_devices, menu);
        mMediaRouteMenuItem = menu.findItem(R.id.flint_devices);

        refreshDevListButtonStatus();

        mSearchTimer = new Timer();
        mSearchTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(MSG_ID_REFRESH_DEV_LIST);
            }
        }, 100, 3000);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.flint_devices:
            mFlintVideoManager.doMediaRouteButtonClicked();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        String videoName = "";

        if (mFlintVideoManager != null && mFlintVideoManager.isMediaConnected()) {
            videoName = getCurrentVideoTitle();
        }
        if (videoName.length() > 0) {
            NotificationManager notificationManager = (NotificationManager) this
                    .getSystemService(android.content.Context.NOTIFICATION_SERVICE);
            String appName = this.getResources().getString(
                    R.string.cast_application_name);
//            Notification notification = new Notification(
//                    R.drawable.ic_launcher, appName, System.currentTimeMillis());
//            // notification.flags |= Notification.FLAG_ONGOING_EVENT;
//            // notification.flags |= Notification.FLAG_NO_CLEAR;
//            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
//            CharSequence contentTitle = appName;
//            CharSequence contentText = "正在播放: " + videoName;

            Intent notificationIntent = new Intent(SenderDemo.this,
                    SenderDemo.class);
            notificationIntent.putExtra("vname", videoName);
            notificationIntent.setData(Uri.parse(mVideoId));
            PendingIntent contentItent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);
//            notification.setLatestEventInfo(this, contentTitle, contentText,
//                    contentItent);

            Notification notification = new Notification.Builder(SenderDemo.this)
                    .setAutoCancel(true)
                    .setContentTitle(appName)
                    .setContentText("正在播放: " + videoName)
                    .setContentIntent(contentItent)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .build();

            notificationManager.notify(0, notification);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy!");
        
        mQuit = true;
        
        if (mSearchTimer != null) {
            mSearchTimer.cancel();
            mSearchTimer = null;
        }

        mHandler.removeMessages(MSG_ID_REFRESH_DEV_LIST);
        
        mNotificationManager.cancelAll();
        
        mFlintVideoManager.onStop();
        
        stopServer();
        super.onDestroy();
    }

    public static String intToIp(int i) {
        return ((i) & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    private void startServer(int port) {
        try {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            mIpAddress = intToIp(wifiInfo.getIpAddress());

            if (wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(
                                "Please connect to a WIFI-network for starting the webserver.")
                        .setPositiveButton("OK", null).show();
                throw new Exception("Please connect to a WIFI-network.");
            }

            Log.e(TAG, "Starting server " + mIpAddress + ":" + port + ".");

            List<File> rootDirs = new ArrayList<File>();
            boolean quiet = false;
            Map<String, String> options = new HashMap<String, String>();
            rootDirs.add(new File(mRootDir).getAbsoluteFile());

            // mNanoHTTPD
            try {
                mNanoHTTPD = new SimpleWebServer(mIpAddress, port, rootDirs,
                        quiet);
                mNanoHTTPD.start();
            } catch (IOException ioe) {
                Log.e(TAG, "Couldn't start server:\n" + ioe);
            }

            Intent i = new Intent(this, SenderDemo.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
                    0);

//            Notification notif = new Notification(R.drawable.ic_launcher,
//                    "Webserver is running:" + mIpAddress + ":" + port,
//                    System.currentTimeMillis());
//            notif.setLatestEventInfo(this, "Webserver", "Webserver is running:"
//                    + mIpAddress + ":" + port, contentIntent);
//            notif.flags = Notification.FLAG_NO_CLEAR;

            Notification notif = new Notification.Builder(this)
                    .setAutoCancel(true)
                    .setContentTitle("Webserver")
                    .setContentText("Webserver is running:" + mIpAddress + ":" + port)
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .build();

            mNotificationManager.notify(1234, notif);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void stopServer() {
        if (mNanoHTTPD != null) {
            mNanoHTTPD.stop();
            Log.e(TAG, "Server was killed.");
            mNotificationManager.cancelAll();
        } else {
            Log.e(TAG, "Cannot kill server!? Please restart your phone.");
        }
    }

    /**
     * Set current playback's position
     * 
     * @param position
     * @param duration
     */
    private void refreshPlaybackPosition(long position, long duration) {
        if (!mIsUserSeeking) {
            if (position == 0) {
                mTextViewCurtime.setText("00:00");
                mSeekbar.setProgress(0);
            } else if (position > 0) {
                mSeekbar.setProgress((int) TimeUnit.MILLISECONDS
                        .toSeconds(position));
            }
            mTextViewCurtime.setText(formatTime(position));
        }

        if (duration == 0) {
            mTextViewDuration.setText("00:00");
            mSeekbar.setMax(0);
        } else if (duration > 0) {
            mTextViewDuration.setText(formatTime(duration));
            if (!mIsUserSeeking) {
                mSeekbar.setMax((int) TimeUnit.MILLISECONDS.toSeconds(duration));
            }
        }
    }

    /**
     * stop timer to stop refresh current playback's UI.
     */
    private void cancelRefreshTimer() {
        mHandler.removeCallbacks(mRefreshRunnable);
    }

    /**
     * start timer to update current playback's UI.
     */
    private void startRefreshTimer() {
        mHandler.postDelayed(mRefreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    public void onDeviceSelected(String name) {
        // TODO Auto-generated method stub

        if (getCurrentVideoUrl() == null) {
            Log.d(TAG, "url is " + getCurrentVideoUrl() + " ignore it!");
            Toast.makeText(this, "url is null!ignore it!", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        
        mFlintVideoManager.playVideo(getCurrentVideoUrl(),
                getCurrentVideoTitle());

        updateButtonStates();

        // show device info
        updateFlingDispInfo(true);

        refreshDevListButtonStatus();
    }

    @Override
    public void onDeviceUnselected() {
        // TODO Auto-generated method stub

        Log.e(TAG, "onDeviceUnselected!");

        cancelRefreshTimer();

        clearMediaState();
        updateButtonStates();

        refreshDevListButtonStatus();
    }

    @Override
    public void onApplicationDisconnected() {
        // TODO Auto-generated method stub

        clearMediaState();
        updateButtonStates();
    }

    @Override
    public void onConnectionFailed() {
        // TODO Auto-generated method stub

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateButtonStates();
                clearMediaState();
                cancelRefreshTimer();
            }
        });
    }
    
    @Override
    public void onApplicationConnectionResult(String applicationStatus) {
        // TODO Auto-generated method stub

        startRefreshTimer();
    }
    
    @Override
    public void onMediaSeekEnd() {
        // TODO Auto-generated method stub

        mSeeking = false;
    }

    /**
     * clear media status when application disconnected.
     */
    private void clearMediaState() {
        mSeeking = false;

        refreshPlaybackPosition(0, 0);
    }

    /**
     * update flint views when device connected.
     * 
     * @param show
     */
    private void updateFlingDispInfo(boolean show) {
    }

    /**
     * Update all views according to current application status.
     */
    private void updateButtonStates() {
        boolean hasMediaConnection = mFlintVideoManager.isMediaConnected();

        if (hasMediaConnection) {
            PlayStateStatus mediaStatus = mFlintVideoManager.getMediaStatus();
            Log.e(TAG, "mediaStatus:" + mediaStatus);
            if (mediaStatus != null) {
                int playerState = PLAYER_STATE_NONE;
                if (mediaStatus == PlayStateStatus.Paused) {
                    playerState = PLAYER_STATE_PAUSED;
                } else if (mediaStatus == PlayStateStatus.Playing) {
                    playerState = PLAYER_STATE_PLAYING;
                } else if (mediaStatus == PlayStateStatus.Buffering) {
                    playerState = PLAYER_STATE_BUFFERING;
                } else if (mediaStatus == PlayStateStatus.Finished) {
                    Log.e(TAG, "updateButtonStates:Idle");
                    playerState = PLAYER_STATE_FINISHED;

                    mSeeking = false;

                    refreshPlaybackPosition(0,
                            mFlintVideoManager.getMediaDuration());
                }
                setPlayerState(playerState);

                updateFlingDispInfo(true);

                setSeekBarEnabled(playerState != PLAYER_STATE_FINISHED
                        && playerState != PLAYER_STATE_NONE);
            }
        } else {
            setPlayerState(PLAYER_STATE_NONE);

            updateFlingDispInfo(false);

            setSeekBarEnabled(false);

            clearMediaState();
        }
    }

    /**
     * update current playback's position
     */
    private void updatePlaybackPosition() {
        refreshPlaybackPosition(mFlintVideoManager.getMediaCurrentTime(),
                mFlintVideoManager.getMediaDuration());
    }

    /**
     * PLAY
     */
    private void onPlayClicked() {
        mFlintVideoManager.playMedia();
    }

    /**
     * PAUSE
     */
    protected void onPauseClicked() {
        mFlintVideoManager.pauseMedia();
    }

    /**
     * SEEK
     * 
     * @param position
     */
    protected void onSeekBarMoved(long position) {
        refreshPlaybackPosition(position, -1);

        mSeeking = true;

        mFlintVideoManager.seekMedia(position);
    }

    /**
     * Called in UI timer thread to update current UI views.
     */
    private void onRefreshEvent() {
        if (!mSeeking) {
            updatePlaybackPosition();
        }

        updateButtonStates();
    }

    /**
     * Set current player's status and update play/pause button status.
     * 
     * @param playerState
     */
    protected final void setPlayerState(int playerState) {
        mPlayerState = playerState;
        if (mPlayerState == PLAYER_STATE_PAUSED) {
            mBtnPlay.setBackgroundResource(R.drawable.btn_play);
        } else if (mPlayerState == PLAYER_STATE_PLAYING) {
            mBtnPlay.setBackgroundResource(R.drawable.btn_pause);
        } else {
            mBtnPlay.setBackgroundResource(R.drawable.btn_play);
        }

        mBtnPlay.setEnabled((mPlayerState == PLAYER_STATE_PAUSED)
                || (mPlayerState == PLAYER_STATE_PLAYING)
                || mPlayerState == PLAYER_STATE_FINISHED
                || mPlayerState == PLAYER_STATE_BUFFERING);
    }

    /**
     * whether enable seek bar.
     * 
     * @param enabled
     */
    protected final void setSeekBarEnabled(boolean enabled) {
        mSeekbar.setEnabled(enabled);
    }

    /**
     * Get time by string format.
     * 
     * @param millisec
     * @return
     */
    private String formatTime(long millisec) {
        int seconds = (int) (millisec / 1000);
        int hours = seconds / (60 * 60);
        seconds %= (60 * 60);
        int minutes = seconds / 60;
        seconds %= 60;

        String time;
        if (hours > 0) {
            time = String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            time = String.format("%d:%02d", minutes, seconds);
        }
        return time;
    }

    /**
     * Called when volume changed.
     * 
     * @param volumeIncrement
     */
    private void onVolumeChange(double volumeIncrement) {
        Log.e(TAG, "volumeIncrement:" + volumeIncrement);

        try {
            double v = mFlintVideoManager.getMediaVolume();

            Log.e(TAG, "volumeIncrement:" + volumeIncrement + " v[" + v + "]");
            v += volumeIncrement;
            if (v > 1.0) {
                v = 1.0;
            } else if (v < 0) {
                v = 0.0;
            }

            mFlintVideoManager.setMediaVolume(v);

        } catch (Exception e) {
            // showErrorDialog(e.getMessage());
        }
    }

    /**
     * refresh current time
     * 
     * @param position
     * @param duration
     */
    protected final void refreshSeekPosition(long position, long duration) {
        mTextViewCurtime.setText(formatTime(position));
    }

    /**
     * Set custom message to device. let device use hardware decoder or not
     * 
     * @param flag
     */
    private void setHardwareDecoder(boolean flag) {
    }

    /**
     * Set dev list menu item's icon
     * 
     * @param connectStatus
     */
    private void setMenuItemIcon(boolean connectStatus) {
        if (mMediaRouteMenuItem != null) {
            if (connectStatus) {
                mMediaRouteMenuItem
                        .setIcon(R.drawable.mr_ic_media_route_on_holo_dark);
            } else {
                mMediaRouteMenuItem
                        .setIcon(R.drawable.mr_ic_media_route_off_holo_dark);
            }
        }
    }

    /**
     * refresh current device list menu button's status.
     */
    private void refreshDevListButtonStatus() {
        if (mFlintVideoManager != null
                && mFlintVideoManager.getDeviceSize() > 0) {
            mMediaRouteMenuItem.setVisible(true);
        } else {
            mMediaRouteMenuItem.setVisible(false);
        }

        if (mFlintVideoManager != null && mFlintVideoManager.isMediaConnected()) {
            setMenuItemIcon(true);
        } else {
            setMenuItemIcon(false);
        }
    }
}
