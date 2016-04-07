package com.crosskr.flint.webview.browser;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

public class MediaFlingBar extends RelativeLayout {
    private static final String LOGTAG = "MediaFlingBar";

    private boolean mInflated;

    public MediaFlingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void inflateContent() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View content = inflater.inflate(R.layout.mediacontroller, this);

        mInflated = true;
    }

    public void show() {
        if (!mInflated)
            inflateContent();

        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void onDestroy() {
    }

}
