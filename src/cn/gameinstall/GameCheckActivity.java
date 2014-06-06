package cn.gameinstall;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;


@SuppressLint("HandlerLeak")
public class GameCheckActivity extends Activity {
	
	String[] safeInfo = {"更新最新病毒库","正在检测系统环境","正在检测SD卡容量","查找官网最新版本号","正在探测最近CDN加速下载点"};
	int duration = 1000;
	int curr_info = 0;
	EditText et_show;
	
	private static final int MSG_INFOSHOW = 4;
	private static final int MSG_UPDATE = 5;
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch(msg.what){
			case MSG_INFOSHOW:
				String showStr;
				int infoLength = safeInfo.length;
				if(curr_info == infoLength-1){
					showStr = safeInfo[curr_info];
					et_show.setText(showStr+"\n"+et_show.getText());
					handler.sendEmptyMessageDelayed(MSG_UPDATE, duration);
					curr_info = 0;
					return;
				}else{
					showStr = safeInfo[curr_info];
					et_show.setText(showStr+"\n"+et_show.getText());
					handler.sendEmptyMessageDelayed(MSG_INFOSHOW, duration);
				}
				curr_info ++;
				break;
			case MSG_UPDATE:
				//显示 两个按钮  CDN加速下载  普通下载
				break;
			}
			
			
		}
		
	};
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check);
		
		et_show = (EditText) findViewById(R.id.et_show);
		
		handler.sendEmptyMessage(MSG_INFOSHOW);
	}
	
}
