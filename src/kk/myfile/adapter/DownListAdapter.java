package kk.myfile.adapter;

import java.util.List;

import kk.myfile.R;
import kk.myfile.ui.DownList;
import kk.myfile.ui.IDialogClickListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DownListAdapter extends BaseAdapter {
	public static class DataItem {
		public int icon;
		public int text;

		public IDialogClickListener click;
		public IDialogClickListener longClick;

		public DataItem() {
		}

		public DataItem(int icon, int text, IDialogClickListener click) {
			this.icon = icon;
			this.text = text;
			this.click = click;
		}

		public DataItem(int icon, int text, IDialogClickListener click,
			IDialogClickListener longClick) {

			this.icon = icon;
			this.text = text;
			this.click = click;
			this.longClick = longClick;
		}
	}

	private final Context mContext;
	private final DownList mDownList;
	private List<DataItem> mData;

	public DownListAdapter(Context context, DownList downList) {
		mContext = context;
		mDownList = downList;
	}

	public void setDataList(List<DataItem> data) {
		mData = data;
	}

	@Override
	public int getCount() {
		return mData == null ? 0 : mData.size();
	}

	@Override
	public Object getItem(int index) {
		return null;
	}

	@Override
	public long getItemId(int index) {
		return 0;
	}

	@Override
	public View getView(int index, View view, ViewGroup parent) {
		final ViewHolder vh;

		if (view == null) {
			view = LayoutInflater.from(mContext).inflate(R.layout.grid_list_down, null);

			vh = new ViewHolder();
			vh.icon = (ImageView) view.findViewById(R.id.iv_icon);
			vh.text = (TextView) view.findViewById(R.id.tv_text);

			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					DataItem data = mData.get(vh.index);
					data.click.onClick(mDownList, vh.index);
					mDownList.dismiss();
				}
			});

			view.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					DataItem data = mData.get(vh.index);
					if (data.longClick == null) {
						return false;
					}

					data.longClick.onClick(mDownList, vh.index);

					mDownList.dismiss();
					return true;
				}
			});

			view.setTag(vh);
		} else {
			vh = (ViewHolder) view.getTag();
		}

		DataItem data = mData.get(index);
		vh.icon.setImageResource(data.icon);
		vh.text.setText(data.text);

		vh.index = index;

		return view;
	}

	static class ViewHolder {
		public ImageView icon;
		public TextView text;

		public int index;
	}
}
