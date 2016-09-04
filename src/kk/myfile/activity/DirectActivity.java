package kk.myfile.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.adapter.DirectAdapter;
import kk.myfile.leaf.Direct;
import kk.myfile.util.AppUtil;

import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DirectActivity extends BaseActivity {
	public static final String KEY_PATH = "direct_path";
	
	public static final String DEF_PATH = Environment.getExternalStorageDirectory()
		.getAbsolutePath();
	
	private Direct mDirect;
	private DirectAdapter mAdapter;
	private final List<Direct> mHistory = new ArrayList<Direct>();
	
	private HorizontalScrollView mHsvPath;
	private ViewGroup mVgPath;
	private GridView mGvList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String path = getIntent().getStringExtra(KEY_PATH);
		mDirect = new Direct(path);
		mAdapter = new DirectAdapter(this);
		mAdapter.setData(mDirect);
		
		setContentView(R.layout.activity_direct);
		
		View llPath = findViewById(R.id.ll_path);
		View ivHome = llPath.findViewById(R.id.iv_home);
		ivHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		mHsvPath = (HorizontalScrollView) llPath.findViewById(R.id.hsv_text);
		mVgPath = (ViewGroup) mHsvPath.findViewById(R.id.ll_text);
		
		mGvList = (GridView) findViewById(R.id.gv_grid);
		mGvList.setAdapter(mAdapter);
	}
	
	public void refresh() {
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
				mDirect = new Direct(DEF_PATH);
			}
		}
		
		mDirect.loadChilren(false);
		
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
					
					setDirect(new Direct(sb.toString()));
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
		
		mAdapter.setData(mDirect);
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		refresh();
	}
	
	public void setDirect(Direct direct) {
		if (mDirect != null) {
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
		
		refresh();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mHistory.size() > 0) {
				mDirect = mHistory.remove(mHistory.size() - 1);
				refresh();
				return true;
			}
		}
		
		return super.onKeyUp(keyCode, event);
	}
}
