package kk.myfile.leaf;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import kk.myfile.R;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;

public class Apk extends Leaf {
	public Apk(String path) {
		super(path);
	}
	
	@Override
	public int getIcon() {
		return R.drawable.file_apk;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void getThum(int width, int height, final IThumListenner listenner) {
		AppUtil.runOnNewThread(new Runnable() {
			public void run() {
				try {
					PackageManager pm = AppUtil.getContext().getPackageManager();
					PackageInfo info = pm.getPackageArchiveInfo(mPath, PackageManager.GET_ACTIVITIES);
					ApplicationInfo appInfo = info.applicationInfo;
					appInfo.sourceDir = mPath;
					appInfo.publicSourceDir = mPath;
					
					Drawable drawable = appInfo.loadIcon(pm);
					listenner.onThumGot(drawable);
				} catch (Exception e) {
					Logger.print(null, e, mPath);
				}
			}
		});
	}
}
