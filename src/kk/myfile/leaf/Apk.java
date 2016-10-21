package kk.myfile.leaf;

import java.util.Map;

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
		PackageInfo pi = pm.getPackageArchiveInfo(mPath, PackageManager.GET_ACTIVITIES);
		ApplicationInfo ai = pi.applicationInfo;
		ai.sourceDir = mPath;
		ai.publicSourceDir = mPath;
		
		return ai.loadIcon(pm);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Map<String, String> getDetail() {
		Map<String, String> map = super.getDetail();
		
		PackageManager pm = AppUtil.getContext().getPackageManager();
		PackageInfo pi = pm.getPackageArchiveInfo(mPath, PackageManager.GET_ACTIVITIES);
		map.put(AppUtil.getString(R.string.word_package_name), pi.packageName);
		
		map.put(AppUtil.getString(R.string.word_version_code), String.valueOf(pi.versionCode));
		map.put(AppUtil.getString(R.string.word_version_name), pi.versionName);
		
		return map;
	}
}
