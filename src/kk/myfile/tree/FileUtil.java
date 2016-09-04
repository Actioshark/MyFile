package kk.myfile.tree;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
	public static String readString(InputStream is, int length) throws Exception {
		byte[] buf = new byte[length];
		int len, off = 0;
		
		while ((len = is.read(buf, off, buf.length - off)) != -1) {
			off += len;
		}
		
		return new String(buf, 0, off);
	}
	
	public static void write(String from, String to) throws Exception {
		InputStream is = new FileInputStream(from);
		OutputStream os = new FileOutputStream(to);
		byte[] buf = new byte[1024 * 1024];
		int len;
		
		while ((len = is.read(buf)) != -1) {
			os.write(buf, 0, len);
		}
		
		is.close();
		os.flush();
		os.close();
	}
}
