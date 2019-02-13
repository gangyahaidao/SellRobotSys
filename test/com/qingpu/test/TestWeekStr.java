package com.qingpu.test;

import java.util.Calendar;
import java.util.Date;

import com.qingpu.common.utils.CommonUtils;

public class TestWeekStr {

	public static String dateToWeekDay(Date date) {
		String[] weekDays = { "周天", "周一", "周二", "周三", "周四", "周五", "周六"};
		Calendar cal = Calendar.getInstance(); // 获得一个日历
		cal.setTime(date);
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
		System.out.println("--w = " + w);
        if (w < 0)
            w = 0;
        return weekDays[w];                
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(CommonUtils.dateToWeekDayInt(new Date()));
		System.out.println(dateToWeekDay(new Date()));
	}

}
