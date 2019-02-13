package com.qingpu.test;

import com.qingpu.common.utils.CommonUtils;

public class TestHttpGet {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String serverInterfaceUrl = "http://www.g58mall.com/service/app/noLogin/user/client/buyRobotGoodsUseGold?clientId="+"jjjjjjjjjjjjjj"
				+"?orderId="+"ccccccccccccccccccc"+"?count="+(-99)+"?remark="+"无备注";
		String retString = CommonUtils.httpGetStr(serverInterfaceUrl);
		System.out.println("--retString = " + retString);
	}

}
