package cn.gameinstall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {
	private static final String TAG = "SplashActivity";
	private static final boolean DEBUG = true;
	
	private static final int DELAYMILLIS=1500;
	private static final int MSG_ID_STARTMAIN = 1;
//	private static final boolean isMainStart=false;
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if (msg.what==MSG_ID_STARTMAIN){
				closeSplash();
			}
			super.handleMessage(msg);
		}
	};
	@Override
	protected void onCreate(Bundle bundle) {
		if (DEBUG) Log.d(TAG, "onCreate() "+bundle);
//		Intent service = new Intent(this, MmsService.class);
//		this.startService(service);
		super.onCreate(bundle);
		
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

//		MobclickAgent.update(this);
		setContentView(R.layout.splash);
		
		//创建快捷方式
//		if(!Utils.readXmlBooleanByKey(Utils.ISFIRST, this)){
//			Utils.createShortCut(this);
//		}
		
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			try {
				closeSplash();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			break;
		}
		return super.onTouchEvent(event);
	}
	private void closeSplash() {
		this.finish();
		Intent intent=new Intent(this,IntroActivity.class);
		this.startActivity(intent);
	}
	@Override
	protected void onPause() {
		handler.removeMessages(MSG_ID_STARTMAIN);
		super.onPause();
	}
	@Override
	protected void onResume() {
		handler.sendEmptyMessageDelayed(MSG_ID_STARTMAIN,DELAYMILLIS);
		super.onResume();
	}
	
}
