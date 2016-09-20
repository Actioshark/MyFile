package kk.myfile.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import kk.myfile.R;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Setting;

public class SettingListStyleActivity extends BaseActivity {
	public static class ListStyle {
		public int layout;
		public int column;
		public int space;
		
		public ListStyle(int layout, int column, int space) {
			this.layout = layout;
			this.column = column;
			this.space = space;
		}
	}
	
	private static final Map<String, ListStyle> sListMap = new HashMap<String, ListStyle>();
	
	static {
		sListMap.put(getKey(1, 1), new ListStyle(R.layout.grid_direct_1_1, 7, 0));
		sListMap.put(getKey(1, 2), new ListStyle(R.layout.grid_direct_1_2, 5, 0));
		sListMap.put(getKey(1, 3), new ListStyle(R.layout.grid_direct_1_3, 3, 0));
		
		sListMap.put(getKey(2, 1), new ListStyle(R.layout.grid_direct_2_1, 1, 4));
		sListMap.put(getKey(2, 2), new ListStyle(R.layout.grid_direct_2_2, 1, 4));
		sListMap.put(getKey(2, 3), new ListStyle(R.layout.grid_direct_2_3, 1, 4));
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
	
	
	class ViewHolder {
		public ImageView select;
		public String key;
	}
	
	private final List<ViewHolder> mList = new ArrayList<ViewHolder>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setting_list_style);
		
		for (int i = 1; i <= 2; i++) {
			for (int j = 1; j <= 3; j++) {
				View root = findViewById(AppUtil.getId("id", String.format(Setting.LOCALE, "ll_%d_%d", i, j)));
				
				final ViewHolder vh = new ViewHolder();
				vh.select = (ImageView) root.findViewById(R.id.iv_select);
				vh.key = getKey(i, j);
				
				root.setTag(vh);
				root.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						Setting.setListStyle(vh.key);
						refreshList();
					}
				});
				
				mList.add(vh);
			}
		}
		
		View menu = findViewById(R.id.ll_menu);
		menu.findViewById(R.id.iv_back)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		refreshList();
	}
	
	private void refreshList() {
		String sel = Setting.getListStyle();
		
		for (ViewHolder vh : mList) {
			if (vh.key.equals(sel)) {
				vh.select.setImageResource(R.drawable.select_pre);
			} else {
				vh.select.setImageResource(R.drawable.select_nor);
			}
		}
	}
}
