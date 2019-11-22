package com.kongque.controller.basics;

import com.kongque.dto.CategoryMeasureRelationDto;
import com.kongque.entity.basics.CategoryMeasureRelation;
import com.kongque.service.basics.ICategoryMeasureRelationService;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("/categoryMeasureRelation")
public class CategoryMeasureRelationController {

    @Resource
    private ICategoryMeasureRelationService service;

    @RequestMapping(value = "/saveOrUpdate",method = RequestMethod.POST)
    public Result saveOrUpdate(@RequestBody CategoryMeasureRelationDto dto){
        return service.saveOrUpdate(dto);
    }
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public Pagination<CategoryMeasureRelation> list(CategoryMeasureRelationDto dto, PageBean pageBean){
        return service.list(dto,pageBean);
    }

    @RequestMapping(value = "/delete",method = RequestMethod.GET)
    public Result delete(String id){
        return service.delete(id);
    }

    @RequestMapping(value = "/getMeasuresByCategoryIds",method = RequestMethod.GET)
    public List<String> getMeasuresByCategoryIds(String[] categoryIds){

        return service.getMeasuresByCategoryIds(categoryIds);
    }
}
/**
 * @program: xingyu-order
 * @description: 品类量体对照库
 * @author: zhuxl
 * @create: 2019-07-04 16:30
 **/
