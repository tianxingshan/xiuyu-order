package com.kongque.service.basics.impl;

import com.kongque.dao.IDaoService;
import com.kongque.entity.basics.BodyMeasureInfo;
import com.kongque.service.basics.IBodyMeasureInfoService;
import com.kongque.util.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Administrator
 */
@Service
public class BodyMeasureInfoServiceImpl implements IBodyMeasureInfoService {

    @Resource
    private IDaoService dao;

    @Override
    public Result findNotSelectedByCategoryId(String categoryId) {
        List<BodyMeasureInfo> list = dao.queryByHql(" FROM BodyMeasureInfo WHERE ifnull(status,'0')=0 and id NOT IN (SELECT bodyMeasureInfo.id FROM CategoryMeasureRelation WHERE categoryId='"+categoryId+"') order by sort asc");
        return new Result(list);
    }
}
/**
 * @program: xingyu-order
 * @description: 量体库
 * @author: zhuxl
 * @create: 2019-07-04 16:01
 **/
