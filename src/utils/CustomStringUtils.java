package utils;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

public final class CustomStringUtils {

	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public static String codeConvert(String origin, String origin_code,
			String result_code) throws UnsupportedEncodingException {
		// byte[] b = origin.getBytes(origin_code);
		// ByteBuffer bb = ByteBuffer.wrap(b);
		//
		// bb = Charset.forName(result_code).encode(
		// Charset.forName(origin_code).decode(bb));// "windows-1252"
		// String result = String.valueOf(bb.asCharBuffer());
		String result = new String(origin.getBytes(result_code), result_code);
		return result;
	}

	/**
	 * 判断一个字符串是否验证码（4位英文or数字）
	 * 
	 * @param string
	 *            字符串
	 * @return
	 */
	public static boolean isCaptcha(String string) {
		Pattern pattern = Pattern.compile("[0-9a-zA-Z]{4}");
		return pattern.matcher(string).matches();
	}

	/**
	 * 判断一个字符是否英文或数字
	 * 
	 * @param tempChar
	 * @return
	 */
	public static boolean isDigitorLetter(char tempChar) {
		// TODO Auto-generated method stub
		return (('0' <= tempChar && tempChar <= '9')
				|| ('a' <= tempChar && tempChar <= 'z') || ('A' <= tempChar && tempChar <= 'Z'));
	}

	/**
	 * 获取文件路径的目录地址
	 * 
	 * @param path
	 * @return
	 */
	public static String getDir(String path) {
		// TODO Auto-generated method stub
		String dir = path.substring(0, getLastSeparator(path));
		// System.out.println(dir);
		return dir;
	}

	/**
	 * 获取路径中的文件名
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileName(String path) {
		// TODO Auto-generated method stub
		String fileName = path.substring(getLastSeparator(path) + 1);
		// System.out.println(fileName);
		return fileName;
	}

	public static Integer getLastSeparator(String path) {
		if (path.contains("/")) {
			return path.lastIndexOf('/');
		} else {
			return path.lastIndexOf('\\');
		}
	}

	/**
	 * 一般用于分隔从文件中读取的键值对
	 * 
	 * @param key_value
	 *            要分割的键值对
	 * @param separator
	 *            键-值的分隔符（一般如'：' '='）
	 * @return key_value[] 以字符串数据存储的分割后的键值对，key_value[0]为键，key_value[1]为值
	 * @author rongyang_lu
	 * @date 2015年8月17日 上午9:04:28
	 */
	public static String[] splitKeyValue(String key_value, String separator) {
		return key_value.split(separator, 2);
	}
}
