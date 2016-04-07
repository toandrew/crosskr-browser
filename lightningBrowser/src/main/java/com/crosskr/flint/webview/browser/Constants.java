/*
 * Copyright 2014 A.C.R. Development
 */
package com.crosskr.flint.webview.browser;

import android.os.Environment;

public final class Constants {

	private Constants() {
	}

	public static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";
	public static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
	
	public static final String  IPAD_USER_AGENT = "Mozilla/5.0 (iPad; CPU OS 7_0_3 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11B511 Safari/9537.53";
	public static final String  IPHONE_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 7_0 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53";
    public static final String  ANDROID_TABLET_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.0.3; en-gb; HTC_Desire_C Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
    
	public static final int API = android.os.Build.VERSION.SDK_INT;
	public static final String YAHOO_SEARCH = "http://search.yahoo.com/search?p=";
	public static final String GOOGLE_SEARCH = "https://www.google.com/search?client=lightning&ie=UTF-8&oe=UTF-8&q=";
	public static final String BING_SEARCH = "http://www.bing.com/search?q=";
	public static final String DUCK_SEARCH = "https://duckduckgo.com/?t=lightning&q=";
	public static final String DUCK_LITE_SEARCH = "https://duckduckgo.com/lite/?t=lightning&q=";
	public static final String STARTPAGE_MOBILE_SEARCH = "https://startpage.com/do/m/mobilesearch?language=english&query=";
	public static final String STARTPAGE_SEARCH = "https://startpage.com/do/search?language=english&query=";
	public static final String ANDROID_SEARCH = "http://www.androidsearchresult.com/search.pg?aff=olb&keyword=";
	public static final String HOMEPAGE = "about:home";
	public static final String BAIDU_SEARCH = "http://www.baidu.com/s?wd=";
	public static final String YANDEX_SEARCH = "http://yandex.ru/yandsearch?lr=21411&text=";
	public static final String EXTERNAL_STORAGE = Environment.getExternalStorageDirectory()
			.toString();

	public static final String SEPARATOR = "\\|\\$\\|SEPARATOR\\|\\$\\|";
	public static final String HTTP = "http://";
	public static final String HTTPS = "https://";
	public static final String FILE = "file://";
	public static final String FOLDER = "folder://";
	public static final String TAG = "Lightning";
}
