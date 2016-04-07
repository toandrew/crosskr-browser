/*
 * Copyright 2014 A.C.R. Development
 */
package com.crosskr.flint.webview.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;

public class LightningDownloadListener implements DownloadListener {

    private Activity mActivity;

    LightningDownloadListener(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onDownloadStart(final String url, final String userAgent,
            final String contentDisposition, final String mimetype,
            long contentLength) {
        final String fileName = URLUtil.guessFileName(url, contentDisposition,
                mimetype);

        final boolean isApkFile = (fileName != null && fileName
                .endsWith(".apk"));

        String desc = mActivity.getResources().getString(R.string.do_fling);
        String action = mActivity.getResources().getString(
                R.string.action_fling);
        if (isApkFile) {
            action = mActivity.getResources()
                    .getString(R.string.action_install);
            desc = mActivity.getResources().getString(R.string.do_install);
        }

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // DownloadHandler.onDownloadStart(mActivity, url,
                    // userAgent,
                    // contentDisposition, mimetype, false);

                    Log.e("LightningDownloadListener", "Ready to fling url?["
                            + url + "]");

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    if (!isApkFile) {
                        intent.setClassName(
                                "com.crosskr.flint.webview.browser",
                                "com.infthink.miuicastsender.SenderDemo");
                    } else {
                        intent.setClassName(
                                "com.crosskr.flint.webview.browser",
                                "com.crosskr.flint.webview.browser.FlintApkInstallActivity");
                    }
                    intent.putExtra("mediaTitle", fileName);
                    intent.setData(Uri.parse(url));
                    mActivity.startActivity(intent);

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity); // dialog
        builder.setTitle(fileName)
                // .setMessage(
                // mActivity.getResources().getString(
                // R.string.dialog_download))
                // .setPositiveButton(
                // mActivity.getResources().getString(
                // R.string.action_download), dialogClickListener)
                .setMessage(desc)
                .setPositiveButton(action, dialogClickListener)
                .setNegativeButton(
                        mActivity.getResources().getString(
                                R.string.action_cancel), dialogClickListener)
                .show();
        Log.i(Constants.TAG, "Downloading" + fileName);

    }
}
