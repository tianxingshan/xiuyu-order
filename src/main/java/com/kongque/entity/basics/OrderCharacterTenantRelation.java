package com.kongque.entity.basics;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Administrator
 */
@Entity
@Table(name = "t_order_character_tenant_relation")
@DynamicInsert(true)
@DynamicUpdate(true)
public class OrderCharacterTenantRelation implements Serializable {
    private static final long serialVersionUID = 5934715634131644933L;
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    @Column(name = "c_id")
    private String id;
    @Column(name = "c_tenant_id")
    private String tenantId;//商户id
    @Column(name = "c_order_character_id")
    private String orderCharacterId; //订单性质id

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

    public String getOrderCharacterId() {
        return orderCharacterId;
    }

    public void setOrderCharacterId(String orderCharacterId) {
        this.orderCharacterId = orderCharacterId;
    }
}
/**
 * @program: xingyu-order
 * @description: 订单性质商户对照
 * @author: zhuxl
 * @create: 2019-04-19 11:02
 **/
