package com.axaet.demo.adpter;

import java.util.Collections;
import java.util.Comparator;

import com.axaet.ibeacon.beans.iBeaconClass.iBeacon;
import com.axaet.ibeacondemo.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DeviceAdpter extends CommonBaseAdpter<iBeacon> {

	public DeviceAdpter(Context context) {
		super(context);
	}


	public synchronized void addData(iBeacon device) {
		if (device == null)
			return;
		boolean b = false;
		for (iBeacon iBeacon : list) {
			b = iBeacon.deviceAddress.equals(device.deviceAddress);
			if (b) {
				list.remove(iBeacon);
				list.add(device);
				break;
			}
		}
		if (!b) {
			list.add(device);
		}
		Collections.sort(this.list, comparator);
		notifyDataSetChanged();
	}

	Comparator<iBeacon> comparator = new Comparator<iBeacon>() {
		@Override
		public int compare(iBeacon h1, iBeacon h2) {
			return h2.rssi - h1.rssi;
		}
	};

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder viewHolder;
		if (view == null) {
			view = inflater.inflate(R.layout.listitem_device, null);
			viewHolder = new ViewHolder();
			viewHolder.distanceMac = (TextView) view.findViewById(R.id.txt_mac);
			viewHolder.deviceName = (TextView) view.findViewById(R.id.txt_deviceName);
			viewHolder.deviceUUID = (TextView) view.findViewById(R.id.txt_uuid);
			viewHolder.deviceMajor = (TextView) view.findViewById(R.id.txt_major);
			viewHolder.deviceMinor = (TextView) view.findViewById(R.id.txt_minor);
			viewHolder.deviceRssi = (TextView) view.findViewById(R.id.txt_rssi);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		iBeacon device = list.get(position);
		viewHolder.deviceName.setText("Name:" + device.deviceName);
		viewHolder.distanceMac.setText("MAC:  " + device.deviceAddress);
		viewHolder.deviceUUID.setText("UUID:" + device.proximityUuid);
		viewHolder.deviceMajor.setText("Major:" + device.major);
		viewHolder.deviceMinor.setText("Minor:" + device.minor);
		viewHolder.deviceRssi.setText("Rssi:" + device.rssi);
		return view;
	}

	static class ViewHolder {
		TextView deviceName;
		TextView deviceUUID;
		TextView distanceMac;
		TextView deviceMajor;
		TextView deviceMinor;
		TextView deviceRssi;
	}

}
