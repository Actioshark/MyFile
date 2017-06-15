package kk.myfile.activity;

import kk.myfile.file.FileUtil;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Setting;

import android.annotation.SuppressLint;
import android.app.Application;
import android.widget.Toast;

public class App extends Application {
	private static Toast sToast;

	@SuppressLint("ShowToast")
	@Override
	public void onCreate() {
		super.onCreate();

		sToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
		sToast.setText("");
		sToast.setDuration(Toast.LENGTH_SHORT);

		AppUtil.init(this);
		Setting.init(this);
		FileUtil.init(this);
	}

	public static void showToast(int resId, Object... args) {
		sToast.setText(AppUtil.getString(resId, args));
		sToast.show();
	}

	public static void showToast(String text, Object... args) {
		sToast.setText(String.format(text, args));
		sToast.show();
	}
}
