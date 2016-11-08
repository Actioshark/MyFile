package kk.myfile.activity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import kk.myfile.R;
import kk.myfile.activity.SettingListStyleActivity.ListStyle;
import kk.myfile.adapter.DirectAdapter;
import kk.myfile.adapter.DownListAdapter.DataItem;
import kk.myfile.file.ClipBoard;
import kk.myfile.file.FileUtil;
import kk.myfile.file.ClipBoard.ClipType;
import kk.myfile.file.Tree;
import kk.myfile.file.Tree.IProgressCallback;
import kk.myfile.file.Tree.ProgressType;
import kk.myfile.leaf.Audio;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Image;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.TempDirect;
import kk.myfile.leaf.Text;
import kk.myfile.leaf.Video;
import kk.myfile.ui.DownList;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.SimpleDialog;
import kk.myfile.util.AppUtil;
import kk.myfile.util.DataUtil;
import kk.myfile.util.IntentUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.MathUtil;
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
import android.view.View.OnLongClickListener;
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
	public static final String KEY_CUR_CHILD = "direct_cur_child";

	public static class Node {
		public Direct direct;
		public int position = 0;

		public Node(Direct direct) {
			this.direct = direct;
		}
	}

	private Node mNode;
	private final List<Node> mHistory = new ArrayList<Node>();
	private String mPath;
	private String mCurChild;

	private Mode mMode = Mode.Normal;

	private View mLlPath;
	private HorizontalScrollView mHsvPath;
	private ViewGroup mVgPath;

	private View mRlTitle;
	private TextView mTvTitle;
	private ImageView mIvSelect;

	private EditText mEtSearch;
	private ImageView mIvDelete;
	private Runnable mSearchRun;
	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence cs, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence cs, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable editable) {
			final String key = editable.toString();

			if (key.length() > 0) {
				mIvDelete.setVisibility(View.VISIBLE);

				synchronized (mEtSearch) {
					mSearchRun = new Runnable() {
						public void run() {
							AtomicBoolean finish = new AtomicBoolean(false);
							List<Leaf> direct = Tree.getDirect(mNode.direct.getPath(), finish);
							final Runnable mark = this;

							while (true) {
								if (direct.size() > 0) {
									final List<Leaf> ret = Tree.search(direct, key);

									synchronized (mEtSearch) {
										if (mSearchRun != mark) {
											return;
										}

										AppUtil.runOnUiThread(new Runnable() {
											public void run() {
												synchronized (mEtSearch) {
													if (mSearchRun == mark) {
														showSearchResult(ret);
													}
												}
											}
										});
									}

									if (finish.get()) {
										return;
									}
								}

								SystemClock.sleep(300);
							}
						}
					};
					AppUtil.runOnNewThread(mSearchRun);
				}
			} else {
				mIvDelete.setVisibility(View.GONE);

				if (mNode.direct instanceof TempDirect) {
					backDirect();
				}

				synchronized (mEtSearch) {
					mSearchRun = null;
				}
			}
		}
	};

	private GridView mGvList;
	private DirectAdapter mDirectAdapter;

	private View mLlDetail;
	private ImageView mIvDetailIcon;
	private TextView mTvDetailName;
	private TextView mTvDetailTime;
	private TextView mTvDetailSize;
	private long mDetailShowPoint = 0;
	private final Runnable mDetailRun = new Runnable() {
		@Override
		public void run() {
			long duration = SystemClock.elapsedRealtime() - mDetailShowPoint - 2000;

			if (duration > 1000) {
				mLlDetail.setAlpha(0);
				AppUtil.removeUiThread(mDetailMark);
			} else {
				mLlDetail.setAlpha(1f - duration / 1000f);
			}
		}
	};
	private Runnable mDetailMark;

	private View mLlInfo;
	private TextView mTvInfoCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 数据
		mPath = getIntent().getStringExtra(KEY_PATH);
		mCurChild = getIntent().getStringExtra(KEY_CUR_CHILD);

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
		mRlTitle.findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				setMode(Mode.Normal);
			}
		});
		mIvSelect = (ImageView) mRlTitle.findViewById(R.id.iv_select);
		mIvSelect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mDirectAdapter.selectAll(mDirectAdapter.getSelectedCount() < mDirectAdapter
					.getCount());
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

					synchronized (mEtSearch) {
						if (mSearchRun == null && mEtSearch.getText().length() > 0) {
							mTextWatcher.afterTextChanged(mEtSearch.getText());
						}
					}
				}

				return false;
			}
		});

		mIvDelete = (ImageView) llSearch.findViewById(R.id.iv_delete);
		mIvDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mEtSearch.getText().clear();
			}
		});
		mEtSearch.addTextChangedListener(mTextWatcher);

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

		// 详情
		mLlDetail = findViewById(R.id.ll_detail);
		mIvDetailIcon = (ImageView) mLlDetail.findViewById(R.id.iv_icon);
		mTvDetailName = (TextView) mLlDetail.findViewById(R.id.tv_name);
		mTvDetailTime = (TextView) mLlDetail.findViewById(R.id.tv_time);
		mTvDetailSize = (TextView) mLlDetail.findViewById(R.id.tv_size);

		// 信息
		mLlInfo = findViewById(R.id.ll_info);
		mTvInfoCount = (TextView) mLlInfo.findViewById(R.id.tv_count);
		mLlInfo.findViewById(R.id.iv_menu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showMenu();
			}
		});
		mLlInfo.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View view, int left, int top, int right, int bottom, int ol,
				int ot, int or, int ob) {

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
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mNode == null) {
			changeDirect(new Node(new Direct(mPath)), false);
		} else {
			refreshDirect();
		}
	}

	public Mode getMode() {
		return mMode;
	}

	public void setMode(Mode mode) {
		if (mMode != mode) {
			mMode = mode;

			updateTitle();
			updateInfo();

			if (mode != Mode.Select) {
				mDirectAdapter.selectAll(false);
			}
			mDirectAdapter.notifyDataSetChanged();
		}
	}

	public void changeDirect(Node node, boolean lastToHistory) {
		updateStyle();

		if (node.direct instanceof TempDirect == false && mNode != null
			&& node.direct.getPath().equals(mNode.direct.getPath())) {

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

					changeDirect(new Node(new Direct(sb.toString())), true);
				}
			});
			grid.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					StringBuilder sb = new StringBuilder();

					for (int i = 1; i <= index; i++) {
						sb.append('/').append(nodes[i]);
					}
					
					ClipBoard.put(DirectActivity.this, "path", sb.toString());
					
					App.showToast(R.string.msg_already_copied_to_clipboard);
					return true;
				}
			});

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
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
		final Direct direct = node.direct;
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				direct.loadChildren(false);
			}
		});
		mDirectAdapter.setData(node.direct.getChildren(), node.position);

		// 信息
		updateInfo();
	}

	public void refreshDirect() {
		// 主题样式
		updateStyle();

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
		final Direct direct = mNode.direct;
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				direct.loadChildren(false);
			}
		});
		mDirectAdapter.setData(mNode.direct.getChildren(), -1);

		// 信息
		updateInfo();
	}

	public boolean backDirect() {
		if (mHistory.size() > 0) {
			Node node = mHistory.remove(mHistory.size() - 1);
			changeDirect(node, false);
			return true;
		}

		return false;
	}

	private void updateStyle() {
		String key = Setting.getListStyle(Classify.Direct);
		ListStyle ls = SettingListStyleActivity.getListStyle(key);
		mGvList.setNumColumns(ls.column);
		mGvList.setVerticalSpacing(ls.vertSpace);
	}

	public void updateTitle() {
		if (mMode == Mode.Select) {
			mTvTitle.setText(R.string.msg_multi_select_mode);
			mIvSelect.setImageResource(mDirectAdapter.getSelectedCount() < mDirectAdapter
				.getCount() ? R.drawable.multi_select_pre : R.drawable.multi_select_nor);
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
		changeDirect(new Node(direct), true);
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
			mTvDetailSize.setText(String.format(Setting.LOCALE, "%s B", MathUtil.insertComma(file
				.length())));
		}

		mDetailShowPoint = SystemClock.elapsedRealtime();

		AppUtil.removeUiThread(mDetailMark);
		mLlDetail.setAlpha(1f);
		mDetailMark = AppUtil.runOnUiThread(mDetailRun, 2000, 20);
	}

	public void updateInfo() {
		if (mMode == Mode.Select) {
			mTvInfoCount.setText(AppUtil.getString(R.string.msg_children_select_with_num,
				mDirectAdapter.getSelectedCount(), mNode.direct.getChildren().size()));
		} else {
			mTvInfoCount.setText(AppUtil.getString(R.string.msg_children_with_num, mNode.direct
				.getChildren().size()));
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

			final Leaf first = selected.get(0);

			boolean hasDirect = false;
			for (Leaf leaf : selected) {
				if (leaf instanceof Direct) {
					hasDirect = true;
					break;
				}
			}

			if (hasDirect == false) {
				list.add(new DataItem(R.drawable.share, R.string.word_share,
					new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int index) {
							if (IntentUtil.share(DirectActivity.this, mDirectAdapter.getSelected(),
								null)) {
								setMode(Mode.Normal);
							} else {
								App.showToast(R.string.err_share_failed);
							}
						}
					}, new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int index) {
							AppUtil.runOnNewThread(new Runnable() {
								@Override
								public void run() {
									for (Leaf leaf : selected) {
										try {
											String path = leaf.getPath();
											if (path.endsWith(".xor")) {
												path = path.substring(0, path.length() - 4);
											} else {
												path = path + ".xor";
											}
											File to = new File(path);

											String ret = FileUtil.createFile(to);
											if (ret != null) {
												App.showToast(ret);
												continue;
											}

											File from = leaf.getFile();

											boolean suc = FileUtil.write(from, to, 0xff);
											if (suc == false) {
												App.showToast(R.string.err_file_read_error);
												continue;
											}

											ret = FileUtil.delete(from);
											if (ret != null) {
												App.showToast(ret);
												continue;
											}
										} catch (Exception e) {
											Logger.print(null, e);
										}
									}

									AppUtil.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											setMode(Mode.Normal);
											refreshDirect();
										}
									});
								}
							});
						}
					}));
			}

			list.add(new DataItem(R.drawable.detail, R.string.word_detail,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						Intent intent = new Intent(DirectActivity.this, DetailActivity.class);
						intent.putCharSequenceArrayListExtra(DetailActivity.KEY_PATH, DataUtil
							.leaf2PathCs(selected));
						intent.putExtra(DetailActivity.KEY_INDEX, 0);
						startActivity(intent);
					}
				}));

			if (selected.size() == 1) {
				list.add(new DataItem(R.drawable.arrow_up, R.string.word_open_as,
					new IDialogClickListener() {
						@Override
						public void onClick(Dialog dl, int index) {
							SimpleDialog dialog = new SimpleDialog(DirectActivity.this);
							dialog.setCanceledOnTouchOutside(true);
							dialog.setMessage(R.string.msg_open_as);
							dialog.setButtons(R.string.type_text, R.string.type_image,
								R.string.type_audio, R.string.type_video, R.string.word_any);
							dialog.setClickListener(new IDialogClickListener() {
								@Override
								public void onClick(Dialog dialog, int index) {
									switch (index) {
									case 0:
										IntentUtil.view(DirectActivity.this, first, Text.TYPE);
										break;

									case 1:
										IntentUtil.view(DirectActivity.this, first, Image.TYPE);
										break;

									case 2:
										IntentUtil.view(DirectActivity.this, first, Audio.TYPE);
										break;

									case 3:
										IntentUtil.view(DirectActivity.this, first, Video.TYPE);
										break;

									case 4:
										IntentUtil.view(DirectActivity.this, first, "*/*");
										break;
									}

									dialog.dismiss();
								}
							});
							dialog.show();
						}
					}));

				list.add(new DataItem(R.drawable.edit, R.string.word_rename,
					new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int index) {
							Tree.rename(DirectActivity.this, first.getFile(),
								new IProgressCallback() {
									@Override
									public void onProgress(ProgressType type) {
										setMode(Mode.Normal);
										refreshDirect();
									}
								});
						}
					}));
			}

			if (hasDirect == false && selected.size() == 1) {
				list.add(new DataItem(R.drawable.edit, R.string.word_edit,
					new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int index) {
							if (IntentUtil.edit(DirectActivity.this, first, null)) {
								setMode(Mode.Normal);
							} else {
								SimpleDialog st = new SimpleDialog(DirectActivity.this);
								st.setCanceledOnTouchOutside(true);
								st.setMessage(R.string.msg_edit_as);
								st.setButtons(R.string.type_text, R.string.type_image,
									R.string.word_any);
								st.setClickListener(new IDialogClickListener() {
									@Override
									public void onClick(Dialog dialog, int index) {
										switch (index) {
										case 0:
											IntentUtil.edit(DirectActivity.this, first, Text.TYPE);
											break;

										case 1:
											IntentUtil.edit(DirectActivity.this, first, Image.TYPE);
											break;

										case 2:
											IntentUtil.edit(DirectActivity.this, first, "*/*");
											break;
										}

										dialog.dismiss();
									}
								});
								st.show();
							}
						}
					}));
			}

			list.add(new DataItem(R.drawable.cross, R.string.word_delete,
				new IDialogClickListener() {
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

			list.add(new DataItem(R.drawable.copy, R.string.word_copy_or_to, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					if (ClipBoard.put(DirectActivity.this, ClipType.Copy, selected)) {
						setMode(Mode.Normal);
						App.showToast(R.string.msg_enter_target_direct_and_paste);
					} else {
						App.showToast(R.string.err_nothing_selected);
					}
				}
			}, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					Intent intent = new Intent(DirectActivity.this, SelectActivity.class);
					startActivityForResult(intent, REQ_COPY_TO);
				}
			}));

			list.add(new DataItem(R.drawable.cut, R.string.word_cut_or_to, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					if (ClipBoard.put(DirectActivity.this, ClipType.Cut, selected)) {
						setMode(Mode.Normal);
						App.showToast(R.string.msg_enter_target_direct_and_paste);
					} else {
						App.showToast(R.string.err_nothing_selected);
					}
				}
			}, new IDialogClickListener() {
				@Override
				public void onClick(Dialog dialog, int index) {
					Intent intent = new Intent(DirectActivity.this, SelectActivity.class);
					startActivityForResult(intent, REQ_CUT_TO);
				}
			}));

			dl.show();
		} else {
			if (ClipBoard.hasFile(DirectActivity.this)) {
				list.add(new DataItem(R.drawable.paste, R.string.word_paste,
					new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int index) {
							List<String> fl = ClipBoard.getFiles(DirectActivity.this);

							if (fl.size() > 0) {
								boolean delete = ClipBoard.getType(DirectActivity.this) == ClipType.Cut;

								Tree.carry(DirectActivity.this, fl, mNode.direct.getPath(), delete,
									new IProgressCallback() {

										@Override
										public void onProgress(ProgressType type) {
											refreshDirect();
										}
									});
							} else {
								App.showToast(R.string.err_nothing_selected);
							}
						}
					}));
			}

			list.add(new DataItem(R.drawable.refresh, R.string.word_refresh,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						refreshDirect();
					}
				}));

			list.add(new DataItem(R.drawable.add, R.string.word_new_direct,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						Tree.createDirect(DirectActivity.this, mNode.direct.getPath(),
							new IProgressCallback() {
								@Override
								public void onProgress(ProgressType type) {
									refreshDirect();
								}
							});
					}
				}));

			list.add(new DataItem(R.drawable.add, R.string.word_new_file,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						Tree.createFile(DirectActivity.this, mNode.direct.getPath(),
							new IProgressCallback() {
								@Override
								public void onProgress(ProgressType type) {
									refreshDirect();
								}
							});
					}
				}));

			list.add(new DataItem(R.drawable.multi_select_pre, R.string.word_multi_select,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						setMode(Mode.Select);
					}
				}));

			list.add(new DataItem(R.drawable.setting, R.string.word_setting,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index) {
						Intent intent = new Intent(DirectActivity.this, SettingActivity.class);
						startActivity(intent);
					}
				}));
		}

		dl.show(DownList.POS_END, DownList.POS_END, 0, mLlInfo.getHeight());
	}

	public void setSelection(int position) {
		if (mCurChild != null) {
			List<Leaf> children = mNode.direct.getChildren();
			synchronized (children) {
				int size = children.size();

				for (int i = 0; i < size; i++) {
					if (mCurChild.equals(children.get(i).getPath())) {
						position = i;
						break;
					}
				}
			}

			mCurChild = null;
		}

		if (position >= 0 && position < mDirectAdapter.getCount()) {
			mGvList.setSelection(position);
		}
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

			Tree.carry(this, DataUtil.leaf2PathString(mDirectAdapter.getSelected()), path, false,
				new IProgressCallback() {
					@Override
					public void onProgress(ProgressType type) {
						setMode(Mode.Normal);
						refreshDirect();
					}
				});
		} else if (requestCode == REQ_CUT_TO) {
			if (resultCode != RESULT_OK || data == null) {
				return;
			}

			String path = data.getStringExtra(SelectActivity.KEY_PATH);

			Tree.carry(this, DataUtil.leaf2PathString(mDirectAdapter.getSelected()), path, true,
				new IProgressCallback() {
					@Override
					public void onProgress(ProgressType type) {
						setMode(Mode.Normal);
						refreshDirect();
					}
				});
		}
	}
}
