package com.kongque.dto;


import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;


public class LogisticDto {
	private String id;
	private String tenantId;
	private String expressCompany;//快递公司
	private String expressNumber;//快递单号
	private String expressPrice;//快递价格           
	@DateTimeFormat(pattern="yy-MM-dd")
	private Date sendTime;//发货时间
	@DateTimeFormat(pattern="yy-MM-dd")
	private Date deliveryTime;//收货时间
	private String logisticType;//收货发货
	private String sender;//寄件人
	private String senderAddress;//寄件地址
	private String receiverAddress;//收件地址
	private String senderPhone;//寄件人电话
	private String settlementType;//结算类型
	private String receiver;//收件人
	private String receiverPhone;//收件人电话
	
	private String shopId;//门店id
	private String shopName;//门店名称
	private String checkStatus;//审核状态
	private String checkTime;//审核时间
	private String checkUserId;//审核人id
	private String note;//备注
	private String deleteFlag;//是否删除1.作废  0.正常
//	private String orderRepairId;
//	private String orderDetailId;
	
	private List<LogisticOrderListDto> orderList;
	
	private String voucherType; //判断是微调单还是订单1.微调单 0.订单
	private String customerName;//顾客姓名
	private String orderRepairCode;
	private String startTime;
	private String endTime;
	private String orderCode;
	
	private String  vourType;//0定制订单   1微调订单
	private String city;
	private String orderReset;
	private String orderCharacter;     
	private String orderDetailGoodsSn;
	private String goodsName;
	private String shopConsume;
	private String orderType;//订单类型//0定制订单   1微调订单
	private String isExtract;//顾客是否提取（0：未提取，1：已提取）
	private String userId;//用户id
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getExpressCompany() {
		return expressCompany;
	}
	public void setExpressCompany(String expressCompany) {
		this.expressCompany = expressCompany;
	}
	public String getExpressNumber() {
		return expressNumber;
	}
	public void setExpressNumber(String expressNumber) {
		this.expressNumber = expressNumber;
	}
	public String getExpressPrice() {
		return expressPrice;
	}
	public void setExpressPrice(String expressPrice) {
		this.expressPrice = expressPrice;
	}
	
	
	public String getLogisticType() {
		return logisticType;
	}
	public void setLogisticType(String logisticType) {
		this.logisticType = logisticType;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getSenderAddress() {
		return senderAddress;
	}
	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}
	public String getReceiverAddress() {
		return receiverAddress;
	}
	public void setReceiverAddress(String receiverAddress) {
		this.receiverAddress = receiverAddress;
	}
	public String getSenderPhone() {
		return senderPhone;
	}
	public void setSenderPhone(String senderPhone) {
		this.senderPhone = senderPhone;
	}
	public String getSettlementType() {
		return settlementType;
	}
	public void setSettlementType(String settlementType) {
		this.settlementType = settlementType;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getReceiverPhone() {
		return receiverPhone;
	}
	public void setReceiverPhone(String receiverPhone) {
		this.receiverPhone = receiverPhone;
	}

	public String getShopId() {
		return shopId;
	}
	public void setShopId(String shopId) {
		this.shopId = shopId;
	}
	public String getShopName() {
		return shopName;
	}
	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	public String getCheckStatus() {
		return checkStatus;
	}
	public void setCheckStatus(String checkStatus) {
		this.checkStatus = checkStatus;
	}
	public String getCheckTime() {
		return checkTime;
	}
	public void setCheckTime(String checkTime) {
		this.checkTime = checkTime;
	}
	public String getCheckUserId() {
		return checkUserId;
	}
	public void setCheckUserId(String checkUserId) {
		this.checkUserId = checkUserId;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getDeleteFlag() {
		return deleteFlag;
	}
	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
//	public String getOrderRepairId() {
//		return orderRepairId;
//	}
//	public void setOrderRepairId(String orderRepairId) {
//		this.orderRepairId = orderRepairId;
//	}
//	public String getOrderDetailId() {
//		return orderDetailId;
//	}
//	public void setOrderDetailId(String orderDetailId) {
//		this.orderDetailId = orderDetailId;
//	}
	
	public String getVoucherType() {
		return voucherType;
	}
	public List<LogisticOrderListDto> getOrderList() {
		return orderList;
	}
	public void setOrderList(List<LogisticOrderListDto> orderList) {
		this.orderList = orderList;
	}
	public void setVoucherType(String voucherType) {
		this.voucherType = voucherType;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getOrderRepairCode() {
		return orderRepairCode;
	}
	public void setOrderRepairCode(String orderRepairCode) {
		this.orderRepairCode = orderRepairCode;
	}
	
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public String getVourType() {
		return vourType;
	}
	public void setVourType(String vourType) {
		this.vourType = vourType;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getOrderReset() {
		return orderReset;
	}
	public void setOrderReset(String orderReset) {
		this.orderReset = orderReset;
	}
	public String getOrderCharacter() {
		return orderCharacter;
	}
	public void setOrderCharacter(String orderCharacter) {
		this.orderCharacter = orderCharacter;
	}
	public String getOrderDetailGoodsSn() {
		return orderDetailGoodsSn;
	}
	public void setOrderDetailGoodsSn(String orderDetailGoodsSn) {
		this.orderDetailGoodsSn = orderDetailGoodsSn;
	}
	/**
	 * @return
	 */
	public String getGoodsName() {
		return goodsName;
	}
	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}
	public String getShopConsume() {
		return shopConsume;
	}
	public void setShopConsume(String shopConsume) {
		this.shopConsume = shopConsume;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	public String getIsExtract() {
		return isExtract;
	}
	public void setIsExtract(String isExtract) {
		this.isExtract = isExtract;
	}
	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	public Date getDeliveryTime() {
		return deliveryTime;
	}
	public void setDeliveryTime(Date deliveryTime) {
		this.deliveryTime = deliveryTime;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	
}
