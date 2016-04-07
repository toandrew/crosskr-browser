package com.crosskr.flint.webview.browser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

public class MainActivity extends BrowserActivity {

	SharedPreferences mPreferences;

	CookieManager mCookieManager;

	Toast mToast;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(PreferenceConstants.PREFERENCES, 0);
	}

	@Override
	public void updateCookiePreference() {
		if (mPreferences == null) {
			mPreferences = getSharedPreferences(PreferenceConstants.PREFERENCES, 0);
		}
		mCookieManager = CookieManager.getInstance();
		CookieSyncManager.createInstance(this);
		mCookieManager.setAcceptCookie(mPreferences.getBoolean(PreferenceConstants.COOKIES, true));
		super.updateCookiePreference();
	}

	@Override
	public synchronized void initializeTabs() {
		super.initializeTabs();
		restoreOrNewTab();
		// if incognito mode use newTab(null, true); instead
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleNewIntent(intent);
		super.onNewIntent(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveOpenTabs();
	}

	@Override
	public void updateHistory(String title, String url) {
		super.updateHistory(title, url);
		addItemToHistory(title, url);
	}

	@Override
	public boolean isIncognito() {
		return false;
	}


    long preQuitTime = 0;
    
	@Override
	public void closeActivity() {
        Log.e("MainActivity", "closeActivity!");
        long currentTime = System.currentTimeMillis();
        if (preQuitTime == 0 || Math.abs(currentTime - preQuitTime) > 1000) {
            preQuitTime = System.currentTimeMillis();
            Log.e("MainActivity", "confirm quit?");
            if (mToast == null) {
                mToast = Toast.makeText(getApplicationContext(), "Press again to Quit!", Toast.LENGTH_SHORT);
            }
            mToast.show();
            
            return;
        }
       
		closeDrawers();
	    //moveTaskToBack(true);
		
		// finish it?
		finish();
	}
}
