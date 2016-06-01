package com.axaet.ibeacon.utils;

/**
 * @date 2016年5月30日
 * @author yuShu
 * @category Used to convert data
 * 
 */
public class Conversion {

	/**
	 * Byte data conversion string
	 * 
	 * @param data
	 * @return
	 */
	public static String bytesToHexString(byte[] data) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (data == null || data.length <= 0) {
			return null;
		}
		for (int i = 0; i < data.length; i++) {
			int v = data[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * Converts a 16 string string into an array of bytes
	 */
	public static byte[] hexStringToByte(String hex) {
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	/**
	 * Calculated distance
	 * 
	 * @param txPower
	 * @param rssi
	 * @return
	 */
	public static double calculateAccuracy(int txPower, double rssi) {
		if (rssi == 0) {
			return -1.0;
		}
		double ratio = rssi * 1.0 / txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio, 10);
		} else {
			double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
			return accuracy;
		}
	}

	public static byte[] str2Byte(String string, byte b) {
		char[] cs = string.toCharArray();
		byte[] bs = new byte[cs.length + 1];
		bs[0] = b;
		for (int i = 1; i <= cs.length; i++) {
			bs[i] = (byte) cs[i - 1];
		}
		return bs;
	}

	/**
	 * Device name to ASCII code
	 * 
	 * @param deviceName
	 *            Visible ASCII character
	 * @return
	 */
	public static byte[] str2ByteDeviceName(String deviceName) {
		char[] cs = deviceName.toCharArray();
		byte[] bs = new byte[cs.length + 2];
		bs[0] = (byte) 0x07;
		bs[1] = (byte) cs.length;
		for (int i = 2; i <= cs.length + 1; i++) {
			bs[i] = (byte) cs[i - 2];
		}
		return bs;
	}

}
