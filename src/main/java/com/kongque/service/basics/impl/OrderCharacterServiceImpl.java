package com.kongque.service.basics.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;

import com.kongque.entity.basics.Tenant;
import com.kongque.model.OrderCharacterModel;
import com.kongque.model.TenantModel;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.kongque.constants.Constants;
import com.kongque.dao.IDaoService;
import com.kongque.dto.OrderCharacterDto;
import com.kongque.entity.basics.OrderCharacter;
import com.kongque.service.basics.IOrderCharacterService;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

@Service
public class OrderCharacterServiceImpl implements IOrderCharacterService {
	@Resource
	IDaoService dao;

	@Override
	public Pagination<OrderCharacter> orderCharacterList(OrderCharacterDto dto, PageBean pageBean) {
		StringBuilder sb = new StringBuilder();
		Criteria criteria = dao.createCriteria(OrderCharacter.class);
		if(StringUtils.isNotBlank(dto.getTenantId())){
			sb.append("select c_order_character_id from t_order_character_tenant_relation where c_tenant_id='"+dto.getTenantId()+"'");
			if (StringUtils.isNotBlank(dto.getOrderCharacterStatus())){
				sb.append(" and c_status='"+dto.getOrderCharacterStatus()+"'");
			}
			List<String> list = dao.queryBySql(sb.toString());
			if (list.size()==0) list.add("");
			criteria.add(Restrictions.in("id",list));
		}
		if(!StringUtils.isBlank(dto.getOrderCharacterName())){
			criteria.add(Restrictions.like("orderCharacterName",dto.getOrderCharacterName(), MatchMode.ANYWHERE));
		}
//		if(!StringUtils.isBlank(dto.getOrderCharacterStatus())){
//			criteria.add(Restrictions.eq("orderCharacterStatus",dto.getOrderCharacterStatus() ));
//		}

		if (pageBean.getPage() == null) {
			pageBean.setPage(0);
		}
		if (pageBean.getRows() == null) {
			pageBean.setRows(9999);
		}
		Pagination<OrderCharacter> pagination = new Pagination<OrderCharacter>();
		pagination.setRows(dao.findListWithPagebeanCriteria(criteria, pageBean));
		pagination.setTotal(dao.findTotalWithCriteria(criteria));
		return pagination;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result saveOrUpdate(OrderCharacterDto dto) {
		if (StringUtils.isBlank(dto.getId())) {
			if (StringUtils.isNotBlank(dto.getOrderCharacterName())) {

				Criteria criteria = dao.createCriteria(OrderCharacter.class);
				criteria.add(Restrictions.eq("orderCharacterName", dto.getOrderCharacterName()));
				List<OrderCharacter> areas = (List<OrderCharacter>) criteria.list();
				if (areas != null && areas.size() > 0) {
					return new Result(Constants.RESULT_CODE.SYS_ERROR, "不能重复添加此订单性质！");
				} else {
					OrderCharacter orderCharacter = new OrderCharacter();
					orderCharacter.setOrderCharacterCreateTime(new Date());
					orderCharacter.setOrderCharacterMemo(dto.getOrderCharacterMemo());
					orderCharacter.setOrderCharacterName(dto.getOrderCharacterName());
					orderCharacter.setOrderCharacterStatus(dto.getOrderCharacterStatus());
					orderCharacter.setDeleteFlag("0");
					dao.save(orderCharacter);
					return new Result(orderCharacter);
				}
			} else {
				return new Result(Constants.RESULT_CODE.SYS_ERROR, "添加失败！");
			}
		} else {
			OrderCharacter orderCharacter= dao.findById(OrderCharacter.class, dto.getId());
			if (orderCharacter == null) {
				return new Result(Constants.RESULT_CODE.SYS_ERROR, "没有此地区！");
			}
			if (StringUtils.isNotBlank(dto.getOrderCharacterName())) {
				orderCharacter.setOrderCharacterName(dto.getOrderCharacterName());
			}
			if (StringUtils.isNotBlank(dto.getOrderCharacterStatus())) {
				orderCharacter.setOrderCharacterStatus(dto.getOrderCharacterStatus());
			}
			if(StringUtils.isNotBlank(dto.getOrderCharacterMemo())){
				orderCharacter.setOrderCharacterMemo(dto.getOrderCharacterMemo());
			}
			dao.update(orderCharacter);
			return new Result(orderCharacter);
		}
	}

	@Override
	public Result updateStatus(String orderCharacterId, String orderCharacterStatus) {
		OrderCharacter orderCharacter = dao.findById(OrderCharacter.class, orderCharacterId);
		if (orderCharacter != null) {
			orderCharacter.setOrderCharacterStatus(orderCharacterStatus);
			dao.update(orderCharacter);
			return new Result(orderCharacter);
		} else {
			return new Result(Constants.RESULT_CODE.SYS_ERROR, "没有此订单性质！");
		}
	}

	@Override
	public Result listByTenant(OrderCharacterDto dto) {
		List<TenantModel> tenantModels = new ArrayList<>();
		Criteria criteria = dao.createCriteria(Tenant.class);
		if(StringUtils.isNotBlank(dto.getParentTenantId())){
			criteria.add(Restrictions.or(Restrictions.eq("id",dto.getParentTenantId()),Restrictions.eq("parentId",dto.getParentTenantId())));
			criteria.add(Restrictions.ne("id","00"));
		}
		PageBean pageBean = new PageBean();
		if (pageBean.getPage() == null) {
			pageBean.setPage(0);
		}
		if (pageBean.getRows() == null) {
			pageBean.setRows(9999);
		}
		List<Tenant> tenants = dao.findListWithPagebeanCriteria(criteria, pageBean);
		//BeanUtils.copyProperties(tenants,tenantModels);
		for(Tenant tenant :tenants){
			TenantModel tenantModel = new TenantModel();
			BeanUtils.copyProperties(tenant,tenantModel);
			List<OrderCharacter> orderCharacters = dao.findListByProperty(OrderCharacter.class,"tenantId",tenantModel.getId());
			List<OrderCharacterModel> orderCharacterModels = new ArrayList<>();
			for(OrderCharacter orderCharacter :orderCharacters){
				OrderCharacterModel orderCharacterModel = new OrderCharacterModel();
				BeanUtils.copyProperties(orderCharacter,orderCharacterModel);
				orderCharacterModels.add(orderCharacterModel);
			}
			tenantModel.setOrderCharacterModels(orderCharacterModels);
			tenantModels.add(tenantModel);
		}
		return new Result(tenantModels);
	}


}
