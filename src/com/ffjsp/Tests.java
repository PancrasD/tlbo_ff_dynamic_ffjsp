package com.ffjsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class Tests {
     
	
	@Test
	public void testMethod() {
		 Integer a[]= {0,1,2,3,4};
		 List<Integer> list=new ArrayList<>(Arrays.asList(a));
		 System.out.println(list);
		 process(list);
		 System.out.println(list);
	 }

	public  void process(List<Integer> list) {
		
		list.remove(0);
		list.remove(0);
	}
}
