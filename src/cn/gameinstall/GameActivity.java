package cn.gameinstall;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsoluteLayout;
import android.widget.Button;

public class GameActivity extends Activity {
	private static final String DL_ID = "downloadId";
	static final String First_Start_From_Downloader = "First_Start_From_Downloader";
	public static final String GAME_NOTIFICATION_ACTION = "com.qqgame.gamenotification";
	private static final Exception IOException = null;
	public static final int MSG_DWFAIL = 3;
	public static final int MSG_DWPACKSIZE = 1;
	public static final int MSG_NOTIFICATION_CLICKED = 6;
	public static final int MSG_PACKAGE_ADDED = 5;
	public static final int MSG_PROGRESS = 2;
	public static final int MSG_UPDATE = 4;
	public static final String QUIT_ID = "QUIT_ID";
	static final String tag = "downhlddz";
	int Channel = 0;
	String ChannelFile;
	String absoluteDownloadPath;
	String apkName = "hlddz.apk";
	Cursor cursor;
	private AlertDialog downloadDialog = null;
	final String downloadPath = "/updatehlddz/newApk/";
	Thread downloadThread = null;
	boolean downok = true;
	long dowsize = 0L;
	private SharedPreferences prefs;
	private int progress = 0;
//	DownloadManager.Query query = new DownloadManager.Query();
	DownloadManager manager;
//	QuitBroadcastReciver quitBroadcastReciver = new QuitBroadcastReciver(null);
	Button startBtn;
	State state = State.Resume;
	public final String strACT = "android.intent.action.PACKAGE_ADDED";
	long totalsize = 0L;
	boolean updateTotalSize = false;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(1);
		getWindow().setFlags(1024, 1024);
		Display display = getWindowManager().getDefaultDisplay();
		int height = display.getHeight();
		int width = display.getWidth();
		Log.i(tag, "screenWidth:" + width + " screenHeight" + height);
		AbsoluteLayout absLayout = new AbsoluteLayout(this);
		AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(-1, -1, 0, 0);
		View bgView = new View(this);
		bgView.setBackgroundResource(R.drawable.back);
		absLayout.addView(bgView, params);
		startBtn = new Button(this);;
		float f1 = height / 800.0F;
		float f2 = width / 480.0F;
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.btn_background);
		Log.i(tag, " bitmapPic.getWidth():" + bitmap.getWidth()+ " bitmapPic.getHeight():" + bitmap.getHeight());
		startBtn.setFocusable(true);
		startBtn.setBackgroundResource(R.drawable.btn_background);
		startBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i(tag, "启动 click");
				startDownload();
				
			}
		});
		Log.i(tag, "fx:" + f2 + " fy:" + f1);
		int k = (int) (106.0F * f2);
		int m = (int) (548.0F * f1);
		int n = (int) (266.0F * f2);
		int i1 = (int) (132.0F * f1);
		AbsoluteLayout.LayoutParams localLayoutParams2 = new AbsoluteLayout.LayoutParams(
				n, i1, k, m);
		Log.i(tag, "x:" + k + " y:" + m + " width:" + n + " height:"
				+ i1);
		absLayout.addView(startBtn, localLayoutParams2);
		setContentView(absLayout);
//		PackageReceiver.handler = handler;
//		Channel = getChannel();
//		getChannelFile();
//		readChannel();
//		writeChannel();
//		manager = ((DownloadManager) getSystemService("download"));
//		prefs = PreferenceManager.getDefaultSharedPreferences(this);
//		registerReceiver(onComplete, new IntentFilter(
//				"android.intent.action.DOWNLOAD_COMPLETE"));
//		registerReceiver(onNotification, new IntentFilter(
//				"android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED"));
//		if (isAppInstalled("com.qqgame.hlddz")){
//				openApp("com.qqgame.hlddz", "");
//			}
//			}
//			if ((prefs.contains("downloadId"))
//					&& (prefs.getLong("downloadId", -1L) >= 0L)) {
//				lastDownload = prefs.getLong("downloadId", 0L);
//				showDownloadDialog();
//				queryDownloadStatus();
//			}
//	}
//		setContentView(R.layout.activity_game_downloader);
	}

	private boolean isAppInstalled(String uri){
		PackageManager pm = getPackageManager();
		boolean installed =false;
		try{
		pm.getPackageInfo(uri,PackageManager.GET_ACTIVITIES);
		installed =true;
		}catch(PackageManager.NameNotFoundException e){
		installed =false;
		}
		return installed;
	}

	protected void startDownload() {
	    Log.v(tag, "startDownload");
	    if ((!this.prefs.contains("downloadId")) || (this.prefs.getLong("downloadId", -1L) < 0L))
	    {
	      download();
	      return;
	    }
//	    this.lastDownload = this.prefs.getLong("downloadId", 0L);
	    showDownloadDialog();
	    queryDownloadStatus();
		
	}

	private void queryDownloadStatus() {
		// TODO Auto-generated method stub
		
	}

	private void showDownloadDialog() {
		// TODO Auto-generated method stub
		
	}

	private void download() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	String failMessage(int exceptionId) {
		switch (exceptionId) {
		case 1003:
		case 1008:
			return "下载失败，无法重新开始任务";
		case 1007:
			return "下载失败，设备没找到";
		case 1009:
			return "文件已存在";
		case 1001:
			return "下载失败，文件错误";
		case 1004:
			return "下载失败，http数据错误";
		case 1006:
			return "下载失败，空间不足";
		case 1005:
			return "下载失败，重定向太多";
		case 1002:
			return "下载失败，未处理http";
		case 1000:
			return "下载失败，未知错误";
		default:
			return "下载失败，未知错误";
		}
	}

	public enum State {
		Resume, Pause, Install
	}
}
