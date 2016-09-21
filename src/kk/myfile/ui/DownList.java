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
	private ListView mLvList;
	private DownListAdapter mAdapter;

	public DownList(Context context) {
		super(context, R.style.down_list);
		init();
	}

	protected void init() {
		setContentView(R.layout.list_down);

		mLvList = (ListView) findViewById(R.id.lv_list);
		mAdapter = new DownListAdapter(getContext(), this);
		mLvList.setAdapter(mAdapter);
	}

	public DownListAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void show() {
		Window window = getWindow();
		LayoutParams lp = window.getAttributes();

		int gridHeight = AppUtil.getDimenInt(R.dimen.downlist_grid_height);
		int divider = AppUtil.getDimenInt(R.dimen.downlist_grid_divider);

		lp.width = AppUtil.getDimenInt(R.dimen.downlist_grid_width);
		lp.x = (AppUtil.getScreenWidth(true) - lp.width) / 2;

		lp.height = (gridHeight + divider) * (mAdapter.getCount() + 1)
				- gridHeight / 2 - divider * 2;
		if (lp.height > AppUtil.getScreenHeight(false)) {
			lp.height = AppUtil.getScreenHeight(false);
		}
		lp.y = -(AppUtil.getScreenHeight(false) - lp.height) / 2;

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
