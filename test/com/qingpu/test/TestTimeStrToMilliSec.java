package com.qingpu.test;

import java.util.Date;

import com.qingpu.common.utils.CommonUtils;

public class TestTimeStrToMilliSec {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long startLoopMiliTime = CommonUtils.translateHourStrToMiniSec("15:05");
		Date date = new Date();
		long currentMilli = date.getTime();
		System.out.println((startLoopMiliTime - currentMilli)/1000 + "秒");
	}

}
