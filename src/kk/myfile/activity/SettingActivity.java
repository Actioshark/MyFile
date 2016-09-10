package kk.myfile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import kk.myfile.R;

public class SettingActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setting);
		
		// 返回
		View menu = findViewById(R.id.ll_menu);
		menu.findViewById(R.id.iv_back)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		
		// 排序
		findViewById(R.id.ll_sort)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SortActivity.class);
				startActivity(intent);
			}
		});
	}
}
