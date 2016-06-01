package com.axaet.ibeacondemo;

import com.axaet.demo.adpter.DeviceAdpter;
import com.axaet.ibeacon.beans.iBeaconClass;
import com.axaet.ibeacon.beans.iBeaconClass.iBeacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 
 * @date 2016年5月31日
 * @author yuShu
 * @category scanning Activity
 *
 */
public class MainActivity extends Activity {

	private ListView mListView;

	private DeviceAdpter deviceAdpter;
	/**
	 * Bluetooth data processing object
	 */
	private iBeaconClass iBeaconClass;

	public BluetoothAdapter mBluetoothAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mListView = (ListView) findViewById(R.id.listview);
		deviceAdpter = new DeviceAdpter(this);
		mListView.setAdapter(deviceAdpter);
		iBeaconClass = com.axaet.ibeacon.beans.iBeaconClass.getInstance();
		
		// Check whether the current phone supports ble Bluetooth, if you do not
		// support the exit program
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
			finish();
		}
		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		}
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				final iBeacon device = deviceAdpter.getItem(arg2);
				// Only when the device name is pBeacon_n, the device is the
				// connection mode
				if (device.deviceName.contains("_n")) {
					Intent intent = new Intent(MainActivity.this, ModifyBeaconActivity.class);
					intent.putExtra("address", device.deviceAddress);
					intent.putExtra("deviceName", device.deviceName);
					startActivity(intent);
				} else {
					Toast.makeText(MainActivity.this,
							"In this mode, the device is not connected, please enter the connection mode.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		mBluetoothAdapter.startLeScan(mLeScanCallback);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_stratscan) {
			deviceAdpter.clearData();
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else if (id == R.id.action_stopscan) {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		return true;
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (device != null && rssi != 127) {
				iBeacon beacon = iBeaconClass.formToiBeacon(device, rssi, scanRecord);
				deviceAdpter.addData(beacon);
			}
		}
	};

}
