package com.kongque.service.customer.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.kongque.service.basics.IDeptService;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.kongque.constants.Constants;
import com.kongque.dao.IDaoService;
import com.kongque.dto.XiuyuCustomerDto;
import com.kongque.entity.basics.XiuyuCustomer;
import com.kongque.service.customer.ICustomerService;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

@Service
public class CustomerServiceImpl implements ICustomerService{
	
	@Resource
	IDaoService dao;

	@Resource
	IDeptService deptService;
	
	@Override
	public Pagination<XiuyuCustomer> customerList(XiuyuCustomerDto dto,PageBean pageBean){
		Pagination<XiuyuCustomer> pagination = new Pagination<XiuyuCustomer>();
		Criteria criteria = dao.createCriteria(XiuyuCustomer.class);

		if(StringUtils.isNotBlank(dto.getCustomerCode())){
			criteria.add(Restrictions.eq("customerCode",dto.getCustomerCode()));
		}
		if(StringUtils.isNotBlank(dto.getCustomerName())){
			criteria.add(Restrictions.eq("customerName",dto.getCustomerName()));
		}
		if(StringUtils.isNotBlank(dto.getPhone())){
			criteria.add(Restrictions.eq("phone",dto.getPhone()));
		}

		if(StringUtils.isNotBlank(dto.getShopId())){
			criteria.add(Restrictions.eq("shopId",dto.getShopId()));
//		}else{
//			criteria.add(Restrictions.in("shopId",deptService.getShopsByUserId(dto.getUserId())));
		}
		if(StringUtils.isNotBlank(dto.getTenantId())){
			criteria.add(Restrictions.eq("tenantId",dto.getTenantId()));
		}
		if(StringUtils.isNotBlank(dto.getUserId())){
			criteria.add(Restrictions.in("shopId",deptService.getShopsByUserId(dto.getUserId())));
		}
//		if(StringUtils.isBlank(dto.getCustomerCode()) && StringUtils.isBlank(dto.getCustomerName()) && StringUtils.isBlank(dto.getPhone())){
//			@SuppressWarnings("unchecked")
//			List<UserRoleRelation> users = dao.createCriteria(UserRoleRelation.class)
//					.add(Restrictions.eq( "userId", dto.getUserId())).list();
//			UserDeptRelation dept = dao.findUniqueByProperty(UserDeptRelation.class, "userId", dto.getUserId());
//			for (UserRoleRelation userRoleRelation : users) {
//				if (Constants.DIANYUAN_ROLE_ID.equalsIgnoreCase(userRoleRelation.getRole().getId())
//						||Constants.JIAMENG_ROLE_ID.equalsIgnoreCase(userRoleRelation.getRole().getId())){
//					criteria.add(Restrictions.eq("shopId", dept.getDeptId()));
//					break;
//				}
//			}
//		}else{
//			if(StringUtils.isNotBlank(dto.getCustomerCode())){
//	        	criteria.add(Restrictions.eq("customerCode",dto.getCustomerCode()));
//	        }
//	        if(StringUtils.isNotBlank(dto.getCustomerName())){
//	        	criteria.add(Restrictions.eq("customerName",dto.getCustomerName()));
//	        }
//	        if(StringUtils.isNotBlank(dto.getPhone())){
//	        	criteria.add(Restrictions.eq("phone",dto.getPhone()));
//	        }
//		}
//        if(StringUtils.isNotBlank(dto.getShopId())){
//        	criteria.add(Restrictions.eq("shopId",dto.getShopId()));
//        }
        criteria.add(Restrictions.eq("deleteFlag","0"));
        pagination.setRows(dao.findListWithPagebeanCriteria(criteria,pageBean));
        pagination.setTotal(dao.findTotalWithCriteria(criteria));
        return pagination;
	}
	
	@Override
	public Pagination<XiuyuCustomer> orderCustomerList(XiuyuCustomerDto dto,PageBean pageBean){
		Pagination<XiuyuCustomer> pagination = new Pagination<XiuyuCustomer>();
		Criteria criteria = dao.createCriteria(XiuyuCustomer.class);
		if (StringUtils.isNotBlank(dto.getTenantId())){
			criteria.add(Restrictions.eq("tenantId",dto.getTenantId()));
		}
		if(StringUtils.isNotBlank(dto.getQ())){
        	criteria.add(Restrictions.or(Restrictions.like("customerCode",dto.getQ(),MatchMode.ANYWHERE),
        			Restrictions.like("customerName",dto.getQ(),MatchMode.ANYWHERE)));
        }
		criteria.add(Restrictions.eq("deleteFlag","0"));
        pagination.setRows(dao.findListWithPagebeanCriteria(criteria,pageBean));
        pagination.setTotal(dao.findTotalWithCriteria(criteria));
        return pagination;
	}
	
	@Override
	public Result saveOrUpdateCustomer(XiuyuCustomer customer){
		if(StringUtils.isBlank(customer.getId())){
			XiuyuCustomer x = dao.findUniqueByProperty(XiuyuCustomer.class, "customerCode", customer.getCustomerCode());
			if(x!=null){
				return new Result("500","用户编码已存在");
			}
			customer.setCreateTime(new Date());
			dao.save(customer); 
		}else{
			customer.setUpdateTime(new Date());
			dao.update(customer); 
		}
		return new Result(customer);
	}
	
	@Override
	public Result delCustomer(String id){
		XiuyuCustomer customer = dao.findById(XiuyuCustomer.class, id);
		if(customer!=null){
			customer.setDeleteFlag("1");
			dao.update(customer);
			return new Result(customer);
		}else{
			return new Result(Constants.RESULT_CODE.SYS_ERROR,"没有此会员！");
		}
	}
	
	@Override
	public Result remoteCustomer(String q,PageBean pageBean){
		Criteria criteria = dao.createCriteria(XiuyuCustomer.class);
		if(StringUtils.isNotBlank(q)){
			criteria.add(Restrictions.like("customerName", q ,MatchMode.ANYWHERE));
		}
		List<XiuyuCustomer> list = dao.findListWithPagebeanCriteria(criteria, pageBean);
		return new Result(list);
	}


}
