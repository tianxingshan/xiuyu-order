package com.kongque.util;

import java.util.Arrays;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
	public static String[] addStringToStringArray(String[] array,String addStr) {
		String[] newArray = array;
		if(isNotBlank(addStr)) {
			newArray = Arrays.copyOf(array, array.length+1);
			newArray[newArray.length-1] = addStr;
		}
		return newArray;
	}
}
