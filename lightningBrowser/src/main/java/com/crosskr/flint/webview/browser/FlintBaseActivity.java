package com.crosskr.flint.webview.browser;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class FlintBaseActivity extends FragmentActivity {

    private String mCurrentVideoUrl;

    private String mCurrentVideoTitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Set current video url
     */
    public void setCurrentVideoUrl(String url) {
        mCurrentVideoUrl = url;
    }

    /**
     * Get current video url
     * 
     * @return
     */
    public String getCurrentVideoUrl() {
        return mCurrentVideoUrl;
    }

    /**
     * Set current video title
     */
    public void setCurrentVideoTitle(String title) {
        mCurrentVideoTitle = title;
    }

    /**
     * Get current video title
     * 
     * @return
     */
    public String getCurrentVideoTitle() {
        return mCurrentVideoTitle;
    }
}
