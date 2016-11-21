package kk.myfile.adapter;

import kk.myfile.R;
import kk.myfile.activity.BaseActivity.Classify;
import kk.myfile.activity.BaseActivity.Mode;
import kk.myfile.activity.SettingListStyleActivity.ListStyle;
import kk.myfile.activity.DetailActivity;
import kk.myfile.activity.SettingListStyleActivity;
import kk.myfile.activity.TypeActivity;
import kk.myfile.file.FileUtil;
import kk.myfile.file.ImageUtil;
import kk.myfile.file.ImageUtil.IThumListener;
import kk.myfile.file.Sorter;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;
import kk.myfile.util.DataUtil;
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

public class TypeAdapter extends BaseAdapter {
	private final TypeActivity mActivity;
	private final Classify mClassify;

	private final List<Leaf> mDataList = new ArrayList<Leaf>();
	private final Set<Integer> mSelected = new HashSet<Integer>();

	private long mTouchDownTime;
	private Runnable mTouchRunnable;

	public TypeAdapter(TypeActivity activity, Classify classify) {
		mActivity = activity;
		mClassify = classify;
	}

	public void setData(final List<Leaf> dataList) {
		if (mClassify == Classify.Type) {
			synchronized (dataList) {
				Sorter.sort(Classify.Type, dataList);
			}
		}

		AppUtil.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mDataList.clear();
				synchronized (dataList) {
					mDataList.addAll(dataList);
				}

				mActivity.updateTitle();
				mActivity.updateInfo();
				notifyDataSetChanged();
			}
		});
	}

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public int getViewTypeCount() {
		return SettingListStyleActivity.getStyleSize();
	}

	@Override
	public int getItemViewType(int position) {
		String key = Setting.getListStyle(mClassify);
		return SettingListStyleActivity.getStyleIndex(key);
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
		int size = mDataList.size();

		for (int i = 0; i < size; i++) {
			if (mSelected.contains(i)) {
				list.add(mDataList.get(i));
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

			int len = getCount();
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
		final ViewHolder vh;

		if (view == null) {
			String key = Setting.getListStyle(mClassify);
			final ListStyle ls = SettingListStyleActivity.getListStyle(key);
			view = mActivity.getLayoutInflater().inflate(ls.layout, null);

			vh = new ViewHolder();
			vh.icon = (ImageView) view.findViewById(R.id.iv_icon);
			vh.name = (TextView) view.findViewById(R.id.tv_name);
			vh.size = (TextView) view.findViewById(R.id.tv_size);
			vh.time = (TextView) view.findViewById(R.id.tv_time);
			vh.sign = (ImageView) view.findViewById(R.id.iv_sign);
			vh.select = (ImageView) view.findViewById(R.id.iv_select);
			view.setTag(vh);

			view.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent event) {
					int action = event.getAction();

					if (action == MotionEvent.ACTION_DOWN) {
						if (ls.needDetail) {
							mActivity.showDetail(vh.leaf);
						}

						mTouchDownTime = SystemClock.elapsedRealtime();
						mTouchRunnable = new Runnable() {
							@Override
							public void run() {
								mTouchRunnable = null;

								List<Leaf> selected = getSelected();
								if (selected.size() < 1) {
									selected = mDataList;
								}

								int index = 0;
								for (int i = 0; i < selected.size(); i++) {
									if (selected.get(i) == vh.leaf) {
										index = i;
										break;
									}
								}

								Intent intent = new Intent(mActivity, DetailActivity.class);
								intent.putCharSequenceArrayListExtra(DetailActivity.KEY_PATH,
									DataUtil.leaf2PathCs(selected));
								intent.putExtra(DetailActivity.KEY_INDEX, index);
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
									if (mSelected.contains(vh.position)) {
										mSelected.remove(vh.position);
									} else {
										mSelected.add(vh.position);
									}

									mActivity.updateTitle();
									mActivity.updateInfo();
									notifyDataSetChanged();
								} else {
									vh.leaf.open(mActivity, false);
								}
							} else {
								if (mActivity.getMode() == Mode.Select) {
									if (mSelected.contains(vh.position)) {
										mSelected.remove(vh.position);
									} else {
										mSelected.add(vh.position);
									}

									mActivity.updateTitle();
									mActivity.updateInfo();
									notifyDataSetChanged();
								} else {
									mSelected.clear();
									mSelected.add(vh.position);
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
			vh = (ViewHolder) view.getTag();
		}

		final Leaf leaf = mDataList.get(position);
		final File file = leaf.getFile();

		vh.name.setText(file.getName());

		if (leaf.equals(vh.leaf) == false || vh.hasThum == false) {
			vh.hasThum = false;
			vh.icon.setImageResource(leaf.getIcon());

			ImageUtil.getThum(leaf, vh.icon.getWidth(), vh.icon.getHeight(),
				new IThumListener() {
					@Override
					public void onThumGot(Drawable drawable) {
						if (leaf.equals(vh.leaf)) {
							vh.hasThum = true;
							vh.icon.setImageDrawable(drawable);
						}
					}
				});
		}

		if (vh.time != null) {
			Date date = new Date(file.lastModified());
			DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Setting.LOCALE);
			vh.time.setText(df.format(date));
		}

		if (vh.size != null) {
			if (leaf instanceof Direct) {
				vh.size.setText("");
			} else {
				vh.size.setText(String.format(Setting.LOCALE, "%s B", MathUtil.insertComma(file
					.length())));
			}
		}

		vh.sign.setVisibility(FileUtil.isLink(file) ? View.VISIBLE : View.GONE);

		if (mActivity.getMode() == Mode.Select) {
			if (mSelected.contains(position)) {
				vh.select.setImageResource(R.drawable.multi_select_pre);
				vh.select.setVisibility(View.VISIBLE);
			} else {
				vh.select.setVisibility(View.GONE);
			}
		} else {
			vh.select.setVisibility(View.GONE);
		}

		vh.leaf = leaf;
		vh.position = position;

		return view;
	}

	static class ViewHolder {
		public Leaf leaf;
		public int position;
		public boolean hasThum;

		public ImageView icon;
		public TextView name;
		public TextView time;
		public TextView size;
		public ImageView sign;
		public ImageView select;
	}
}
