package com.dashi.adwall.model;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;

/**
 * 广告墙抽象类 初始化,展示,消费,更新积分,销毁
 * 
 * @author YuanBo
 * 
 */

public abstract class AdWall {
	Activity mActivity;
	/**
	 * 当前用户可用积分
	 */
	public int currentPoint = 0;

	public AdWall(Activity activity) {
		super();
		this.mActivity = activity;
		init();
	}

	/**
	 * 初始化
	 */
	public abstract void init();

	/**
	 * 展示积分墙
	 */
	public abstract void showWall();

	/**
	 * 消费i积分
	 */
	public abstract void consume(int i);

	/**
	 * 从服务器获取数据更新积分
	 */
	public abstract void updatePoint();

	/**
	 * 销毁
	 */
	public abstract void destory();

	public void onPause() {
		MobclickAgent.onPause(mActivity);
	}

	public void onResume() {
		MobclickAgent.onResume(mActivity);
		updatePoint();
	}

}
