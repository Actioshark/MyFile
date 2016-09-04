package kk.myfile.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Setting {
	public static final String KEY_SHOW_HIDE = "show_hide";

	public static final String KEY_SORT_BRANCH = "sort_brach";
	public static final String KEY_SORT_LEAF = "sort_leaf";

	private static SharedPreferences sPrefer;

	public static void init(Context context) {
		if (sPrefer != null) {
			return;
		}
		
		sPrefer = context.getSharedPreferences("setting",
				Context.MODE_PRIVATE);
	}

	public static void setShowHide(boolean show) {
		Editor editor = sPrefer.edit();
		editor.putBoolean(KEY_SHOW_HIDE, show);
		editor.commit();
	}

	public static boolean getShowHide() {
		return sPrefer.getBoolean(KEY_SHOW_HIDE, false);
	}

	public static void setSortBranch(String value) {
		Editor editor = sPrefer.edit();
		editor.putString(KEY_SORT_BRANCH, value);
		editor.commit();
	}

	public static String getSortBranch() {
		return sPrefer.getString(KEY_SORT_BRANCH, null);
	}

	public static void setSortLeaf(String value) {
		Editor editor = sPrefer.edit();
		editor.putString(KEY_SORT_LEAF, value);
		editor.commit();
	}

	public static String getSortLeaf() {
		return sPrefer.getString(KEY_SORT_LEAF, null);
	}
}
