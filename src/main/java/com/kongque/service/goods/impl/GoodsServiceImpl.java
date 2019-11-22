package com.kongque.service.goods.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kongque.dao.IDaoService;
import com.kongque.dto.GoodsDto;
import com.kongque.entity.basics.Category;
import com.kongque.entity.goods.Goods;
import com.kongque.entity.goods.GoodsDetail;
import com.kongque.entity.goods.GoodsDetailTanant;
import com.kongque.service.goods.IGoodsService;
import com.kongque.util.DateUtil;
import com.kongque.util.FileOSSUtil;
import com.kongque.util.HttpUtil;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;
import com.kongque.util.StringUtils;

@Service
public class GoodsServiceImpl implements IGoodsService{
	
	@Resource
	IDaoService dao;
	@Resource
	private FileOSSUtil fileOSSUtil;

	@Override
	public Pagination<Goods> list(GoodsDto dto, PageBean pageBean) {
		Pagination<Goods> pagination=new Pagination<>();
		Criteria criteria=dao.createCriteria(Goods.class);

		if(StringUtils.isNotBlank(dto.getTenantId())){
			criteria.add(Restrictions.in("id",this.getGoodIdsByTenantId(dto.getTenantId())));
		}

		if(!StringUtils.isBlank(dto.getQ())){
			criteria.add(Restrictions.or(Restrictions.like("code", dto.getQ().trim(), MatchMode.ANYWHERE),
					Restrictions.like("name", dto.getQ().trim(), MatchMode.ANYWHERE)) );
		}
		if(!StringUtils.isBlank(dto.getCode())){
			criteria.add(Restrictions.like("code", dto.getCode().trim(), MatchMode.ANYWHERE));
		}
		if(!StringUtils.isBlank(dto.getName())){
			criteria.add(Restrictions.like("name", dto.getName().trim(), MatchMode.ANYWHERE));
		}
		if(!StringUtils.isBlank(dto.getStatus())){
			criteria.add(Restrictions.eq("status", dto.getStatus()));
		}

		if(pageBean.getPage()==null){
			pageBean.setPage(1);
		}
		if(pageBean.getRows()==null){
			pageBean.setRows(999);
		}
		pagination.setRows(dao.findListWithPagebeanCriteria(criteria, pageBean));
		pagination.setTotal(dao.findTotalWithCriteria(criteria));
		return pagination;
	}

	@Override
	public List<String> getGoodIdsByTenantId(String tenantId){
		List<String> list = dao.queryBySql(" select distinct  tgd.c_goods_id " +
				                           " from t_goods_detail tgd " +
				                           " join t_goods_detail_tanant_relation tdtr on tgd.c_id = tdtr.c_goods_detail_id" +
				                           " join t_tenant tt on tdtr.c_tenant_id = tt.c_id " +
				                           " where (tt.c_id='"+tenantId+"' or  tt.c_parent_id ='"+tenantId+"')");
		if(list==null || list.size()==0){
			list.add("");
		}
		return list;
	}

	@Override
	public Pagination<Goods> listAll(GoodsDto dto, PageBean pageBean) {
		Pagination<Goods> pagination=new Pagination<>();
		Criteria criteria=dao.createCriteria(Goods.class);

		if(StringUtils.isNotBlank(dto.getTenantId())){
			criteria.add(Restrictions.in("id",this.getGoodIdsByTenantId(dto.getTenantId())));
		}

		if(!StringUtils.isBlank(dto.getQ())){
			criteria.add(Restrictions.or(Restrictions.like("code", dto.getQ().trim(), MatchMode.ANYWHERE),
					Restrictions.like("name", dto.getQ().trim(), MatchMode.ANYWHERE)) );
		}
		if(!StringUtils.isBlank(dto.getCode())){
			criteria.add(Restrictions.like("code", dto.getCode().trim(), MatchMode.ANYWHERE));
		}
		if(!StringUtils.isBlank(dto.getStatus())){
			criteria.add(Restrictions.eq("status", dto.getStatus()));
		}
		if(StringUtils.isNotBlank(dto.getForOrder())) {
			criteria.add(Restrictions.eq("forOrder", dto.getForOrder()));
		}
		List<Goods> list = dao.findListWithPagebeanCriteria(criteria, pageBean);
		for (int i = 0; i < list.size(); i++) {
			List<GoodsDetail> detailList = list.get(i).getGoodsDetailList();
//			for (int j = 0; j < detailList.size(); j++) {
//				List<GoodsDetailTanant> goodsDetailTanantList = detailList.get(j).getListGoodsDetailTanant();
//				for (int k = 0; k < goodsDetailTanantList.size(); k++) {
//					if(!goodsDetailTanantList.get(k).getTenantId().equals(dto.getTenantId())){
//						goodsDetailTanantList.remove(goodsDetailTanantList.get(k));
//					}
//				}
//			}

			for(int m =detailList.size() - 1; m >= 0;m-- ){
				List<GoodsDetailTanant> goodsDetailTanantList = detailList.get(m).getListGoodsDetailTanant();
				for(int n = goodsDetailTanantList.size()-1;n >= 0;n--){
					if(!goodsDetailTanantList.get(n).getTenantId().equals(dto.getTenantId())){
						goodsDetailTanantList.remove(goodsDetailTanantList.get(n));
					}
				}
				if (goodsDetailTanantList.size()==0){
					detailList.remove(m);
				}
			}


		}
		if(pageBean.getPage()==null){
			pageBean.setPage(1);
		}
		if(pageBean.getRows()==null){
			pageBean.setRows(999);
		}
		pagination.setRows(list);
		pagination.setTotal(dao.findTotalWithCriteria(criteria));
		return pagination;
	}

	@Override
	public Result saveOrUpdate(GoodsDto dto) {
		if(StringUtils.isBlank(dto.getId())){
			Goods goods=new Goods();
			BeanUtils.copyProperties(dto, goods); 
			goods.setCategory(dao.findById(Category.class, dto.getCategoryId()));
			goods.setCreateTime(new Date());
			dao.save(goods);
			saveGoodsDetail(goods,dto);
			return new Result(goods);
		}else{
			Goods goods = new Goods();
			BeanUtils.copyProperties(dto, goods); 
			goods.setCategory(dao.findById(Category.class, dto.getCategoryId()));
			goods.setUpdateTime(new Date());
			dao.update(goods);
			updateGoodsDetail(goods,dto);
			return new Result(goods);
		}
	}
	
	private void saveGoodsDetail(Goods goods,GoodsDto dto){
		if(dto.getListGoodsDetail()!=null){
			for (int i = 0; i < dto.getListGoodsDetail().size(); i++) {
				GoodsDetail goodsDetail=new GoodsDetail();
				goodsDetail.setGoodsId(goods.getId());
				goodsDetail.setColorName(dto.getListGoodsDetail().get(i).getColorName());
				goodsDetail.setCostPrice(dto.getListGoodsDetail().get(i).getCostPrice());
				goodsDetail.setImageKeys(dto.getListGoodsDetail().get(i).getImageKeys());
				goodsDetail.setMaterielCode(dto.getListGoodsDetail().get(i).getMaterielCode());
				dao.save(goodsDetail);
				if(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant()!=null){
					for (int j = 0; j < dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().size(); j++) {
						GoodsDetailTanant goodsDetailTanant = new GoodsDetailTanant();
						goodsDetailTanant.setGoodsDetailId(goodsDetail.getId());
						goodsDetailTanant.setBalancePrice(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getBalancePrice());
						goodsDetailTanant.setTenantId(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getTenantId());
						goodsDetailTanant.setTenantPrice(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getTenantPrice());
						dao.save(goodsDetailTanant);
					}
				}
			}
		}
	}
	
	private void updateGoodsDetail(Goods goods,GoodsDto dto){
		if(dto.getListGoodsDetail()!=null){
			for (int i = 0; i < dto.getListGoodsDetail().size(); i++) {
				//如果详情id不为空，则进行修改
				if(org.apache.commons.lang3.StringUtils.isNotBlank(dto.getListGoodsDetail().get(i).getId())){
					GoodsDetail goodsDetail = dao.findById(GoodsDetail.class, dto.getListGoodsDetail().get(i).getId());
					goodsDetail.setColorName(dto.getListGoodsDetail().get(i).getColorName());
					goodsDetail.setCostPrice(dto.getListGoodsDetail().get(i).getCostPrice());
					goodsDetail.setImageKeys(dto.getListGoodsDetail().get(i).getImageKeys());
					goodsDetail.setMaterielCode(dto.getListGoodsDetail().get(i).getMaterielCode());
					dao.update(goodsDetail);
					if(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant()!=null){
						for (int j = 0; j < dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().size(); j++) {
							//如果商品-商户关系id不为空，则进行修改
							if(org.apache.commons.lang3.StringUtils.isNotBlank(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getId())){
								GoodsDetailTanant goodsDetailTanant = dao.findById(GoodsDetailTanant.class, dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getId());
								goodsDetailTanant.setGoodsDetailId(goodsDetail.getId());
								goodsDetailTanant.setBalancePrice(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getBalancePrice());
								goodsDetailTanant.setTenantId(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getTenantId());
								goodsDetailTanant.setTenantPrice(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getTenantPrice());
								dao.update(goodsDetailTanant);
							}else{
								//如果商品-商户关系id为空，则进行新增
								GoodsDetailTanant goodsDetailTanant = new GoodsDetailTanant();
								goodsDetailTanant.setGoodsDetailId(goodsDetail.getId());
								goodsDetailTanant.setBalancePrice(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getBalancePrice());
								goodsDetailTanant.setTenantId(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getTenantId());
								goodsDetailTanant.setTenantPrice(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getTenantPrice());
								dao.save(goodsDetailTanant);
							}
						}
					}
				}else{
					//如果详情id为空，则进行新增
					GoodsDetail goodsDetail=new GoodsDetail();
					goodsDetail.setGoodsId(goods.getId());
					goodsDetail.setColorName(dto.getListGoodsDetail().get(i).getColorName());
					goodsDetail.setCostPrice(dto.getListGoodsDetail().get(i).getCostPrice());
					goodsDetail.setImageKeys(dto.getListGoodsDetail().get(i).getImageKeys());
					goodsDetail.setMaterielCode(dto.getListGoodsDetail().get(i).getMaterielCode());
					dao.save(goodsDetail);
					if(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant()!=null){
						for (int j = 0; j < dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().size(); j++) {
							if(org.apache.commons.lang3.StringUtils.isNotBlank(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getId())){
								GoodsDetailTanant goodsDetailTanant = dao.findById(GoodsDetailTanant.class, dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getId());
								goodsDetailTanant.setGoodsDetailId(goodsDetail.getId());
								goodsDetailTanant.setBalancePrice(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getBalancePrice());
								goodsDetailTanant.setTenantId(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getTenantId());
								goodsDetailTanant.setTenantPrice(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getTenantPrice());
								dao.update(goodsDetailTanant);
							}else{
								GoodsDetailTanant goodsDetailTanant = new GoodsDetailTanant();
								goodsDetailTanant.setGoodsDetailId(goodsDetail.getId());
								goodsDetailTanant.setBalancePrice(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getBalancePrice());
								goodsDetailTanant.setTenantId(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getTenantId());
								goodsDetailTanant.setTenantPrice(dto.getListGoodsDetail().get(i).getListGoodsDetailTanant().get(j).getTenantPrice());
								dao.save(goodsDetailTanant);
							}
						}
					}
				}
				
			}
		}
	}
	
	@Override
	public Result updateStatus(String id,String status){
		Goods goods = dao.findById(Goods.class, id);
		goods.setStatus(status);
		dao.update(goods);
		return new Result(goods); 
	}
	
	@Override
	public Result uploadFile(MultipartFile[] files,String[] imageDelKeys){
		if(imageDelKeys!=null){
			delOSSFile(imageDelKeys);
		}
		Set<String> imageKeys = null;
		if(files!=null){
			imageKeys = appendOrderImage(files);
		}
		return new Result(imageKeys.toString());
	}
	
	private Set<String> appendOrderImage(MultipartFile[] file) {
		Set<String> addedImageNames = null;
		if (file == null) {// 如果没有上传任何图片文件
			return addedImageNames;
		}
		addedImageNames = new HashSet<>();// 初始化保存失败的上传文件名称列表
		for (MultipartFile imageFile : file) {// 遍历上传的图片文件
			try {
				String newKey ="xiuyu/goods/"+ getFileFolder(new Date()) +UUID.randomUUID().toString().replace("-", "") +"." +imageFile.getOriginalFilename();
				newKey = fileOSSUtil.uploadPrivateFile(newKey, imageFile.getInputStream());
				String keys = "\"" + newKey + "\"";
				// 把上传文件保存到OSS系统，并把OSS系统保存文件成功后返回的该文件的key添加到JSON数组中
				addedImageNames.add(keys);
			} catch (IOException e) {// 当读取上传图片文件输入流抛出异常时
				e.printStackTrace();
			}
		}
		return addedImageNames;// 返回保存失败的上传文件名称列表	
	}

	private String getFileFolder(Date date) {

		return DateUtil.formatDate(date, "yyyy") + "/" + DateUtil.formatDate(date, "MM") + "/"
				+ DateUtil.formatDate(date, "dd");
	}
	
	
	/**
	 * 删除上传到oss的文件
	 * 
	 * @param dto
	 * @return
	 */
	public String[] delOSSFile(String[] delFileList) {
		if (delFileList != null) {
			for (String imgKey : delFileList) {
				String removeImgKey = fileOSSUtil.fromUrlToKey(imgKey);
				// 在oss 删除 文件
				fileOSSUtil.deletePrivateFile(removeImgKey);
			}
		}
		return delFileList;
	}
	
	public void getAttachment(String filePath) {
		OutputStream outputStream = null;
		try {
			if (StringUtils.isNotBlank(filePath)) {
				File file = new File(filePath);
				HttpServletResponse response = HttpUtil.getHttpServletResponse();
				response.reset();
				response.setContentType("application/octet-stream; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
				outputStream = response.getOutputStream();
				outputStream.write(fileOSSUtil.getPrivateObject(filePath));
				outputStream.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}
	}
}
