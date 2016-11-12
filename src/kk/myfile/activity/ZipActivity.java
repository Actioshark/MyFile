package kk.myfile.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import kk.myfile.R;
import kk.myfile.activity.DirectActivity.Node;
import kk.myfile.adapter.ZipAdapter;
import kk.myfile.file.FileUtil;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.InputDialog;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;

import android.app.Dialog;
import android.os.Bundle;
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

public class ZipActivity extends BaseActivity {
	private ZipFile mZipFile;
	private final Map<String, Direct> mZipMap = new HashMap<String, Direct>();

	private Node mNode;
	private final List<Node> mHistory = new ArrayList<Node>();

	private HorizontalScrollView mHsvPath;
	private ViewGroup mVgPath;

	private ListView mLvList;
	private ZipAdapter mZipAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_zip);

		// 路径栏
		mHsvPath = (HorizontalScrollView) findViewById(R.id.hsv_path);
		mVgPath = (ViewGroup) mHsvPath.findViewById(R.id.ll_path);

		// 文件列表
		mZipAdapter = new ZipAdapter(this);
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

		// 返回按钮
		menu.findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		// 数据
		String path = getIntent().getStringExtra(KEY_PATH);
		try {
			mZipFile = new ZipFile(path);
			
			if (mZipFile.isValidZipFile() == false) {
				App.showToast(R.string.err_not_zip);
				finish();
				return;
			}
			
			if (mZipFile.isEncrypted()) {
				final InputDialog id = new InputDialog(this);
				id.setMessage(R.string.msg_input_password);
				id.setClickListener(new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index, ClickType type) {
						if (index == 1) {
							String password = id.getInput();
							
							try {
								mZipFile.setPassword(password);
							} catch (Exception e){
								Logger.print(null, e);
								App.showToast(R.string.err_decompress_failed);
								return;
							}
							
							dialog.dismiss();
							parseAndStart();
						} else {
							dialog.dismiss();
							finish();
						}
					}
				});
				id.setCanceledOnTouchOutside(false);
				id.show();
			} else {
				parseAndStart();
			}
		} catch (Exception e) {
			Logger.print(null, e);
			App.showToast(R.string.err_decompress_failed);
			finish();
			return;
		}
	}
	
	private void parseAndStart() {
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				final AtomicBoolean success = new AtomicBoolean(true);
				
				try {
					for (Object obj : mZipFile.getFileHeaders()) {
						FileHeader fh = (FileHeader) obj;
						String path = fh.getFileName();
						Leaf leaf;
						
						if (path.charAt(path.length() - 1) == '/') {
							Direct direct = mZipMap.get(path);
							if (direct == null) {
								direct = new Direct(path);
								mZipMap.put(path, direct);
							}
							
							leaf = direct;
						} else {
							leaf = FileUtil.createTempLeaf(path);
						}
						
						while(true) {
							int ni = -1;
							for (int i = path.length() - 2; i >= 0; i--) {
								if (path.charAt(i) == '/') {
									ni = i;
									break;
								}
							}
							
							String pp;
							if (ni == -1) {
								pp = "/";
							} else {
								pp = path.substring(0, ni + 1);
							}
							
							Direct parent = mZipMap.get(pp);
							boolean found = false;
							if (parent == null) {
								parent = new Direct(pp);
								mZipMap.put(pp, parent);
							} else {
								found = true;
							}
							
							parent.getChildren().add(leaf);
							
							if (found || pp.equals("/")) {
								break;
							} else {
								path = pp;
								leaf = parent;
							}
						}
					}
				} catch (Exception e) {
					Logger.print(null, e);
					
					success.set(false);
				}
				
				AppUtil.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (success.get()) {
							showDirect(new Node(mZipMap.get("/")), false);
						} else {
							App.showToast(R.string.err_decompress_failed);
							finish();
						}
					}
				});
			}
		});
	}

	public void showDirect(Node node, boolean lastToHistory) {
		if (mNode != null && node.direct.getPath().equals(mNode.direct.getPath())) {
			mNode = node;
			refreshDirect();
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
					
					sb.append('/');

					showDirect(new Node(mZipMap.get(sb.toString())), true);
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

	public void refreshDirect() {
		mZipAdapter.setData(mNode.direct.getChildren(), -1);
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
