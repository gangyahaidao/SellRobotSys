package com.qingpu.test;

import java.security.SecureRandom;
import java.util.Random;

public class TestRandomInt {

	public static void main(String[] args) {
		// TODO Auto-generated method stub		
		
		for (int i = 0; i < 10; i++) {
			Random RANDOM = new SecureRandom();		
			int num = RANDOM.nextInt(100)+1;
			System.out.println("num = " + num);
		}
	}

}
