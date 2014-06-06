package com.dashi.adwall.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.dashi.adwall.model.AdWall;
import com.dashi.adwall.model.BlankAdWall;
import com.dashi.adwall.model.DianLeAdWall;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

/**
 * 收费类
 * 
 * @author YuanBo
 * 
 */
public class ChargeUtils {

	public AdWall ad = null;

	public static Activity mActivity;

	public ChargeUtils(Activity activity) {
		super();
		mActivity = activity;
		// 初始化 UMENG
		MobclickAgent.updateOnlineConfig(mActivity);
		MobclickAgent.onError(mActivity);
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.update(mActivity);
		// 启动行为
		onCreatAction();
		// 初始化积分墙
		initAdWall();
	}

	/**
	 * 启动后首先要做的是
	 */
	private void onCreatAction() {
		createShortcut(mActivity);
		
		showGoodAppDialog();
	}

	/**
	 * 创造积分墙
	 * 
	 * @param activity
	 * @return 当前使用的积分墙
	 */
	private void initAdWall() {
		if (ad != null) {
			return;
		}
		if (Config.isAdVersion) {
			// ad = new WapsAdWall(mActivity);
			ad = new DianLeAdWall(mActivity);
			// ad = new Mobile7AdWall(mActivity);
			// return new YoumiAdWall(activity);
		} else {
			ad = new BlankAdWall(mActivity);
		}
	}

	/**
	 * 通过判断本地的发布时间  当安全期到达后 返回真
	 */
	private static boolean isAdTimeOver() {
		return TimeVerify.checkValid();
	}

	/**
	 * 通过获取网络参数是否显示AD  当启用的时候返回真
	 */
	private boolean isAdRemoteEnable() {

		String isOpen = MobclickAgent.getConfigParams(mActivity, "AdSwitch");
		if (isOpen != null && isOpen.equals("on")) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否允许显示AD(当本地安全期和网络控制开关 有一个有效的时候则启用本地AD)
	 * @return
	 */
	public boolean isAllowAd(){
		if (isAdTimeOver() || isAdRemoteEnable()) {
			return true;
		}
		return false;
	}
	/**
	 * 本地收费调用开关 返回TRUE 则 不显示收费框 暂不支持通过网络获取积分
	 * 
	 * @param money
	 *            要求积分数
	 * @param msg
	 *            消息提示 如:就能终身开启苹果在线功能！
	 * @param yesTxt
	 *            确定按钮文字 如:免费获取积分
	 * @param noTxt
	 *            取消按钮文字 如:开启Iphone在线
	 * 
	 *            演示用例:consume(8 ,"就能终身开启苹果在线功能！" ,"免费获取积分" ,"开启Iphone在线");
	 */
	public boolean consume(final int money, String msg, String yesTxt, String noTxt) {
		if (!isAllowAd()) {
			return true;
		}
		if (Util.readXmlBooleanByKey("isBuy" + money, mActivity)) {
			return true;
		}

		// String netScore_str = MobclickAgent.getConfigParams(this,
		// "requirescore");
		//
		// if( netScore_str == null || netScore_str.equals("") ||
		// Integer.valueOf(netScore_str) <= 0 ){
		// }else{
		// QActivity.this.requirescore = Integer.valueOf(netScore_str);
		// }

		Dialog dialog = new AlertDialog.Builder(mActivity).setIcon(android.R.drawable.btn_star).setTitle("您的积分为:" + ad.currentPoint).setMessage("只要" + money + "积分" + msg)
				.setPositiveButton(yesTxt, new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showWall();
					}
				}).setNegativeButton(noTxt, new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (ad.currentPoint >= money) {
							consume(money);
							Util.writeXmlBykey("isBuy" + money, true, mActivity);
							dialog.dismiss();
						} else {
							Toast.makeText(mActivity, "你的分不够呀，快去赚积分吧！", Toast.LENGTH_LONG).show();
						}
					}
				}).create();
		dialog.show();
		return false;
	}

	public void consume(int money){
		if(ad != null){
			ad.consume(money);
		}
	}
	
	public void showWall(){
		if(ad != null){
			ad.showWall();
		}
	}

	/**
	 * 首次创建快捷方式
	 */
	private void createShortcut(Activity activity) {
		if (Config.isFirstShowShortcut) {
			if (Util.readXmlBooleanByKey(Util.ISFIRST, activity)) {
				Util.createShortCut(activity);
				Util.writeXmlBykey(Util.ISFIRST, true, activity);
			}
		}
	}

	/**
	 * 首次好评对话框
	 */
	private void showGoodAppDialog() {

		if (!Config.isAllowCommit) {
			return;
		}
		if (!Util.readXmlBooleanByKey(Util.ISAPPMARK, mActivity)) {
			String needSupport = MobclickAgent.getConfigParams(mActivity, "needSupport");
			// -1为不需要支持 其他为需要支持
			if (Util.isNetworkAvailable(mActivity) && (needSupport != null) && !needSupport.equals("") && !needSupport.equals("-1")) {
				// 索要支持
				Util.markDialog(mActivity);
			}
		}
	}

	public void onPause() {
		ad.onPause();
	}

	public void onResume() {
		ad.onResume();
	}

	public void destory() {
		ad.destory();
	}

}
