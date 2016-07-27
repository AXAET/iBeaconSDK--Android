package com.axaet.ibeacon.service;

import com.axaet.ibeacon.utils.Conversion;
import com.axaet.ibeacon.utils.UUIDUtils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * @date 2016年5月31日
 * @author yuShu
 * @category Service for managing connection and data communication with a GATT
 *           server hosted on a given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {

	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGatt mBluetoothGatt;
	private String mBluetoothDeviceAddress;

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.action_gatt_connected";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.action_gatt_disconnected";
	public final static String ACTION_DATA_ONSEND = "com.example.bluetooth.le.action_data_onsend";
	public final static String ACTION_DATA_ONGETUUID = "com.example.bluetooth.le.action_data_ongetuuid";
	public final static String ACTION_DATA_ONGETTH = "com.example.bluetooth.le.action_data_ongetth";
	public final static String ACTION_DATA_ONGETOTHER = "com.example.bluetooth.le.action_data_ongetother";

	public final static String ACTION_PASSWORD_SUCCESS = "com.example.bluetooth.le.action_password_success";
	public final static String ACTION_PASSWORD_ERROR = "com.example.bluetooth.le.action_password_error";

	public final static String UUID_DATA = "com.example.bluetooth.le.UUID_DATA";
	public final static String MAJOR_DATA = "com.example.bluetooth.le.MAJOR_DATA";
	public final static String MINOR_DATA = "com.example.bluetooth.le.MINOR_DATA";
	public final static String PERIOD_DATA = "com.example.bluetooth.le.PERIOD_DATA";
	public final static String TXPOWER_DATA = "com.example.bluetooth.le.TXPOWER_DATA";
	public final static String HUMIDITY_DATA = "com.example.bluetooth.le.humidity_data";
	public final static String TEMPERATURE_DATA = "com.example.bluetooth.le.temperature_data";

	public final static String EXTRA_DATA = "com.example.bluetooth.le.extra_data";

	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initialize();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		disconnect();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			// Connect successfully
			if (newState == BluetoothGatt.STATE_CONNECTED) {
				// Looking for service
				gatt.discoverServices();
			} // Connection failed or disconnected
			else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
				broadcastUpdate(ACTION_GATT_DISCONNECTED);
			}
			if (status != BluetoothGatt.GATT_SUCCESS) {
				disconnect();
				broadcastUpdate(ACTION_GATT_DISCONNECTED);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				enableNotication();
				broadcastUpdate(ACTION_GATT_CONNECTED);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(characteristic);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(characteristic);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicRead(gatt, characteristic, status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(characteristic);
			}
		}
		
		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			super.onReadRemoteRssi(gatt, rssi, status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// Can get rssi here
			}
		}

	};

	private void broadcastUpdate(String action) {
		Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	@SuppressLint("DefaultLocale")
	private void broadcastUpdate(BluetoothGattCharacteristic characteristic) {
		Intent intent = new Intent();
		byte[] bs = characteristic.getValue();
		if (UUIDUtils.UUID_ENABLE.equals(characteristic.getUuid())) {
			if (bs[0] == 5) {
				intent.setAction(ACTION_PASSWORD_SUCCESS);
			} else if (bs[0] == 10) {
				intent.setAction(ACTION_PASSWORD_ERROR);
			} else if (bs[0] == 17) {
				String uuidTemp = Conversion.bytesToHexString(bs);
				uuidTemp = uuidTemp.substring(2, 34).toUpperCase();
				intent.setAction(ACTION_DATA_ONGETUUID);
				intent.putExtra(UUID_DATA, uuidTemp);
			} else if (bs[0] == 18) {
				String string = Conversion.bytesToHexString(bs);
				int major = Integer.parseInt(string.substring(2, 6), 16);
				int minor = Integer.parseInt(string.substring(6, 10), 16);
				int txPower = Integer.parseInt(string.substring(10, 12), 16);
				int period = Integer.parseInt(string.substring(12, 16), 16);
				intent.setAction(ACTION_DATA_ONGETOTHER);
				intent.putExtra(MAJOR_DATA, major);
				intent.putExtra(MINOR_DATA, minor);
				intent.putExtra(PERIOD_DATA, period);
				intent.putExtra(TXPOWER_DATA, txPower);
			}
		} else if (UUIDUtils.UUID_ENABLETH.equals(characteristic.getUuid())) {
			String string = Conversion.bytesToHexString(bs);
			int temperature = Integer.parseInt(string.substring(0, 2), 16);
			int humidity = Integer.parseInt(string.substring(4, 6), 16);
			intent.setAction(ACTION_DATA_ONGETTH);
			intent.putExtra(TEMPERATURE_DATA, temperature);
			intent.putExtra(HUMIDITY_DATA, humidity);
		} else if (UUIDUtils.UUID_WRITE.equals(characteristic.getUuid())) {
			intent.setAction(ACTION_DATA_ONSEND);
			intent.putExtra(EXTRA_DATA, characteristic.getValue());
		} else if (UUIDUtils.UUID_READTH.equals(characteristic.getUuid())) {
			String string = Conversion.bytesToHexString(bs);
			int temperature = Integer.parseInt(string.substring(0, 2), 16);
			int humidity = Integer.parseInt(string.substring(4, 6), 16);
			intent.setAction(ACTION_DATA_ONGETTH);
			intent.putExtra(TEMPERATURE_DATA, temperature);
			intent.putExtra(HUMIDITY_DATA, humidity);
		}
		sendBroadcast(intent);
	}

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	private boolean initialize() {
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				return false;
			}
		}
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			return false;
		}
		return true;
	}

	/**
	 * Connecting device
	 * 
	 * @param address
	 * @return
	 */
	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			return false;
		}
		// Previously connected device. Try to reconnect.
		if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
			if (mBluetoothGatt.connect()) {
				return true;
			} else {
				return false;
			}
		}
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		if (device != null) {
			this.mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		}
		mBluetoothDeviceAddress = address;
		return true;
	}

	/**
	 * Disconnect device
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.disconnect();
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	public void remove() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.disconnect();
	}

	/**
	 * If you buy the product with temperature and humidity, you can use this
	 * method to get the temperature and humidity
	 */
	public void getTemperatureAndHumidity() {
		BluetoothGattService bluetoothService = mBluetoothGatt.getService(UUIDUtils.UUID_SERVICE);
		if (bluetoothService == null) {
			return;
		}
		BluetoothGattCharacteristic enableCharacteristic = bluetoothService.getCharacteristic(UUIDUtils.UUID_READTH);
		if (enableCharacteristic == null) {
			return;
		}
		readCharacteristic(enableCharacteristic);
	}

	/**
	 * read Characteristic
	 * 
	 * @param characteristic
	 */
	private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	/**
	 * Notication
	 */
	private void enableNotication() {
		if(mBluetoothGatt==null){
			return;
		}
		BluetoothGattService bluetoothService = mBluetoothGatt.getService(UUIDUtils.UUID_SERVICE);
		if (bluetoothService == null) {
			return;
		}
		BluetoothGattCharacteristic enableCharacteristic = bluetoothService.getCharacteristic(UUIDUtils.UUID_ENABLE);
		if (enableCharacteristic == null) {
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(enableCharacteristic, true);
		BluetoothGattDescriptor descriptor = enableCharacteristic.getDescriptor(UUIDUtils.CLIENT_CHARACTERISTIC_CONFIG);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor);

		// Compatible with other products,If your product does not have
		// temperature and humidity, you can delete the following code
		BluetoothGattCharacteristic enableTHCharacteristic = bluetoothService
				.getCharacteristic(UUIDUtils.UUID_ENABLETH);
		if (enableTHCharacteristic == null) {
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(enableTHCharacteristic, true);
		BluetoothGattDescriptor descriptor2 = enableTHCharacteristic
				.getDescriptor(UUIDUtils.CLIENT_CHARACTERISTIC_CONFIG);
		descriptor2.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor2);
	}

	/**
	 * Send data to Bluetooth device
	 * 
	 * @param bs
	 */
	public void sendDataToDevice(byte[] bs) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			return;
		}
		BluetoothGattService bluetoothService = mBluetoothGatt.getService(UUIDUtils.UUID_SERVICE);
		if (bluetoothService == null) {
			return;
		}
		BluetoothGattCharacteristic writeCharacteristic = bluetoothService.getCharacteristic(UUIDUtils.UUID_WRITE);
		if (writeCharacteristic == null) {
			return;
		}
		writeCharacteristic.setValue(bs);
		writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		mBluetoothGatt.writeCharacteristic(writeCharacteristic);
	}

	/**
	 * Before modifying the parameters, you must verify the password, in order
	 * to modify the parameters after verification,
	 * 
	 * @param password
	 *            English letters and numbers,6 character length
	 */
	public void verifyPassword(String password) {
		if (password.length() != 6) {
			password = "123456";
		}
		byte[] bs = Conversion.str2Byte(password, (byte) 0x04);
		sendDataToDevice(bs);
	}

	/**
	 * modify the UUID
	 * 
	 * @param uuid
	 *            16 binary string,32 character length
	 */
	public void modifyUUID(String uuid) {
		if (uuid.length() != 32) {
			uuid = "FDA50693A4E24FB1AFCFC6EB07647825";
		}
		byte[] bs = Conversion.hexStringToByte(uuid);
		byte[] uuidByte = new byte[bs.length + 1];
		uuidByte[0] = (byte) 0x01;
		for (int i = 0; i < bs.length; i++) {
			uuidByte[i + 1] = bs[i];
		}
		sendDataToDevice(uuidByte);
	}

	/**
	 * modify the major,minor,Broadcast cycle,txPower
	 * 
	 * @param major
	 *            Greater than or equal to 0, less than or equal to 65535,
	 *            0<=majro<65535
	 * @param minor
	 *            Greater than or equal to 0, less than or equal to 65535,
	 *            0<=minor<65535
	 * @param period
	 *            Greater than or equal to 100, less than or equal to 9800,
	 *            100<=period<=9800
	 * @param txPower
	 *            The value of txPower in the 4,0, -6, -23 in the choice of one
	 */
	public void modifyMajorAndOther(int major, int minor, int period, int txPower) {
		if (major > 65535 || major < 0) {
			major = 1004;
		}
		if (minor > 65535 || minor < 0) {
			minor = 54480;
		}
		if (period < 100 || period > 9800) {
			period = 1000;
		}
		if (txPower == 0) {
			txPower = 2;
		} else if (txPower == -6) {
			txPower = 1;
		} else if (txPower == -23) {
			txPower = 0;
		} else if (txPower == 4) {
			txPower = 3;
		} else {
			txPower = 0;
		}
		byte[] data = new byte[8];
		data[0] = (byte) 0x02;
		data[1] = (byte) (major / 256);
		data[2] = (byte) (major % 256);
		data[3] = (byte) (minor / 256);
		data[4] = (byte) (minor % 256);
		data[5] = (byte) txPower;
		data[6] = (byte) (period / 256);
		data[7] = (byte) (period % 256);
		sendDataToDevice(data);
	}


/**
	 * read rssi
	 * 
	 * @return
	 */
	public boolean readRssi() {
		if (mBluetoothGatt != null) {
			return mBluetoothGatt.readRemoteRssi();
		}
		return false;
	}

	/**
	 * Modify the device name
	 * 
	 * @param deviceName
	 *            Visible ASCII character,length less than 16 characters
	 */
	public void modifyDeviceName(String deviceName) {
		if (deviceName.length() > 15) {
			deviceName = "pBeacon";
		}
		byte[] bs = Conversion.str2ByteDeviceName(deviceName);
		sendDataToDevice(bs);
	}

	/**
	 * modify the password
	 * 
	 * @param oldPassword
	 *            English letters and numbers,6 character length
	 * @param newPassword
	 *            English letters and numbers,6 character length
	 */
	public void modifyPassword(String oldPassword, String newPassword) {
		byte[] bs = Conversion.str2Byte(oldPassword + newPassword, (byte) 0x0c);
		sendDataToDevice(bs);
	}

	/**
	 * After modifying the parameters,must turn off the device, otherwise the
	 * data is not successful.
	 */
	public void closeDevice() {
		byte[] bs = new byte[1];
		bs[0] = (byte) 0x03;
		sendDataToDevice(bs);
	}

}
