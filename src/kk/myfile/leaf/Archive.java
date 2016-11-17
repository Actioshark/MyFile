package kk.myfile.leaf;

import android.content.Context;
import android.content.Intent;

import kk.myfile.R;
import kk.myfile.activity.ArchiveActivity;

public class Archive extends Leaf {
	public static final int COLOR = 0xff00cccc;

	public Archive(String path) {
		super(path);
	}

	@Override
	public int getIcon() {
		return R.drawable.file_archive;
	}
	
	@Override
	public void open(Context context, boolean forceSelect) {
		Intent intent = new Intent(context, ArchiveActivity.class);
		intent.putExtra(ArchiveActivity.KEY_PATH, mPath);
		context.startActivity(intent);
	}
}
