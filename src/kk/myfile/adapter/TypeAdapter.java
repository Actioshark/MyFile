package kk.myfile.adapter;

import kk.myfile.R;
import kk.myfile.activity.BaseActivity.Mode;
import kk.myfile.activity.SettingListStyleActivity.ListStyle;
import kk.myfile.activity.TypeActivity;
import kk.myfile.file.Sorter;
import kk.myfile.file.Sorter.Classify;
import kk.myfile.leaf.Audio;
import kk.myfile.leaf.Direct;
import kk.myfile.leaf.Image;
import kk.myfile.leaf.Leaf;
import kk.myfile.leaf.Text;
import kk.myfile.leaf.Video;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TypeAdapter extends BaseAdapter {
	private final TypeActivity mActivity;
	private final int mType;
	private final ListStyle mListStyle;
	
	private final List<Leaf> mData = new ArrayList<Leaf>();
	private Object mMark;
	private final Set<Integer> mSelected = new HashSet<Integer>();
	
	public TypeAdapter(TypeActivity activity, int type, ListStyle ls) {
		mActivity = activity;
		mType = type;
		mListStyle = ls;
	}
	
	public void setData(final List<Leaf> data) {
		if (mType != TypeActivity.TYPE_CLASS) {
			synchronized (mData) {
				mData.clear();
				synchronized (data) {
					mData.addAll(data);
				}
			}
			
			AppUtil.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					notifyDataSetChanged();
					mActivity.showInfo();
				}
			});
			
			return;
		}
		
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
							synchronized (mData) {
								mData.clear();
								synchronized (data) {
									mData.addAll(data);
								}
							}
							
							notifyDataSetChanged();
							mActivity.showInfo();
						}
					}
				});
			}
		});
	}
	
	@Override
	public int getCount() {
		synchronized (mData) {
			return mData.size();
		}
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	public List<Leaf> getSelected() {
		List<Leaf> list = new ArrayList<Leaf>();
		
		synchronized (mData) {
			int size = mData.size();
			
			for (Integer position : mSelected) {
				if (position < size) {
					list.add(mData.get(position));
				}
			}
		}
		
		return list;
	}
	
	public int getSelectedCount() {
		return mSelected.size();
	}
	
	public void selectAll(boolean select) {
		if (select) {
			mSelected.clear();
			
			int len = getCount();
			for (int i = 0 ; i < len; i++) {
				mSelected.add(i);
			}
		} else {
			mSelected.clear();
		}
		
		mActivity.showTitle();
		mActivity.showInfo();
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		final ViewHolder holder;
		
		if (view == null) {
			view = mActivity.getLayoutInflater().inflate(mListStyle.layout, null);
			
			holder = new ViewHolder();
			holder.icon = (ImageView) view.findViewById(R.id.iv_icon);
			holder.name = (TextView) view.findViewById(R.id.tv_name);
			holder.size = (TextView) view.findViewById(R.id.tv_size);
			holder.time = (TextView) view.findViewById(R.id.tv_time);
			holder.select = (ImageView) view.findViewById(R.id.iv_select);
			view.setTag(holder);
			
			if (mListStyle.needDetail) {
				view.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View view, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							mActivity.showDetail(holder.leaf);
						}
						
						return view.onTouchEvent(event);
					}
				});
			}
			
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mActivity.getMode() == Mode.Select) {
						if (mSelected.contains(holder.position)) {
							mSelected.remove(holder.position);
						} else {
							mSelected.add(holder.position);
						}
						
						mActivity.showTitle();
						mActivity.showInfo();
						notifyDataSetChanged();
					} else {
						if (IntentUtil.view(mActivity, holder.leaf, null) == false) {
							SimpleDialog dialog = new SimpleDialog(mActivity);
							dialog.setCanceledOnTouchOutside(true);
							dialog.setMessage(R.string.msg_open_as);
							dialog.setButtons(new int[] {R.string.type_text, R.string.type_image,
									R.string.type_audio, R.string.type_video, R.string.word_any});
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
									
									case 4:
										IntentUtil.view(mActivity, holder.leaf, "*/*");
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
					if (mActivity.getMode() == Mode.Normal) {
						mSelected.clear();
						mSelected.add(holder.position);
						mActivity.setMode(Mode.Select);
					} else {
						if (mSelected.contains(holder.position)) {
							mSelected.remove(holder.position);
						} else {
							mSelected.add(holder.position);
						}
						
						mActivity.showTitle();
						mActivity.showInfo();
						notifyDataSetChanged();
					}
					
					return true;
				}
			});
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		Leaf leaf = mData.get(position);
		File file = leaf.getFile();
		
		holder.name.setText(file.getName());
		
		if (leaf.equals(holder.leaf) == false || holder.hasBitmap == false) {
			Bitmap bmp = leaf.getThum(holder.icon.getWidth(), holder.icon.getHeight());
			if (bmp != null) {
				holder.hasBitmap = true;
				holder.icon.setImageBitmap(bmp);
			} else {
				holder.hasBitmap = false;
				holder.icon.setImageResource(leaf.getIcon());
			}
		}
		
		if (holder.time != null) {
			Date date = new Date(file.lastModified());
			DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss", Setting.LOCALE);
			holder.time.setText(df.format(date));
		}
		
		if (holder.size != null) {
			if (leaf instanceof Direct) {
				holder.size.setText("");
			} else {
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
		}
		
		if (mActivity.getMode() == Mode.Select) {
			if (mSelected.contains(position)) {
				holder.select.setImageResource(R.drawable.multi_select_pre);
				holder.select.setVisibility(View.VISIBLE);
			} else {
				holder.select.setVisibility(View.GONE);
			}
		} else {
			holder.select.setVisibility(View.GONE);
		}
		
		holder.leaf = leaf;
		holder.position = position;
		
		return view;
	}

	class ViewHolder {
		public Leaf leaf;
		public int position;
		public boolean hasBitmap;
		
		public ImageView icon;
		public TextView name;
		public TextView time;
		public TextView size;
		public ImageView select;
	}
}
