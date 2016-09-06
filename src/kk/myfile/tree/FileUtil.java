package kk.myfile.tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Unknown;
import kk.myfile.util.Logger;

public class FileUtil {
	private static JSONObject sTypeMap;
	
	public static void init(Context context) {
		if (sTypeMap != null) {
			return;
		}
		
		AssetManager sm = context.getAssets();
		try {
			InputStream is = sm.open("file_type.json");
			String string = FileUtil.readString(is, 1024 * 1024);
			sTypeMap = new JSONObject(string);
		} catch (Exception e) {
			Logger.print(null, e);
		}
	}
	
	public static Leaf createLeaf(File file) {
		String path = file.getAbsolutePath();
		if (file.isDirectory()) {
			return new Direct(path);
		}
		
		String type = null;
		
		String name = file.getName();
		int pointIndex = name.lastIndexOf('.');
		if (pointIndex != -1) {
			String subfix = name.substring(pointIndex + 1, name.length())
				.toLowerCase(Locale.ENGLISH);
			
			try {
				JSONObject map = sTypeMap.getJSONObject(subfix);
				if (map != null) {
					type = map.getString("type");
					
					String cls = map.getString("cls");
					cls = String.format("%c%s", Character.toUpperCase(cls.charAt(0)),
						cls.substring(1));
					Class<?> clazz = Class.forName(String.format("kk.myfile.leaf.%s", cls));
					if (clazz != null) {
						Constructor<?> ct = clazz.getConstructor(String.class);
						Leaf leaf = (Leaf) ct.newInstance(path);
						leaf.setType(type);
						
						return leaf;
					}
				}
			} catch (Exception e) {
			}
		}
		
		Leaf leaf = new Unknown(path);
		leaf.setType(type);
		return leaf;
	}
	
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
