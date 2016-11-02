package kk.myfile.adapter;

import kk.myfile.R;
import kk.myfile.activity.BaseActivity.Classify;
import kk.myfile.activity.BaseActivity.Mode;
import kk.myfile.activity.DetailActivity;
import kk.myfile.activity.DirectActivity;
import kk.myfile.activity.SettingListStyleActivity;
import kk.myfile.activity.DirectActivity.Node;
import kk.myfile.activity.SettingListStyleActivity.ListStyle;
import kk.myfile.file.FileUtil;
import kk.myfile.file.ImageUtil;
import kk.myfile.file.ImageUtil.IThumListenner;
import kk.myfile.file.Sorter;
import kk.myfile.leaf.Audio;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Image;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Text;
import kk.myfile.leaf.Video;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.SimpleDialog;
import kk.myfile.util.AppUtil;
import kk.myfile.util.IntentUtil;
import kk.myfile.util.MathUtil;
import kk.myfile.util.Setting;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DirectAdapter extends BaseAdapter {
	private final DirectActivity mActivity;
	private final List<Leaf> mData = new ArrayList<Leaf>();
	private Object mMark;
	private final Set<Integer> mSelected = new HashSet<Integer>();
	
	private long mTouchDownTime;
	private Runnable mTouchRunnable;

	public DirectAdapter(DirectActivity activity) {
		mActivity = activity;
	}

	public void setData(final List<Leaf> data, final int position) {
		mMark = data;

		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				synchronized (data) {
					Sorter.sort(Classify.Direct, data);
				}

				AppUtil.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mMark == data) {
							mData.clear();
							synchronized (data) {
								mData.addAll(data);
							}

							mActivity.updateTitle();
							mActivity.updateInfo();
							notifyDataSetChanged();

							AppUtil.runOnUiThread(new Runnable() {
								public void run() {
									mActivity.setSelection(position);
								}
							});
						}
					}
				});
			}
		});
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public List<Leaf> getSelected() {
		List<Leaf> list = new ArrayList<Leaf>();
		int size = mData.size();

		for (Integer position : mSelected) {
			if (position < size) {
				list.add(mData.get(position));
			}
		}

		return list;
	}

	public int getSelectedCount() {
		return mSelected.size();
	}

	public void selectAll(boolean select) {
		if (select) {
			mSelected.clear();

			int len = mData.size();
			for (int i = 0; i < len; i++) {
				mSelected.add(i);
			}
		} else {
			mSelected.clear();
		}

		mActivity.updateTitle();
		mActivity.updateInfo();
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		final ViewHolder holder;

		if (view == null) {
			String key = Setting.getListStyle(Classify.Direct);
			final ListStyle ls = SettingListStyleActivity.getListStyle(key);
			view = mActivity.getLayoutInflater().inflate(ls.layout, null);

			holder = new ViewHolder();
			holder.icon = (ImageView) view.findViewById(R.id.iv_icon);
			holder.name = (TextView) view.findViewById(R.id.tv_name);
			holder.size = (TextView) view.findViewById(R.id.tv_size);
			holder.time = (TextView) view.findViewById(R.id.tv_time);
			holder.sign = (ImageView) view.findViewById(R.id.iv_sign);
			holder.select = (ImageView) view.findViewById(R.id.iv_select);
			view.setTag(holder);
			
			view.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent event) {
					int action = event.getAction();
					
					if (action == MotionEvent.ACTION_DOWN) {
						if (ls.needDetail) {
							mActivity.showDetail(holder.leaf);
						}
						
						mTouchDownTime = SystemClock.elapsedRealtime();
						mTouchRunnable = new Runnable() {
							@Override
							public void run() {
								mTouchRunnable = null;
								
								Intent intent = new Intent(mActivity, DetailActivity.class);
								intent.putExtra(DetailActivity.KEY_PATH, holder.leaf.getPath());
								mActivity.startActivity(intent);
							}
						};
						AppUtil.runOnUiThread(mTouchRunnable, 800);
					} else if (action == MotionEvent.ACTION_UP) {
						if (mTouchRunnable != null) {
							AppUtil.removeUiThread(mTouchRunnable);
							mTouchRunnable = null;
							
							long delta = SystemClock.elapsedRealtime() - mTouchDownTime;
							
							if (delta < 300) {
								if (mActivity.getMode() == Mode.Select) {
									if (mSelected.contains(holder.position)) {
										mSelected.remove(holder.position);
									} else {
										mSelected.add(holder.position);
									}
	
									mActivity.updateTitle();
									mActivity.updateInfo();
									notifyDataSetChanged();
								} else if (holder.leaf instanceof Direct) {
									mActivity.changeDirect(new Node((Direct) holder.leaf), true);
								} else {
									if (IntentUtil.view(mActivity, holder.leaf, null) == false) {
										SimpleDialog dialog = new SimpleDialog(mActivity);
										dialog.setCanceledOnTouchOutside(true);
										dialog.setMessage(R.string.msg_open_as);
										dialog.setButtons(R.string.type_text, R.string.type_image, R.string.type_audio,
												R.string.type_video, R.string.word_any);
										dialog.setClickListener(new IDialogClickListener() {
											@Override
											public void onClick(Dialog dialog, int index) {
												switch (index) {
												case 0:
													IntentUtil.view(mActivity, holder.leaf, Text.TYPE);
													break;
	
												case 1:
													IntentUtil.view(mActivity, holder.leaf, Image.TYPE);
													break;
	
												case 2:
													IntentUtil.view(mActivity, holder.leaf, Audio.TYPE);
													break;
	
												case 3:
													IntentUtil.view(mActivity, holder.leaf, Video.TYPE);
													break;
	
												case 4:
													IntentUtil.view(mActivity, holder.leaf, "*/*");
													break;
												}
	
												dialog.dismiss();
											}
										});
										dialog.show();
									}
								}
							} else {
								if (mActivity.getMode() == Mode.Select) {
									if (mSelected.contains(holder.position)) {
										mSelected.remove(holder.position);
									} else {
										mSelected.add(holder.position);
									}
	
									mActivity.updateTitle();
									mActivity.updateInfo();
									notifyDataSetChanged();
								} else {
									mSelected.clear();
									mSelected.add(holder.position);
									mActivity.setMode(Mode.Select);
								}
							}
						}
					} else if (action == MotionEvent.ACTION_CANCEL) {
						if (mTouchRunnable != null) {
							AppUtil.removeUiThread(mTouchRunnable);
							mTouchRunnable = null;
						}
					}

					return view.onTouchEvent(event);
				}
			});

			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
				}
			});
		} else {
			holder = (ViewHolder) view.getTag();
		}

		final Leaf leaf = mData.get(position);
		final File file = leaf.getFile();

		holder.name.setText(file.getName());

		if (leaf.equals(holder.leaf) == false || holder.hasThum == false) {
			holder.hasThum = false;
			holder.icon.setImageResource(leaf.getIcon());

			ImageUtil.getThum(leaf, holder.icon.getWidth(), holder.icon.getHeight(), new IThumListenner() {
				@Override
				public void onThumGot(Drawable drawable) {
					if (leaf.equals(holder.leaf)) {
						holder.hasThum = true;
						holder.icon.setImageDrawable(drawable);
					}
				}
			});
		}

		if (holder.time != null) {
			Date date = new Date(file.lastModified());
			DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Setting.LOCALE);
			holder.time.setText(df.format(date));
		}

		if (holder.size != null) {
			if (leaf instanceof Direct) {
				holder.size.setText("");
			} else {
				holder.size.setText(String.format(Setting.LOCALE,
					"%s B", MathUtil.insertComma(file.length())));
			}
		}

		holder.sign.setVisibility(FileUtil.isLink(file) ? View.VISIBLE : View.GONE);

		if (mActivity.getMode() == Mode.Select) {
			if (mSelected.contains(position)) {
				holder.select.setImageResource(R.drawable.multi_select_pre);
				holder.select.setVisibility(View.VISIBLE);
			} else {
				holder.select.setVisibility(View.GONE);
			}
		} else {
			holder.select.setVisibility(View.GONE);
		}

		holder.leaf = leaf;
		holder.position = position;

		return view;
	}

	class ViewHolder {
		public Leaf leaf;
		public int position = -1;
		public boolean hasThum = false;

		public ImageView icon;
		public TextView name;
		public TextView time;
		public TextView size;
		public ImageView sign;
		public ImageView select;
	}
}
