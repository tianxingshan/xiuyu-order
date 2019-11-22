package com.kongque.service.basics.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.kongque.dao.IDaoService;
import com.kongque.dto.TenantDto;
import com.kongque.entity.basics.Sys;
import com.kongque.entity.basics.Tenant;
import com.kongque.service.basics.ITenantService;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;


@Service
public class TenantServiceImpl implements ITenantService{
	
	@Resource
	IDaoService dao;

	@Override
	public Pagination<Tenant> TenantList(PageBean pageBean, TenantDto tenant) {
		Pagination<Tenant> pagination = new Pagination<>();
		Criteria criteria = dao.createCriteria(Tenant.class);
		if(!StringUtils.isBlank(tenant.getId())) {
			criteria.add(Restrictions.eq("id", tenant.getId()));
		}
		if(!StringUtils.isBlank(tenant.getTenantName())) {
			criteria.add(Restrictions.like("tenantName", tenant.getTenantName()));
		}
		if(pageBean.getPage() == null) {
			pageBean.setPage(0);
		}
		if(pageBean.getRows() == null) {
			pageBean.setRows(999);
		}
		criteria.add(Restrictions.eq("tenantDel", "0"));
		pagination.setRows(dao.findListWithPagebeanCriteria(criteria, pageBean));
		pagination.setTotal(dao.findTotalWithCriteria(criteria));
		return pagination;
	}

	@Override
	public Result saveOrUpdate(Tenant tenant) {
		if(StringUtils.isBlank(tenant.getId())) {
			tenant.setTenantDel("0");
			dao.save(tenant);
			return new Result(tenant);
		}else {
			tenant.setTenantDel("0");
			dao.update(tenant);
			return new Result(tenant);
		}
	}

	@Override
	public Result deleteTenant(String tenantId) {
		Tenant tenant = dao.findById(Tenant.class, tenantId);
		tenant.setTenantDel("1");
		dao.update(tenant);
		return new Result(tenant);
	}

	@Override
	public Result updateStatusTenant(String tenantId,String tenantStatus) {
		Tenant tenant = dao.findById(Tenant.class, tenantId);
		tenant.setTenantStatus(tenantStatus);
		dao.update(tenant);
		return new Result(tenant);
	}

	@Override
	public Result getSysList() {
		Criteria criteria = dao.createCriteria(Sys.class);
		@SuppressWarnings("unchecked")
		List<Sys> list = criteria.list();
		return new Result(list);
	}

	@Override
	public Result getListByParentId(String parentId) {
		List<Tenant> tenantList = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		sb.append("select c_id,c_tenant_name from t_tenant where c_del=0 and (c_id='"+parentId+"' or c_parent_id='"+parentId+"')");
		List<Object> list = dao.queryBySql(sb.toString());
		for(Object object :list){
			Tenant tenant = new Tenant();
			Object[] properties = (Object[])object;
			tenant.setId(properties[0]==null ? "" : properties[0].toString());
			tenant.setTenantName(properties[1]==null ? "" : properties[1].toString());
			tenantList.add(tenant);
		}
		return new Result(tenantList);
	}
}
