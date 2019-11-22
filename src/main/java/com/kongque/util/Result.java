package com.kongque.util;

public class Result {

	private String returnCode = "200";

	private String returnMsg = "操作成功";

	private Object returnData;

	public Result() {

	}

	public Result(Object data) {
		this.returnData = data;
	}

	public Result(String returnCode, String returnMsg) {
		this.returnCode = returnCode;
		this.returnMsg = returnMsg;
	}

	public Result(String returnCode, String returnMsg, Object returnData) {
		this.returnCode = returnCode;
		this.returnMsg = returnMsg;
		this.returnData = returnData;
	}

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public String getReturnMsg() {
		return returnMsg;
	}

	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}

	public Object getReturnData() {
		return returnData;
	}

	public void setReturnData(Object returnData) {
		this.returnData = returnData;
	}

}
