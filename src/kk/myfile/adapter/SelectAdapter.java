package kk.myfile.adapter;

import kk.myfile.R;
import kk.myfile.activity.SelectActivity;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SelectAdapter extends BaseAdapter {
	private final SelectActivity mActivity;
	private Leaf[] mData;
	private Direct mSelected;
	
	public SelectAdapter(SelectActivity activity) {
		mActivity = activity;
	}
	
	public void setData(Leaf[] data) {
		mData = data;
		mSelected = null;
	}
	
	@Override
	public int getCount() {
		return mData == null ? 0 : mData.length;
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
		final Holder holder;
		
		if (view == null) {
			view = mActivity.getLayoutInflater().inflate(R.layout.grid_select, null);
			
			holder = new Holder();
			holder.icon = (ImageView) view.findViewById(R.id.iv_icon);
			holder.name = (TextView) view.findViewById(R.id.tv_name);
			holder.select = (ImageView) view.findViewById(R.id.iv_select);
			view.setTag(holder);
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (holder.leaf instanceof Direct) {
						mActivity.showDirect((Direct) holder.leaf, true);
					}
				}
			});
			
			holder.select.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (holder.leaf instanceof Direct) {
						if (mSelected == holder.leaf) {
							mSelected = null;
						} else {
							mSelected = (Direct) holder.leaf;
						}
						
						notifyDataSetChanged();
					}
				}
			});
		} else {
			holder = (Holder) view.getTag();
		}
		
		if (position < 0 || position >= mData.length) {
			return view;
		}
		Leaf leaf = mData[position];
		
		holder.icon.setImageResource(leaf.getIcon());
		holder.name.setText(leaf.getFile().getName());
		holder.select.setImageResource(mSelected == leaf ? R.drawable.setting : R.drawable.close);
		holder.leaf = leaf;
		
		return view;
	}
	
	public Direct getSelected() {
		return mSelected;
	}

	class Holder {
		public ImageView icon;
		public TextView name;
		public ImageView select;
		public Leaf leaf;
	}
}
