package com.kongque.controller.repair;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.kongque.dto.OrderRepairDto;
import com.kongque.entity.repair.OrderRepairCheck;
import com.kongque.model.OrderRepairModel;
import com.kongque.service.repair.IOrderRepairService;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

@Controller
@RequestMapping("/repair")
public class OrderRepairController {
	@Resource
	private IOrderRepairService service;
	private static Logger logger = LoggerFactory.getLogger(OrderRepairController.class);

	/*
	 * 根据条件分页查询微调单
	 * 
	 * @param dto
	 * 
	 * @return
	 * 
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public Pagination<OrderRepairModel> list(OrderRepairDto dto, PageBean pageBean) {
		logger.info(dto.toString(), pageBean.toString());
		return service.list(dto, pageBean);
	}

	/*
	 * 添加或修改微调单
	 * 
	 * @param dto
	 * 
	 * @return
	 * 
	 */
	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
	@ResponseBody
	public Result saveOrUpdate(OrderRepairDto dto, MultipartFile[] files) {
		logger.info(dto.toString());
		return service.saveOrUpdate(dto, files);
	}

	/*
	 * 根据id查询某一微调单
	 * 
	 * @param id
	 * 
	 * @return
	 * 
	 */
	@RequestMapping(value = "/RepairById", method = RequestMethod.GET)
	@ResponseBody
	public Result RepairById(String id) {
		return service.RepairById(id);
	}


	/*
	 * 根据id查询某一微调单
	 *
	 * @param id
	 *
	 * @return
	 *
	 */
	@RequestMapping(value = "/RepairNoHistoryById", method = RequestMethod.GET)
	@ResponseBody
	public Result RepairNoHistoryById(String id) {
		return service.RepairNoHistoryById(id);
	}

	/*
	 * 审核微调单
	 * 
	 * @param id
	 * 
	 * @return
	 * 
	 */
	@RequestMapping(value = "/checkRepair", method = RequestMethod.POST)
	@ResponseBody
	public Result checkRepair(@RequestBody OrderRepairCheck dto) {
		logger.info(dto.toString());
		return service.checkRepair(dto);
	}



	/*
	 * 修改微调单状态
	 * 
	 * @param id
	 * 
	 * @return
	 * 
	 */
	@RequestMapping(value = "/changeStatus", method = RequestMethod.POST)
	@ResponseBody
	public Result changeStatus(String id, String status) {
		logger.info("修改微调单状态", id.toString(), status);
		return service.changeStatus(id, status);
	}
	
	/**
	 * 根据条件模糊查询微调单
	 * @param q
	 * @return
	 */
	@RequestMapping(value = "/remote", method = RequestMethod.GET)
	@ResponseBody
	public Result remoteOrderRepair(String q){
		return service.remoteOrderRepair(q);
	}
	/**
	 * 删除微调单
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/del", method = RequestMethod.GET)
	@ResponseBody
	public Result del(String id) {
		return service.del(id);
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
	 * 新增/删除图片
	 * @param dto
	 * @param files
	 * @return
	 */
	@RequestMapping(value = "/file/upload", method = RequestMethod.POST)
	@ResponseBody
	public Result uploadFile(OrderRepairDto dto, MultipartFile[] files) {
		logger.info(dto.toString());
		return service.uploadFile(dto, files);
	}
	
	/**
	 * 微调单责任归属人
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/owner", method = RequestMethod.GET)
	@ResponseBody
	public Result owner(String id,String owner) {
		return service.owner(id,owner);
	}
	
	/**
	 * 微调单明细导出
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/exportExcle", method = RequestMethod.GET)
	@ResponseBody
	public void exportExcle(OrderRepairDto dto, HttpServletRequest request, HttpServletResponse response) {
		 service.exportExcle(dto, request, response);
	}
	
}
