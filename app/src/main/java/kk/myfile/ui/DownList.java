package kk.myfile.ui;

import kk.myfile.R;
import kk.myfile.adapter.DownListAdapter;
import kk.myfile.util.AppUtil;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ListView;

public class DownList extends Dialog {
	public static final int POS_START = 0;
	public static final int POS_CENTER = 1;
	public static final int POS_END = 2;

	private ListView mLvList;
	private DownListAdapter mAdapter;

	public DownList(Context context) {
		super(context, R.style.down_list);
		init();
	}

	protected void init() {
		setContentView(R.layout.list_down);

		mLvList = findViewById(R.id.lv_list);
		mAdapter = new DownListAdapter(getContext(), this);
		mLvList.setAdapter(mAdapter);

		setCanceledOnTouchOutside(true);
	}

	public DownListAdapter getAdapter() {
		return mAdapter;
	}

	public void show(int horPos, int verPos, int horPadding, int verPadding) {
		Window window = getWindow();
		if (window == null) {
			return;
		}

		LayoutParams lp = window.getAttributes();

		int screenWidth = AppUtil.getScreenWidth() - horPadding - AppUtil.getPixcel(5);

		lp.width = AppUtil.getDimenInt(R.dimen.downlist_grid_width)
			+ AppUtil.getDimenInt(R.dimen.downlist_gap) * 2;
		if (horPos == POS_START) {
			lp.x = -(screenWidth - lp.width) / 2;
		} else if (horPos == POS_END) {
			lp.x = (screenWidth - lp.width) / 2;
		} else {
			lp.x = 0;
		}

		int gridHeight = AppUtil.getDimenInt(R.dimen.downlist_grid_height);
		int divider = mLvList.getDividerHeight();
		int screenHeight = AppUtil.getScreenHeight() - verPadding - AppUtil.getPixcel(20);

		lp.height = (gridHeight + divider) * mAdapter.getCount() + divider;
		if (lp.height > screenHeight) {
			lp.height = screenHeight;
		}
		if (verPos == POS_START) {
			lp.y = -(screenHeight - lp.height) / 2;
		} else if (verPos == POS_END) {
			lp.y = (screenHeight - lp.height) / 2;
		} else {
			lp.y = 0;
		}

		window.setAttributes(lp);

		super.show();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			dismiss();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}
}
