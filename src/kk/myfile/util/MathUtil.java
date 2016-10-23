package kk.myfile.util;

public class MathUtil {
	public static String insertComma(long num) {
		return insertComma(num, 3);
	}
	
	public static String insertComma(long num, int interval) {
		return insertComma(String.valueOf(num), interval);
	}
	
	public static String insertComma(String str) {
		return insertComma(str, 3);
	}
	
	public static String insertComma(String str, int interval) {
		StringBuilder sb = new StringBuilder();
		
		int len = str.length();
		for (int i = 0; i < len; i++) {
			sb.append(str.charAt(i));

			if (i + 1 != len && (len - i) % interval == 1) {
				sb.append(',');
			}
		}
		
		return sb.toString();
	}
}
