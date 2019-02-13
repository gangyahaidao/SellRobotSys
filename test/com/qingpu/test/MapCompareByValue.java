package com.qingpu.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MapCompareByValue {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        Map<String, String> map = new HashMap<String, String>();
        map.put("a", "ccccc");
        map.put("c", "aaaaa");
        map.put("b", "bbbbb");
        map.put("d", "ddddd");
        
        List<Map.Entry<String,String>> list = new ArrayList<Map.Entry<String,String>>(map.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<String,String>>() {
            //升序排序
            public int compare(Entry<String, String> o1, Entry<String, String> o2)
            {
                return o1.getValue().compareTo(o2.getValue());
            }
            
        });
        
        for(Map.Entry<String,String> mapping:list){ 
               System.out.println(mapping.getKey()+":"+mapping.getValue()); 
        }
	}

}
