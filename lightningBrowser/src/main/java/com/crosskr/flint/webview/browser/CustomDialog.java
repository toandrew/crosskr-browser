package com.crosskr.flint.webview.browser;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 自定义的Dialog 可对应list模式，加载模式，取消按钮模式，可添加自定义view
 * 
 * @author TGZ
 * 
 */
public class CustomDialog extends Dialog {

    private Context context;

    private static CustomDialog customDialog = null;

    private static ListView listView;

    private LayoutParams layoutParams;

    private Button enterBtn;

    private Button cancelBtn;

    private BrowserActivity mBrowserActivity;

    protected CustomDialog(Context context, int theme) {
        super(context, theme);

        mBrowserActivity = (BrowserActivity) context;

        this.context = context;
        layoutParams = new LayoutParams(Utils.getScreenWidth(context) / 7 * 6,
                LayoutParams.WRAP_CONTENT);
        View view = LayoutInflater.from(context).inflate(
                R.layout.custom_dialog, null);
        this.setContentView(view, layoutParams);
        this.getWindow().getAttributes().gravity = Gravity.CENTER;
    }

    protected CustomDialog(Context context, int theme, int layout) {
        super(context, theme);

        this.context = context;
        layoutParams = new LayoutParams(Utils.getScreenWidth(context) / 7 * 6,
                LayoutParams.WRAP_CONTENT);
        View view = LayoutInflater.from(context).inflate(layout, null);
        this.setContentView(view, layoutParams);
        this.getWindow().getAttributes().gravity = Gravity.CENTER;
    }

    public static CustomDialog createProgressDialog(Context context,
            OnKeyListener onKeyListener) {

        customDialog = new CustomDialog(context, R.style.CustomDialogStyle);
        customDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        customDialog.setOnKeyListener(onKeyListener);
        customDialog.findViewById(R.id.custom_dialog_progress_mode_layout)
                .setVisibility(View.VISIBLE);
        return customDialog;
    }

    public static CustomDialog createListDialog(Context context,
            OnItemClickListener onItemClickListener) {

        customDialog = new CustomDialog(context, R.style.CustomDialogStyle);
        customDialog.findViewById(R.id.custom_dialog_list_mode_layout)
                .setVisibility(View.VISIBLE);
        listView = (ListView) customDialog
                .findViewById(R.id.custom_dialog_list_view);
        listView.setOnItemClickListener(onItemClickListener);
        customDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        return customDialog;
    }

    public static CustomDialog createCancelDialog(Context context,
            android.view.View.OnClickListener onClickListener) {

        customDialog = new CustomDialog(context, R.style.CustomDialogStyle);
        customDialog.findViewById(R.id.custom_dialog_cancle_mode_layout)
                .setVisibility(View.VISIBLE);
        customDialog.findViewById(R.id.custom_dialog_cancel_mode_cancel_btn)
                .setOnClickListener(onClickListener);
        customDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        return customDialog;
    }

    public static CustomDialog createCustomDialog(Context context) {

        customDialog = new CustomDialog(context, R.style.CustomDialogStyle);
        customDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        return customDialog;
    }

    /*
     * public static CustomDialog createWhiteCustomDialog(Context context) {
     * 
     * customDialog = new CustomDialog(context, R.style.CustomWhiteDialogStyle,
     * R.layout.white_custom_dialog);
     * customDialog.getWindow().getAttributes().gravity = Gravity.CENTER; return
     * customDialog;
     * 
     * }
     */

    public void onWindowFocusChanged(boolean hasFocus) {

        if (customDialog == null) {
            return;
        }
    }

    /**
     * 
     * setTitile 标题
     * 
     * @param titleStr
     * @return
     * 
     */
    public CustomDialog setDialogTitile(String titleStr) {

        TextView title = (TextView) customDialog
                .findViewById(R.id.custom_dialog_title);

        if (title != null) {
            title.setText(titleStr);
        }
        return customDialog;
    }

    /**
     * 
     * setProgressMessage 主要信息
     * 
     * @param msg
     * @return
     * 
     */
    public CustomDialog setProgressDialogMessage(String messageStr) {

        TextView message = (TextView) customDialog
                .findViewById(R.id.custom_dialog_progress_message);

        if (message != null) {
            message.setText(messageStr);
        }
        return customDialog;
    }

    /**
     * 设置自定义提示信息
     * 
     * @param messageStr
     * @return
     */
    public CustomDialog setCustomDialogMessage(String messageStr) {

        customDialog.findViewById(R.id.custom_dialog_message_layout)
                .setVisibility(View.VISIBLE);
        ;
        TextView message = (TextView) customDialog
                .findViewById(R.id.custom_dialog_message);

        if (message != null) {
            message.setText(messageStr);
        }
        return customDialog;
    }

    /**
     * 设置自定义确定按钮
     * 
     * @param messageStr
     * @return
     */
    public CustomDialog setCustomEnterButton(String btnText,
            final CustomDialogListener customDialogListener) {

        if (customDialog.findViewById(R.id.custom_dialog_button_layout)
                .getVisibility() == View.GONE) {
            customDialog.findViewById(R.id.custom_dialog_button_layout)
                    .setVisibility(View.VISIBLE);
        }

        if (cancelBtn != null && cancelBtn.getVisibility() == View.VISIBLE) {
            customDialog.findViewById(
                    R.id.custom_dialog_button_layout_gary_line_1)
                    .setVisibility(View.VISIBLE);
        }

        enterBtn = (Button) customDialog
                .findViewById(R.id.custom_dialog_enter_btn);
        enterBtn.setVisibility(View.VISIBLE);

        if (customDialogListener != null) {
            enterBtn.setOnClickListener(new android.view.View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (customDialogListener == null
                            || !customDialogListener
                                    .setOnClick(customDialog, v)) {
                        dismiss();
                    }
                }
            });
        }

        if (btnText != null) {
            enterBtn.setText(btnText);
        }
        return customDialog;
    }

    /**
     * 设置自定义取消按钮
     * 
     * @param messageStr
     * @return
     */
    public CustomDialog setCustomCancelButton(String btnText,
            final CustomDialogListener customDialogListener) {

        if (customDialog.findViewById(R.id.custom_dialog_button_layout)
                .getVisibility() == View.GONE) {
            customDialog.findViewById(R.id.custom_dialog_button_layout)
                    .setVisibility(View.VISIBLE);
        }
        if (enterBtn != null && enterBtn.getVisibility() == View.VISIBLE) {
            customDialog.findViewById(
                    R.id.custom_dialog_button_layout_gary_line_1)
                    .setVisibility(View.VISIBLE);
        }

        cancelBtn = (Button) customDialog
                .findViewById(R.id.custom_dialog_cancel_btn);
        cancelBtn.setVisibility(View.VISIBLE);

        cancelBtn.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (customDialogListener == null
                        || !customDialogListener.setOnClick(customDialog, v)) {
                    dismiss();
                }
            }
        });

        if (btnText != null) {
            cancelBtn.setText(btnText);
        }
        return customDialog;
    }

    /**
     * 添加自定义view
     * 
     * @param view
     * @return
     */
    public CustomDialog addCustomView(View view) {

        LinearLayout customView = (LinearLayout) customDialog
                .findViewById(R.id.custom_dialog_custom_view_layout);
        customView.setVisibility(View.VISIBLE);
        customView.addView(view);
        return customDialog;
    }

    public CustomDialog setListData(ArrayList<String> datas) {

        listView.setAdapter(new ListAdapte(context, datas));
        return customDialog;
    }

    public class ListAdapte extends BaseAdapter {

        private Context context;

        private ArrayList<String> datas;

        public ListAdapte(Context context, ArrayList<String> datas) {
            this.context = context;
            this.datas = datas;
        }

        @Override
        public int getCount() {
            if (datas != null && datas.size() != 0) {
                return datas.size();
            }
            return 0;
        }

        @Override
        public String getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.custom_dialog_list_item, parent, false);
                holder = new ViewHolder();
                holder.resolutionName = (TextView) convertView
                        .findViewById(R.id.custom_dialog_list_item_resolution_name);
                holder.secectedImg = (ImageView) convertView
                        .findViewById(R.id.custom_dialog_list_item_selected_img);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (getItem(position) != null) {
                holder.resolutionName.setText(getItem(position));
                holder.secectedImg.setVisibility(View.INVISIBLE);

                if (getItem(position).equals(
                        mBrowserActivity.getCurrentResolution())) {
                    holder.secectedImg.setVisibility(View.VISIBLE);
                }
            }

            return convertView;
        }
    }

    static class ViewHolder {
        TextView resolutionName;
        ImageView secectedImg;
    }

    public interface CustomDialogListener {

        /**
         * if return false, dialog dismiss
         * 
         * @param customDialog
         * @param view
         * @return
         */
        public boolean setOnClick(CustomDialog customDialog, View view);
    }
}
