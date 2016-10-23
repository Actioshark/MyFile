package kk.myfile.leaf;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import kk.myfile.R;
import kk.myfile.util.AppUtil;
import kk.myfile.util.Logger;
import kk.myfile.util.MathUtil;
import kk.myfile.util.Setting;

public abstract class Leaf {
	protected final String mPath;
	
	private Object mTag;
	
	public Leaf(String path) {
		if (path == null || path.startsWith("/") == false) {
			mPath = "/";
		} else {
			mPath = path;
		}
	}
	
	public File getFile() {
		return new File(mPath);
	}
	
	public String getPath() {
		return mPath;
	}
	
	public void setTag(Object tag) {
		mTag = tag;
	}
	
	public Object getTag() {
		return mTag;
	}
	
	public abstract int getIcon();
	
	public abstract int getTypeName();
	
	protected void putDetail(Map<String, String> map, int key, Object value, Object... args) {
		if (value == null) {
			return;
		}
		
		String v = String.valueOf(value);
		if (v == null || v.length() < 1) {
			return;
		}
		
		if (args != null && args.length > 0) {
			v = String.format(v, args);
			
			if (v == null || v.length() < 1) {
				return;
			}
		}
			
		map.put(AppUtil.getString(key), v);
	}
	
	public Map<String, String> getDetail() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		int idx = mPath.lastIndexOf('/');
		String parent = mPath.substring(0, idx);
		String name = mPath.substring(idx + 1);
		File file = new File(mPath);
		
		putDetail(map, R.string.word_name, name);
		putDetail(map, R.string.word_parent, parent);
		try {
			putDetail(map, R.string.word_real_path, file.getCanonicalPath());
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		putDetail(map, R.string.word_type, AppUtil.getString(getTypeName()));
		
		try {
			putDetail(map, R.string.word_size, "%s B",
				MathUtil.insertComma(file.length()));
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		try {
			Date date = new Date(file.lastModified());
			DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Setting.LOCALE);
			putDetail(map, R.string.word_modify_time, df.format(date));
		} catch (Exception e) {
			Logger.print(null, e);
		}
		
		return map;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Leaf == false) {
			return false;
		}
		
		Leaf other = (Leaf) obj;
		return mPath.equals(other.mPath);
	}
}
