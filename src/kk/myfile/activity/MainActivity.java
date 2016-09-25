package kk.myfile.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import kk.myfile.R;
import kk.myfile.leaf.Apk;
import kk.myfile.leaf.Audio;
import kk.myfile.leaf.Image;
import kk.myfile.leaf.Office;
import kk.myfile.leaf.Text;
import kk.myfile.leaf.Video;
import kk.myfile.leaf.Zip;
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
	
	private TextView mTvText;
	private TextView mTvImage;
	private TextView mTvAudio;
	private TextView mTvVideo;
	private TextView mTvOffice;
	private TextView mTvZip;
	private TextView mTvApk;
	
	private View mViewRefresh;
	
	@SuppressLint("CutPasteId")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		View root, temp;
		
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
					dialog.setMessage(R.string.hint_select_operation);
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
		
		temp = root.findViewById(R.id.ll_text);
		mTvText = (TextView) temp.findViewById(R.id.tv_text);
		
		temp = root.findViewById(R.id.ll_image);
		mTvImage = (TextView) temp.findViewById(R.id.tv_text);
		
		temp = root.findViewById(R.id.ll_audio);
		mTvAudio = (TextView) temp.findViewById(R.id.tv_text);
		
		temp = root.findViewById(R.id.ll_video);
		mTvVideo = (TextView) temp.findViewById(R.id.tv_text);
		
		root = findViewById(R.id.ll_file_2);
		
		temp = root.findViewById(R.id.ll_office);
		mTvOffice = (TextView) temp.findViewById(R.id.tv_text);
		
		temp = root.findViewById(R.id.ll_zip);
		mTvZip = (TextView) temp.findViewById(R.id.tv_text);
		
		temp = root.findViewById(R.id.ll_apk);
		mTvApk = (TextView) temp.findViewById(R.id.tv_text);
		
		// 功能按钮
		root = findViewById(R.id.ll_fun);
		
		root.findViewById(R.id.iv_exit)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		
		mViewRefresh = root.findViewById(R.id.iv_refresh);
		mViewRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Tree.refresh();
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
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Broadcast.addListener(this, Tree.BC_START, true);
		Broadcast.addListener(this, Tree.BC_UPDATE, true);
		Broadcast.addListener(this, Tree.BC_COMPLETED, true);
		
		setRefreshAnim(Tree.isRefreshing());
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
			mTvText.setText(String.format("%d", Tree.getTypedLeaves(Text.class).size()));
			mTvImage.setText(String.format("%d", Tree.getTypedLeaves(Image.class).size()));
			mTvAudio.setText(String.format("%d", Tree.getTypedLeaves(Audio.class).size()));
			mTvVideo.setText(String.format("%d", Tree.getTypedLeaves(Video.class).size()));
			mTvOffice.setText(String.format("%d", Tree.getTypedLeaves(Office.class).size()));
			mTvZip.setText(String.format("%d", Tree.getTypedLeaves(Zip.class).size()));
			mTvApk.setText(String.format("%d", Tree.getTypedLeaves(Apk.class).size()));
			
			setRefreshAnim(true);
		} else if (Tree.BC_COMPLETED.equals(name)) {
			setRefreshAnim(false);
		}
	}
	
	private void setRefreshAnim(boolean start) {
		if (start) {
			mViewRefresh.setVisibility(View.GONE);
		} else {
			mViewRefresh.setVisibility(View.VISIBLE);
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
