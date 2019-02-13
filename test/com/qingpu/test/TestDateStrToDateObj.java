package com.qingpu.test;

import java.util.Date;

import com.qingpu.common.utils.CommonUtils;

public class TestDateStrToDateObj {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Date dd = CommonUtils.translateDatePickerStrToDate("2018-11-14T16:00:00.000Z");
		System.out.println("--dd = " + dd.toString());
	}

}
