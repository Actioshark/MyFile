package kk.myfile.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.activity.SettingListStyleActivity.ListStyle;
import kk.myfile.adapter.DirectAdapter;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.tree.Tree;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Setting;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DirectActivity extends BaseActivity {
	public static final String KEY_PATH = "direct_path";
	
	public static class Node {
		public Direct direct;
		public int position = 0;
		
		public Node(Direct direct) {
			this.direct = direct;
		}
	}
	private Node mNode;
	private final List<Node> mHistory = new ArrayList<Node>();
	
	private HorizontalScrollView mHsvPath;
	private ViewGroup mVgPath;
	
	private EditText mEtSearch;
	private Runnable mSearchRun;
	
	private GridView mGvList;
	private DirectAdapter mDirectAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 数据
		String path = getIntent().getStringExtra(KEY_PATH);
		Direct direct = new Direct(path);
		
		setContentView(R.layout.activity_direct);
		
		// 路径栏
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
		
		// 搜索栏
		View llSearch = findViewById(R.id.ll_search);
		mEtSearch = (EditText) llSearch.findViewById(R.id.et_input);
		mEtSearch.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mEtSearch.setFocusable(true);
					mEtSearch.setFocusableInTouchMode(true);
				}
					
				return false;
			}
		});
		
		final View ivDelete = llSearch.findViewById(R.id.iv_delete);
		ivDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mEtSearch.getText().clear();
			}
		});
		mEtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence cs, int start, int count, int after) {
			}
			
			@Override
			public void onTextChanged(CharSequence cs, int start, int before, int count) {
			}
			
			@Override
			public void afterTextChanged(final Editable editable) {
				if (editable.length() > 0) {
					ivDelete.setVisibility(View.VISIBLE);
					
					synchronized (mEtSearch) {
						mSearchRun = new Runnable() {
							public void run() {
								List<Leaf> list = Tree.getLeaves(Tree.findDirect(mNode.direct.getPath()));
								final List<Leaf> rst = new ArrayList<Leaf>();
								String input = editable.toString().toLowerCase(Setting.LOCALE);
								long time = SystemClock.uptimeMillis();
								
								for (Leaf leaf : list) {
									if (leaf.getFile().getName().toLowerCase(Setting.LOCALE)
											.contains(input)) {
										rst.add(leaf);
									}
									
									long now = SystemClock.uptimeMillis();
									if (now > time + 200) {
										time = now;
										
										synchronized (mEtSearch) {
											if (mSearchRun == this) {
												AppUtil.runOnUiThread(new Runnable() {
													public void run() {
														showSearchResult(rst.toArray(new Leaf[] {}));
													}
												});
											} else {
												return;
											}
										}
									}
								}
								
								synchronized (mEtSearch) {
									if (mSearchRun == this) {
										AppUtil.runOnUiThread(new Runnable() {
											public void run() {
												showSearchResult(rst.toArray(new Leaf[] {}));
											}
										});
									}
								}
							}
						};
						AppUtil.runOnNewThread(mSearchRun);
					}
					
				} else {
					synchronized (mEtSearch) {
						mSearchRun = null;
					}
					
					ivDelete.setVisibility(View.GONE);
					
					mDirectAdapter.setData(mNode.direct.getChildren());
					mDirectAdapter.notifyDataSetChanged();
				}
			}
		});
		
		// 文件列表
		mDirectAdapter = new DirectAdapter(this);
		mGvList = (GridView) findViewById(R.id.gv_list);
		mGvList.setAdapter(mDirectAdapter);
		mGvList.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View view, int left, int top, int right, int bottom,
				int ol, int ot, int or, int ob) {
				
				if (ob != 0 && ob < bottom) {
					mEtSearch.setFocusable(false);
					mEtSearch.setFocusableInTouchMode(false);
				}
			}
		});
		mGvList.setOnScrollListener(new OnScrollListener() {
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

		String key = Setting.getListStyle();
		ListStyle ls = SettingListStyleActivity.getListStyle(key);
		mGvList.setNumColumns(ls.column);
		
		showDirect(new Node(direct), false);
	}
	
	public void showDirect(Node node, boolean lastToHistory) {
		// 合法性
		try {
			File file = node.direct.getFile();
			if (file.exists() == false || file.isDirectory() == false || file.list() == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), R.string.err_file_read_error,
				Toast.LENGTH_SHORT).show();
			
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
			if (mHistory.size() > 0) {
				Node n = mHistory.get(0);
				if (n.direct.getPath().equals(mNode.direct.getPath()) == false) {
					mHistory.add(mNode);
				}
			} else {
				mHistory.add(mNode);
			}
		}
		
		// 关闭搜索
		synchronized (mEtSearch) {
			mSearchRun = null;
		}
		
		// 更新路径
		mNode = node;
		
		String path = node.direct.getPath();
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
					
					showDirect(new Node(new Direct(sb.toString())), true);
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
		
		// 更新文件列表
		node.direct.loadChilren();
		mDirectAdapter.setData(node.direct.getChildren());
		mDirectAdapter.notifyDataSetChanged();
		
		final int POSITION = node.position;
		AppUtil.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mGvList.setSelection(POSITION);
			}
		});
	}
	
	public void refresh() {
		// 合法性
		try {
			File file = mNode.direct.getFile();
			if (file.exists() == false || file.isDirectory() == false || file.list() == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), R.string.err_file_read_error,
				Toast.LENGTH_SHORT).show();
			
			if (mHistory.size() > 0) {
				mNode = mHistory.remove(mHistory.size() - 1);
			} else {
				mNode = new Node(new Direct(Setting.DEFAULT_PATH));
			}
		}
		
		// 更新文件列表
		mNode.direct.loadChilren();
		mDirectAdapter.setData(mNode.direct.getChildren());
		mDirectAdapter.notifyDataSetChanged();
	}
	
	public void showSearchResult(Leaf[] list) {
		mDirectAdapter.setData(list);
		mDirectAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		refresh();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			int size = mHistory.size();
			if (size > 0) {
				showDirect(mHistory.remove(size - 1), false);
				return true;
			}
		}
		
		return super.onKeyUp(keyCode, event);
	}
}
