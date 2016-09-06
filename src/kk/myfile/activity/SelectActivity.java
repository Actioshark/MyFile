package kk.myfile.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.adapter.SelectAdapter;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SelectActivity extends BaseActivity {
	public static final String KEY_PATH = "select_path";
	
	private Direct mDirect;
	private final List<Direct> mHistory = new ArrayList<Direct>();
	
	private HorizontalScrollView mHsvPath;
	private ViewGroup mVgPath;
	
	private ListView mLvList;
	private SelectAdapter mSelectAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 数据
		String path = getIntent().getStringExtra(KEY_PATH);
		Direct direct = path == null ? null : new Direct(path);
		
		setContentView(R.layout.activity_select);
		
		// 路径栏
		mHsvPath = (HorizontalScrollView) findViewById(R.id.hsv_path);
		mVgPath = (ViewGroup) mHsvPath.findViewById(R.id.ll_path);
		
		// 文件列表
		mSelectAdapter = new SelectAdapter(this);
		mLvList = (ListView) findViewById(R.id.lv_list);
		mLvList.setAdapter(mSelectAdapter);
		
		View menu = findViewById(R.id.ll_menu);
		
		// 取消按钮
		menu.findViewById(R.id.iv_cancel)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		// 确定按钮
		menu.findViewById(R.id.iv_confirm)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Direct direct = mSelectAdapter.getSelected();
				if (direct == null) {
					Toast.makeText(getApplicationContext(), R.string.err_nothing_selected,
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				Intent intent = getIntent();
				intent.putExtra(KEY_PATH, direct.getPath());
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		showDirect(direct, false);
	}
	
	public void showDirect(Direct direct, boolean lastToHistory) {
		// 合法性
		try {
			File file = direct.getFile();
			if (file.exists() == false || file.isDirectory() == false || file.list() == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			if (lastToHistory) {
				Toast.makeText(getApplicationContext(), R.string.err_file_read_error,
					Toast.LENGTH_SHORT).show();
			}
			
			if (mHistory.size() > 0) {
				direct = mHistory.remove(mHistory.size() - 1);
				return;
			} else {
				direct = new Direct(Setting.DEFAULT_PATH);
			}
		}
		
		// 加入历史
		if (lastToHistory && mDirect != null) {
			if (mHistory.size() > 0) {
				Direct dir = mHistory.get(0);
				if (dir.getPath().equals(mDirect.getPath()) == false) {
					mHistory.add(mDirect);
				}
			} else {
				mHistory.add(mDirect);
			}
		}
		
		mDirect = direct;
		
		// 更新路径
		String path = mDirect.getPath();
		final String[] nodes;
		String[] temp = path.split("/");
		if (temp.length > 0) {
			nodes = temp;
		} else {
			nodes = new String[] {""};
		}
		mVgPath.removeAllViews();
		
		for (int i = 0; i < nodes.length; i++) {
			final int index = i;
			View grid = getLayoutInflater().inflate(R.layout.grid_path, null);
			TextView text = (TextView) grid.findViewById(R.id.tv_text);
			text.setText(String.format("%s %c", i == 0 ? "/" : nodes[i],
				i == nodes.length - 1 ? ' ' : '>'));
			
			grid.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					StringBuilder sb = new StringBuilder();
					
					for (int i = 1; i <= index; i++) {
						sb.append('/').append(nodes[i]);
					}
					
					showDirect(new Direct(sb.toString()), true);
				}
			});
			
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
			mVgPath.addView(grid, lp);
		}
		
		AppUtil.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mHsvPath.fullScroll(View.FOCUS_RIGHT);
			}
		});
		
		// 更新文件列表
		mDirect.loadChilren();
		mSelectAdapter.setData(mDirect.getChildren());
		mSelectAdapter.notifyDataSetChanged();
	}
	
	public void refresh() {
		// 合法性
		try {
			File file = mDirect.getFile();
			if (file.exists() == false || file.isDirectory() == false || file.list() == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), R.string.err_file_read_error,
				Toast.LENGTH_SHORT).show();
			
			if (mHistory.size() > 0) {
				mDirect = mHistory.remove(mHistory.size() - 1);
				return;
			} else {
				mDirect = new Direct(Setting.DEFAULT_PATH);
			}
		}
		
		// 更新文件列表
		mDirect.loadChilren();
		mSelectAdapter.setData(mDirect.getChildren());
		mSelectAdapter.notifyDataSetChanged();
	}
	
	public void showSearchResult(Leaf[] list) {
		mSelectAdapter.setData(list);
		mSelectAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		refresh();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			int size = mHistory.size();
			if (size > 0) {
				showDirect(mHistory.remove(size - 1), false);
				return true;
			}
		}
		
		return super.onKeyUp(keyCode, event);
	}
}
