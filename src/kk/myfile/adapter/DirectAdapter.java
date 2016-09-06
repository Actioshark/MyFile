package kk.myfile.adapter;

import kk.myfile.R;
import kk.myfile.activity.DirectActivity;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Leaf;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DirectAdapter extends BaseAdapter {
	private final DirectActivity mActivity;
	private Leaf[] mData;
	
	public DirectAdapter(DirectActivity activity) {
		mActivity = activity;
	}
	
	public void setData(Leaf[] data) {
		mData = data;
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
			view = mActivity.getLayoutInflater().inflate(R.layout.grid_direct, null);
			
			holder = new Holder();
			holder.icon = (ImageView) view.findViewById(R.id.iv_icon);
			holder.name = (TextView) view.findViewById(R.id.tv_name);
			view.setTag(holder);
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (holder.leaf instanceof Direct) {
						mActivity.showDirect((Direct) holder.leaf, true);
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
		holder.leaf = leaf;
		
		return view;
	}

	class Holder {
		public ImageView icon;
		public TextView name;
		public Leaf leaf;
	}
}
