package cn.gameinstall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class IntroActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_intro);
	}

	public void StartClick(View v){
		Intent intent = new Intent(this,GameCheckActivity.class);
		startActivity(intent);
//		finish();
	}
}
