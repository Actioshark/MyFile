package kk.myfile.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kk.myfile.R;
import kk.myfile.activity.MainActivity.DefPath;
import kk.myfile.activity.SettingNumLimitActivity;
import kk.myfile.file.Sorter.Classify;

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
	static final String KEY_LIST_STYLE = "list_style";
	static final String KEY_SHOW_HIDDEN = "show_hidden";
	static final String KEY_BIG_FILE_NUM = "big_file_num";
	static final String KEY_RECENT_FILE_NUM = "recent_file_num";

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
				
				ja.put(i, dp);
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
	
	public static String getListStyle() {
		return sPrefer.getString(KEY_LIST_STYLE, null);
	}
	
	public static void setListStyle(String value) {
		Editor editor = sPrefer.edit();
		editor.putString(KEY_LIST_STYLE, value);
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
	
	public static int getBigFileNum() {
		int num = sPrefer.getInt(KEY_BIG_FILE_NUM, SettingNumLimitActivity.DEF_NUM);
		if (num < SettingNumLimitActivity.MIN_NUM || num > SettingNumLimitActivity.MAX_NUM) {
			num = SettingNumLimitActivity.DEF_NUM;
			setBigFileNum(num);
		}
		
		return num;
	}
	
	public static void setBigFileNum(int num) {
		if (num < SettingNumLimitActivity.MIN_NUM || num > SettingNumLimitActivity.MAX_NUM) {
			num = SettingNumLimitActivity.DEF_NUM;
		}
		
		Editor editor = sPrefer.edit();
		editor.putInt(KEY_BIG_FILE_NUM, num);
		editor.commit();
	}
	
	public static int getRecentFileNum() {
		int num = sPrefer.getInt(KEY_RECENT_FILE_NUM, SettingNumLimitActivity.DEF_NUM);
		if (num < SettingNumLimitActivity.MIN_NUM || num > SettingNumLimitActivity.MAX_NUM) {
			num = SettingNumLimitActivity.DEF_NUM;
			setRecentFileNum(num);
		}
		
		return num;
	}
	
	public static void setRecentFileNum(int num) {
		if (num < SettingNumLimitActivity.MIN_NUM || num > SettingNumLimitActivity.MAX_NUM) {
			num = SettingNumLimitActivity.DEF_NUM;
		}
		
		Editor editor = sPrefer.edit();
		editor.putInt(KEY_RECENT_FILE_NUM, num);
		editor.commit();
	}
}
