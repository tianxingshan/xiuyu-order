package com.kongque.model;

public class BalanceModel {
	private String num;

	/**
	 * 城市
	 */
	private String city;
	/**
	 * 门店
	 */
	private String shopName;
	/**
	 * 分公司名
	 */
	private String unitName;
	/**
	 * 顾客姓名
	 */
	private String customerName;
	/**
	 * 微调项目
	 */
	private String repairProject;
	/**
	 * 款式名
	 */
	private String styleName;
	/**
	 * 款式码
	 */
	private String styleCode;

	/**
	 * 件数
	 */
	private String number;
	/**
	 * 颜色
	 */
	private String color;
	/**
	 * 微调单号
	 */
	private String repairCode;
	/**
	 * 结算时间
	 */
	private String balanceTime;

	/**
	 * 微调单创建时间
	 */
	private String createTime;

	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 单据状态
	 */
	private String repairStatus;
	/**
	 * 财务审核状态
	 */
	private String checkStatus;
	/**
	 * 微调单id
	 */
	private String repairId;

	/**
	 * 物流单删除标识 0删除 1正常
	 */
	private String logisticdeleteflag;
	/**
	 * 店铺id
	 */
	private String shopId;
	/**
	 * 分公司id
	 */
	private String companyId;
	/**
	 * 结算单号
	 */
	private String balanceNumeber;
	/**
	 * 结算单id
	 */
	private String balanceId;
	/**
	 * 结算状态.1：待确认，2：已确认，3：已结算
	 */
	private String balanceStatus;
	/**
	 * 线下订单号
	 */
	private String lineOrderCode;
	/**
	 * 物料编码
	 */
	private String matterCode;
	/**
	 * 孔雀订单号
	 */
	private String orderCode;
	/**
	 * 单价
	 */
	private String unitPrice;
	/**
	 * 金额
	 */
	private String amount;
	/**
	 * 发货时间
	 */
	private String sendTime;
	/**
	 * 收货时间
	 */
	private String expressTime;
	/**
	 * 物流单号
	 */
	private String expressNumber;
	

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

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getRepairProject() {
		return repairProject;
	}

	public void setRepairProject(String repairProject) {
		this.repairProject = repairProject;
	}

	public String getStyleName() {
		return styleName;
	}

	public void setStyleName(String styleName) {
		this.styleName = styleName;
	}

	public String getStyleCode() {
		return styleCode;
	}

	public void setStyleCode(String styleCode) {
		this.styleCode = styleCode;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getRepairCode() {
		return repairCode;
	}

	public void setRepairCode(String repairCode) {
		this.repairCode = repairCode;
	}

	public String getBalanceTime() {
		return balanceTime;
	}

	public void setBalanceTime(String balanceTime) {
		this.balanceTime = balanceTime;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getRepairStatus() {
		return repairStatus;
	}

	public void setRepairStatus(String repairStatus) {
		this.repairStatus = repairStatus;
	}

	public String getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(String checkStatus) {
		this.checkStatus = checkStatus;
	}

	public String getLogisticdeleteflag() {
		return logisticdeleteflag;
	}

	public void setLogisticdeleteflag(String logisticdeleteflag) {
		this.logisticdeleteflag = logisticdeleteflag;
	}

	public String getShopId() {
		return shopId;
	}

	public void setShopId(String shopId) {
		this.shopId = shopId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getBalanceNumeber() {
		return balanceNumeber;
	}

	public void setBalanceNumeber(String balanceNumeber) {
		this.balanceNumeber = balanceNumeber;
	}

	public String getBalanceId() {
		return balanceId;
	}

	public void setBalanceId(String balanceId) {
		this.balanceId = balanceId;
	}

	public String getBalanceStatus() {
		return balanceStatus;
	}

	public void setBalanceStatus(String balanceStatus) {
		this.balanceStatus = balanceStatus;
	}

	public String getLineOrderCode() {
		return lineOrderCode;
	}

	public void setLineOrderCode(String lineOrderCode) {
		this.lineOrderCode = lineOrderCode;
	}

	public String getMatterCode() {
		return matterCode;
	}

	public void setMatterCode(String matterCode) {
		this.matterCode = matterCode;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(String unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getExpressTime() {
		return expressTime;
	}

	public void setExpressTime(String expressTime) {
		this.expressTime = expressTime;
	}

	public String getExpressNumber() {
		return expressNumber;
	}

	public void setExpressNumber(String expressNumber) {
		this.expressNumber = expressNumber;
	}

	public String getRepairId() {
		return repairId;
	}

	public void setRepairId(String repairId) {
		this.repairId = repairId;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}

}
