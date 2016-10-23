package kk.myfile.activity;

import java.io.File;

import kk.myfile.R;
import kk.myfile.adapter.DetailAdapter;
import kk.myfile.file.FileUtil;
import kk.myfile.file.ImageUtil.IThumable;
import kk.myfile.leaf.Leaf;
import kk.myfile.util.AppUtil;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class DetailActivity extends BaseActivity {
	public static final String KEY_PATH = "detail_path";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String path = getIntent().getStringExtra(KEY_PATH);
		File file;
		try {
			file = new File(path);
			if (file.exists() == false) {
				throw new Exception();
			}
		} catch (Exception e) {
			finish();
			return;
		}
		final Leaf leaf = FileUtil.createLeaf(file);
		
		setContentView(R.layout.activity_detail);
		
		final ImageView thum = (ImageView) findViewById(R.id.iv_thum);
		thum.setImageResource(leaf.getIcon());
		if (leaf instanceof IThumable) {
			AppUtil.runOnNewThread(new Runnable() {
				@Override
				public void run() {
					try {
						thum.setImageDrawable(((IThumable) leaf).getThum(40960, 40960));
					} catch (Exception e) {
					}
				}
			});
		}
		
		ListView lv = (ListView) findViewById(R.id.lv_list);
		DetailAdapter adapter = new DetailAdapter(this, leaf.getDetail());
		lv.setAdapter(adapter);
		
		findViewById(R.id.iv_back)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}
}
