package kk.myfile.leaf;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import kk.myfile.R;
import kk.myfile.util.AppUtil;
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
	
	public Map<String, String> getDetail() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		int idx = mPath.lastIndexOf('/');
		String parent = mPath.substring(0, idx);
		String name = mPath.substring(idx + 1);
		File file = new File(mPath);
		
		map.put(AppUtil.getString(R.string.word_name), name);
		map.put(AppUtil.getString(R.string.word_parent), parent);
		try {
			map.put(AppUtil.getString(R.string.word_real_path), file.getCanonicalPath());
		} catch (Exception e) {
		}
		
		map.put(AppUtil.getString(R.string.word_type), AppUtil.getString(getTypeName()));
		
		try {
			String num = String.valueOf(file.length());
			StringBuilder sb = new StringBuilder();
			int len = num.length();
			for (int i = 0; i < len; i++) {
				sb.append(num.charAt(i));
	
				if (i + 1 != len && (len - i) % 3 == 1) {
					sb.append(',');
				}
			}
			map.put(AppUtil.getString(R.string.word_size), String.format(
					Setting.LOCALE, "%s B", sb.toString()));
		} catch (Exception e) {
		}
		
		try {
			Date date = new Date(file.lastModified());
			DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Setting.LOCALE);
			map.put(AppUtil.getString(R.string.word_modify_time), df.format(date));
		} catch (Exception e) {
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
