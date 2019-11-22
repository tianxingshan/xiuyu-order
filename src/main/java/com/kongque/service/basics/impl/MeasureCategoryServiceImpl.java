package com.kongque.service.basics.impl;

import com.kongque.dao.IDaoService;
import com.kongque.entity.basics.MeasureCategory;
import com.kongque.service.basics.IMeasureCategoryService;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @program: xingyu-order
 * @description: 量体分类
 * @author: zhuxl
 * @create: 2019-07-05 08:47
 **/
@Service
public class MeasureCategoryServiceImpl implements IMeasureCategoryService {

    @Resource
    private IDaoService dao;
    @Override
    public Pagination<MeasureCategory> list(MeasureCategory dto, PageBean pageBean) {
        Pagination<MeasureCategory> pagination = new Pagination<>();
        Criteria criteria = dao.createCriteria(MeasureCategory.class);
        if(StringUtils.isNotBlank(dto.getCode())){
            criteria.add(Restrictions.like("code",dto.getCode()));
        }
        if(StringUtils.isNotBlank(dto.getName())){
            criteria.add(Restrictions.like("name",dto.getName()));
        }
        if(StringUtils.isNotBlank(dto.getStatus())){
            criteria.add(Restrictions.eq("status",dto.getStatus()));
        }
        pagination.setRows(dao.findListWithPagebeanCriteria(criteria, pageBean));
        pagination.setTotal(dao.findTotalWithCriteria(criteria));
        return pagination;
    }

    @Override
    public Result saveOrUpdate(MeasureCategory dto) {
        if(StringUtils.isBlank(dto.getStatus())){
            dto.setStatus("0");
        }
        if(StringUtils.isNotBlank(dto.getId())){
            dao.update(dto);
        }else{
            dao.save(dto);
        }
        return new Result(dto);
    }

    @Override
    public Result delete(String id) {
        dao.delete(dao.findById(MeasureCategory.class, id));
        return new Result();
    }
}

