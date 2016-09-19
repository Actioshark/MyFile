package kk.myfile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import kk.myfile.R;
import kk.myfile.tree.Sorter.Classify;

public class SettingActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setting);
		
		// 返回
		findViewById(R.id.ll_menu)
		.findViewById(R.id.iv_back)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		
		// 文件夹排序
		findViewById(R.id.ll_sort_tree)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingSortActivity.class);
				intent.putExtra(SettingSortActivity.KEY_CLASSIFY, Classify.Tree.name());
				startActivity(intent);
			}
		});
		
		// 分类文件排序
		findViewById(R.id.ll_sort_type)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingSortActivity.class);
				intent.putExtra(SettingSortActivity.KEY_CLASSIFY, Classify.Type.name());
				startActivity(intent);
			}
		});
		
		// 列表视图
		findViewById(R.id.ll_list_style)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingListStyleActivity.class);
				startActivity(intent);
			}
		});
	}
}
