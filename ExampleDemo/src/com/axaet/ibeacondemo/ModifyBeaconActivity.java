package com.axaet.ibeacondemo;

import java.util.ArrayList;
import java.util.List;

import com.axaet.ibeacon.service.BluetoothLeService;
import com.axaet.ibeacon.utils.Conversion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @date 2016年5月31日
 * @author yuShu
 * @category Modify iBeacon parameters
 */
public class ModifyBeaconActivity extends Activity implements OnClickListener {

	private String address = "";
	private String deviceName = "";

	private BluetoothLeService mBluetoothLeService;

	private boolean isConnected = false;
	private EditText textUuid;
	private TextView textState;
	private EditText textMajor;
	private EditText textMinor;
	private EditText textPeriod;
	private TextView textTemp;
	private TextView textHumidity;
	private Spinner spinner;
	private EditText textName;
	private EditText textPassword;
	private EditText textNewPassword;
	private Button btnModifypass;
	private Button btnModifyall;
	private Button btnReadTH;

	private String uuid;
	private int major;
	private int minor;
	private int period;
	private int txPower;
	private String password;
	private String newPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modifybeacon);
		address = getIntent().getStringExtra("address");
		deviceName = getIntent().getStringExtra("deviceName");
		initView();
		textName.setText(deviceName);
		initSpinner();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	private void initSpinner() {
		List<String> list = new ArrayList<String>();
		list.add("-23");
		list.add("-6");
		list.add("0");
		list.add("4");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				switch (arg2) {
				case 0:
					txPower = -23;
					break;
				case 1:
					txPower = -6;
					break;
				case 2:
					txPower = 0;
					break;
				case 3:
					txPower = 4;
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

	}

	private void initView() {
		textUuid = (EditText) findViewById(R.id.text_uuid);
		textState = (TextView) findViewById(R.id.text_state);
		textMajor = (EditText) findViewById(R.id.text_Major);
		textMinor = (EditText) findViewById(R.id.text_Minor);
		textPeriod = (EditText) findViewById(R.id.text_Period);
		textTemp = (TextView) findViewById(R.id.text_temp);
		textHumidity = (TextView) findViewById(R.id.text_humidity);
		spinner = (Spinner) findViewById(R.id.spinner);
		textName = (EditText) findViewById(R.id.text_Name);
		textPassword = (EditText) findViewById(R.id.text_Password);
		textNewPassword = (EditText) findViewById(R.id.text_newpassword);
		btnModifypass = (Button) findViewById(R.id.btn_modifypass);
		btnModifyall = (Button) findViewById(R.id.btn_modifyall);
		btnReadTH = (Button) findViewById(R.id.btn_readth);
		btnModifyall.setOnClickListener(this);
		btnModifypass.setOnClickListener(this);
		btnReadTH.setOnClickListener(this);
	}

	BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				// Connect to a successful
				isConnected = true;
				invalidateOptionsMenu();
				textState.setText("Connected");
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				// Connection failed or disconnected
				isConnected = false;
				invalidateOptionsMenu();
				textState.setText("Disconnected");
			} else if (BluetoothLeService.ACTION_DATA_ONGETUUID.equals(action)) {
				// After the connection, by enabling the notification to get to
				// the UUID
				textUuid.setText(intent.getStringExtra(BluetoothLeService.UUID_DATA));
			} else if (BluetoothLeService.ACTION_DATA_ONGETOTHER.equals(action)) {
				// After the connection, by enabling the notification to get to
				// the majro,minor,period,txpower
				textMajor.setText(intent.getIntExtra(BluetoothLeService.MAJOR_DATA, 0) + "");
				textMinor.setText(intent.getIntExtra(BluetoothLeService.MINOR_DATA, 0) + "");
				textPeriod.setText(intent.getIntExtra(BluetoothLeService.PERIOD_DATA, 0) + "");
				txPower = intent.getIntExtra(BluetoothLeService.TXPOWER_DATA, 0);
				switch (txPower) {
				case 0:
					spinner.setSelection(0);
					txPower = -23;
					break;
				case 1:
					spinner.setSelection(1);
					txPower = -6;
					break;
				case 2:
					spinner.setSelection(2);
					txPower = 0;
					break;
				case 3:
					spinner.setSelection(3);
					txPower = 4;
					break;
				}
			}
			// If your product does not have the temperature and humidity, it
			// will not receive the broadcast.
			else if (BluetoothLeService.ACTION_DATA_ONGETTH.equals(action)) {
				textHumidity.setVisibility(View.VISIBLE);
				textTemp.setVisibility(View.VISIBLE);
				textTemp.setText("temperature:" + intent.getIntExtra(BluetoothLeService.TEMPERATURE_DATA, -1) + "℃");
				textHumidity.setText("humidity:" + intent.getIntExtra(BluetoothLeService.HUMIDITY_DATA, -1) + "%");
			} else if (BluetoothLeService.ACTION_PASSWORD_SUCCESS.equals(action)) {
				// Password correct, to modify the parameters
				handler.sendEmptyMessage(1);
			} else if (BluetoothLeService.ACTION_PASSWORD_ERROR.equals(action)) {
				// Password error
				Toast.makeText(ModifyBeaconActivity.this, "Password error", Toast.LENGTH_SHORT).show();
			} else if (BluetoothLeService.ACTION_DATA_ONSEND.equals(action)) {
				// Write data to the success of the callback, you can view which
				// data is written to success
				Log.i("yushu", Conversion.bytesToHexString(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA)));
			}
		}

	};

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			// After binding service, start the connection device
			mBluetoothLeService.connect(address);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	/**
	 * broadcast action
	 * 
	 * @return
	 */
	private IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_ONGETUUID);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_ONGETOTHER);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_ONGETTH);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_ONSEND);
		intentFilter.addAction(BluetoothLeService.ACTION_PASSWORD_SUCCESS);
		intentFilter.addAction(BluetoothLeService.ACTION_PASSWORD_ERROR);
		return intentFilter;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.modifymenu, menu);
		if (isConnected) {
			menu.findItem(R.id.action_connect).setVisible(false);
			menu.findItem(R.id.action_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.action_connect).setVisible(true);
			menu.findItem(R.id.action_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_connect:
			mBluetoothLeService.connect(address);
			return true;
		case R.id.action_disconnect:
			mBluetoothLeService.remove();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		unbindService(mServiceConnection);
		unregisterReceiver(mGattUpdateReceiver);
		super.onDestroy();
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				if (modifyPassOrUuid) {
					modifyUuidAndOther();
				} else {
					modifyPass();
				}

			}
		};
	};

	private void modifyUuidAndOther() {
		mBluetoothLeService.modifyUUID(uuid);
		try {
			// Each completion of an order, we need to delay for a period of
			// time, and then send the next command, otherwise the modification
			// is not successful;Delay time length is different, need to test
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mBluetoothLeService.modifyMajorAndOther(major, minor, period, txPower);
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// When the device name is unchanged, no changes are made.
		if (!deviceName.equals(textName.getText().toString())) {
			deviceName = textName.getText().toString();
			mBluetoothLeService.modifyDeviceName(deviceName);
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mBluetoothLeService.closeDevice();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finish();
	}

	private void modifyPass() {
		mBluetoothLeService.modifyPassword(password, newPassword);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mBluetoothLeService.closeDevice();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finish();
	}

	boolean modifyPassOrUuid = true;

	@Override
	public void onClick(View arg0) {
		if (arg0 == btnModifypass) {
			password = textPassword.getText().toString();
			newPassword = textNewPassword.getText().toString();
			if (TextUtils.isEmpty(password) || password.length() != 6) {
				Toast.makeText(this, "Password length must be 6 characters", Toast.LENGTH_SHORT).show();
				return;
			}
			if (TextUtils.isEmpty(newPassword) || newPassword.length() != 6) {
				Toast.makeText(this, "Password length must be 6 characters", Toast.LENGTH_SHORT).show();
				return;
			}
			// Before modifying the parameters, you must verify the password, in
			// order to modify the parameters after verification,
			mBluetoothLeService.verifyPassword(password);
			modifyPassOrUuid = false;
		} else if (arg0 == btnModifyall) {
			uuid = textUuid.getText().toString();
			try {
				major = Integer.parseInt(textMajor.getText().toString());
				minor = Integer.parseInt(textMinor.getText().toString());
				period = Integer.parseInt(textPeriod.getText().toString());
			} catch (Exception e) {
			}
			password = textPassword.getText().toString();
			if (TextUtils.isEmpty(password) || password.length() != 6) {
				Toast.makeText(this, "Password length must be 6 characters", Toast.LENGTH_SHORT).show();
				return;
			}
			if (TextUtils.isEmpty(uuid) || uuid.length() != 32) {
				Toast.makeText(this, "uuid length must be 32 characters", Toast.LENGTH_SHORT).show();
				return;
			}

			if (major > 65535 || major < 0) {
				Toast.makeText(this, "0<major<65535", Toast.LENGTH_SHORT).show();
				return;
			}
			if (minor > 65535 || minor < 0) {
				Toast.makeText(this, "0<minor<65535", Toast.LENGTH_SHORT).show();
				return;
			}
			if (period < 100 || period > 9800) {
				Toast.makeText(this, "100<minor<9800", Toast.LENGTH_SHORT).show();
				return;
			}
			// Before modifying the parameters, you must verify the password, in
			// order to modify the parameters after verification,
			mBluetoothLeService.verifyPassword(password);
			modifyPassOrUuid = true;
		} else if (arg0 == btnReadTH) {
			mBluetoothLeService.getTemperatureAndHumidity();
		}
	}

}
