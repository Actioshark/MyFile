package kk.myfile.activity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
		public int vertSpace;
		public boolean needDetail;

		public ListStyle(int layout, int column, int vertSpace, boolean needDetail) {
			this.layout = layout;
			this.column = column;
			this.vertSpace = vertSpace;
			this.needDetail = needDetail;
		}
	}

	private static final Map<String, ListStyle> sListMap = new LinkedHashMap<String, ListStyle>();

	static {
		sListMap.put(getKey(1, 1), new ListStyle(R.layout.grid_direct_1_1, 7, 0, true));
		sListMap.put(getKey(1, 2), new ListStyle(R.layout.grid_direct_1_2, 5, 0, true));
		sListMap.put(getKey(1, 3), new ListStyle(R.layout.grid_direct_1_3, 3, 0, true));

		sListMap.put(getKey(2, 1), new ListStyle(R.layout.grid_direct_2_1, 1, 4, false));
		sListMap.put(getKey(2, 2), new ListStyle(R.layout.grid_direct_2_2, 1, 4, false));
		sListMap.put(getKey(2, 3), new ListStyle(R.layout.grid_direct_2_3, 1, 4, false));
	}

	public static int getStyleSize() {
		return sListMap.size();
	}

	public static int getStyleIndex(String key) {
		int i = 0;

		for (String k : sListMap.keySet()) {
			if (k.equals(key)) {
				return i;
			}

			i++;
		}

		return -1;
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

	private Classify mClassify;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 数据
		try {
			mClassify = Classify.valueOf(getIntent().getStringExtra(KEY_CLASSIFY));
		} catch (Exception e) {
			mClassify = Classify.Direct;
		}

		setContentView(R.layout.activity_setting_list_style);

		for (int i = 1; i <= 2; i++) {
			for (int j = 1; j <= 3; j++) {
				View root = findViewById(AppUtil.getId("id", String.format(Setting.LOCALE,
					"ll_%d_%d", i, j)));

				final ViewHolder vh = new ViewHolder();
				vh.select = root.findViewById(R.id.iv_select);
				vh.key = getKey(i, j);

				root.setTag(vh);
				root.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						Setting.setListStyle(mClassify, vh.key);
						refreshList();
					}
				});

				mList.add(vh);
			}
		}

		View menu = findViewById(R.id.ll_menu);
		menu.findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
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
		String sel = Setting.getListStyle(mClassify);

		for (ViewHolder vh : mList) {
			if (vh.key.equals(sel)) {
				vh.select.setImageResource(R.drawable.single_select_pre);
			} else {
				vh.select.setImageResource(R.drawable.single_select_nor);
			}
		}
	}
}
