package com.kongque.service.basics.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.kongque.constants.Constants;
import com.kongque.dao.IDaoService;
import com.kongque.dto.LogisticDto;
import com.kongque.entity.basics.Dept;
import com.kongque.entity.basics.Logistic;
import com.kongque.entity.basics.LogisticOrder;
import com.kongque.entity.basics.Tenant;
import com.kongque.entity.basics.XiuyuCustomer;
import com.kongque.entity.order.Order;
import com.kongque.entity.order.OrderDetail;
import com.kongque.entity.repair.OrderRepair;
import com.kongque.entity.user.UserDeptRelation;
import com.kongque.entity.user.UserRoleRelation;
import com.kongque.model.LogisticExportModel;
import com.kongque.model.LogisticOrderModel;
import com.kongque.service.basics.ILogisticService;
import com.kongque.util.ExportExcelUtil;
import com.kongque.util.ListUtils;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

@Service
public class LogisticServiceImpl implements ILogisticService{
	
	@Resource
	IDaoService dao;
	@Override
	public Pagination<Logistic> list(LogisticDto dto, PageBean pageBean) {
		Criteria criteria = dao.createCriteria(Logistic.class);
		if(!StringUtils.isBlank(dto.getLogisticType())){
			criteria.add(Restrictions.eq("logisticType", dto.getLogisticType()));
		}
		if (!StringUtils.isBlank(dto.getExpressCompany())) {
			criteria.add(Restrictions.like("expressCompany", dto.getExpressCompany(), MatchMode.ANYWHERE));
		}
		if (!StringUtils.isBlank(dto.getExpressNumber())) {
			criteria.add(Restrictions.like("expressNumber", dto.getExpressNumber(), MatchMode.ANYWHERE));
		}
		
		if(!StringUtils.isBlank(dto.getShopName())){//向本次数据库查询中添加发货门店查询条件[2017-06-21]
			criteria.add(Restrictions.like("shopName", dto.getShopName(),MatchMode.ANYWHERE));
		}
		criteria.add(Restrictions.eq("deleteFlag","0"));
		if(dto.getLogisticType()!=null){
			switch(dto.getLogisticType()){
			case "0":
				Criteria logisticOrderCriteria = null;
				Disjunction disjunction = Restrictions.disjunction();
				if (!StringUtils.isBlank(dto.getOrderCode())) {
					logisticOrderCriteria = criteria.createCriteria("list", "ll");
					disjunction.add(Restrictions.like("ll.orderCode", dto.getOrderCode(), MatchMode.ANYWHERE));
				}
				if (!StringUtils.isBlank(dto.getOrderRepairCode())) {
					if(logisticOrderCriteria == null){
						criteria.createCriteria("list", "ll").createCriteria("orderRepair", "o")
								.add(Restrictions.like("o.orderRepairCode", dto.getOrderRepairCode(), MatchMode.ANYWHERE));
					}else{
						logisticOrderCriteria.createCriteria("orderRepair", "o");
						disjunction.add(Restrictions.like("o.orderRepairCode", dto.getOrderRepairCode(), MatchMode.ANYWHERE));
					}
				}
				criteria.addOrder(org.hibernate.criterion.Order.desc("sendTime"));
				break;
			case "1":
				if (!StringUtils.isBlank(dto.getOrderRepairCode())) {
					criteria.createCriteria("list", "ll").createCriteria("orderRepair", "o")
							.add(Restrictions.like("o.orderRepairCode", dto.getOrderRepairCode(), MatchMode.ANYWHERE));
				}
				criteria.addOrder(org.hibernate.criterion.Order.desc("deliveryTime"));
				break;
			}
		}
		if(dto.getLogisticType()!=null){
			switch(dto.getLogisticType()){
			case "1": 
					  if (StringUtils.isNotBlank(dto.getStartTime()) && StringUtils.isNotBlank(dto.getEndTime())) {
							criteria.add(Restrictions.ge("deliveryTime",dto.getStartTime()));
							criteria.add(Restrictions.lt("deliveryTime",dto.getEndTime()));
						} else if (StringUtils.isNotBlank(dto.getStartTime())) {
							criteria.add(Restrictions.ge("deliveryTime",dto.getStartTime()));
						} else if (StringUtils.isNotBlank(dto.getEndTime())) {
							criteria.add(Restrictions.le("deliveryTime",dto.getEndTime()));
						}
				break;
			case "0": 
				 if (StringUtils.isNotBlank(dto.getEndTime())&&StringUtils.isNotBlank(dto.getEndTime())) {
						criteria.add(Restrictions.ge("sendTime",dto.getStartTime()));
						criteria.add(Restrictions.lt("sendTime",dto.getEndTime()));
					} else if (StringUtils.isNotBlank(dto.getEndTime())) {
						criteria.add(Restrictions.ge("sendTime",dto.getStartTime()));
					} else if (StringUtils.isNotBlank(dto.getEndTime())) {
						criteria.add(Restrictions.le("sendTime",dto.getEndTime()));
					}
				break;
			}
		}
		
		Pagination<Logistic> pagination = new Pagination<>();
		pagination.setRows(dao.findListWithPagebeanCriteria(criteria, pageBean));
		pagination.setTotal(dao.findTotalWithCriteria(criteria));
		return pagination;
	}

	@Override
	public Result saveOrUpdate(LogisticDto dto) {

		if (StringUtils.isBlank(dto.getShopId())){
			return new Result("500","店铺代码不能为空!");
		}

		if(StringUtils.isBlank(dto.getId())){ 
			Logistic logistic=new Logistic();
			logistic.setCheckStatus("0");
			logistic.setDeleteFlag("0");
			if(dto.getLogisticType() != null) {
				logistic.setLogisticType(dto.getLogisticType());
				if(dto.getLogisticType().equals("0")){
					logistic.setSendTime(new Date());
				}else if(dto.getLogisticType().equals("1")){
					logistic.setDeliveryTime(new Date());
				}
			}
			logistic.setSender(dto.getSender());
			logistic.setSenderAddress(dto.getSenderAddress());
			logistic.setSenderPhone(dto.getSenderPhone());
			logistic.setReceiver(dto.getReceiver());
			logistic.setReceiverAddress(dto.getReceiverAddress());
			logistic.setReceiverPhone(dto.getReceiverPhone());
			logistic.setExpressCompany(dto.getExpressCompany());
			logistic.setExpressNumber(dto.getExpressNumber());
			logistic.setExpressPrice(dto.getExpressPrice());
			logistic.setSettlementType(dto.getSettlementType());
			logistic.setNote(dto.getNote());
			logistic.setShopId(dto.getShopId());
			logistic.setShopName(dto.getShopName());
			logistic.setTenantId(dto.getTenantId());
			logistic.setLogisticStatus("1");
			dao.save(logistic);
					if(dto.getOrderList()!=null && dto.getOrderList().size()>0){
						if(dto.getOrderList().get(0).getOrderRepairId().length>0){
							for(int i=0;i<dto.getOrderList().get(0).getOrderRepairId().length;i++){
								LogisticOrder logisticOrder=new LogisticOrder();
								logisticOrder.setLogisticId(logistic.getId());
								logisticOrder.setOrderRepairId(dto.getOrderList().get(0).getOrderRepairId()[i]);
								OrderRepair orderRepair =dao.findById(OrderRepair.class,dto.getOrderList().get(0).getOrderRepairId()[i] );
								logisticOrder.setOrderCode(orderRepair.getOrderRepairCode());
								dao.save(logisticOrder);
							}
						}
						if(dto.getOrderList().get(1).getOrderDetailId().length>0){
							for(int i=0;i<dto.getOrderList().get(1).getOrderDetailId().length;i++){
								LogisticOrder logisticOrder=new LogisticOrder();
								logisticOrder.setLogisticId(logistic.getId());
								logisticOrder.setOrderDetailId(dto.getOrderList().get(1).getOrderDetailId()[i]);
								OrderDetail orderDetail =dao.findById(OrderDetail.class, dto.getOrderList().get(1).getOrderDetailId()[i]);
								Order order =dao.findById(Order.class, orderDetail.getOrderId());
								logisticOrder.setOrderCode(order.getOrderCode());
								dao.save(logisticOrder);
							}
							
						}		
						}
			
//			if(dto.getOrderList()!=null && dto.getOrderList().size()>0){
//				for (int i = 0; i < dto.getOrderList().size(); i++) {
//					LogisticOrder logisticOrder=new LogisticOrder();
//					if(StringUtils.isNotBlank(dto.getOrderList().get(i).getOrderDetailId())){
//						logisticOrder.setLogisticId(logistic.getId());
//						logisticOrder.setOrderDetailId(dto.getOrderList().get(i).getOrderDetailId());
//						dao.save(logisticOrder);
//					}
//					if(StringUtils.isNotBlank(dto.getOrderList().get(i).getOrderRepairId())){
//						logisticOrder.setLogisticId(logistic.getId());
//						logisticOrder.setOrderRepairId(dto.getOrderList().get(i).getOrderRepairId());
//						dao.save(logisticOrder);
//					}
//				}
//			}
//			LogisticOrder logisticOrder=new LogisticOrder();
//			logisticOrder.setLogisticId(logistic.getId());
//			if(dto.getOrderRepairId()!=null) {
//				logisticOrder.setOrderRepair(dao.findById(OrderRepair.class, dto.getOrderRepairId()));
//			}
//			if(dto.getOrderDetailId()!=null) {
//				logisticOrder.setOrderDetail(dao.findById(OrderDetail.class,dto.getOrderDetailId()));
//			}
//			List<LogisticOrder> list=new ArrayList<LogisticOrder>();
//			list.add(logisticOrder);
//			//logistic.setList(list);
//			dao.save(logisticOrder);
			return new Result(logistic);
			}
		else{
			Logistic logistic=dao.findById(Logistic.class, dto.getId());
			if(dto.getLogisticType() != null) {
				logistic.setLogisticType(dto.getLogisticType());
				if(dto.getLogisticType().equals("0")){
					logistic.setSendTime(new Date());
				}else if(dto.getLogisticType().equals("1")){
					logistic.setDeliveryTime(new Date());
				}
			}
			logistic.setSender(dto.getSender());
			logistic.setSenderAddress(dto.getSenderAddress());
			logistic.setSenderPhone(dto.getSenderPhone());
			logistic.setReceiver(dto.getReceiver());
			logistic.setReceiverAddress(dto.getReceiverAddress());
			logistic.setReceiverPhone(dto.getReceiverPhone());
			logistic.setExpressCompany(dto.getExpressCompany());
			logistic.setExpressNumber(dto.getExpressNumber());
			logistic.setExpressPrice(dto.getExpressPrice());
			logistic.setSettlementType(dto.getSettlementType());
			logistic.setNote(dto.getNote());
			logistic.setShopId(dto.getShopId());
			logistic.setShopName(dto.getShopName());
			logistic.setTenantId(dto.getTenantId());
			dao.update(logistic);
			Criteria criteria = dao.createCriteria(LogisticOrder.class);
			criteria.add(Restrictions.eq("logisticId", logistic.getId()));
			List<LogisticOrder> listt = criteria.list();
			for (LogisticOrder logisticOrder2 : listt) {
				dao.delete(logisticOrder2);
			}
			if(dto.getOrderList()!=null && dto.getOrderList().size()>0){
				if(dto.getOrderList().get(0).getOrderRepairId().length>0){
					for(int i=0;i<dto.getOrderList().get(0).getOrderRepairId().length;i++){
						LogisticOrder logisticOrder=new LogisticOrder();
						logisticOrder.setLogisticId(logistic.getId());
						logisticOrder.setOrderRepairId(dto.getOrderList().get(0).getOrderRepairId()[i]);
						OrderRepair orderRepair =dao.findById(OrderRepair.class,dto.getOrderList().get(0).getOrderRepairId()[i] );
						logisticOrder.setOrderCode(orderRepair.getOrderRepairCode());
						dao.save(logisticOrder);
					}
				}
				if(dto.getOrderList().get(1).getOrderDetailId().length>0){
					for(int i=0;i<dto.getOrderList().get(1).getOrderDetailId().length;i++){
						LogisticOrder logisticOrder=new LogisticOrder();
						logisticOrder.setLogisticId(logistic.getId());
						logisticOrder.setOrderDetailId(dto.getOrderList().get(1).getOrderDetailId()[i]);
						OrderDetail orderDetail =dao.findById(OrderDetail.class, dto.getOrderList().get(1).getOrderDetailId()[i]);
						Order order =dao.findById(Order.class, orderDetail.getOrderId());
						logisticOrder.setOrderCode(order.getOrderCode());
						dao.save(logisticOrder);
					}
					
				}
			}
			
//			LogisticOrder logisticOrder=dao.findUniqueByProperty(LogisticOrder.class, "logisticId", logistic.getId());
//			if(logisticOrder.getOrderRepair()!=null) {
//				logisticOrder.setOrderRepair(dao.findById(OrderRepair.class, dto.getOrderRepairId()));
//			}
//			if(logisticOrder.getOrderDetail()!=null) {
//				logisticOrder.setOrderDetail(dao.findById(OrderDetail.class,dto.getOrderDetailId()));
//			}
//			dao.update(logisticOrder);
			return new Result(logistic);
		}
	}
	public String orderDetailStatus(String orderDetailStatus){
		String orderDetailStatuss=null;
		if(orderDetailStatus.equals("0")){
			orderDetailStatuss="已排程";
		}else if(orderDetailStatus.equals("1")){
			orderDetailStatuss="生产中";
		}else if(orderDetailStatus.equals("2")){
			orderDetailStatuss="生产完成";
		}
		else if(orderDetailStatus.equals("3")){
			orderDetailStatuss="已发货";
		}
		else if(orderDetailStatus.equals("4")){
			orderDetailStatuss="已收货";
		}else if(orderDetailStatus.equals("5")){
			orderDetailStatuss="已反馈";
		}
		return orderDetailStatuss;
	}
	
	public String orderRepairStatus(String orderRepairStatus){
		String orderRepairStatuss=null;
		if(orderRepairStatus.equals("0")){
			orderRepairStatuss="未送出";
		}else if(orderRepairStatus.equals("1")){
			orderRepairStatuss="已送出";
		}else if(orderRepairStatus.equals("2")){
			orderRepairStatuss="星域审核通过";
		}else if(orderRepairStatus.equals("3")){
			orderRepairStatuss="星域驳回";
		}else if(orderRepairStatus.equals("4")){
			orderRepairStatuss="已排程";
		}else if(orderRepairStatus.equals("5")){
			orderRepairStatuss="生产中";
		}else if(orderRepairStatus.equals("6")){
			orderRepairStatuss="生产完成";
		}else if(orderRepairStatus.equals("7")){
			orderRepairStatuss="已发货";
		}else if(orderRepairStatus.equals("8")){
			orderRepairStatuss="已收货";
		}
		return orderRepairStatuss;
	}
	@Override
	public Result delete(String id) {
		/*Logistic logistic=dao.findById(Logistic.class,id);
		logistic.setDeleteFlag("1");
		dao.update(logistic);*/
		Logistic logistic =null;
		if (StringUtils.isNotBlank(id)) {
			logistic = dao.findById(Logistic.class,id);
			if (logistic != null){
				List<LogisticOrder> logisticOrders = dao.findListByProperty(LogisticOrder.class,"logisticId",logistic.getId());
				switch(logistic.getLogisticType()) {
				case "0":
					if (logisticOrders.size() > 0) {
						for (LogisticOrder logisticOrder:logisticOrders){//修改发货单状态前，对发货单中所包含的明细信息状态进行校验[2017-07-27]
							if (logisticOrder.getOrderRepair()!= null){
								 OrderRepair orderRepair = dao.findById(OrderRepair.class, logisticOrder.getOrderRepair().getId());
								 if(!orderRepair.getOrderRepairStatus().equals("7")){//如果物流单所包含的微调单中有不是"已收货"状态的微调单则该物流单不能作废[2017-07-27]							 
			                        return new Result("500","包含["+orderRepairStatus(orderRepair.getOrderRepairStatus())+"]状态的微调单的发货单不能作废！");
								 }
							}
							if (logisticOrder.getOrderDetail() != null){
								   OrderDetail orderDetail = dao.findById(OrderDetail.class, logisticOrder.getOrderDetail().getId());
								 if(!orderDetail.getOrderDetailStatus().equals("3") &&!orderDetail.getOrderDetailStatus().equals("2")  ){//如果物流单所包含的微调单中有不是"已收货"状态的微调单则该物流单不能作废[2017-07-27]							 
				                        return new Result("500","包含["+orderDetailStatus(orderDetail.getOrderDetailStatus())+"]状态的订单明细信息的发货单不能作废！");
								}
							}
						}
						for (LogisticOrder logisticOrder: logisticOrders){
	                        if (logisticOrder.getOrderRepair()!= null){
	                            OrderRepair orderRepair = dao.findById(OrderRepair.class, logisticOrder.getOrderRepair().getId());
	                            orderRepair.setOrderRepairStatus("6");
	                            dao.update(orderRepair);
	                            //记录作废发货物流单时微调单的状态变化
	                     /*       HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(orderRepair.getId(), "OrderRepair", new Date(),user.getUserName()+"作废发货物流单时修改微调单:"+orderRepair.getRepairCode(),JSON.toJSONString(orderRepair), user.getId());
	                			dao.save(historyRecord);*/
	                        }
	                        if (logisticOrder.getOrderDetail() != null){
	                            OrderDetail orderDetail = dao.findById(OrderDetail.class, logisticOrder.getOrderDetail().getId());
	                            orderDetail.setOrderDetailStatus("2");
	                            dao.update(orderDetail);
	                            //记录作废发货物流单时订单的状态变化
	                      /*      HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(orderDetail.getId(), "OrderDetail", new Date(),user.getUserName()+"作废发货物流单时修改订单:"+orderDetail.getOrderId(),JSON.toJSONString(orderDetail), user.getId());
	                			dao.save(historyRecord);*/
	                        }
	                    }
						logistic.setDeleteFlag("1");
						dao.update(logistic);
						//记录作废发货物流单
					/*	 HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(logistic.getId(), "Logistic", new Date(),user.getUserName()+"作废发货物流单:"+logistic.getExpressNumber(),JSON.toJSONString(logistic), user.getId());
						dao.save(historyRecord);*/
					} else {
						logistic.setDeleteFlag("1");
						dao.update(logistic);
						//记录作废发货物流单
			/*			 HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(logistic.getId(), "Logistic", new Date(),user.getUserName()+"作废发货物流单:"+logistic.getExpressNumber(),JSON.toJSONString(logistic), user.getId());
						dao.save(historyRecord);*/
					};
					break;
				case "1":
					if (logisticOrders.size() > 0) {
						for (LogisticOrder logisticOrder:logisticOrders){//修改收货单状态前，对收货单中所包含的明细信息状态进行校验[2017-07-27]
							 OrderRepair orderRepair = dao.findById(OrderRepair.class, logisticOrder.getOrderRepair().getId());
							 if(!orderRepair.getOrderRepairStatus().equals("4")){//如果物流单所包含的微调单中有不是"已收货"状态的微调单则该物流单不能作废[2017-07-27]							 
		                        return new Result("500","包含["+orderRepairStatus(orderRepair.getOrderRepairStatus())+"]状态的微调单[微调单号："+orderRepair.getOrderRepairCode()+"]的收货单不能作废！");
							 }
						}
						Boolean checkStatus = true;
						for (LogisticOrder logisticOrder:logisticOrders){
	                        if (logisticOrder.getOrderRepair() != null){
	                        	OrderRepair orderRepair = dao.findById(OrderRepair.class, logisticOrder.getOrderRepair().getId());
	                            if ("2".equals(orderRepair.getOrderRepairStatus()) || "8".equals(orderRepair.getOrderRepairStatus())){
									orderRepair.setOrderRepairStatus("2");
									dao.update(orderRepair);
									//记录作废收货物流单时微调单的状态变化
						/*			HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(orderRepair.getId(), "OrderRepair", new Date(),user.getUserName()+"作废收货物流单时修改微调单:"+orderRepair.getRepairCode(),JSON.toJSONString(orderRepair), user.getId());
									dao.save(historyRecord);*/
								}else {
									checkStatus = false;
									break;
								}
	                        }
	                    }
	                    if (checkStatus == false){
							return new Result("500","微调单状态不是星域审核通过或者已收货状态");
						}
						logistic.setDeleteFlag("1");
						dao.update(logistic);
						//记录作废收货物流单
					/*	 HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(logistic.getId(), "Logistic", new Date(),user.getUserName()+"作废收货物流单:"+logistic.getExpressNumber(),JSON.toJSONString(logistic), user.getId());
	         			dao.save(historyRecord);*/
//	         			return new Result(logistic);
					}else {
						logistic.setDeleteFlag("1");
						dao.update(logistic);
						//记录作废收货物流单
	/*					 HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(logistic.getId(), "Logistic", new Date(),user.getUserName()+"作废收货物流单:"+logistic.getExpressNumber(),JSON.toJSONString(logistic), user.getId());
	        			dao.save(historyRecord);*/
//						return new Result(logistic);
					};
					break;
				}
				
			}else {
				return new Result("500","不存在该物流单");
			}
		}else {
			return new Result("500","物流单号不能为空");
		}
		return new Result(logistic);   
	}

	@SuppressWarnings("unused")
	@Override
	public Result updateStatus(String checkUserId,String id, String checkStatus) {
		Logistic logistic=dao.findById(Logistic.class, id);
		StringBuilder excludedLogistic = new StringBuilder();
		Integer _0 = 0;
		boolean canCheckStatus = true;
		
		for(LogisticOrder logisticOrder : logistic.getList()){
			/*if(_0.equals(logisticOrder.getDeleteFlag())){
				continue;
			}*/
			OrderRepair orderRepair=null;
			if(logisticOrder.getOrderRepair()!=null){
				if(logisticOrder.getOrderRepair().getId()!=null){
					orderRepair = dao.findById(OrderRepair.class, logisticOrder.getOrderRepair().getId());
				}
			}
			if(orderRepair != null){
				dao.refresh(orderRepair);
				if(StringUtils.isNotBlank(logistic.getLogisticType())){//判断物流单的类型属性是否为空[2017-07-27]
					switch(logistic.getLogisticType()){//判断物流单的类型是收货还是发货[2017-07-27]
				//修改微调单明细之前，对微调单明细的状态进行检验，如果要修改的微调单明细状态不是“星域审核通过”时，则不能审核该微调单所属物流单更不能改变微调单状态[2017-07-27]
						case "0":if(!orderRepair.getOrderRepairStatus().equals("6")){
							excludedLogistic.append(logistic.getExpressNumber()).append(",");//把当前物流单单号添加到不可审核物流单单号列表中[2017-07-28]
							canCheckStatus = false;//把审核标识变量设置为不可审核[2017-07-28]	
							return new Result("500","对应的微调单状态不是生产完成，不能确认审核");
						}break;
				//修改微调单明细之前，对微调单明细的状态进行检验，如果要修改的微调单明细状态不是“星域审核通过”时，则不能审核该微调单所属物流单更不能改变微调单状态[2017-07-27]
						case "1":if(!orderRepair.getOrderRepairStatus().equals("2")){
							excludedLogistic.append(logistic.getExpressNumber()).append(",");//把当前物流单单号添加到不可审核物流单单号列表中[2017-07-28]
							canCheckStatus = false;//把审核标识变量设置为不可审核[2017-07-28]	
							return new Result("500","对应的微调单状态不是星域审核通过，不能确认审核");
						}break;
					}						
				}
				else{//如果物流单的类型属性为空则不予审核
					canCheckStatus = false;
				}
			}else{
				//判断订单明细状态
//				if(!new Integer(0).equals(logisticOrder.getDeleteFlag())){
				OrderDetail orderDetail=null;
				if(logisticOrder.getOrderDetail()!=null){
					if(logisticOrder.getOrderDetail().getId()!=null){
						orderDetail = dao.findById(OrderDetail.class, logisticOrder.getOrderDetail().getId());
					}
				}
					dao.refresh(orderDetail);
					//修改订单明细之前，对订单明细的状态进行检验，只有当要修改的订调单明细状态是“生产完成”时，才能对该订单明细所属物流单进行审核并改变订单明细状态[2017-07-27]
					if(!orderDetail.getOrderDetailStatus().equals("2")){
						excludedLogistic.append(logistic.getExpressNumber()).append(",");//把当前物流单单号添加到不可审核物流单单号列表中[2017-07-28]
						canCheckStatus = false;//把审核标识变量设置为不可审核[2017-07-28]
					}
//				}
			}
			if(!canCheckStatus) break;
		}
		if(canCheckStatus) {
			logistic=dao.findById(Logistic.class,id);
			logistic.setCheckStatus(checkStatus);
			logistic.setCheckUserId(checkUserId);
			logistic.setCheckTime(new Date());
//			dao.update(logistic);
			updateLogisticOrderStatus(logistic);
		}
		Result result = new Result();//初始化返回数据模型
		String message="所选物流单全部审核成功！";//初始化返回信息
		if(excludedLogistic.length() > 0){//如果所选物流单中有不符合审核要求的物流单被排除
			result.setReturnCode("300");//返回结果模型中的状态码设为300
			message = "由于包含的微调单或订单的状态不符合要求，不能进行审核的物流单单号有：["+excludedLogistic.deleteCharAt(excludedLogistic.length()-1)+"]";
		}
		result.setReturnMsg(message);//向返回结果数据模型中设置返回信息
		return new Result(logistic);
	}
	private void updateLogisticOrderStatus(Logistic logistic) {
		for(LogisticOrder logisticOrder : logistic.getList()){
			OrderRepair orderRepair = null;
			if(logisticOrder.getOrderRepair()!=null){
				if(logisticOrder.getOrderRepair().getId()!=null){
					orderRepair =dao.findById(OrderRepair.class, logisticOrder.getOrderRepair().getId());
				}
			}
			if(orderRepair != null){
				dao.refresh(orderRepair);
				if(StringUtils.isNotBlank(logistic.getLogisticType())){//判断物流单的类型属性是否为空[2017-07-25]
					switch(logistic.getLogisticType()){//判断物流单的类型是收货还是发货[2017-07-25]
					case "0":orderRepair.setOrderRepairStatus("7"); break;//如果物流单类型是发货，则把对应的微调单状态改为已发货[2017-07-25]
					case "1":orderRepair.setOrderRepairStatus("8");break;//如果物流单类型是收货，则把对应的微调单状态改为已收货[2017-07-25]
					}
				}
				dao.update(orderRepair);
				//记录审核通过物流单中微调单的状态变化
/*				HistoryRecord	historyRecord=HistoryRecordUtil.historyRecord(orderRepair.getId(), "OrderRepair", date,user.getUserName()+"审核通过物流单中的微调单:"+orderRepair.getRepairCode(),JSON.toJSONString(orderRepair), user.getId());
				dao.save(historyRecord);*/
			}else{
				if(logisticOrder.getOrderDetail().getId()!=null){
					OrderDetail orderDetail = dao.findById(OrderDetail.class, logisticOrder.getOrderDetail().getId());
					dao.refresh(orderDetail);
					orderDetail.setOrderDetailStatus("3");
				}
//				OrderUtil.setWarrantyTime(orderDetail);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<LogisticOrderModel> queryLogisticWithParam(LogisticDto dto, Integer page,Integer rows) {
		List<LogisticOrderModel> list=new ArrayList<LogisticOrderModel>();
		StringBuilder sql = null;

		if(dto.getVoucherType()!=null){
		if(dto.getVoucherType().equals("0")){//订单
			 sql = new StringBuilder("SELECT l.c_shop_name AS shopName,z.c_customer_name AS customerName,r.c_order_repair_code AS orderRepairCode,q.c_order_code AS orderCode,r.c_is_extract AS shopConsume,l.c_logistic_type AS logisticType,o.c_goods_sn AS orderDetailGoodsSN,s.c_code AS orderDetailGoodsCode,s.c_name AS orderDetailGoodsName,h.c_color_name AS orderDetailGoodsColorName,o.c_num AS orderDetailGoodsNum,o.c_goods_sn as goodsSn,tenant.c_tenant_name " +
					 "FROM t_logistic_order t " +
					 "LEFT JOIN t_logistic l  ON l.c_id=t.c_logistic_id " +
					 "LEFT JOIN t_order_detail o ON o.c_id=t.c_order_detail_id " +
					 "LEFT JOIN t_order_repair r ON r.c_id=t.c_order_repair_id " +
					 "LEFT JOIN t_order q ON q.c_id = o.c_order_id " +
					 "LEFT JOIN t_goods_detail h ON o.c_goods_detail_id=h.c_id " +
					 "LEFT JOIN t_goods s ON h.c_goods_id=s.c_id " +
					 "LEFT JOIN t_xiuyu_customer z ON q.c_customer_id= z.c_id " +
					 "LEFT JOIN t_tenant tenant ON r.c_tenant_id = tenant.c_id " +
					 "WHERE l.c_delete_flag='0' ");
			 if(StringUtils.isNotBlank(dto.getTenantId())){
			 	sql.append(" AND tenant.c_id='").append(dto.getTenantId()).append("'");
			 }
		}else if(dto.getVoucherType().equals("1")){
			sql = new StringBuilder("SELECT l.c_shop_name AS shopName,r.c_order_repair_code AS orderRepairCode,q.c_order_code AS orderCode,r.c_is_extract AS shopConsume,r.c_customer_name AS repairCustomerName,l.c_logistic_type AS logisticType,s.c_code AS orderRepairGoodsCode,s.c_name AS orderRepairGoodsName,h.c_color_name AS orderRepairGoodsColor,r.c_num AS orderRepairGoodsNum,r.c_solution as solution,tenant.c_tenant_name " +
					"FROM t_logistic_order t  " +
					"LEFT JOIN t_logistic l ON l.c_id=t.c_logistic_id " +
					"LEFT JOIN t_order_detail o ON o.c_id=t.c_order_detail_id " +
					"LEFT JOIN t_order_repair r ON r.c_id=t.c_order_repair_id " +
					"LEFT JOIN t_order q ON q.c_id = o.c_order_id " +
					"LEFT JOIN t_goods_detail h ON r.c_goods_detail_id=h.c_id " +
					"LEFT JOIN t_goods s ON h.c_goods_id=s.c_id " +
					"LEFT JOIN t_xiuyu_customer z ON q.c_customer_id= z.c_id " +
					"LEFT JOIN t_tenant tenant ON q.c_tenant_id = tenant.c_id " +
					"WHERE l.c_delete_flag='0' ");
			if(StringUtils.isNotBlank(dto.getTenantId())){
				sql.append(" AND tenant.c_id='").append(dto.getTenantId()).append("'");
			}
		}
		}else{
			sql = new StringBuilder("SELECT l.c_shop_name AS shopName,r.c_order_repair_code AS orderRepairCode,q.c_order_code AS orderCode,r.c_is_extract AS shopConsume,r.c_customer_name AS repairCustomerName,l.c_logistic_type AS logisticType,s.c_code AS orderRepairGoodsCode,s.c_name AS orderRepairGoodsName,h.c_color_name AS orderRepairGoodsColor,r.c_num AS orderRepairGoodsNum,r.c_solution AS solution,q.c_customer_name AS customerName,o.c_goods_sn AS orderDetailGoodsSN,o.c_goods_code AS orderDetailGoodsCode,o.c_goods_name AS orderDetailGoodsName,o.c_goods_color_name AS orderDetailGoodsColorName,o.c_num AS orderDetailGoodsNum,q.c_city as orderCity,r.c_city as orderRepairCity,q.c_reset as orderReset,q.c_order_character  as orderCharacter,l.c_send_time as sendTime,l.c_delivery_time as deliveryTime,l.c_express_company as expressCompany,l.c_express_number as expressNumber,l.c_sender as sender,l.c_receiver as receiver,l.c_express_price as expressPrice,l.c_settlement_type as settlementType,o.c_goods_sn as goodsSn,case when l.c_order_type='0' then o_tenant.c_tenant_name else r_tenant.c_tenant_name end as tenant_name " +
					",case when l.c_order_type='0' then q.c_shop_name else r.c_shop_name end as order_shop_name " +
					"FROM t_logistic_order t  " +
					"LEFT JOIN t_logistic l ON l.c_id=t.c_logistic_id " +
					"LEFT JOIN t_order_detail o ON o.c_id=t.c_order_detail_id " +
					"LEFT JOIN t_order_repair r ON r.c_id=t.c_order_repair_id " +
					"LEFT JOIN t_order q ON q.c_id = o.c_order_id " +
					"LEFT JOIN t_tenant o_tenant ON q.c_tenant_id = o_tenant.c_id " +
					"LEFT JOIN t_tenant r_tenant ON r.c_tenant_id = r_tenant.c_id " +
					"LEFT JOIN t_goods_detail h ON r.c_goods_detail_id=h.c_id " +
					"LEFT JOIN t_goods s ON h.c_goods_id=s.c_id " +
					"LEFT JOIN t_xiuyu_customer z ON q.c_customer_id= z.c_id WHERE l.c_delete_flag='0' ");

		}
		if(StringUtils.isNotBlank(dto.getUserId())){
			sql.append(" and l.c_shop_id in (select c_shop_id from t_xiuyu_shop_director_relation where c_user_id='").append(dto.getUserId()).append("')");
		}

		if(dto.getLogisticType()!=null) {
			switch(dto.getLogisticType()){
			case "1": sql.append(" AND l.c_logistic_type = '1' ");break;
			case "0": sql.append(" AND l.c_logistic_type = '0' ");break;
			}
		}
		if(dto.getVoucherType()!=null){
			if(dto.getVoucherType().equals("0")){
				sql.append(" AND t.c_order_detail_id is not null AND t.c_order_repair_id is null ");
			}else if(dto.getVoucherType().equals("1")){
				sql.append(" AND t.c_order_repair_id is not null AND t.c_order_detail_id is null");
			}
		}
		//订单类型   0定制订单   1微调单
		if(dto.getOrderType()!=null && !dto.getOrderType().isEmpty()){
			if(dto.getOrderType().equals("0")){
				sql.append(" AND t.c_order_detail_id is not null AND t.c_order_repair_id is null ");
				if(StringUtils.isNotBlank(dto.getTenantId())) {
					sql.append(" AND o_tenant.c_id='").append(dto.getTenantId()).append("'");
				}
			}else if(dto.getOrderType().equals("1")){
				sql.append(" AND t.c_order_repair_id is not null AND t.c_order_detail_id is null");
				if(StringUtils.isNotBlank(dto.getTenantId())) {
					sql.append(" AND r_tenant.c_id='").append(dto.getTenantId()).append("'");
				}
			}
		}else {
			if(StringUtils.isNotBlank(dto.getTenantId())){
				sql.append(" AND (q.c_tenant_id='").append(dto.getTenantId()).append("'");
				sql.append(" OR r.c_tenant_id='").append(dto.getTenantId()).append("')");
			}
		}
		if(dto.getOrderCode()!=null && !dto.getOrderCode().isEmpty()){
			sql.append(" and q.c_order_code like '%").append(dto.getOrderCode()).append("%' ");
			
		}
		if(dto.getOrderRepairCode()!=null && !dto.getOrderRepairCode().isEmpty()) {
			sql.append(" and r.c_order_repair_code like '%").append(dto.getOrderRepairCode()).append("%' ");
		}
		if(dto.getShopName()!=null && !dto.getShopName().isEmpty()) {
			sql.append(" and l.c_shop_name = '").append(dto.getShopName()).append("' ");
		}
		//城市
		if(dto.getCity()!=null && !dto.getCity().isEmpty()) {
			sql.append(" and q.c_city like '%").append(dto.getCity()).append("%' ");
		}
		if(dto.getCity()!=null && !dto.getCity().isEmpty()) {
			sql.append(" or r.c_city like '%").append(dto.getCity()).append("%' ");
		}
		//商品名称
		if(dto.getGoodsName()!=null && !dto.getGoodsName().isEmpty()){
			sql.append(" and s.c_name = '").append(dto.getGoodsName()).append("' ");
		}
//		if(dto.getGoodsName()!=null && !dto.getGoodsName().isEmpty()){
//			sql.append(" or r.c_goods_name = '").append(dto.getGoodsName()).append("' ");
//		}
		if(dto.getOrderRepairCode()!=null && !dto.getOrderRepairCode().isEmpty()){
			sql.append(" and r.c_order_repair_code like '%").append(dto.getOrderRepairCode()).append("%' ");
		}
		//提取状态
		if(dto.getShopConsume()!=null && !dto.getShopConsume().isEmpty()){
			sql.append(" and r.c_is_extract = '").append(dto.getShopConsume()).append("'");
		}
		if(dto.getExpressCompany()!=null && !dto.getExpressCompany().isEmpty()) {
			sql.append(" and l.c_express_company like '%").append(dto.getExpressCompany()).append("%' ");
		}
		if(dto.getExpressNumber() != null && !dto.getExpressNumber().isEmpty()){//添加物流单号限定条件
			sql.append(" and l.c_express_number like '%").append(dto.getExpressNumber()).append("%'");
		}
		/**
		 * 订单类型
		 */
		if(dto.getOrderReset() !=null&& !dto.getOrderReset().isEmpty()){
			sql.append(" and q.c_reset = '").append(dto.getOrderReset()).append("'");
		}
		//订单性质
		if(dto.getOrderCharacter()!=null&& !dto.getOrderCharacter().isEmpty()){
			sql.append(" and q.c_order_character like '%").append(dto.getOrderCharacter()).append("%'");
		}
		if(dto.getOrderDetailGoodsSn()!=null&& !dto.getOrderDetailGoodsSn().isEmpty()){
			sql.append(" and o.c_goods_sn  like '%").append(dto.getOrderDetailGoodsSn()).append("%'");
		}
		if(!"".equals(dto.getStartTime())&&dto.getStartTime() != null  && !"".equals(dto.getEndTime())&&dto.getEndTime() != null ){//添加同时包含起始发货日期和截止发货日期的限定条件
			switch(dto.getLogisticType()){
			case "1": sql.append(" and l.c_delivery_time between '").append(dto.getStartTime()).append(" 00:00:00'").append(" and '").append(dto.getEndTime()).append(" 23:59:59'");break;
			case "0": sql.append(" and l.c_send_time between '").append(dto.getStartTime()).append(" 00:00:00'").append(" and '").append(dto.getEndTime()).append(" 23:59:59'");break;
			}			
		}else if(!"".equals(dto.getStartTime())&&dto.getStartTime() != null  && (dto.getEndTime() == null || dto.getEndTime().isEmpty() )){//添加只包含起始发货日期而不包含截止发货日期的限定条件
			switch(dto.getLogisticType()){
			case "1": sql.append(" and l.c_delivery_time >= '").append(dto.getStartTime()).append(" 00:00:00'");break;
			case "0": sql.append(" and l.c_send_time >= '").append(dto.getStartTime()).append(" 00:00:00'");break;
			}
			
		}else if (((dto.getStartTime() == null || dto.getStartTime().isEmpty()) && !"".equals(dto.getEndTime())&&dto.getEndTime() != null)) {//添加只包含截止发货日期而不包含起始发货日期的限定条件
			switch(dto.getLogisticType()){
			case "1": sql.append(" and l.c_delivery_time"
					+ " <= '").append(dto.getEndTime()).append(" 23:59:59'");break;
			case "0": sql.append(" and l.c_send_time <= '").append(dto.getEndTime()).append(" 23:59:59'");break;
			}			
		}
		if(dto.getCustomerName()!=null&& !dto.getCustomerName().isEmpty()){
			sql.append(" and  r.c_customer_name  like '%").append(dto.getCustomerName()).append("%'");
		}
		if(dto.getCustomerName()!=null&& !dto.getCustomerName().isEmpty()){
			sql.append(" or  q.c_customer_name  like '%").append(dto.getCustomerName()).append("%'");
		}
		if(page != null && rows != null){
			sql.append(" limit "+(page - 1) * rows+","+rows);
		}
		List resultSet = dao.queryBySql(sql.toString());
		if(dto.getVoucherType()!=null){
			if(dto.getVoucherType().equals("0")){
				for(Object result : resultSet){
					LogisticOrderModel rsModel = new LogisticOrderModel();
					Object[] properties = (Object[])result;
					rsModel.setShopName(properties[0]==null ? "" : properties[0].toString());
					rsModel.setCustomerName(properties[1]==null ? "" : properties[1].toString());
					rsModel.setOrderRepairCode(properties[2]==null ? "" : properties[2].toString());
					rsModel.setOrderCode(properties[3]==null ? "" : properties[3].toString());
					rsModel.setShopConsume(properties[4]==null ? "" : properties[4].toString());
					rsModel.setLogisticType(properties[5]==null ? "" : properties[5].toString());
					rsModel.setOrderDetailGoodsSN(properties[6]==null ? "" : properties[6].toString());
					rsModel.setOrderDetailGoodsCode(properties[7]==null ? "" : properties[7].toString());
					rsModel.setOrderDetailGoodsName(properties[8]==null ? "" : properties[8].toString());
					rsModel.setOrderDetailGoodsColorName(properties[9]==null ? "" : properties[9].toString());
					rsModel.setOrderDetailNum(properties[10]==null ? "" : properties[10].toString());
					rsModel.setOrderDetailGoodsSN(properties[11]==null ? "" : properties[11].toString());
					rsModel.setTenantName(properties[12]==null ? "" : properties[12].toString());
					list.add(rsModel);
				}
			}else if(dto.getVoucherType().equals("1")){
				for(Object result : resultSet){
					LogisticOrderModel rsModel = new LogisticOrderModel();
					Object[] properties = (Object[])result;
					rsModel.setShopName(properties[0]==null ? "" : properties[0].toString());
					rsModel.setOrderRepairCode(properties[1]==null ? "" : properties[1].toString());
					rsModel.setOrderCode(properties[2]==null ? "" : properties[2].toString());
					rsModel.setShopConsume(properties[3]==null ? "" : properties[3].toString());
					rsModel.setRepairCustomerName(properties[4]==null ? "" : properties[4].toString());
					rsModel.setLogisticType(properties[5]==null ? "" : properties[5].toString());
					rsModel.setOrderRepairGoodsCode(properties[6]==null ? "" : properties[6].toString());
					rsModel.setOrderRepairGoodsName(properties[7]==null ? "" : properties[7].toString());
					rsModel.setOrderRepairGoodsColor(properties[8]==null ? "" : properties[8].toString());
					rsModel.setOrderRepairGoodsNum(properties[9]==null ? "" : properties[9].toString());
					rsModel.setSolution(properties[10]==null ? "" : properties[10].toString());
					rsModel.setTenantName(properties[11]==null ? "" : properties[11].toString());
					list.add(rsModel);
				}
			}	
		}else{
			for(Object result : resultSet){
				LogisticOrderModel rsModel = new LogisticOrderModel();
				Object[] properties = (Object[])result;
				rsModel.setShopName(properties[0]==null ? "" : properties[0].toString());
				rsModel.setOrderRepairCode(properties[1]==null ? "" : properties[1].toString());
				rsModel.setOrderCode(properties[2]==null ? "" : properties[2].toString());
				rsModel.setShopConsume(properties[3]==null ? "" : properties[3].toString());
				rsModel.setRepairCustomerName(properties[4]==null ? "" : properties[4].toString());
				rsModel.setLogisticType(properties[5]==null ? "" : properties[5].toString());
				rsModel.setOrderRepairGoodsCode(properties[6]==null ? "" : properties[6].toString());
				rsModel.setOrderRepairGoodsName(properties[7]==null ? "" : properties[7].toString());
				rsModel.setOrderRepairGoodsColor(properties[8]==null ? "" : properties[8].toString());
				rsModel.setOrderRepairGoodsNum(properties[9]==null ? "" : properties[9].toString());
				rsModel.setSolution(properties[10]==null ? "" : properties[10].toString());
				rsModel.setCustomerName(properties[11]==null ? "" : properties[11].toString());
				rsModel.setOrderDetailGoodsSN(properties[12]==null ? "" : properties[12].toString());
				rsModel.setOrderDetailGoodsCode(properties[13]==null ? "" : properties[13].toString());
				rsModel.setOrderDetailGoodsName(properties[14]==null ? "" : properties[14].toString());
				rsModel.setOrderDetailGoodsColorName(properties[15]==null ? "" : properties[15].toString());
				rsModel.setOrderDetailNum(properties[16]==null ? "" : properties[16].toString());
				rsModel.setOrderCity(properties[17]==null ? "" : properties[17].toString());
				rsModel.setOrderRepairCity(properties[18]==null ? "" : properties[18].toString());
				rsModel.setOrderReset(properties[19]==null ? "" : properties[19].toString());
				rsModel.setOrderCharacter(properties[20]==null ? "" : properties[20].toString());
				rsModel.setSendTime(time(properties[21]==null ? "" : properties[21].toString()));
				rsModel.setDeliveryTime(time(properties[22]==null ? "" : properties[22].toString()));
				rsModel.setExpressCompany(properties[23]==null ? "" : properties[23].toString());
				rsModel.setExpressNumber(properties[24]==null ? "" : properties[24].toString());
				rsModel.setSender(properties[25]==null ? "" : properties[25].toString());
				rsModel.setReceiver(properties[26]==null ? "" : properties[26].toString());
				rsModel.setExpressPrice(properties[27]==null ? "" : properties[27].toString());
				rsModel.setSettlementType(properties[28]==null ? "" : properties[28].toString());
				rsModel.setOrderDetailGoodsSN(properties[29]==null ? "" : properties[29].toString());
				rsModel.setTenantName(properties[30]==null ? "" : properties[30].toString());
				rsModel.setOrderShopName(properties[31]==null ? "" : properties[31].toString());
				list.add(rsModel);
			}
		}
		return list;
	}
	//规定时间格式
	public String time(String time){
		String t=null;
		if(time!=null && !"".equals(time)){
			t=time.substring(0, 19);
		}
		return t;
	}
	@Override
	public long queryLogisticCountWithParam(LogisticDto dto) {
		StringBuilder sql = new StringBuilder("SELECT count(*) FROM t_logistic_order t LEFT JOIN t_logistic l ON l.c_id=t.c_logistic_id LEFT JOIN t_order_detail o ON o.c_id=t.c_order_detail_id LEFT JOIN t_order_repair r ON r.c_id=t.c_order_repair_id LEFT JOIN t_order q ON q.c_id = o.c_order_id LEFT JOIN t_goods_detail h ON o.c_goods_detail_id=h.c_id LEFT JOIN t_goods s ON h.c_goods_id=s.c_id LEFT JOIN t_xiuyu_customer z ON q.c_customer_id= z.c_id WHERE l.c_delete_flag='0' ");

		if(StringUtils.isNotBlank(dto.getTenantId())){
			if(StringUtils.isNotBlank(dto.getOrderType())) {
				if("0".equals(dto.getOrderType())){
					sql.append(" and q.c_tenant_id='").append(dto.getTenantId()).append("'");
				}else {
					sql.append(" and r.c_tenant_id='").append(dto.getTenantId()).append("'");
				}

			}else{
				sql.append(" and (q.c_tenant_id='").append(dto.getTenantId()).append("'");
				sql.append(" or r.c_tenant_id='").append(dto.getTenantId()).append("')");
			}
		}
		if(StringUtils.isNotBlank(dto.getUserId())){
			sql.append(" and l.c_shop_id in (select c_shop_id from t_xiuyu_shop_director_relation where c_user_id='").append(dto.getUserId()).append("') ");
//			@SuppressWarnings("unchecked")
//			List<UserRoleRelation> users = dao.createCriteria(UserRoleRelation.class)
//					.add(Restrictions.eq( "userId", dto.getUserId())).list();
//			for (UserRoleRelation userRoleRelation : users) {
//				if (Constants.DIANYUAN_ROLE_ID.equalsIgnoreCase(userRoleRelation.getRole().getId())){
//					sql.append(" AND l.c_shop_id='"+dto.getShopId()+"' ");
//					break;
//				}
//			}

		}
		if(dto.getLogisticType()!=null) {
			switch(dto.getLogisticType()){
			case "1": sql.append(" AND l.c_logistic_type = '1' and r.c_del='0' and t.c_delete_flag = '0'");break;
			case "0": sql.append(" AND l.c_logistic_type = '0' and t.c_delete_flag = '0'");break;
			}

		}
		//订单类型   0定制订单   1微调单
				if(dto.getVoucherType()!=null && !dto.getVoucherType().isEmpty()){
					if(dto.getVoucherType().equals("0")){
						sql.append(" AND t.c_order_detail_id is not null AND t.c_order_repair_id is null ");
					}else if(dto.getVoucherType().equals("1")){
						sql.append(" AND t.c_order_repair_id is not null AND t.c_order_detail_id is null");
					}
				}
		//订单类型   0定制订单   1微调单
		if(dto.getOrderType()!=null && !dto.getOrderType().isEmpty()){
			if(dto.getOrderType().equals("0")){
				sql.append(" AND t.c_order_detail_id is not null AND t.c_order_repair_id is null ");
			}else if(dto.getOrderType().equals("1")){
				sql.append(" AND t.c_order_repair_id is not null AND t.c_order_detail_id is null");
			}
		}
		if(dto.getOrderCode()!=null && !dto.getOrderCode().isEmpty()){
			sql.append(" and q.c_order_code like '%").append(dto.getOrderCode()).append("%' ");
			
		}
		if(dto.getOrderRepairCode()!=null && !dto.getOrderRepairCode().isEmpty()) {
			sql.append(" and r.c_order_repair_code like '%").append(dto.getOrderRepairCode()).append("%' ");
		}
		if(dto.getShopName()!=null && !dto.getShopName().isEmpty()) {
			sql.append(" and l.c_shop_name like '%").append(dto.getShopName()).append("%' ");
		}
		//城市
		if(dto.getCity()!=null && !dto.getCity().isEmpty()) {
			sql.append(" and q.c_city like '%").append(dto.getCity()).append("%' ");
		}
		if(dto.getCity()!=null && !dto.getCity().isEmpty()) {
			sql.append(" or r.c_city like '%").append(dto.getCity()).append("%' ");
		}
		//商品名称
		if(dto.getGoodsName()!=null && !dto.getGoodsName().isEmpty()){
			sql.append(" and s.c_name like '%").append(dto.getGoodsName()).append("%' ");
		}
//		if(dto.getGoodsName()!=null && !dto.getGoodsName().isEmpty()){
//			sql.append(" or r.c_goods_name like '%").append(dto.getGoodsName()).append("%' ");
//		}
		if(dto.getOrderRepairCode()!=null && !dto.getOrderRepairCode().isEmpty()){
			sql.append(" and r.c_order_repair_code like '%").append(dto.getOrderRepairCode()).append("%' ");
		}
		//提取状态
		if(dto.getShopConsume()!=null && !dto.getShopConsume().isEmpty()){
			sql.append(" and r.c_is_extract = '").append(dto.getShopConsume()).append("'");
		}
		if(dto.getExpressCompany()!=null && !dto.getExpressCompany().isEmpty()) {
			sql.append(" and l.c_express_company like '%").append(dto.getExpressCompany()).append("%' ");
		}
		if(dto.getExpressNumber() != null && !dto.getExpressNumber().isEmpty()){//添加物流单号限定条件
			sql.append(" and l.c_express_number like '%").append(dto.getExpressNumber()).append("%'");
		}
		/**
		 * 订单类型
		 */
		if(dto.getOrderReset() !=null&& !dto.getOrderReset().isEmpty()){
			sql.append(" and q.c_reset = '").append(dto.getOrderReset()).append("'");
		}
		//订单性质
		if(dto.getOrderCharacter()!=null&& !dto.getOrderCharacter().isEmpty()){
			sql.append(" and q.c_order_character = '").append(dto.getOrderCharacter()).append("'");
		}
		if(dto.getOrderDetailGoodsSn()!=null&& !dto.getOrderDetailGoodsSn().isEmpty()){
			sql.append(" and o.c_goods_sn  like '%").append(dto.getOrderDetailGoodsSn()).append("%'");
		}
if(!"".equals(dto.getStartTime())&&dto.getStartTime() != null  && !"".equals(dto.getEndTime())&&dto.getEndTime() != null ){//添加同时包含起始发货日期和截止发货日期的限定条件
			
			switch(dto.getLogisticType()){
			case "1": sql.append(" and l.c_delivery_time between '").append(dto.getStartTime()).append(" 00:00:00'").append(" and '").append(dto.getEndTime()).append(" 23:59:59'");break;
			case "0": sql.append(" and l.c_send_time between '").append(dto.getStartTime()).append(" 00:00:00'").append(" and '").append(dto.getEndTime()).append(" 23:59:59'");break;
			}			
		}
		else if(!"".equals(dto.getStartTime())&&dto.getStartTime() != null  && (dto.getEndTime() == null || dto.getEndTime().isEmpty() )){//添加只包含起始发货日期而不包含截止发货日期的限定条件
			
			switch(dto.getLogisticType()){
			case "1": sql.append(" and l.c_delivery_time >= '").append(dto.getStartTime()).append(" 00:00:00'");break;
			case "0": sql.append(" and l.c_send_time >= '").append(dto.getStartTime()).append(" 00:00:00'");break;
			}
			
		}
		else if (((dto.getStartTime() == null || dto.getStartTime().isEmpty()) && !"".equals(dto.getEndTime())&&dto.getEndTime() != null)) {//添加只包含截止发货日期而不包含起始发货日期的限定条件
			
			switch(dto.getLogisticType()){
			case "1": sql.append(" and l.c_delivery_time"
					+ " <= '").append(dto.getEndTime()).append(" 23:59:59'");break;
			case "0": sql.append(" and l.c_send_time <= '").append(dto.getEndTime()).append(" 23:59:59'");break;
			}			
		}
		if(dto.getCustomerName()!=null&& !dto.getCustomerName().isEmpty()){
			sql.append(" and  r.c_customer_name  like '%").append(dto.getCustomerName()).append("%'");
		}
		if(dto.getCustomerName()!=null&& !dto.getCustomerName().isEmpty()){
			sql.append(" or  q.c_customer_name  like '%").append(dto.getCustomerName()).append("%'");
		}
		List<BigInteger> result = dao.queryBySql(sql.toString());	
		return result == null || result.isEmpty() ? 0L : result.get(0).longValue();
	}
	public List<LogisticExportModel> queryLogisticWithParam1(LogisticDto  dto) {
		List<LogisticExportModel> list=new ArrayList<LogisticExportModel>();
		StringBuilder sql = new StringBuilder("SELECT l.c_express_number AS expressNumber,l.c_express_company AS expressCompany,l.c_send_time AS sendTime,l.c_delivery_time AS deliveryTime,r.c_city AS orderRepairCity,q.c_city AS orderCity,l.c_shop_name AS shopName,q.c_customer_name AS orderCustomerName,r.c_customer_name AS customerName,q.c_order_code AS orderCode,r.c_order_repair_code AS orderRepairCode,s.c_name AS goodsName,r.c_goods_name AS orderRepairGoodsName,o.c_num AS Num,h.c_color_name AS GoodsColorName,r.c_num AS orderRepairNum,l.c_express_price AS expressPrice,l.c_settlement_type AS settlementType,r.c_is_extract AS shopConsume,r.c_repair_reason AS repqirContext,q.c_order_character AS orderCharacter,r.c_order_character AS orderRepairCharacter,o.c_goods_sn AS goodsSn " +
				"FROM t_logistic_order t  " +
				"LEFT JOIN t_logistic l ON l.c_id=t.c_logistic_id " +
				"LEFT JOIN t_order_detail o ON o.c_id=t.c_order_detail_id " +
				"LEFT JOIN t_order_repair r ON r.c_id=t.c_order_repair_id " +
				"LEFT JOIN t_order q ON q.c_id = o.c_order_id " +
				"LEFT JOIN t_goods_detail h ON o.c_goods_detail_id=h.c_id  " +
				"LEFT JOIN t_goods s ON h.c_goods_id=s.c_id " +
				"LEFT JOIN t_xiuyu_customer z ON q.c_customer_id= z.c_id " +
				"WHERE l.c_delete_flag='0' ");
				if(dto.getLogisticType()!=null) {
					switch(dto.getLogisticType()){
					case "1": sql.append(" AND l.c_logistic_type = '1' and r.c_del='0' and t.c_delete_flag = '0'");break;
					case "0": sql.append(" AND l.c_logistic_type = '0' and t.c_delete_flag = '0'");
					if(dto.getOrderType()!=null && !dto.getOrderType().isEmpty()){
						if(dto.getOrderType().equals("0")){
							sql.append(" AND t.c_order_detail_id is not null AND t.c_order_repair_id is null ");
							if(StringUtils.isNotBlank(dto.getTenantId())) {
								sql.append(" AND q.c_tenant_id='").append(dto.getTenantId()).append("'");
							}
						}else if(dto.getOrderType().equals("1")){
							sql.append(" AND t.c_order_repair_id is not null AND t.c_order_detail_id is null");
							if(StringUtils.isNotBlank(dto.getTenantId())) {
								sql.append(" AND r.c_tenant_id='").append(dto.getTenantId()).append("'");
							}
						}
					}else{
						if(StringUtils.isNotBlank(dto.getTenantId())) {
							sql.append(" AND (q.c_tenant_id='").append(dto.getTenantId()).append("'");
							sql.append(" OR r.c_tenant_id='").append(dto.getTenantId()).append("')");
						}
					}
					break;
					}
				}

				if(StringUtils.isNotBlank(dto.getUserId())){
					sql.append(" and l.c_shop_id in (select c_shop_id from t_xiuyu_shop_director_relation where c_user_id='").append(dto.getUserId()).append("')");
				}


		//订单类型   1微调单  0定制订单 
				
				//城市
				if(dto.getCity()!=null && !dto.getCity().isEmpty()) {
					sql.append(" and q.c_city like '%").append(dto.getCity()).append("%' ");
				}
				if(dto.getCity()!=null && !dto.getCity().isEmpty()) {
					sql.append(" or r.c_city like '%").append(dto.getCity()).append("%' ");
				}
				//商品名称
				if(dto.getGoodsName()!=null && !dto.getGoodsName().isEmpty()){
					sql.append(" and o.c_goods_name like '%").append(dto.getGoodsName()).append("%' ");
				}
				if(dto.getGoodsName()!=null && !dto.getGoodsName().isEmpty()){
					sql.append(" or r.c_goods_name like '%").append(dto.getGoodsName()).append("%' ");
				}
				
				//提取状态
				if(dto.getShopConsume()!=null && !dto.getShopConsume().isEmpty()){
					sql.append(" and r.c_is_extract = '").append(dto.getShopConsume()).append("'");
				}
				/**
				 * 订单类型
				 */
				if(dto.getOrderReset() !=null&& !dto.getOrderReset().isEmpty()){
					sql.append(" and q.c_reset = '").append(dto.getOrderReset()).append("'");
				}
				//订单性质
				if(dto.getOrderCharacter()!=null&& !dto.getOrderCharacter().isEmpty()){
					sql.append(" and q.c_order_character = '").append(dto.getOrderCharacter()).append("'");
				}
				if(dto.getOrderDetailGoodsSn()!=null&& !dto.getOrderDetailGoodsSn().isEmpty()){
					sql.append(" and o.c_goods_sn  like '%").append(dto.getOrderDetailGoodsSn()).append("%'");
				}
		if(dto.getOrderCode()!=null&&!dto.getOrderCode().isEmpty()){
			sql.append(" and q.c_order_code like '%").append(dto.getOrderCode()).append("%' ");
			
		}
		if(dto.getCustomerName()!=null&& !dto.getCustomerName().isEmpty()){
			sql.append(" and  r.c_customer_name  like '%").append(dto.getCustomerName()).append("%'");
		}
		
		if(dto.getCustomerName()!=null&& !dto.getCustomerName().isEmpty()){
			sql.append(" or  q.c_customer_name  like '%").append(dto.getCustomerName()).append("%'");
		}
		
		if(dto.getOrderRepairCode()!=null && !dto.getOrderRepairCode().isEmpty()) {
			sql.append(" and r.c_order_repair_code like '%").append(dto.getOrderRepairCode()).append("%' ");
		}
		if(dto.getShopName()!=null && !dto.getShopName().isEmpty()) {
			sql.append(" and l.c_shop_name like '%").append(dto.getShopName()).append("%' ");
		}
		if(dto.getExpressCompany()!=null && !dto.getExpressCompany().isEmpty()) {
			sql.append(" and l.c_express_company like '%").append(dto.getExpressCompany()).append("%' ");
		}
		if(dto.getExpressNumber() != null && !dto.getExpressNumber().isEmpty()){//添加物流单号限定条件
			sql.append(" and l.c_express_number like '%").append(dto.getExpressNumber()).append("%'");
		}
		if(dto.getStartTime() != null  && dto.getEndTime() != null && dto.getEndTime()!=""&&dto.getStartTime()!="" ){//添加同时包含起始发货日期和截止发货日期的限定条件
			switch(dto.getLogisticType()){
			case "1": sql.append(" and l.c_delivery_time between '").append(dto.getStartTime()).append(" 00:00:00'").append(" and '").append(dto.getEndTime()).append(" 23:59:59'");break;
			case "0": sql.append(" and l.c_send_time between '").append(dto.getStartTime()).append(" 00:00:00'").append(" and '").append(dto.getEndTime()).append(" 23:59:59'");break;
			}			
		}
		else if(dto.getStartTime() != null && dto.getStartTime()!="" && (dto.getEndTime() == null &&dto.getEndTime()=="")){//添加只包含起始发货日期而不包含截止发货日期的限定条件
			switch(dto.getLogisticType()){
			case "1": sql.append(" and l.c_delivery_time >= '").append(dto.getStartTime()).append(" 00:00:00'");break;
			case "0": sql.append(" and l.c_send_time >= '").append(dto.getStartTime()).append(" 00:00:00'");break;
			}
			
		}
		else if ((dto.getStartTime() == null && dto.getStartTime()==""&& dto.getEndTime() != null&&dto.getEndTime()!="")) {//添加只包含截止发货日期而不包含起始发货日期的限定条件
			switch(dto.getLogisticType()){
			case "1": sql.append(" and l.c_delivery_time"
					+ " <= '").append(dto.getEndTime()).append(" 23:59:59'");break;
			case "0": sql.append(" and l.c_send_time <= '").append(dto.getEndTime()).append(" 23:59:59'");break;
			}
		}
		System.out.println(sql.toString());
		List resultSet = dao.queryBySql(sql.toString());
			for(Object result : resultSet){
				LogisticExportModel rsModel = new LogisticExportModel();
				Object[] properties = (Object[])result;
				rsModel.setExpressNumber(properties[0]==null ? "" : properties[0].toString());
				rsModel.setExpressCompany(properties[1]==null ? "" : properties[1].toString());
				rsModel.setSendTime(time(properties[2]==null ? "" : properties[2].toString()));
				rsModel.setDeliveryTime(time(properties[3]==null ? "" : properties[3].toString()));
				rsModel.setOrderRepairCity(properties[4]==null ? "" : properties[4].toString());
				rsModel.setOrderCity(properties[5]==null ? "" : properties[5].toString());
				rsModel.setShopName(properties[6]==null ? "" : properties[6].toString());
				rsModel.setOrderCustomerName(properties[7]==null ? "" : properties[7].toString());
				rsModel.setCustomerName(properties[8]==null ? "" : properties[8].toString());
				rsModel.setOrderCode(properties[9]==null ? "" : properties[9].toString());
				rsModel.setOrderRepairCode(properties[10]==null ? "" : properties[10].toString());
				rsModel.setGoodsName(properties[11]==null ? "" : properties[11].toString());
				rsModel.setOrderRepairGoodsName(properties[12]==null ? "" : properties[12].toString());
				rsModel.setNum(properties[13]==null ? "" : properties[13].toString());
				rsModel.setGoodsColorName(properties[14]==null?"":properties[14].toString());
				rsModel.setOrderRepairNum(properties[15]==null ? "" : properties[15].toString());
				rsModel.setExpressPrice(properties[16]==null ? "" : properties[16].toString());
				rsModel.setSettlementType(properties[17]==null ? "" : properties[17].toString());
				rsModel.setShopConsume(isextract(properties[18]==null ? "" : properties[18].toString()));
				rsModel.setRepairContext(properties[19]==null ? "" : properties[19].toString());
				rsModel.setOrderCharacter(properties[20]==null ? "" : properties[20].toString());
				rsModel.setOrderRepairCharacter(properties[21]==null ? "" : properties[21].toString());
				rsModel.setGoodsSn(properties[22]==null ? "" : properties[22].toString());
				list.add(rsModel);
			}
			
		return list;
	}
		public String isextract(String shopconsume){
		String isextract = null;
		if(shopconsume.equals("0")){
			isextract = "未提取";
		}else if(shopconsume.equals("1")){
			isextract = "已提取";
		}
		return isextract;
		}

	@Override
	public Result exportExcelData(LogisticDto dto, HttpServletRequest request, HttpServletResponse response) {
		
		String excelFileName = "1".equals(dto.getLogisticType()) ? "收货明细查询结果" : "1".equals(dto.getOrderType()) ? "微调单发货明细查询结果" : "订单发货明细查询结果";
		OutputStream out = null;
		try {
			
			//转码防止乱码
			final String userAgent = request.getHeader("USER-AGENT");
			if(userAgent.toLowerCase().contains("msie")){//IE浏览器  
		    	excelFileName = URLEncoder.encode(excelFileName,"UTF8");  
		    }else if(userAgent.toLowerCase().contains( "mozilla") || userAgent.toLowerCase().contains("chrom")){//google浏览器,火狐浏览器  
		    	excelFileName = new String(excelFileName.getBytes(), "ISO8859-1");  
		    }else{  
		    	excelFileName = URLEncoder.encode(excelFileName,"UTF8");//其他浏览器  
		    } 			
			response.setContentType("application/vnd.ms-excel;charset=UTF-8");
			response.addHeader("content-Disposition", "attachment;filename="+ excelFileName +".xls");
			response.flushBuffer();
			List<LogisticExportModel> resultList = queryLogisticWithParam1(dto);
			String[] headers = null;
			Set<String> excludedFieldSet = new HashSet<String>();
			excludedFieldSet.add("serialVersionUID");
			switch(dto.getLogisticType()){
				case "1": 
					headers =new String[]{"物流单号","物流公司","收货日期","发货门店","下单商户","顾客姓名","微调单号","商品名称","数量","物流费","结算类型","顾客提取状态","微调内容"};
					excludedFieldSet.add("sendTime");
					excludedFieldSet.add("orderRepairCity");
					excludedFieldSet.add("orderCity");
					excludedFieldSet.add("orderCustomerName");
					excludedFieldSet.add("orderCode");
					excludedFieldSet.add("goodsName");
					excludedFieldSet.add("GoodsColorName");
					excludedFieldSet.add("Num");
					excludedFieldSet.add("orderCharacter");
					excludedFieldSet.add("orderRepairCharacter");
					excludedFieldSet.add("goodsSn");
					break;
				case "0": 
					if(dto.getOrderType()!=null && !dto.getOrderType().isEmpty()){
						switch(dto.getOrderType()){
						case "1"://微调单物流信息导出
							headers =new String[]{"物流单号","物流公司","发货日期","收货日期","城市","收货门店","下单商户","顾客姓名","微调单号","商品名称","商品颜色","数量","物流费","结算类型","顾客提取状态","微调内容","订单性质","商品唯一码"};
							//excludedFieldSet.add("expressNumber");
							//excludedFieldSet.add("expressCompany");
							//excludedFieldSet.add("sendTime");
							//excludedFieldSet.add("deliveryTime");
							//excludedFieldSet.add("orderRepairCity");
							excludedFieldSet.add("orderCity");
							//excludedFieldSet.add("shopName");
							excludedFieldSet.add("orderCustomerName");
//							excludedFieldSet.add("customerName");
							excludedFieldSet.add("orderCode");
//							excludedFieldSet.add("orderRepairCode");
							excludedFieldSet.add("goodsName");
//							excludedFieldSet.add("orderRepairGoodsName");
//							excludedFieldSet.add("GoodsColorName");
							excludedFieldSet.add("Num");
//							excludedFieldSet.add("orderRepairNum");
							//excludedFieldSet.add("expressPrice");
							//excludedFieldSet.add("settlementType");
//							excludedFieldSet.add("shopConsume");
//							excludedFieldSet.add("repairContext");
//							excludedFieldSet.add("orderCharacter");
							excludedFieldSet.add("orderRepairCharacter");
							//excludedFieldSet.add("goodsSn");
							break;
						case "0"://订单物流信息导出
							headers =new String[]{"物流单号","物流公司","发货日期","城市","收货门店","下单商户","顾客姓名","订单单号","商品名称","数量","物流费","结算类型","订单性质"};
							//excludedFieldSet.add("expressNumber");
							//excludedFieldSet.add("expressCompany");
							//excludedFieldSet.add("sendTime");
							excludedFieldSet.add("deliveryTime");
							excludedFieldSet.add("orderRepairCity");
							//excludedFieldSet.add("orderCity");
							//excludedFieldSet.add("shopName");
							//excludedFieldSet.add("orderCustomerName");
							excludedFieldSet.add("customerName");
							//excludedFieldSet.add("orderCode");
							excludedFieldSet.add("orderRepairCode");
							//excludedFieldSet.add("goodsName");
							excludedFieldSet.add("orderRepairGoodsName");
							excludedFieldSet.add("GoodsColorName");
							//excludedFieldSet.add("Num");
							excludedFieldSet.add("orderRepairNum");
							//excludedFieldSet.add("expressPrice");
							//excludedFieldSet.add("settlementType");
							excludedFieldSet.add("shopConsume");
							excludedFieldSet.add("repairContext");
							//excludedFieldSet.add("orderCharacter");
							excludedFieldSet.add("orderRepairCharacter");
							excludedFieldSet.add("goodsSn");
							break;
						}
					}else{
						headers =new String[]{"物流单号","物流公司","发货日期","收货日期","城市","收货门店","下单商户","顾客姓名","订单单号","微调单号","商品名称","数量","物流费","结算类型","顾客提取状态","微调内容","订单性质"};
						//excludedFieldSet.add("expressNumber");
						//excludedFieldSet.add("expressCompany");
						//excludedFieldSet.add("sendTime");
						//excludedFieldSet.add("deliveryTime");
						//excludedFieldSet.add("orderRepairCity");
						//excludedFieldSet.add("orderCity");
						//excludedFieldSet.add("shopName");
						//excludedFieldSet.add("orderCustomerName");
						//excludedFieldSet.add("customerName");
						//excludedFieldSet.add("orderCode");
						//excludedFieldSet.add("orderRepairCode");
						//excludedFieldSet.add("goodsName");
						//excludedFieldSet.add("orderRepairGoodsName");
						//excludedFieldSet.add("Num");
						//excludedFieldSet.add("orderRepairNum");
						//excludedFieldSet.add("expressPrice");
						//excludedFieldSet.add("settlementType");
						//excludedFieldSet.add("shopConsume");
						//excludedFieldSet.add("repairContext");
						//excludedFieldSet.add("orderCharacter");
						excludedFieldSet.add("orderRepairCharacter");
						excludedFieldSet.add("goodsSn");
					}
										
					break;
			}			
			
			 out = response.getOutputStream();  
			ExportExcelUtil.exportExcel(0,0,0,dto.getLogisticType().equals("1") ? "收货明细" : "发货明细", headers,ExportExcelUtil.buildCustomizedExportedModel(resultList, excludedFieldSet) , out, "yyyy-MM-dd");			
		} catch (IOException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new Result();
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public Result rollBackCheckStatus(String checkUserId,String id, String logisticType) {
		Criteria criteria = dao.createCriteria(Logistic.class);
		criteria.add(Restrictions.eq("id",id));
		List<Logistic> list = criteria.list();
		Boolean checkStatus = true;
		String logistictype = null;
		if(logisticType!=null) {
			if(logisticType.equals("0")){
				logistictype="发货";
			}else if(logisticType .equals("1")){
				logistictype="收货";
			}
		}
		switch(logistictype) {
		case"收货":
			for (Logistic logistic:list){
				Boolean status =rollBackOrderRepairStatus(logistic);
				 if (status == false){
				 	checkStatus = false;
					return new Result("500","该收货单中微调单的状态不是已收货，不能取消审核");
				 }

				logistic.setCheckStatus("0");
				logistic.setCheckTime(new Date());
				logistic.setCheckUserId(checkUserId);
				//记录取消审核物流单
		/*		 HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(logistic.getId(), "Logistic", new Date(),user.getUserName()+"取消审核收货物流单:"+logistic.getExpressNumber(),JSON.toJSONString(logistic), user.getId());
				 dao.save(historyRecord);*/
				 			}
			break;
		case"发货":
			for (Logistic logistic: list){
				logistic.setCheckStatus("0");
				logistic.setCheckTime(new Date());
				logistic.setCheckUserId(checkUserId);
				//记录取消审核物流单
	/*			 HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(logistic.getId(), "Logistic", new Date(),user.getUserName()+"取消审核发货物流单:"+logistic.getExpressNumber(),JSON.toJSONString(logistic), user.getId());
				 dao.save(historyRecord);*/
				rollBackDeliveryStatus(logistic);
			}
			break;
		}
//		if (checkStatus == false){
//			return new Result("该收货单中微调单的状态不是已收货，不能取消审核");
//		}
	
		return new Result();
	}
	 //回滚收货单微调单的状态为星域审核通过
    private Boolean rollBackOrderRepairStatus(Logistic logistic){
        List<String> orderRepairIdList = new ArrayList<>();
        //如果物流单信息中包含微调单信息
        if (logistic.getList()!=null){
        	Boolean checkStatus = true;
            for (LogisticOrder logistic1:logistic.getList()){
				if (logistic1.getOrderRepair() != null) {
					OrderRepair orderRepair=dao.findById(OrderRepair.class, logistic1.getOrderRepair().getId());
					if (!"8".equals(orderRepair.getOrderRepairStatus())){
						checkStatus = false;
						break;
					}
					orderRepairIdList.add(orderRepair.getId());
				}
			}
			if (checkStatus == false){
            	return checkStatus;
			}
            Criteria criteria = dao.createCriteria(OrderRepair.class);
            criteria.add(Restrictions.in("id",orderRepairIdList));
            @SuppressWarnings("unchecked")
            List<OrderRepair> orderRepairList = criteria.list();
            for (OrderRepair orderRepair :orderRepairList){
                orderRepair.setOrderRepairStatus("2");
                //记录收货物流单取消审核时微调单的状态变化
            /*    HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(orderRepair.getId(), "OrderRepair", new Date(),user.getUserName()+"取消审核物流单中的微调单:"+orderRepair.getRepairCode(),JSON.toJSONString(orderRepair), user.getId());
    			dao.save(historyRecord);*/
            }
            return checkStatus;
        }
        return true;
    }
	 //回滚发货单中订单以及微调单的状态为生产完成状态
    private void rollBackDeliveryStatus(Logistic logistic){
        List<String> orderRepairIdList = new ArrayList<>();
        List<String> orderDetialIdList = new ArrayList<>();
       
        if (logistic.getList()!=null){
        			for (LogisticOrder logisticOrder:logistic.getList()){
                    	if (logisticOrder != null){
                    		if(logisticOrder.getOrderRepair()!=null){
                    			if(logisticOrder.getOrderRepair().getId()!=null){
                        			OrderRepair orderRepair	=dao.findById(OrderRepair.class, logisticOrder.getOrderRepair().getId());
                           		 if (orderRepair!= null) {
                           			 if(StringUtils.isNotBlank(orderRepair.getId())){
                           				 orderRepairIdList.add(orderRepair.getId());
                           			 }
                                    }
                        		}
                    		}
                    		if(logisticOrder.getOrderDetail()!=null){
                    			if(logisticOrder.getOrderDetail().getId()!=null){
                        			OrderDetail orderDetail	=dao.findById(OrderDetail.class, logisticOrder.getOrderDetail().getId());
                                    if (orderDetail!= null) {
                                   	 if(StringUtils.isNotBlank(orderDetail.getId())){
                                        orderDetialIdList.add(orderDetail.getId());
                                   	 }
                                    }
                    			}
                    		}
                    		
                    	} 
                    }
                    	 
        			
//            for (LogisticOrder logisticOrder:logistic.getList()){
//            	if (logisticOrder != null){
//            		OrderRepair orderRepair	=dao.findById(OrderRepair.class, logisticOrder.getOrderRepair().getId());
//            		 if (orderRepair!= null) {
//            			 if(StringUtils.isNotBlank(orderRepair.getId())){
//            				 orderRepairIdList.add(orderRepair.getId());
//            			 }
//                     }
//            		 OrderDetail orderDetail	=dao.findById(OrderDetail.class, logisticOrder.getOrderDetail().getId());
//                     if (orderDetail!= null) {
//                    	 if(StringUtils.isNotBlank(orderDetail.getId())){
//                         orderDetialIdList.add(orderDetail.getId());
//                     }
//            	} 
//            }
			if (orderRepairIdList.size() > 0) {
				Criteria criteria1 = dao.createCriteria(OrderRepair.class);
				criteria1.add(Restrictions.in("id",orderRepairIdList));
				@SuppressWarnings("unchecked")
				List<OrderRepair> orderRepairList = criteria1.list();
				for (OrderRepair orderRepair : orderRepairList){
                    orderRepair.setOrderRepairStatus("6");
                	//记录星域审核通过信息
       /* 			HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(orderRepair.getId(), "OrderRepair", new Date(),user.getUserName()+"取消审核微调单:"+orderRepair.getRepairCode(),JSON.toJSONString(orderRepair), user.getId());
        			dao.save(historyRecord);  */
                }
			}
			if (orderDetialIdList.size() > 0){
				Criteria criteria2 = dao.createCriteria(OrderDetail.class);
				criteria2.add(Restrictions.in("id",orderDetialIdList));
				@SuppressWarnings("unchecked")
				List<OrderDetail> orderDetailList = criteria2.list();
				for (OrderDetail orderDetail:orderDetailList){
					orderDetail.setOrderDetailStatus("2");
					//记录星域审核通过信息
        	/*		HistoryRecord historyRecord =HistoryRecordUtil.historyRecord(orderDetail.getId(), "OrderRepair", new Date(),user.getUserName()+"取消审核订单:"+orderDetail.getOrderId(),JSON.toJSONString(orderDetail), user.getId());
        			dao.save(historyRecord);  */
					
				}

			}
			}
		}

	@Override
	public Result updateLogisticStatus(String logisticId, String logisticStatus) {
		Logistic logistic = dao.findById(Logistic.class, logisticId);
		logistic.setLogisticStatus(logisticStatus);
		logistic.setDeliveryTime(new Date());
		return new Result(logistic);
	}

	@Override
	public Result selectLogisticOne(String logisticId) {
		Logistic logistic = dao.findById(Logistic.class, logisticId);
		return new Result(logistic);
	}

	@Override 
	public List<OrderRepair> queryOrderRepair(String q,String logisticType) {
		Criteria criteria = dao.createCriteria(OrderRepair.class);
		Disjunction disjunction = Restrictions.disjunction();
		if(!StringUtils.isBlank(q)){
			disjunction .add(Restrictions.like("orderRepairCode", q, MatchMode.ANYWHERE));
		}
		if(!StringUtils.isBlank(logisticType)){
			switch (logisticType) {
			case "1":
				criteria.add(Restrictions.eq("orderRepairStatus", "2"));
				break;
			case "0":
				criteria.add(Restrictions.eq("orderRepairStatus", "6"));
				break;
			default:
				break;
			}
		}
		criteria.add(disjunction);
		PageBean pageBean = new PageBean();
		pageBean.setPage(0);
		pageBean.setRows(30);
		return dao.findListWithPagebeanCriteria(criteria, pageBean);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<Dept> queryTenant(String q,String tenantId) {
		Criteria criteria = dao.createCriteria(Dept.class);
		Tenant tenant = dao.findById(Tenant.class, tenantId);
		criteria.add(Restrictions.eq("deptTenantId", tenant));
		List<Dept> list = criteria.list();
		if(q!=null && !"".equals(q)){
			for( int i=0;i<list.size();i++){
				criteria.add(Restrictions.eq("deptName", q));
				criteria.add(Restrictions.eq("deptType", "1"));
			}
		}
		//Disjunction disjunction = Restrictions.disjunction();
		/*if(!StringUtils.isBlank(q)){
			//criteria .add(Restrictions.eq("deptType","1")).add(Restrictions.like("deptName", q, MatchMode.ANYWHERE));
			criteria.add(Restrictions.like("deptName", q, MatchMode.ANYWHERE));
		}*/
		//criteria.add(disjunction);
		PageBean pageBean = new PageBean();
		pageBean.setPage(0);
		pageBean.setRows(30);
		return dao.findListWithPagebeanCriteria(criteria, pageBean);
	}

	@Override
	public List<XiuyuCustomer> queryXiuyuCustomer(String q) {
		Criteria criteria = dao.createCriteria(XiuyuCustomer.class);
		Disjunction disjunction = Restrictions.disjunction();
		if(!StringUtils.isBlank(q)){
			disjunction .add(Restrictions.like("customerName", q, MatchMode.ANYWHERE));
		}
		criteria.add(disjunction);
		PageBean pageBean = new PageBean();
		pageBean.setPage(0);
		pageBean.setRows(30);
		return dao.findListWithPagebeanCriteria(criteria, pageBean);
	}

	@Override
	public List<Order> queryOrder(String q) {
		Criteria criteria = dao.createCriteria(Order.class);
		criteria.add(Restrictions.eq("deleteFlag", "0"));
		Disjunction disjunction = Restrictions.disjunction();
		if(!StringUtils.isBlank(q)){
			disjunction .add(Restrictions.like("orderCode", q, MatchMode.ANYWHERE));
		}
		criteria.add(disjunction);
		PageBean pageBean = new PageBean();
		pageBean.setPage(0);
		pageBean.setRows(30);
		return dao.findListWithPagebeanCriteria(criteria, pageBean);
	}
	

}
