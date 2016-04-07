package com.crosskr.flint.webview.browser;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FxService extends Service {

    private static String msVideoUrl = "";

    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    WindowManager mWindowManager;

    Button mFloatView;

    Button mStopView;

    TextView mVideoUrlTextView;

    private static boolean sIsRunning = false;

    private static final String TAG = "FxService";

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        Log.i(TAG, "onCreate");

        createFloatView();

        sIsRunning = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            Log.e(TAG, "onStartCommand: [" + intent.getAction() + "]url["
                    + intent.getStringExtra("url") + "]");
            if (intent.getStringExtra("url") != null) {
                Toast.makeText(getApplicationContext(),
                        intent.getStringExtra("url"), Toast.LENGTH_SHORT)
                        .show();

                mVideoUrlTextView.setText(intent.getStringExtra("url"));
                setVideoUrl(intent.getStringExtra("url"));
            }
        } else {
            Log.e(TAG, "onStartCommand: intent is null!");
        }

        return START_STICKY;
    }

    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(
                getApplication().WINDOW_SERVICE);
        wmParams.type = LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;

        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;

        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout,
                null);
        mWindowManager.addView(mFloatLayout, wmParams);

        Log.i(TAG, "mFloatLayout-->left" + mFloatLayout.getLeft());
        Log.i(TAG, "mFloatLayout-->right" + mFloatLayout.getRight());
        Log.i(TAG, "mFloatLayout-->top" + mFloatLayout.getTop());
        Log.i(TAG, "mFloatLayout-->bottom" + mFloatLayout.getBottom());

        mFloatView = (Button) mFloatLayout.findViewById(R.id.float_id);

        mStopView = (Button) mFloatLayout.findViewById(R.id.stop_float);

        mVideoUrlTextView = (TextView) mFloatLayout
                .findViewById(R.id.flint_video_url);
        mVideoUrlTextView.setText(getVideoUrl());

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth() / 2);
        Log.i(TAG, "Height/2--->" + mFloatView.getMeasuredHeight() / 2);

        mFloatView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                wmParams.x = (int) event.getRawX()
                        - mFloatView.getMeasuredWidth() / 2;
                // Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth()/2);
                Log.i(TAG, "RawX" + event.getRawX());
                Log.i(TAG, "X" + event.getX());
                wmParams.y = (int) event.getRawY()
                        - mFloatView.getMeasuredHeight() / 2 - 80;
                // Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredHeight()/2);
                Log.i(TAG, "RawY" + event.getRawY());
                Log.i(TAG, "Y" + event.getY());
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                return false;
            }
        });

        mFloatView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (getVideoUrl() == null || "".equals(getVideoUrl())) {
                    Toast.makeText(FxService.this,
                            "The video url is empty!ignore!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(FxService.this, getVideoUrl(),
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setClassName("com.crosskr.flint.webview.browser",
                        "com.infthink.miuicastsender.SenderDemo");
                intent.setData(Uri.parse(getVideoUrl()));
                intent.putExtra("mediaTitle", "Hello!");
                startActivity(intent);
            }
        });

        mStopView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                wmParams.x = (int) event.getRawX()
                        - mStopView.getMeasuredWidth() / 2;
                // Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth()/2);
                Log.i(TAG, "RawX" + event.getRawX());
                Log.i(TAG, "X" + event.getX());
                wmParams.y = (int) event.getRawY()
                        - mStopView.getMeasuredHeight() / 2 - 80;
                // Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredHeight()/2);
                Log.i(TAG, "RawY" + event.getRawY());
                Log.i(TAG, "Y" + event.getY());
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                return false;
            }
        });
        mStopView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Intent flingIntent = new Intent(FxService.this, FxService.class);
                stopService(flingIntent);
            }
        });
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.e(TAG, "onDestroy!");

        super.onDestroy();
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }

        sIsRunning = false;
    }

    /**
     * Set video url
     * 
     * @param url
     */
    public static void setVideoUrl(String url) {
        msVideoUrl = url;
    }

    /**
     * Get Video Url
     * 
     * @return
     */
    public static String getVideoUrl() {
        return msVideoUrl;
    }

    public static boolean isRunning() {
        return sIsRunning;
    }

}
