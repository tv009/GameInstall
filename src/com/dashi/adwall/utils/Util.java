package com.dashi.adwall.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import cn.gameinstall.R;

/**
 * 通用工具类
 * 
 * @author YuanBo
 * 
 */
public class Util {
	public final static String ISEXITTIP = "isExitTip";
	public final static String ISAPPMARK = "isAppMark";
	public final static String ISFIRST = "isFirst";

	/**
	 * 给程序创建一个快捷方式
	 * 
	 * @param activity
	 */
	public static void createShortCut(Activity activity) {
		

		Intent targetIntent = new Intent(activity.getBaseContext(), activity.getClass());
		Intent shortcutIntent = new Intent();
		shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, ShortcutIconResource.fromContext(activity.getBaseContext(), R.drawable.ic_launcher));
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, activity.getString(R.string.app_name)); // activity.getString(R.string.app_name)
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, targetIntent);
		activity.sendBroadcast(shortcutIntent);
	}

	/**
	 * 给软件好评的对话框
	 * 
	 * @param activity
	 */
	public static void markDialog(final Activity activity) {
		AlertDialog.Builder builder0 = new Builder(activity);
		builder0.setMessage(activity.getResources().getString(R.string.app_name) + "是一款优质Android软件,\n为软件打分好评才能免费使用哟!\n亲,谢谢您的支持!");
		builder0.setTitle("请给软件评分");
		builder0.setNegativeButton("五星支持", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				writeXmlBykey(ISAPPMARK, true, activity);
				openAppInMarket(activity.getPackageName(), activity.getBaseContext());
			}
		});
		if(Config.isSupportCanCancle){
			builder0.setPositiveButton("下次吧", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
		}
		builder0.create().show();
	}

	/**
	 * 退出对话框的显示
	 * 
	 * @param activity
	 */
	public static void exitDialog(final Activity activity) {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("退出确认");
		CheckBox cb = new CheckBox(activity);
		cb.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				writeXmlBykey(ISEXITTIP, true, activity);
			}
		});
		cb.setText("不再提示(点菜单键可评分和分享)");
		cb.setTextColor(0xffff0000);
		builder.setMessage("您确定要退出吗?\n\n(如果您喜欢本应用,给我们好评并分享给您好友吧^-^)");
		builder.setView(cb);

		builder.setPositiveButton("确定", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				activity.finish();
			}
		});
		builder.setNeutralButton("分享", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				openAppShare(activity);
			}
		});
		builder.setNegativeButton("五星支持", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				openAppInMarket(activity.getPackageName(), activity.getBaseContext());
			}
		});
		builder.show();

	}

	/**
	 * 通过传入的包名打开对应市场上的应用,让用户进行好评
	 * 
	 * @param packName
	 * @param context
	 */
	public static void openAppInMarket(String packName, Context context) {
		if ((context == null) || (packName == null) || (packName.length() == 0))
			return;

		String str1 = "market://details?id=" + packName;
		try {
			Uri localUri1 = Uri.parse(str1);
			Intent intent1 = new Intent("android.intent.action.VIEW", localUri1);
			intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Log.v("WindowControl", "intent" + intent1);
			context.startActivity(intent1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 打开其他程序进行分享推广
	 * 
	 * @param activity
	 */
	public static void openAppShare(Activity activity) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
		intent.putExtra(Intent.EXTRA_TEXT, "我正在使用<" + activity.getResources().getString(R.string.app_name) + ">,推荐你也来体验一把,相当方便呀!market://details?id=" + activity.getPackageName());
		activity.startActivity(Intent.createChooser(intent, "分享"));
	}

	/**
	 * 读取默认配置文件制定键值的内容
	 * 
	 * @param key
	 * @param activity
	 * @return
	 */
	public static Boolean readXmlBooleanByKey(String key, final Activity activity) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
		Boolean isTrue = settings.getBoolean(key, false);
		return isTrue;

	}

	/**
	 * 写入具体的键与值到默认的配置文件
	 * 
	 * @param key
	 * @param value
	 * @param activity
	 */
	public static void writeXmlBykey(String key, Boolean value, final Activity activity) {
		Editor e = PreferenceManager.getDefaultSharedPreferences(activity).edit();
		e.putBoolean(key, value);
		e.commit();
	}

	
	/**
	 * 判断是否有网络存在
	 * 
	 * @param context
	 * @return 有网则返回TRUE 否则返回FALSE
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mMobile = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean flag = false;
		if ((mWifi != null) && ((mWifi.isAvailable()) || (mMobile.isAvailable()))) {
			if ((mWifi.isConnected()) || (mMobile.isConnected())) {
				flag = true;
			}
		}
		return flag;
	}
}
