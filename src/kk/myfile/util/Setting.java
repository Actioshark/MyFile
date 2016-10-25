package kk.myfile.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kk.myfile.R;
import kk.myfile.activity.BaseActivity.Classify;
import kk.myfile.activity.MainActivity.DefPath;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

public class Setting {
	public static final String DEFAULT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	public static final Locale LOCALE = Locale.ENGLISH;
	
	static final String KEY_DEF_PATH = "def_path";
	static final String KEY_SORT_FACTOR = "sort_factor_%s";
	static final String KEY_LIST_STYLE = "list_style_%s";
	static final String KEY_SHOW_HIDDEN = "show_hidden";
	static final String KEY_NUM_LIMIT = "num_limit_%s";
	
	public static final int NUM_LIMIT_MIN = 1;
	public static final int NUM_LIMIT_MAX = 9999;
	public static final int NUM_LIMIT_DEF = 100;

	private static SharedPreferences sPrefer;

	public static void init(Context context) {
		if (sPrefer != null) {
			return;
		}
		
		sPrefer = context.getSharedPreferences("setting",
				Context.MODE_PRIVATE);
	}
	
	public static List<DefPath> getDefPath() {
		String value = sPrefer.getString(KEY_DEF_PATH, null);
		List<DefPath> result = new ArrayList<DefPath>();
		
		try {
			JSONArray ja = new JSONArray(value);
			int len = ja.length();
			
			for (int i = 0; i < len; i++) {
				JSONObject jo = ja.getJSONObject(i);
				
				DefPath dp = new DefPath();
				dp.name = jo.getString("name");
				dp.path = jo.getString("path");
				
				result.add(dp);
			}
		} catch (Exception e) {
		}
		
		if (result.size() < 1) {
			DefPath dp = new DefPath();
			dp.name = AppUtil.getString(R.string.def_path_name);
			dp.path = DEFAULT_PATH;
			
			result.add(dp);
			setDefPath(result);
		}
		
		return result;
	}
	
	public static void setDefPath(List<DefPath> paths) {
		JSONArray ja = new JSONArray();
		for (int i = 0; i < paths.size(); i++) {
			try {
				DefPath dp = paths.get(i);
				JSONObject jo = new JSONObject();
				jo.put("name", dp.name);
				jo.put("path", dp.path);
				
				ja.put(i, jo);
			} catch (Exception e){
			}
		}
		
		Editor editor = sPrefer.edit();
		editor.putString(KEY_DEF_PATH, ja.toString());
		editor.commit();
	}
	
	public static String getSortFactor(Classify classify) {
		return sPrefer.getString(String.format(KEY_SORT_FACTOR, classify.name()), null);
	}
	
	public static void setSortFactor(Classify classify, String str) {
		Editor editor = sPrefer.edit();
		editor.putString(String.format(KEY_SORT_FACTOR, classify.name()), str);
		editor.commit();
	}
	
	public static String getListStyle(Classify classify) {
		return sPrefer.getString(String.format(KEY_LIST_STYLE, classify.name()), null);
	}
	
	public static void setListStyle(Classify classify, String value) {
		Editor editor = sPrefer.edit();
		editor.putString(String.format(KEY_LIST_STYLE, classify.name()), value);
		editor.commit();
	}
	
	public static boolean getShowHidden() {
		return sPrefer.getBoolean(KEY_SHOW_HIDDEN, false);
	}
	
	public static void setShowHidden(boolean visible) {
		Editor editor = sPrefer.edit();
		editor.putBoolean(KEY_SHOW_HIDDEN, visible);
		editor.commit();
	}
	
	public static int getNumLimit(Classify classify) {
		int num = sPrefer.getInt(String.format(KEY_NUM_LIMIT, classify.name()), NUM_LIMIT_DEF);
		if (num < NUM_LIMIT_MIN || num > NUM_LIMIT_MAX) {
			num = NUM_LIMIT_DEF;
			setNumLimit(classify, num);
		}
		
		return num;
	}
	
	public static void setNumLimit(Classify classify, int num) {
		if (num < NUM_LIMIT_MIN || num > NUM_LIMIT_MAX) {
			num = NUM_LIMIT_DEF;
		}
		
		Editor editor = sPrefer.edit();
		editor.putInt(String.format(KEY_NUM_LIMIT, classify.name()), num);
		editor.commit();
	}
}
