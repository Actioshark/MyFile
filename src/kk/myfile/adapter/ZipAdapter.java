package kk.myfile.adapter;

import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.model.FileHeader;

import kk.myfile.R;
import kk.myfile.activity.DirectActivity.Node;
import kk.myfile.activity.BaseActivity.Classify;
import kk.myfile.activity.ZipActivity;
import kk.myfile.file.Sorter;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;
import kk.myfile.util.DataUtil;
import kk.myfile.util.MathUtil;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ZipAdapter extends BaseAdapter {
	private final ZipActivity mActivity;
	private final List<Leaf> mDataList = new ArrayList<Leaf>();

	public ZipAdapter(ZipActivity activity) {
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
							mDataList.addAll(dataList);
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
			view = mActivity.getLayoutInflater().inflate(R.layout.grid_zip, null);

			vh = new ViewHolder();
			vh.icon = (ImageView) view.findViewById(R.id.iv_icon);
			vh.name = (TextView) view.findViewById(R.id.tv_name);
			vh.desc = (TextView) view.findViewById(R.id.tv_desc);
			view.setTag(vh);

			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (vh.leaf instanceof Direct) {
						mActivity.showDirect(new Node((Direct) vh.leaf), true);
					} else {
						mActivity.extractFile(vh.leaf.getPath());
					}
				}
			});
		} else {
			vh = (ViewHolder) view.getTag();
		}

		Leaf leaf = mDataList.get(position);

		vh.icon.setImageResource(leaf.getIcon());
		vh.name.setText(DataUtil.getFileName(leaf.getPath()));
		
		if (leaf instanceof Direct) {
			int cn = ((Direct) leaf).getChildren().size();
			vh.desc.setText(AppUtil.getString(R.string.msg_children_with_num, cn));
		} else {
			FileHeader fh = (FileHeader) leaf.getTag();
			String cs = MathUtil.insertComma(fh.getCompressedSize());
			String us = MathUtil.insertComma(fh.getUncompressedSize());
			vh.desc.setText(String.format("%s/%s B", cs, us));
		}
		
		vh.leaf = leaf;

		return view;
	}

	static class ViewHolder {
		public ImageView icon;
		public TextView name;
		public TextView desc;
		
		public Leaf leaf;
	}
}
