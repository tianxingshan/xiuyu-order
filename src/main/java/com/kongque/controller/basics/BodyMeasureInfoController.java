package com.kongque.controller.basics;

import com.kongque.service.basics.IBodyMeasureInfoService;
import com.kongque.util.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("/bodyMeasureInfo")
public class BodyMeasureInfoController {

    @Resource
    private IBodyMeasureInfoService service;

    @RequestMapping("/findNotSelectedByCategoryId")
    public Result findNotSelectedByCategoryId(String categoryId){
        return service.findNotSelectedByCategoryId(categoryId);
    }
}
/**
 * @program: xingyu-order
 * @description: 量体库
 * @author: zhuxl
 * @create: 2019-07-04 15:52
 **/
