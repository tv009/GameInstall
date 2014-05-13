package cn.gameinstall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class PackageReceiver extends BroadcastReceiver {

	public static Handler handler = null;
	static final String hlddzPackage = "com.qqgame.hlddz";
	static final String tag = "downhlddz";

	@Override
	public void onReceive(Context context, Intent intent) {
		 Log.i("downhlddz", "PackageReceiver:");
		 if(intent.getAction().equals("android.intent.action.PACKAGE_ADDED")){
			 
		 }

	}

}
