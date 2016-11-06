package kk.myfile.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.file.FileUtil;
import kk.myfile.file.ImageUtil.IThumable;
import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

public class DetailAdapter extends PagerAdapter {
	static class ViewHolder {
		public ImageView thum;
		public ListView list;

		public int position = -1;
	}

	private final Context mContext;
	private List<String> mDataList;
	private final List<View> mViewList = new ArrayList<View>();

	public DetailAdapter(Context context) {
		mContext = context;

		for (int i = 0; i < 5; i++) {
			View root = LayoutInflater.from(context).inflate(R.layout.grid_detail, null);
			ViewHolder holder = new ViewHolder();

			holder.thum = (ImageView) root.findViewById(R.id.iv_thum);
			holder.list = (ListView) root.findViewById(R.id.lv_list);

			root.setTag(holder);
			mViewList.add(root);
		}
	}

	public void setDataList(List<String> list) {
		mDataList = list;
	}

	@Override
	public int getCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	protected int getViewPosition(int position) {
		return position % mViewList.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(mViewList.get(getViewPosition(position)));
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		View root = mViewList.get(getViewPosition(position));
		final ViewHolder vh = (ViewHolder) root.getTag();

		String path = mDataList.get(position);
		if (vh.position != position) {
			vh.position = position;

			final Leaf leaf = FileUtil.createLeaf(new File(path));

			vh.thum.setImageResource(leaf.getIcon());
			if (leaf instanceof IThumable) {
				AppUtil.runOnNewThread(new Runnable() {
					@Override
					public void run() {
						try {
							final Drawable drawable = ((IThumable) leaf).getThum(512, 512);
							AppUtil.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (vh.position == position) {
										vh.thum.setImageDrawable(drawable);
									}
								}
							});
						} catch (Exception e) {
						}
					}
				});
			}

			DetailItemAdapter adapter = new DetailItemAdapter(mContext);
			adapter.setDataList(leaf.getDetail());
			vh.list.setAdapter(adapter);
		}

		if (root.getParent() == null) {
			container.addView(root, 0);
		}

		return root;
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return view == obj;
	}
}
