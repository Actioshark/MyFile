package kk.myfile.leaf;

import android.content.Context;
import android.content.Intent;

import kk.myfile.R;
import kk.myfile.activity.ZipActivity;

public class Zip extends Leaf {
	public static final int COLOR = 0xff00cccc;

	public Zip(String path) {
		super(path);
	}

	@Override
	public int getIcon() {
		return R.drawable.file_zip;
	}
	
	@Override
	public void open(Context context, boolean forceSelect) {
		Intent intent = new Intent(context, ZipActivity.class);
		intent.putExtra(ZipActivity.KEY_PATH, mPath);
		context.startActivity(intent);
	}
}
