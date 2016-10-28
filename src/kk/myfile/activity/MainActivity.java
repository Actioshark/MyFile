package kk.myfile.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import kk.myfile.R;
import kk.myfile.file.FileUtil;
import kk.myfile.file.Tree;
import kk.myfile.leaf.Apk;
import kk.myfile.leaf.Audio;
import kk.myfile.leaf.Image;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Office;
import kk.myfile.leaf.Text;
import kk.myfile.leaf.Video;
import kk.myfile.leaf.Zip;
import kk.myfile.ui.CakeView;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.InputDialog;
import kk.myfile.ui.SimpleDialog;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.Setting;

public class MainActivity extends BaseActivity {
	public static final int REQ_SELECT_PATH = 1;
	
	public static final String KEY_INDEX = "main_index";
	public static final String KEY_NAME = "main_name";
	
	public static class DefPath {
		public String name;
		public String path;
	}
	private List<DefPath> mPaths;
	
	private final List<TextView> mTvDirects = new ArrayList<TextView>();
	private View mLlAdd;
	
	private final Class<?>[] mTypes = new Class<?>[] {
		Text.class, Image.class, Audio.class, Video.class,
		Office.class, Zip.class, Apk.class,
	};
	private final List<TextView> mTvTypes = new ArrayList<TextView>();
	private Object mTypeMark;
	
	private CakeView mCvStat;
	private TextView mTvStat;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		View root, parent;
		
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
					DefPath dp = mPaths.get(index);
					try {
						File file = new File(dp.path);
						if (file.exists() == false || file.isDirectory() == false) {
							throw new Exception();
						}
					} catch (Exception e) {
						App.showToast(R.string.err_path_not_valid);
						return;
					}
					
					Intent intent = new Intent(MainActivity.this, DirectActivity.class);
					intent.putExtra(DirectActivity.KEY_PATH, dp.path);
					startActivity(intent);
				}
			});
			
			tv.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					SimpleDialog dialog = new SimpleDialog(MainActivity.this);
					dialog.setMessage(R.string.msg_select_operation);
					dialog.setButtons(R.string.word_cancel, R.string.word_delete, R.string.word_edit);
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
								intent.putExtra(SelectActivity.KEY_PATH, mPaths.get(index).path);
								intent.putExtra(KEY_INDEX, index);
								intent.putExtra(KEY_NAME, mPaths.get(index).name);
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
		
		parent = root.findViewById(R.id.ll_text);
		parent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Type.name());
				intent.putExtra(TypeActivity.KEY_TYPE, Text.class.getName());
				startActivity(intent);
			}
		});
		mTvTypes.add((TextView) parent.findViewById(R.id.tv_text));
		
		parent = root.findViewById(R.id.ll_image);
		parent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Type.name());
				intent.putExtra(TypeActivity.KEY_TYPE, Image.class.getName());
				startActivity(intent);
			}
		});
		mTvTypes.add((TextView) parent.findViewById(R.id.tv_text));
		
		parent = root.findViewById(R.id.ll_audio);
		parent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Type.name());
				intent.putExtra(TypeActivity.KEY_TYPE, Audio.class.getName());
				startActivity(intent);
			}
		});
		mTvTypes.add((TextView) parent.findViewById(R.id.tv_text));
		
		parent = root.findViewById(R.id.ll_video);
		parent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Type.name());
				intent.putExtra(TypeActivity.KEY_TYPE, Video.class.getName());
				startActivity(intent);
			}
		});
		mTvTypes.add((TextView) parent.findViewById(R.id.tv_text));
		
		root = findViewById(R.id.ll_file_2);
		
		parent = root.findViewById(R.id.ll_office);
		parent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Type.name());
				intent.putExtra(TypeActivity.KEY_TYPE, Office.class.getName());
				startActivity(intent);
			}
		});
		mTvTypes.add((TextView) parent.findViewById(R.id.tv_text));
		
		parent = root.findViewById(R.id.ll_zip);
		parent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Type.name());
				intent.putExtra(TypeActivity.KEY_TYPE, Zip.class.getName());
				startActivity(intent);
			}
		});
		mTvTypes.add((TextView) parent.findViewById(R.id.tv_text));
		
		parent = root.findViewById(R.id.ll_apk);
		parent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Type.name());
				intent.putExtra(TypeActivity.KEY_TYPE, Apk.class.getName());
				startActivity(intent);
			}
		});
		mTvTypes.add((TextView) parent.findViewById(R.id.tv_text));
		
		parent = root.findViewById(R.id.ll_stat);
		mCvStat = (CakeView) parent.findViewById(R.id.cv_view);
		mTvStat = (TextView) parent.findViewById(R.id.tv_text);
		
		// 文件分类
		root = findViewById(R.id.ll_file_3);
		
		root.findViewById(R.id.ll_big).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Big.name());
				startActivity(intent);
			}
		});
		
		root.findViewById(R.id.ll_recent).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, TypeActivity.class);
				intent.putExtra(KEY_CLASSIFY, Classify.Recent.name());
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
		
		Tree.refreshTypeDirect();
	}
	
	private void refreshPath() {
		for (int i = 0; i < mTvDirects.size(); i++) {
			TextView tv = mTvDirects.get(i);
			
			if (i < mPaths.size()) {
				tv.setText(mPaths.get(i).name);
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
	protected void onResume() {
		super.onResume();
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				final Object mark = new Object();
				synchronized (mTvTypes) {
					mTypeMark = mark;
				}
				
				while (isFinishing() == false) {
					synchronized (mTypeMark) {
						if (mTypeMark != mark) {
							return;
						}
					}
					boolean finished = Tree.isTypeDirectRefreshing() == false;
					
					final long[] counts = new long[mTypes.length];
					final long[] sizes = new long[mTypes.length];
					
					List<Leaf> list = Tree.getTypeDirect();
					for (int i = 0; i < list.size(); i++) {
						Leaf leaf = list.get(i);
							
						for (int j = 0; j < mTypes.length; j++) {
							try {
								if (mTypes[j].isInstance(leaf)) {
									counts[j]++;
									sizes[j] += leaf.getFile().length();
									break;
								}
							} catch (Exception e) {
								Logger.print(null, e);
							}
						}
					}
					
					final List<CakeView.Arc> arcs = new ArrayList<CakeView.Arc>();
					float total = 0l;
					float avail = 0l;
					try {
						total = FileUtil.getTotalSize();
						avail = FileUtil.getAvailSize();
						float unknown = total - avail;
						
						for (int i = 0; i < sizes.length; i++) {
							CakeView.Arc arc = new CakeView.Arc();
							arc.ratio = sizes[i] / total;
							arc.color = mTypes[i].getDeclaredField("COLOR").getInt(null);
							arcs.add(arc);
							
							unknown -= sizes[i];
						}
						
						CakeView.Arc arc = new CakeView.Arc();
						arc.ratio = unknown / total;
						arc.color = 0xffcccccc;
						arcs.add(arc);
					} catch (Exception e) {
						Logger.print(null, e);
					}
					
					final float TOTAL = total / (1024 * 1024 * 1024);
					final float AVAIL = avail / (1024 * 1024 * 1024);
					AppUtil.runOnUiThread(new Runnable() {
						public void run() {
							synchronized (mTvTypes) {
								if (mTypeMark == mark) {
									for (int i = 0; i < mTvTypes.size(); i++) {
										mTvTypes.get(i).setText(String.format("%d", counts[i]));
									}
									
									mCvStat.setArcs(arcs);
									mCvStat.invalidate();
									mTvStat.setText(String.format("%.2f/%.2f", TOTAL - AVAIL, TOTAL));
								}
							}
						}
					});
					
					if (finished) {
						return;
					}
					
					SystemClock.sleep(300);
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQ_SELECT_PATH) {
			if (resultCode != RESULT_OK || data == null) {
				return;
			}
			
			final String path = data.getStringExtra(SelectActivity.KEY_PATH);
			File file;
			try {
				file = new File(path);
				if (file.exists() == false || file.isDirectory() == false) {
					throw new Exception();
				}
			} catch (Exception e) {
				App.showToast(R.string.err_path_not_valid);
				return;
			}
			
			final int idx = data.getIntExtra(KEY_INDEX, -1);
			String name = data.getStringExtra(KEY_NAME);
			if (name == null) {
				name = file.getName();
			}
			
			final InputDialog id = new InputDialog(this);
			id.setMessage(R.string.msg_input_path_name);
			id.setInput(name);
			id.setClickListener(new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					if (index == 1) {
						String name = id.getInput();
						if (name.length() < 1 || name.length() > 20) {
							App.showToast(R.string.err_name_length_valid, 1, 20);
							return;
						}
						
						DefPath dp = new DefPath();
						dp.name = name;
						dp.path = path;
						
						if (idx >= 0 && idx < mPaths.size()) {
							mPaths.set(idx, dp);
						} else {
							mPaths.add(dp);
						}
						
						Setting.setDefPath(mPaths);
						refreshPath();
					}
					
					id.dismiss();
				}
			});
			id.show();
		}
	}
}
