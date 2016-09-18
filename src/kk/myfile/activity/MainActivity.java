package kk.myfile.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import kk.myfile.R;
import kk.myfile.tree.Tree;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.SimpleDialog;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Broadcast;
import kk.myfile.util.Broadcast.IListener;
import kk.myfile.util.Setting;

public class MainActivity extends BaseActivity implements IListener {
	public static final int REQ_SELECT_PATH = 1;
	
	public static final String KEY_INDEX = "main_index";
	
	private List<String> mPaths;
	private final List<TextView> mTvDirects = new ArrayList<TextView>();
	private View mLlAdd;
	
	private View mViewRefresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		// 常用路径
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
					SimpleDialog dialog = new SimpleDialog(MainActivity.this);
					dialog.setContent(R.string.hint_select_operation);
					dialog.setButtons(new int[] {R.string.word_cancel, R.string.word_delete, R.string.word_edit});
					dialog.setClickListener(new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int btn) {
							if (btn == 1) {
								if (index < mPaths.size()) {
									mPaths.remove(index);
									Setting.setDefPath(mPaths);
									refreshPath();
								}
							} else if (btn == 2) {
								Intent intent = new Intent(MainActivity.this, SelectActivity.class);
								intent.putExtra(SelectActivity.KEY_PATH, mPaths.get(index));
								intent.putExtra(KEY_INDEX, index);
								startActivityForResult(intent, REQ_SELECT_PATH);
							}
							
							dialog.dismiss();
						}
					});
					dialog.show();
					
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
		
		// 文件分类
		// TODO
		
		// 功能按钮
		View funRoot = findViewById(R.id.ll_fun);
		
		funRoot.findViewById(R.id.iv_exit)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		
		View llRefresh = funRoot.findViewById(R.id.ll_refresh);
		llRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Tree.refresh();
			}
		});
		mViewRefresh = llRefresh.findViewById(R.id.iv_refresh);
		
		funRoot.findViewById(R.id.iv_setting)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, SettingActivity.class);
				startActivity(intent);
			}
		});
		
		mPaths = Setting.getDefPath();
		refreshPath();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Broadcast.addListener(this, Tree.BC_START, true);
		Broadcast.addListener(this, Tree.BC_UPDATE, true);
		Broadcast.addListener(this, Tree.BC_COMPLETED, true);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		Broadcast.removeLsitener(this, null);
	}
	
	@Override
	public void onReceive(String name, Object data) {
		if (Tree.BC_START.equals(name)) {
			setRefreshAnim(true);
		} else if (Tree.BC_UPDATE.equals(name)) {
			setRefreshAnim(true);
		} else if (Tree.BC_COMPLETED.equals(name)) {
			setRefreshAnim(false);
		}
	}
	
	private void setRefreshAnim(boolean start) {
		if (start && mViewRefresh.getTag() == null) {
			Animation anim = new RotateAnimation(0f, -360f,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			anim.setDuration(1000);
			anim.setRepeatCount(-1);
			
			mViewRefresh.startAnimation(anim);
			mViewRefresh.setTag(anim);
		} else if (start == false && mViewRefresh.getTag() != null) {
			mViewRefresh.clearAnimation();
			mViewRefresh.setTag(null);
		}
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
