package com.kongque.model;

import com.kongque.entity.order.Order;
import com.kongque.entity.order.OrderDetail;

/**
 * @author xiaxt
 * @date 2019/1/8.
 */
public class RemoteOrderCustomerModel {

    private Order order;
    private OrderDetail orderDetail;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public OrderDetail getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(OrderDetail orderDetail) {
        this.orderDetail = orderDetail;
    }
}
