package kk.myfile.adapter;

import java.util.ArrayList;
import java.util.List;

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
	private final List<Direct> mData = new ArrayList<Direct>();
	private Direct mSelected;
	
	public SelectAdapter(SelectActivity activity) {
		mActivity = activity;
	}
	
	public void setData(Leaf[] data) {
		mData.clear();
		for (Leaf leaf : data) {
			if (leaf instanceof Direct) {
				mData.add((Direct) leaf);
			}
		}
		
		mSelected = null;
	}
	
	@Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
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
					mActivity.showDirect(holder.data, true);
				}
			});
			
			holder.select.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mSelected == holder.data) {
						mSelected = null;
					} else {
						mSelected = holder.data;
					}
					
					notifyDataSetChanged();
				}
			});
		} else {
			holder = (Holder) view.getTag();
		}
		
		if (position < 0 || position >= mData.size()) {
			return view;
		}
		Direct data = mData.get(position);
		
		holder.icon.setImageResource(data.getIcon());
		holder.name.setText(data.getFile().getName());
		holder.select.setImageResource(mSelected == data ? R.drawable.select_pre
			: R.drawable.select_nor);
		holder.data = data;
		
		return view;
	}
	
	public Direct getSelected() {
		return mSelected;
	}

	class Holder {
		public ImageView icon;
		public TextView name;
		public ImageView select;
		public Direct data;
	}
}
