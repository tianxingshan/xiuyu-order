package com.kongque.util;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class DateUtil {
	/**
	 * 按指定格式格式化日期
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String formatDate(Date date, String pattern){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		return simpleDateFormat.format(date);
	}

	public static Date minDate(Date date)  {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
		try {
		    return simpleDateFormat.parse(formatDate(date, "yyyyMMdd"));
		} catch (ParseException e) {
		    throw new RuntimeException(e);
		}
	}

	public static Date maxDate(Date date) {
		return DateUtils.addDays(minDate(date), 1);
	}

	/**
	 * 获取当前时间距当日最后一刻相差的毫秒数
	 * @param date
	 * @return
	 */
	public static long getTimeDifference(Date date) {
		
		return maxDate(date).getTime() - date.getTime();
	}
	public static String stringOfDateTime(Date date) {
		Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}
}
