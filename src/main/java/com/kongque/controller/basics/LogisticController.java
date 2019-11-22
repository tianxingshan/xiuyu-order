package com.kongque.controller.basics;

import java.text.ParseException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kongque.dto.LogisticDto;
import com.kongque.entity.basics.Dept;
import com.kongque.entity.basics.Logistic;
import com.kongque.entity.basics.XiuyuCustomer;
import com.kongque.entity.order.Order;
import com.kongque.entity.repair.OrderRepair;
import com.kongque.model.LogisticOrderModel;
import com.kongque.service.basics.ILogisticService;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

@RestController
@RequestMapping("/logistic")
public class LogisticController {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LogisticController.class);
	@Resource
	private ILogisticService service;
	
	/**
	 * 根据条件分页查询列表
	 * @param dto
	 * @param pageBean
	 * @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public  Pagination<Logistic> List(LogisticDto dto, PageBean pageBean){
		return service.list(dto,pageBean);
	}
	
	/**
	 * 保存或更新门店信息
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
	public  Result saveOrUpdate(@RequestBody LogisticDto dto) {
		return service.saveOrUpdate(dto);
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
    @RequestMapping(value ="/delete", method = RequestMethod.GET)
    public Result delete(String id){
        return service.delete(id);
    }
    /**
	 * 审核通过 
	 * @param id 
	 * @return
	 */
    @RequestMapping(value ="/updateStatus", method = RequestMethod.GET)
    public  Result updateStatus(String checkUserId,String id,String checkStatus){
        return service.updateStatus(checkUserId,id, checkStatus);
    }
    /**
	 *  取消审核
	 * @param id 
	 * @return
	 */
    @RequestMapping(value ="/rollBackStatus", method = RequestMethod.GET)
    public  Result rollBackStatus(String checkUserId,String id, String logisticType){
        return service.rollBackCheckStatus(checkUserId,id, logisticType);
    }
    
    @RequestMapping(value="/listLogisticOrder",method=RequestMethod.GET)
	public  Pagination<LogisticOrderModel> List(LogisticDto dto, Integer page,Integer rows) throws ParseException{
		Pagination<LogisticOrderModel> pagination = new Pagination<>();	
		Long total = service.queryLogisticCountWithParam(dto);
		if(total != null){
			pagination.setTotal(total);
		}
		List<LogisticOrderModel> resultList = service.queryLogisticWithParam(dto,page,rows);
		if(resultList != null){			
			pagination.setRows(resultList);			
		}		
		return pagination;
	}
	
	@RequestMapping(value="/exportExcel",method=RequestMethod.GET)
	public  Result List(LogisticDto dto,HttpServletRequest request,HttpServletResponse response){
		return service.exportExcelData(dto,request,response);
	}
	//确认收货
	@RequestMapping(value ="/updateLogisticStatus", method = RequestMethod.POST)
	public Result updateLogisticStatus(String logisticId,String logisticStatus){
		return service.updateLogisticStatus(logisticId,logisticStatus);
	}
	@RequestMapping(value="/selectLogisticOne",method=RequestMethod.GET)
	public  Result List(String logisticId){
		return service.selectLogisticOne(logisticId);
	}
	
	@RequestMapping(value="/remote/orderRepairCode",method=RequestMethod.GET)
	public  List<OrderRepair> listOrderRepair(String q,String logisticType){
		List<OrderRepair> styleList = service.queryOrderRepair(q,logisticType);		
		return styleList;
	}
	
	@RequestMapping(value="/remote/shopName",method=RequestMethod.GET)
	public  List<Dept> listTenant(String q,String tenantId){
		List<Dept> styleList = service.queryTenant(q,tenantId);		
		return styleList;
	}
	
	@RequestMapping(value="/remote/XiuyuCustomer",method=RequestMethod.GET)
	public  List<XiuyuCustomer> listStyle(String q){
		List<XiuyuCustomer> styleList = service.queryXiuyuCustomer(q);		
		return styleList;
	}
	
	@RequestMapping(value="/remote/Order",method=RequestMethod.GET)
	public  List<Order> listOrder(String q){
		List<Order> styleList = service.queryOrder(q);		
		return styleList;
	}
	
}
