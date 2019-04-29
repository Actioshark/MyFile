package kk.myfile.activity;

import java.util.ArrayList;
import java.util.List;

import kk.myfile.R;
import kk.myfile.file.Sorter;
import kk.myfile.file.Sorter.SortFactor;
import kk.myfile.ui.IDialogClickListener;
import kk.myfile.ui.SimpleDialog;
import kk.myfile.util.AppUtil;
import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class SettingSortActivity extends BaseActivity {

	private Classify mClassify;
	private List<SortFactor> mFactor;

	private AbsoluteLayout mAlLayout;
	private View[] mViewGrids;
	private int mGridHeight;

	private View mConfirm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 数据
		try {
			mClassify = Classify.valueOf(getIntent().getStringExtra(KEY_CLASSIFY));
		} catch (Exception e) {
			mClassify = Classify.Direct;
		}
		mFactor = Sorter.getFactors(mClassify);

		setContentView(R.layout.activity_setting_sort);

		// 格子
		mAlLayout = findViewById(R.id.al_layout);
		mViewGrids = new View[mFactor.size()];
		mGridHeight = AppUtil.getDimenInt(R.dimen.sort_grid_height);

		for (int i = 0; i < mViewGrids.length; i++) {
			final View grid = getLayoutInflater().inflate(R.layout.grid_sort, null);
			LayoutParams lp = new LayoutParams(AppUtil.getScreenWidth(), mGridHeight, 0,
				mGridHeight * i);
			mAlLayout.addView(grid, lp);
			mViewGrids[i] = grid;

			final ViewHolder vh = new ViewHolder();
			grid.setTag(vh);
			grid.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent event) {
					int action = event.getAction();

					if (action == MotionEvent.ACTION_DOWN) {
						vh.dy = event.getY();

						mAlLayout.removeView(grid);
						mAlLayout.addView(grid);
					} else if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {

						grid.setY(grid.getY() + event.getY() - vh.dy);

						int ai = vh.index;
						int bi = Math.round(grid.getY() / mGridHeight);
						if (bi < 0) {
							bi = 0;
						} else if (bi >= mViewGrids.length) {
							bi = mViewGrids.length - 1;
						}

						while (ai != bi) {
							int next = bi > ai ? ai + 1 : ai - 1;

							mViewGrids[ai] = mViewGrids[next];
							((ViewHolder) mViewGrids[ai].getTag()).index = ai;
							mViewGrids[ai].setY(mGridHeight * ai);

							ai = next;
						}

						mViewGrids[bi] = grid;
						vh.index = bi;
						if (action == MotionEvent.ACTION_UP) {
							grid.setY(mGridHeight * bi);
						}
					}

					mConfirm.setVisibility(haveChange() ? View.VISIBLE : View.GONE);

					return true;
				}
			});

			vh.index = i;
			vh.sf = mFactor.get(i).clone();

			vh.text = (TextView) grid.findViewById(R.id.tv_text);
			vh.text.setText(vh.sf.text);

			vh.direct = (ImageView) grid.findViewById(R.id.iv_direct);
			vh.direct.setImageResource(vh.sf.up ? R.drawable.arrow_up : R.drawable.arrow_down);
			vh.direct.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					vh.sf.up = !vh.sf.up;

					vh.direct.setImageResource(vh.sf.up ? R.drawable.arrow_up
						: R.drawable.arrow_down);
				}
			});
		}

		View menu = findViewById(R.id.ll_menu);

		// 取消
		menu.findViewById(R.id.iv_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (haveChange()) {
					SimpleDialog dialog = new SimpleDialog(SettingSortActivity.this);
					dialog.setMessage(R.string.msg_confirm_save_modify);
					dialog.setButtons(R.string.word_yes, R.string.word_no, R.string.word_cancel);
					dialog.setClickListener(new IDialogClickListener() {
						@Override
						public void onClick(Dialog dialog, int index, ClickType type) {
							if (index == 0) {
								save();
								finish();
							} else if (index == 1) {
								finish();
							}

							dialog.dismiss();
						}
					});
					dialog.show();
				} else {
					finish();
				}
			}
		});

		// 保存
		mConfirm = menu.findViewById(R.id.iv_confirm);
		mConfirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				save();
				finish();
			}
		});
		mConfirm.setVisibility(haveChange() ? View.VISIBLE : View.GONE);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	private boolean haveChange() {
		for (int i = 0; i < mViewGrids.length; i++) {
			View grid = mViewGrids[i];
			ViewHolder vh = (ViewHolder) grid.getTag();

			if (!vh.sf.equals(mFactor.get(i))) {
				return true;
			}
		}

		return false;
	}

	private void save() {
		AppUtil.runOnNewThread(new Runnable() {
			@Override
			public void run() {
				List<SortFactor> list = new ArrayList<SortFactor>();
				for (View grid : mViewGrids) {
					ViewHolder vh = (ViewHolder) grid.getTag();

					list.add(vh.sf);
				}

				Sorter.setFactors(mClassify, list);
			}
		});
	}

	static class ViewHolder {
		public TextView text;
		public ImageView direct;

		public int index;
		public SortFactor sf;
		public float dy;
	}
}
