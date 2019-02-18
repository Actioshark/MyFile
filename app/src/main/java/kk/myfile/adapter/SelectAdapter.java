package kk.myfile.adapter;

import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.activity.DirectActivity.Node;
import kk.myfile.activity.SelectActivity;
import kk.myfile.activity.BaseActivity.Classify;
import kk.myfile.file.Sorter;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SelectAdapter extends BaseAdapter {
	private final SelectActivity mActivity;
	private final List<Direct> mDataList = new ArrayList<Direct>();
	private Direct mSelected;

	public SelectAdapter(SelectActivity activity) {
		mActivity = activity;
	}

	public void setData(final List<Leaf> dataList, final int position) {
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				synchronized (dataList) {
					Sorter.sort(Classify.Direct, dataList);
				}

				AppUtil.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mDataList.clear();
						synchronized (dataList) {
							for (Leaf leaf : dataList) {
								if (leaf instanceof Direct) {
									mDataList.add((Direct) leaf);
								}
							}
						}

						notifyDataSetChanged();

						AppUtil.runOnUiThread(new Runnable() {
							public void run() {
								mActivity.setSelection(position);
							}
						});
					}
				});
			}
		});

		mSelected = null;
	}

	@Override
	public int getCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		final ViewHolder vh;

		if (view == null) {
			view = mActivity.getLayoutInflater().inflate(R.layout.grid_select, null);

			vh = new ViewHolder();
			vh.icon = (ImageView) view.findViewById(R.id.iv_icon);
			vh.name = (TextView) view.findViewById(R.id.tv_name);
			vh.select = (ImageView) view.findViewById(R.id.iv_select);
			view.setTag(vh);

			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					mActivity.showDirect(new Node(vh.data), true);
				}
			});

			vh.select.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mSelected == vh.data) {
						mSelected = null;
					} else {
						mSelected = vh.data;
					}

					notifyDataSetChanged();
				}
			});
		} else {
			vh = (ViewHolder) view.getTag();
		}

		Direct data = mDataList.get(position);

		vh.icon.setImageResource(data.getIcon());
		vh.name.setText(data.getFile().getName());
		vh.select.setImageResource(mSelected == data ? R.drawable.single_select_pre
			: R.drawable.single_select_nor);
		
		vh.data = data;

		return view;
	}

	public Direct getSelected() {
		return mSelected;
	}

	static class ViewHolder {
		public ImageView icon;
		public TextView name;
		public ImageView select;
		public Direct data;
	}
}
