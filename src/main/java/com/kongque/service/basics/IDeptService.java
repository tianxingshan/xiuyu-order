package com.kongque.service.basics;

import com.kongque.dto.AddShopListDto;
import com.kongque.dto.DeptDto;
import com.kongque.dto.XiuyuShopDto;
import com.kongque.entity.basics.Dept;
import com.kongque.entity.basics.XiuyuShopAreaRelation;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface IDeptService {

	Pagination<Dept> DeptList(DeptDto dto, PageBean pageBean);

	void exportDeptList(HttpServletRequest request, HttpServletResponse response, DeptDto dto);

	Result saveOrUpdate(XiuyuShopDto dto);

    Result parent();
    
    Result getShopInfoByUser(String shopId);
    
    Pagination<Dept> getShopListByAreaId(String areaId,PageBean pageBean);
    
    Result addShopList(AddShopListDto dto);
    
    Pagination<Dept> xiuyuDeptList(String userId,String deptName,PageBean pageBean);
    
    Result addShopListByUserId(AddShopListDto dto);
    
    Result delShopListByUserId(AddShopListDto dto);

    List<String> getShopsByUserId(String userId);

    Result updateShopAddressById(String id, String address);
}
