package com.qingpu.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MapCompareInteger {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Map<String, Integer> userTotalFeeMap = new HashMap<String, Integer>();
		
		userTotalFeeMap.put("2222", 120);
		userTotalFeeMap.put("1111", 300);
		userTotalFeeMap.put("3333", 500);
		userTotalFeeMap.put("4444", 120);
		
		List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String,Integer>>(userTotalFeeMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String,Integer>>() {
            //降序排序
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2)
            {
            	int o1Value = o1.getValue();
            	int o2Value = o2.getValue();
            	if(o1Value > o2Value) {
            		return -1;
            	}else if(o1Value < o2Value) {
            		return 1;
            	}else{
            		return 0;
            	}
            }	            
        });
		
		for(Map.Entry<String,Integer> mapping:list){ 
            System.out.println(mapping.getKey()+":"+mapping.getValue()); 
		}
	}

}
