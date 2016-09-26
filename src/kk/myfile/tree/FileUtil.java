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
	
	public static String checkNewName(File parent, String name) {
		// TODO
		return null;
	}
	
	public static String createDirect(String path) {
		try {
			if (new File(path).mkdirs()) {
				return null;
			}
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		return AppUtil.getString(R.string.err_create_direct_failed);
	}
	
	public static boolean createFile(String path) {
		return false;
	}
}
