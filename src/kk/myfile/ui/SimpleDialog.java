package kk.myfile.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import kk.myfile.R;
import kk.myfile.util.AppUtil;

public class SimpleDialog extends Dialog {
	private TextView mTvContent;
	private final TextView[] mTvButtons = new TextView[4];

	private IDialogClickListener mClickListener;

	public SimpleDialog(Context context) {
		super(context, R.style.simple_dialog);
		init();
	}

	protected void init() {
		setContentView(R.layout.dialog_simple);

		Window window = getWindow();
		LayoutParams lp = window.getAttributes();
		lp.width = Math.min(AppUtil.getDimenInt(R.dimen.dialog_width),
				AppUtil.getScreenWidth(false) * 9 / 10);
		lp.height = Math.min(AppUtil.getDimenInt(R.dimen.dialog_height),
				AppUtil.getScreenHeight(false) * 8 / 10);
		window.setAttributes(lp);

		mTvContent = (TextView) findViewById(R.id.tv_content);

		for (int i = 0; i < mTvButtons.length; i++) {
			int id = AppUtil.getId("id", "tv_btn_" + i);
			mTvButtons[i] = (TextView) findViewById(id);

			final int index = i;
			mTvButtons[i].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mClickListener != null) {
						mClickListener.onClick(SimpleDialog.this, index);
					}
				}
			});
		}
	}

	public void setContent(int resId) {
		mTvContent.setText(resId);
	}

	public void setContent(String text) {
		mTvContent.setText(text);
	}

	public void setButtons(int[] resIds) {
		for (int i = 0; i < mTvButtons.length; i++) {
			if (i < resIds.length) {
				mTvButtons[i].setText(resIds[i]);
				mTvButtons[i].setVisibility(View.VISIBLE);
			} else {
				mTvButtons[i].setVisibility(View.GONE);
			}
		}
	}

	public void setButtons(String[] texts) {
		for (int i = 0; i < mTvButtons.length; i++) {
			if (i < texts.length) {
				mTvButtons[i].setText(texts[i]);
				mTvButtons[i].setVisibility(View.VISIBLE);
			} else {
				mTvButtons[i].setVisibility(View.GONE);
			}
		}
	}

	public void setClickListener(IDialogClickListener listener) {
		mClickListener = listener;
	}
}
