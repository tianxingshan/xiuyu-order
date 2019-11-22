package com.kongque.constants;

import java.io.File;

import com.kongque.util.PropertiesUtils;

public class Constants {
	
	public static  PropertiesUtils propertiesUtils=new PropertiesUtils(File.separator+"dao.properties");
	
	public static String ACCOUNT_URL;
	
	static{
		ACCOUNT_URL = propertiesUtils.getString("kongque.account.url");
	}
	
	/**
	 * 系统标识
	 */
	public final static String SYS_ID = "xiuyu-order";
	
	/**
	 * 秀域主管角色id
	 */
	public final static String XIUYU_ROLE_ID = "4";
	/**
	 * 店员角色id
	 */
	public static final String DIANYUAN_ROLE_ID = "2";
	/**
	 * 星域主管角色id
	 */
	public static final String XINGYU_ROLE_ID = "3";
	/**
	 * 秀域分公司财务
	 */
	public static final String FENGONGSICAIWU_ROLE_ID = "9";
	/**
	 * 加盟店角色id
	 */
	public static final String JIAMENG_ROLE_ID = "10";
	
	/**
	 * 错误类型
	 */
	public static class ERROR_TYPE {
		// redis错误
		public static final String REDIS_ERROR = "REDIS_ERROR";

	}

	/**
	 * 接口执行结果状态码
	 */
	public static class RESULT_CODE {
		// 成功
		public static final String SUCCESS = "200";
		// 程序出现bug、异常
		public static final String SYS_ERROR = "500";
		// 未授权
		public static final String UN_AUTHORIZED = "301";
		// 无此用户
		public static final String USER_NOT_EXIST = "1000";
		// 密码错误
		public static final String PWD_ERROR = "1001";

	}
	
	public static class SYSCONSTANTS{
		/**
		 * token有效时间 单位s
		 */
		public static int TOKEN_TIMEOUT=propertiesUtils.getInt("defaultExpire");
	}
	
	public static class ACCOUNT {
		//账号系统接口 账号添加
		public static final String KONGQUE_ACCOUNT_ADD = ACCOUNT_URL + "/kongque-account/account/add";
	}
}
