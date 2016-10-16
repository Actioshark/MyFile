package kk.myfile.leaf;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import kk.myfile.R;
import kk.myfile.file.ImageUtil.IThumable;
import kk.myfile.util.AppUtil;

public class Apk extends Leaf implements IThumable {
	public static final int COLOR = 0xff00cc00;
	
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
		PackageManager pm = AppUtil.getContext().getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(mPath, PackageManager.GET_ACTIVITIES);
		ApplicationInfo appInfo = info.applicationInfo;
		appInfo.sourceDir = mPath;
		appInfo.publicSourceDir = mPath;
		
		return appInfo.loadIcon(pm);
	}
}
