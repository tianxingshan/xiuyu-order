package com.kongque.service.order;

import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kongque.model.RemoteOrderCustomerModel;
import org.springframework.web.multipart.MultipartFile;

import com.kongque.dto.OrderCheckDto;
import com.kongque.dto.OrderDetailSearchDto;
import com.kongque.dto.OrderDto;
import com.kongque.entity.order.Order;
import com.kongque.entity.order.OrderDetail;
import com.kongque.model.OrderDetailSearchModel;
import com.kongque.model.OrderFinishedLabel;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

public interface IOrderService {
	
	Pagination<Order> orderList(OrderDto dto,PageBean pageBean);

	/**
	 * 重置订单接口
	 * @param dto
	 * @param pageBean
	 * @return
	 */
	Pagination<Order> resetOrderList(OrderDto dto,PageBean pageBean);
	
	Pagination<Order> orderCheckList(OrderDto dto,PageBean pageBean);
	
	Result orderDetail(String id);
	
	Result saveOrUpdate(OrderDto dto,MultipartFile[] files);
	
	Result orderDel(String id);
	
	Result saveGoodsDetail(OrderDetail orderDetailList);
	
	Result delOrderDetail(String orderDetailId);
	
	Result checkOrUpdate(OrderCheckDto dto);
	
	List<OrderDetailSearchModel> queryDetailWithParam(OrderDetailSearchDto dto,PageBean pageBean);
	
	Long queryCountWithParam(OrderDetailSearchDto dto) throws ParseException;
	
	void exportRepairBalExcel(OrderDetailSearchDto dto, HttpServletRequest request, HttpServletResponse response);
	
	Pagination<OrderFinishedLabel> getOrderFinishedLabel(OrderFinishedLabel dto, PageBean pageBean);
	
    Result remoteOrder(OrderDto dto,PageBean pageBean);

	Pagination<Order> remoteOrderByCustomer(String customer,PageBean pageBean);

	Pagination<RemoteOrderCustomerModel> remoteOrderCustomer(OrderDto dto, PageBean pageBean);
    
    Result findSizeCode(String number);
    
    void getAttachment(String filePath);
    
    Result uploadFile(OrderDto dto,MultipartFile[] files);

	Pagination<OrderDetailSearchModel> orderDetailList(OrderDetailSearchDto dto,PageBean pageBean);

    Boolean checkDuplication(String id);
}
