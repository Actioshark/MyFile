package kk.myfile.activity;

import java.util.List;

import kk.myfile.R;
import kk.myfile.adapter.DetailAdapter;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;

public class DetailActivity extends BaseActivity {
	public static final String KEY_PATH = "detail_path";
	public static final String KEY_INDEX = "detail_index";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		List<String> paths = getIntent().getStringArrayListExtra(KEY_PATH);
		int index = getIntent().getIntExtra(KEY_INDEX, 0);
		
		setContentView(R.layout.activity_detail);
		
		ViewPager vp = (ViewPager) findViewById(R.id.vp_list);
		DetailAdapter adapter = new DetailAdapter(this);
		adapter.setDataList(paths);
		vp.setAdapter(adapter);
		vp.setCurrentItem(index);
		
		findViewById(R.id.iv_back)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}
}
