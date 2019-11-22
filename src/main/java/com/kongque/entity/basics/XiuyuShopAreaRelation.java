package com.kongque.entity.basics;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "t_xiuyu_shop_area_relation")
@DynamicInsert(true)
@DynamicUpdate(true)
public class XiuyuShopAreaRelation implements Serializable {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 3777698547415020066L;

	@Id
	@GenericGenerator(name = "idGenerator", strategy = "uuid")
	@GeneratedValue(generator = "idGenerator")
	@Column(name = "c_id")
	private String id;
	
	@Column(name = "c_area_id")
	private String areaId;

	@Column(name = "c_shop_id")
	private String shopId;
	
	@ManyToOne
    @JoinColumn(name="c_area_id",insertable=false, updatable=false, nullable=true)
	@NotFound(action=NotFoundAction.IGNORE)
	private Area area;
	
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public String getShopId() {
		return shopId;
	}

	public void setShopId(String shopId) {
		this.shopId = shopId;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}
}
