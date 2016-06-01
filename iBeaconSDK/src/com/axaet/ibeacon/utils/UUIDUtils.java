package com.axaet.ibeacon.utils;

import java.util.UUID;

/**
 * @date 2016年5月30日
 * @author yuShu
 * @category UUIDUtils
 * 
 */
public class UUIDUtils {

	public final static UUID HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
	public final static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	/**
	 * UUID of BluetoothGattService
	 */
	public final static UUID UUID_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
	/**
	 * UUID of BluetoothGattCharacteristic,used to write data
	 */
	public final static UUID UUID_WRITE = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");

	/**
	 * UUID of BluetoothGattCharacteristic,used to read data of temperature and
	 * humidity
	 */
	public final static UUID UUID_READTH = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
	/**
	 * Characteristic value of uuid , major,minor such parameters ,Used to
	 * enable notification
	 */
	public final static UUID UUID_ENABLE = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
	/**
	 * Characteristic value of temperature and humidity ,Used to enable
	 * notification
	 */
	public final static UUID UUID_ENABLETH = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
}