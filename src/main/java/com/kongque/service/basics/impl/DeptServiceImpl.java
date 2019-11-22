package com.kongque.service.basics.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kongque.model.ShopExcelModel;
import com.kongque.util.*;
import com.kongque.util.StringUtils;
import org.apache.commons.lang3.*;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.kongque.dao.IDaoService;
import com.kongque.dto.AddShopListDto;
import com.kongque.dto.DeptDto;
import com.kongque.dto.XiuyuShopDto;
import com.kongque.entity.basics.Dept;
import com.kongque.entity.basics.Tenant;
import com.kongque.entity.basics.XiuyuShop;
import com.kongque.entity.basics.XiuyuShopAreaRelation;
import com.kongque.entity.basics.XiuyuShopCompanyRelation;
import com.kongque.entity.basics.XiuyuShopDirectorRelation;
import com.kongque.service.basics.IDeptService;

@Service
public class DeptServiceImpl implements IDeptService{
	
	@Resource
	IDaoService dao;

	@Override
	public Pagination<Dept> DeptList(DeptDto dto, PageBean pageBean) {
		Criteria criteria = dao.createCriteria(Dept.class);

		if(StringUtils.isNotBlank(dto.getUserId())){
			criteria.add(Restrictions.in("id",this.getShopsByUserId(dto.getUserId())));
		}

		if(!StringUtils.isBlank(dto.getDeptName())){
			criteria.add(Restrictions.like("deptName", dto.getDeptName(), MatchMode.ANYWHERE));
		}
		if(!StringUtils.isBlank(dto.getDeptCode())){
			criteria.add(Restrictions.like("deptCode", dto.getDeptCode(), MatchMode.ANYWHERE));
		}
		if(!StringUtils.isBlank(dto.getDeptPhone())) {
			criteria.add(Restrictions.eq("deptPhone", dto.getDeptPhone()));
		}
		if (!StringUtils.isBlank(dto.getCompanyId())) {
			criteria.createCriteria("companyList", "ll").createAlias("company", "o")
					.add(Restrictions.eq("o.id", dto.getCompanyId()));
		}
		criteria.add(Restrictions.eq("deptType", "1"));
//		criteria.createCriteria("deptTenantId", "a").add(Restrictions.eq("a.sysId", "xiuyu-order"));
		if(pageBean.getPage() == null) {
			pageBean.setPage(0);
		}
		if(pageBean.getRows() == null) {
			pageBean.setRows(999);
		}
		Pagination<Dept> pagination=new Pagination<>();
		pagination.setRows(dao.findListWithPagebeanCriteria(criteria, pageBean));
		pagination.setTotal(dao.findTotalWithCriteria(criteria));
		return pagination;
	}

	@Override
	public void exportDeptList(HttpServletRequest request, HttpServletResponse response, DeptDto dto) {
		List<ShopExcelModel> list = excelList(dto);
		String excelFileName = "门店";
		OutputStream out = null;
		try {
			final String userAgent = request.getHeader("USER-AGENT");
			if (userAgent.toLowerCase().contains("msie")) {// IE浏览器
				excelFileName = URLEncoder.encode(excelFileName, "UTF8");
			} else if (userAgent.toLowerCase().contains("mozilla") || userAgent.toLowerCase().contains("chrom")) {// google浏览器,火狐浏览器
				excelFileName = new String(excelFileName.getBytes(), "ISO8859-1");
			} else {
				excelFileName = URLEncoder.encode(excelFileName, "UTF8");// 其他浏览器
			}
			response.setContentType("application/vnd.ms-excel;charset=UTF-8");
			response.addHeader("content-Disposition", "attachment;filename=" + excelFileName + ".xls");
			response.flushBuffer();
			out = response.getOutputStream();
			Set<String> excludedFieldSet = new HashSet<String>();
			String[] headers = new String[]{"店铺编号", "店铺名称", "城市", "所在地区", "所属分公司", "联系人", "联系电话", "地址", "最新收货地址"};
			ExportExcelUtil.exportExcel(headers, ExportOrderdetailExcelUtil.buildCustomizedExportedModel(list, excludedFieldSet), out, "yyyy-MM-dd HH:mm:ss");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Result saveOrUpdate(XiuyuShopDto dto) {
		if(StringUtils.isBlank(dto.getId())){
			Criteria criteria=dao.createCriteria(XiuyuShop.class);
			criteria.add(Restrictions.eq("deptCode", dto.getDeptCode()));
			@SuppressWarnings("unchecked")
			List<XiuyuShop> list = criteria.list();
			if(list!=null && list.size()>0){
				return new Result("500","编码已存在！");
			}
			Tenant tenant = dao.findById(Tenant.class, dto.getTenantId());
			XiuyuShop xiuyuShop = new XiuyuShop();
			BeanUtils.copyProperties(dto, xiuyuShop);
			xiuyuShop.setDeptType("1");
			xiuyuShop.setCreateTime(new Date());
			xiuyuShop.setDeptTenantId(tenant);
			dao.save(xiuyuShop);
			XiuyuShopAreaRelation xiuyuShopAreaRelation = new XiuyuShopAreaRelation();
			xiuyuShopAreaRelation.setAreaId(dto.getAreaId());
			xiuyuShopAreaRelation.setShopId(xiuyuShop.getId());
			dao.save(xiuyuShopAreaRelation);
			XiuyuShopCompanyRelation xiuyuShopCompanyRelation = new XiuyuShopCompanyRelation();
			xiuyuShopCompanyRelation.setCompanyId(dto.getCompanyId());
			xiuyuShopCompanyRelation.setShopId(xiuyuShop.getId());
			dao.save(xiuyuShopCompanyRelation);
			return new Result(xiuyuShop);
		}else{
			Criteria criteria=dao.createCriteria(XiuyuShop.class);
			criteria.add(Restrictions.eq("deptCode", dto.getDeptCode()));
			criteria.add(Restrictions.ne("id",dto.getId()));
			@SuppressWarnings("unchecked")
			List<XiuyuShop> list = criteria.list();
			if(list!=null && list.size()>0){
				return new Result("500","编码已存在！");
			}
			Tenant tenant = dao.findById(Tenant.class, dto.getTenantId());
			XiuyuShop xiuyuShop = new XiuyuShop();
			BeanUtils.copyProperties(dto, xiuyuShop);
			xiuyuShop.setCreateTime(new Date());
			xiuyuShop.setDeptTenantId(tenant);
			dao.update(xiuyuShop);
			XiuyuShopAreaRelation xiuyuShopAreaRelation = dao.findUniqueByProperty(XiuyuShopAreaRelation.class, "shopId", xiuyuShop.getId());
			if(xiuyuShopAreaRelation==null){
				XiuyuShopAreaRelation x = new XiuyuShopAreaRelation();
				x.setAreaId(dto.getAreaId());
				x.setShopId(xiuyuShop.getId());
				dao.save(x);
			}else{
				xiuyuShopAreaRelation.setAreaId(dto.getAreaId());
				dao.update(xiuyuShopAreaRelation);
			}
			
			XiuyuShopCompanyRelation xiuyuShopCompanyRelation = dao.findUniqueByProperty(XiuyuShopCompanyRelation.class, "shopId", xiuyuShop.getId());
			if(xiuyuShopCompanyRelation==null){
				XiuyuShopCompanyRelation xx = new XiuyuShopCompanyRelation();
				xx.setShopId(xiuyuShop.getId());
				xx.setCompanyId(dto.getCompanyId());
				dao.save(xx);
			}else{
				xiuyuShopCompanyRelation.setCompanyId(dto.getCompanyId());
				dao.update(xiuyuShopCompanyRelation);
			}
			return new Result(xiuyuShop);
		}
	}

	@Override
	public  Result parent() {
		Criteria criteria = dao.createCriteria(Dept.class);
		@SuppressWarnings("unchecked")
		List<Dept> list = criteria.list();
		Map<String,List<Dept>> map = new HashMap<String,List<Dept>>();
		for(Dept c : list) {
			String key = StringUtils.isBlank(c.getDeptParentId())?"null":c.getDeptParentId();
			if(null == map.get(key)) {
				map.put(key, new ArrayList<Dept>());
			}
			map.get(key).add(c);
		}
		for(Dept c : list) {
			c.setDeptList(map.get(c.getId())); 
		}
		List<Dept> returnList = map.get("null");
		return new Result(returnList);

	}
	
	@Override
	public  Result getShopInfoByUser(String shopId){
		XiuyuShop xiuyuShop = dao.findById(XiuyuShop.class, shopId);
		return new Result(xiuyuShop);
	}
	
	@Override
	public Pagination<Dept> getShopListByAreaId(String areaId, PageBean pageBean){
		Pagination<Dept> pagination = new Pagination<>();
		Criteria criteria = dao.createCriteria(XiuyuShopAreaRelation .class);
		criteria.add(Restrictions.eq("areaId", areaId));
		List<XiuyuShopAreaRelation> list = dao.findListWithPagebeanCriteria(criteria, pageBean);
		List<Dept> deptList = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			Dept dept = dao.findById(Dept.class, list.get(i).getShopId());
			deptList.add(dept);
		}
		pagination.setRows(deptList);
		pagination.setTotal(dao.findTotalWithCriteria(criteria));
		return pagination;
	}
	
	@Override
	public Result addShopList(AddShopListDto dto){
		for (int i = 0; i < dto.getShopIds().length; i++) {
			XiuyuShopAreaRelation s = dao.findUniqueByProperty(XiuyuShopAreaRelation.class, "shopId", dto.getShopIds()[i]);
			if(s!=null){
				s.setAreaId(dto.getAreaId());
				dao.update(s);
			}
		}
		return new Result("200","操作成功！");
	}
	
	@Override
	public Pagination<Dept> xiuyuDeptList(String userId,String deptName,PageBean pageBean){
		Pagination<Dept> pagination = new Pagination<>();
		Criteria deptCriteria = dao.createCriteria(Dept.class);
		DetachedCriteria xiuyuShopDirectorRelationCriteria = DetachedCriteria.forClass(XiuyuShopDirectorRelation.class);
		xiuyuShopDirectorRelationCriteria.add(Restrictions.eq("userId", userId));
		xiuyuShopDirectorRelationCriteria.setProjection(Projections.property("deptId"));
		deptCriteria.add(Property.forName("id").in(xiuyuShopDirectorRelationCriteria));
		if(StringUtils.isNoneBlank(deptName)){
			deptCriteria.add(Restrictions.like("deptName", deptName,MatchMode.ANYWHERE));
		}
		List<Dept> list = dao.findListWithPagebeanCriteria(deptCriteria, pageBean);
		pagination.setRows(list);
		pagination.setTotal(dao.findTotalWithCriteria(deptCriteria));
		return pagination;
		
//		Criteria criteria = dao.createCriteria(XiuyuShopDirectorRelation.class);
//		criteria.add(Restrictions.eq("userId", userId));
//		@SuppressWarnings("unchecked")
//		List<XiuyuShopDirectorRelation> deptLsit = criteria.list();
//		Set<String> addedImageNames = new HashSet<>();
//		for (XiuyuShopDirectorRelation userDeptRelation : deptLsit) {
//			addedImageNames.add(userDeptRelation.getDeptId());
//		}
//		Pagination<Dept> pagination = new Pagination<>();
//		if(addedImageNames!=null && addedImageNames.size()>0){
//			Criteria criteria1 = dao.createCriteria(Dept.class);
//			criteria1.add(Restrictions.in("id", addedImageNames));
//			pagination.setRows(dao.findListWithPagebeanCriteria(criteria1, pageBean));
//			pagination.setTotal(dao.findTotalWithCriteria(criteria1));
//			return pagination;
//		}else{
//			List<Dept> s = null;
//			pagination.setRows(s);
//			pagination.setTotal(0);
//			return pagination;
//		}
		
	}
	
	
	@Override
	public Result addShopListByUserId(AddShopListDto dto){
		for (int i = 0; i < dto.getShopIds().length; i++) {
			Criteria criteria = dao.createCriteria(XiuyuShopDirectorRelation.class);
			criteria.add(Restrictions.eq("deptId", dto.getShopIds()[i]));
			criteria.add(Restrictions.eq("userId", dto.getUserId()));
			@SuppressWarnings("unchecked")
			List<XiuyuShopDirectorRelation> list = criteria.list();
			if(list==null || list.size()==0){
				XiuyuShopDirectorRelation s = new XiuyuShopDirectorRelation();
				s.setDeptId(dto.getShopIds()[i]);
				s.setUserId(dto.getUserId());
				dao.save(s); 
			}
//			else{
//				s.setUserId(dto.getUserId());
//				dao.update(s);
//			}
		}
		return new Result("200","操作成功！");
	}
	
	@Override
	public Result delShopListByUserId(AddShopListDto dto){
		for (int i = 0; i < dto.getShopIds().length; i++) {
			Criteria criteria = dao.createCriteria(XiuyuShopDirectorRelation.class);
			criteria.add(Restrictions.eq("deptId", dto.getShopIds()[i]));
			criteria.add(Restrictions.eq("userId", dto.getUserId()));
			@SuppressWarnings("unchecked")
			List<XiuyuShopDirectorRelation> list = criteria.list();
			XiuyuShopDirectorRelation s = list.get(0);
			if(s!=null){
				dao.delete(s); 
			}
		}
		return new Result("200","操作成功！");
	}

	@Override
	public List<String> getShopsByUserId(String userId) {
		List<String> list = dao.queryBySql("select c_shop_id from t_xiuyu_shop_director_relation where c_user_id='"+userId+"' ");
		if(list.size()==0){
			list.add("");
		}
		return list;
	}

	@Override
	public Result updateShopAddressById(String id, String address) {
		XiuyuShop shop = dao.findById(XiuyuShop.class,id);
		if (shop == null){
			return new Result("500","不存在该店铺");
		}
		shop.setLatestSddress(address);
		dao.update(shop);
		return new Result();
	}

	private List<ShopExcelModel> excelList(DeptDto dto){
		List<ShopExcelModel> list = new ArrayList<>();
		List<Dept> depts = DeptList(dto,new PageBean(1,Integer.MAX_VALUE)).getRows();
		if (null==depts || depts.size()==0){
			return list;
		}
		for (Dept d : depts) {
			ShopExcelModel m = new ShopExcelModel();
			XiuyuShop xiuyuShop = (XiuyuShop)d;
			m.setShopCode(xiuyuShop.getDeptCode());
			m.setShopName(xiuyuShop.getDeptName());
			m.setCity(xiuyuShop.getCity());
			if (null!=xiuyuShop.getAreaList()&&xiuyuShop.getAreaList().size()>0&&null!=xiuyuShop.getAreaList().get(0).getArea()){
				m.setArea(xiuyuShop.getAreaList().get(0).getArea().getAreaName());
			}
			m.setAddress(xiuyuShop.getAddress());
			m.setLatestSddress(xiuyuShop.getLatestSddress());
			if (null!=xiuyuShop.getCompanyList()&&xiuyuShop.getCompanyList().size()>0&&null!=xiuyuShop.getCompanyList().get(0).getCompany()){
				m.setCompany(xiuyuShop.getCompanyList().get(0).getCompany().getUnitName());
			}
			m.setContact(xiuyuShop.getContact());
			m.setDeptPhone(xiuyuShop.getDeptPhone());
			list.add(m);
		}
		return list;
	}
}
