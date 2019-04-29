package kk.myfile.ui;

import android.app.Dialog;

public interface IDialogClickListener {
	enum ClickType {
		Click, LongClick,
	}

	void onClick(Dialog dialog, int index, ClickType type);
}
