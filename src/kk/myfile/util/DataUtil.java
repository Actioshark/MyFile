package kk.myfile.util;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.leaf.Leaf;

public class DataUtil {
	public static byte[] md5(byte[] src) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return md.digest(src);
		} catch (Exception e) {
			Logger.print(null, e);
		}

		return null;
	}

	public static byte[] md5(InputStream is) {
		try {
			byte[] buf = new byte[1024 * 1024];
			int len;

			MessageDigest md = MessageDigest.getInstance("MD5");

			while ((len = is.read(buf)) != -1) {
				md.update(buf, 0, len);
			}

			return md.digest();
		} catch (Exception e) {
			Logger.print(null, e);
		}

		return null;
	}

	private static final char[] sHex2Char = new char[] {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
	};

	public static String toHexStr(byte[] bs) {
		StringBuilder sb = new StringBuilder();

		for (byte b : bs) {
			int ch = (b & 0xf0) >> 4;
			sb.append(sHex2Char[ch]);

			ch = b & 0x0f;
			sb.append(sHex2Char[ch]);
		}

		return sb.toString();
	}

	public static ArrayList<CharSequence> leaf2PathCs(List<Leaf> leafs) {
		ArrayList<CharSequence> paths = new ArrayList<CharSequence>();

		for (Leaf leaf : leafs) {
			paths.add(leaf.getPath());
		}

		return paths;
	}

	public static List<String> leaf2PathString(List<Leaf> leafs) {
		List<String> paths = new ArrayList<String>();

		for (Leaf leaf : leafs) {
			paths.add(leaf.getPath());
		}

		return paths;
	}
	
	public static String getFileName(String path) {
		for (int i = path.length() - 2; i >= 0; i--) {
			if (path.charAt(i) == '/') {
				StringBuilder sb = new StringBuilder();
				
				int len = path.length();
				for (int j = i + 1; j < len; j++) {
					char ch = path.charAt(j);
					if (ch != '/') {
						sb.append(ch);
					}
				}
				
				return sb.toString();
			}
		}
		
		return path;
	}

	public static String getSubfix(String path) {
		int len = path.length();

		for (int i = len - 1; i >= 0; i--) {
			char ch = path.charAt(i);

			if (ch == '/') {
				return null;
			}

			if (ch == '.') {
				return path.substring(i + 1, len).toLowerCase(Setting.LOCALE);
			}
		}

		return null;
	}
}
