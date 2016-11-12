package kk.myfile.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import kk.myfile.R;
import kk.myfile.util.AppUtil;

public abstract class BaseActivity extends Activity {
	public static final int REQ_COPY_TO = 1;
	public static final int REQ_CUT_TO = 2;
	public static final int REQ_COMPRESS_TO = 3;
	public static final int REQ_EXTRACT_TO = 4;
	
	public static final String KEY_PATH = "base_path";
	public static final String KEY_CLASSIFY = "base_classify";

	public static enum Classify {
		Direct, Type, Big, Recent,
	}

	public static enum Mode {
		Normal, Select,
	}

	protected View mStatusBar;

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

			mStatusBar = new View(this);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, AppUtil
				.getStatusBarHeight());
			params.gravity = Gravity.TOP;
			mStatusBar.setLayoutParams(params);
			mStatusBar.setBackgroundColor(AppUtil.getColor(R.color.main_bg));

			ViewGroup decor = (ViewGroup) getWindow().getDecorView();
			decor.addView(mStatusBar);
		}
	}
}
