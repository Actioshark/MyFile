package kk.myfile.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import kk.myfile.R;

public class DetailAdapter extends BaseAdapter {
	private final Context mContext;
	private final List<Data> mData = new ArrayList<Data>();

	public DetailAdapter(Context context, Map<String, String> data) {
		mContext = context;
		
		for (Entry<String, String> entry : data.entrySet()) {
			Data d = new Data();
			d.key = entry.getKey();
			d.value = entry.getValue();
			mData.add(d);
		}
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

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;
		Data data = mData.get(position);

		if (view == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.grid_detail, null);
			holder = new ViewHolder();
			holder.key = (TextView) view.findViewById(R.id.tv_key);
			holder.value = (TextView) view.findViewById(R.id.tv_value);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		holder.key.setText(data.key);
		holder.value.setText(data.value);

		return view;
	}
	
	public class Data {
		public String key;
		public String value;
	}

	class ViewHolder {;
		public TextView key;
		public TextView value;
	}
}
