package com.kongque.service.basics.impl;

import javax.annotation.Resource;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.kongque.dao.IDaoService;
import com.kongque.dto.CategoryDto;
import com.kongque.entity.basics.Category;
import com.kongque.service.basics.ICategoryService;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;
import com.kongque.util.StringUtils;

@Service
public class CategoryServiceImpl implements ICategoryService{
	
	@Resource
	IDaoService dao;

	@Override
	public Pagination<Category> list(CategoryDto dto, PageBean pageBean) {
		Criteria criteria = dao.createCriteria(Category.class);
		criteria.add(Restrictions.eq("del", "0"));
		if(!StringUtils.isBlank(dto.getCode())){
			criteria.add(Restrictions.like("code", dto.getCode(), MatchMode.ANYWHERE));
		}
		if(!StringUtils.isBlank(dto.getName())){
			criteria.add(Restrictions.like("name", dto.getName(), MatchMode.ANYWHERE));
		}
		Pagination<Category> pagination=new Pagination<>();
		pagination.setRows(dao.findListWithPagebeanCriteria(criteria, pageBean));
		pagination.setTotal(dao.findTotalWithCriteria(criteria));
		return pagination;
	}

	@Override
	public Result saveOrUpdate(CategoryDto dto) {
		if(StringUtils.isBlank(dto.getId())){ 
			Category category=new Category();
			category.setCode(dto.getCode());
			category.setName(dto.getName());
			category.setDel("0");
			dao.save(category);
			return new Result(category);
			}
		else{
			Category category=dao.findById(Category.class, dto.getId());
			category.setCode(dto.getCode());
			category.setName(dto.getName());
			dao.update(category);
			return new Result(category);
		}
	}

	@Override
	public Result delete(String id) {
		Category category=dao.findById(Category.class, id);
		category.setDel("1");
		dao.update(category);;
		return new Result(category);
	}

}
