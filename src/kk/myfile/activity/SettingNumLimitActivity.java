package kk.myfile.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import kk.myfile.R;
import kk.myfile.util.Setting;

public class SettingNumLimitActivity extends BaseActivity {
	public static final int MIN_NUM = 1;
	public static final int MAX_NUM = 9999;
	public static final int DEF_NUM = 100;
	
	private EditText mEtBig;
	private EditText mEtRecent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setting_num_limit);
		
		mEtBig = (EditText) findViewById(R.id.ll_big).findViewById(R.id.et_input);
		mEtBig.setText(String.valueOf(Setting.getBigFileNum()));
		
		mEtRecent = (EditText) findViewById(R.id.ll_recent).findViewById(R.id.et_input);
		mEtRecent.setText(String.valueOf(Setting.getRecentFileNum()));
		
		View menu = findViewById(R.id.ll_menu);
		
		menu.findViewById(R.id.iv_cancel)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		
		menu.findViewById(R.id.iv_confirm)
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Setting.setBigFileNum(Integer.parseInt(mEtBig.getText().toString()));
				Setting.setRecentFileNum(Integer.parseInt(mEtRecent.getText().toString()));
				finish();
			}
		});
	}
}
