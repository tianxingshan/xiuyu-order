package com.kongque.entity.goods;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "t_goods_detail_tanant_relation")
@DynamicInsert(true)
@DynamicUpdate(true)
public class GoodsDetailTanant implements Serializable{

	private static final long serialVersionUID = 1906140057110985788L;
	
	@Id
	@GenericGenerator(name = "idGenerator", strategy = "uuid")
	@GeneratedValue(generator = "idGenerator")
	@Column(name = "c_id")
	private String id;
	
	@Column(name = "c_goods_detail_id")
	private String goodsDetailId;
	
	@Column(name = "c_tenant_id")
	private String tenantId;
	
	@Column(name = "c_balance_price")
	private String balancePrice;
	
	@Column(name = "c_tenant_price")
	private String tenantPrice;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGoodsDetailId() {
		return goodsDetailId;
	}

	public void setGoodsDetailId(String goodsDetailId) {
		this.goodsDetailId = goodsDetailId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getBalancePrice() {
		return balancePrice;
	}

	public void setBalancePrice(String balancePrice) {
		this.balancePrice = balancePrice;
	}

	public String getTenantPrice() {
		return tenantPrice;
	}

	public void setTenantPrice(String tenantPrice) {
		this.tenantPrice = tenantPrice;
	}
	
	
	

}
