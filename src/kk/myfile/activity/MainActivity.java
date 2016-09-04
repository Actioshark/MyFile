package kk.myfile.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import kk.myfile.R;
import kk.myfile.util.AppUtil;

public class MainActivity extends BaseActivity {
	private final List<TextView> mDirects = new ArrayList<TextView>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		for (int i = 1; ; i++) {
			TextView tv = (TextView) findViewById(AppUtil.getId("id", "tv_dir_" + i));
			if (tv == null) {
				break;
			}
			mDirects.add(tv);
			
			tv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(MainActivity.this, DirectActivity.class);
					startActivity(intent);
				}
			});
		}
	}
}
