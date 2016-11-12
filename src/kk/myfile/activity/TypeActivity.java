package kk.myfile.activity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kk.myfile.R;
import kk.myfile.activity.SettingListStyleActivity.ListStyle;
import kk.myfile.adapter.DownListAdapter.DataItem;
import kk.myfile.adapter.TypeAdapter;
import kk.myfile.file.ClipBoard;
import kk.myfile.file.FileUtil;
import kk.myfile.file.Tree;
import kk.myfile.file.ClipBoard.ClipType;
import kk.myfile.file.Tree.IProgressCallback;
import kk.myfile.file.Tree.ProgressType;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Image;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Text;
import kk.myfile.leaf.Zip;
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
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class TypeActivity extends BaseActivity {
	public static final String KEY_TYPE = "type_type";

	private Classify mClassify;
	private Class<?> mType;
	private String mName;

	private Mode mMode;

	private TextView mTvTitle;
	private ImageView mIvSelect;

	private EditText mEtSearch;
	private Object mSearchMark;

	private GridView mGvList;
	private TypeAdapter mTypeAdapter;

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
				AppUtil.removeUiThread(mDetailMark);
			} else {
				mLlDetail.setAlpha(1 - duration / 1000f);
			}
		}
	};
	private Runnable mDetailMark;

	private View mLlInfo;
	private TextView mTvInfoCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			mClassify = Classify.valueOf(getIntent().getStringExtra(KEY_CLASSIFY));
		} catch (Exception e) {
			mClassify = Classify.Type;
		}

		if (mClassify == Classify.Type) {
			try {
				String type = getIntent().getStringExtra(KEY_TYPE);
				mType = Class.forName(type);
				int index = type.lastIndexOf('.');

				mName = AppUtil.getString(String.format("type_%s", type.substring(index + 1)
					.toLowerCase(Setting.LOCALE)));
			} catch (Exception e) {
				finish();
				return;
			}
		} else if (mClassify == Classify.Big) {
			mName = AppUtil.getString(R.string.word_big_file);
		} else if (mClassify == Classify.Recent) {
			mName = AppUtil.getString(R.string.word_recent_file);
		}

		setContentView(R.layout.activity_type);

		// 标题
		View rlTitle = findViewById(R.id.rl_title);
		mTvTitle = (TextView) rlTitle.findViewById(R.id.tv_title);
		rlTitle.findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mMode == Mode.Select) {
					setMode(Mode.Normal);
				} else {
					finish();
				}
			}
		});
		mIvSelect = (ImageView) rlTitle.findViewById(R.id.iv_select);
		mIvSelect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mTypeAdapter.selectAll(mTypeAdapter.getSelectedCount() < mTypeAdapter.getCount());
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
				} else {
					ivDelete.setVisibility(View.GONE);
				}

				synchronized (mGvList) {
					if (Tree.isTypeDirectRefreshing()) {
						return;
					}

					final Object mark = new Object();
					synchronized (mEtSearch) {
						mSearchMark = mark;
					}

					AppUtil.runOnNewThread(new Runnable() {
						public void run() {
							List<Leaf> list;
							if (mClassify == Classify.Big) {
								list = Tree.loadBig(Tree.getTypeDirect(), Setting
									.getNumLimit(Classify.Big));
							} else if (mClassify == Classify.Recent) {
								list = Tree.loadRecent(Tree.getTypeDirect(), Setting
									.getNumLimit(Classify.Recent));
							} else {
								list = Tree.loadType(Tree.getTypeDirect(), mType);
							}

							final List<Leaf> ret = Tree
								.search(list, mEtSearch.getText().toString());

							AppUtil.runOnUiThread(new Runnable() {
								public void run() {
									synchronized (mEtSearch) {
										if (mSearchMark == mark) {
											mTypeAdapter.setData(ret);
										}
									}
								}
							});
						}
					});
				}
			}
		});

		// 文件列表

		mTypeAdapter = new TypeAdapter(this, mClassify);
		mGvList = (GridView) findViewById(R.id.gv_list);
		mGvList.setAdapter(mTypeAdapter);

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
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		refresh(false);
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
				mTypeAdapter.selectAll(false);
			}
			mTypeAdapter.notifyDataSetChanged();
		}
	}

	public void refresh(boolean reload) {
		updateStyle();

		if (reload) {
			Tree.refreshTypeDirect();
		}

		final Object mark = new Object();
		synchronized (mEtSearch) {
			mSearchMark = mark;
		}

		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				while (isFinishing() == false) {
					boolean finished = Tree.isTypeDirectRefreshing() == false;

					List<Leaf> list;
					if (mClassify == Classify.Big) {
						list = Tree
							.loadBig(Tree.getTypeDirect(), Setting.getNumLimit(Classify.Big));
					} else if (mClassify == Classify.Recent) {
						list = Tree.loadRecent(Tree.getTypeDirect(), Setting
							.getNumLimit(Classify.Recent));
					} else {
						list = Tree.loadType(Tree.getTypeDirect(), mType);
					}

					final List<Leaf> ret = Tree.search(list, mEtSearch.getText().toString());

					AppUtil.runOnUiThread(new Runnable() {
						public void run() {
							synchronized (mEtSearch) {
								if (mSearchMark == mark) {
									mTypeAdapter.setData(ret);
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

	private void updateStyle() {
		String key = Setting.getListStyle(mClassify);
		ListStyle ls = SettingListStyleActivity.getListStyle(key);
		mGvList.setNumColumns(ls.column);
		mGvList.setVerticalSpacing(ls.vertSpace);
	}

	public void updateTitle() {
		if (mMode == Mode.Select) {
			mTvTitle.setText(R.string.msg_multi_select_mode);
			mIvSelect
				.setImageResource(mTypeAdapter.getSelectedCount() < mTypeAdapter.getCount() ? R.drawable.multi_select_pre
					: R.drawable.multi_select_nor);
			mIvSelect.setVisibility(View.VISIBLE);
		} else {
			mTvTitle.setText(mName);
			mIvSelect.setVisibility(View.GONE);
		}
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
				mTypeAdapter.getSelectedCount(), mTypeAdapter.getCount()));
		} else {
			mTvInfoCount.setText(AppUtil.getString(R.string.msg_children_with_num, mTypeAdapter
				.getCount()));
		}
	}

	public void showMenu() {
		DownList dl = new DownList(this);
		List<DataItem> list = new ArrayList<DataItem>();
		dl.getAdapter().setDataList(list);

		if (mMode == Mode.Select) {
			final List<Leaf> selected = mTypeAdapter.getSelected();
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
						public void onClick(Dialog dialog, int index, ClickType type) {
							if (type == ClickType.Click) {
								if (IntentUtil.share(TypeActivity.this, mTypeAdapter.getSelected(),
									null)) {
									setMode(Mode.Normal);
								} else {
									App.showToast(R.string.err_share_failed);
								}
							} else if (type == ClickType.LongClick) {

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
									}
								});
							}
						}
					}));
			}

			list.add(new DataItem(R.drawable.detail, R.string.word_detail,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index, ClickType type) {
						Intent intent = new Intent(TypeActivity.this, DetailActivity.class);
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
						public void onClick(Dialog dl, int index, ClickType type) {
							first.open(TypeActivity.this, true);
						}
					}));

				list.add(new DataItem(R.drawable.edit, R.string.word_rename,
					new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int index, ClickType type) {
							Tree.rename(TypeActivity.this, first.getFile(),
								new IProgressCallback() {
									@Override
									public void onProgress(ProgressType type, Object... data) {
										setMode(Mode.Normal);
										refresh(true);
									}
								});
						}
					}));
			}

			if (hasDirect == false && selected.size() == 1) {
				list.add(new DataItem(R.drawable.edit, R.string.word_edit,
					new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int index, ClickType type) {
							if (IntentUtil.edit(TypeActivity.this, first, null)) {
								setMode(Mode.Normal);
							} else {
								SimpleDialog st = new SimpleDialog(TypeActivity.this);
								st.setCanceledOnTouchOutside(true);
								st.setMessage(R.string.msg_edit_as);
								st.setButtons(R.string.type_text, R.string.type_image,
									R.string.word_any);
								st.setClickListener(new IDialogClickListener() {
									@Override
									public void onClick(Dialog dialog, int index, ClickType type) {
										switch (index) {
										case 0:
											IntentUtil.edit(TypeActivity.this, first, Text.TYPE);
											break;

										case 1:
											IntentUtil.edit(TypeActivity.this, first, Image.TYPE);
											break;

										case 2:
											IntentUtil.edit(TypeActivity.this, first, "*/*");
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
					public void onClick(Dialog dialog, int index, ClickType type) {
						Tree.delete(TypeActivity.this, selected, new IProgressCallback() {
							@Override
							public void onProgress(ProgressType type, Object... data) {
								if (type == ProgressType.Finish || type == ProgressType.Cancel) {
									setMode(Mode.Normal);
									refresh(true);
								}
							}
						});
					}
				}));

			list.add(new DataItem(R.drawable.copy, R.string.word_copy_or_to,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index, ClickType type) {
						if (type == ClickType.Click) {
							if (ClipBoard.put(TypeActivity.this, ClipType.Copy, selected)) {
								setMode(Mode.Normal);
								App.showToast(R.string.msg_enter_target_direct_and_paste);
							} else {
								App.showToast(R.string.err_nothing_selected);
							}
						} else if (type == ClickType.LongClick) {
							Intent intent = new Intent(TypeActivity.this, SelectActivity.class);
							startActivityForResult(intent, REQ_COPY_TO);
						}
					}
				}));

			list.add(new DataItem(R.drawable.cut, R.string.word_cut_or_to,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index, ClickType type) {
						if (type == ClickType.Click) {
							if (ClipBoard.put(TypeActivity.this, ClipType.Cut, selected)) {
								setMode(Mode.Normal);
								App.showToast(R.string.msg_enter_target_direct_and_paste);
							} else {
								App.showToast(R.string.err_nothing_selected);
							}
						} else if (type == ClickType.LongClick) {
							Intent intent = new Intent(TypeActivity.this, SelectActivity.class);
							startActivityForResult(intent, REQ_CUT_TO);
						}
					}
				}));

			list.add(new DataItem(R.drawable.compress, R.string.word_compress_to,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index, ClickType type) {
						Intent intent = new Intent(TypeActivity.this, SelectActivity.class);
						startActivityForResult(intent, REQ_COMPRESS_TO);
					}
				}));

			if (selected.size() == 1 && first instanceof Zip) {
				list.add(new DataItem(R.drawable.decompress, R.string.word_decompress_to,
					new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int index, ClickType type) {
							Intent intent = new Intent(TypeActivity.this, SelectActivity.class);
							startActivityForResult(intent, REQ_DECOMPRESS_TO);
						}
					}));
			}

			dl.show();
		} else {
			list.add(new DataItem(R.drawable.refresh, R.string.word_refresh,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index, ClickType type) {
						refresh(true);
					}
				}));

			list.add(new DataItem(R.drawable.multi_select_pre, R.string.word_multi_select,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index, ClickType type) {
						setMode(Mode.Select);
					}
				}));

			list.add(new DataItem(R.drawable.setting, R.string.word_setting,
				new IDialogClickListener() {
					@Override
					public void onClick(Dialog dialog, int index, ClickType type) {
						Intent intent = new Intent(TypeActivity.this, SettingActivity.class);
						startActivity(intent);
					}
				}));
		}

		dl.show(DownList.POS_END, DownList.POS_END, 0, mLlInfo.getHeight());
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mMode == Mode.Select) {
				setMode(Mode.Normal);
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

		if (resultCode != RESULT_OK || data == null) {
			return;
		}

		if (requestCode == REQ_COPY_TO) {
			String path = data.getStringExtra(SelectActivity.KEY_PATH);

			Tree.carry(this, DataUtil.leaf2PathString(mTypeAdapter.getSelected()), path, false,
				new IProgressCallback() {
					@Override
					public void onProgress(ProgressType type, Object... data) {
						if (type == ProgressType.Finish || type == ProgressType.Cancel) {
							setMode(Mode.Normal);
							refresh(true);
						}
					}
				});
		} else if (requestCode == REQ_CUT_TO) {
			String path = data.getStringExtra(SelectActivity.KEY_PATH);

			Tree.carry(this, DataUtil.leaf2PathString(mTypeAdapter.getSelected()), path, true,
				new IProgressCallback() {
					@Override
					public void onProgress(ProgressType type, Object... data) {
						if (type == ProgressType.Finish || type == ProgressType.Cancel) {
							setMode(Mode.Normal);
							refresh(true);
						}
					}
				});
		} else if (requestCode == REQ_COMPRESS_TO) {
			String path = data.getStringExtra(SelectActivity.KEY_PATH);

			Tree.zip(this, path, mTypeAdapter.getSelected(), new IProgressCallback() {
				@Override
				public void onProgress(ProgressType type, Object... data) {
					if (type == ProgressType.Finish) {
						setMode(Mode.Normal);
					}
				}
			});
		} else if (requestCode == REQ_DECOMPRESS_TO) {
			String path = data.getStringExtra(SelectActivity.KEY_PATH);

			Tree.unzip(TypeActivity.this, mTypeAdapter.getSelected().get(0).getPath(), path,
				new IProgressCallback() {
					@Override
					public void onProgress(ProgressType type, Object... data) {
						if (type == ProgressType.Finish) {
							setMode(Mode.Normal);
						}
					}
				});
		}
	}
}
