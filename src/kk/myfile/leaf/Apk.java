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
	public Drawable getThum(int width, int height) {
		try {
			PackageManager pm = AppUtil.getContext().getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo(mPath, PackageManager.GET_ACTIVITIES);
			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.sourceDir = mPath;
			appInfo.publicSourceDir = mPath;
			
			return appInfo.loadIcon(pm);
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		return null;
	}
}
