package kk.myfile.adapter;

import kk.myfile.R;
import kk.myfile.activity.DirectActivity;
import kk.myfile.activity.SettingListStyleActivity;
import kk.myfile.activity.DirectActivity.Node;
import kk.myfile.activity.SettingListStyleActivity.ListStyle;
import kk.myfile.adapter.DownListAdapter.DataItem;
import kk.myfile.leaf.Audio;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Image;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Text;
import kk.myfile.leaf.Video;
import kk.myfile.tree.Sorter;
import kk.myfile.tree.Sorter.Classify;
import kk.myfile.ui.DownList;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.SimpleDialog;
import kk.myfile.util.AppUtil;
import kk.myfile.util.IntentUtil;
import kk.myfile.util.Setting;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DirectAdapter extends BaseAdapter {
	private final DirectActivity mActivity;
	private Leaf[] mData;
	private Object mMark;
	
	public DirectAdapter(DirectActivity activity) {
		mActivity = activity;
	}
	
	public void setData(final Leaf[] data) {
		mMark = data;
		
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				synchronized (data) {
					Sorter.sort(Classify.Tree, data);
				}
				
				AppUtil.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mMark == data) {
							mData = data;
							notifyDataSetChanged();
						}
					}
				});
			}
		});
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
		final ViewHolder holder;
		
		if (view == null) {
			String key = Setting.getListStyle();
			ListStyle ls = SettingListStyleActivity.getListStyle(key);
			view = mActivity.getLayoutInflater().inflate(ls.layout, null);
			
			holder = new ViewHolder();
			holder.icon = (ImageView) view.findViewById(R.id.iv_icon);
			holder.name = (TextView) view.findViewById(R.id.tv_name);
			holder.size = (TextView) view.findViewById(R.id.tv_size);
			holder.time = (TextView) view.findViewById(R.id.tv_time);
			view.setTag(holder);
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (holder.leaf instanceof Direct) {
						mActivity.showDirect(new Node((Direct) holder.leaf), true);
					} else {
						if (IntentUtil.view(mActivity, holder.leaf, null) == false) {
							SimpleDialog dialog = new SimpleDialog(mActivity);
							dialog.setCanceledOnTouchOutside(true);
							dialog.setContent(R.string.hint_open_type_select);
							dialog.setButtons(new int[] {R.string.type_text, R.string.type_image,
									R.string.type_audio, R.string.type_video});
							dialog.setClickListener(new IDialogClickListener() {
								@Override
								public void onClick(Dialog dialog, int index) {
									switch (index) {
									case 0:
										IntentUtil.view(mActivity, holder.leaf, Text.TYPE);
										break;
										
									case 1:
										IntentUtil.view(mActivity, holder.leaf, Image.TYPE);
										break;
										
									case 2:
										IntentUtil.view(mActivity, holder.leaf, Audio.TYPE);
										break;
										
									case 3:
										IntentUtil.view(mActivity, holder.leaf, Video.TYPE);
										break;
									}
									
									dialog.dismiss();
								}
							});
							dialog.show();
						}
					}
				}
			});
			
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					DownList dl = new DownList(mActivity);
					List<DataItem> list = new ArrayList<DataItem>();
					DownListAdapter dla = dl.getAdapter();
					dla.setDataList(list);
					
					list.add(new DataItem(R.drawable.detail, R.string.word_detail,
						new IDialogClickListener() {
							@Override
							public void onClick(Dialog dialog, int index) {
								// TODO
							}
						}
					));
					
					list.add(new DataItem(R.drawable.share, R.string.word_share,
						new IDialogClickListener() {
							@Override
							public void onClick(Dialog dialog, int index) {
								// TODO
							}
						}
					));
					
					list.add(new DataItem(R.drawable.copy, R.string.word_copy,
						new IDialogClickListener() {
							@Override
							public void onClick(Dialog dialog, int index) {
								// TODO
							}
						}
					));
					
					list.add(new DataItem(R.drawable.cut, R.string.word_cut,
						new IDialogClickListener() {
							@Override
							public void onClick(Dialog dialog, int index) {
								// TODO
							}
						}
					));
					
					list.add(new DataItem(R.drawable.cross, R.string.word_delete,
						new IDialogClickListener() {
							@Override
							public void onClick(Dialog dialog, int index) {
								// TODO
							}
						}
					));
					
					dl.show();
					
					return true;
				}
			});
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		if (position < 0 || position >= mData.length) {
			return view;
		}
		Leaf leaf = mData[position];
		
		File file = leaf.getFile();
		holder.leaf = leaf;
		
		holder.icon.setImageResource(leaf.getIcon());
		holder.name.setText(file.getName());
		
		if (holder.time != null) {
			Date date = new Date(file.lastModified());
			DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss", Setting.LOCALE);
			holder.time.setText(df.format(date));
		}
		
		if (holder.size != null) {
			String num = String.valueOf(file.length());
			StringBuilder sb = new StringBuilder();
			int len = num.length();
			for (int i = 0; i < len; i++) {
				sb.append(num.charAt(i));

				if (i + 1 != len && (len - i) % 3 == 1) {
					sb.append(',');
				}
			}
			holder.size.setText(String.format(Setting.LOCALE, "%s B", sb.toString()));
		}
		
		return view;
	}

	class ViewHolder {
		public Leaf leaf;
		public ImageView icon;
		public TextView name;
		public TextView time;
		public TextView size;
	}
}
