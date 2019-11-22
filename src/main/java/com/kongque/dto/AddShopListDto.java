package com.kongque.dto;

public class AddShopListDto {
	
	private String areaId;
	
	private String userId;
	
	private String[] shopIds;

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public String[] getShopIds() {
		return shopIds;
	}

	public void setShopIds(String[] shopIds) {
		this.shopIds = shopIds;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
}
