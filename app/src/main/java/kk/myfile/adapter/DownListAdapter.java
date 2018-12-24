package kk.myfile.adapter;

import java.util.List;

import kk.myfile.R;
import kk.myfile.ui.DownList;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.IDialogClickListener.ClickType;
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

		public DataItem() {
		}

		public DataItem(int icon, int text, IDialogClickListener click) {
			this.icon = icon;
			this.text = text;
			this.click = click;
		}
	}

	private final Context mContext;
	private final DownList mDownList;
	private List<DataItem> mDataList;

	public DownListAdapter(Context context, DownList downList) {
		mContext = context;
		mDownList = downList;
	}

	public void setDataList(List<DataItem> dataList) {
		mDataList = dataList;
	}

	@Override
	public int getCount() {
		return mDataList == null ? 0 : mDataList.size();
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
					DataItem data = mDataList.get(vh.index);
					data.click.onClick(mDownList, vh.index, ClickType.Click);
					mDownList.dismiss();
				}
			});

			view.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					DataItem data = mDataList.get(vh.index);
					data.click.onClick(mDownList, vh.index, ClickType.LongClick);
					mDownList.dismiss();
					return true;
				}
			});

			view.setTag(vh);
		} else {
			vh = (ViewHolder) view.getTag();
		}

		DataItem data = mDataList.get(index);
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
