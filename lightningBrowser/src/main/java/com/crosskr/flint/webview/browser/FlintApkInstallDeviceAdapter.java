package com.crosskr.flint.webview.browser;

import java.util.List;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.connectsdk.device.ConnectableDevice;

public class FlintApkInstallDeviceAdapter extends BaseAdapter {
    private static final String TAG = "FlintApkInstallDeviceAdapter";
    private List<ConnectableDevice> mAvailableDevices;
    private FlintApkInstallActivity mFlintApkInstallActivity;
    private int mItemCount;

    public FlintApkInstallDeviceAdapter(FlintApkInstallActivity context,
            List<ConnectableDevice> availableDevices) {
        mAvailableDevices = availableDevices;
        mFlintApkInstallActivity = context;
    }

    @Override
    public int getCount() {
        mItemCount = mAvailableDevices.size();
        return mItemCount;

    }

    @Override
    public Object getItem(int position) {
        return mAvailableDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mFlintApkInstallActivity)
                    .inflate(R.layout.install_apk_list_item, null);
            viewHolder.mDeviceNameTextView = (TextView) convertView
                    .findViewById(R.id.device_list_item_device_name);
            viewHolder.mInstallFileNameTextView = (TextView) convertView
                    .findViewById(R.id.device_list_item_device_install_file_name);
            viewHolder.mInstallPercentTextView = (TextView) convertView
                    .findViewById(R.id.device_list_item_device_install_percent);
            viewHolder.mDeviceSelectedImage = (ImageView) convertView
                    .findViewById(R.id.device_list_item_selected_img);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ConnectableDevice deviceInfo = mAvailableDevices.get(position);
        if (deviceInfo == null) {
            return convertView;
        }

        Log.d(TAG, "deviceInfo_adapter:" + deviceInfo.getFriendlyName());

        viewHolder.mDeviceNameTextView.setText(deviceInfo.getFriendlyName());

        if (deviceInfo.isConnected()) {
            viewHolder.mDeviceSelectedImage.setVisibility(View.VISIBLE);
            viewHolder.mDeviceSelectedImage
                    .setImageResource(R.drawable.icon_device_selected);
            viewHolder.mInstallFileNameTextView
                    .setText(mFlintApkInstallActivity.getInstallApkName());
            viewHolder.mInstallPercentTextView.setText("(" + mFlintApkInstallActivity
                    .getCurrentInstallPercent() + "%)");
        } else {
            viewHolder.mDeviceSelectedImage.setVisibility(View.INVISIBLE);
            viewHolder.mInstallFileNameTextView.setText(deviceInfo.getIpAddress());
            viewHolder.mInstallPercentTextView.setText(deviceInfo.getModelName());
        }

        return convertView;
    }

    final class ViewHolder {
        public ViewHolder() {
            // TODO Auto-generated constructor stub
        }

        public TextView mDeviceNameTextView;
        public TextView mInstallFileNameTextView;
        public TextView mInstallPercentTextView;
        public ImageView mDeviceSelectedImage;
    }
}
