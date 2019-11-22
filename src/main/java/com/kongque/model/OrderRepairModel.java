package com.kongque.model;

public class OrderRepairModel {
	private String id;
	private String orderRepairCode;// 微调单号
	private String customerName;// 会员姓名
	private String orderCharacter;// 订单性质
	private String customerCode;// 会员卡号
	private String city;// 城市
	private String shopName;// 门店名称
	private String goodsName;// 商品名称
	private String goodsCode;//商品编码
	private String goodsSn;//商品唯一标识
	private String goodsColor;//颜色
	private String num;//数量
	private String solution;//处理方案
	private String orderRepairStatus;// 微调单状态
	private String sendExpressNumber;// 发货单号
	private String receiveExpressNumber;// 收货单号
	private String repairPerson;// 微调人
	private String isExtract;// 顾客是否提取
	private String checkTime;
	private String companyName;//分公司名称
	private String extendedFileName;//档案名
	private String description;//档案说明
	private String owner;//责任归属人

	private String sendTime;	//工厂发货日期
	private String deliveryTime;	//工厂收货日期
	private String createTime;	//创建日期
	private String orderCode;	//孔雀订单号
	private String ecOrderCode;	//EC订单号


	public String getExtendedFileName() {
		return extendedFileName;
	}
	public void setExtendedFileName(String extendedFileName) {
		this.extendedFileName = extendedFileName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOrderRepairCode() {
		return orderRepairCode;
	}
	public void setOrderRepairCode(String orderRepairCode) {
		this.orderRepairCode = orderRepairCode;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getOrderCharacter() {
		return orderCharacter;
	}
	public void setOrderCharacter(String orderCharacter) {
		this.orderCharacter = orderCharacter;
	}
	public String getCustomerCode() {
		return customerCode;
	}
	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getShopName() {
		return shopName;
	}
	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public String getOrderRepairStatus() {
		return orderRepairStatus;
	}
	public void setOrderRepairStatus(String orderRepairStatus) {
		this.orderRepairStatus = orderRepairStatus;
	}
	public String getSendExpressNumber() {
		return sendExpressNumber;
	}
	public void setSendExpressNumber(String sendExpressNumber) {
		this.sendExpressNumber = sendExpressNumber;
	}
	public String getReceiveExpressNumber() {
		return receiveExpressNumber;
	}
	public void setReceiveExpressNumber(String receiveExpressNumber) {
		this.receiveExpressNumber = receiveExpressNumber;
	}
	public String getRepairPerson() {
		return repairPerson;
	}
	public void setRepairPerson(String repairPerson) {
		this.repairPerson = repairPerson;
	}
	public String getIsExtract() {
		return isExtract;
	}
	public void setIsExtract(String isExtract) {
		this.isExtract = isExtract;
	}
	public String getCheckTime() {
		return checkTime;
	}
	public void setCheckTime(String checkTime) {
		this.checkTime = checkTime;
	}
	public String getGoodsCode() {
		return goodsCode;
	}
	public void setGoodsCode(String goodsCode) {
		this.goodsCode = goodsCode;
	}
	public String getGoodsSn() {
		return goodsSn;
	}
	public void setGoodsSn(String goodsSn) {
		this.goodsSn = goodsSn;
	}
	public String getGoodsColor() {
		return goodsColor;
	}
	public void setGoodsColor(String goodsColor) {
		this.goodsColor = goodsColor;
	}
	public String getNum() {
		return num;
	}
	public void setNum(String num) {
		this.num = num;
	}
	public String getSolution() {
		return solution;
	}
	public void setSolution(String solution) {
		this.solution = solution;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(String deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getEcOrderCode() {
		return ecOrderCode;
	}

	public void setEcOrderCode(String ecOrderCode) {
		this.ecOrderCode = ecOrderCode;
	}
}
