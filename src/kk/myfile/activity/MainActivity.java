package kk.myfile.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import kk.myfile.R;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Setting;

public class MainActivity extends BaseActivity {
	public static final int REQ_SELECT_PATH = 1;
	
	public static final String KEY_INDEX = "main_index";
	
	private List<String> mPaths;
	private final List<TextView> mTvDirects = new ArrayList<TextView>();
	private View mLlAdd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		for (int i = 0; ; i++) {
			int id = AppUtil.getId("id", "tv_dir_" + i);
			if (id == 0) {
				break;
			}
			TextView tv = (TextView) findViewById(id);
			mTvDirects.add(tv);
			
			final int index = i;
			
			tv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					String path = mPaths.get(index);
					try {
						File file = new File(path);
						if (file.exists() == false || file.isDirectory() == false) {
							throw new Exception();
						}
					} catch (Exception e) {
						Toast.makeText(getApplicationContext(), R.string.err_path_not_valid,
								Toast.LENGTH_SHORT).show();;
						return;
					}
					
					Intent intent = new Intent(MainActivity.this, DirectActivity.class);
					intent.putExtra(DirectActivity.KEY_PATH, path);
					startActivity(intent);
				}
			});
			
			tv.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					Intent intent = new Intent(MainActivity.this, SelectActivity.class);
					intent.putExtra(SelectActivity.KEY_PATH, mPaths.get(index));
					intent.putExtra(KEY_INDEX, index);
					startActivityForResult(intent, REQ_SELECT_PATH);
					return true;
				}
			});
		}
		
		mLlAdd = findViewById(R.id.ll_add);
		mLlAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, SelectActivity.class);
				startActivityForResult(intent, REQ_SELECT_PATH);
			}
		});
		
		mPaths = Setting.getDefPath();
		refreshPath();
	}
	
	private void refreshPath() {
		for (int i = 0; i < mTvDirects.size(); i++) {
			TextView tv = mTvDirects.get(i);
			
			if (i < mPaths.size()) {
				tv.setText(mPaths.get(i));
				tv.setVisibility(View.VISIBLE);
			} else {
				tv.setVisibility(View.GONE);
			}
		}
		
		if (mPaths.size() < mTvDirects.size()) {
			mLlAdd.setVisibility(View.VISIBLE);
		} else {
			mLlAdd.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQ_SELECT_PATH) {
			if (data != null) {
				int index = data.getIntExtra(KEY_INDEX, -1);
				String path = data.getStringExtra(SelectActivity.KEY_PATH);
				try {
					File file = new File(path);
					if (file.exists() == false || file.isDirectory() == false) {
						throw new Exception();
					}
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), R.string.err_path_not_valid,
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (index >= 0 && index < mPaths.size()) {
					mPaths.set(index, path);
				} else {
					mPaths.add(path);
				}
				
				Setting.setDefPath(mPaths);
				refreshPath();
			}
		}
	}
}
