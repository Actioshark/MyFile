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
import kk.myfile.adapter.DownListAdapter.DataItem;
import kk.myfile.file.ClipPad;
import kk.myfile.file.Tree;
import kk.myfile.file.Tree.IProgressCallback;
import kk.myfile.file.Tree.ProgressType;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.TempDirect;
import kk.myfile.ui.DownList;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Setting;
import android.app.Dialog;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DirectActivity extends BaseActivity {
	public static final String KEY_PATH = "direct_path";
	
	public static final int REQ_COPY_TO = 1;
	public static final int REQ_CUT_TO = 2;
	
	public static class Node {
		public Direct direct;
		public int position = 0;
		
		public Node(Direct direct) {
			this.direct = direct;
		}
	}
	private Node mNode;
	private final List<Node> mHistory = new ArrayList<Node>();
	
	public static enum Mode {
		Normal, Select,
	}
	private Mode mMode = Mode.Normal;
	
	private View mLlPath;
	private HorizontalScrollView mHsvPath;
	private ViewGroup mVgPath;
	
	private View mRlTitle;
	private TextView mTvTitle;
	private ImageView mIvSelect;
	
	private EditText mEtSearch;
	private Runnable mSearchRun;
	
	private GridView mGvList;
	private DirectAdapter mDirectAdapter;
	
	private View mLlDetail;
	private ImageView mIvDetailIcon;
	private TextView mTvDetailName;
	private TextView mTvDetailTime;
	private TextView mTvDetailSize;
	private long mDetailShowPoint = 0;
	private Runnable mDetailRun = new Runnable() {
		@Override
		public void run() {
			long duration = SystemClock.elapsedRealtime() - mDetailShowPoint - 2000;
			
			if (duration > 1000) {
				mLlDetail.setAlpha(0);
			} else if (duration < 0) {
				mLlDetail.setAlpha(1);
			} else {
				mLlDetail.setAlpha(1 - duration / 1000f);
			}
		}
	};
	
	private View mLlInfo;
	private TextView mTvInfoCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 数据
		String path = getIntent().getStringExtra(KEY_PATH);
		Direct direct = new Direct(path);
		
		setContentView(R.layout.activity_direct);
		
		// 路径栏
		mLlPath = findViewById(R.id.ll_path);
		View ivHome = mLlPath.findViewById(R.id.iv_home);
		ivHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		mHsvPath = (HorizontalScrollView) mLlPath.findViewById(R.id.hsv_text);
		mVgPath = (ViewGroup) mHsvPath.findViewById(R.id.ll_text);
		
		// 标题
		mRlTitle = findViewById(R.id.rl_title);
		mTvTitle = (TextView) mRlTitle.findViewById(R.id.tv_title);
		mRlTitle.findViewById(R.id.iv_back).
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setMode(Mode.Normal);
			}
		});
		mIvSelect = (ImageView) mRlTitle.findViewById(R.id.iv_select);
		mIvSelect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mDirectAdapter.selectAll(mDirectAdapter.getSelectedCount() < mDirectAdapter.getCount());
			}
		});
		
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
								Direct direct = Tree.load(mNode.direct.getPath());
								String input = mEtSearch.getText().toString();
								
								while (true) {
									boolean finished = direct.getTag() == null;
									final List<Leaf> ret = Tree.search(direct, input);
									
									synchronized (mEtSearch) {
										if (mSearchRun == this) {
											AppUtil.runOnUiThread(new Runnable() {
												public void run() {
													showSearchResult(ret);
												}
											});
										} else {
											return;
										}
									}
									
									if (finished) {
										return;
									}
									
									SystemClock.sleep(1000);
								}
							}
						};
						AppUtil.runOnNewThread(mSearchRun);
					}
				} else {
					ivDelete.setVisibility(View.GONE);
					
					if (mNode.direct instanceof TempDirect) {
						backDirect();
					}
					
					synchronized (mEtSearch) {
						mSearchRun = null;
					}
				}
			}
		});
		
		// 文件列表
		mDirectAdapter = new DirectAdapter(this);
		mGvList = (GridView) findViewById(R.id.gv_list);
		mGvList.setAdapter(mDirectAdapter);
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
		mGvList.setVerticalSpacing(ls.vertSpace);
		
		// 详情
		mLlDetail = findViewById(R.id.ll_detail);
		mIvDetailIcon = (ImageView) mLlDetail.findViewById(R.id.iv_icon);
		mTvDetailName = (TextView) mLlDetail.findViewById(R.id.tv_name);
		mTvDetailTime = (TextView) mLlDetail.findViewById(R.id.tv_time);
		mTvDetailSize = (TextView) mLlDetail.findViewById(R.id.tv_size);
		
		// 信息
		mLlInfo = findViewById(R.id.ll_info);
		mTvInfoCount = (TextView) mLlInfo.findViewById(R.id.tv_count);
		mLlInfo.findViewById(R.id.iv_menu)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showMenu();
			}
		});
		mLlInfo.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View view, int left, int top, int right, int bottom,
				int ol, int ot, int or, int ob) {
				
				if (ob < bottom) {
					mEtSearch.setFocusable(false);
					mEtSearch.setFocusableInTouchMode(false);
				} else if (ob > bottom) {
					if (mEtSearch.hasFocus()) {
						showSearchResult(mNode.direct.getChildren());
					}
				}
			}
		});
		
		// 开始
		showDirect(new Node(direct), false);
	}
	
	public Mode getMode() {
		return mMode;
	}
	
	public void setMode(Mode mode) {
		if (mMode != mode) {
			mMode = mode;
			
			showTitle();
			showInfo();
			mDirectAdapter.notifyDataSetChanged();
		}
	}
	
	public void showDirect(Node node, boolean lastToHistory) {
		if (node.direct instanceof TempDirect == false && mNode != null &&
				node.direct.getPath().equals(mNode.direct.getPath())) {
			
			mNode = node;
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
			App.showToast(R.string.err_file_read_error);
			
			if (mNode != null) {
				return;
			} else if (mHistory.size() > 0) {
				node = mHistory.remove(mHistory.size() - 1);
			} else {
				node = new Node(new Direct(Setting.DEFAULT_PATH));
			}
		}
		
		// 加入历史
		if (lastToHistory && mNode != null && mNode.direct instanceof TempDirect == false) {
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
		
		mLlPath.setVisibility(View.VISIBLE);
		
		AppUtil.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mHsvPath.fullScroll(View.FOCUS_RIGHT);
			}
		});
		
		// 标题
		mRlTitle.setVisibility(View.GONE);
		
		// 关闭搜索
		if (node.direct instanceof TempDirect == false) {
			synchronized (mEtSearch) {
				mSearchRun = null;
			}
		}
		
		// 更新文件列表
		node.direct.loadChildrenAll();
		mDirectAdapter.setData(node.direct.getChildren(), node.position);
		
		// 信息
		showInfo();
	}
	
	public void refreshDirect() {
		// 合法性
		try {
			File file = mNode.direct.getFile();
			if (file.exists() == false || file.isDirectory() == false || file.list() == null) {
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
		mNode.direct.loadChildrenAll();
		mDirectAdapter.setData(mNode.direct.getChildren(), -1);
		
		// 信息
		showInfo();
	}
	
	public boolean backDirect() {
		if (mHistory.size() > 0) {
			Node node = mHistory.remove(mHistory.size() - 1);
			showDirect(node, false);
			return true;
		}
		
		return false;
	}
	
	public void showTitle() {
		if (mMode == Mode.Select) {
			mTvTitle.setText(R.string.msg_multi_select_mode);
			mIvSelect.setImageResource(mDirectAdapter.getSelectedCount() < mDirectAdapter.getCount()
					? R.drawable.multi_select_pre : R.drawable.multi_select_nor);
			mLlPath.setVisibility(View.GONE);
			mRlTitle.setVisibility(View.VISIBLE);
		} else {
			mLlPath.setVisibility(View.VISIBLE);
			mRlTitle.setVisibility(View.GONE);
		}
	}
	
	public void showSearchResult(List<Leaf> list) {
		TempDirect direct = new TempDirect(mNode.direct.getPath());
		direct.setChildren(list);
		showDirect(new Node(direct), true);
	}
	
	public void showDetail(Leaf leaf) {
		File file = leaf.getFile();
		
		mIvDetailIcon.setImageResource(leaf.getIcon());
		
		mTvDetailName.setText(file.getName());
		
		Date date = new Date(file.lastModified());
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss", Setting.LOCALE);
		mTvDetailTime.setText(df.format(date));
		
		if (leaf instanceof Direct) {
			String[] children = file.list();
			mTvDetailSize.setText(AppUtil.getString(R.string.msg_children_with_num,
					children == null ? 0 : children.length));
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
			mTvDetailSize.setText(String.format(Setting.LOCALE, "%s B", sb.toString()));
		}
		
		mDetailShowPoint = SystemClock.elapsedRealtime();
	}
	
	public void showInfo() {
		if (mMode == Mode.Select) {
			mTvInfoCount.setText(AppUtil.getString(R.string.msg_children_select_with_num,
				mDirectAdapter.getSelectedCount(), mNode.direct.getChildren().size()));
		} else {
			mTvInfoCount.setText(AppUtil.getString(R.string.msg_children_with_num,
					mNode.direct.getChildren().size()));
		}
	}
	
	public void showMenu() {
		DownList dl = new DownList(this);
		List<DataItem> list = new ArrayList<DataItem>();
		dl.getAdapter().setDataList(list);
		
		if (mMode == Mode.Select) {
			final List<Leaf> selected = mDirectAdapter.getSelected();
			if (selected.size() < 1) {
				App.showToast(R.string.err_nothing_selected);
				return;
			}
			
			if (mDirectAdapter.getSelectedCount() == 1) {
				list.add(new DataItem(R.drawable.edit, R.string.word_rename, new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						Tree.rename(DirectActivity.this, mDirectAdapter.getSelected().get(0).getFile(),
							new IProgressCallback() {
								@Override
								public void onProgress(ProgressType type) {
									setMode(Mode.Normal);
									refreshDirect();
								}
							}
						);
					}
				}));
			}
			
			list.add(new DataItem(R.drawable.cross, R.string.word_delete, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					Tree.delete(DirectActivity.this, selected, new IProgressCallback() {
						@Override
						public void onProgress(ProgressType type) {
							setMode(Mode.Normal);
							refreshDirect();
						}
					});
				}
			}));
			
			list.add(new DataItem(R.drawable.copy, R.string.word_copy_to, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					Intent intent = new Intent(DirectActivity.this, SelectActivity.class);
					startActivityForResult(intent, REQ_COPY_TO);
				}
			}));
			
			list.add(new DataItem(R.drawable.copy, R.string.word_copy, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					ClipPad.setClip(ClipPad.Mode.Copy, mDirectAdapter.getSelected());
				}
			}));
			
			list.add(new DataItem(R.drawable.cut, R.string.word_cut_to, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					Intent intent = new Intent(DirectActivity.this, SelectActivity.class);
					startActivityForResult(intent, REQ_CUT_TO);
				}
			}));
			
			list.add(new DataItem(R.drawable.cut, R.string.word_cut, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					ClipPad.setClip(ClipPad.Mode.Cut, mDirectAdapter.getSelected());
				}
			}));
			
			dl.show();
		} else {
			list.add(new DataItem(R.drawable.refresh, R.string.word_refresh, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					refreshDirect();
				}
			}));
			
			list.add(new DataItem(R.drawable.add, R.string.word_new_direct, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					Tree.createDirect(DirectActivity.this, mNode.direct.getPath(), new IProgressCallback() {
						@Override
						public void onProgress(ProgressType type) {
							refreshDirect();
						}
					});
				}
			}));
			
			list.add(new DataItem(R.drawable.add, R.string.word_new_file, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					Tree.createFile(DirectActivity.this, mNode.direct.getPath(), new IProgressCallback() {
						@Override
						public void onProgress(ProgressType type) {
							refreshDirect();
						}
					});
				}
			}));
		}
			
		dl.show(DownList.POS_END, DownList.POS_END, 0, mLlInfo.getHeight());
	}
	
	public void setSelection(int position) {
		if (position >= 0 && position < mDirectAdapter.getCount()) {
			mGvList.setSelection(position);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		refreshDirect();
		
		AppUtil.runOnUiThread(mDetailRun, 20, 20);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		AppUtil.removeUiThread(mDetailRun);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mMode == Mode.Select) {
				setMode(Mode.Normal);
				return true;
			} else if (backDirect()) {
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			showMenu();
			return true;
		}
		
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQ_COPY_TO) {
			if (resultCode != RESULT_OK || data == null) {
				return;
			}
			
			String path = data.getStringExtra(SelectActivity.KEY_PATH);
			
			Tree.carry(this, mDirectAdapter.getSelected(), path, false,
				new IProgressCallback() {
					@Override
					public void onProgress(ProgressType type) {
						setMode(Mode.Normal);
						refreshDirect();
					}
				}
			);
		} else if (requestCode == REQ_CUT_TO) {
			if (resultCode != RESULT_OK || data == null) {
				return;
			}
			
			String path = data.getStringExtra(SelectActivity.KEY_PATH);
			
			Tree.carry(this, mDirectAdapter.getSelected(), path, true,
				new IProgressCallback() {
					@Override
					public void onProgress(ProgressType type) {
						setMode(Mode.Normal);
						refreshDirect();
					}
				}
			);
		}
	}
}
