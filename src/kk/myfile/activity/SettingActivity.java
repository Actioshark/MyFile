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
		findViewById(R.id.ll_menu)
		.findViewById(R.id.iv_back)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		
		// 目录文件排序
		findViewById(R.id.ll_sort_direct)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingSortActivity.class);
				intent.putExtra(SettingSortActivity.KEY_CLASSIFY, Classify.Direct.name());
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
		
		// 目录文件列表视图
		findViewById(R.id.ll_list_style_direct)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingListStyleActivity.class);
				intent.putExtra(SettingSortActivity.KEY_CLASSIFY, Classify.Direct.name());
				startActivity(intent);
			}
		});
		
		// 分类文件列表视图
		findViewById(R.id.ll_list_style_type)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingListStyleActivity.class);
				intent.putExtra(SettingSortActivity.KEY_CLASSIFY, Classify.Type.name());
				startActivity(intent);
			}
		});
		
		// 大/最近文件列表视图
		findViewById(R.id.ll_list_style_big_recent)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingActivity.this, SettingListStyleActivity.class);
				intent.putExtra(SettingSortActivity.KEY_CLASSIFY, Classify.BigRecent.name());
				startActivity(intent);
			}
		});
		
		// 隐藏文件
		findViewById(R.id.ll_hidden)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO
			}
		});
		
		// 大文件数量限制
		findViewById(R.id.ll_num_limit_big)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO
			}
		});

		// 最近文件数量限制
		findViewById(R.id.ll_num_limit_recent)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO
			}
		});
	}
}
