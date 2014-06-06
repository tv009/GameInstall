package com.dashi.adwall.utils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeVerify {

	/**
	 * 检测是否过了免费时间
	 * 
	 * @return 如果不是免费时间则返回真,是免费时间则返回假
	 */
	public static boolean checkValid() {

		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
		Calendar publish_Date = Calendar.getInstance();
		try {
			publish_Date.setTime(formatDate.parse(Config.PUBLISH_TIME));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		publish_Date.add(Calendar.DAY_OF_MONTH, Config.APP_FREE_TIME);
		Calendar expireTime = publish_Date;
		Calendar now_Calendar = Calendar.getInstance();

		System.out.println(expireTime.getTime().toString());
		System.out.println(now_Calendar.getTime().toString());
		int temp = now_Calendar.compareTo(expireTime);
		if (temp > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是安全期
	 * 
	 * @return 安全为真,不安全为假
	 */
	public static boolean isSafe() {
		return checkValid() ? true : false;
	}

	public static void main(String[] args) {
		checkValid();
	}
}
