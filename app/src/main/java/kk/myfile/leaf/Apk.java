package kk.myfile.leaf;

import java.util.List;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import kk.myfile.R;
import kk.myfile.adapter.DetailItemAdapter.Data;
import kk.myfile.file.ImageUtil.IThumable;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;

public class Apk extends Leaf implements IThumable {
	public static final int COLOR = 0xff00cc00;
	
	private boolean mInstalled = false;

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
	public List<Data> getDetail() {
		List<Data> list = super.getDetail();

		try {
			PackageManager pm = AppUtil.getContext().getPackageManager();
			PackageInfo pi = pm.getPackageArchiveInfo(mPath, PackageManager.GET_ACTIVITIES);
			ApplicationInfo ai = pi.applicationInfo;
			ai.sourceDir = mPath;
			ai.publicSourceDir = mPath;

			putDetail(list, 2, R.string.word_app_name, pi.applicationInfo.loadLabel(pm));
			putDetail(list, 2, R.string.word_package_name, pi.packageName);
			putDetail(list, 2, R.string.word_version_code, pi.versionCode);
			putDetail(list, 2, R.string.word_version_name, pi.versionName);
			putDetail(list, 2, R.string.word_state, AppUtil
				.getString(isInstalled(true) ? R.string.msg_already_installed : R.string.msg_not_installed));
		} catch (Exception e) {
			Logger.print(null, e);
		}

		return list;
	}
	
	@SuppressWarnings("deprecation")
	public boolean isInstalled(boolean real) {
		if (real) {
			try {
				PackageManager pm = AppUtil.getContext().getPackageManager();
				PackageInfo pi = pm.getPackageArchiveInfo(mPath, PackageManager.GET_ACTIVITIES);
				pm.getPackageInfo(pi.packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
				return true;
			} catch (Exception e) {
			}
			
			return false;
		} else {
			return mInstalled;
		}
	}
	
	public void setInstalled(boolean installed) {
		mInstalled = installed;
	}
}
