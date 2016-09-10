package kk.myfile.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

public class Setting {
	public static final String DEFAULT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	public static final Locale LOCALE = Locale.ENGLISH;
	
	static final String KEY_DEF_PATH = "def_path";
	static final String KEY_SORT_FACTOR = "sort_factor";

	private static SharedPreferences sPrefer;

	public static void init(Context context) {
		if (sPrefer != null) {
			return;
		}
		
		sPrefer = context.getSharedPreferences("setting",
				Context.MODE_PRIVATE);
	}
	
	public static List<String> getDefPath() {
		String value = sPrefer.getString(KEY_DEF_PATH, null);
		List<String> result = new ArrayList<String>();
		
		try {
			JSONArray ja = new JSONArray(value);
			int len = ja.length();
			
			for (int i = 0; i < len; i++) {
				result.add(ja.getString(i));
			}
		} catch (Exception e) {
		}
		
		if (result.size() < 1) {
			result.add(DEFAULT_PATH);
			setDefPath(result);
		}
		
		return result;
	}
	
	public static void setDefPath(List<String> paths) {
		JSONArray ja = new JSONArray();
		for (int i = 0; i < paths.size(); i++) {
			try {
				ja.put(i, paths.get(i));
			} catch (Exception e){
			}
		}
		
		Editor editor = sPrefer.edit();
		editor.putString(KEY_DEF_PATH, ja.toString());
		editor.commit();
	}
	
	public static String getSortFactor() {
		return sPrefer.getString(KEY_SORT_FACTOR, null);
	}
	
	public static void setSortFactor(String str) {
		Editor editor = sPrefer.edit();
		editor.putString(KEY_SORT_FACTOR, str);
		editor.commit();
	}
}
