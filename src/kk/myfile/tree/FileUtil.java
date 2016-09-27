package kk.myfile.tree;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;
import kk.myfile.R;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Unknown;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.Setting;

public class FileUtil {
	private static JSONObject sTypeMap;
	
	private static final char[] ILLEGAL_FILE_NAME_CHAR = {
		'/', '\0',
	};
	
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
				.toLowerCase(Setting.LOCALE);
			
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
	
	public static String checkNewName(File file, String name) {
		if (name == null || name.length() < 1 || name.length() > 255) {
			return AppUtil.getString(R.string.err_illegal_file_name_length);
		}
		
		for (char ch : ILLEGAL_FILE_NAME_CHAR) {
			if (name.contains(String.valueOf(ch))) {
				return AppUtil.getString(R.string.err_illegal_file_name_char);
			}
		}
		
		if (file.exists()) {
			return AppUtil.getString(R.string.err_file_exist);
		}
		
		return null;
	}
	
	public static String createDirect(File file) {
		try {
			if (file.mkdirs()) {
				return null;
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		return AppUtil.getString(R.string.err_create_direct_failed);
	}
	
	public static String createFile(File file) {
		try {
			if (file.createNewFile()) {
				return null;
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		return AppUtil.getString(R.string.err_create_file_failed);
	}
	
	public static String delete(File file) {
		try {
			if (deleteRec(file)) {
				return null;
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		String name;
		try {
			name = file.getName();
		} catch (Exception e) {
			name = "";
		}
		
		return AppUtil.getString(R.string.err_create_file_failed, name);
	}
	
	private static boolean deleteRec(File file) {
		try {
			boolean suc = true;
			
			if (file.isDirectory()) {
				for (File temp : file.listFiles()) {
					suc = deleteRec(temp) && suc;
				}
			}
			
			return file.delete() && suc;
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		return false;
	}
}
