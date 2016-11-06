package kk.myfile.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import kk.myfile.R;
import kk.myfile.activity.App;
import kk.myfile.leaf.Leaf;

public class DetailItemAdapter extends BaseAdapter {
	public static class Data {
		public Leaf leaf;
		public int sort;
		public String key;
		public String value;
		public IClickListener clickListener;
	}

	public static interface IClickListener {
		public void onClick(Data data, ViewHolder vh);
	}

	public static class ViewHolder {
		;
		public TextView key;
		public TextView value;

		public Data data;
	}

	private final Context mContext;
	private final List<Data> mDataList = new ArrayList<Data>();

	public DetailItemAdapter(Context context) {
		mContext = context;
	}

	public void setDataList(List<Data> dataList) {
		mDataList.clear();
		mDataList.addAll(dataList);
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
		final Data data = mDataList.get(position);

		if (view == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.grid_detail_item, null);
			vh = new ViewHolder();
			vh.key = (TextView) view.findViewById(R.id.tv_key);
			vh.value = (TextView) view.findViewById(R.id.tv_value);
			view.setTag(vh);

			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (vh.data.clickListener != null) {
						vh.data.clickListener.onClick(vh.data, vh);
					}
				}
			});

			view.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					ClipboardManager cbm = (ClipboardManager) mContext
						.getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData cd = ClipData.newPlainText(vh.data.key, vh.data.value);
					cbm.setPrimaryClip(cd);

					App.showToast(R.string.msg_already_copied_to_clipboard);

					return true;
				}
			});
		} else {
			vh = (ViewHolder) view.getTag();
		}

		vh.key.setText(data.key);
		vh.value.setText(data.value);

		vh.data = data;

		return view;
	}
}
