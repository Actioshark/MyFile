package kk.myfile.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.activity.DirectActivity.Node;
import kk.myfile.adapter.ArchiveAdapter;
import kk.myfile.file.ArchiveHelper;
import kk.myfile.file.ArchiveHelper.FileHeader;
import kk.myfile.file.FileUtil;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.InputDialog;
import kk.myfile.util.AppUtil;
import kk.myfile.util.DataUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.Setting;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class ArchiveActivity extends BaseActivity {
	private ArchiveHelper mArchiveHelper;

	private Node mNode;
	private final List<Node> mHistory = new ArrayList<Node>();

	private HorizontalScrollView mHsvPath;
	private ViewGroup mVgPath;

	private ListView mLvList;
	private ArchiveAdapter mZipAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_archive);

		// 路径栏
		mHsvPath = (HorizontalScrollView) findViewById(R.id.hsv_path);
		mVgPath = (ViewGroup) mHsvPath.findViewById(R.id.ll_path);

		// 文件列表
		mZipAdapter = new ArchiveAdapter(this);
		mLvList = (ListView) findViewById(R.id.lv_list);
		mLvList.setAdapter(mZipAdapter);
		mLvList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int state) {
			}

			@Override
			public void onScroll(AbsListView view, int first, int visible, int total) {
				if (mNode != null) {
					mNode.position = first;
				}
			}
		});

		View menu = findViewById(R.id.ll_menu);
		
		// 主页按钮
		menu.findViewById(R.id.iv_home).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		// 回退按钮
		menu.findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (backDirect() == false) {
					finish();
				}
			}
		});

		// 数据
		Uri uri = getIntent().getData();
		initArchive(uri);
	}
	
	private void initArchive(final Uri uri) {
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				try {
					mArchiveHelper = new ArchiveHelper();
					boolean valid = mArchiveHelper.setFile(uri.getPath());
					if (valid == false) {
						AppUtil.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								App.showToast(R.string.err_not_support);
								finish();
							}
						});
						return;
					}
					
					if (mArchiveHelper.isEncrypted()) {
						AppUtil.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								final InputDialog id = new InputDialog(ArchiveActivity.this);
								id.setMessage(R.string.msg_input_password);
								id.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
								id.setClickListener(new IDialogClickListener() {
									@Override
									public void onClick(Dialog dialog, int index, ClickType type) {
										if (index == 1) {
											final String password = id.getInput();
											dialog.dismiss();
											
											AppUtil.runOnNewThread(new Runnable() {
												@Override
												public void run() {
													parseFileHeader(password);
												}
											});
										} else {
											dialog.dismiss();
											finish();
										}
									}
								});
								id.setCanceledOnTouchOutside(false);
								id.show();
							}
						});
					} else {
						parseFileHeader(null);
					}
				} catch (Exception e) {
					Logger.print(null, e);
					
					AppUtil.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							App.showToast(R.string.err_not_support);
							finish();
						}
					});
				}
			}
		});
	}
	
	private void parseFileHeader(String password) {
		if (password != null) {
			mArchiveHelper.setPassword(password);
		}
		final boolean success = mArchiveHelper.parseFileHeader();
		
		AppUtil.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (success) {
						Leaf leaf = mArchiveHelper.getFileHeader("/").getLeaf();
						showDirect(new Node((Direct) leaf), false);
						return;
					}
				} catch (Exception e) {
					Logger.print(null, e);
				}
					
				App.showToast(R.string.err_extract_failed);
				finish();
			}
		});
	}
	
	public void extractFile(final String path) {
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				try {
					String destStr = Setting.DEFAULT_PATH + "/temp";
					File destFile = new File(destStr);
					if (destFile.exists()) {
						FileUtil.delete(destFile, true);
					} else {
						FileUtil.createDirect(destFile);
					}
					
					FileHeader fh = mArchiveHelper.getFileHeader(path);
					mArchiveHelper.extractFile(fh, destStr);
					
					String name = DataUtil.getFileName(path);
					final Leaf leaf = FileUtil.createLeaf(new File(destStr, name));
					AppUtil.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							leaf.open(ArchiveActivity.this, false);
						}
					});
				} catch (Exception e) {
					Logger.print(null, e);
				}
			}
		});
	}

	public void showDirect(Node node, boolean lastToHistory) {
		if (mNode != null && node.direct.getPath().equals(mNode.direct.getPath())) {
			return;
		}

		// 加入历史
		if (lastToHistory && mNode != null) {
			int size = mHistory.size();
			if (size > 0) {
				Node n = mHistory.get(size - 1);
				if (mNode.direct.getPath().equals(n.direct.getPath()) == false) {
					mHistory.add(mNode);
				}
			} else {
				mHistory.add(mNode);
			}
		}

		// 更新路径
		mNode = node;

		String path = node.direct.getPath();
		final String[] nodes;
		String[] temp = path.split("/");
		if (temp.length > 0) {
			nodes = temp;
		} else {
			nodes = new String[] {
				""
			};
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
					
					if (sb.length() == 0) {
						sb.append('/');
					}

					Leaf leaf = mArchiveHelper.getFileHeader(sb.toString()).getLeaf();
					showDirect(new Node((Direct) leaf), true);
				}
			});

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
			mVgPath.addView(grid, lp);
		}

		AppUtil.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mHsvPath.fullScroll(View.FOCUS_RIGHT);
			}
		});

		// 更新文件列表
		mZipAdapter.setData(node.direct.getChildren(), node.position);
	}

	public boolean backDirect() {
		if (mHistory.size() > 0) {
			Node node = mHistory.remove(mHistory.size() - 1);
			showDirect(node, false);
			return true;
		}

		return false;
	}

	public void setSelection(int position) {
		if (position >= 0 && position < mZipAdapter.getCount()) {
			mLvList.setSelection(position);
		}
	}
	
	public FileHeader getFileHeader(String path) {
		return mArchiveHelper.getFileHeader(path);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (backDirect()) {
				return true;
			}
		}

		return super.onKeyUp(keyCode, event);
	}
}
