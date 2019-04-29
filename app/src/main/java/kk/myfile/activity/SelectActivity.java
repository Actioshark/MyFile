package kk.myfile.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.activity.DirectActivity.Node;
import kk.myfile.adapter.SelectAdapter;
import kk.myfile.file.Tree;
import kk.myfile.file.Tree.IProgressCallback;
import kk.myfile.file.Tree.ProgressType;
import kk.myfile.leaf.Direct;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.InputDialog;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Setting;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class SelectActivity extends BaseActivity {
	private Node mNode;
	private final List<Node> mHistory = new ArrayList<Node>();

	private HorizontalScrollView mHsvPath;
	private ViewGroup mVgPath;

	private ListView mLvList;
	private SelectAdapter mSelectAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 数据
		String path = getIntent().getStringExtra(KEY_PATH);
		if (path == null) {
			path = Setting.getLastSelectPath();
		}
		Direct direct = new Direct(path);

		setContentView(R.layout.activity_select);

		// 路径栏
		mHsvPath = (HorizontalScrollView) findViewById(R.id.hsv_path);
		mVgPath = (ViewGroup) mHsvPath.findViewById(R.id.ll_path);

		// 文件列表
		mSelectAdapter = new SelectAdapter(this);
		mLvList = (ListView) findViewById(R.id.lv_list);
		mLvList.setAdapter(mSelectAdapter);
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

		// 取消按钮
		menu.findViewById(R.id.iv_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		menu.findViewById(R.id.iv_cancel).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				final InputDialog id = new InputDialog(SelectActivity.this);
				id.setMessage(R.string.msg_input_path_name);
				id.setClickListener(new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index, ClickType type) {
						if (index == 1) {
							String path = id.getInput();
							
							if (!path.startsWith("/")) {
								String dir = mNode.direct.getPath();
								if (dir.endsWith("/")) {
									path = dir + path;
								} else {
									path = dir + "/" + path;
								}
							}
							
							showDirect(new Node(new Direct(path.toString())), true);
						}
						
						dialog.dismiss();
					}
				});
				id.show();
				return true;
			}
		});
		
		// 新建目录
		menu.findViewById(R.id.iv_new).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Tree.createDirect(SelectActivity.this, mNode.direct.getPath(), null,
					new IProgressCallback() {
						@Override
						public void onProgress(ProgressType type, Object... data) {
							refreshDirect();
						}
					});
			}
		});

		// 确定按钮
		menu.findViewById(R.id.iv_confirm).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Direct direct = mSelectAdapter.getSelected();
				if (direct == null) {
					direct = mNode.direct;
				}
				
				String path = direct.getPath();
				Setting.setLastSelectPath(path);

				Intent intent = getIntent();
				intent.putExtra(KEY_PATH, path);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		showDirect(new Node(direct), false);
	}

	public void showDirect(Node node, boolean lastToHistory) {
		if (mNode != null && node.direct.getPath().equals(mNode.direct.getPath())) {
			mNode = node;
			refreshDirect();
			return;
		}

		// 合法性
		try {
			File file = node.direct.getFile();
			if (!file.exists() || !file.isDirectory() || file.list() == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			if (mNode != null) {
				App.showToast(R.string.err_file_read_error);
			}

			if (mNode != null) {
				return;
			} else if (mHistory.size() > 0) {
				node = mHistory.remove(mHistory.size() - 1);
			} else {
				node = new Node(new Direct(Setting.DEFAULT_PATH));
			}
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
			TextView text = (TextView) grid.findViewById(R.id.tv_text);
			text.setText(String.format("%s %c", i == 0 ? "/" : nodes[i],
				i == nodes.length - 1 ? ' ' : '>'));

			grid.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					StringBuilder sb = new StringBuilder();

					if (index > 0) {
						for (int i = 1; i <= index; i++) {
							sb.append('/').append(nodes[i]);
						}
					} else {
						sb.append('/');
					}

					showDirect(new Node(new Direct(sb.toString())), true);
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
		node.direct.loadChildren(false);
		mSelectAdapter.setData(node.direct.getChildren(), node.position);
	}

	public void refreshDirect() {
		// 合法性
		try {
			File file = mNode.direct.getFile();
			if (!file.exists() || !file.isDirectory() || file.list() == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			App.showToast(R.string.err_file_read_error);

			if (mHistory.size() > 0) {
				mNode = mHistory.remove(mHistory.size() - 1);
			} else {
				mNode = new Node(new Direct(Setting.DEFAULT_PATH));
			}
		}

		// 更新文件列表
		mNode.direct.loadChildren(false);
		mSelectAdapter.setData(mNode.direct.getChildren(), -1);
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
		if (position >= 0 && position < mSelectAdapter.getCount()) {
			mLvList.setSelection(position);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		refreshDirect();
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
