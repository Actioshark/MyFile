package kk.myfile.activity;

import java.util.HashMap;
import java.util.Map;

import kk.myfile.R;
import kk.myfile.util.Setting;

public class SettingListStyleActivity {
	public static class ListStyle {
		public int layout;
		public int column;
		
		public ListStyle(int layout, int column) {
			this.layout = layout;
			this.column = column;
		}
	}
	
	private static final Map<String, ListStyle> sListMap = new HashMap<String, ListStyle>();
	
	static {
		sListMap.put(getKey(1, 1), new ListStyle(R.layout.grid_direct_1_1, 2));
		sListMap.put(getKey(1, 2), new ListStyle(R.layout.grid_direct_1_2, 4));
		sListMap.put(getKey(1, 3), new ListStyle(R.layout.grid_direct_1_3, 6));
		
		sListMap.put(getKey(2, 1), new ListStyle(R.layout.grid_direct_2_1, 1));
		sListMap.put(getKey(2, 2), new ListStyle(R.layout.grid_direct_2_2, 1));
		sListMap.put(getKey(2, 3), new ListStyle(R.layout.grid_direct_2_3, 1));
	}
	
	public static String getKey(int cls, int index) {
		return String.format(Setting.LOCALE, "ls_%d_%d", cls, index);
	}
	
	public static ListStyle getListStyle(String key) {
		if (sListMap.containsKey(key)) {
			return sListMap.get(key);
		} else {
			return sListMap.get(getKey(1, 2));
		}
	}
}
