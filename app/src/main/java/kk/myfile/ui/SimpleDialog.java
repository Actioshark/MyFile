package kk.myfile.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import kk.myfile.R;
import kk.myfile.ui.IDialogClickListener.ClickType;
import kk.myfile.util.AppUtil;

public class SimpleDialog extends Dialog {
	private TextView mTvMessage;
	private final TextView[] mTvButtons = new TextView[5];

	private IDialogClickListener mClickListener;

	public SimpleDialog(Context context) {
		super(context, R.style.simple_dialog);
		init();
	}

	protected void init() {
		setContentView(R.layout.dialog_simple);

		Window window = getWindow();
		if (window == null) {
			return;
		}

		LayoutParams lp = window.getAttributes();
		lp.width = Math.min(AppUtil.getDimenInt(R.dimen.dialog_width),
			AppUtil.getScreenWidth() * 9 / 10);
		lp.height = Math.min(AppUtil.getDimenInt(R.dimen.dialog_height),
			AppUtil.getScreenHeight() * 8 / 10);
		window.setAttributes(lp);

		mTvMessage = findViewById(R.id.tv_message);

		for (int i = 0; i < mTvButtons.length; i++) {
			int id = AppUtil.getId("id", "tv_btn_" + i);
			mTvButtons[i] = findViewById(id);

			final int index = i;
			mTvButtons[i].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mClickListener != null) {
						mClickListener.onClick(SimpleDialog.this, index, ClickType.Click);
					}
				}
			});
		}

		setCanceledOnTouchOutside(true);
		setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface di) {
				if (mClickListener != null) {
					mClickListener.onClick(SimpleDialog.this, -1, ClickType.Click);
				}
			}
		});
	}

	public void setMessage(int resId) {
		mTvMessage.setText(resId);
	}

	public void setMessage(String text) {
		mTvMessage.setText(text);
	}

	public void setButtons(Object... btns) {
		for (int i = 0; i < mTvButtons.length; i++) {
			if (i < btns.length) {
				Object btn = btns[i];
				if (btn instanceof Integer) {
					btn = AppUtil.getString((Integer) btn);
				}

				mTvButtons[i].setText(String.valueOf(btn));
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
