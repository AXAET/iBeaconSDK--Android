package com.axaet.ibeacon.beans;

import java.text.DecimalFormat;

import com.axaet.ibeacon.utils.Conversion;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

/**
 * @date 2016年5月30日
 * @author yuShu
 * @category Package Bluetooth data
 */
public class iBeaconClass {
	iBeacon beacon = null;

	private iBeaconClass() {
		beacon = new iBeacon();
	}

	/**
	 * Singleton Pattern
	 * 
	 * @return
	 */
	private static class LazyHolder {
		private static final iBeaconClass INSTANCE = new iBeaconClass();
	}

	/**
	 * Processing Bluetooth data objects
	 * 
	 * @return
	 */
	public static final iBeaconClass getInstance() {
		return LazyHolder.INSTANCE;
	}

	static public class iBeacon implements  Cloneable {
		/**
		 * Bluetooth device
		 */
		public BluetoothDevice bluetoothDevice;
		/**
		 * Bluetooth device name
		 */
		public String deviceName;
		/**
		 * major
		 */
		public int major;
		/**
		 * minor
		 */
		public int minor;
		/**
		 * proximityUuid
		 */
		public String proximityUuid;
		/**
		 * Bluetooth device address
		 */
		public String deviceAddress;
		/**
		 * txPower
		 */
		public int txPower;
		/**
		 * rssi
		 */
		public int rssi;
		/**
		 * distance
		 */
		public String distance;

		@Override
		public iBeacon clone() throws CloneNotSupportedException {
			iBeacon beacon = null;
			try {
				beacon = (iBeacon) super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return beacon;
		}
	}

	private static DecimalFormat df = new DecimalFormat("#0.00");

	/**
	 * The encapsulated data into iBeacon
	 * 
	 * @param device
	 * @param rssi
	 * @param scanRecord
	 * @return iBeacon
	 */
	@SuppressLint("DefaultLocale")
	public iBeacon formToiBeacon(BluetoothDevice device, int rssi, byte[] scanRecord) {
		iBeacon iBeacon = null;
		int startByte = 2;
		boolean patternFound = false;
		while (startByte <= 5) {
			if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && ((int) scanRecord[startByte + 3] & 0xff) == 0x15) {
				// yes! This is an iBeacon
				patternFound = true;
				break;
			} else if (((int) scanRecord[startByte] & 0xff) == 0x2d && ((int) scanRecord[startByte + 1] & 0xff) == 0x24
					&& ((int) scanRecord[startByte + 2] & 0xff) == 0xbf
					&& ((int) scanRecord[startByte + 3] & 0xff) == 0x16) {
				try {
					iBeacon = beacon.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					return null;
				}
				iBeacon.major = 0;
				iBeacon.minor = 0;
				iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
				iBeacon.txPower = -55;
				iBeacon.bluetoothDevice = device;
				return iBeacon;
			} else if (((int) scanRecord[startByte] & 0xff) == 0xad && ((int) scanRecord[startByte + 1] & 0xff) == 0x77
					&& ((int) scanRecord[startByte + 2] & 0xff) == 0x00
					&& ((int) scanRecord[startByte + 3] & 0xff) == 0xc6) {
				try {
					iBeacon = beacon.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					return null;
				}
				iBeacon.major = 0;
				iBeacon.minor = 0;
				iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
				iBeacon.txPower = -55;
				iBeacon.bluetoothDevice = device;
				return iBeacon;
			}
			startByte++;
		}
		if (patternFound == false) {
			return null;
		}
		try {
			iBeacon = beacon.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		iBeacon.major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);
		iBeacon.minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);
		iBeacon.txPower = (int) scanRecord[startByte + 24];
		iBeacon.rssi = rssi;
		iBeacon.deviceAddress = device.getAddress();
		if (device.getName() != null) {
			iBeacon.deviceName = device.getName();
		} else {
			iBeacon.deviceName = "unknown device";
		}

		iBeacon.bluetoothDevice = device;
		byte[] proximityUuidBytes = new byte[16];
		System.arraycopy(scanRecord, startByte + 4, proximityUuidBytes, 0, 16);
		String hexString = Conversion.bytesToHexString(proximityUuidBytes);
		StringBuilder sb = new StringBuilder();
		sb.append(hexString.substring(0, 8));
		sb.append("-");
		sb.append(hexString.substring(8, 12));
		sb.append("-");
		sb.append(hexString.substring(12, 16));
		sb.append("-");
		sb.append(hexString.substring(16, 20));
		sb.append("-");
		sb.append(hexString.substring(20, 32));
		iBeacon.proximityUuid = sb.toString().toUpperCase();
		iBeacon.distance = df.format(Conversion.calculateAccuracy((int) scanRecord[startByte + 24], rssi));
		return iBeacon;
	}

}
