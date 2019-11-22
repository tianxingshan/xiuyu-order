package com.kongque.service.order.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kongque.entity.basics.*;
import com.kongque.model.*;
import com.kongque.service.basics.IDeptService;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kongque.constants.Constants;
import com.kongque.dao.IDaoService;
import com.kongque.dto.OrderCheckDto;
import com.kongque.dto.OrderDetailSearchDto;
import com.kongque.dto.OrderDto;
import com.kongque.entity.goods.Goods;
import com.kongque.entity.goods.GoodsDetail;
import com.kongque.entity.order.BodyLanguage;
import com.kongque.entity.order.BodyMeasure;
import com.kongque.entity.order.Order;
import com.kongque.entity.order.OrderAttachment;
import com.kongque.entity.order.OrderCheck;
import com.kongque.entity.order.OrderDetail;
import com.kongque.entity.user.User;
import com.kongque.entity.user.UserDeptRelation;
import com.kongque.entity.user.UserRoleRelation;
import com.kongque.service.order.IOrderService;
import com.kongque.util.CodeUtil;
import com.kongque.util.DateUtil;
import com.kongque.util.ExportExcelUtil;
import com.kongque.util.ExportOrderdetailExcelUtil;
import com.kongque.util.FileOSSUtil;
import com.kongque.util.HttpUtil;
import com.kongque.util.ListUtils;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;

import net.sf.json.JSONArray;

@Service
public class OrderServiceImpl implements IOrderService {

	@Resource
	private IDaoService dao;

	@Resource
	private FileOSSUtil fileOSSUtil;

	@Resource
	IDeptService deptService;

	@Override
	public Pagination<Order> orderList(OrderDto dto, PageBean pageBean) {
		Pagination<Order> pagination = new Pagination<Order>();
		Criteria criteria = dao.createCriteria(Order.class);
		if(StringUtils.isNotBlank(dto.getUserId())){
			criteria.add(Restrictions.in("shopId",deptService.getShopsByUserId(dto.getUserId())));
		}
		if(StringUtils.isNotBlank(dto.getOrderCode())){
			criteria.add(Restrictions.like("orderCode", dto.getOrderCode(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getCustomerName())){
			criteria.add(Restrictions.like("customerName", dto.getCustomerName(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getCity())){
			criteria.add(Restrictions.like("city", dto.getCity(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getShopName())){
			criteria.add(Restrictions.like("shopName", dto.getShopName(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getOrderCharacter())){
			criteria.add(Restrictions.like("orderCharacter", dto.getOrderCharacter(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getStatusBussiness())){
			criteria.add(Restrictions.like("statusBussiness", dto.getStatusBussiness(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getReset())){
			criteria.add(Restrictions.like("reset", dto.getReset(),MatchMode.ANYWHERE));
		}
		if (dto.getCrdateTimeBegin() != null && dto.getCrdateTimeEnd() != null) {
			criteria.add(Restrictions.ge("createTime", dto.getCrdateTimeBegin()));
			criteria.add(Restrictions.lt("createTime", DateUtil.maxDate(dto.getCrdateTimeEnd())));
		} else if (dto.getCrdateTimeBegin() != null) {
			criteria.add(Restrictions.ge("createTime", dto.getCrdateTimeBegin()));
		} else if (dto.getCrdateTimeEnd() != null) {
			criteria.add(Restrictions.le("createTime", dto.getCrdateTimeEnd()));
		}
		criteria.add(Restrictions.eq("deleteFlag", "0"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("createTime"));
		List<Order> list = dao.findListWithPagebeanCriteria(criteria, pageBean);
		for (int i = 0; i < list.size(); i++) {
			List<OrderDetail> detailList = list.get(i).getOrderDetailList();
			for (int j = 0; j < detailList.size(); j++) {
				GoodsDetail goodsDetail = detailList.get(j).getGoodsDetail(); 
				String goodsId = goodsDetail.getGoodsId();
				Goods goods = dao.findById(Goods.class, goodsId);
				goodsDetail.setDetail(goods.getDetail());
				goodsDetail.setCategoryName(goods.getCategory().getName()); 
			}
			
		}
		pagination.setRows(list);
		pagination.setTotal(dao.findTotalWithCriteria(criteria));
		return pagination;
	}
	@Override
	public Pagination<Order> resetOrderList(OrderDto dto, PageBean pageBean) {
		Pagination<Order> pagination = new Pagination<Order>();
		Criteria criteria = dao.createCriteria(Order.class);

		if(StringUtils.isNotBlank(dto.getTenantId())){
			criteria.add(Restrictions.eq("tenantId",dto.getTenantId()));
		}

		if(StringUtils.isNotBlank(dto.getOrderCode())){
			criteria.add(Restrictions.like("orderCode", dto.getOrderCode(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getCustomerName())){
			criteria.add(Restrictions.like("customerName", dto.getCustomerName(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getCity())){
			criteria.add(Restrictions.like("city", dto.getCity(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getShopName())){
			criteria.add(Restrictions.like("shopName", dto.getShopName(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getOrderCharacter())){
			criteria.add(Restrictions.like("orderCharacter", dto.getOrderCharacter(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getStatusBussiness())){
			criteria.add(Restrictions.like("statusBussiness", dto.getStatusBussiness(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getReset())){
			criteria.add(Restrictions.like("reset", dto.getReset(),MatchMode.ANYWHERE));
		}
		if (dto.getCrdateTimeBegin() != null && dto.getCrdateTimeEnd() != null) {
			criteria.add(Restrictions.ge("createTime", dto.getCrdateTimeBegin()));
			criteria.add(Restrictions.lt("createTime", DateUtil.maxDate(dto.getCrdateTimeEnd())));
		} else if (dto.getCrdateTimeBegin() != null) {
			criteria.add(Restrictions.ge("createTime", dto.getCrdateTimeBegin()));
		} else if (dto.getCrdateTimeEnd() != null) {
			criteria.add(Restrictions.le("createTime", dto.getCrdateTimeEnd()));
		}
		criteria.add(Restrictions.eq("deleteFlag", "0"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("createTime"));
		List<Order> list = dao.findListWithPagebeanCriteria(criteria, pageBean);
		for (int i = 0; i < list.size(); i++) {
			List<OrderDetail> detailList = list.get(i).getOrderDetailList();
			for (int j = 0; j < detailList.size(); j++) {
				GoodsDetail goodsDetail = detailList.get(j).getGoodsDetail();
				String goodsId = goodsDetail.getGoodsId();
				Goods goods = dao.findById(Goods.class, goodsId);
				goodsDetail.setDetail(goods.getDetail());
				goodsDetail.setCategoryName(goods.getCategory().getName());
			}

		}
		pagination.setRows(list);
		pagination.setTotal(dao.findTotalWithCriteria(criteria));
		return pagination;
	}
	@Override
	public Pagination<Order> orderCheckList(OrderDto dto, PageBean pageBean) {
		Pagination<Order> pagination = new Pagination<Order>();
		Criteria criteria = dao.createCriteria(Order.class);
		if(StringUtils.isNotBlank(dto.getUserId())) {
			criteria.add(Restrictions.in("shopId", deptService.getShopsByUserId(dto.getUserId())));
		}


		if(StringUtils.isNotBlank(dto.getOrderCode())){
			criteria.add(Restrictions.like("orderCode", dto.getOrderCode(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getCustomerName())){
			criteria.add(Restrictions.eq("customerName", dto.getCustomerName()));
		}
		if(StringUtils.isNotBlank(dto.getCity())){
			criteria.add(Restrictions.like("city", dto.getCity(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getShopName())){
			criteria.add(Restrictions.like("shopName", dto.getShopName(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getOrderCharacter())){
			criteria.add(Restrictions.like("orderCharacter", dto.getOrderCharacter(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getStatusBussiness())){
			criteria.add(Restrictions.like("statusBussiness", dto.getStatusBussiness(),MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getReset())){
			criteria.add(Restrictions.like("reset", dto.getReset(),MatchMode.ANYWHERE));
		}
		if (dto.getCrdateTimeBegin() != null && dto.getCrdateTimeEnd() != null) {
			criteria.add(Restrictions.ge("createTime", dto.getCrdateTimeBegin()));
			criteria.add(Restrictions.lt("createTime", DateUtil.maxDate(dto.getCrdateTimeEnd())));
		} else if (dto.getCrdateTimeBegin() != null) {
			criteria.add(Restrictions.ge("createTime", dto.getCrdateTimeBegin()));
		} else if (dto.getCrdateTimeEnd() != null) {
			criteria.add(Restrictions.le("createTime", dto.getCrdateTimeEnd()));
		}
		if (dto.getXiuyuCheckTimeStr() != null && dto.getXiuyuCheckTimeEnd() != null) {
			criteria.add(Restrictions.ge("xiuyuChekTime", dto.getXiuyuCheckTimeStr()));
			criteria.add(Restrictions.lt("xiuyuChekTime", DateUtil.maxDate(dto.getXiuyuCheckTimeEnd())));
		} else if (dto.getXiuyuCheckTimeStr() != null) {
			criteria.add(Restrictions.ge("xiuyuChekTime", dto.getXiuyuCheckTimeStr()));
		} else if (dto.getXiuyuCheckTimeEnd() != null) {
			criteria.add(Restrictions.le("xiuyuChekTime", dto.getXiuyuCheckTimeEnd()));
		}
		if (dto.getSubmitTimeStr() != null && dto.getSubmitTimeEnd() != null) {
			criteria.add(Restrictions.ge("submitTime", dto.getSubmitTimeStr()));
			criteria.add(Restrictions.lt("submitTime", DateUtil.maxDate(dto.getSubmitTimeEnd())));
		} else if (dto.getSubmitTimeStr() != null) {
			criteria.add(Restrictions.ge("submitTime", dto.getSubmitTimeStr()));
		} else if (dto.getSubmitTimeEnd() != null) {
			criteria.add(Restrictions.le("submitTime", dto.getSubmitTimeEnd()));
		}
		criteria.add(Restrictions.eq("deleteFlag", "0"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("createTime"));
		List<Order> list = dao.findListWithPagebeanCriteria(criteria, pageBean);
		for (int i = 0; i < list.size(); i++) {
			List<OrderDetail> detailList = list.get(i).getOrderDetailList();
			for (int j = 0; j < detailList.size(); j++) {
				GoodsDetail goodsDetail = detailList.get(j).getGoodsDetail(); 
				String goodsId = goodsDetail.getGoodsId();
				Goods goods = dao.findById(Goods.class, goodsId);
				goodsDetail.setDetail(goods.getDetail());
				goodsDetail.setCategoryName(goods.getCategory().getName()); 
			}
			
		}
		pagination.setRows(list);
		pagination.setTotal(dao.findTotalWithCriteria(criteria));
		return pagination;
	}
	
	private List<String> getShopList(UserRoleRelation user) {
		Criteria criteria = dao.createCriteria(XiuyuShopDirectorRelation.class);
		criteria.add(Restrictions.eq("userId", user.getUserId()));
		criteria.setProjection(Projections.property("deptId"));
		@SuppressWarnings("unchecked")
		List<String> list = criteria.list();
		if (!ListUtils.isEmptyOrNull(list)) {
			return list;
		} else {
			return null;
		}
	}
	
	@Override
	public Result orderDel(String id){
		Order order = dao.findById(Order.class, id);
		if(order.getStatusBussiness().equals("0") || order.getStatusBussiness().equals("5") || order.getStatusBussiness().equals("6")){
			order.setDeleteFlag("1");
			dao.update(order); 
			return new Result("删除成功");
		}else{
			return new Result("500","当前订单状态不允许删除！");
		}
	}
	
	@Override
	public Result orderDetail(String id) {
		OrderModel model = new OrderModel();
		Order order = dao.findById(Order.class, id);
		if (order != null) {
			if(order.getOrderDetailList()!=null && order.getOrderDetailList().size()>0){
				for (int i = 0; i < order.getOrderDetailList().size(); i++) {
					OrderDetail orderDetail = order.getOrderDetailList().get(i);
					GoodsDetail goodsDetail = dao.findById(GoodsDetail.class, orderDetail.getGoodsDetailId());
					Goods goods = dao.findById(Goods.class, goodsDetail.getGoodsId());
					String categoryName = goods.getCategory().getName();
					orderDetail.setCategoryName(categoryName);
					orderDetail.setGoodsName(goods.getName());
				}
			}
			model.setOrder(order);
		}
		BodyMeasure bodyMeasure = dao.findUniqueByProperty(BodyMeasure.class, "orderId", id);
		if (bodyMeasure != null) {
			model.setBodyMeasure(bodyMeasure);
		}
		BodyLanguage bodyLanguage = dao.findUniqueByProperty(BodyLanguage.class, "orderId", id);
		if (bodyLanguage != null) {
			model.setBodyLanguage(bodyLanguage);
		}
		Criteria criteria2 = dao.createCriteria(OrderAttachment.class);
		criteria2.add(Restrictions.eq("orderId", id));
		@SuppressWarnings("unchecked")
		List<OrderAttachment> list2 = criteria2.list();
		if (list2 != null && list2.size() > 0) {
			model.setOrderAttachmentList(list2);
		}
		Criteria criteria = dao.createCriteria(OrderCheck.class);
		criteria.add(Restrictions.eq("orderId", id));
		@SuppressWarnings("unchecked")
		List<OrderCheck> list = criteria.list();
		if (list != null && list.size() > 0) {
			model.setOrderCheckList(list);
		}
		return new Result(model);
	}

	@Override
	public Result saveOrUpdate(OrderDto dto, MultipartFile[] files) {
		if (StringUtils.isBlank(dto.getId())) {
			Order order = new Order();
			BeanUtils.copyProperties(dto, order);
			if(StringUtils.isBlank(order.getReset())){
				return new Result("500","订单重置状态未选择！");
			}
			if(StringUtils.isBlank(dto.getShopId())){
				return new Result("500","店铺代码不能为空！");
			}
			order.setOrderCode(CodeUtil.createOrderCode(getOrderMaxValue()));
			order.setSysId(Constants.SYS_ID); 
			order.setCreateTime(new Date());
			order.setStatusBeforeProduce("0");
			order.setStatusBeforeSend("0");
			order.setDeleteFlag("0");
			order.setCreateUser(dao.findById(User.class, dto.getCreateUserId()));
			dao.save(order);
			if(StringUtils.isNoneBlank(dto.getReceivingAddress())){
				XiuyuShop shop = dao.findById(XiuyuShop.class, dto.getShopId());
				if(shop!=null){
					shop.setLatestSddress(dto.getReceivingAddress());
					dao.update(shop); 
				}
			}
			OrderCheck orderCheck = new OrderCheck();
			orderCheck.setOrderId(order.getId());
			orderCheck.setCheckType("1");
			orderCheck.setCheckerName(dao.findById(User.class, dto.getCreateUserId()).getUserName());
//			orderCheck.setCheckStatus("2");
			orderCheck.setCheckInstruction("订单新建");
			orderCheck.setCheckTime(new Date());
			dao.save(orderCheck);
			OrderModel model = new OrderModel();
			model.setOrder(order);
			return new Result(model);
		} else {
			Order order = new Order();
			BeanUtils.copyProperties(dto, order);
			if(StringUtils.isBlank(order.getReset())){
				return new Result("500","订单重置状态未选择！");
			}
			order.setDeleteFlag("0");
			order.setSysId(Constants.SYS_ID); 
			order.setUpdateTime(new Date());
			dao.update(order);
			if(StringUtils.isNoneBlank(dto.getReceivingAddress())){
				XiuyuShop shop = dao.findById(XiuyuShop.class, dto.getShopId());
				if(shop!=null){
					shop.setLatestSddress(dto.getReceivingAddress());
					dao.update(shop); 
				}
			}
			updateOrder(order, dto);
			User user = dao.findById(User.class, order.getCreateUserId());
			order.setCreateUser(user); 
			BodyMeasure bodyMeasure = dao.findUniqueByProperty(BodyMeasure.class, "orderId", order.getId());
			BodyLanguage bodyLanguage = dao.findUniqueByProperty(BodyLanguage.class, "orderId", order.getId());
			Criteria criteria = dao.createCriteria(OrderAttachment.class);
			criteria.add(Restrictions.eq("orderId", order.getId()));
			@SuppressWarnings("unchecked")
			List<OrderAttachment> orderAttachmentList = criteria.list();
			OrderModel model = new OrderModel();
			model.setOrder(order);
			if(bodyMeasure!=null){
				model.setBodyMeasure(bodyMeasure);
			}
			if(bodyLanguage!=null){
				model.setBodyLanguage(bodyLanguage);
			}
			if(orderAttachmentList!=null && orderAttachmentList.size()>0){
				model.setOrderAttachmentList(orderAttachmentList);
			}
			return new Result(model);
		}
	}

	public void updateOrder(Order order, OrderDto dto) {
		if (dto.getOrderLanguage() != null) {
			BodyLanguage bodyLanguage = dao.findUniqueByProperty(BodyLanguage.class, "orderId", order.getId());
			if(bodyLanguage==null){
				BodyLanguage bodyLanguage1 = new BodyLanguage();
				bodyLanguage1.setOrderId(order.getId());
				bodyLanguage1.setCustomerId(order.getCustomerId());
				bodyLanguage1.setOrderLanguage(dto.getOrderLanguage());
				dao.save(bodyLanguage1);
			}else{
				bodyLanguage.setOrderLanguage(dto.getOrderLanguage());
				dao.update(bodyLanguage);
			}
		}
		if (dto.getOrderMeasure() != null) {
			BodyMeasure bodyMeasure = dao.findUniqueByProperty(BodyMeasure.class, "orderId", order.getId());
			if(bodyMeasure==null){
				BodyMeasure bodyMeasure1 = new BodyMeasure();
				bodyMeasure1.setOrderId(order.getId());
				bodyMeasure1.setCustomerId(order.getCustomerId());
				bodyMeasure1.setOrderMeasure(dto.getOrderMeasure());
				bodyMeasure1.setOrderMeasureTemplate(dto.getOrderMeasureTemplate());
				dao.save(bodyMeasure1);
			}else{
				bodyMeasure.setOrderMeasure(dto.getOrderMeasure());
				bodyMeasure.setOrderMeasureTemplate(dto.getOrderMeasureTemplate());
				dao.update(bodyMeasure);
			}
		}
	}

	@Override
	public Result checkOrUpdate(OrderCheckDto dto) {
		Order order = dao.findById(Order.class, dto.getOrderId());
		if ("2".equals(dto.getCheckType())) {
			order.setStatusBeforeProduce(dto.getCheckStatus());
			order.setBusinessCheckerName(dto.getCheckerName());
			order.setBusinessCheckerTime(new Date());
		}
		if ("3".equals(dto.getCheckType())) {
			order.setStatusBeforeSend(dto.getCheckStatus());
			order.setBusinessCheckerName(dto.getCheckerName());
			order.setBusinessCheckerTime(new Date());
		}
		if("1".equals(dto.getCheckType())){
			if (StringUtils.isNotBlank(dto.getStatus())) {
				order.setStatusBussiness(dto.getStatus());
			}
			if("1".equals(dto.getStatus())){
				order.setSubmitTime(new Date()); 
			}
			if("3".equals(dto.getStatus()) || "4".equals(dto.getStatus())){  
				order.setBusinessCheckerName(dto.getCheckerName());
				order.setXingyuChekTime(new Date());
			}
			if("5".equals(dto.getStatus()) || "2".equals(dto.getStatus())){
				order.setBusinessCheckerName(dto.getCheckerName());
				order.setXiuyuChekTime(new Date());
			}
			if("6".equals(dto.getStatus()) || "7".equals(dto.getStatus()) || "8".equals(dto.getStatus())){
				order.setBusinessCheckerName(dto.getCheckerName());
			}
		}
		dao.update(order);
		OrderCheck orderCheck = new OrderCheck();
		orderCheck.setOrderId(dto.getOrderId());
		orderCheck.setCheckType(dto.getCheckType());
		orderCheck.setCheckerName(dto.getCheckerName());
		orderCheck.setCheckStatus(dto.getCheckStatus());
		orderCheck.setCheckInstruction(dto.getCheckInstruction());
		orderCheck.setCheckTime(new Date());
		dao.save(orderCheck);
		return new Result(orderCheck);
	}

	

	private String getSnValue() {
		Date day = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = df.format(day);
		int a = (int) (Math.random() * (9999 - 1000 + 1)) + 1000;
		String sn = date + String.valueOf(a);
		return sn;
	}

	private String getOrderMaxValue() {
		Date date = new Date();
		Criteria criteria = dao.createCriteria(Code.class);
		criteria.add(Restrictions.between("updateTime", DateUtil.minDate(date), DateUtil.maxDate(date)));
		criteria.add(Restrictions.eq("type", "DD"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("maxValue"));
		criteria.setMaxResults(1);
		Code code = (Code) criteria.uniqueResult();
		if (code == null) {
			code = new Code();
			code.setMaxValue(1);
			code.setType("DD");
			code.setUpdateTime(date);
			dao.save(code);
		} else {
			code.setMaxValue(code.getMaxValue() + 1);
		}
		return String.format("%0" + 6 + "d", code.getMaxValue());
	}

	
	
	@Override
	public void exportRepairBalExcel(OrderDetailSearchDto dto, HttpServletRequest request, HttpServletResponse response){
		List<ExportOrderDetailSearchModel> odsModelList = queryOrderDetailWithParam(dto);
		String excelFileName = "订单明细";
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
			String[] headers = new String[] {"订单编号", "ERP订单号","唯一码","城市","分公司", "门店", "性质", "会员名称", "绣字名","商品名称", "颜色", "数量" ,"订单状态" ,"订单明细状态","生产前财务审核状态","发货前财务审核状态","建立时间","工厂审核时间","工厂发货日期", "物料编码", "结案状态","尺码", "发货单号"};
			excludedFieldSet.add("orderId");
			
			
			ExportExcelUtil.exportExcel(headers, ExportOrderdetailExcelUtil.buildCustomizedExportedModel(odsModelList, excludedFieldSet), out,"yyyy-MM-dd HH:mm:ss");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
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
	
	public	List<ExportOrderDetailSearchModel> queryOrderDetailWithParam(OrderDetailSearchDto odsDto){
		List<ExportOrderDetailSearchModel> odsModelList = new ArrayList<>();
		StringBuilder sql = new StringBuilder("select " +
				"a.c_id," +
				"a.c_order_code as orderCode," +
				"b.c_erp_no as erpNo," +
				"b.c_goods_sn as goodsSn," +
				"a.c_city as city," +
				"a.c_shop_name as shopName," +
				"a.c_order_character as orderCharacter," +
				"a.c_customer_name as customerName," +
				"a.c_embroid_name as embroidName," +
				"b.c_goods_name as goodsName," +
				"b.c_goods_color_name as goodsColor," +
				"b.c_num as num," +
				"a.c_status_bussiness as orderStatus," +
				"b.c_order_detail_status as orderDetailStutas," +
				"a.c_status_before_produce as beforePriduce," +
				"a.c_status_before_send as beforeSend," +
				"a.c_create_time as createTime," +
				"a.c_xingyu_chek_time as xingyuCheckTime," +
				"tb.c_send_time as sendTime," +
				"c.c_materiel_code as materielCode," +
				"b.c_closed_status as closedStatus," +
				"case tb.c_logistic_type when '0' then tb.c_express_number else '' end as expressNumber,h.company_name " +
				"from t_order_detail b " +
				"left join t_order a on a.c_id=b.c_order_id " +
				"left join t_goods_detail c on c.c_id=b.c_goods_detail_id " +
				"left join t_goods d on d.c_id=c.c_goods_id " +
				"left join (select e.c_order_detail_id,f.c_express_number,f.c_send_time,f.c_logistic_type from  t_logistic_order e,t_logistic f where f.c_id=e.c_logistic_id and f.c_delete_flag = '0' and e.c_delete_flag='0' ) tb ON tb.c_order_detail_id=b.c_id  " +
				"LEFT JOIN (SELECT shop.c_id AS shop_id,shop.c_dept_name AS shop_name,company.c_dept_name AS company_name FROM t_dept shop,t_dept company WHERE shop.c_parent_id = company.c_id AND shop.c_dept_type='1' ) h ON a.c_shop_id = h.shop_id   " +
				"where a.c_delete_flag='0'");

		if(StringUtils.isNotBlank(odsDto.getTenantId())){
			sql.append(" and a.c_tenant_id ='").append(odsDto.getTenantId()).append("'");
		}
		if (StringUtils.isNotBlank(odsDto.getUserId())){
			sql.append(" and  a.c_shop_id in (select c_shop_id from t_xiuyu_shop_director_relation where c_user_id='").append(odsDto.getUserId()).append("')");
		}
		if(odsDto.getOrderCode() != null && !odsDto.getOrderCode().isEmpty()){
			sql.append(" and a.c_order_code like '%").append(odsDto.getOrderCode()).append("%'");
		}

		if(odsDto.getCompanyName() != null && !odsDto.getCompanyName().isEmpty()){
			sql.append(" and h.company_name like '%").append(odsDto.getCompanyName()).append("%'");
		}

		if(odsDto.getCustomerQ() != null && !odsDto.getCustomerQ().isEmpty()){
			sql.append(" and (a.c_customer_name like '%"+odsDto.getCustomerQ()+"%' or a.c_customer_code like '%"+odsDto.getCustomerQ()+"%')");
		}
		if(odsDto.getGoodsQ() != null && !odsDto.getGoodsQ().isEmpty()){
			sql.append(" and (b.c_goods_name like '%"+odsDto.getGoodsQ()+"%' or b.c_goods_code like '%"+odsDto.getGoodsQ()+"%')");
		}
		if(odsDto.getCity() != null && !odsDto.getCity().isEmpty()){
			sql.append(" and a.c_city like '%").append(odsDto.getCity()).append("%'");
		}
		if(odsDto.getShopName() != null && !odsDto.getShopName().isEmpty()){
			sql.append(" and a.c_shop_name like '%").append(odsDto.getShopName()).append("%'");
		}
		if(odsDto.getOrderCharacter() != null && !odsDto.getOrderCharacter().isEmpty()){
			sql.append(" and a.c_order_character = '").append(odsDto.getOrderCharacter()).append("'");
		}
		if(odsDto.getReset() != null && !odsDto.getReset().isEmpty()){
			sql.append(" and a.c_reset = '").append(odsDto.getReset()).append("'");
		}
		if(odsDto.getStatusBussiness() != null && !odsDto.getStatusBussiness().isEmpty()){
			sql.append(" and a.c_status_bussiness = '").append(odsDto.getStatusBussiness()).append("'");
		}
		if(odsDto.getStatusBeforeProduce() != null && !odsDto.getStatusBeforeProduce().isEmpty()){
			sql.append(" and a.c_status_before_produce = '").append(odsDto.getStatusBeforeProduce()).append("'");
		}
		if(odsDto.getStatusBeforeSend() != null && !odsDto.getStatusBeforeSend().isEmpty()){
			sql.append(" and a.c_status_before_send = '").append(odsDto.getStatusBeforeSend()).append("'");
		}
		if(odsDto.getOrderDetailStatus() != null && !odsDto.getOrderDetailStatus().isEmpty()){
			sql.append(" and b.c_order_detail_status = '").append(odsDto.getOrderDetailStatus()).append("'");
		}
		if(odsDto.getGoodsColorName() != null && !odsDto.getGoodsColorName().isEmpty()){
			sql.append(" and b.c_goods_color_name = '").append(odsDto.getGoodsColorName()).append("'");
		}
		if(odsDto.getCategoryId() != null && !odsDto.getCategoryId().isEmpty()){
			sql.append(" and d.c_category_id = '").append(odsDto.getCategoryId()).append("'");
		}
		if(odsDto.getErpNo() != null && !odsDto.getErpNo().isEmpty()){
			sql.append(" and b.c_erp_no like '%").append(odsDto.getErpNo()).append("%'");
		}
		if(odsDto.getGoodsSn() != null && !odsDto.getGoodsSn().isEmpty()){
			sql.append(" and b.c_goods_sn like '%").append(odsDto.getGoodsSn()).append("%'");
		}
		//添加订单创建日期限定条件
		if(odsDto.getCreateTimeStart() != null && !odsDto.getCreateTimeStart().isEmpty() && odsDto.getCreateTimeEnd() != null && !odsDto.getCreateTimeEnd().isEmpty()){//添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and a.c_create_time between '").append(odsDto.getCreateTimeStart()).append(" 00:00:00'").append(" and '").append(odsDto.getCreateTimeEnd()).append(" 23:59:59'");
		}
		else if(odsDto.getCreateTimeStart() != null && !odsDto.getCreateTimeStart().isEmpty() && (odsDto.getCreateTimeEnd() == null || odsDto.getCreateTimeEnd().isEmpty())){//添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and a.c_create_time >= '").append(odsDto.getCreateTimeStart()).append(" 00:00:00'");
		}
		else if ((odsDto.getCreateTimeStart() == null || odsDto.getCreateTimeStart().isEmpty()) && odsDto.getCreateTimeEnd() != null && !odsDto.getCreateTimeEnd().isEmpty()) {//添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and a.c_create_time <= '").append(odsDto.getCreateTimeEnd()).append(" 23:59:59'");
		}
		//添加物流单发货日期限定条件
		if(odsDto.getSendTimeStart() != null && !odsDto.getSendTimeStart().isEmpty() && odsDto.getSendTimeEnd() != null && !odsDto.getSendTimeEnd().isEmpty()){//添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and tb.c_send_time between '").append(odsDto.getSendTimeStart()).append(" 00:00:00'").append(" and '").append(odsDto.getSendTimeEnd()).append(" 23:59:59'");
		}
		else if(odsDto.getSendTimeStart() != null && !odsDto.getSendTimeStart().isEmpty() && (odsDto.getSendTimeEnd() == null || odsDto.getSendTimeEnd().isEmpty())){//添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and tb.c_send_time >= '").append(odsDto.getSendTimeStart()).append(" 00:00:00'");
		}
		else if ((odsDto.getSendTimeStart() == null || odsDto.getSendTimeStart().isEmpty()) && odsDto.getSendTimeEnd() != null && !odsDto.getSendTimeEnd().isEmpty()) {//添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and tb.c_send_time <= '").append(odsDto.getSendTimeEnd()).append(" 23:59:59'");
		}
		//星域审核日期查询
		if(StringUtils.isNotBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isNotBlank(odsDto.getXingyuChekTimeEnd())){
			sql.append(" and a.c_xingyu_chek_time between '").append(odsDto.getXingyuChekTimeStr()).append(" 00:00:00'").append(" and '").append(odsDto.getXingyuChekTimeEnd()).append(" 23:59:59'");
		}else if(StringUtils.isNotBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isBlank(odsDto.getXingyuChekTimeEnd())){
			sql.append(" and a.c_xingyu_chek_time >= '").append(odsDto.getXingyuChekTimeStr()).append(" 00:00:00'");
		}else if(StringUtils.isBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isNotBlank(odsDto.getXingyuChekTimeEnd())){
			sql.append(" and a.c_xingyu_chek_time <= '").append(odsDto.getXingyuChekTimeEnd()).append(" 00:00:00'");
		}

		sql.append(" order by a.c_create_time desc");
		List resultSet = dao.queryBySql(sql.toString());		
		for(Object result : resultSet){
			ExportOrderDetailSearchModel odsModel = new ExportOrderDetailSearchModel();//构建返回数据模型
			Object[] properties = (Object[])result;
			odsModel.setOrderId(properties[0]==null ? "" : properties[0].toString());
			odsModel.setOrderCode(properties[1]==null ? "" : properties[1].toString());
			odsModel.setErpNum(properties[2]==null ? "" : properties[2].toString());
			odsModel.setGoodsSN(properties[3]==null ? "" : properties[3].toString());
			odsModel.setCity(properties[4]==null ? "" : properties[4].toString());
			odsModel.setShopName(properties[5]==null ? "" : properties[5].toString());
			odsModel.setCharacteres(properties[6]==null ? "" : properties[6].toString());
			odsModel.setCustomerName(properties[7]==null ? "" : properties[7].toString());
			odsModel.setEmbroidName(properties[8]==null ? "" : properties[8].toString());
			odsModel.setGoodsName(properties[9]==null ? "" : properties[9].toString());
			odsModel.setGoodsColor(properties[10]==null ? "" : properties[10].toString());
			odsModel.setNum(properties[11]==null ? "" : properties[11].toString());
			odsModel.setBillStatus(properties[12]==null ? "" : changeOrderStatus(properties[12].toString()));
			odsModel.setOrderDetailStatus(properties[13]==null ? "" : changeOrderDetailStatus(properties[13].toString()));
			odsModel.setBeforePriduce(properties[14]==null ? "" : changeStatus(properties[14].toString()));
			odsModel.setBeforeSend(properties[15]==null ? "" :changeStatus(properties[15].toString()));
			odsModel.setCreateTime(properties[16]==null ? "" : properties[16].toString().substring(0,10));
			odsModel.setXingyuCheckTime(properties[17]==null ? "" : properties[17].toString().substring(0,10));
			odsModel.setSendTime(properties[18]==null ? "" : properties[18].toString().substring(0,10));
			odsModel.setMaterielCode(properties[19]==null ? "" : properties[19].toString());
			odsModel.setClosedStatus(properties[20]==null ? "" : changeClosedStatus(properties[20].toString()));
//			odsModel.setLogisticType(properties[21]==null ? "" : properties[21].toString());//收发货单据类型 20181210添加
			odsModel.setExpressNumber(properties[21]==null ? "" : properties[21].toString());//收发货单号 20181210添加
			odsModel.setCompanyName(properties[22]==null ? "" : properties[22].toString());
			odsModelList.add(odsModel);
		}
		return 	odsModelList;
	}
	
	@Override
	public	List<OrderDetailSearchModel> queryDetailWithParam(OrderDetailSearchDto odsDto,PageBean pageBean){
		List<OrderDetailSearchModel> odsModelList = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT " +
				"a.c_id AS orderId," +
				"a.c_order_code AS orderCode," +
				"b.c_erp_no AS erpNo," +
				"b.c_goods_sn AS goodsSn," +
				"a.c_city,a.c_shop_name," +
				"a.c_order_character," +
				"a.c_customer_name," +
				"b.c_goods_name," +
				"b.c_goods_color_name," +
				"b.c_num," +
				"a.c_status_bussiness," +
				"b.c_order_detail_status," +
				"a.c_create_time AS createTime," +
				"tb.c_send_time AS sendTime," +
				"c.c_materiel_code," +
				"b.c_closed_status," +
				"d.c_code,b.c_id AS orderDetailId," +
				"a.c_embroid_name," +
				"g.c_name AS categoryName," +
				"a.c_business_checker_name," +
				"a.c_business_checker_time," +
				"a.c_recorder_name," +
				"a.c_reset," +
				"a.c_status_before_produce," +
				"a.c_status_before_send," +
				"a.c_xiuyu_chek_time," +
				"a.c_xingyu_chek_time," +
				"tb.c_express_number as expressNumber " +
				"FROM t_order_detail b " +
				"LEFT JOIN t_order a ON b.c_order_id=a.c_id " +
				"LEFT JOIN t_goods_detail c ON c.c_id=b.c_goods_detail_id " +
				"LEFT JOIN t_goods d ON d.c_id=c.c_goods_id " +
				"LEFT JOIN t_category g on d.c_category_id = g.c_id and g.c_del='0' " +
				"LEFT JOIN (select e.c_order_detail_id,f.c_express_number,f.c_send_time from  t_logistic_order e,t_logistic f where f.c_id=e.c_logistic_id and f.c_delete_flag = '0' and e.c_delete_flag='0' ) tb ON tb.c_order_detail_id=b.c_id  " +
				"where a.c_delete_flag='0' ");
		@SuppressWarnings("unchecked")
		List<UserRoleRelation> users = dao.createCriteria(UserRoleRelation.class)
				.add(Restrictions.eq( "userId", odsDto.getUserId())).list();
		UserDeptRelation dept = dao.findUniqueByProperty(UserDeptRelation.class, "userId", odsDto.getUserId());
		for (UserRoleRelation user : users) {
			if(user.getRole().getId().equalsIgnoreCase(Constants.XIUYU_ROLE_ID)){
				// 秀域审核主管
				List<String> list = getShopList(user);
				String d = "";
				for (int i = 0; i < list.size(); i++) {
					if(d==null || d==""){
						d = "'"+list.get(i)+"'";
					}else{
						d = d+","+"'"+list.get(i)+"'";
					}
				}
				if(d!=null){
					sql.append(" and a.c_shop_id in ("+d+")");
					break;
				}
			} else if(user.getRole().getId().equalsIgnoreCase(Constants.DIANYUAN_ROLE_ID)){
				sql.append(" and a.c_shop_id = '"+dept.getDeptId()+"'");
				break;
			}
		}

		
		if(odsDto.getOrderCode() != null && !odsDto.getOrderCode().isEmpty()){
			sql.append(" and a.c_order_code like '%").append(odsDto.getOrderCode()).append("%'");
		}

		if(odsDto.getCustomerQ() != null && !odsDto.getCustomerQ().isEmpty()){
			sql.append(" and (a.c_customer_name like '%"+odsDto.getCustomerQ()+"%' or a.c_customer_code like '%"+odsDto.getCustomerQ()+"%')");
		}
		if(odsDto.getGoodsQ() != null && !odsDto.getGoodsQ().isEmpty()){
			sql.append(" and (b.c_goods_name like '%"+odsDto.getGoodsQ()+"%' or b.c_goods_code like '%"+odsDto.getGoodsQ()+"%')");
		}
		if(odsDto.getCity() != null && !odsDto.getCity().isEmpty()){
			sql.append(" and a.c_city like '%").append(odsDto.getCity()).append("%'");
		}
		if(odsDto.getShopName() != null && !odsDto.getShopName().isEmpty()){
			sql.append(" and a.c_shop_name like '%").append(odsDto.getShopName()).append("%'");
		}
		if(odsDto.getOrderCharacter() != null && !odsDto.getOrderCharacter().isEmpty()){
			sql.append(" and a.c_order_character = '").append(odsDto.getOrderCharacter()).append("'");
		}
		if(odsDto.getReset() != null && !odsDto.getReset().isEmpty()){
			sql.append(" and a.c_reset = '").append(odsDto.getReset()).append("'");
		}
		if(odsDto.getStatusBussiness() != null && !odsDto.getStatusBussiness().isEmpty()){
			sql.append(" and a.c_status_bussiness = '").append(odsDto.getStatusBussiness()).append("'");
		}
		if(odsDto.getStatusBeforeProduce() != null && !odsDto.getStatusBeforeProduce().isEmpty()){
			sql.append(" and a.c_status_before_produce = '").append(odsDto.getStatusBeforeProduce()).append("'");
		}
		if(odsDto.getStatusBeforeSend() != null && !odsDto.getStatusBeforeSend().isEmpty()){
			sql.append(" and a.c_status_before_send = '").append(odsDto.getStatusBeforeSend()).append("'");
		}
		if(odsDto.getOrderDetailStatus() != null && !odsDto.getOrderDetailStatus().isEmpty()){
			sql.append(" and b.c_order_detail_status = '").append(odsDto.getOrderDetailStatus()).append("'");
		}
		if(odsDto.getGoodsColorName() != null && !odsDto.getGoodsColorName().isEmpty()){
			sql.append(" and b.c_goods_color_name = '").append(odsDto.getGoodsColorName()).append("'");
		}
		if(odsDto.getCategoryId() != null && !odsDto.getCategoryId().isEmpty()){
			sql.append(" and d.c_category_id = '").append(odsDto.getCategoryId()).append("'");
		}
		if(odsDto.getErpNo() != null && !odsDto.getErpNo().isEmpty()){
			sql.append(" and b.c_erp_no like '%").append(odsDto.getErpNo()).append("%'");
		}
		if(odsDto.getGoodsSn() != null && !odsDto.getGoodsSn().isEmpty()){
			sql.append(" and b.c_goods_sn like '%").append(odsDto.getGoodsSn()).append("%'");
		}
		//添加订单创建日期限定条件
		if(odsDto.getCreateTimeStart() != null && !odsDto.getCreateTimeStart().isEmpty() && odsDto.getCreateTimeEnd() != null && !odsDto.getCreateTimeEnd().isEmpty()){//添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and a.c_create_time between '").append(odsDto.getCreateTimeStart()).append(" 00:00:00'").append(" and '").append(odsDto.getCreateTimeEnd()).append(" 23:59:59'");
		}
		else if(odsDto.getCreateTimeStart() != null && !odsDto.getCreateTimeStart().isEmpty() && (odsDto.getCreateTimeEnd() == null || odsDto.getCreateTimeEnd().isEmpty())){//添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and a.c_create_time >= '").append(odsDto.getCreateTimeStart()).append(" 00:00:00'");
		}
		else if ((odsDto.getCreateTimeStart() == null || odsDto.getCreateTimeStart().isEmpty()) && odsDto.getCreateTimeEnd() != null && !odsDto.getCreateTimeEnd().isEmpty()) {//添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and a.c_create_time <= '").append(odsDto.getCreateTimeEnd()).append(" 23:59:59'");
		}
		//添加物流单发货日期限定条件
		if(odsDto.getSendTimeStart() != null && !odsDto.getSendTimeStart().isEmpty() && odsDto.getSendTimeEnd() != null && !odsDto.getSendTimeEnd().isEmpty()){//添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and tb.c_send_time between '").append(odsDto.getSendTimeStart()).append(" 00:00:00'").append(" and '").append(odsDto.getSendTimeEnd()).append(" 23:59:59'");
		}
		else if(odsDto.getSendTimeStart() != null && !odsDto.getSendTimeStart().isEmpty() && (odsDto.getSendTimeEnd() == null || odsDto.getSendTimeEnd().isEmpty())){//添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and tb.c_send_time >= '").append(odsDto.getSendTimeStart()).append(" 00:00:00'");
		}
		else if ((odsDto.getSendTimeStart() == null || odsDto.getSendTimeStart().isEmpty()) && odsDto.getSendTimeEnd() != null && !odsDto.getSendTimeEnd().isEmpty()) {//添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and tb.c_send_time <= '").append(odsDto.getSendTimeEnd()).append(" 23:59:59'");
		}
		//星域审核日期查询
		if(StringUtils.isNotBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isNotBlank(odsDto.getXingyuChekTimeEnd())){
            sql.append(" and a.c_xingyu_chek_time between '").append(odsDto.getXingyuChekTimeStr()).append(" 00:00:00'").append(" and '").append(odsDto.getXingyuChekTimeEnd()).append(" 23:59:59'");
        }else if(StringUtils.isNotBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isBlank(odsDto.getXingyuChekTimeEnd())){
        	sql.append(" and a.c_xingyu_chek_time >= '").append(odsDto.getXingyuChekTimeStr()).append(" 00:00:00'");
        }else if(StringUtils.isBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isNotBlank(odsDto.getXingyuChekTimeEnd())){
        	sql.append(" and a.c_xingyu_chek_time <= '").append(odsDto.getXingyuChekTimeEnd()).append(" 00:00:00'");
        }
			sql.append(" order by a.c_create_time desc ");
		if(pageBean.getPage()!=null && pageBean.getRows()!=null){
			sql.append(" limit "+(pageBean.getPage() - 1) * pageBean.getRows()+","+pageBean.getRows());
		}
		List resultSet = dao.queryBySql(sql.toString());		
		for(Object result : resultSet){
			OrderDetailSearchModel odsModel = new OrderDetailSearchModel();//构建返回数据模型
			Object[] properties = (Object[])result;
			odsModel.setOrderId(properties[0]==null ? "" : properties[0].toString());
			odsModel.setOrderCode(properties[1]==null ? "" : properties[1].toString());
			odsModel.setErpNum(properties[2]==null ? "" : properties[2].toString());
			odsModel.setGoodsSN(properties[3]==null ? "" : properties[3].toString());
			odsModel.setCity(properties[4]==null ? "" : properties[4].toString());
			odsModel.setShopName(properties[5]==null ? "" : properties[5].toString());
			odsModel.setCharacteres(properties[6]==null ? "" : properties[6].toString());
			odsModel.setCustomerName(properties[7]==null ? "" : properties[7].toString());
			odsModel.setGoodsName(properties[8]==null ? "" : properties[8].toString());
			odsModel.setGoodsColor(properties[9]==null ? "" : properties[9].toString());
			odsModel.setNum(properties[10]==null ? "" : properties[10].toString());
			odsModel.setBillStatus(properties[11]==null ? "" : properties[11].toString());
			odsModel.setOrderDetailStatus(properties[12]==null ? "" : properties[12].toString());
			odsModel.setCreateTime(properties[13]==null ? "" : properties[13].toString());
			odsModel.setSendTime(properties[14]==null ? "" : properties[14].toString());
			odsModel.setMaterielCode(properties[15]==null ? "" : properties[15].toString());
			odsModel.setClosedStatus(properties[16]==null ? "" : properties[16].toString());
			odsModel.setGoodsCode(properties[17]==null ? "" : properties[17].toString());
			odsModel.setOrderDetailId(properties[18]==null ? "" : properties[18].toString());
			odsModel.setEmbroidName(properties[19]==null ? "" : properties[19].toString());
			odsModel.setCategoryName(properties[20]==null ? "" : properties[20].toString());
			odsModel.setCheckerName(properties[21]==null ? "" : properties[21].toString());
			odsModel.setCheckerTime(properties[22]==null ? "" : properties[22].toString());
			odsModel.setRecorderName(properties[23]==null ? "" : properties[23].toString());
			odsModel.setResets(properties[24]==null ? "" : properties[24].toString());
			odsModel.setBeforePriduce(properties[25]==null ? "" : properties[25].toString());
			odsModel.setBeforeSend(properties[26]==null ? "" : properties[26].toString());
			odsModel.setXiuyuCheckTime(properties[27]==null ? "" : properties[27].toString());
			odsModel.setXingyuCheckTime(properties[28]==null ? "" : properties[28].toString());
			odsModel.setExpressNumber(properties[29]==null ? "" : properties[29].toString());
			odsModelList.add(odsModel); 
		}
		return odsModelList;
	}
	
	@Override
	public Long queryCountWithParam(OrderDetailSearchDto odsDto) throws ParseException{
		StringBuilder sql = new StringBuilder("SELECT count(*) " +
				"FROM t_order_detail b " +
				"LEFT JOIN t_order a ON b.c_order_id=a.c_id " +
				"LEFT JOIN t_goods_detail c ON c.c_id=b.c_goods_detail_id " +
				"LEFT JOIN t_goods d ON d.c_id=c.c_goods_id " +
				"LEFT JOIN t_category g on d.c_category_id = g.c_id and g.c_del='0' " +
				"LEFT JOIN (select e.c_order_detail_id,f.c_express_number,f.c_send_time from  t_logistic_order e,t_logistic f where f.c_id=e.c_logistic_id and f.c_delete_flag = '0' and e.c_delete_flag='0' ) tb ON tb.c_order_detail_id=b.c_id  " +
				"where a.c_delete_flag='0' ");
		@SuppressWarnings("unchecked")
		List<UserRoleRelation> users = dao.createCriteria(UserRoleRelation.class)
				.add(Restrictions.eq( "userId", odsDto.getUserId())).list();
		UserDeptRelation dept = dao.findUniqueByProperty(UserDeptRelation.class, "userId", odsDto.getUserId());
		for (UserRoleRelation user : users) {
			if(user.getRole().getId().equals(Constants.XIUYU_ROLE_ID)){
				// 秀域审核主管
				List<String> list = getShopList(user);
				String d = "";
				for (int i = 0; i < list.size(); i++) {
					if(d==null || d==""){
						d = "'"+list.get(i)+"'";
					}else{
						d = d+","+"'"+list.get(i)+"'";
					}
				}
				if(d!=null){
					sql.append(" and a.c_shop_id in ("+d+")");
					break;
				}
			} else if(user.getRole().getId().equals(Constants.DIANYUAN_ROLE_ID)){
				sql.append(" and a.c_shop_id = '"+dept.getDeptId()+"'");
				break;
			}
		}

		if(odsDto.getOrderCode() != null && !odsDto.getOrderCode().isEmpty()){
			sql.append(" and a.c_order_code like '%").append(odsDto.getOrderCode()).append("%'");
		}
		if(odsDto.getCustomerName() != null && !odsDto.getCustomerName().isEmpty()){
			sql.append(" and a.c_customer_name like '%").append(odsDto.getCustomerName()).append("%'");
		}
		if(odsDto.getCity() != null && !odsDto.getCity().isEmpty()){
			sql.append(" and a.c_city like '%").append(odsDto.getCity()).append("%'");
		}
		if(odsDto.getShopName() != null && !odsDto.getShopName().isEmpty()){
			sql.append(" and a.c_shop_name like '%").append(odsDto.getShopName()).append("%'");
		}
		if(odsDto.getOrderCharacter() != null && !odsDto.getOrderCharacter().isEmpty()){
			sql.append(" and a.c_order_character = '").append(odsDto.getOrderCharacter()).append("'");
		}
		if(odsDto.getReset() != null && !odsDto.getReset().isEmpty()){
			sql.append(" and a.c_reset = '").append(odsDto.getReset()).append("'");
		}
		if(odsDto.getStatusBussiness() != null && !odsDto.getStatusBussiness().isEmpty()){
			sql.append(" and a.c_status_bussiness = '").append(odsDto.getStatusBussiness()).append("'");
		}
		if(odsDto.getStatusBeforeProduce() != null && !odsDto.getStatusBeforeProduce().isEmpty()){
			sql.append(" and a.c_status_before_produce = '").append(odsDto.getStatusBeforeProduce()).append("'");
		}
		if(odsDto.getStatusBeforeSend() != null && !odsDto.getStatusBeforeSend().isEmpty()){
			sql.append(" and a.c_status_before_send = '").append(odsDto.getStatusBeforeSend()).append("'");
		}
		if(odsDto.getOrderDetailStatus() != null && !odsDto.getOrderDetailStatus().isEmpty()){
			sql.append(" and b.c_order_detail_status = '").append(odsDto.getOrderDetailStatus()).append("'");
		}
		if(odsDto.getGoodsName() != null && !odsDto.getGoodsName().isEmpty()){
			sql.append(" and b.c_goods_name like '%").append(odsDto.getGoodsName()).append("%'");
		}
		if(odsDto.getGoodsColorName() != null && !odsDto.getGoodsColorName().isEmpty()){
			sql.append(" and b.c_goods_color_name = '").append(odsDto.getGoodsColorName()).append("'");
		}
		if(odsDto.getCategoryId() != null && !odsDto.getCategoryId().isEmpty()){
			sql.append(" and d.c_category_id = '").append(odsDto.getCategoryId()).append("'");
		}
		if(odsDto.getErpNo() != null && !odsDto.getErpNo().isEmpty()){
			sql.append(" and b.c_erp_no like '%").append(odsDto.getErpNo()).append("%'");
		}
		if(odsDto.getGoodsSn() != null && !odsDto.getGoodsSn().isEmpty()){
			sql.append(" and b.c_goods_sn like '%").append(odsDto.getGoodsSn()).append("%'");
		}
		//添加订单创建日期限定条件
		if(odsDto.getCreateTimeStart() != null && !odsDto.getCreateTimeStart().isEmpty() && odsDto.getCreateTimeEnd() != null && !odsDto.getCreateTimeEnd().isEmpty()){//添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and a.c_create_time between '").append(odsDto.getCreateTimeStart()).append(" 00:00:00'").append(" and '").append(odsDto.getCreateTimeEnd()).append(" 23:59:59'");
		}
		else if(odsDto.getCreateTimeStart() != null && !odsDto.getCreateTimeStart().isEmpty() && (odsDto.getCreateTimeEnd() == null || odsDto.getCreateTimeEnd().isEmpty())){//添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and a.c_create_time >= '").append(odsDto.getCreateTimeStart()).append(" 00:00:00'");
		}
		else if ((odsDto.getCreateTimeStart() == null || odsDto.getCreateTimeStart().isEmpty()) && odsDto.getCreateTimeEnd() != null && !odsDto.getCreateTimeEnd().isEmpty()) {//添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and a.c_create_time <= '").append(odsDto.getCreateTimeEnd()).append(" 23:59:59'");
		}
		//添加物流单发货日期限定条件
		if(odsDto.getSendTimeStart() != null && !odsDto.getSendTimeStart().isEmpty() && odsDto.getSendTimeEnd() != null && !odsDto.getSendTimeEnd().isEmpty()){//添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and tb.c_send_time between '").append(odsDto.getSendTimeStart()).append(" 00:00:00'").append(" and '").append(odsDto.getSendTimeEnd()).append(" 23:59:59'");
		}
		else if(odsDto.getSendTimeStart() != null && !odsDto.getSendTimeStart().isEmpty() && (odsDto.getSendTimeEnd() == null || odsDto.getSendTimeEnd().isEmpty())){//添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and tb.c_send_time >= '").append(odsDto.getSendTimeStart()).append(" 00:00:00'");
		}
		else if ((odsDto.getSendTimeStart() == null || odsDto.getSendTimeStart().isEmpty()) && odsDto.getSendTimeEnd() != null && !odsDto.getSendTimeEnd().isEmpty()) {//添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and tb.c_send_time <= '").append(odsDto.getSendTimeEnd()).append(" 23:59:59'");
		}
		//星域审核日期查询
		if(StringUtils.isNotBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isNotBlank(odsDto.getXingyuChekTimeEnd())){
            sql.append(" and a.c_xingyu_chek_time between '").append(odsDto.getXingyuChekTimeStr()).append(" 00:00:00'").append(" and '").append(odsDto.getXingyuChekTimeEnd()).append(" 23:59:59'");
        }else if(StringUtils.isNotBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isBlank(odsDto.getXingyuChekTimeEnd())){
        	sql.append(" and a.c_xingyu_chek_time >= '").append(odsDto.getXingyuChekTimeStr()).append(" 00:00:00'");
        }else if(StringUtils.isBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isNotBlank(odsDto.getXingyuChekTimeEnd())){
        	sql.append(" and a.c_xingyu_chek_time <= '").append(odsDto.getXingyuChekTimeEnd()).append(" 00:00:00'");
        }
		List<BigInteger> result = dao.queryBySql(sql.toString());
		return result == null || result.isEmpty() ? 0L : result.get(0).longValue();
	}

	@Override
	public Pagination<OrderDetailSearchModel> orderDetailList(OrderDetailSearchDto odsDto, PageBean pageBean) {
		List<OrderDetailSearchModel> odsModelList = new ArrayList<>();
		Pagination<OrderDetailSearchModel> pagination = new Pagination<>();
		Long count = 0L;//lllll
		StringBuilder sqlCount = new StringBuilder("select count(1) ");
		StringBuilder sqlList  = new StringBuilder("SELECT " +
				"a.c_id AS orderId," +
				"a.c_order_code AS orderCode," +
				"b.c_erp_no AS erpNo," +
				"b.c_goods_sn AS goodsSn," +
				"a.c_city,a.c_shop_name," +
				"a.c_order_character," +
				"a.c_customer_name," +
				"d.c_name goods_name," +
				"b.c_goods_color_name," +
				"b.c_num," +
				"a.c_status_bussiness," +
				"b.c_order_detail_status," +
				"a.c_create_time AS createTime," +
				"tb.c_send_time AS sendTime," +
				"c.c_materiel_code," +
				"b.c_closed_status," +
				"d.c_code,b.c_id AS orderDetailId," +
				"a.c_embroid_name," +
				"g.c_name AS categoryName," +
				"a.c_business_checker_name," +
				"a.c_business_checker_time," +
				"a.c_recorder_name," +
				"a.c_reset," +
				"a.c_status_before_produce," +
				"a.c_status_before_send," +
				"a.c_xiuyu_chek_time," +
				"a.c_xingyu_chek_time," +
				"tb.c_express_number as expressNumber,t.c_tenant_name,h.company_name "  );

		StringBuilder sql = new StringBuilder(
				"FROM t_order_detail b " +
				"LEFT JOIN t_order a ON b.c_order_id=a.c_id " +
				"LEFT JOIN t_goods_detail c ON c.c_id=b.c_goods_detail_id " +
				"LEFT JOIN t_goods d ON d.c_id=c.c_goods_id " +
				"LEFT JOIN t_category g on d.c_category_id = g.c_id and g.c_del='0' " +
				"LEFT JOIN (select e.c_order_detail_id,f.c_express_number,f.c_send_time from  t_logistic_order e,t_logistic f where f.c_id=e.c_logistic_id and f.c_delete_flag = '0' and e.c_delete_flag='0' ) tb ON tb.c_order_detail_id=b.c_id  " +
				"LEFT JOIN t_tenant t ON a.c_tenant_id = t.c_id " +
				"LEFT JOIN (SELECT shop.c_id AS shop_id,shop.c_dept_name AS shop_name,company.c_dept_name AS company_name FROM t_dept shop,t_dept company WHERE shop.c_parent_id = company.c_id AND shop.c_dept_type='1' ) h ON a.c_shop_id = h.shop_id   " +
				"where a.c_delete_flag='0' ");

		if(StringUtils.isNotBlank(odsDto.getTenantId())){
			sql.append(" and a.c_tenant_id ='").append(odsDto.getTenantId()).append("'");
		}
		if (StringUtils.isNotBlank(odsDto.getUserId())){
			sql.append(" and  a.c_shop_id in (select c_shop_id from t_xiuyu_shop_director_relation where c_user_id='").append(odsDto.getUserId()).append("')");
		}

		if(odsDto.getOrderCode() != null && !odsDto.getOrderCode().isEmpty()){
			sql.append(" and a.c_order_code like '%").append(odsDto.getOrderCode()).append("%'");
		}

		if (StringUtils.isNotBlank(odsDto.getCompanyName())){
			sql.append(" and h.company_name like '%").append(odsDto.getCompanyName()).append("%'");
		}

		if(odsDto.getCustomerQ() != null && !odsDto.getCustomerQ().isEmpty()){
			sql.append(" and (a.c_customer_name like '%"+odsDto.getCustomerQ()+"%' or a.c_customer_code like '%"+odsDto.getCustomerQ()+"%')");
		}
		if(odsDto.getGoodsQ() != null && !odsDto.getGoodsQ().isEmpty()){
			sql.append(" and (d.c_name like '%"+odsDto.getGoodsQ()+"%' or d.c_code like '%"+odsDto.getGoodsQ()+"%')");
		}
		if(odsDto.getCity() != null && !odsDto.getCity().isEmpty()){
			sql.append(" and a.c_city like '%").append(odsDto.getCity()).append("%'");
		}
		if(odsDto.getShopName() != null && !odsDto.getShopName().isEmpty()){
			sql.append(" and a.c_shop_name like '%").append(odsDto.getShopName()).append("%'");
		}
		if(odsDto.getOrderCharacter() != null && !odsDto.getOrderCharacter().isEmpty()){
			sql.append(" and a.c_order_character = '").append(odsDto.getOrderCharacter()).append("'");
		}
		if(odsDto.getReset() != null && !odsDto.getReset().isEmpty()){
			sql.append(" and a.c_reset = '").append(odsDto.getReset()).append("'");
		}
		if(odsDto.getStatusBussiness() != null && !odsDto.getStatusBussiness().isEmpty()){
			sql.append(" and a.c_status_bussiness = '").append(odsDto.getStatusBussiness()).append("'");
		}
		if(odsDto.getStatusBeforeProduce() != null && !odsDto.getStatusBeforeProduce().isEmpty()){
			sql.append(" and a.c_status_before_produce = '").append(odsDto.getStatusBeforeProduce()).append("'");
		}
		if(odsDto.getStatusBeforeSend() != null && !odsDto.getStatusBeforeSend().isEmpty()){
			sql.append(" and a.c_status_before_send = '").append(odsDto.getStatusBeforeSend()).append("'");
		}
		if(odsDto.getOrderDetailStatus() != null && !odsDto.getOrderDetailStatus().isEmpty()){
			sql.append(" and b.c_order_detail_status = '").append(odsDto.getOrderDetailStatus()).append("'");
		}
		if(odsDto.getGoodsColorName() != null && !odsDto.getGoodsColorName().isEmpty()){
			sql.append(" and b.c_goods_color_name = '").append(odsDto.getGoodsColorName()).append("'");
		}
		if(odsDto.getCategoryId() != null && !odsDto.getCategoryId().isEmpty()){
			sql.append(" and d.c_category_id = '").append(odsDto.getCategoryId()).append("'");
		}
		if(odsDto.getErpNo() != null && !odsDto.getErpNo().isEmpty()){
			sql.append(" and b.c_erp_no like '%").append(odsDto.getErpNo()).append("%'");
		}
		if(odsDto.getGoodsSn() != null && !odsDto.getGoodsSn().isEmpty()){
			sql.append(" and b.c_goods_sn like '%").append(odsDto.getGoodsSn()).append("%'");
		}
		//添加订单创建日期限定条件
		if(odsDto.getCreateTimeStart() != null && !odsDto.getCreateTimeStart().isEmpty() && odsDto.getCreateTimeEnd() != null && !odsDto.getCreateTimeEnd().isEmpty()){//添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and a.c_create_time between '").append(odsDto.getCreateTimeStart()).append(" 00:00:00'").append(" and '").append(odsDto.getCreateTimeEnd()).append(" 23:59:59'");
		}
		else if(odsDto.getCreateTimeStart() != null && !odsDto.getCreateTimeStart().isEmpty() && (odsDto.getCreateTimeEnd() == null || odsDto.getCreateTimeEnd().isEmpty())){//添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and a.c_create_time >= '").append(odsDto.getCreateTimeStart()).append(" 00:00:00'");
		}
		else if ((odsDto.getCreateTimeStart() == null || odsDto.getCreateTimeStart().isEmpty()) && odsDto.getCreateTimeEnd() != null && !odsDto.getCreateTimeEnd().isEmpty()) {//添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and a.c_create_time <= '").append(odsDto.getCreateTimeEnd()).append(" 23:59:59'");
		}
		//添加物流单发货日期限定条件
		if(odsDto.getSendTimeStart() != null && !odsDto.getSendTimeStart().isEmpty() && odsDto.getSendTimeEnd() != null && !odsDto.getSendTimeEnd().isEmpty()){//添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and tb.c_send_time between '").append(odsDto.getSendTimeStart()).append(" 00:00:00'").append(" and '").append(odsDto.getSendTimeEnd()).append(" 23:59:59'");
		}
		else if(odsDto.getSendTimeStart() != null && !odsDto.getSendTimeStart().isEmpty() && (odsDto.getSendTimeEnd() == null || odsDto.getSendTimeEnd().isEmpty())){//添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and tb.c_send_time >= '").append(odsDto.getSendTimeStart()).append(" 00:00:00'");
		}
		else if ((odsDto.getSendTimeStart() == null || odsDto.getSendTimeStart().isEmpty()) && odsDto.getSendTimeEnd() != null && !odsDto.getSendTimeEnd().isEmpty()) {//添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and tb.c_send_time <= '").append(odsDto.getSendTimeEnd()).append(" 23:59:59'");
		}
		//星域审核日期查询
		if(StringUtils.isNotBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isNotBlank(odsDto.getXingyuChekTimeEnd())){
			sql.append(" and a.c_xingyu_chek_time between '").append(odsDto.getXingyuChekTimeStr()).append(" 00:00:00'").append(" and '").append(odsDto.getXingyuChekTimeEnd()).append(" 23:59:59'");
		}else if(StringUtils.isNotBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isBlank(odsDto.getXingyuChekTimeEnd())){
			sql.append(" and a.c_xingyu_chek_time >= '").append(odsDto.getXingyuChekTimeStr()).append(" 00:00:00'");
		}else if(StringUtils.isBlank(odsDto.getXingyuChekTimeStr()) && StringUtils.isNotBlank(odsDto.getXingyuChekTimeEnd())){
			sql.append(" and a.c_xingyu_chek_time <= '").append(odsDto.getXingyuChekTimeEnd()).append(" 00:00:00'");
		}
		sqlList.append(sql);
		sqlList.append(" order by a.c_create_time desc");
		if(pageBean.getPage()!=null && pageBean.getRows()!=null){
			sqlList.append(" limit "+(pageBean.getPage() - 1) * pageBean.getRows()+","+pageBean.getRows());

			sqlCount.append(sql);
			List<BigInteger> result = dao.queryBySql(sqlCount.toString());
			count = result == null || result.isEmpty() ? 0L : result.get(0).longValue();
		}

		List resultSet = dao.queryBySql(sqlList.toString());
		for(Object result : resultSet){
			OrderDetailSearchModel odsModel = new OrderDetailSearchModel();//构建返回数据模型
			Object[] properties = (Object[])result;
			odsModel.setOrderId(properties[0]==null ? "" : properties[0].toString());
			odsModel.setOrderCode(properties[1]==null ? "" : properties[1].toString());
			odsModel.setErpNum(properties[2]==null ? "" : properties[2].toString());
			odsModel.setGoodsSN(properties[3]==null ? "" : properties[3].toString());
			odsModel.setCity(properties[4]==null ? "" : properties[4].toString());
			odsModel.setShopName(properties[5]==null ? "" : properties[5].toString());
			odsModel.setCharacteres(properties[6]==null ? "" : properties[6].toString());
			odsModel.setCustomerName(properties[7]==null ? "" : properties[7].toString());
			odsModel.setGoodsName(properties[8]==null ? "" : properties[8].toString());
			odsModel.setGoodsColor(properties[9]==null ? "" : properties[9].toString());
			odsModel.setNum(properties[10]==null ? "" : properties[10].toString());
			odsModel.setBillStatus(properties[11]==null ? "" : properties[11].toString());
			odsModel.setOrderDetailStatus(properties[12]==null ? "" : properties[12].toString());
			odsModel.setCreateTime(properties[13]==null ? "" : properties[13].toString());
			odsModel.setSendTime(properties[14]==null ? "" : properties[14].toString());
			odsModel.setMaterielCode(properties[15]==null ? "" : properties[15].toString());
			odsModel.setClosedStatus(properties[16]==null ? "" : properties[16].toString());
			odsModel.setGoodsCode(properties[17]==null ? "" : properties[17].toString());
			odsModel.setOrderDetailId(properties[18]==null ? "" : properties[18].toString());
			odsModel.setEmbroidName(properties[19]==null ? "" : properties[19].toString());
			odsModel.setCategoryName(properties[20]==null ? "" : properties[20].toString());
			odsModel.setCheckerName(properties[21]==null ? "" : properties[21].toString());
			odsModel.setCheckerTime(properties[22]==null ? "" : properties[22].toString());
			odsModel.setRecorderName(properties[23]==null ? "" : properties[23].toString());
			odsModel.setResets(properties[24]==null ? "" : properties[24].toString());
			odsModel.setBeforePriduce(properties[25]==null ? "" : properties[25].toString());
			odsModel.setBeforeSend(properties[26]==null ? "" : properties[26].toString());
			odsModel.setXiuyuCheckTime(properties[27]==null ? "" : properties[27].toString());
			odsModel.setXingyuCheckTime(properties[28]==null ? "" : properties[28].toString());
			odsModel.setExpressNumber(properties[29]==null ? "" : properties[29].toString());
			odsModel.setTenantName(properties[30]==null ? "" : properties[30].toString());
			odsModel.setCompanyName(properties[31]==null ? "" : properties[31].toString());
			odsModelList.add(odsModel);
		}

		pagination.setTotal(count);
		pagination.setRows(odsModelList);

		return 	pagination;
	}

	@Override
	public Boolean checkDuplication(String id) {
		Order order = dao.findById(Order.class,id);
		if(order !=null){
			String sql = "select c_id from t_order where c_shop_id='"+order.getShopId()+"' and c_customer_name='"+order.getCustomerName()+"' and c_id <> '"+order.getId()+"' and c_create_time >= date_add(now(), interval -1 month)  and c_delete_flag='0'";
			List<String> list = dao.queryBySql(sql);
			if(list.size()> 0){
				List<BigInteger> details = dao.queryBySql("select count(1) from t_order_detail where c_order_id in ("+sql+") and c_goods_detail_id in (select c_goods_detail_id from t_order_detail where c_order_id ='"+id+"') ");
                if(details.size()> 0){
                	if(Integer.parseInt(details.get(0).toString()) > 0){
                		return true;
					}
				}
			}
		}
		return false;
	}


	@Override
	public Pagination<OrderFinishedLabel> getOrderFinishedLabel(OrderFinishedLabel dto, PageBean pageBean) {
		Pagination<OrderFinishedLabel> pagination = new Pagination<>();
		StringBuilder sql = new StringBuilder(" FROM t_order a LEFT JOIN t_order_detail b ON a.c_id=b.c_order_id LEFT JOIN t_goods_detail c ON c.c_id=b.c_goods_detail_id LEFT JOIN t_goods d ON d.c_id=c.c_goods_id WHERE 1=1");
		if (StringUtils.isNotBlank(dto.getCode())) {
			sql.append(" and a.c_code like '%").append(dto.getCode()).append("%'");
		}
//		if (StringUtils.isNotBlank(dto.getPlanNo())) {
//			sql.append(" and opp.plan_no like '%").append(dto.getPlanNo()).append("%'");
//		}
		if (StringUtils.isNotBlank(dto.getCustomerName())) {
			sql.append(" and a.c_customer_name like '%").append(dto.getCustomerName()).append("%'");
		}
		if (StringUtils.isNotBlank(dto.getStyleSN())) {
			sql.append(" and b.c_goods_sn like '%").append(dto.getStyleSN()).append("%'");
		}
		if (StringUtils.isNotBlank(dto.getStyleCode())) {
			sql.append(" and d.c_code like '%").append(dto.getStyleCode()).append("%'");
		}
		if (StringUtils.isNotBlank(dto.getStyleName())) {
			sql.append(" and d.c_name like '%").append(dto.getStyleName()).append("%'");
		}
		if (StringUtils.isNotBlank(dto.getColor())) {
			sql.append(" and b.c_goods_color_name like '%").append(dto.getColor()).append("%'");
		}
		if (StringUtils.isNotBlank(dto.getShopName())) {
			sql.append(" and a.c_shop_name like '%").append(dto.getShopName()).append("%'");
		}
		List<BigInteger> result = dao.queryBySql(" select count(*) " + sql.toString());
		Long total = ((result == null || result.isEmpty()) ? 0L : result.get(0).longValue());
		if (total != null) {
			pagination.setTotal(total);
		}
		List<OrderFinishedLabel> returnList = new ArrayList<OrderFinishedLabel>();
		sql.append(" limit " + (pageBean.getPage() - 1) * pageBean.getRows() + "," + pageBean.getRows());
		List resultSet = dao.queryBySql("SELECT a.c_order_code,a.c_customer_name,b.c_goods_sn,d.c_code,d.c_name,b.c_goods_color_name,a.c_shop_name "+sql.toString());
		for (Object item : resultSet) {
			OrderFinishedLabel ofl = new OrderFinishedLabel();// 构建返回数据模型
			Object[] properties = (Object[]) item;
			ofl.setCode(properties[0] == null ? "" : properties[0].toString());
//			ofl.setPlanNo(properties[1] == null ? "" : properties[1].toString());
			ofl.setCustomerName(properties[1] == null ? "" : properties[1].toString());
			ofl.setStyleSN(properties[2] == null ? "" : properties[2].toString());
			ofl.setStyleCode(properties[3] == null ? "" : properties[3].toString());
			ofl.setStyleName(properties[4] == null ? "" : properties[4].toString());
			ofl.setColor(properties[5] == null ? "" : properties[5].toString());
			ofl.setShopName(properties[6] == null ? "" : properties[6].toString());
			returnList.add(ofl);
		}
		pagination.setRows(returnList);
		return pagination;
	}
	
	@Override
	public Result remoteOrder(OrderDto dto,PageBean pageBean){
		Criteria criteria = dao.createCriteria(Order.class);
		if(StringUtils.isNotBlank(dto.getQ())){
			criteria.add(Restrictions.like("orderCode", dto.getQ() ,MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(dto.getTenantId())){
			criteria.add(Restrictions.eq("tenantId",dto.getTenantId()));
		}
		if (StringUtils.isBlank(dto.getStatusBussiness())){
			criteria.add(Restrictions.in("statusBussiness",new String[]{"3","6","7","8"}));
		}
		List<Order> list = dao.findListWithPagebeanCriteria(criteria, pageBean);
		for (int i = 0; i < list.size(); i++) {
			List<OrderDetail> detailList = list.get(i).getOrderDetailList();
			for (int j = detailList.size() - 1;j >= 0;j--){
				if (Pattern.matches("[^2,3,4]",detailList.get(j).getOrderDetailStatus())){
					detailList.remove(j);
				}else {
					GoodsDetail goodsDetail = detailList.get(j).getGoodsDetail();
					String goodsId = goodsDetail.getGoodsId();
					Goods goods = dao.findById(Goods.class, goodsId);
					goodsDetail.setDetail(goods.getDetail());
					goodsDetail.setCategoryName(goods.getCategory().getName());
					goodsDetail.setCategoryId(goods.getCategory().getId());
				}
			}
//			for (int j = 0; j < detailList.size(); j++) {
//				GoodsDetail goodsDetail = detailList.get(j).getGoodsDetail();
//				String goodsId = goodsDetail.getGoodsId();
//				Goods goods = dao.findById(Goods.class, goodsId);
//				goodsDetail.setDetail(goods.getDetail());
//				goodsDetail.setCategoryName(goods.getCategory().getName());
//				goodsDetail.setCategoryId(goods.getCategory().getId());
//			}
			
		}
		return new Result(list);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Pagination<Order> remoteOrderByCustomer(String custom,PageBean pageBean){
		if (pageBean.getRows()==null||pageBean.getPage()==null){
			pageBean.setPage(1);
			pageBean.setRows(10);
		}
		Criteria cusCriteria = dao.createCriteria(XiuyuCustomer.class);
		List<XiuyuCustomer> cusList = cusCriteria.add(Restrictions.eq("customerName",custom)).list();
		Criteria criteria = dao.createCriteria(Order.class);
		Criterion[] criterion;
		if (cusList!=null&&cusList.size()>0){
			criterion = new Criterion[cusList.size()];
			for(int i=0;i<cusList.size();i++){
				criterion[i]=Restrictions.eq("customerId",cusList.get(i).getId());
			}
			criteria.add(Restrictions.or(criterion));
		} else {
			criteria.add(Restrictions.eq("customerId",""));
		}
		List<Order> list = dao.findListWithPagebeanCriteria(criteria, pageBean);
		long total = dao.findTotalWithCriteria(criteria);
		for (int i = 0; i < list.size(); i++) {
			List<OrderDetail> detailList = list.get(i).getOrderDetailList();
			for (int j = 0; j < detailList.size(); j++) {
				GoodsDetail goodsDetail = detailList.get(j).getGoodsDetail();
				String goodsId = goodsDetail.getGoodsId();
				Goods goods = dao.findById(Goods.class, goodsId);
				goodsDetail.setDetail(goods.getDetail());
				goodsDetail.setCategoryName(goods.getCategory().getName());
				goodsDetail.setCategoryId(goods.getCategory().getId());
			}
		}
		return new Pagination<Order>(list,total);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Pagination<RemoteOrderCustomerModel> remoteOrderCustomer(OrderDto dto, PageBean pageBean){
		if (pageBean.getRows()==null||pageBean.getPage()==null){
			pageBean.setPage(1);
			pageBean.setRows(10);
		}
		StringBuilder listSql = new StringBuilder("select tod.c_id ");
		StringBuilder countSql = new StringBuilder("select count(1) ");
		StringBuilder baseSql = new StringBuilder(" from t_order_detail tod \n" +
				"inner join t_order tor on tod.c_order_id=tor.c_id\n" +
				"left join t_xiuyu_customer txc on tor.c_customer_id=txc.c_id\n" +
				"where tor.c_delete_flag='0' and txc.c_delete_flag='0' ");
		if (StringUtils.isNotBlank(dto.getCustomer())){
			baseSql.append(" and (txc.c_customer_name='").append(dto.getCustomer()).append("' or txc.c_customer_code='").append(dto.getCustomer()).append("') ");
		} else {
			baseSql.append(" and 1=0 ");
		}
		if(StringUtils.isNotBlank(dto.getTenantId())){
			baseSql.append(" and tor.c_tenant_id='").append(dto.getTenantId()).append("'");
		}
		countSql.append(baseSql);
		listSql.append(baseSql)
				.append(" ORDER BY tor.c_create_time DESC limit ")
				.append((pageBean.getPage() - 1) * pageBean.getRows())
				.append(",")
				.append(pageBean.getRows());
		List<Object> list = dao.queryBySql(listSql.toString());
		List<Object> counts = dao.queryBySql(countSql.toString());
		Set<String> ids = new HashSet<>();
		for (Object orderDetailId : list) {
			ids.add(orderDetailId==null?"":orderDetailId.toString());
		}
		if (ids.size()==0){
			ids.add("");
		}
		Criteria criteria = dao.createCriteria(OrderDetail.class);
		criteria.add(Restrictions.in("id",ids));
		List<OrderDetail> orderDetails = criteria.list();
		List<RemoteOrderCustomerModel> rocms = new ArrayList<>();
		Map<String,Order> orderMap = new HashMap<>();
		for (OrderDetail orderDetail : orderDetails) {
			GoodsDetail goodsDetail = orderDetail.getGoodsDetail();
			String goodsId = goodsDetail.getGoodsId();
			Goods goods = dao.findById(Goods.class, goodsId);
			goodsDetail.setDetail(goods.getDetail());
			goodsDetail.setCategoryName(goods.getCategory().getName());
			goodsDetail.setCategoryId(goods.getCategory().getId());
			RemoteOrderCustomerModel remoteOrderCustomerModel = new RemoteOrderCustomerModel();
			remoteOrderCustomerModel.setOrderDetail(orderDetail);
			Order order = null;
			if (orderMap.get(orderDetail.getOrderId())==null){
				order = dao.findById(Order.class,orderDetail.getOrderId());
				orderMap.put(orderDetail.getOrderId(),order);
			} else {
				order = orderMap.get(orderDetail.getOrderId());
			}
			remoteOrderCustomerModel.setOrder(order);
			if (null!=order && null!=order.getOrderDetailList()){
				for (OrderDetail orderDetail_temp : order.getOrderDetailList()) {
					if (orderDetail.getId().equals(orderDetail_temp.getId())){
						GoodsDetail goodsDetail_temp = orderDetail_temp.getGoodsDetail();
						if (null!=goodsDetail_temp){
							goodsDetail_temp.setDetail(goods.getDetail());
							goodsDetail_temp.setCategoryName(goods.getCategory().getName());
							goodsDetail_temp.setCategoryId(goods.getCategory().getId());
						}
					}
				}
			}
			rocms.add(remoteOrderCustomerModel);
		}
		return new Pagination<RemoteOrderCustomerModel>(rocms,Long.parseLong(counts==null||counts.size()==0?"0":counts.get(0).toString()));
	}
	
	public String changeOrderStatus(String a){
		String value= "";
		if("0".equals(a)){
			value = "未送出";
		}else if("1".equals(a)){
			value = "已送出";
		}else if("2".equals(a)){
			value = "已提交工厂";
		}else if("3".equals(a)){
			value = "工厂审核通过";
		}else if("4".equals(a)){
			value = "工厂驳回";
		}else if("5".equals(a)){
			value = "主管驳回";
		}else if("6".equals(a)){
			value = "生产完成";
		}else if("7".equals(a)){
			value = "已发货";
		}else if("8".equals(a)){
			value = "已收货";
		}
		return value;
	}
	
	
	public String changeStatus(String a) {
		String value = "";
		if("0".equals(a)) {
			value = "未审核";
		}else if("1".equals(a)) {
			value = "审核不通过";
		}else if ("2".equals(a)) {
			value = "审核通过";
		}
		return value;
	}
	
	
	public String changeOrderDetailStatus(String a){
		String value= "";
		if("0".equals(a)){
			value = "已排程";
		}else if("1".equals(a)){
			value = "生产中";
		}else if("2".equals(a)){
			value = "生产完成";
		}else if("3".equals(a)){
			value = "已发货";
		}else if("4".equals(a)){
			value = "已收货";
		}else if("5".equals(a)){
			value = "已反馈";
		}
		return value;
	}
	
	public String changeClosedStatus(String a){
		String value= "";
		if("1".equals(a)){
			value = "未提交";
		}else if("2".equals(a)){
			value = "已提交";
		}else if("3".equals(a)){
			value = "工厂审核通过";
		}else if("4".equals(a)){
			value = "工厂驳回";
		}
		return value;
	}
	
	@Override
	public Result saveGoodsDetail(OrderDetail orderDetail){
		GoodsDetail goodsDetail = dao.findById(GoodsDetail.class, orderDetail.getGoodsDetailId());
		Goods goods = dao.findById(Goods.class, goodsDetail.getGoodsId());
		setGoodsSN(orderDetail, goods);
		orderDetail.setGoodsCode(goods.getCode());
		orderDetail.setGoodsColorName(goodsDetail.getColorName());
		orderDetail.setGoodsName(goods.getName());
		orderDetail.setGoodsDetailImageKeys(goodsDetail.getImageKeys());
		orderDetail.setErpNo(orderDetail.getErpNo().trim());
		orderDetail.setOrderDetailStatus("6"); 
		dao.saveOrUpdate(orderDetail);
		Criteria criteria = dao.createCriteria(OrderDetail.class);
		criteria.add(Restrictions.eq("orderId", orderDetail.getOrderId())); 
		@SuppressWarnings("unchecked")
		List<OrderDetail> list = criteria.list();
		for (int i = 0; i < list.size(); i++) {
			GoodsDetail goodsDetails = dao.findById(GoodsDetail.class, list.get(i).getGoodsDetailId());
			Goods goodss = dao.findById(Goods.class, goodsDetails.getGoodsId());
			String categoryName = goodss.getCategory().getName();
			list.get(i).setGoodsDetail(goodsDetails);
			list.get(i).setCategoryName(categoryName);  
		}
		return new Result(list);
	}
	
	@Override
	public Result delOrderDetail(String orderDetailId){
		OrderDetail orderDetail = dao.findById(OrderDetail.class, orderDetailId);
		if(orderDetail!=null){
			dao.delete(orderDetail);
		}
		return new Result("200","删除成功");
	}

	private void setGoodsSN(OrderDetail orderDetail, Goods goods) {
		Order order = dao.findById(Order.class, orderDetail.getOrderId());
		Criteria criteria = dao.createCriteria(Code.class);
		criteria.add(Restrictions.eq("type", "SN"));
		criteria.add(Restrictions.between("updateTime", DateUtil.minDate(order.getCreateTime()),
				DateUtil.maxDate(order.getCreateTime())));
		criteria.setLockMode(LockMode.PESSIMISTIC_WRITE);
		Code code = (Code) criteria.uniqueResult();
		if (code == null) {
			code = new Code();
			code.setMaxValue(0);
			code.setType("SN");
			code.setUpdateTime(order.getCreateTime());
			dao.save(code);
		}
		orderDetail.setGoodsSn(CodeUtil.createStyleSN(goods.getCode(), code.getMaxValue(), order.getCreateTime()));
		code.setMaxValue(code.getMaxValue() + 1);
	}
	
	@Override
	public Result findSizeCode(String number){
		Criteria criteria = dao.createCriteria(SizeCode.class);
		criteria.add(Restrictions.le("minNumber", Double.parseDouble(number)));
		criteria.add(Restrictions.gt("maxNumber", Double.parseDouble(number)));
		@SuppressWarnings("unchecked")
		List<SizeCode> list = criteria.list();
		if(list!=null && !list.isEmpty()){
			String code = list.get(0).getSizeCode();
			return new Result(code);
		}else{
			return new Result("500","未找到CUP适用规格，请重新填写");
		}
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
	
	@Override
	public Result uploadFile(OrderDto dto,MultipartFile[] files){
		String[] delFileList = dto.getImageDelKeys();
		if (delFileList != null && delFileList.length > 0) {
			/*OrderAttachment orderAttachment = dao.findUniqueByProperty(OrderAttachment.class, "orderId", dto.getId());
			delOSSFile(orderAttachment, delFileList,dto);*/
			List<OrderAttachment> orderAttachments = dao.findListByProperty(OrderAttachment.class, "orderId", dto.getId());
			delOSSFile(orderAttachments, delFileList);
		}
		Order order = dao.findById(Order.class, dto.getId());
		if (files!=null){
			for (int i=0;i<files.length;i++) {
				MultipartFile file = files[i];
				String fileName;
				if (dto.getFileName()!=null&&files.length==dto.getFileName().length){
					fileName = dto.getFileName()[i];
				} else {
					fileName = file.getOriginalFilename();
				}
				String key = appendOrderImageTwo(order,file);
				if (key!=null){
					OrderAttachment orderAttachment = new OrderAttachment();
					orderAttachment.setOrderId(order.getId());
					orderAttachment.setOssKey(key);
					orderAttachment.setFileName(fileName);
					dao.save(orderAttachment);
				}
			}
		}
		/*if (files != null && files.length > 0) {
			OrderAttachment orderAttachment = dao.findUniqueByProperty(OrderAttachment.class, "orderId", dto.getId());
			if(orderAttachment!=null){
				Set<String> OssKey = null;
				OssKey = appendOrderImageTwo(order,files);
				JSONArray imgKeys = orderAttachment.getOssKey() == null || orderAttachment.getOssKey().isEmpty()
						? new JSONArray() : JSONArray.fromObject(orderAttachment.getOssKey());// 初始化JSON数组对象
				for (String key : OssKey) {
					imgKeys.add(key);
				}
				JSONArray fileName = orderAttachment.getFileName() == null || orderAttachment.getFileName().isEmpty()
						? new JSONArray() : JSONArray.fromObject(orderAttachment.getFileName());// 初始化JSON数组对象
				for (String name : dto.getFileName()) {
					fileName.add(name);
				}
				orderAttachment.setOssKey(imgKeys.toString());
				orderAttachment.setFileName(fileName.toString()); 
				dao.update(orderAttachment);
			}else{
				Set<String> OssKey = null;
				OssKey = appendOrderImage(order,files);
				OrderAttachment orderAttachment1 = new OrderAttachment();
				orderAttachment1.setOrderId(dto.getId());
				orderAttachment1.setOssKey(OssKey.toString());
				Set<String> fileNames = new HashSet<>();
				for (String name : dto.getFileName()) {
					String names = "\"" + name + "\"";
					fileNames.add(names);
				}
				orderAttachment1.setFileName(fileNames.toString()); 
				dao.save(orderAttachment1);
			}
		}*/
		Criteria criteria = dao.createCriteria(OrderAttachment.class);
		criteria.add(Restrictions.eq("orderId", dto.getId()));
		@SuppressWarnings("unchecked")
		List<OrderAttachment> orderAttachmentList = criteria.list();
		return new Result(orderAttachmentList);
	}



	private Set<String> appendOrderImage(Order order,MultipartFile[] file) {
		Set<String> addedImageNames = null;
		if (file == null) {// 如果没有上传任何图片文件
			return addedImageNames;
		}
		addedImageNames = new HashSet<>();// 初始化保存失败的上传文件名称列表
		for (MultipartFile imageFile : file) {// 遍历上传的图片文件
			try {
				String newKey = "xiuyu/order/"+getFileFolder(new Date())+"/"+order.getOrderCode()+"/" +UUID.randomUUID().toString().replace("-", "") +"."+ imageFile.getOriginalFilename();
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
	private String appendOrderImageTwo(Order order,MultipartFile file) {
		if (file == null) {// 如果没有上传任何图片文件
			return null;
		}
		try {
			String newKey = "xiuyu/order/"+getFileFolder(new Date())+"/"+order.getOrderCode()+"/" +UUID.randomUUID().toString().replace("-", "") +"."+ file.getOriginalFilename();
			return fileOSSUtil.uploadPrivateFile(newKey, file.getInputStream());
			// 把上传文件保存到OSS系统，并把OSS系统保存文件成功后返回的该文件的key添加到set集合中
		} catch (IOException e) {// 当读取上传图片文件输入流抛出异常时
			e.printStackTrace();
		}
		return null;
	}
	private Set<String> appendOrderImageTwo(Order order,MultipartFile[] file) {
		Set<String> addedImageNames = null;
		if (file == null) {// 如果没有上传任何图片文件
			return addedImageNames;
		}
		addedImageNames = new HashSet<>();// 初始化保存失败的上传文件名称列表
		for (MultipartFile imageFile : file) {// 遍历上传的图片文件
			try {
				String newKey = "xiuyu/order/"+getFileFolder(new Date())+"/"+order.getOrderCode()+"/" +UUID.randomUUID().toString().replace("-", "") +"."+ imageFile.getOriginalFilename();
				newKey = fileOSSUtil.uploadPrivateFile(newKey, imageFile.getInputStream());
//				String keys = "\"" + newKey + "\"";
				// 把上传文件保存到OSS系统，并把OSS系统保存文件成功后返回的该文件的key添加到JSON数组中
				addedImageNames.add(newKey);
			} catch (IOException e) {// 当读取上传图片文件输入流抛出异常时
				e.printStackTrace();
			}
		}
		return addedImageNames;// 返回保存失败的上传文件名称列表
	}

	/**
	 * 删除上传到oss的文件
	 * @param orderAttachments
	 * @param delFileList
	 * @return
	 */
	public String[] delOSSFile(List<OrderAttachment> orderAttachments, String[] delFileList) {
		if (delFileList != null) {
			for (String imgKey : delFileList) {
				/*String removeImgKey = fileOSSUtil.fromUrlToKey(imgKey);
				// 在oss 删除 文件
				fileOSSUtil.deletePrivateFile(removeImgKey);*/
				for (OrderAttachment orderAttachment : orderAttachments) {
					if (orderAttachment.getOssKey()!=null&&orderAttachment.getOssKey().contains(imgKey.trim())) {
						dao.delete(orderAttachment);
					}
				}
			}
			dao.flush();// 释放缓冲区
		}
		return delFileList;
	}
	/**
	 * 删除上传到oss的文件
	 * 
	 * @param dto
	 * @return
	 */
	public String[] delOSSFile(OrderAttachment orderAttachment, String[] delFileList,OrderDto dto) {
		if (delFileList != null) {
			JSONArray imgKeys = orderAttachment.getOssKey() == null || orderAttachment.getOssKey().isEmpty()
					? new JSONArray() : JSONArray.fromObject(orderAttachment.getOssKey());// 初始化JSON数组对象
			for (String imgKey : delFileList) {
				String removeImgKey = fileOSSUtil.fromUrlToKey(imgKey);
				imgKeys.remove(imgKey);
				// 在oss 删除 文件
				fileOSSUtil.deletePrivateFile(removeImgKey);
			}
			JSONArray fileName = orderAttachment.getFileName() == null || orderAttachment.getFileName().isEmpty()
					? new JSONArray() : JSONArray.fromObject(orderAttachment.getFileName());// 初始化JSON数组对象
			for (String name : dto.getDelFileName()) {
				fileName.remove(name);
			}
			orderAttachment.setOssKey(imgKeys.toString());
			orderAttachment.setFileName(fileName.toString());
			dao.update(orderAttachment);
			dao.flush();// 释放缓冲区
		}
		return delFileList;
	}
	
	private String getFileFolder(Date date) {
		return DateUtil.formatDate(date, "yyyy") + "/" + DateUtil.formatDate(date, "MM") + "/"
				+ DateUtil.formatDate(date, "dd");
	}
	
}
