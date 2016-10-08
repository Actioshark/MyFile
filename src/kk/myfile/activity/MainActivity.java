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
import android.widget.TextView;

import kk.myfile.R;
import kk.myfile.leaf.Apk;
import kk.myfile.leaf.Audio;
import kk.myfile.leaf.Image;
import kk.myfile.leaf.Office;
import kk.myfile.leaf.Text;
import kk.myfile.leaf.Video;
import kk.myfile.leaf.Zip;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.SimpleDialog;
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
		View root;
		
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
						App.showToast(R.string.err_path_not_valid);
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
					dialog.setMessage(R.string.msg_select_operation);
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
		root = findViewById(R.id.ll_file_1);
		
		root.findViewById(R.id.ll_text).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(TypeActivity.KEY_TYPE, TypeActivity.TYPE_CLASS);
				intent.putExtra(TypeActivity.KEY_CLASS, Text.class.getName());
				startActivity(intent);
			}
		});
		
		root.findViewById(R.id.ll_image).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(TypeActivity.KEY_TYPE, TypeActivity.TYPE_CLASS);
				intent.putExtra(TypeActivity.KEY_CLASS, Image.class.getName());
				startActivity(intent);
			}
		});
		
		root.findViewById(R.id.ll_audio).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(TypeActivity.KEY_TYPE, TypeActivity.TYPE_CLASS);
				intent.putExtra(TypeActivity.KEY_CLASS, Audio.class.getName());
				startActivity(intent);
			}
		});
		
		root.findViewById(R.id.ll_video).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(TypeActivity.KEY_TYPE, TypeActivity.TYPE_CLASS);
				intent.putExtra(TypeActivity.KEY_CLASS, Video.class.getName());
				startActivity(intent);
			}
		});
		
		root = findViewById(R.id.ll_file_2);
		
		root.findViewById(R.id.ll_office).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(TypeActivity.KEY_TYPE, TypeActivity.TYPE_CLASS);
				intent.putExtra(TypeActivity.KEY_CLASS, Office.class.getName());
				startActivity(intent);
			}
		});
		
		root.findViewById(R.id.ll_zip).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(TypeActivity.KEY_TYPE, TypeActivity.TYPE_CLASS);
				intent.putExtra(TypeActivity.KEY_CLASS, Zip.class.getName());
				startActivity(intent);
			}
		});
		
		root.findViewById(R.id.ll_apk).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(TypeActivity.KEY_TYPE, TypeActivity.TYPE_CLASS);
				intent.putExtra(TypeActivity.KEY_CLASS, Apk.class.getName());
				startActivity(intent);
			}
		});
		
		// 文件分类
		root = findViewById(R.id.ll_file_3);
		
		root.findViewById(R.id.ll_big).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(TypeActivity.KEY_TYPE, TypeActivity.TYPE_BIG);
				startActivity(intent);
			}
		});
		
		root.findViewById(R.id.ll_recent).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(TypeActivity.KEY_TYPE, TypeActivity.TYPE_RECENT);
				startActivity(intent);
			}
		});
		
		// 功能按钮
		root = findViewById(R.id.ll_fun);
		
		root.findViewById(R.id.iv_exit)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		
		root.findViewById(R.id.iv_setting)
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
			if (resultCode != RESULT_OK || data == null) {
				return;
			}
			
			int index = data.getIntExtra(KEY_INDEX, -1);
			String path = data.getStringExtra(SelectActivity.KEY_PATH);
			try {
				File file = new File(path);
				if (file.exists() == false || file.isDirectory() == false) {
					throw new Exception();
				}
			} catch (Exception e) {
				App.showToast(R.string.err_path_not_valid);
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
