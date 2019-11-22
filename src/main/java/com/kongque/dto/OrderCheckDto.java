package com.kongque.dto;

public class OrderCheckDto {
	
	private String orderId;
	
	private String checkType;
	
	private String checkStatus;
	
	private String checkerName;
	
	private String checkInstruction;
	
	private String status;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getCheckType() {
		return checkType;
	}

	public void setCheckType(String checkType) {
		this.checkType = checkType;
	}

	public String getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(String checkStatus) {
		this.checkStatus = checkStatus;
	}

	public String getCheckerName() {
		return checkerName;
	}

	public void setCheckerName(String checkerName) {
		this.checkerName = checkerName;
	}

	public String getCheckInstruction() {
		return checkInstruction;
	}

	public void setCheckInstruction(String checkInstruction) {
		this.checkInstruction = checkInstruction;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
