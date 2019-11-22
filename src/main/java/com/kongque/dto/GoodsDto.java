package com.kongque.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 商品查询条件
 * 
 * @author yiyi
 *
 */
public class GoodsDto {
	private String id;
	private String code;
	private String name;// 商品名称
	private String detail;//其他要增加的要以Json的形式放在detail中
	private String categoryId;// 商品品类
	private String status;// 商品状态是否可用：0启用1禁用
	private String createUser;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date createTime;
	private String updateUser;
	private String remark;
	private String tenantId;
	private String forOrder;//专属款  0  是  1 否
	private String q;
	
	
	private List<GoodsDetailDto> listGoodsDetail;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public List<GoodsDetailDto> getListGoodsDetail() {
		return listGoodsDetail;
	}

	public void setListGoodsDetail(List<GoodsDetailDto> listGoodsDetail) {
		this.listGoodsDetail = listGoodsDetail;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public String getForOrder() {
		return forOrder;
	}

	public void setForOrder(String forOrder) {
		this.forOrder = forOrder;
	}
}
