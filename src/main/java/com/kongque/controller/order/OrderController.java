package com.kongque.controller.order;

import java.text.ParseException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kongque.model.RemoteOrderCustomerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.kongque.dto.OrderCheckDto;
import com.kongque.dto.OrderDetailSearchDto;
import com.kongque.dto.OrderDto;
import com.kongque.entity.order.Order;
import com.kongque.entity.order.OrderDetail;
import com.kongque.model.OrderDetailSearchModel;
import com.kongque.model.OrderFinishedLabel;
import com.kongque.service.order.IOrderService;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

@Controller
@RequestMapping(value="/order")
public class OrderController {
	
	private final static Logger log = LoggerFactory.getLogger(OrderController.class);
	@Resource
	private IOrderService service;
	
	/**
	 * 查询订单列表
	 * @param dto
	 * @param pageBean
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Pagination<Order> orderList(OrderDto dto,PageBean pageBean){
		log.error("订单查询查询"+dto.toString());
		return service.orderList(dto,pageBean);
	}

	@RequestMapping(value = "/reset/list", method = RequestMethod.GET)
	@ResponseBody
	public Pagination<Order> orderResetList(OrderDto dto,PageBean pageBean){
		log.error("重置订单查询查询"+dto.toString());
		return service.resetOrderList(dto,pageBean);
	}
	
	/**
	 * 订单审核查询列表
	 * @param dto
	 * @param pageBean
	 * @return
	 */
	@RequestMapping(value = "/check/list", method = RequestMethod.GET)
	@ResponseBody
	public Pagination<Order> orderCheckList(OrderDto dto,PageBean pageBean){
		log.error("订单查询查询"+dto.toString());
		return service.orderCheckList(dto,pageBean);
	}
	
	/**
	 * 根据订单id获取订单全部信息
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/getDetail/byId", method = RequestMethod.GET)
	@ResponseBody
	public Result orderDetail(String id){
		return service.orderDetail(id);
	}
	
	/**
	 * 新增或修改订单
	 * @param dto
	 * @return
	 */
	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
	@ResponseBody
	public Result saveOrUpdate(OrderDto dto,MultipartFile[] files){
		return service.saveOrUpdate(dto,files);
	}
	
	/**
	 * 删除订单
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/del", method = RequestMethod.GET)
	@ResponseBody
	public Result orderDel(String id){
		return service.orderDel(id);
	}
	
	/**
	 * 添加款式记录
	 * @param dto
	 * @return
	 */
	@RequestMapping(value = "/saveOrderDetail", method = RequestMethod.POST)
	@ResponseBody
	public Result saveOrderDetail(@RequestBody OrderDetail orderDetail){
		return service.saveGoodsDetail(orderDetail);
	}
	
	/**
	 * 删除款式记录
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delOrderDetail", method = RequestMethod.GET)
	@ResponseBody
	public Result delOrderDetail(String orderDetailId){
		return service.delOrderDetail(orderDetailId);
	}
	
	/**
	 * 修改订单状态及财务审核
	 * @param dto
	 * @return
	 */
	@RequestMapping(value = "/check", method = RequestMethod.POST)
	@ResponseBody
	public Result checkOrder(@RequestBody OrderCheckDto dto){
		return service.checkOrUpdate(dto);
	}
	
	/**
	 * 查询订单明细列表
	 * @param dto
	 * @param pageBean
	 * @return
	 */
	@RequestMapping(value = "/detail/list", method = RequestMethod.GET)
	@ResponseBody
	public Pagination<OrderDetailSearchModel> orderDetailList(OrderDetailSearchDto dto,PageBean pageBean){
//		log.error("订单明细查询查询"+dto.toString());
//		Pagination<OrderDetailSearchModel> pagination = new Pagination<>();
//		try {
//			Long total = service.queryCountWithParam(dto);
//			if(total != null){
//				pagination.setTotal(total);
//			}
//			List<OrderDetailSearchModel> resultList = service.queryDetailWithParam(dto,pageBean);
//			if(resultList != null){
//				pagination.setRows(resultList);
//			}
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		return service.orderDetailList(dto,pageBean);
	}
	
	
	/**
	 * 导出订单明细excel
	 * @param odsDto
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/exportExcel",method = RequestMethod.GET)
	public void exportRepairBalExcel(OrderDetailSearchDto dto, HttpServletRequest request, HttpServletResponse response){
		service.exportRepairBalExcel(dto, request, response);
	}
	
	
	/**
	 * 查询成品标签列表
	 *
	 * @param dto
	 * @param pageBean
	 * @return
	 */
	@RequestMapping(value = "/listFinishedLabel", method = RequestMethod.GET)
	public @ResponseBody Pagination<OrderFinishedLabel> getOrderFinishedLabel(OrderFinishedLabel dto,PageBean pageBean) {
		return service.getOrderFinishedLabel(dto,pageBean);
	}
	
	
	/**
	 * 根据条件模糊查询订单列表
	 * @param dto
	 * @return
	 */
	@RequestMapping(value = "/remote", method = RequestMethod.GET)
	@ResponseBody
	public Result remoteOrder(OrderDto dto,PageBean pageBean){
		return service.remoteOrder(dto,pageBean);
	}

	/**
	 * 根据会员名称查询订单列表
	 * @param customer
	 * @return
	 */
	@RequestMapping(value = "/remote/customer", method = RequestMethod.GET)
	@ResponseBody
	public Pagination<Order> remoteOrderByCustomer(String customer,PageBean pageBean){
		return service.remoteOrderByCustomer(customer,pageBean);
	}

	/**
	 * 根据会员名称或编码查询订单明细列表
	 * @param dto
	 * @return
	 */
	@RequestMapping(value = "/remote/customer/detail", method = RequestMethod.GET)
	@ResponseBody
	public Pagination<RemoteOrderCustomerModel> remoteOrderCustomer(OrderDto dto, PageBean pageBean){
		return service.remoteOrderCustomer(dto,pageBean);
	}
	
	/**
	 * 根据bra值查找尺码
	 * @param sizeCode
	 * @return
	 */
	@RequestMapping(value = "/findSizeCode",method = RequestMethod.GET)
    @ResponseBody
	public Result findSizeCode(String number){
		return service.findSizeCode(number);
	}
	
	/**
	 * 获取订单附件文件流
	 * 
	 * @param path
	 */
	@RequestMapping(value = "/file", method = RequestMethod.GET)
	public @ResponseBody void getAttachement(String path) {
		service.getAttachment(path);
	}
	
	
	/**
	 * 图片上传/删除
	 * @param dto
	 * @return
	 */
	@RequestMapping(value = "/file/upload", method = RequestMethod.POST)
	@ResponseBody
	public Result uploadFile(OrderDto dto,MultipartFile[] files){
		return service.uploadFile(dto,files);
	}

	/**
	 * 校验重复
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/checkDuplication", method = RequestMethod.GET)
	@ResponseBody
	public Boolean checkDuplication(String id){
		return service.checkDuplication(id);
	}
	
}
