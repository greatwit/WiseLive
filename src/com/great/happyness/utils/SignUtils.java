package com.great.happyness.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class SignUtils {
	/**
	 * @param params
	 * @param secret
	 * @return
	 * @throws IOException
	 */
	
	public static String getSignature(HashMap<String, String> params,
			String secret) {
		try {
			// 先将参数以其参数名的字典序升序进行排序
			Map<String, String> sortedParams = new TreeMap<String, String>(
					params);
			Set<Entry<String, String>> entrys = sortedParams.entrySet();

			// 遍历排序后的字典，将所有参数按"key=value"格式拼接在一起
			StringBuilder basestring = new StringBuilder();
			for (Entry<String, String> param : entrys) {
				basestring.append(param.getKey()).append("=")
						.append(param.getValue());
			}
			basestring.append(secret);
			// 使用MD5对待签名串求签
			byte[] bytes = null;
			try {
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				bytes = md5.digest(basestring.toString().getBytes("UTF-8"));
			} catch (GeneralSecurityException ex) {
				throw new IOException(ex);
			}

			// 将MD5输出的二进制结果转换为小写的十六进制
			StringBuilder sign = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				String hex = Integer.toHexString(bytes[i] & 0xFF);
				if (hex.length() == 1) {
					sign.append("0");
				}
				sign.append(hex);
			}
			return sign.toString();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] int2Bytes(int integer) {
		int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer
				: integer)) / 8;
		byte[] byteArray = new byte[4];

		for (int n = 0; n < byteNum; n++)
			byteArray[3 - n] = (byte) (integer >>> (n * 8));

		return (byteArray);
	}

	public static void putBytes(byte[] b, byte[] src, int index) {
		System.arraycopy(src, 0, b, index, src.length);
	}

	public static void putbyte(byte[] b, int value, int index) {
		b[index] = (byte) (value);
	}

	public static void putShort(byte[] b, int value, int index) {
		b[index] = (byte) (value >> 8);
		b[index + 1] = (byte) (value >> 0);
	}

	public static void putInt(byte[] bb, int x, int index) {
		bb[index + 3] = (byte) (x >> 0);
		bb[index + 2] = (byte) (x >> 8);
		bb[index + 1] = (byte) (x >> 16);
		bb[index + 0] = (byte) (x >> 24);
	}

	public static void putLong(byte[] bb, long nCrcValue, int index) {

		for (int ix = 4; ix < 8; ++ix) {
			int offset = 64 - (ix + 1) * 8;
			bb[index + ix - 4] = (byte) ((nCrcValue >> offset) & 0xff);
		}
	}

	public static void putIp(byte[] bb, String ip, int index) {
		if (ip == null || ip.equals("")) {
			return;
		}
		String[] arrIp = ip.split("\\.");

		for (int i = 0; i < arrIp.length; i++) {
			int nNum = Integer.parseInt(arrIp[i]);
			// byte nNum = Byte.parseByte(arrIp[i]);
			bb[index + i] = (byte) nNum;
		}
	}

	 public static void putNum(byte[] bb, String hexString, int index) {
		if (hexString == null || hexString.equals("")) {
			return;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();

		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			bb[index + i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
	}

	public static void putMD5(byte[] bb, String hexString, int index) {
		if (hexString == null || hexString.equals("")) {
			return;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length();
		char[] hexChars = hexString.toCharArray();

		for (int i = 0; i < length; i++) {
			bb[index + i] = charToByte(hexChars[i]);
		}
	}

	public static int bytes2Int(byte[] byteNum) {
		int num = 0;
		for (int ix = 0; ix < 4; ++ix) {
			num <<= 8;
			num |= (byteNum[ix] & 0xff);
		}
		return num;
	}

	public static int bytes2Short(byte[] b, int offset) {
		int value = (int) ((b[offset] & 0xFF) << 8 | (b[offset + 1] & 0xFF));
		return value;
	}

	public static int bytes2Int(byte[] b, int offset) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}
		return value;
	}

	public static String byte2MD5(byte[] b, int offset) {
		String strIp = "";
		for (int i = 0; i < 32; i++) {
			byte nTemp = b[i + offset];
			strIp += byteToChar(nTemp);
		}

		return strIp;
	}

	public static String byte2Ip(byte[] b, int offset) {
		String strIp = "";
		for (int i = 0; i < 4; i++) {
			byte nTemp = b[i + offset];
			int n = nTemp + 256;
			strIp += (n % 256) + ".";
		}

		return strIp.substring(0, strIp.length() - 1);
	}

	/**
	 * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
	 */
	public static int bytesToInt2(byte[] src, int offset) {
		int value;
		value = (int) (((src[offset] & 0xFF) << 24)
				| ((src[offset + 1] & 0xFF) << 16)
				| ((src[offset + 2] & 0xFF) << 8) | (src[offset + 3] & 0xFF));
		return value;
	}

	public static byte int2OneByte(int num) {
		return (byte) (num & 0x000000ff);
	}

	public static int oneByte2Int(byte byteNum) {
		// 针对正数的int
		return byteNum > 0 ? byteNum : (128 + (128 + byteNum));
	}

	public static byte[] long2Bytes(long num) {
		byte[] byteNum = new byte[8];
		for (int ix = 0; ix < 8; ++ix) {
			int offset = 64 - (ix + 1) * 8;
			byteNum[ix] = (byte) ((num >> offset) & 0xff);
		}
		return byteNum;
	}

	public static long bytes2Long(byte[] byteNum) {
		long num = 0;
		for (int ix = 0; ix < 8; ++ix) {
			num <<= 8;
			num |= (byteNum[ix] & 0xff);
		}
		return num;
	}

	public static String bytes2RoomNum(byte[] src, int offset) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = offset; i < 2; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static String bytes2Num(byte[] src, int offset) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = offset; i < offset + 8; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 * 
	 * @param src
	 *            byte[] data
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase(Locale.US);
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	private static char byteToChar(int n) {
		return "0123456789ABCDEF".charAt(n);
	}

	public static String byte2String(byte buffer[], int offset, int len) // 不包含bEnd
	{
		byte[] bytePost = new byte[len];
		System.arraycopy(buffer, offset, bytePost, 0, len);
		try {
			String str = new String(bytePost, "UTF-8");
			return str;
		} catch (Exception ex) {
			return "";
		}
	}

	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2, int offset,
			int len) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, offset, byte_3, byte_1.length, len);
		return byte_3;
	}

	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}
	

    public static String bytesToHexString(byte[] src,int len){  
        StringBuilder stringBuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < len; i++) {  
            int v = src[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {  
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString();  
    }  
}

