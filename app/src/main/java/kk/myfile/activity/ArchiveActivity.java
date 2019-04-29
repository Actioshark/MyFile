package kk.myfile.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.activity.DirectActivity.Node;
import kk.myfile.adapter.ArchiveAdapter;
import kk.myfile.file.ArchiveHelper;
import kk.myfile.file.ArchiveHelper.FileHeader;
import kk.myfile.file.Tree.IProgressCallback;
import kk.myfile.file.Tree.ProgressType;
import kk.myfile.file.FileUtil;
import kk.myfile.file.Tree;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.InputDialog;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.Setting;

import android.app.Dialog;
import android.content.Intent;
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
	
	private Uri mUri;

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
		
		// 解压到
		menu.findViewById(R.id.iv_extract).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(ArchiveActivity.this,
					SelectActivity.class);
				startActivityForResult(intent, REQ_EXTRACT_TO);
			}
		});

		// 回退按钮
		menu.findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!backDirect()) {
					finish();
				}
			}
		});

		// 数据
		mUri = getIntent().getData();
		initArchive();
	}
	
	private void initArchive() {
		try {
			if (mUri == null || mUri.getPath() == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			Logger.print(e);
			finish();
			return;
		}
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				try {
					mArchiveHelper = new ArchiveHelper();
					boolean valid = mArchiveHelper.setFile(mUri.getPath());
					if (!valid) {
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
					Logger.print(e);
					
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
					Logger.print(e);
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
					File file = mArchiveHelper.extractFile(fh, destStr);
					
					final Leaf leaf = FileUtil.createLeaf(file);
					AppUtil.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							leaf.open(ArchiveActivity.this, false);
						}
					});
				} catch (Exception e) {
					Logger.print(e);
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
				if (!mNode.direct.getPath().equals(n.direct.getPath())) {
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
			TextView text = grid.findViewById(R.id.tv_text);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK || data == null) {
			return;
		}
		
		if (requestCode == REQ_EXTRACT_TO) {
			String path = data.getStringExtra(SelectActivity.KEY_PATH);

			Tree.extract(ArchiveActivity.this, mUri.getPath(), path,
				new IProgressCallback() {
					@Override
					public void onProgress(ProgressType type, Object... data) {
					}
				});
		}
	}
}
