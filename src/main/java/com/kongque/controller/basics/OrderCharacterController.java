package com.kongque.controller.basics;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.kongque.dto.OrderCharacterDto;
import com.kongque.entity.basics.OrderCharacter;
import com.kongque.service.basics.IOrderCharacterService;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

@Controller
@RequestMapping("/orderCharacter")
public class OrderCharacterController {
	private static Logger logger = LoggerFactory.getLogger(OrderCharacterController.class);
	@Resource
	private IOrderCharacterService service;

	/**
	 * 根据条件分页查询地区列表
	 * 
	 * @param dto
	 * @param pageBean
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Pagination<OrderCharacter> list(OrderCharacterDto dto, PageBean pageBean) {
		return service.orderCharacterList(dto, pageBean);
	}

	
	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
	@ResponseBody
	public Result saveOrUpdate(@RequestBody OrderCharacterDto dto) {
		logger.info(dto.toString());
		return service.saveOrUpdate(dto);
	}

	
	@RequestMapping(value = "/updateStatus", method = RequestMethod.GET)
	@ResponseBody
	public Result delete(String orderCharacterId, String orderCharacterStatus) {
		return service.updateStatus(orderCharacterId,orderCharacterStatus);
	}

	/**
	 * 按照商户分类显示订单性质
	 * @param dto
	 * @return
	 */
	@RequestMapping(value = "/listByTenant", method = RequestMethod.GET)
	@ResponseBody
	public Result listByTenant(OrderCharacterDto dto) {
		return service.listByTenant(dto);
	}

}
