package kk.myfile.adapter;

import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.activity.DirectActivity.Node;
import kk.myfile.activity.BaseActivity.Classify;
import kk.myfile.activity.ArchiveActivity;
import kk.myfile.file.ArchiveHelper.FileHeader;
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

public class ArchiveAdapter extends BaseAdapter {
	private final ArchiveActivity mActivity;
	private final List<Leaf> mDataList = new ArrayList<>();

	public ArchiveAdapter(ArchiveActivity activity) {
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
		return mDataList.size();
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
			view = mActivity.getLayoutInflater().inflate(R.layout.grid_archive, null);

			vh = new ViewHolder();
			vh.icon = view.findViewById(R.id.iv_icon);
			vh.name = view.findViewById(R.id.tv_name);
			vh.desc = view.findViewById(R.id.tv_desc);
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
			FileHeader fh = mActivity.getFileHeader(leaf.getPath());
			String cs = MathUtil.insertComma(fh.getCompressSize());
			String us = MathUtil.insertComma(fh.getExtractSize());
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
