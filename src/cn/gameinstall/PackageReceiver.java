package cn.gameinstall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PackageReceiver extends BroadcastReceiver {

	public static Handler handler = new Handler();
	static final String hlddzPackage = "com.qqgame.hlddz";
	static final String tag = "downhlddz";

	@Override
	public void onReceive(Context context, Intent intent) {
		 Log.i("downhlddz", "PackageReceiver:");
		 if(intent.getAction().equals("android.intent.action.PACKAGE_ADDED")){
			 String pkgName = intent.getDataString().substring(8);
		      Log.i("downhlddz", "installed:" + pkgName);
		      Log.i("downhlddz", "hlddzPackage:com.qqgame.hlddz");
		     
		     if("com.qqgame.hlddz".equals(pkgName)){
		    	  Log.i("downhlddz", "packageName is hlddz");
		    	  Message msg = new Message();
		          msg.what = GameDownActivity.MSG_PACKAGE_ADDED;
		          Log.i("downhlddz", "handler:" + handler);
		          if (handler != null)
		          {
		            handler.sendMessage(msg);
		            Log.i("downhlddz", "sendMessage:MSG_PACKAGE_ADDED");
		          }
		     }else if(intent.getAction().equals("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED")){
		    	  long l = intent.getLongExtra("extra_download_id", 0L);
		    	  Log.v("downhlddz", " ACTION_NOTIFICATION_CLICKED id : " + l);
		     }
		 }

	}

}
