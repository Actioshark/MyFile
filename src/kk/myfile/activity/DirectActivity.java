package kk.myfile.activity;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.activity.SettingListStyleActivity.ListStyle;
import kk.myfile.adapter.DirectAdapter;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.TempDirect;
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
	
	private View mViewInfoNormal;
	private TextView mTvInfoCount;
	private View mViewInfoSelect;
	private TextView mTvInfoName;
	private TextView mTvInfoTime;
	private TextView mTvInfoSize;
	
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
					
					if (mNode.direct instanceof TempDirect) {
						backDirect();
						showSearchResult(mNode.direct.getChildren());
					} else {
						refreshDirect();
					}
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
				
				if (ob != 0) {
					if (ob < bottom) {
						mEtSearch.setFocusable(false);
						mEtSearch.setFocusableInTouchMode(false);
					} else if (ob > bottom) {
						showSearchResult(mNode.direct.getChildren());
					}
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
		mGvList.setVerticalSpacing(ls.space);
		
		// 信息栏
		View llInfo = findViewById(R.id.fl_info);
		llInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showInfo(null);
			}
		});
		
		mViewInfoNormal = llInfo.findViewById(R.id.ll_normal);
		mTvInfoCount = (TextView) mViewInfoNormal.findViewById(R.id.tv_count);
		mViewInfoNormal.findViewById(R.id.iv_menu)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			}
		});
		mViewInfoSelect = llInfo.findViewById(R.id.ll_select);
		mTvInfoName = (TextView) mViewInfoSelect.findViewById(R.id.tv_name);
		mTvInfoTime = (TextView) mViewInfoSelect.findViewById(R.id.tv_time);
		mTvInfoSize = (TextView) mViewInfoSelect.findViewById(R.id.tv_size);
		
		// 开始
		showDirect(new Node(direct), false);
	}
	
	public void showDirect(Node node, boolean lastToHistory) {
		if (node.direct instanceof TempDirect == false && mNode != null &&
				node.direct.getPath().equals(mNode.direct.getPath())) {
			
			refreshDirect();
			return;
		}
		
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
			int size = mHistory.size();
			if (size > 0) {
				Node n = mHistory.get(size - 1);
				if (mNode.direct.getPath().equals(n.direct.getPath()) == false) {
					mHistory.add(mNode);
				} else if((mNode.direct instanceof TempDirect) != (n.direct instanceof TempDirect)) {
					mHistory.add(mNode);
				}  else if((mNode.direct instanceof TempDirect) && (n.direct instanceof TempDirect)) {
					mHistory.set(size - 1, mNode);
				}
			} else {
				mHistory.add(mNode);
			}
		}
		
		// 关闭搜索
		if (node.direct instanceof TempDirect == false) {
			synchronized (mEtSearch) {
				mSearchRun = null;
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
		node.direct.loadChildrenAll();
		mDirectAdapter.setData(node.direct.getChildren());
		
		final int POSITION = node.position;
		AppUtil.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mGvList.setSelection(POSITION);
			}
		});
		
		// 信息栏
		showInfo(null);
	}
	
	public void refreshDirect() {
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
		mNode.direct.loadChildrenAll();
		mDirectAdapter.setData(mNode.direct.getChildren());
		
		// 信息栏
		showInfo(null);
	}
	
	public boolean backDirect() {
		while (mHistory.size() > 0) {
			Node node = mHistory.remove(mHistory.size() - 1);
			if (node.direct instanceof TempDirect == false) {
				showDirect(node, false);
				return true;
			}
		}
		
		return false;
	}
	
	public void showSearchResult(Leaf[] list) {
		TempDirect direct = new TempDirect(mNode.direct.getPath());
		direct.setChildren(list);
		showDirect(new Node(direct), true);
	}
	
	public void showInfo(Leaf leaf) {
		if (leaf == null) {
			mTvInfoCount.setText(AppUtil.getString(R.string.hint_children_with_num, mNode.direct.getChildren().length));
			mViewInfoNormal.setVisibility(View.VISIBLE);
			mViewInfoSelect.setVisibility(View.GONE);
		} else {
			File file = leaf.getFile();
			
			mTvInfoName.setText(file.getName());
			
			Date date = new Date(file.lastModified());
			DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss", Setting.LOCALE);
			mTvInfoTime.setText(df.format(date));
			
			if (leaf instanceof Direct) {
				mTvInfoSize.setText(AppUtil.getString(R.string.hint_children_with_num,
						file.list().length));
			} else {
				String num = String.valueOf(file.length());
				StringBuilder sb = new StringBuilder();
				int len = num.length();
				for (int i = 0; i < len; i++) {
					sb.append(num.charAt(i));
	
					if (i + 1 != len && (len - i) % 3 == 1) {
						sb.append(',');
					}
				}
				mTvInfoSize.setText(String.format(Setting.LOCALE, "%s B", sb.toString()));
			}
			
			mViewInfoNormal.setVisibility(View.GONE);
			mViewInfoSelect.setVisibility(View.VISIBLE);
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
