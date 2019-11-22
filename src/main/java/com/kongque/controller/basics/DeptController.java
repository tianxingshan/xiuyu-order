package com.kongque.controller.basics;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.kongque.dto.AddShopListDto;
import com.kongque.dto.DeptDto;
import com.kongque.dto.XiuyuShopDto;
import com.kongque.entity.basics.Dept;
import com.kongque.entity.basics.XiuyuShopAreaRelation;
import com.kongque.service.basics.IDeptService;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

@RestController
@RequestMapping("/dept")
public class DeptController {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DeptController.class);
	@Resource
	private IDeptService service;
	
	/**
	 * 根据条件分页查询门店列表
	 * @param dto
	 * @param pageBean
	 * @return
	 */
	@RequestMapping(value="/list",method=RequestMethod.GET)
	public  Pagination<Dept> DeptList(DeptDto dto, PageBean pageBean){
		return service.DeptList(dto,pageBean);
	}

	/**
	 * 导出门店
	 * @param request
	 * @param response
	 * @param dto
	 */
	@RequestMapping(value="/exportDeptList",method=RequestMethod.GET)
	public void exportDeptList(HttpServletRequest request, HttpServletResponse response, DeptDto dto){
		service.exportDeptList(request,response,dto);
	}


	/**
	 * 保存或更新秀域门店信息
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
	public  Result saveOrUpdate(@RequestBody XiuyuShopDto dto) {
		return service.saveOrUpdate(dto);
	}
	
	/**
	 * 根据shopId获取门店信息
	 * 
	 * @param 
	 * @return
	 */
	@RequestMapping(value = "/getShopInfoByUser", method = RequestMethod.GET)
	public  Result getShopInfoByUser(String shopId) {
		return service.getShopInfoByUser(shopId);
	}
    
	/**
	 * 获取门店父级列表
	 * @return
	 */
    @RequestMapping(value ="/list/parent", method = RequestMethod.GET)
    public Result parent(){
        return service.parent();
    }
    
    /**
	 * 根据地区id获取地区下门店列表
	 * @return
	 */
    @RequestMapping(value ="/getShopList/byAreaId", method = RequestMethod.GET)
    public Pagination<Dept> getShopListByAreaId(String areaId, PageBean pageBean){
        return service.getShopListByAreaId(areaId,pageBean);
    }
    
    /**
     * 根据城市id批量添加门店列表
     * @param dto
     * @return
     */
    @RequestMapping(value ="/addShopList", method = RequestMethod.POST)
    public Result addShopList(@RequestBody AddShopListDto dto){
        return service.addShopList(dto);
    }
    
    /**
	 * 根据用户id获取用户所属门店列表（秀域主管所属门店）
	 * @param pageBean
	 * @return
	 */
	@RequestMapping(value = "/getList/byUserId", method = RequestMethod.GET)
	public @ResponseBody Pagination<Dept> xiuyuDeptList(String userId,String deptName,PageBean pageBean) {
		return service.xiuyuDeptList(userId,deptName,pageBean);
	}
	
	/**
     * 根据用户id批量添加门店列表(秀域主管操作)
     * @param dto
     * @return
     */
    @RequestMapping(value ="/addShopList/byUserId", method = RequestMethod.POST)
    public Result addShopListByUserId(@RequestBody AddShopListDto dto){
        return service.addShopListByUserId(dto);
    }
    
    
    /**
     * 根据用户id批量删除门店列表(秀域主管操作)
     * @param dto
     * @return
     */
    @RequestMapping(value ="/delShopList/byUserId", method = RequestMethod.POST)
    public Result delShopListByUserId(@RequestBody AddShopListDto dto){
        return service.delShopListByUserId(dto);
    }

	/**
	 * 通过店铺id修改店铺地址
	 * @param id
	 * @return
	 */
	@RequestMapping(value ="/updateShopAddress/ById", method = RequestMethod.POST)
	public Result updateShopAddressById( String id, String address){
    	return service.updateShopAddressById(id,address);
	}
	
}
