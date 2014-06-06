package com.dashi.adwall.model;

import android.app.Activity;

import com.aardvarks.Bfedsd;
import com.aardvarks.GetTotalMoneyListener;
import com.aardvarks.SpendMoneyListener;

/**
 * download_notification.xml ����ļ���layout�±��뿽��,��Ҫע��
 * @author Administrator
 *
 */
public class DianLeAdWall extends AdWall implements GetTotalMoneyListener, SpendMoneyListener {

	// ���Ƶ�APPkey
    static String appKey = "0f3baaad768402d29dcc9680a480af01";
	
	public DianLeAdWall(Activity activity) {
		super(activity);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
//		Dianle.initDianleContext(mActivity, appKey);
		Bfedsd.initGoogleContext(mActivity, appKey, "gfan");
		
	}

	@Override
	public void showWall() {
		// TODO Auto-generated method stub
		Bfedsd.showOffers(mActivity);
	}

	@Override
	public void consume(int i) {
		// TODO Auto-generated method stub
		Bfedsd.spendMoney(mActivity,i, this);
	}

	@Override
	public void updatePoint() {
		// TODO Auto-generated method stub
		Bfedsd.getTotalMoney(mActivity,this);
	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getTotalMoneyFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getTotalMoneySuccessed(String arg0, long amount) {
		currentPoint = (int) amount;
		
	}

	@Override
	public void spendMoneyFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void spendMoneySuccess(long arg0) {
		// TODO Auto-generated method stub
		
	}

}