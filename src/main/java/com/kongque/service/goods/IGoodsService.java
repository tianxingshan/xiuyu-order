package com.kongque.service.goods;


import org.springframework.web.multipart.MultipartFile;

import com.kongque.dto.GoodsDto;
import com.kongque.entity.goods.Goods;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

import java.util.List;

public interface IGoodsService {

	Pagination<Goods> list(GoodsDto dto, PageBean pageBean);

	List<String> getGoodIdsByTenantId(String tenantId);
	
	Pagination<Goods> listAll(GoodsDto dto, PageBean pageBean);

	Result saveOrUpdate(GoodsDto dto);
	
	Result updateStatus(String id,String status);
	
	Result uploadFile(MultipartFile[] files,String[] imageDelKeys);
	
	void getAttachment(String filePath);

}
