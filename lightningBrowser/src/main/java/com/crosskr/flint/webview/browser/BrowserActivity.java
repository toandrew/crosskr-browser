/*
 * Copyright 2014 A.C.R. Development
 */

package com.crosskr.flint.webview.browser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewDatabase;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.VideoView;

import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.service.capability.MediaControl.PlayStateStatus;
import com.github.amlcurran.showcaseview.ApiUtils;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

public class BrowserActivity extends FlintBaseActivity implements
        BrowserController, FlintStatusChangeListener {
    private static final String TAG = "BrowserActivity";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListLeft;
    private RelativeLayout mDrawerLeft;
    private LinearLayout mDrawerRight;
    private ListView mDrawerListRight;
    private RelativeLayout mNewTab;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<LightningView> mWebViews = new ArrayList<LightningView>();
    private LightningView mCurrentView;
    private int mIdGenerator;
    private LightningViewAdapter mTitleAdapter;
    private List<HistoryItem> mBookmarkList;
    private BookmarkViewAdapter mBookmarkAdapter;
    private AutoCompleteTextView mSearch;
    private ClickHandler mClickHandler;
    private ProgressBar mProgressBar;
    private boolean mSystemBrowser = false;
    private ValueCallback<Uri> mUploadMessage;
    private View mCustomView;
    private int mOriginalOrientation;
    private int mActionBarSize;
    private ActionBar mActionBar;
    private boolean mFullScreen;
    private FrameLayout mBrowserFrame;
    private FullscreenHolder mFullscreenContainer;
    private CustomViewCallback mCustomViewCallback;
    private final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    private Bitmap mDefaultVideoPoster;
    private View mVideoProgressView;
    private HistoryDatabaseHandler mHistoryHandler;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditPrefs;
    private Context mContext;
    private Bitmap mWebpageBitmap;
    private String mSearchText;
    private Activity mActivity;
    private final int API = android.os.Build.VERSION.SDK_INT;
    private Drawable mDeleteIcon;
    private Drawable mRefreshIcon;
    private Drawable mCopyIcon;
    private Drawable mIcon;
    private int mActionBarSizeDp;
    private int mNumberIconColor;
    private String mHomepage;
    private boolean mIsNewIntent = false;
    private VideoView mVideoView;
    private static SearchAdapter mSearchAdapter;
    private static LayoutParams mMatchParent = new LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    private BookmarkManager mBookmarkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.e(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.e(TAG, "onStop");
    }

    /**
     * Use the followings to chech whether input method is active!
     */
    boolean isKeyBoardOpened = false;

    public void setListenerToRootView() {
        final View activityRootView = getWindow().getDecorView().findViewById(
                R.id.content_frame);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        int heightDiff = activityRootView.getRootView()
                                .getHeight() - activityRootView.getHeight();
                        if (heightDiff > 600) { // 99% of the time the height
                                                // diff will be due to a
                                                // keyboard.
                            // Toast.makeText(getApplicationContext(),
                            // "Gotcha!!! softKeyboardup", 0).show();
                            Log.e(TAG, "keyboard is shown?![" + heightDiff
                                    + "]");
                            if (isKeyBoardOpened == false) {
                                // Do two things, make the view top visible and
                                // the editText smaller
                            }
                            isKeyBoardOpened = true;
                        } else if (isKeyBoardOpened == true) {
                            Log.e(TAG, "keyboard is hidden?![" + heightDiff
                                    + "]");

                            // Toast.makeText(getApplicationContext(),
                            // "softkeyborad Down!!!", 0).show();
                            isKeyBoardOpened = false;
                        }
                    }
                });
    }

    @SuppressWarnings("deprecation")
    private synchronized void initialize() {
        setContentView(R.layout.activity_main);

        setListenerToRootView();

        TypedValue typedValue = new TypedValue();
        Theme theme = getTheme();
        theme.resolveAttribute(R.attr.numberColor, typedValue, true);
        mNumberIconColor = typedValue.data;
        mPreferences = getSharedPreferences(PreferenceConstants.PREFERENCES, 0);
        mEditPrefs = mPreferences.edit();
        mContext = this;
        if (mWebViews != null) {
            mWebViews.clear();
        } else {
            mWebViews = new ArrayList<LightningView>();
        }
        mBookmarkManager = new BookmarkManager(this);
        if (!mPreferences.getBoolean(
                PreferenceConstants.OLD_BOOKMARKS_IMPORTED, false)) {
            List<HistoryItem> old = Utils.getOldBookmarks(this);
            mBookmarkManager.addBookmarkList(old);
            mEditPrefs.putBoolean(PreferenceConstants.OLD_BOOKMARKS_IMPORTED,
                    true).apply();
        }
        mActivity = this;
        mClickHandler = new ClickHandler(this);
        mBrowserFrame = (FrameLayout) findViewById(R.id.content_frame);
        mProgressBar = (ProgressBar) findViewById(R.id.activity_bar);
        mProgressBar.setVisibility(View.GONE);
        mNewTab = (RelativeLayout) findViewById(R.id.new_tab_button);
        mDrawerLeft = (RelativeLayout) findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListLeft = (ListView) findViewById(R.id.left_drawer_list);
        mDrawerListLeft.setDivider(null);
        mDrawerListLeft.setDividerHeight(0);
        mDrawerRight = (LinearLayout) findViewById(R.id.right_drawer);
        mDrawerListRight = (ListView) findViewById(R.id.right_drawer_list);
        mDrawerListRight.setDivider(null);
        mDrawerListRight.setDividerHeight(0);
        setNavigationDrawerWidth();
        mWebpageBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_webpage);
        mActionBar = getActionBar();
        final TypedArray styledAttributes = mContext.getTheme()
                .obtainStyledAttributes(
                        new int[] { android.R.attr.actionBarSize });
        mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        if (pixelsToDp(mActionBarSize) < 48) {
            mActionBarSize = Utils.convertToDensityPixels(mContext, 48);
        }
        mActionBarSizeDp = pixelsToDp(mActionBarSize);
        styledAttributes.recycle();

        mHomepage = mPreferences.getString(PreferenceConstants.HOMEPAGE,
                Constants.HOMEPAGE);

        mTitleAdapter = new LightningViewAdapter(this, R.layout.tab_list_item,
                mWebViews);
        mDrawerListLeft.setAdapter(mTitleAdapter);
        mDrawerListLeft.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerListLeft
                .setOnItemLongClickListener(new DrawerItemLongClickListener());

        mBookmarkList = mBookmarkManager.getBookmarks(true);
        mBookmarkAdapter = new BookmarkViewAdapter(this,
                R.layout.bookmark_list_item, mBookmarkList);
        mDrawerListRight.setAdapter(mBookmarkAdapter);
        mDrawerListRight
                .setOnItemClickListener(new BookmarkItemClickListener());
        mDrawerListRight
                .setOnItemLongClickListener(new BookmarkItemLongClickListener());

        if (mHistoryHandler == null) {
            mHistoryHandler = new HistoryDatabaseHandler(this);
        } else if (!mHistoryHandler.isOpen()) {
            mHistoryHandler = new HistoryDatabaseHandler(this);
        }

        // set display options of the ActionBar
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setCustomView(R.layout.search);

        RelativeLayout back = (RelativeLayout) findViewById(R.id.action_back);
        RelativeLayout forward = (RelativeLayout) findViewById(R.id.action_forward);
        if (back != null) {
            back.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCurrentView != null) {
                        if (mCurrentView.canGoBack()) {
                            mCurrentView.goBack();
                        } else {
                            deleteTab(mDrawerListLeft.getCheckedItemPosition());
                        }
                    }
                }

            });
        }
        if (forward != null) {
            forward.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCurrentView != null) {
                        if (mCurrentView.canGoForward()) {
                            mCurrentView.goForward();
                        }
                    }
                }

            });
        }

        // create the search EditText in the ActionBar
        mSearch = (AutoCompleteTextView) mActionBar.getCustomView()
                .findViewById(R.id.search);
        mDeleteIcon = getResources().getDrawable(R.drawable.ic_action_delete);
        mDeleteIcon.setBounds(0, 0, Utils.convertToDensityPixels(mContext, 24),
                Utils.convertToDensityPixels(mContext, 24));
        mRefreshIcon = getResources().getDrawable(R.drawable.ic_action_refresh);
        mRefreshIcon.setBounds(0, 0,
                Utils.convertToDensityPixels(mContext, 24),
                Utils.convertToDensityPixels(mContext, 24));
        mCopyIcon = getResources().getDrawable(R.drawable.ic_action_copy);
        mCopyIcon.setBounds(0, 0, Utils.convertToDensityPixels(mContext, 24),
                Utils.convertToDensityPixels(mContext, 24));
        mIcon = mRefreshIcon;
        mSearch.setCompoundDrawables(null, null, mRefreshIcon, null);
        mSearch.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {

                switch (arg1) {
                case KeyEvent.KEYCODE_ENTER:
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mSearch.getWindowToken(), 0);
                    searchTheWeb(mSearch.getText().toString());
                    if (mCurrentView != null) {
                        mCurrentView.requestFocus();
                    }
                    return true;
                default:
                    break;
                }
                return false;
            }

        });
        mSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && mCurrentView != null) {
                    if (mCurrentView != null) {
                        if (mCurrentView.getProgress() < 100) {
                            setIsLoading();
                        } else {
                            setIsFinishedLoading();
                        }
                    }
                    updateUrl(mCurrentView.getUrl());
                } else if (hasFocus) {
                    mIcon = mCopyIcon;
                    mSearch.setCompoundDrawables(null, null, mCopyIcon, null);
                }
            }
        });
        mSearch.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int actionId,
                    KeyEvent arg2) {
                // hide the keyboard and search the web when the enter key
                // button is pressed
                if (actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_SEARCH
                        || (arg2.getAction() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mSearch.getWindowToken(), 0);
                    searchTheWeb(mSearch.getText().toString());
                    if (mCurrentView != null) {
                        mCurrentView.requestFocus();
                    }
                    return true;
                }
                return false;
            }

        });

        mSearch.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mSearch.getCompoundDrawables()[2] != null) {
                    boolean tappedX = event.getX() > (mSearch.getWidth()
                            - mSearch.getPaddingRight() - mIcon
                            .getIntrinsicWidth());
                    if (tappedX) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (mSearch.hasFocus()) {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label",
                                        mSearch.getText().toString());
                                clipboard.setPrimaryClip(clip);
                                Utils.showToast(
                                        mContext,
                                        mContext.getResources().getString(
                                                R.string.message_text_copied));
                            } else {
                                refreshOrStop();
                            }
                        }
                        return true;
                    }
                }
                return false;
            }

        });

        mSystemBrowser = getSystemBrowser();
        Thread initialize = new Thread(new Runnable() {

            @Override
            public void run() {
                initializeSearchSuggestions(mSearch);
            }

        });
        initialize.run();
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
        mDrawerLayout, /* DrawerLayout object */
        R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
        R.string.drawer_open, /* "open drawer" description for accessibility */
        R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (view.equals(mDrawerLeft)) {
                    mDrawerLayout.setDrawerLockMode(
                            DrawerLayout.LOCK_MODE_UNLOCKED, mDrawerRight);
                } else if (view.equals(mDrawerRight)) {
                    mDrawerLayout.setDrawerLockMode(
                            DrawerLayout.LOCK_MODE_UNLOCKED, mDrawerLeft);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (drawerView.equals(mDrawerLeft)) {
                    mDrawerLayout.closeDrawer(mDrawerRight);
                    mDrawerLayout.setDrawerLockMode(
                            DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mDrawerRight);
                } else if (drawerView.equals(mDrawerRight)) {
                    mDrawerLayout.closeDrawer(mDrawerLeft);
                    mDrawerLayout.setDrawerLockMode(
                            DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mDrawerLeft);
                }
            }

        };

        mNewTab.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                newTab(null, true);
            }

        });

        mNewTab.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                String url = mPreferences.getString(
                        PreferenceConstants.SAVE_URL, null);
                if (url != null) {
                    newTab(url, true);
                    Toast.makeText(mContext, R.string.deleted_tab,
                            Toast.LENGTH_SHORT).show();
                }
                mEditPrefs.putString(PreferenceConstants.SAVE_URL, null)
                        .apply();
                return true;
            }

        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_right_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_left_shadow,
                GravityCompat.START);
        initializePreferences();
        initializeTabs();

        if (API < 19) {
            WebIconDatabase.getInstance().open(
                    getDir("icons", MODE_PRIVATE).getPath());
        }

        checkForTor();

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                initFlint();
            }
   
        }, 500);
    }

    /*
     * If Orbot/Tor is installed, prompt the user if they want to enable
     * proxying for this session
     */
    public boolean checkForTor() {
        boolean useProxy = mPreferences.getBoolean(
                PreferenceConstants.USE_PROXY, false);
/*
        OrbotHelper oh = new OrbotHelper(this);
        if (oh.isOrbotInstalled()
                && !mPreferences.getBoolean(
                        PreferenceConstants.INITIAL_CHECK_FOR_TOR, false)) {
            mEditPrefs.putBoolean(PreferenceConstants.INITIAL_CHECK_FOR_TOR,
                    true);
            mEditPrefs.apply();
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        mPreferences
                                .edit()
                                .putBoolean(PreferenceConstants.USE_PROXY, true)
                                .apply();

                        initializeTor();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        mPreferences
                                .edit()
                                .putBoolean(PreferenceConstants.USE_PROXY,
                                        false).apply();
                        break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.use_tor_prompt)
                    .setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener).show();

            return true;
        } else if (oh.isOrbotInstalled() & useProxy == true) {
            initializeTor();
            return true;
        } else {
            mEditPrefs.putBoolean(PreferenceConstants.USE_PROXY, false);
            mEditPrefs.apply();
            return false;
        }
        */
        
        mEditPrefs.putBoolean(PreferenceConstants.USE_PROXY, false);
        mEditPrefs.apply();
        return false;
    }

    /*
     * Initialize WebKit Proxying for Tor
     */
    public void initializeTor() {

//        OrbotHelper oh = new OrbotHelper(this);
//        if (!oh.isOrbotRunning()) {
//            oh.requestOrbotStart(this);
//        }
//        try {
//            String host = mPreferences.getString(
//                    PreferenceConstants.USE_PROXY_HOST, "localhost");
//            int port = mPreferences.getInt(PreferenceConstants.USE_PROXY_PORT,
//                    8118);
//            WebkitProxy.setProxy(
//                    "com.crosskr.flint.webview.browser.BrowserApp",
//                    getApplicationContext(), host, port);
//        } catch (Exception e) {
//            Log.d(Constants.TAG, "error enabling web proxying", e);
//        }

    }

    public void setNavigationDrawerWidth() {
        int width = getResources().getDisplayMetrics().widthPixels * 3 / 4;
        int maxWidth = Utils.convertToDensityPixels(mContext, 300);
        if (width > maxWidth) {
            DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) mDrawerLeft
                    .getLayoutParams();
            params.width = maxWidth;
            mDrawerLeft.setLayoutParams(params);
            DrawerLayout.LayoutParams paramsRight = (android.support.v4.widget.DrawerLayout.LayoutParams) mDrawerRight
                    .getLayoutParams();
            paramsRight.width = maxWidth;
            mDrawerRight.setLayoutParams(paramsRight);
        } else {
            DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) mDrawerLeft
                    .getLayoutParams();
            params.width = width;
            mDrawerLeft.setLayoutParams(params);
            DrawerLayout.LayoutParams paramsRight = (android.support.v4.widget.DrawerLayout.LayoutParams) mDrawerRight
                    .getLayoutParams();
            paramsRight.width = width;
            mDrawerRight.setLayoutParams(paramsRight);
        }
    }

    /*
     * Override this class
     */
    public synchronized void initializeTabs() {

    }

    public void restoreOrNewTab() {
        mIdGenerator = 0;

        String url = null;
        if (getIntent() != null) {
            url = getIntent().getDataString();
            if (url != null) {
                if (url.startsWith(Constants.FILE)) {
                    Utils.showToast(
                            this,
                            getResources().getString(
                                    R.string.message_blocked_local));
                    url = null;
                }
            }
        }
        if (mPreferences.getBoolean(PreferenceConstants.RESTORE_LOST_TABS,
                false)) {
            String mem = mPreferences.getString(PreferenceConstants.URL_MEMORY,
                    "");
            mEditPrefs.putString(PreferenceConstants.URL_MEMORY, "");
            String[] array = Utils.getArray(mem);
            int count = 0;
            for (int n = 0; n < array.length; n++) {
                if (array[n].length() > 0) {
                    newTab(array[n], true);
                    count++;
                }
            }
            if (url != null) {
                newTab(url, true);
            } else if (count == 0) {
                newTab(null, true);
            }
        } else {
            newTab(url, true);
        }
    }

    public void initializePreferences() {
        if (mPreferences == null) {
            mPreferences = getSharedPreferences(
                    PreferenceConstants.PREFERENCES, 0);
        }
        mFullScreen = mPreferences.getBoolean(PreferenceConstants.FULL_SCREEN,
                false);
        if (mPreferences.getBoolean(PreferenceConstants.HIDE_STATUS_BAR, false)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        switch (mPreferences.getInt(PreferenceConstants.SEARCH, 1)) {
        case 0:
            mSearchText = mPreferences.getString(
                    PreferenceConstants.SEARCH_URL, Constants.GOOGLE_SEARCH);
            if (!mSearchText.startsWith(Constants.HTTP)
                    && !mSearchText.startsWith(Constants.HTTPS)) {
                mSearchText = Constants.GOOGLE_SEARCH;
            }
            break;
        case 1:
            mSearchText = Constants.GOOGLE_SEARCH;
            break;
        case 2:
            mSearchText = Constants.ANDROID_SEARCH;
            break;
        case 3:
            mSearchText = Constants.BING_SEARCH;
            break;
        case 4:
            mSearchText = Constants.YAHOO_SEARCH;
            break;
        case 5:
            mSearchText = Constants.STARTPAGE_SEARCH;
            break;
        case 6:
            mSearchText = Constants.STARTPAGE_MOBILE_SEARCH;
            break;
        case 7:
            mSearchText = Constants.DUCK_SEARCH;
            break;
        case 8:
            mSearchText = Constants.DUCK_LITE_SEARCH;
            break;
        case 9:
            mSearchText = Constants.BAIDU_SEARCH;
            break;
        case 10:
            mSearchText = Constants.YANDEX_SEARCH;
            break;
        }

        updateCookiePreference();
//        if (mPreferences.getBoolean(PreferenceConstants.USE_PROXY, false)) {
//            initializeTor();
//        } else {
//            try {
//                WebkitProxy.resetProxy(
//                        "com.crosskr.flint.webview.browser.BrowserApp",
//                        getApplicationContext());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    /*
     * Override this if class overrides BrowserActivity
     */
    public void updateCookiePreference() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (mSearch.hasFocus()) {
                searchTheWeb(mSearch.getText().toString());
            }
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (mMediaFlingBar != null
                    && mMediaFlingBar.getVisibility() == View.VISIBLE
                    && mFlintVideoManager.isDeviceConnected()) {
                onVolumeChange(0.1);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (mMediaFlingBar != null
                    && mMediaFlingBar.getVisibility() == View.VISIBLE
                    && mFlintVideoManager.isDeviceConnected()) {
                onVolumeChange(-0.1);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            if (mDrawerLayout.isDrawerOpen(mDrawerRight)) {
                mDrawerLayout.closeDrawer(mDrawerRight);
                mDrawerLayout.openDrawer(mDrawerLeft);
            } else if (mDrawerLayout.isDrawerOpen(mDrawerLeft)) {
                mDrawerLayout.closeDrawer(mDrawerLeft);
            }
            mDrawerToggle.syncState();
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
        case android.R.id.home:
            if (mDrawerLayout.isDrawerOpen(mDrawerRight)) {
                mDrawerLayout.closeDrawer(mDrawerRight);
            }
            mDrawerToggle.syncState();
            return true;
        case R.id.action_back:
            if (mCurrentView != null) {
                if (mCurrentView.canGoBack()) {
                    mCurrentView.goBack();
                }
            }
            return true;
        case R.id.action_forward:
            if (mCurrentView != null) {
                if (mCurrentView.canGoForward()) {
                    mCurrentView.goForward();
                }
            }
            return true;
        case R.id.action_new_tab:
            newTab(null, true);
            return true;
            // case R.id.action_incognito:
            // startActivity(new Intent(this, IncognitoActivity.class));
            // return true;
        case R.id.action_share:
            if (!mCurrentView.getUrl().startsWith(Constants.FILE)) {
                Intent shareIntent = new Intent(
                        android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        mCurrentView.getTitle());
                String shareMessage = mCurrentView.getUrl();
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        shareMessage);
                startActivity(Intent.createChooser(shareIntent, getResources()
                        .getString(R.string.dialog_title_share)));
            }
            return true;
        case R.id.action_bookmarks:
            openBookmarks();
            return true;
        case R.id.action_copy:
            if (mCurrentView != null) {
                if (!mCurrentView.getUrl().startsWith(Constants.FILE)) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", mCurrentView
                            .getUrl().toString());
                    clipboard.setPrimaryClip(clip);
                    Utils.showToast(mContext, mContext.getResources()
                            .getString(R.string.message_link_copied));
                }
            }
            return true;
        case R.id.action_user_agent:
        case R.id.action_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        case R.id.action_history:
            openHistory();
            return true;
        case R.id.action_add_bookmark:
            if (!mCurrentView.getUrl().startsWith(Constants.FILE)) {
                HistoryItem bookmark = new HistoryItem(mCurrentView.getUrl(),
                        mCurrentView.getTitle());
                if (mBookmarkManager.addBookmark(bookmark)) {
                    mBookmarkList.add(bookmark);
                    Collections.sort(mBookmarkList, new SortIgnoreCase());
                    notifyBookmarkDataSetChanged();
                    mSearchAdapter.refreshBookmarks();
                }
            }
            return true;
        case R.id.action_find:
            findInPage();
            return true;

        case R.id.action_fling:
            if (mMediaFlingBar.getVisibility() != View.GONE) {
                mMediaFlingBar.hide();
            } else {
                mMediaFlingBar.show();
            }

            return true;
            
        case R.id.action_feedback:
            FeedbackAgent agent = new FeedbackAgent(mContext);
            agent.startFeedbackActivity();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * refreshes the underlying list of the Bookmark adapter since the bookmark
     * adapter doesn't always change when notifyDataChanged gets called.
     */
    private void notifyBookmarkDataSetChanged() {
        mBookmarkAdapter.clear();
        mBookmarkAdapter.addAll(mBookmarkList);
        mBookmarkAdapter.notifyDataSetChanged();
    }

    /**
     * method that shows a dialog asking what string the user wishes to search
     * for. It highlights the text entered.
     */
    private void findInPage() {
        final AlertDialog.Builder finder = new AlertDialog.Builder(mActivity);
        finder.setTitle(getResources().getString(R.string.action_find));
        final EditText getHome = new EditText(this);
        getHome.setHint(getResources().getString(R.string.search_hint));
        finder.setView(getHome);
        finder.setPositiveButton(
                getResources().getString(R.string.search_hint),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String query = getHome.getText().toString();
                        if (query.length() > 0)
                            showSearchInterfaceBar(query);
                    }
                });
        finder.show();
    }

    private void showSearchInterfaceBar(String text) {
        if (mCurrentView != null) {
            mCurrentView.find(text);
        }

        final RelativeLayout bar = (RelativeLayout) findViewById(R.id.search_bar);
        bar.setVisibility(View.VISIBLE);

        TextView tw = (TextView) findViewById(R.id.search_query);
        tw.setText("'" + text + "'");

        ImageButton up = (ImageButton) findViewById(R.id.button_next);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentView.getWebView().findNext(false);
            }
        });
        ImageButton down = (ImageButton) findViewById(R.id.button_back);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentView.getWebView().findNext(true);
            }
        });

        ImageButton quit = (ImageButton) findViewById(R.id.button_quit);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentView.getWebView().clearMatches();
                bar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * The click listener for ListView in the navigation drawer
     */
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            mIsNewIntent = false;
            selectItem(position);
        }
    }

    /**
     * long click listener for Navigation Drawer
     */
    private class DrawerItemLongClickListener implements
            ListView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                int position, long arg3) {
            deleteTab(position);
            return false;
        }
    }

    private class BookmarkItemClickListener implements
            ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (mCurrentView != null) {
                mCurrentView.loadUrl(mBookmarkList.get(position).getUrl());
            }
            // keep any jank from happening when the drawer is closed after the
            // URL starts to load
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawer(mDrawerRight);
                }
            }, 150);
        }
    }

    private class BookmarkItemLongClickListener implements
            ListView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                final int position, long arg3) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(mContext.getResources().getString(
                    R.string.action_bookmarks));
            builder.setMessage(
                    getResources().getString(R.string.dialog_bookmark))
                    .setCancelable(true)
                    .setPositiveButton(
                            getResources().getString(R.string.action_new_tab),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    newTab(mBookmarkList.get(position).getUrl(),
                                            false);
                                    mDrawerLayout.closeDrawers();
                                }
                            })
                    .setNegativeButton(
                            getResources().getString(R.string.action_delete),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    if (mBookmarkManager
                                            .deleteBookmark(mBookmarkList.get(
                                                    position).getUrl())) {
                                        mBookmarkList.remove(position);
                                        notifyBookmarkDataSetChanged();
                                        mSearchAdapter.refreshBookmarks();
                                        openBookmarks();
                                    }
                                }
                            })
                    .setNeutralButton(
                            getResources().getString(R.string.action_edit),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    editBookmark(position);
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }
    }

    /**
     * Takes in the id of which bookmark was selected and shows a dialog that
     * allows the user to rename and change the url of the bookmark
     * 
     * @param id
     *            which id in the list was chosen
     */
    public synchronized void editBookmark(final int id) {
        final AlertDialog.Builder homePicker = new AlertDialog.Builder(
                mActivity);
        homePicker.setTitle(getResources().getString(
                R.string.title_edit_bookmark));
        final EditText getTitle = new EditText(mContext);
        getTitle.setHint(getResources().getString(R.string.hint_title));
        getTitle.setText(mBookmarkList.get(id).getTitle());
        getTitle.setSingleLine();
        final EditText getUrl = new EditText(mContext);
        getUrl.setHint(getResources().getString(R.string.hint_url));
        getUrl.setText(mBookmarkList.get(id).getUrl());
        getUrl.setSingleLine();
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(getTitle);
        layout.addView(getUrl);
        homePicker.setView(layout);
        homePicker.setPositiveButton(
                getResources().getString(R.string.action_ok),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBookmarkList.get(id).setTitle(
                                getTitle.getText().toString());
                        mBookmarkList.get(id).setUrl(
                                getUrl.getText().toString());
                        mBookmarkManager.overwriteBookmarks(mBookmarkList);
                        Collections.sort(mBookmarkList, new SortIgnoreCase());
                        notifyBookmarkDataSetChanged();
                        if (mCurrentView != null) {
                            if (mCurrentView.getUrl()
                                    .startsWith(Constants.FILE)
                                    && mCurrentView.getUrl().endsWith(
                                            "bookmarks.html")) {
                                openBookmarkPage(mCurrentView.getWebView());
                            }
                        }
                    }
                });
        homePicker.show();
    }

    /**
     * displays the WebView contained in the LightningView Also handles the
     * removal of previous views
     * 
     * @param view
     *            the LightningView to show
     */
    private synchronized void showTab(LightningView view) {
        if (view == null) {
            return;
        }
        mBrowserFrame.removeAllViews();
        if (mCurrentView != null) {
            mCurrentView.setForegroundTab(false);
            mCurrentView.onPause();
        }
        mCurrentView = view;
        mCurrentView.setForegroundTab(true);
        if (mCurrentView.getWebView() != null) {
            updateUrl(mCurrentView.getUrl());
            updateProgress(mCurrentView.getProgress());
        } else {
            updateUrl("");
            updateProgress(0);
        }

        mBrowserFrame.addView(mCurrentView.getWebView(), mMatchParent);
        mCurrentView.onResume();

        // Use a delayed handler to make the transition smooth
        // otherwise it will get caught up with the showTab code
        // and cause a janky motion
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawers();
            }
        }, 150);
    }

    /**
     * creates a new tab with the passed in URL if it isn't null
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    public void handleNewIntent(Intent intent) {
        if (mCurrentView == null) {
            initialize();
        }

        String url = null;
        if (intent != null) {
            url = intent.getDataString();
        }
        int num = 0;
        if (intent != null && intent.getExtras() != null) {
            num = intent.getExtras().getInt(getPackageName() + ".Origin");
        }
        if (num == 1) {
            mCurrentView.loadUrl(url);
        } else if (url != null) {
            if (url.startsWith(Constants.FILE)) {
                Utils.showToast(this,
                        getResources()
                                .getString(R.string.message_blocked_local));
                url = null;
            }
            newTab(url, true);
            mIsNewIntent = true;
        }
    }

    @Override
    public void closeEmptyTab() {
        if (mCurrentView != null
                && mCurrentView.getWebView().copyBackForwardList().getSize() == 0) {
            closeCurrentTab();
        }
    }

    private void closeCurrentTab() {
        // don't delete the tab because the browser will close and mess stuff up
    }

    private void selectItem(final int position) {
        // update selected item and title, then close the drawer

        showTab(mWebViews.get(position));

    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    protected synchronized void newTab(String url, boolean show) {
        mIsNewIntent = false;
        LightningView startingTab = new LightningView(mActivity, url);
        if (mIdGenerator == 0) {
            startingTab.resumeTimers();
        }
        mIdGenerator++;
        mWebViews.add(startingTab);

        Drawable icon = writeOnDrawable(mWebViews.size());
        mActionBar.setIcon(icon);
        mTitleAdapter.notifyDataSetChanged();
        if (show) {
            mDrawerListLeft.setItemChecked(mWebViews.size() - 1, true);
            showTab(startingTab);
        }
    }

    private synchronized void deleteTab(int position) {
        if (position >= mWebViews.size()) {
            return;
        }

        int current = mDrawerListLeft.getCheckedItemPosition();
        LightningView reference = mWebViews.get(position);
        if (reference == null) {
            return;
        }
        if (reference.getUrl() != null
                && !reference.getUrl().startsWith(Constants.FILE)) {
            mEditPrefs.putString(PreferenceConstants.SAVE_URL,
                    reference.getUrl()).apply();
        }
        boolean isShown = reference.isShown();
        if (current > position) {
            mWebViews.remove(position);
            mDrawerListLeft.setItemChecked(current - 1, true);
            reference.onDestroy();
        } else if (mWebViews.size() > position + 1) {
            if (current == position) {
                showTab(mWebViews.get(position + 1));
                mWebViews.remove(position);
                mDrawerListLeft.setItemChecked(position, true);
            } else {
                mWebViews.remove(position);
            }

            reference.onDestroy();
        } else if (mWebViews.size() > 1) {
            if (current == position) {
                showTab(mWebViews.get(position - 1));
                mWebViews.remove(position);
                mDrawerListLeft.setItemChecked(position - 1, true);
            } else {
                mWebViews.remove(position);
            }

            reference.onDestroy();
        } else {
            if (mCurrentView.getUrl() == null
                    || mCurrentView.getUrl().startsWith(Constants.FILE)
                    || mCurrentView.getUrl().equals(mHomepage)) {
                closeActivity();
            } else {
                mWebViews.remove(position);
                if (mPreferences.getBoolean(
                        PreferenceConstants.CLEAR_CACHE_EXIT, false)
                        && mCurrentView != null && !isIncognito()) {
                    mCurrentView.clearCache(true);
                    Log.i(Constants.TAG, "Cache Cleared");

                }
                if (mPreferences.getBoolean(
                        PreferenceConstants.CLEAR_HISTORY_EXIT, false)
                        && !isIncognito()) {
                    clearHistory();
                    Log.i(Constants.TAG, "History Cleared");

                }
                if (mPreferences.getBoolean(
                        PreferenceConstants.CLEAR_COOKIES_EXIT, false)
                        && !isIncognito()) {
                    clearCookies();
                    Log.i(Constants.TAG, "Cookies Cleared");

                }
                if (reference != null) {
                    reference.pauseTimers();
                    reference.onDestroy();
                }
                mCurrentView = null;
                mTitleAdapter.notifyDataSetChanged();
                finish();

            }
        }
        mTitleAdapter.notifyDataSetChanged();
        Drawable icon = writeOnDrawable(mWebViews.size());
        mActionBar.setIcon(icon);

        if (mIsNewIntent && isShown) {
            mIsNewIntent = false;
            closeActivity();
        }

        Log.i(Constants.TAG, "deleted tab");
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPreferences.getBoolean(PreferenceConstants.CLEAR_CACHE_EXIT,
                    false) && mCurrentView != null && !isIncognito()) {
                mCurrentView.clearCache(true);
                Log.i(Constants.TAG, "Cache Cleared");

            }
            if (mPreferences.getBoolean(PreferenceConstants.CLEAR_HISTORY_EXIT,
                    false) && !isIncognito()) {
                clearHistory();
                Log.i(Constants.TAG, "History Cleared");

            }
            if (mPreferences.getBoolean(PreferenceConstants.CLEAR_COOKIES_EXIT,
                    false) && !isIncognito()) {
                clearCookies();
                Log.i(Constants.TAG, "Cookies Cleared");

            }
            mCurrentView = null;
            for (int n = 0; n < mWebViews.size(); n++) {
                if (mWebViews.get(n) != null) {
                    mWebViews.get(n).onDestroy();
                }
            }
            mWebViews.clear();
            mTitleAdapter.notifyDataSetChanged();
            finish();
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    public void clearHistory() {
        this.deleteDatabase(HistoryDatabaseHandler.DATABASE_NAME);
        WebViewDatabase m = WebViewDatabase.getInstance(this);
        m.clearFormData();
        m.clearHttpAuthUsernamePassword();
        if (API < 18) {
            m.clearUsernamePassword();
            WebIconDatabase.getInstance().removeAllIcons();
        }
        if (mSystemBrowser) {
            try {
                //android.provider.Browser.clearHistory(getContentResolver());
            } catch (NullPointerException ignored) {
            }
        }
        Utils.trimCache(this);
    }

    public void clearCookies() {
        CookieManager c = CookieManager.getInstance();
        CookieSyncManager.createInstance(this);
        c.removeAllCookie();
    }

    @Override
    public void onBackPressed() {
        if (!mActionBar.isShowing()) {
            mActionBar.show();
        }
        if (mDrawerLayout.isDrawerOpen(mDrawerLeft)) {
            mDrawerLayout.closeDrawer(mDrawerLeft);
        } else if (mDrawerLayout.isDrawerOpen(mDrawerRight)) {
            mDrawerLayout.closeDrawer(mDrawerRight);
        } else {
            if (mCurrentView != null) {
                Log.i(Constants.TAG, "onBackPressed");
                if (mCurrentView.canGoBack()) {
                    if (!mCurrentView.isShown()) {
                        onHideCustomView();
                    } else {
                        mCurrentView.goBack();
                    }
                } else {
                    deleteTab(mDrawerListLeft.getCheckedItemPosition());
                }
            } else {
                Log.e(Constants.TAG, "So madness. Much confusion. Why happen.");
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        MobclickAgent.onPause(this);

        Log.i(Constants.TAG, "onPause");
        if (mCurrentView != null) {
            mCurrentView.pauseTimers();
            mCurrentView.onPause();
        }
        if (mHistoryHandler != null) {
            if (mHistoryHandler.isOpen()) {
                mHistoryHandler.close();
            }
        }

    }

    public void saveOpenTabs() {
        if (mPreferences
                .getBoolean(PreferenceConstants.RESTORE_LOST_TABS, true)) {
            String s = "";
            for (int n = 0; n < mWebViews.size(); n++) {
                if (mWebViews.get(n).getUrl() != null) {
                    s = s + mWebViews.get(n).getUrl() + "|$|SEPARATOR|$|";
                }
            }
            mEditPrefs.putString(PreferenceConstants.URL_MEMORY, s);
            mEditPrefs.commit();
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(Constants.TAG, "onDestroy");
        if (mHistoryHandler != null) {
            if (mHistoryHandler.isOpen()) {
                mHistoryHandler.close();
            }
        }

        mQuit = true;

        if (mGetVideoUrlRunnable != null) {
            try {
                synchronized (mGetVideoUrlRunnable) {
                    mGetVideoUrlRunnable.notify();
                }
            } catch (Exception e) {

            }
        }

        mFlintVideoManager.onStop();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MobclickAgent.onResume(this);

        Log.i(Constants.TAG, "onResume");
        if (mSearchAdapter != null) {
            mSearchAdapter.refreshPreferences();
            mSearchAdapter.refreshBookmarks();
        }
        if (mActionBar != null) {
            if (!mActionBar.isShowing()) {
                mActionBar.show();
            }
        }
        if (mCurrentView != null) {
            mCurrentView.resumeTimers();
            mCurrentView.onResume();

            if (mHistoryHandler == null) {
                mHistoryHandler = new HistoryDatabaseHandler(this);
            } else if (!mHistoryHandler.isOpen()) {
                mHistoryHandler = new HistoryDatabaseHandler(this);
            }
            mBookmarkList = mBookmarkManager.getBookmarks(true);
            notifyBookmarkDataSetChanged();
        } else {
            initialize();
        }
        initializePreferences();
        if (mWebViews != null) {
            for (int n = 0; n < mWebViews.size(); n++) {
                if (mWebViews.get(n) != null) {
                    mWebViews.get(n).initializePreferences(this);
                } else {
                    mWebViews.remove(n);
                }
            }
        } else {
            initialize();
        }
    }

    /**
     * searches the web for the query fixing any and all problems with the input
     * checks if it is a search, url, etc.
     */
    void searchTheWeb(String query) {
        if (query.equals("")) {
            return;
        }
        String SEARCH = mSearchText;
        query = query.trim();
        mCurrentView.stopLoading();

        if (query.startsWith("www.")) {
            query = Constants.HTTP + query;
        } else if (query.startsWith("ftp.")) {
            query = "ftp://" + query;
        }

        boolean containsPeriod = query.contains(".");
        boolean isIPAddress = (TextUtils.isDigitsOnly(query.replace(".", ""))
                && (query.replace(".", "").length() >= 4) && query
                .contains("."));
        boolean aboutScheme = query.contains("about:");
        boolean validURL = (query.startsWith("ftp://")
                || query.startsWith(Constants.HTTP)
                || query.startsWith(Constants.FILE) || query
                    .startsWith(Constants.HTTPS)) || isIPAddress;
        boolean isSearch = ((query.contains(" ") || !containsPeriod) && !aboutScheme);

        if (isIPAddress
                && (!query.startsWith(Constants.HTTP) || !query
                        .startsWith(Constants.HTTPS))) {
            query = Constants.HTTP + query;
        }

        if (isSearch) {
            try {
                query = URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mCurrentView.loadUrl(SEARCH + query);
        } else if (!validURL) {
            mCurrentView.loadUrl(Constants.HTTP + query);
        } else {
            mCurrentView.loadUrl(query);
        }
    }

    private int pixelsToDp(int num) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) ((num - 0.5f) / scale);
    }

    /**
     * writes the number of open tabs on the icon.
     */
    public BitmapDrawable writeOnDrawable(int number) {

        Bitmap bm = Bitmap.createBitmap(mActionBarSize, mActionBarSize,
                Config.ARGB_8888);
        String text = number + "";
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL);
        paint.setColor(mNumberIconColor);
        if (number > 99) {
            number = 99;
        }
        // pixels, 36 dp
        if (mActionBarSizeDp < 50) {
            if (number > 9) {
                paint.setTextSize(mActionBarSize * 3 / 4); // originally
                // 40
                // pixels,
                // 24 dp
            } else {
                paint.setTextSize(mActionBarSize * 9 / 10); // originally 50
                // pixels, 30 dp
            }
        } else {
            paint.setTextSize(mActionBarSize * 3 / 4);
        }
        Canvas canvas = new Canvas(bm);
        // originally only vertical padding of 5 pixels

        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint
                .ascent()) / 2));

        canvas.drawText(text, xPos, yPos, paint);

        return new BitmapDrawable(getResources(), bm);
    }

    public class LightningViewAdapter extends ArrayAdapter<LightningView> {

        Context context;

        int layoutResourceId;

        List<LightningView> data = null;

        public LightningViewAdapter(Context context, int layoutResourceId,
                List<LightningView> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            View row = convertView;
            LightningViewHolder holder = null;
            if (row == null) {
                LayoutInflater inflater = ((Activity) context)
                        .getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new LightningViewHolder();
                holder.txtTitle = (TextView) row.findViewById(R.id.text1);
                holder.favicon = (ImageView) row.findViewById(R.id.favicon1);
                holder.exit = (ImageView) row.findViewById(R.id.delete1);
                holder.exit.setTag(position);
                row.setTag(holder);
            } else {
                holder = (LightningViewHolder) row.getTag();
            }

            holder.exit.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    deleteTab(position);
                }

            });

            LightningView web = data.get(position);
            holder.txtTitle.setText(web.getTitle());
            if (web.isForegroundTab()) {
                holder.txtTitle.setTextAppearance(context, R.style.boldText);
            } else {
                holder.txtTitle.setTextAppearance(context, R.style.normalText);
            }

            Bitmap favicon = web.getFavicon();
            if (web.isForegroundTab()) {

                holder.favicon.setImageBitmap(favicon);
            } else {
                Bitmap grayscaleBitmap = Bitmap.createBitmap(
                        favicon.getWidth(), favicon.getHeight(),
                        Bitmap.Config.ARGB_8888);

                Canvas c = new Canvas(grayscaleBitmap);
                Paint p = new Paint();
                ColorMatrix cm = new ColorMatrix();

                cm.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
                p.setColorFilter(filter);
                c.drawBitmap(favicon, 0, 0, p);
                holder.favicon.setImageBitmap(grayscaleBitmap);
            }
            return row;
        }

        class LightningViewHolder {

            TextView txtTitle;

            ImageView favicon;

            ImageView exit;
        }
    }

    public class BookmarkViewAdapter extends ArrayAdapter<HistoryItem> {

        Context context;

        int layoutResourceId;

        List<HistoryItem> data = null;

        public BookmarkViewAdapter(Context context, int layoutResourceId,
                List<HistoryItem> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            BookmarkViewHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context)
                        .getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new BookmarkViewHolder();
                holder.txtTitle = (TextView) row.findViewById(R.id.text1);
                holder.favicon = (ImageView) row.findViewById(R.id.favicon1);
                row.setTag(holder);
            } else {
                holder = (BookmarkViewHolder) row.getTag();
            }

            HistoryItem web = data.get(position);
            holder.txtTitle.setText(web.getTitle());
            holder.favicon.setImageBitmap(mWebpageBitmap);
            if (web.getBitmap() == null) {
                getImage(holder.favicon, web);
            } else {
                holder.favicon.setImageBitmap(web.getBitmap());
            }
            return row;
        }

        class BookmarkViewHolder {

            TextView txtTitle;

            ImageView favicon;
        }
    }

    public void getImage(ImageView image, HistoryItem web) {
        try {
            new DownloadImageTask(image, web).execute(Constants.HTTP
                    + getDomainName(web.getUrl()) + "/favicon.ico");
        } catch (URISyntaxException e) {
            new DownloadImageTask(image, web)
                    .execute("https://www.google.com/s2/favicons?domain_url="
                            + web.getUrl());
            e.printStackTrace();
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView bmImage;

        HistoryItem mWeb;

        public DownloadImageTask(ImageView bmImage, HistoryItem web) {
            this.bmImage = bmImage;
            this.mWeb = web;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon = null;
            // unique path for each url that is bookmarked.
            String hash = String.valueOf(urldisplay.hashCode());
            File image = new File(mContext.getCacheDir(), hash + ".png");
            // checks to see if the image exists
            if (!image.exists()) {
                try {
                    // if not, download it...
                    URL url = new URL(urldisplay);
                    HttpURLConnection connection = (HttpURLConnection) url
                            .openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream in = connection.getInputStream();

                    if (in != null) {
                        mIcon = BitmapFactory.decodeStream(in);
                    }
                    // ...and cache it
                    if (mIcon != null) {
                        FileOutputStream fos = new FileOutputStream(image);
                        mIcon.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.flush();
                        fos.close();
                        Log.i(Constants.TAG, "Downloaded: " + urldisplay);
                    }

                } catch (Exception e) {
                } finally {

                }
            } else {
                // if it exists, retrieve it from the cache
                mIcon = BitmapFactory.decodeFile(image.getPath());
            }
            if (mIcon == null) {
                try {
                    // if not, download it...
                    InputStream in = new java.net.URL(
                            "https://www.google.com/s2/favicons?domain_url="
                                    + urldisplay).openStream();

                    if (in != null) {
                        mIcon = BitmapFactory.decodeStream(in);
                    }
                    // ...and cache it
                    if (mIcon != null) {
                        FileOutputStream fos = new FileOutputStream(image);
                        mIcon.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.flush();
                        fos.close();
                    }

                } catch (Exception e) {
                }
            }
            if (mIcon == null) {
                return mWebpageBitmap;
            } else {
                return mIcon;
            }
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            mWeb.setBitmap(result);
            notifyBookmarkDataSetChanged();
        }
    }

    static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain == null) {
            return url;
        }
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    @Override
    public void updateUrl(String url) {
        if (url == null) {
            return;
        }
        url = url.replaceFirst(Constants.HTTP, "");
        if (url.startsWith(Constants.FILE)) {
            url = "";
        }

        mSearch.setText(url);
    }

    @Override
    public void updateProgress(int n) {

        if (n > mProgressBar.getProgress()) {
            ObjectAnimator animator = ObjectAnimator.ofInt(mProgressBar,
                    "progress", n);
            animator.setDuration(200);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        } else if (n < mProgressBar.getProgress()) {
            ObjectAnimator animator = ObjectAnimator.ofInt(mProgressBar,
                    "progress", 0, n);
            animator.setDuration(200);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        }
        if (n >= 100) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    setIsFinishedLoading();
                }
            }, 200);

        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            setIsLoading();
        }
    }

    @Override
    public void updateHistory(final String title, final String url) {

    }

    public void addItemToHistory(final String title, final String url) {
        Runnable update = new Runnable() {
            @Override
            public void run() {
                if (isSystemBrowserAvailable()
                        && mPreferences.getBoolean(
                                PreferenceConstants.SYNC_HISTORY, true)) {
                    try {
//                        Browser.updateVisitedHistory(getContentResolver(), url,
//                                true);
                    } catch (NullPointerException ignored) {
                    }
                }
                try {
                    if (mHistoryHandler == null && !mHistoryHandler.isOpen()) {
                        mHistoryHandler = new HistoryDatabaseHandler(mContext);
                    }
                    mHistoryHandler.visitHistoryItem(url, title);
                } catch (IllegalStateException e) {
                    Log.e(Constants.TAG,
                            "IllegalStateException in updateHistory");
                } catch (NullPointerException e) {
                    Log.e(Constants.TAG,
                            "NullPointerException in updateHistory");
                } catch (SQLiteException e) {
                    Log.e(Constants.TAG, "SQLiteException in updateHistory");
                }
            }
        };
        if (url != null && !url.startsWith(Constants.FILE)) {
            new Thread(update).start();
        }
    }

    public boolean isSystemBrowserAvailable() {
        return mSystemBrowser;
    }

    public boolean getSystemBrowser() {
        Cursor c = null;
        String[] columns = new String[] { "url", "title" };
        boolean browserFlag = false;
        try {
//            Uri bookmarks = Browser.BOOKMARKS_URI;
//            c = getContentResolver()
//                    .query(bookmarks, columns, null, null, null);
        } catch (SQLiteException ignored) {
        } catch (IllegalStateException ignored) {
        } catch (NullPointerException ignored) {
        }

        if (c != null) {
            Log.i("Browser", "System Browser Available");
            browserFlag = true;
        } else {
            Log.e("Browser", "System Browser Unavailable");
            browserFlag = false;
        }
        if (c != null) {
            c.close();
            c = null;
        }
        mEditPrefs.putBoolean("SystemBrowser", browserFlag);
        mEditPrefs.commit();
        return browserFlag;
    }

    /**
     * method to generate search suggestions for the AutoCompleteTextView from
     * previously searched URLs
     */
    private void initializeSearchSuggestions(final AutoCompleteTextView getUrl) {

        getUrl.setThreshold(1);
        getUrl.setDropDownWidth(-1);
        getUrl.setDropDownAnchor(R.id.progressWrapper);
        getUrl.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                try {
                    String url;
                    url = ((TextView) arg1.findViewById(R.id.url)).getText()
                            .toString();
                    if (url.startsWith(mContext.getString(R.string.suggestion))) {
                        url = ((TextView) arg1.findViewById(R.id.title))
                                .getText().toString();
                    } else {
                        getUrl.setText(url);
                    }
                    searchTheWeb(url);
                    url = null;
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getUrl.getWindowToken(), 0);
                    if (mCurrentView != null) {
                        mCurrentView.requestFocus();
                    }
                } catch (NullPointerException e) {
                    Log.e("Browser Error: ",
                            "NullPointerException on item click");
                }
            }

        });

        getUrl.setSelectAllOnFocus(true);
        mSearchAdapter = new SearchAdapter(mContext, isIncognito());
        getUrl.setAdapter(mSearchAdapter);
    }

    @Override
    public boolean isIncognito() {
        return false;
    }

    /**
     * function that opens the HTML history page in the browser
     */
    private void openHistory() {
        // use a thread so that history retrieval doesn't block the UI
        Thread history = new Thread(new Runnable() {

            @Override
            public void run() {
                mCurrentView.loadUrl(HistoryPage.getHistoryPage(mContext));
                mSearch.setText("");
            }

        });
        history.run();
    }

    /**
     * helper function that opens the bookmark drawer
     */
    private void openBookmarks() {
        if (mDrawerLayout.isDrawerOpen(mDrawerLeft)) {
            mDrawerLayout.closeDrawers();
        }
        mDrawerToggle.syncState();
        mDrawerLayout.openDrawer(mDrawerRight);
    }

    public void closeDrawers() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    /**
     * open the HTML bookmarks page, parameter view is the WebView that should show the page
     */
    public void openBookmarkPage(WebView view) {
        String bookmarkHtml = BookmarkPage.HEADING;
        Iterator<HistoryItem> iter = mBookmarkList.iterator();
        HistoryItem helper;
        while (iter.hasNext()) {
            helper = iter.next();
            bookmarkHtml += (BookmarkPage.PART1 + helper.getUrl()
                    + BookmarkPage.PART2 + helper.getUrl() + BookmarkPage.PART3
                    + helper.getTitle() + BookmarkPage.PART4);
        }
        bookmarkHtml += BookmarkPage.END;
        File bookmarkWebPage = new File(mContext.getFilesDir(),
                BookmarkPage.FILENAME);
        try {
            FileWriter bookWriter = new FileWriter(bookmarkWebPage, false);
            bookWriter.write(bookmarkHtml);
            bookWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        view.loadUrl(Constants.FILE + bookmarkWebPage);
    }

    @Override
    public void update() {
        mTitleAdapter.notifyDataSetChanged();
    }

    @Override
    /**
     * opens a file chooser
     * param ValueCallback is the message from the WebView indicating a file chooser
     * should be opened
     */
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(
                Intent.createChooser(i, getString(R.string.title_file_chooser)),
                1);
    }

    @Override
    /**
     * used to allow uploading into the browser
     */
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        if (requestCode == 1) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        }
    }

    @Override
    /**
     * handles long presses for the browser, tries to get the
     * url of the item that was clicked and sends it (it can be null)
     * to the click handler that does cool stuff with it
     */
    public void onLongPress() {
        if (mClickHandler == null) {
            mClickHandler = new ClickHandler(mContext);
        }
        Message click = mClickHandler.obtainMessage();
        if (click != null) {
            click.setTarget(mClickHandler);
        }
        mCurrentView.getWebView().requestFocusNodeHref(click);
    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation,
            CustomViewCallback callback) {
        if (view == null) {
            return;
        }
        if (mCustomView != null && callback != null) {
            callback.onCustomViewHidden();
            return;
        }
        view.setKeepScreenOn(true);
        mOriginalOrientation = getRequestedOrientation();
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        mFullscreenContainer = new FullscreenHolder(this);
        mCustomView = view;
        mFullscreenContainer.addView(mCustomView, COVER_SCREEN_PARAMS);
        decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
        setFullscreen(true);
        mCurrentView.setVisibility(View.GONE);
        if (view instanceof FrameLayout) {
            if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
                mVideoView = (VideoView) ((FrameLayout) view).getFocusedChild();
                mVideoView.setOnErrorListener(new VideoCompletionListener());
                mVideoView
                        .setOnCompletionListener(new VideoCompletionListener());
            }
        }
        mCustomViewCallback = callback;
    }

    @Override
    public void onHideCustomView() {
        if (mCustomView == null || mCustomViewCallback == null
                || mCurrentView == null) {
            return;
        }
        Log.i(Constants.TAG, "onHideCustomView");
        mCurrentView.setVisibility(View.VISIBLE);
        mCustomView.setKeepScreenOn(false);
        setFullscreen(mPreferences.getBoolean(
                PreferenceConstants.HIDE_STATUS_BAR, false));
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        if (decor != null) {
            decor.removeView(mFullscreenContainer);
        }

        if (API < 19) {
            try {
                mCustomViewCallback.onCustomViewHidden();
            } catch (Throwable ignored) {

            }
        }
        mFullscreenContainer = null;
        mCustomView = null;
        if (mVideoView != null) {
            mVideoView.setOnErrorListener(null);
            mVideoView.setOnCompletionListener(null);
            mVideoView = null;
        }
        setRequestedOrientation(mOriginalOrientation);
    }

    private class VideoCompletionListener implements
            MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            onHideCustomView();
        }

    }

    /**
     * turns on fullscreen mode in the app
     * 
     * @param enabled
     *            whether to enable fullscreen or not
     */
    public void setFullscreen(boolean enabled) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        if (enabled) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
            if (mCustomView != null) {
                mCustomView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                mBrowserFrame
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
        win.setAttributes(winParams);
    }

    /**
     * a class extending FramLayout used to display fullscreen videos
     */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(
                    android.R.color.black));
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }

    }

    @Override
    /**
     * a stupid method that returns the bitmap image to display in place of
     * a loading video
     */
    public Bitmap getDefaultVideoPoster() {
        if (mDefaultVideoPoster == null) {
            mDefaultVideoPoster = BitmapFactory.decodeResource(getResources(),
                    android.R.drawable.ic_media_play);
        }
        return mDefaultVideoPoster;
    }

    @SuppressLint("InflateParams")
    @Override
    /**
     * dumb method that returns the loading progress for a video
     */
    public View getVideoLoadingProgressView() {
        if (mVideoProgressView == null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            mVideoProgressView = inflater.inflate(
                    R.layout.video_loading_progress, null);
        }
        return mVideoProgressView;
    }

    @Override
    /**
     * handles javascript requests to create a new window in the browser
     */
    public void onCreateWindow(boolean isUserGesture, Message resultMsg) {
        if (resultMsg == null) {
            return;
        }
        newTab("", true);
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(mCurrentView.getWebView());
        resultMsg.sendToTarget();
    }

    @Override
    /**
     * returns the Activity instance for this activity,
     * very helpful when creating things in other classes... I think
     */
    public Activity getActivity() {
        return mActivity;
    }

    /**
     * it hides the action bar, seriously what else were you expecting
     */
    @Override
    public void hideActionBar() {
        if (mActionBar.isShowing() && mFullScreen) {
            mActionBar.hide();
        }
    }

    @Override
    /**
     * obviously it shows the action bar if it's hidden
     */
    public void showActionBar() {
        if (!mActionBar.isShowing() && mFullScreen) {
            mActionBar.show();
        }
    }

    @Override
    /**
     * handles a long click on the page, parameter String url 
     * is the url that should have been obtained from the WebView touch node
     * thingy, if it is null, this method tries to deal with it and find a workaround
     */
    public void longClickPage(final String url) {
        HitTestResult result = null;
        if (mCurrentView.getWebView() != null) {
            result = mCurrentView.getWebView().getHitTestResult();
        }
        if (url != null) {
            if (result != null) {
                if (result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                        || result.getType() == HitTestResult.IMAGE_TYPE) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                newTab(url, false);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                mCurrentView.loadUrl(url);
                                break;

                            case DialogInterface.BUTTON_NEUTRAL:
                                if (API > 8) {
                                    Utils.downloadFile(mActivity, url,
                                            mCurrentView.getUserAgent(),
                                            "attachment", false);
                                }
                                break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            mActivity); // dialog
                    builder.setTitle(url.replace(Constants.HTTP, ""))
                            .setMessage(
                                    getResources().getString(
                                            R.string.dialog_image))
                            .setPositiveButton(
                                    getResources().getString(
                                            R.string.action_new_tab),
                                    dialogClickListener)
                            .setNegativeButton(
                                    getResources().getString(
                                            R.string.action_open),
                                    dialogClickListener)
                            .setNeutralButton(
                                    getResources().getString(
                                            R.string.action_download),
                                    dialogClickListener).show();

                } else {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                newTab(url, false);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                mCurrentView.loadUrl(url);
                                break;

                            case DialogInterface.BUTTON_NEUTRAL:
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label",
                                        url);
                                clipboard.setPrimaryClip(clip);
                                break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            mActivity); // dialog
                    builder.setTitle(url)
                            .setMessage(
                                    getResources().getString(
                                            R.string.dialog_link))
                            .setPositiveButton(
                                    getResources().getString(
                                            R.string.action_new_tab),
                                    dialogClickListener)
                            .setNegativeButton(
                                    getResources().getString(
                                            R.string.action_open),
                                    dialogClickListener)
                            .setNeutralButton(
                                    getResources().getString(
                                            R.string.action_copy),
                                    dialogClickListener).show();
                }
            } else {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            newTab(url, false);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            mCurrentView.loadUrl(url);
                            break;

                        case DialogInterface.BUTTON_NEUTRAL:
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("label", url);
                            clipboard.setPrimaryClip(clip);

                            break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity); // dialog
                builder.setTitle(url)
                        .setMessage(
                                getResources().getString(R.string.dialog_link))
                        .setPositiveButton(
                                getResources().getString(
                                        R.string.action_new_tab),
                                dialogClickListener)
                        .setNegativeButton(
                                getResources().getString(R.string.action_open),
                                dialogClickListener)
                        .setNeutralButton(
                                getResources().getString(R.string.action_copy),
                                dialogClickListener).show();
            }
        } else if (result != null) {
            if (result.getExtra() != null) {
                final String newUrl = result.getExtra();
                if (result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                        || result.getType() == HitTestResult.IMAGE_TYPE) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                newTab(newUrl, false);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                mCurrentView.loadUrl(newUrl);
                                break;

                            case DialogInterface.BUTTON_NEUTRAL:
                                if (API > 8) {
                                    Utils.downloadFile(mActivity, newUrl,
                                            mCurrentView.getUserAgent(),
                                            "attachment", false);
                                }
                                break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            mActivity); // dialog
                    builder.setTitle(newUrl.replace(Constants.HTTP, ""))
                            .setMessage(
                                    getResources().getString(
                                            R.string.dialog_image))
                            .setPositiveButton(
                                    getResources().getString(
                                            R.string.action_new_tab),
                                    dialogClickListener)
                            .setNegativeButton(
                                    getResources().getString(
                                            R.string.action_open),
                                    dialogClickListener)
                            .setNeutralButton(
                                    getResources().getString(
                                            R.string.action_download),
                                    dialogClickListener).show();

                } else {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                newTab(newUrl, false);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                mCurrentView.loadUrl(newUrl);
                                break;

                            case DialogInterface.BUTTON_NEUTRAL:
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label",
                                        newUrl);
                                clipboard.setPrimaryClip(clip);

                                break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            mActivity); // dialog
                    builder.setTitle(newUrl)
                            .setMessage(
                                    getResources().getString(
                                            R.string.dialog_link))
                            .setPositiveButton(
                                    getResources().getString(
                                            R.string.action_new_tab),
                                    dialogClickListener)
                            .setNegativeButton(
                                    getResources().getString(
                                            R.string.action_open),
                                    dialogClickListener)
                            .setNeutralButton(
                                    getResources().getString(
                                            R.string.action_copy),
                                    dialogClickListener).show();
                }

            }

        }

    }

    /**
     * This method lets the search bar know that the page is currently loading
     * and that it should display the stop icon to indicate to the user that
     * pressing it stops the page from loading
     */
    public void setIsLoading() {
        if (!mSearch.hasFocus()) {
            mIcon = mDeleteIcon;
            mSearch.setCompoundDrawables(null, null, mDeleteIcon, null);
        }
    }

    /**
     * This tells the search bar that the page is finished loading and it should
     * display the refresh icon
     */
    public void setIsFinishedLoading() {
        if (!mSearch.hasFocus()) {
            mIcon = mRefreshIcon;
            mSearch.setCompoundDrawables(null, null, mRefreshIcon, null);
        }
    }

    /**
     * handle presses on the refresh icon in the search bar, if the page is
     * loading, stop the page, if it is done loading refresh the page.
     * 
     * See setIsFinishedLoading and setIsLoading for displaying the correct icon
     */
    public void refreshOrStop() {
        if (mCurrentView != null) {
            if (mCurrentView.getProgress() < 100) {
                mCurrentView.stopLoading();
            } else {
                mCurrentView.reload();
            }
        }
    }

    @Override
    public boolean isActionBarShowing() {
        if (mActionBar != null) {
            return mActionBar.isShowing();
        } else {
            return false;
        }
    }

    // Override this, use finish() for Incognito, moveTaskToBack for Main
    public void closeActivity() {
        finish();
    }

    public class SortIgnoreCase implements Comparator<HistoryItem> {

        public int compare(HistoryItem o1, HistoryItem o2) {
            return o1.getTitle().toLowerCase(Locale.getDefault())
                    .compareTo(o2.getTitle().toLowerCase(Locale.getDefault()));
        }

    }

    // add for flint
    private static final String APPLICATION_ID = "~flintplayer";

    protected static final int PLAYER_STATE_NONE = 0;
    protected static final int PLAYER_STATE_PLAYING = 1;
    protected static final int PLAYER_STATE_PAUSED = 2;
    protected static final int PLAYER_STATE_BUFFERING = 3;
    protected static final int PLAYER_STATE_FINISHED = 4;

    private static final int REFRESH_INTERVAL_MS = (int) TimeUnit.SECONDS
            .toMillis(1);

    private boolean mQuit = false;

    private ServerSocket mServerSocket = null;

    // private String mCurrentVideoUrl = null;

    protected Handler mHandler = new Handler();

    private Runnable mRefreshRunnable;
    private Runnable mRefreshFlingRunnable;

    // used to get video url!
    private Runnable mVideoUrlRunnable;

    private static final String VIDEO_URL_PREFIX = "xxx:";

    private boolean mSeeking;

    private ImageButton mPlayPauseButton;
    private SeekBar mMediaSeekBar;
    private TextView mFlingCurrentTimeTextView;
    private TextView mFlingTotalTimeTextView;
    private TextView mFlingDeviceNameTextView;

    private TextView mVideoResolutionTextView;

    private int mPlayerState;

    private boolean mIsUserSeeking;

    private MediaFlingBar mMediaFlingBar;

    private CheckBox mHardwareDecoderCheckbox;

    private CheckBox mAutoplayCheckbox;

    private boolean mShouldAutoPlayMedia = true;

    private FlintVideoManager mFlintVideoManager;
    private ImageButton mMediaRouteButton;

    private ImageButton mVideoRefreshBtn;

    private ProgressBar mVideoRefreshProgressBar;

    private String mSelectedVideoUrlByApi;

    private ShowcaseView mShowcaseView;

    private final ApiUtils apiUtils = new ApiUtils();

    private int mCounter = 0;

    private static final int HINT_SINGLE_ID = 0x123456;

    private boolean mIsZh = true;

    private SSLContext mSslcontext = null;

    MyTrustManager tm = null;

    X509HostnameVerifier mHostnameVerifier = null;

    HttpsURLConnection httpsConn = null;

    private Runnable mGetVideoUrlRunnable;

    private String mCurrentUrl = null;

    private javax.net.ssl.SSLSocketFactory mSSLSocketFactory;
    
    private FeedbackAgent mFeedbackAgent;

    /**
     * Init all Flint related
     */
    private void initFlint() {

        MobclickAgent.updateOnlineConfig(mContext);

        UmengUpdateAgent.update(this);

        try {
            mSslcontext = SSLContext.getInstance("TLS");
            tm = new MyTrustManager();
            mSslcontext.init(null, new TrustManager[] { tm },
                    new java.security.SecureRandom());
            mHostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            mSSLSocketFactory = mSslcontext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String lang = Locale.getDefault().getLanguage();
        if (lang.equals("zh")) {
            mIsZh = true;
        } else {
            //mIsZh = false; // does not send get video url request in other lang???
        }

        Log.e(TAG, "initFlingServerSocket!");

        mMediaFlingBar = (MediaFlingBar) findViewById(R.id.media_fling);
        mMediaFlingBar.show();

        mMediaRouteButton = (ImageButton) mMediaFlingBar
                .findViewById(R.id.media_route_button);

        mMediaRouteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                mFlintVideoManager.doMediaRouteButtonClicked();
            }

        });

        mFlintVideoManager = new FlintVideoManager(this, APPLICATION_ID, this);

        mPlayPauseButton = (ImageButton) mMediaFlingBar
                .findViewById(R.id.mediacontroller_play_pause);
        mPlayPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerState == PLAYER_STATE_FINISHED) {
                    doPlay();
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

        mMediaSeekBar = (SeekBar) mMediaFlingBar
                .findViewById(R.id.mediacontroller_seekbar);
        mMediaSeekBar
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mIsUserSeeking = false;

                        mMediaSeekBar.setSecondaryProgress(0);
                        onSeekBarMoved(TimeUnit.SECONDS.toMillis(seekBar
                                .getProgress()));

                        refreshSeekPosition(TimeUnit.SECONDS.toMillis(seekBar
                                .getProgress()), mFlintVideoManager
                                .getMediaDuration());
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mIsUserSeeking = true;

                        refreshSeekPosition(TimeUnit.SECONDS.toMillis(seekBar
                                .getProgress()), mFlintVideoManager
                                .getMediaDuration());

                        mMediaSeekBar.setSecondaryProgress(seekBar
                                .getProgress());
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                            int progress, boolean fromUser) {

                        refreshSeekPosition(TimeUnit.SECONDS.toMillis(seekBar
                                .getProgress()), mFlintVideoManager
                                .getMediaDuration());
                    }
                });

        mFlingCurrentTimeTextView = (TextView) mMediaFlingBar
                .findViewById(R.id.mediacontroller_time_current);
        mFlingTotalTimeTextView = (TextView) mMediaFlingBar
                .findViewById(R.id.mediacontroller_time_total);

        mFlingDeviceNameTextView = (TextView) mMediaFlingBar
                .findViewById(R.id.fling_device_name);

        mVideoResolutionTextView = (TextView) mMediaFlingBar
                .findViewById(R.id.resolution);

        mVideoResolutionTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (listDialog != null) {
                    listDialog.dismiss();
                }
                Log.e(TAG, "onClick!");

                initListDialog();
            }

        });

        hideVideoResolutionView();

        setPlayerState(PLAYER_STATE_NONE);

        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (mQuit) {
                    Log.e(TAG, "mRefreshRunnable:quit!");
                    return;
                }

                Log.e(TAG, "show media cast control?!["
                        + DiscoveryManager.getInstance().getCompatibleDevices()
                                .size() + "]");

                if (DiscoveryManager.getInstance().getCompatibleDevices()
                        .size() > 0) {

                    if (mCurrentView == null) {
                        return;
                    }

                    mCurrentUrl = mCurrentView.getUrl();

                    setCurrentVideoTitle(mCurrentView.getTitle());

                    Toast.makeText(mContext, getCurrentVideoUrl(),
                            Toast.LENGTH_SHORT).show();

                    mMediaFlingBar.show();

                    // hide
                    hideVideoResolutionView();

                    final String url = mCurrentView.getUrl();
                    if (!url.equals(mSiteUrl) && mIsZh) {
                        try {
                            synchronized (mGetVideoUrlRunnable) {
                                mGetVideoUrlRunnable.notify();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        autoPlayIfIsNecessary(getCurrentVideoUrl());
                    }
                }
            }

        };

        mRefreshFlingRunnable = new Runnable() {
            @Override
            public void run() {
                if (mQuit) {
                    Log.e(TAG, "mRefreshFlingRunnable:quit!");
                    return;
                }

                if (mCurrentView != null) {
                    mCurrentUrl = mCurrentView.getUrl();
                }

                onRefreshEvent();

                startRefreshTimer();
            }
        };

        mGetVideoUrlRunnable = new Runnable() {
            @Override
            public void run() {
                while (!mQuit) {
                    try {
                        synchronized (this) {
                            Log.e(TAG, "mGetVideoUrlRunnable:wait!");
                            this.wait();
                            Log.e(TAG, "mGetVideoUrlRunnable:quit wait!!");
                        }

                        if (mQuit) {
                            Log.e(TAG, "mGetVideoUrlRunnable:quit!");
                            return;
                        }

                        final String url = mCurrentUrl;

                        getVideoPlayUrlByApi(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Log.e(TAG, "mGetVideoUrlRunnable:quit!");
            }
        };

        new Thread(mGetVideoUrlRunnable).start();

        // use this thread to get video url!
        mVideoUrlRunnable = new Runnable() {
            @Override
            public void run() {
                if (mQuit) {
                    Log.e(TAG, "mVideoUrlRunnable:quit!");
                    return;
                }

                // Get Video's url.
                if (mCurrentView != null && !isKeyBoardOpened) {
                    // Log.e(TAG, "try to extract real video url!");
                    String GET_VIDEO_URL_SCRIPT = "(function () {var videos = document.getElementsByTagName('video'); if (videos != null && videos[0] != null) {alert('xxx:' + videos[0].src);}})();";
                    mCurrentView.getWebView().loadUrl(
                            "javascript:" + GET_VIDEO_URL_SCRIPT);
                }

                mHandler.removeCallbacks(mVideoUrlRunnable);

                mHandler.postDelayed(mVideoUrlRunnable, REFRESH_INTERVAL_MS);
            }
        };
        mHandler.postDelayed(mVideoUrlRunnable, REFRESH_INTERVAL_MS);

        mHardwareDecoderCheckbox = (CheckBox) mMediaFlingBar
                .findViewById(R.id.device_hardware_decoder);
        mHardwareDecoderCheckbox
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        // TODO Auto-generated method stub

                        Log.e(TAG, "setHardwareDecoder:" + isChecked);
                    }

                });

        mAutoplayCheckbox = (CheckBox) mMediaFlingBar
                .findViewById(R.id.media_auto_play);
        mAutoplayCheckbox
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        // TODO Auto-generated method stub

                        Log.e(TAG, "auto play:" + isChecked);

                        mShouldAutoPlayMedia = isChecked;

                        if (mShouldAutoPlayMedia) {
                            doPlay();
                        }
                    }
                });

        mVideoRefreshBtn = (ImageButton) mMediaFlingBar
                .findViewById(R.id.media_get_video_url_btn);
        mVideoRefreshBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mCurrentView == null || !mIsZh) {
                    return;
                }

                updateGetVideoRealBtnStatus(false);

                mCurrentUrl = mCurrentView.getUrl();
                
                try {
                    synchronized (mGetVideoUrlRunnable) {
                        mGetVideoUrlRunnable.notify();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        mVideoRefreshProgressBar = (ProgressBar) mMediaFlingBar
                .findViewById(R.id.media_get_video_url_progressbar);

        // show flint hints
        showHint();
        
        mFlintVideoManager.onStart();
    }

    /**
     * clear media status when application disconnected.
     */
    private void clearMediaState() {
        mSeeking = false;

        refreshPlaybackPosition(0, 0);
    }

    /**
     * show or hide device name or other views when device connected.
     * 
     * @param show
     */
    private void updateFlingDispInfo(boolean show) {
        if (show) {
            mFlingDeviceNameTextView.setVisibility(View.VISIBLE);
        } else {
            mFlingDeviceNameTextView.setVisibility(View.GONE);
            mFlingDeviceNameTextView.setText("");
        }
    }

    /**
     * Update all views according to current application status.
     */
    private void updateButtonStates() {
        boolean hasMediaConnection = mFlintVideoManager.isMediaConnected();

        if (hasMediaConnection) {
            mFlingDeviceNameTextView.setText(mFlintVideoManager
                    .getCurrentSelectDeviceName());

            PlayStateStatus mediaStatus = mFlintVideoManager.getMediaStatus();
            Log.e(TAG, "mediaStatus:" + mediaStatus);
            if (mediaStatus != null) {
                int playerState = PLAYER_STATE_NONE;
                if (mediaStatus == PlayStateStatus.Paused) {
                    playerState = PLAYER_STATE_PAUSED;
                } else if (mediaStatus == PlayStateStatus.Playing) {
                    playerState = PLAYER_STATE_PLAYING;
                } else if (mediaStatus == PlayStateStatus.Buffering) {
                    mFlingDeviceNameTextView.setText(mFlintVideoManager
                            .getCurrentSelectDeviceName() + "(Buffering...)");
                    playerState = PLAYER_STATE_BUFFERING;
                } else if (mediaStatus == PlayStateStatus.Finished) {
                    Log.e(TAG, "PlayStateStatus.Finished");
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
     * Set current playback's position
     * 
     * @param position
     * @param duration
     */
    protected final void refreshPlaybackPosition(long position, long duration) {
        if (!mIsUserSeeking) {
            if (position == 0) {
                mFlingTotalTimeTextView.setText("N/A");
                mMediaSeekBar.setProgress(0);
            } else if (position > 0) {
                mMediaSeekBar.setProgress((int) TimeUnit.MILLISECONDS
                        .toSeconds(position));
            }
            mFlingCurrentTimeTextView.setText(formatTime(position));
        }

        if (duration == 0) {
            mMediaSeekBar.setProgress(0);
            mFlingTotalTimeTextView.setText("N/A");
            mMediaSeekBar.setMax(0);
        } else if (duration > 0) {
            mFlingTotalTimeTextView.setText(formatTime(duration));
            if (!mIsUserSeeking) {
                mMediaSeekBar.setMax((int) TimeUnit.MILLISECONDS
                        .toSeconds(duration));
            }
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
     * stop timer to stop refresh current playback's UI.
     */
    private final void cancelRefreshTimer() {
        mHandler.removeCallbacks(mRefreshFlingRunnable);
    }

    /**
     * start timer to update current playback's UI.
     */
    private final void startRefreshTimer() {
        mHandler.removeCallbacks(mRefreshFlingRunnable);

        mHandler.postDelayed(mRefreshFlingRunnable, REFRESH_INTERVAL_MS);
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
    protected void onRefreshEvent() {
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
            mPlayPauseButton.setImageResource(R.drawable.mediacontroller_play);
        } else if (mPlayerState == PLAYER_STATE_PLAYING) {
            mPlayPauseButton.setImageResource(R.drawable.mediacontroller_pause);
        } else {
            mPlayPauseButton.setImageResource(R.drawable.mediacontroller_play);
        }

        mPlayPauseButton.setEnabled((mPlayerState == PLAYER_STATE_PAUSED)
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
        mMediaSeekBar.setEnabled(enabled);
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

            Log.e("DLNA", "volumeIncrement:" + volumeIncrement + " v[" + v
                    + "]");
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
        mFlingCurrentTimeTextView.setText(formatTime(position));
    }

    /**
     * Get current video's resolution name.
     * 
     * @return
     */
    public String getCurrentResolution() {
        return mVideoResolutionTextView.getText().toString();
    }

    @Override
    public void onDeviceSelected(String name) {
        // TODO Auto-generated method stub

        if (getCurrentVideoUrl() == null) {
            Log.d(TAG, "url is " + getCurrentVideoUrl() + " ignore it!");
            Toast.makeText(BrowserActivity.this,
                    getString(R.string.flint_empty_video_url),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mMediaRouteButton
                .setImageResource(R.drawable.mr_ic_media_route_on_holo_dark);

        // ready to play media
        mFlintVideoManager.playVideo(getCurrentVideoUrl(),
                getCurrentVideoTitle());

        updateButtonStates();

        // show device info
        mFlingDeviceNameTextView.setText(name + "(Loading...)");
        updateFlingDispInfo(true);
    }

    @Override
    public void onDeviceUnselected() {
        // TODO Auto-generated method stub
        Log.e(TAG, "onDeviceUnselected!");

        mMediaRouteButton
                .setImageResource(R.drawable.mr_ic_media_route_off_holo_dark);

        cancelRefreshTimer();

        clearMediaState();
        updateButtonStates();
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
                mMediaRouteButton
                        .setImageResource(R.drawable.mr_ic_media_route_off_holo_dark);

                updateButtonStates();
                clearMediaState();
                cancelRefreshTimer();
                // showErrorDialog(getString(R.string.error_no_device_connection));
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

    private class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // TODO Auto-generated method stub
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)

        throws CertificateException {
            // TODO Auto-generated method stub
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    /**
     * Send POST request.
     * 
     * @param url
     * @param param
     * @param data
     * @return
     */
    public String SendHttpsPOST(String url, List<NameValuePair> param,
            String data) {
        String result = null;
        Log.e(TAG, "SendHttpsPOST!");

        // 使用此工具可以将键值对编码成"Key=Value&amp;Key2=Value2&amp;Key3=Value3&rdquo;形式的请求参数
        String requestParam = URLEncodedUtils.format(param, "UTF-8");

        try {
            URL requestUrl = new URL(url);
            httpsConn = (HttpsURLConnection) requestUrl.openConnection();

            httpsConn.setSSLSocketFactory(mSSLSocketFactory);
            httpsConn.setHostnameVerifier(mHostnameVerifier);

            // POST
            httpsConn.setRequestMethod("POST");

            httpsConn.setConnectTimeout(5000);
            httpsConn.setDoOutput(true);
            httpsConn.setDoInput(true);
            httpsConn.setUseCaches(false);
            httpsConn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            // send the POST request to server
            OutputStream outputStream = null;
            try {
                outputStream = httpsConn.getOutputStream();
                outputStream.write(requestParam.toString().getBytes("utf-8"));
                outputStream.flush();
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }

            int code = httpsConn.getResponseCode();
            if (HttpsURLConnection.HTTP_OK == code) {

                // 获取输入流
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        httpsConn.getInputStream()));

                String temp = in.readLine();
                /* 连接成一个字符串 */
                while (temp != null) {
                    if (result != null)
                        result += temp;
                    else
                        result = temp;
                    temp = in.readLine();
                }
                in.close();

                Log.e(TAG, "SendHttpsPOST:response[" + result + "]");

                // ready to processs video urls!
                processVideoUrls(result);
            }

            Log.e(TAG, "SendHttpsPOST![" + code + "]");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpsConn != null) {
                httpsConn.disconnect();
                httpsConn = null;
            }
        }

        return result;
    }

    /**
     * Get video url list
     * 
     * @param result
     */
    private void processVideoUrls(String result) {
        videoUrls.clear();

        // {"status":200,"data":[{"url":"http:\/\/v.youku.com\/v_show\/id_XODUwMDM0NDUy.html?from=y1.3-tv-grid-1007-9910.86804.1-1","id":"","num":"","vid":"","source":"youku","sourceName":"\u4f18\u9177\u89c6\u9891","playUrl":{"HD":["http:\/\/pl.youku.com\/playlist\/m3u8?ts=1427076444&keyframe=1&vid=XODUwMDM0NDUy&type=hd2&sid=442707644496121c78ca6&token=5158&oip=1008521675&ep=v4TS4z2PnxtMZfqTd5f%2FdgrHMEbE4Lhvk9YdQoGTJsv7lbbElD2WtWp9mT7DI5SF&did=3f2189e6a744e68de6761a20ceaf379aa8acfad4&ctype=21&ev=1"],"SD":["http:\/\/pl.youku.com\/playlist\/m3u8?ts=1427076444&keyframe=1&vid=XODUwMDM0NDUy&type=mp4&sid=442707644496121c78ca6&token=5158&oip=1008521675&ep=v4TS4z2PnxtMZfqTd5f%2FdgrHMEbE4Lhvk9YdQoGTJsv7lbbElD2WtWp9mT7DI5SF&did=3f2189e6a744e68de6761a20ceaf379aa8acfad4&ctype=21&ev=1"]}}]}
        try {
            JSONObject obj = new JSONObject(result);
            String status = obj.getString("status");

            if ("200".equals(status)) {
                JSONArray data = obj.getJSONArray("data");

                JSONObject r = data.getJSONObject(0);
                final String url = r.getString("url");
                JSONObject playUrl = r.getJSONObject("playUrl");

                String label = "";

                try {
                    JSONArray Smooth = playUrl.getJSONArray("Smooth");
                    videoUrls.put(getString(R.string.resolution_Smooth),
                            Smooth.getString(0));
                    label = getString(R.string.resolution_Smooth);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                try {
                    JSONArray SD = playUrl.getJSONArray("SD");
                    videoUrls.put(getString(R.string.resolution_SD),
                            SD.getString(0));
                    label = getString(R.string.resolution_SD);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                try {
                    JSONArray HD = playUrl.getJSONArray("HD");
                    videoUrls.put(getString(R.string.resolution_HD),
                            HD.getString(0));

                    label = getString(R.string.resolution_HD);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                try {
                    JSONArray Ultraclear = playUrl.getJSONArray("Ultraclear");
                    videoUrls.put(getString(R.string.resolution_Ultraclear),
                            Ultraclear.getString(0));

                    label = getString(R.string.resolution_Ultraclear);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                try {
                    JSONArray Bluray = playUrl.getJSONArray("Bluray");
                    videoUrls.put(getString(R.string.resolution_Bluray),
                            Bluray.getString(0));

                    label = getString(R.string.resolution_Bluray);
                } catch (Exception e) {
                    // e.printStackTrace();
                }

                Log.e(TAG,
                        "playUrl:" + playUrl.toString() + "["
                                + videoUrls.toString() + "]");

                final String videoQualityLabel = label;

                if (videoUrls.size() > 0) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub

                            mSiteUrl = url;

                            if (listDialog != null) {
                                listDialog.setDialogTitile(url);
                            }

                            videoList.clear();

                            videoList.addAll(videoUrls.keySet());

                            mMediaFlingBar.show();

                            mVideoResolutionTextView.setText(videoQualityLabel);

                            setCurrentVideoUrl(videoUrls.get(videoQualityLabel));

                            mVideoResolutionTextView
                                    .setVisibility(View.VISIBLE);

                            updateGetVideoRealBtnStatus(true);

                            autoPlayIfIsNecessary(videoUrls
                                    .get(videoQualityLabel));
                        }

                    });
                } else {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub

                            hideVideoResolutionView();

                            updateGetVideoRealBtnStatus(true);

                            autoPlayIfIsNecessary(getCurrentVideoUrl());
                        }

                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub

                    hideVideoResolutionView();

                    updateGetVideoRealBtnStatus(true);

                    autoPlayIfIsNecessary(getCurrentVideoUrl());
                }

            });
        }
    }

    private CustomDialog listDialog;
    Map<String, String> videoUrls = new HashMap<String, String>();
    ArrayList<String> videoList = new ArrayList<String>();

    private String mSiteUrl;

    /**
     * Show video's play url list
     */
    private void initListDialog() {
        listDialog = CustomDialog.createListDialog(this,
                new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                            int arg2, long arg3) {
                        listDialog.dismiss();

                        // disable auto play
                        mAutoplayCheckbox.setChecked(false);
                        mShouldAutoPlayMedia = false;

                        setCurrentVideoUrl(videoUrls.get(videoList.get(arg2)));

                        mVideoResolutionTextView.setText(videoList.get(arg2));

                        if (mFlintVideoManager.isMediaConnected()) {
                            mFlintVideoManager.playVideo(
                                    videoUrls.get(videoList.get(arg2)),
                                    getCurrentVideoTitle());
                        }
                    }
                });
        if (mSiteUrl != null) {
            final String title = mCurrentView.getTitle();
            listDialog.setDialogTitile(title);
        } else {
            listDialog.setDialogTitile(getResources().getString(
                    R.string.custom_dialog_list_title_str));
        }

        videoList.clear();

        videoList.addAll(videoUrls.keySet());

        listDialog.setListData(videoList);
        listDialog.show();
    }

    /**
     * Hide video resolution view.
     */
    private void hideVideoResolutionView() {
        mVideoResolutionTextView.setText("");
        mVideoResolutionTextView.setVisibility(View.INVISIBLE);
    }

    /**
     * get video real play url by rabbit's api
     * 
     * @param url
     */
    private void getVideoPlayUrlByApi(final String url) {
        Log.e(TAG, "getVideoPlayUrlByApi: " + url + "]mCurrentView["
                + mCurrentView + "]");

        if (mCurrentView == null) {
            return;
        }

        // TODO Auto-generated method stub
        List<NameValuePair> param = new ArrayList<NameValuePair>();

        param.add(new BasicNameValuePair("apptoken",
                "3e52201f5037ad9bd8e389348916bd3a"));
        param.add(new BasicNameValuePair("method", "core.video.realurl"));
        param.add(new BasicNameValuePair("packageName", "com.infthink.test"));
        param.add(new BasicNameValuePair("url", url));

        Log.e(TAG, "get real video url[" + url + "]site[" + mSiteUrl + "]");

        SendHttpsPOST("https://play.aituzi.com", param, null);
    }

    /**
     * show or hide views related with get video's real play url.
     * 
     * @param show
     */
    void updateGetVideoRealBtnStatus(boolean show) {
        if (!show) {
            mVideoRefreshBtn.setVisibility(View.GONE);
            mVideoRefreshProgressBar.setVisibility(View.VISIBLE);
        } else {
            mVideoRefreshBtn.setVisibility(View.VISIBLE);
            mVideoRefreshProgressBar.setVisibility(View.GONE);
        }

    }

    /**
     * auto fling if necessary
     */
    private void autoPlayIfIsNecessary(String url) {
        if (!mShouldAutoPlayMedia || url == null) {
            return;
        }

        Log.e(TAG, "should show!");

        if (mFlintVideoManager.isMediaConnected()) {
            mFlintVideoManager.playVideo(url, getCurrentVideoTitle());
        }
    }

    private String mFetchedVideoUrl;

    public void notifyGetVideoUrl(String url) {
        if ((url != null && url.startsWith(VIDEO_URL_PREFIX))
                && url.length() > 4
                && (mFetchedVideoUrl == null || !mFetchedVideoUrl.equals(url
                        .substring(4)))) {
            Log.e(TAG, "Get valid video Url[" + url + "]fetched["
                    + mFetchedVideoUrl + "]");

            mFetchedVideoUrl = url.substring(4);

            setCurrentVideoUrl(mFetchedVideoUrl);
            mHandler.postDelayed(mRefreshRunnable, 0);
        }
    }

    /**
     * Use this to show user some hints on UI about how to use Flint functions.
     */
    private void showHint() {
        if (mShowcaseView == null) {
            mShowcaseView = new ShowcaseView.Builder(this, true)
                    .setTarget(new ViewTarget(mCurrentView.getWebView()))
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .singleShot(HINT_SINGLE_ID)
                    .setContentTitle(
                            getString(R.string.flint_hint_webview_title))
                    .setContentText(
                            getString(R.string.flint_hint_webview_details))
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            Log.e(TAG, "showHint:mCounter" + mCounter);
                            switch (mCounter) {
                            case 0:
                                mShowcaseView.setShowcase(new ViewTarget(
                                        mMediaRouteButton), true);
                                mShowcaseView
                                        .setContentTitle(getString(R.string.flint_hint_control_title));
                                mShowcaseView
                                        .setContentText(getString(R.string.flint_hint_control_details));
                                break;

                            case 1:
                                mShowcaseView.setShowcase(new ViewTarget(
                                        mVideoRefreshBtn), true);
                                mShowcaseView
                                        .setContentTitle(getString(R.string.flint_hint_video_quality_title));
                                mShowcaseView
                                        .setContentText(getString(R.string.flint_hint_video_quality_details));
                                break;

                            case 2:
                                mShowcaseView.setTarget(Target.NONE);
                                mShowcaseView
                                        .setContentTitle(getString(R.string.flint_hint_final_title));
                                mShowcaseView
                                        .setContentText(getString(R.string.flint_hint_final_details));
                                mShowcaseView
                                        .setButtonText(getString(R.string.flint_hint_close));
                                setAlpha(0.4f, mMediaRouteButton,
                                        mVideoRefreshBtn,
                                        mCurrentView.getWebView());
                                break;

                            case 3:
                                mShowcaseView.hide();
                                setAlpha(1.0f, mMediaRouteButton,
                                        mVideoRefreshBtn,
                                        mCurrentView.getWebView());
                                break;
                            }
                            mCounter++;
                        }

                    }).build();
        }
        mShowcaseView.setButtonText(getString(R.string.flint_hint_next));
        mShowcaseView.setShouldCentreText(false);
    }

    /**
     * set alpha
     * 
     * @param alpha
     * @param views
     */
    private void setAlpha(float alpha, View... views) {
        if (apiUtils.isCompatWithHoneycomb()) {
            for (View view : views) {
                view.setAlpha(alpha);
            }
        }
    }

    /**
     * directly play current video
     */
    private void doPlay() {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (DiscoveryManager.getInstance().getCompatibleDevices()
                        .size() > 0) {
                    autoPlayIfIsNecessary(getCurrentVideoUrl());
                }

            }
        }, 50);
    }
}
