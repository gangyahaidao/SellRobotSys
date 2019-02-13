package com.qingpu.test;

public class TestIP {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// String s = "lsx[ip:192.168.19.176]的[Physical Memory]...[ip:192.168.19.158]";
		String s = "http://192.168.1.33:8899/group1/M01/1F/49/wKgBw1tNU7KAFkwlAAIAYsoJhiQ829.wav";
		String reg = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)"; //匹配ip的正则
		System.out.println(s.replaceAll(reg, "192.168.0.125"));//替换全部
		System.out.println(s.replaceFirst(reg, "123456"));//替换第一个
	}

}
