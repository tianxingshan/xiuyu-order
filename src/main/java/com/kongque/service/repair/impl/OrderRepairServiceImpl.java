package com.kongque.service.repair.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kongque.constants.Constants;
import com.kongque.dao.IDaoService;
import com.kongque.dto.OrderRepairDto;
import com.kongque.entity.basics.Code;
import com.kongque.entity.basics.XiuyuShopDirectorRelation;
import com.kongque.entity.goods.Goods;
import com.kongque.entity.goods.GoodsDetail;
import com.kongque.entity.order.OrderDetail;
import com.kongque.entity.repair.OrderRepair;
import com.kongque.entity.repair.OrderRepairAttachment;
import com.kongque.entity.repair.OrderRepairCheck;
import com.kongque.entity.user.UserDeptRelation;
import com.kongque.entity.user.UserRoleRelation;
import com.kongque.model.OrderRepairModel;
import com.kongque.model.RepairExportExcleModel;
import com.kongque.service.repair.IOrderRepairService;
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
import com.kongque.util.StringUtils;

import net.sf.json.JSONArray;

@Service
public class OrderRepairServiceImpl implements IOrderRepairService {
	@Resource
	private IDaoService dao;
	@Resource
	private FileOSSUtil fileOSSUtil;

	/**
	 * 根据条件分页查询微调单
	 * 
	 * @param dto
	 * @param pageBean
	 * @return
	 */
	@Override
	public Pagination<OrderRepairModel> list(OrderRepairDto dto, PageBean pageBean) {
		Pagination<OrderRepairModel> pagination = new Pagination<>();
		if (pageBean.getPage() == null||pageBean.getRows() == null) {
			pageBean.setPage(1);
			pageBean.setRows(10);
		}
		StringBuffer sqlCount = new StringBuffer("select count(1) from ( SELECT DISTINCT orderrepair.c_id ");
		StringBuffer sql = new StringBuffer("select orderrepair.c_id,orderrepair.c_order_repair_code,"
				+ "orderrepair.c_customer_name,orderrepair.c_order_character, "
				+ "orderrepair.c_customer_code,  orderrepair.c_city,  orderrepair.c_shop_name, "
				+ "goods.c_name goods_name,  orderrepair.c_order_repair_status,orderrepair.c_repair_person, "
				+ "orderrepair.c_is_extract,orderrepair.c_check_time,"
				+ "max(case logistic.c_logistic_type when '0' then logistic.c_express_number else null end) 'send',	"
				+ "	max(case logistic.c_logistic_type when '1' then logistic.c_express_number else null end) 'reseve',	"
				+ "goods.c_code goods_code,  goodsDetail.c_color_name goods_color,  orderrepair.c_num, "
				+ "orderrepair.c_solution,  orderdetail.c_goods_sn 	,"
				+ "orderrepair.c_extended_file_name ,orderrepair.c_description,orderrepair.c_owner, "
				+ "max(logistic.c_send_time), max(logistic.c_delivery_time), orderrepair.c_create_time, orderrepair.c_order_code, orderrepair.c_ec_order_code ");
		String from = "from t_order_repair orderrepair 	" +
				" left join t_goods_detail goodsDetail ON orderrepair.c_goods_detail_id = goodsDetail.c_id " +
				" left join t_goods goods on goodsDetail.c_goods_id = goods.c_id "
				+ "left join t_logistic_order logisticorder on orderrepair.c_id=logisticorder.c_order_repair_id   		"
				+ "left join t_logistic logistic on logisticorder.c_logistic_id=logistic.c_id    		"
				+ "left join t_order_detail  orderdetail on orderrepair.c_order_detail_id = orderdetail.c_id where orderrepair.c_del = '0' ";
		sql.append(from);
		sqlCount.append(from);
		if(StringUtils.isNotBlank(dto.getUserId())){
			sql.append(" and orderrepair.c_shop_id  in (select c_shop_id from t_xiuyu_shop_director_relation where c_user_id='").append(dto.getUserId()).append("') ");
			sqlCount.append(" and orderrepair.c_shop_id  in (select c_shop_id from t_xiuyu_shop_director_relation where c_user_id='").append(dto.getUserId()).append("') ");
		}
		if (StringUtils.isNotBlank(dto.getOrderRepairCode())) {
			sql.append(" and orderrepair.c_order_repair_code like '%" + dto.getOrderRepairCode() + "%' ");
			sqlCount.append(" and orderrepair.c_order_repair_code like '%" + dto.getOrderRepairCode() + "%' ");
		}
		if (StringUtils.isNotBlank(dto.getCustomerInfo())) {
			sql.append(" and ((orderrepair.c_customer_name like '%" + dto.getCustomerInfo()
					+ "%')  or  (orderrepair.c_customer_code like '%" + dto.getCustomerInfo() + "%')) ");
			sqlCount.append(" and ((orderrepair.c_customer_name like '%" + dto.getCustomerInfo()
					+ "%')  or  (orderrepair.c_customer_code like '%" + dto.getCustomerInfo() + "%')) ");
		}
		if (StringUtils.isNotBlank(dto.getCity())) {
			sql.append(" and orderrepair.c_city  like '%" + dto.getCity() + "%' ");
			sqlCount.append(" and orderrepair.c_city  like '%" + dto.getCity() + "%' ");
		}
		if (StringUtils.isNotBlank(dto.getShopId())) {
			sql.append(" and orderrepair.c_shop_id ='" + dto.getShopId() + "' ");
			sqlCount.append(" and orderrepair.c_shop_id ='" + dto.getShopId() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getShopName())) {
			sql.append(" and orderrepair.c_shop_name like '%" + dto.getShopName() + "%' ");
			sqlCount.append(" and orderrepair.c_shop_name like '%" + dto.getShopName() + "%' ");
		}
		if (StringUtils.isNotBlank(dto.getGoodsName())) {
			sql.append(" and goods.c_name  like '%" + dto.getGoodsName() + "%' ");
			sqlCount.append(" and goods.c_name  like '%" + dto.getGoodsName() + "%' ");
		}
		if (StringUtils.isNotBlank(dto.getOrderCharacter()) && !"全部".equals(dto.getOrderCharacter())) {
			sql.append(" and orderrepair.c_order_character ='" + dto.getOrderCharacter() + "' ");
			sqlCount.append(" and orderrepair.c_order_character ='" + dto.getOrderCharacter() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getOrderRepairStatus())) {
			sql.append(" and orderrepair.c_order_repair_status ='" + dto.getOrderRepairStatus() + "' ");
			sqlCount.append(" and orderrepair.c_order_repair_status ='" + dto.getOrderRepairStatus() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getIsExtract())) {
			sql.append(" and orderrepair.c_is_extract ='" + dto.getIsExtract() + "' ");
			sqlCount.append(" and orderrepair.c_is_extract ='" + dto.getIsExtract() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getRepairPerson())) {
			sql.append(" and orderrepair.c_repair_person  like '%" + dto.getRepairPerson() + "%' ");
			sqlCount.append(" and orderrepair.c_repair_person  like '%" + dto.getRepairPerson() + "%' ");
		}
		if (StringUtils.isNotBlank(dto.getExpressNumber())) {
			sql.append(" and logistic.c_express_number ='" + dto.getExpressNumber() + "' ");
			sqlCount.append(" and logistic.c_express_number ='" + dto.getExpressNumber() + "' ");
		}
		// 审核时间
		if (dto.getStartCheckTime() != null && !dto.getStartCheckTime().isEmpty() && dto.getEndCheckTime() != null
				&& !dto.getEndCheckTime().isEmpty()) {// 添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and orderrepair.c_check_time between '").append(dto.getStartCheckTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getEndCheckTime()).append(" 23:59:59'");
			sqlCount.append(" and orderrepair.c_check_time between '").append(dto.getStartCheckTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getEndCheckTime()).append(" 23:59:59'");
		} else if (dto.getStartCheckTime() != null && !dto.getStartCheckTime().isEmpty()
				&& (dto.getEndCheckTime() == null || dto.getEndCheckTime().isEmpty())) {// 添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and orderrepair.c_check_time >= '").append(dto.getStartCheckTime()).append(" 00:00:00'");
			sqlCount.append(" and orderrepair.c_check_time >= '").append(dto.getStartCheckTime()).append(" 00:00:00'");
		} else if ((dto.getStartCheckTime() == null || dto.getStartCheckTime().isEmpty())
				&& dto.getEndCheckTime() != null && !dto.getEndCheckTime().isEmpty()) {// 添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and orderrepair.c_check_time  <= '").append(dto.getEndCheckTime()).append(" 00:00:00'");
			sqlCount.append(" and orderrepair.c_check_time  <= '").append(dto.getEndCheckTime()).append(" 00:00:00'");
		}
		sql.append("  group by orderrepair.c_id  order by orderrepair.c_order_repair_code desc");
		sqlCount.append("  ) tt ");
		List<Object> resultList = dao.queryBySql(sqlCount.toString());
		pagination.setTotal(resultList == null || resultList.isEmpty() ? 0L : Long.parseLong(resultList.get(0).toString()));
		if (pageBean.getPage() != null && pageBean.getRows() != null) {
			sql.append("  limit " + (pageBean.getPage() - 1) * pageBean.getRows() + "," + pageBean.getRows());
		}
		List<OrderRepairModel> lrbm = new ArrayList<>();
		List<Object> resultSet = dao.queryBySql(sql.toString());
		if (resultSet != null && resultSet.size() > 0) {
			for (Object result : resultSet) {
				OrderRepairModel rbm = new OrderRepairModel();
				Object[] properties = (Object[]) result;
				rbm.setId(properties[0] == null ? "" : properties[0].toString());
				rbm.setOrderRepairCode(properties[1] == null ? "" : properties[1].toString());
				rbm.setCustomerName(properties[2] == null ? "" : properties[2].toString());
				rbm.setOrderCharacter(properties[3] == null ? "" : properties[3].toString());
				rbm.setCustomerCode(properties[4] == null ? "" : properties[4].toString());
				rbm.setCity(properties[5] == null ? "" : properties[5].toString());
				rbm.setShopName(properties[6] == null ? "" : properties[6].toString());
				rbm.setGoodsName(properties[7] == null ? "" : properties[7].toString());
				rbm.setOrderRepairStatus(properties[8] == null ? "" : properties[8].toString());
				rbm.setRepairPerson(properties[9] == null ? "" : properties[9].toString());
				rbm.setIsExtract(properties[10] == null ? "" : properties[10].toString());
				rbm.setCheckTime(properties[11] == null ? "" : properties[11].toString());
				rbm.setSendExpressNumber(properties[12] == null ? "" : properties[12].toString());
				rbm.setReceiveExpressNumber(properties[13] == null ? "" : properties[13].toString());
				rbm.setGoodsCode(properties[14] == null ? "" : properties[14].toString());
				rbm.setGoodsColor(properties[15] == null ? "" : properties[15].toString());
				rbm.setNum(properties[16] == null ? "" : properties[16].toString());
				rbm.setSolution(properties[17] == null ? "" : properties[17].toString());
				rbm.setGoodsSn(properties[18] == null ? "" : properties[18].toString());
				rbm.setExtendedFileName(properties[19] == null ? "" : properties[19].toString());
				rbm.setDescription(properties[20] == null ? "" : properties[20].toString());
				rbm.setOwner(properties[21] == null ? "" : properties[21].toString());

				rbm.setSendTime(properties[22] == null ? "" : properties[22].toString());
				rbm.setDeliveryTime(properties[23] == null ? "" : properties[23].toString());
				rbm.setCreateTime(properties[24] == null ? "" : properties[24].toString());
				rbm.setOrderCode(properties[25] == null ? "" : properties[25].toString());
				rbm.setEcOrderCode(properties[26] == null ? "" : properties[26].toString());
				lrbm.add(rbm);
			}
		}
		pagination.setRows(lrbm);
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

	/**
	 * 增加或修改微调单
	 * 
	 * @param dto
	 * @param files
	 * @return
	 */
	@Override
	public Result saveOrUpdate(OrderRepairDto dto, MultipartFile[] files) {
		//数据校验
		if(StringUtils.isBlank(dto.getCustomerId()) || "undefined".equals(dto.getCustomerId())){
			return new Result("500","请选择会员！");
		}
		if(dto.getCustomerName().length()==32){
			return new Result("500","会员名称错误！");
		}
		if(dto.getGoodsCode().length()==32){
			return new Result("500","款式码错误！");
		}
		if(StringUtils.isBlank(dto.getGoodsColor())){
			return new Result("500","请选择颜色！");
		}
		if(StringUtils.isNotBlank(dto.getEcOrderCode())){
			if(org.apache.commons.lang3.StringUtils.isBlank(dto.getGoodsDetailId()) || "undefined".equals(dto.getGoodsDetailId())){
				return new Result("500","请选择款式！");
			}
		}
		if (StringUtils.isBlank(dto.getGoodsDetailId())){
			return new Result("500","该商品id不能为空！");
		}
		if(StringUtils.isBlank(dto.getOrderCharacter())){
			return new Result("500","订单性质不能为空!");
		}
		//保存
		if (StringUtils.isBlank(dto.getId())) {
			OrderRepair repair = new OrderRepair();
			BeanUtils.copyProperties(dto, repair);
			repair.setOrderRepairCode(CodeUtil.createOrderRepairCode(getOrderRepairMaxValue()));
			repair.setSysId(Constants.SYS_ID);
			repair.setDel("0");
			repair.setFinanceCheckStatus("0"); 
			repair.setCreateTime(new Date());
			dao.save(repair);
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("repair", repair);
			//saveOrderRepairImg(repair, files);
			return new Result(map);
		} else {
			//修改
			OrderRepair repair = new OrderRepair();
			BeanUtils.copyProperties(dto, repair);
			repair.setSysId(Constants.SYS_ID);
			repair.setUpdateTime(new Date());
			dao.update(repair);
			//updateOrderRepairImg(repair, dto, files);
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("repair", repair);
			// 图片
			Criteria criteria = dao.createCriteria(OrderRepairAttachment.class);
			criteria.add(Restrictions.eq("orderRepairId", repair.getId()));
			@SuppressWarnings("unchecked")
			List<OrderRepairAttachment> list = criteria.list();
			if(list!=null){
				map.put("list", list);
			}
			return new Result(map);
		}

	}

	/**
	 * 根据id查询微调单
	 * 
	 * @param id
	 * 
	 * @return
	 */
	@Override
	public Result RepairById(String id) {
		OrderRepair repair = dao.findById(OrderRepair.class, id);
		Map<String, Object> map = new HashMap<String, Object>();
		if (repair != null) {
			OrderDetail detail = dao.findById(OrderDetail.class, repair.getOrderDetailId());
			if(detail!=null){
				GoodsDetail g = dao.findById(GoodsDetail.class, detail.getGoodsDetailId());
				if (g!=null){
					Goods goods = dao.findById(Goods.class, g.getGoodsId());
					repair.setCategoryId(goods.getCategory().getId());
					repair.setCategoryName(goods.getCategory().getName());
					repair.setGoodsName(goods.getName());
					repair.setGoodsSn(detail.getGoodsSn());
				}
				map.put("repair", repair);
			}else{
				GoodsDetail g = dao.findById(GoodsDetail.class, repair.getGoodsDetailId());
				if (g!=null){
					Goods goods = dao.findById(Goods.class, g.getGoodsId());
					repair.setCategoryId(goods.getCategory().getId());
					repair.setCategoryName(goods.getCategory().getName());
					repair.setGoodsName(goods.getName());
				}
				map.put("repair", repair);
			}
			List<OrderRepair> list = getHistory(repair);
			if (list != null) {
				map.put("history", list);
			}
			return new Result(map);
		} else {
			return new Result(Constants.RESULT_CODE.SYS_ERROR, "没有此微调单！");
		}
	}

	private String getOrderRepairMaxValue() {
		Date date = new Date();
		Criteria criteria = dao.createCriteria(Code.class);
		criteria.add(Restrictions.between("updateTime", DateUtil.minDate(date), DateUtil.maxDate(date)));
		criteria.add(Restrictions.eq("type", "FX"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("maxValue"));
		criteria.setMaxResults(1);
		Code code = (Code) criteria.uniqueResult();
		if (code == null) {
			code = new Code();
			code.setMaxValue(1);
			code.setType("FX");
			code.setUpdateTime(date);
			dao.save(code);
		} else {
			code.setMaxValue(code.getMaxValue() + 1);
		}
		return String.format("%0" + 6 + "d", code.getMaxValue());
	}

	/**
	 * 审核微调单
	 * 
	 * @param dto
	 * 
	 * @return
	 */
	@Override
	public Result checkRepair(OrderRepairCheck dto) {
		OrderRepairCheck check = new OrderRepairCheck();
		if (StringUtils.isNotBlank(dto.getCheckerName())) {
			check.setCheckerName(dto.getCheckerName());
		}
		if (StringUtils.isNotBlank(dto.getCheckStatus())) {
			check.setCheckStatus(dto.getCheckStatus());
		}
		if (StringUtils.isNotBlank(dto.getOrderRepairId())) {
			check.setOrderRepairId(dto.getOrderRepairId());
		}
		check.setCheckTime(new Date());

		if (StringUtils.isNotBlank(dto.getCheckInstruction())) {
			check.setCheckInstruction(dto.getCheckInstruction());
		}

		switch (dto.getCheckStatus()) {
		case "1":
			changeStatus(dto.getOrderRepairId(), "3");

			break;

		case "2":
			changeStatus(dto.getOrderRepairId(), "2");
			break;
		}
		OrderRepair orderRepair = dao.findById(OrderRepair.class, dto.getOrderRepairId());
		if (null!=orderRepair){
			orderRepair.setCheckTime(new Date());
			dao.update(orderRepair);
		}
		dao.save(check);

		return new Result(check);

	}

	// 查询微调历史记录
	@Override
	public List<OrderRepair> getHistory(OrderRepair repair) {

		Criteria criteria = dao.createCriteria(OrderRepair.class);
		if (StringUtils.isNotBlank(repair.getOrderRepairCode())) {
			criteria.add(Restrictions.eq("goodsDetailId", repair.getGoodsDetailId()));
			return criteria.list();
		}
		/*if (StringUtils.isNotBlank(repair.getEcOrderCode())) {
			criteria.add(Restrictions.eq("ecOrderCode", repair.getEcOrderCode()));
		}*/
		return new ArrayList<>();
	}
	// // 修改微调单状态

	@Override
	public Result changeStatus(String id, String status) {
		OrderRepair orderRepair = dao.findById(OrderRepair.class, id);
		if (orderRepair != null) {
			switch (status) {
			case "0":
				orderRepair.setOrderRepairStatus("0");
				break;

			case "1":
				orderRepair.setOrderRepairStatus("1");
				break;

			case "2":
				orderRepair.setOrderRepairStatus("2");
				break;

			case "3":
				orderRepair.setOrderRepairStatus("3");
				break;
			case "4":
				orderRepair.setOrderRepairStatus("4");
				break;
			case "5":
				orderRepair.setOrderRepairStatus("5");
				break;
			case "6":
				orderRepair.setOrderRepairStatus("6");
				break;
			case "7":
				orderRepair.setOrderRepairStatus("7");
				break;
			case "8":
				orderRepair.setOrderRepairStatus("8");
				break;
			}
			dao.update(orderRepair);
		}

		return new Result(orderRepair);
	}

	@Override
	public Result remoteOrderRepair(String q) {
		Criteria criteria = dao.createCriteria(OrderRepair.class);
		if (StringUtils.isNotBlank(q)) {
			criteria.add(Restrictions.like("orderRepairCode", q, MatchMode.ANYWHERE));
		}
		@SuppressWarnings("unchecked")
		List<OrderRepair> list = criteria.list();
		return new Result(list);
	}

	@Override
	public Result del(String id) {
		if (StringUtils.isNotBlank(id)) {
			OrderRepair repair = dao.findById(OrderRepair.class, id);
//			List<OrderRepairAttachment> attachments = repair.getAttachments();
//			if (attachments != null && attachments.size() > 0) {
//				for (OrderRepairAttachment orderRepairAttachment : attachments) {
//					dao.delete(orderRepairAttachment);
//				}
//			}
//			List<OrderRepairCheck> checks = repair.getChecks();
//			if (checks != null && checks.size() > 0) {
//				for (OrderRepairCheck orderRepairCheck : checks) {
//					dao.delete(orderRepairCheck);
//				}
//			}
			repair.setDel("1");
			dao.update(repair);
			return new Result(repair);
		} else {
			return new Result(Constants.RESULT_CODE.SYS_ERROR, "没有此微调单！");
		}
	}
	
	private String convertShopIds(List<String> list){
	    if(ListUtils.isEmptyOrNull(list)){
	    	return "''";
	    }
	    StringBuilder ids = new StringBuilder();
	    for(String id : list){
	    	ids.append("'").append(id).append("',");
	    }
	    ids.deleteCharAt(ids.length()-1);
	    return ids.toString();
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
	public Result uploadFile(OrderRepairDto dto, MultipartFile[] files){
		OrderRepair repair = dao.findById(OrderRepair.class, dto.getId());
		String imageDelKeys = dto.getImageDelKeys();
		JSONArray delkeys = null;
		if (StringUtils.isNotBlank(imageDelKeys)) {
			delkeys = JSONArray.fromObject(imageDelKeys);
		}
		// 查询出此微调单对应的OrderRepairAttachment
		Criteria criteria = dao.createCriteria(OrderRepairAttachment.class);
		@SuppressWarnings("unchecked")
		List<OrderRepairAttachment> attachments = criteria.add(Restrictions.eq("orderRepairId", repair.getId())).list();
		// 判断要删的图片路径OssKey数组是否为空，不为空删掉OssKey属性等于要删的图片路径OssKey
		if (delkeys != null && delkeys.size() > 0) {
			for (int i = 0; i < delkeys.size(); i++) {
				for (OrderRepairAttachment orderRepairAttachment : attachments) {
					if (delkeys.get(i).toString().equals(orderRepairAttachment.getOssKey())) {
						dao.delete(orderRepairAttachment);
					}
				}
			}
		}
		// 如果有新增，添加
		saveOrderRepairImg(repair, files);
		// Oss删除图片
		if (delkeys != null && delkeys.size() > 0) {
			for (int i = 0; i < delkeys.size(); i++) {
				delOSSFile(delkeys.get(i).toString());
			}

		}
		Criteria criteria1 = dao.createCriteria(OrderRepairAttachment.class);
		@SuppressWarnings("unchecked")
		List<OrderRepairAttachment> attachmentsList = criteria1.add(Restrictions.eq("orderRepairId", repair.getId())).list();
		return new Result(attachmentsList);
	}
	
	@Override
	public Result owner(String id,String owner){
		OrderRepair repair = dao.findById(OrderRepair.class, id);
		repair.setOwner(owner);
		dao.update(repair);
		return new Result(repair);
	}
	
	@Override
	public void exportExcle(OrderRepairDto dto, HttpServletRequest request, HttpServletResponse response){
		List<RepairExportExcleModel> odsModelList = queryOrderDetailWithParam(dto);
		String excelFileName = "微调明细表";
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
			String[] headers = new String[] {"微调单号","顾客姓名","款号","款名","颜色","店铺","版师","微调单状态","订单性质","订单生产工厂","责任归属","孔雀订单号","产品唯一码","EC订单号","订单审核日期","微调审核日期","微调生产日期","微调原因","实调内容","订单发货日期"};
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

	@Override
	public Result RepairNoHistoryById(String id) {
		OrderRepair repair = dao.findById(OrderRepair.class, id);
		Map<String, Object> map = new HashMap<String, Object>();
		if (repair != null) {
			OrderDetail detail = dao.findById(OrderDetail.class, repair.getOrderDetailId());
			if(detail!=null){
				GoodsDetail g = dao.findById(GoodsDetail.class, detail.getGoodsDetailId());
				if (g!=null){
					Goods goods = dao.findById(Goods.class, g.getGoodsId());
					repair.setCategoryId(goods.getCategory().getId());
					repair.setCategoryName(goods.getCategory().getName());
					repair.setGoodsName(goods.getName());
					repair.setGoodsSn(detail.getGoodsSn());
				}
				map.put("repair", repair);
			}else{
				GoodsDetail g = dao.findById(GoodsDetail.class, repair.getGoodsDetailId());
				if (g!=null){
					Goods goods = dao.findById(Goods.class, g.getGoodsId());
					repair.setCategoryId(goods.getCategory().getId());
					repair.setCategoryName(goods.getCategory().getName());
					repair.setGoodsName(goods.getName());
				}
				map.put("repair", repair);
			}
			return new Result(map);
		} else {
			return new Result(Constants.RESULT_CODE.SYS_ERROR, "没有此微调单！");
		}
	}

	public List<RepairExportExcleModel> queryOrderDetailWithParam(OrderRepairDto dto){
		List<RepairExportExcleModel> odsModelList = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT a.c_order_repair_code orderRepairCode,a.c_customer_name customerName,goods.c_code goodsCode,goods.c_name goodsName,goodsDetail.c_color_name goodsColor,a.c_shop_name shopName,g.c_technician_name technicianName,a.c_order_repair_status orderRepairStatus,c.c_order_character orderCharacter,i.c_prod_factory_id prodFactory,a.c_owner owner,c.c_order_code orderCode,b.c_goods_sn goodsSn,a.c_ec_order_code EcCode,c.c_business_checker_time businessCheckerTime,a.c_check_time repairCheckerTime, f.c_plan_date planDate,a.c_repair_reason repairReason,e.c_repair_feedback repairFeedback,ss.send_time sendTime "
			+" FROM t_order_repair a " +
				" LEFT JOIN t_goodsDetail goodsDetail on a.c_goods_detail_id = goodsDetail.c_id " +
				" LEFT JOIN t_goods goods ON goodsDetail.c_goods_id = goods.c_id "
			+" left join t_order_detail b on a.c_order_detail_id=b.c_id "
			+" left join t_order c on b.c_order_id=c.c_id "
			+" left join mes_order_plan_detail d on a.c_order_detail_id=d.c_order_detail_id "
			+" left join mes_repair_production_plan_detail e on a.c_id=e.c_order_repair_id "
			+" left join mes_repair_production_plan f on e.c_repair_plan_id = f.c_id "
			+" left join mes_order_detail_assign g on a.c_order_detail_id=g.c_order_detail_id "
			+" left join mes_order_plan_detail h on a.c_order_detail_id=h.c_order_detail_id "
			+" left join mes_order_plan i on h.c_order_plan_id=i.c_id "
//			+" left join (SELECT MAX(c_check_time) c_check_time,c_order_repair_id FROM t_order_repair_check  GROUP BY c_order_repair_id) k on a.c_id = k.c_order_repair_id "
			+" LEFT JOIN (SELECT orderrepair.c_id, MAX(CASE logistic.c_logistic_type WHEN '0' THEN logistic.c_send_time ELSE NULL END) 'send_time', MAX(CASE logistic.c_logistic_type WHEN '1' THEN logistic.c_delivery_time ELSE NULL END) 'express_time' "
			+" FROM t_order_repair orderrepair  "
			+" LEFT JOIN t_logistic_order logisticorder ON orderrepair.c_id = logisticorder.c_order_repair_id "
			+" LEFT JOIN t_logistic logistic ON logisticorder.c_logistic_id = logistic.c_id "
			+" WHERE 1 = 1 AND logistic.c_delete_flag = '0' "
			+" GROUP BY orderrepair.c_id) ss on ss.c_id = a.c_id "
			+" where a.c_del='0' ");
		if(StringUtils.isNotBlank(dto.getUserId())){
//			UserRoleRelation user = dao.findUniqueByProperty(UserRoleRelation.class, "userId", dto.getUserId());
			@SuppressWarnings("unchecked")
			List<UserRoleRelation> users = dao.createCriteria(UserRoleRelation.class)
					.add(Restrictions.eq( "userId", dto.getUserId())).list();
			UserDeptRelation dept = dao.findUniqueByProperty(UserDeptRelation.class, "userId", dto.getUserId());
			for (UserRoleRelation userRoleRelation : users) {
				if (Constants.XIUYU_ROLE_ID.equalsIgnoreCase(userRoleRelation.getRole().getId())){
					sql.append(" and a.c_shop_id in (").append(convertShopIds(getShopList(userRoleRelation))).append(")");
					break;
				} else if (Constants.DIANYUAN_ROLE_ID.equalsIgnoreCase(userRoleRelation.getRole().getId())
						||Constants.JIAMENG_ROLE_ID.equalsIgnoreCase(userRoleRelation.getRole().getId())){
					sql.append(" and a.c_shop_id = '" + dept.getDeptId() + "' ");
					break;
				}
			}
			/*switch (user.getRole().getId()) {
			case Constants.DIANYUAN_ROLE_ID:
				// 店员
				sql.append(" and a.c_shop_id ='" + dept.getDeptId() + "' ");
				break;
			case Constants.XIUYU_ROLE_ID:
				// 秀域审核主管
				sql.append(" and a.c_shop_id in (").append(convertShopIds(getShopList(user))).append(")");
				break;
			case Constants.JIAMENG_ROLE_ID:
				// 加盟店
				sql.append(" and a.c_shop_id = '" + dept.getDeptId() + "' ");
			default:
				break;
			}*/
		}
		if (StringUtils.isNotBlank(dto.getOrderRepairCode())) {
			sql.append(" and a.c_order_repair_code ='" + dto.getOrderRepairCode() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getCustomerInfo())) {
			sql.append(" and ((a.c_customer_name like '%" + dto.getCustomerInfo()
					+ "%')  or  (a.c_customer_code like '%" + dto.getCustomerInfo() + "%')) ");
		}
		if (StringUtils.isNotBlank(dto.getCity())) {
			sql.append(" and a.c_city  like '%" + dto.getCity() + "%' ");
		}
		if (StringUtils.isNotBlank(dto.getShopId())) {
			sql.append(" and a.c_shop_id ='" + dto.getShopId() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getShopName())) {
			sql.append(" and a.c_shop_name like '%" + dto.getShopName() + "%' ");
		}
		if (StringUtils.isNotBlank(dto.getGoodsName())) {
			sql.append(" and goods.c_name  like '%" + dto.getGoodsName() + "%' ");
		}
		if (StringUtils.isNotBlank(dto.getOrderCharacter())&&!"全部".equals(dto.getOrderCharacter().trim())) {
			sql.append(" and a.c_order_character ='" + dto.getOrderCharacter() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getOrderRepairStatus())) {
			sql.append(" and a.c_order_repair_status ='" + dto.getOrderRepairStatus() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getIsExtract())) {
			sql.append(" and a.c_is_extract ='" + dto.getIsExtract() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getRepairPerson())) {
			sql.append(" and a.c_repair_person  like '%" + dto.getRepairPerson() + "%' ");
		}
		if (StringUtils.isNotBlank(dto.getExpressNumber())) {
			sql.append(" and m.c_express_number ='" + dto.getExpressNumber() + "' ");
		}
		// 审核时间
		if (dto.getStartCheckTime() != null && !dto.getStartCheckTime().isEmpty() && dto.getEndCheckTime() != null
				&& !dto.getEndCheckTime().isEmpty()) {// 添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and a.c_check_time between '").append(dto.getStartCheckTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getEndCheckTime()).append(" 23:59:59'");
		} else if (dto.getStartCheckTime() != null && !dto.getStartCheckTime().isEmpty()
				&& (dto.getEndCheckTime() == null || dto.getEndCheckTime().isEmpty())) {// 添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and a.c_check_time between '").append(dto.getStartCheckTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getStartCheckTime()).append(" 23:59:59'");
		} else if ((dto.getStartCheckTime() == null || dto.getStartCheckTime().isEmpty())
				&& dto.getEndCheckTime() != null && !dto.getEndCheckTime().isEmpty()) {// 添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and a.c_check_time  between '").append(dto.getEndCheckTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getEndCheckTime()).append(" 23:59:59'");
		}
		sql.append(" order by a.c_order_repair_code desc");
		List resultSet = dao.queryBySql(sql.toString());		
		for(Object result : resultSet){
			RepairExportExcleModel odsModel = new RepairExportExcleModel();//构建返回数据模型
			Object[] properties = (Object[])result;
			odsModel.setOrderRepairCode(properties[0]==null ? "" : properties[0].toString());
			odsModel.setCustomerName(properties[1]==null ? "" : properties[1].toString());
			odsModel.setGoodsCode(properties[2]==null ? "" : properties[2].toString());
			odsModel.setGoodsName(properties[3]==null ? "" : properties[3].toString());
			odsModel.setGoodsColor(properties[4]==null ? "" : properties[4].toString());
			odsModel.setShopName(properties[5]==null ? "" : properties[5].toString());
			odsModel.setTechnicianName(properties[6]==null ? "" : properties[6].toString());
			odsModel.setOrderRepairStatus(changestatus(properties[7]==null ? "" : properties[7].toString()));
			odsModel.setOrderCharacter(properties[8]==null ? "" : properties[8].toString());
			odsModel.setProdFactory(properties[9]==null ? "" : properties[9].toString());
			odsModel.setOwner(properties[10]==null ? "" : properties[10].toString());
			odsModel.setOrderCode(properties[11]==null ? "" : properties[11].toString());
			odsModel.setGoodsSn(properties[12]==null ? "" : properties[12].toString());
			odsModel.setEcCode(properties[13]==null ? "" : properties[13].toString());
			odsModel.setBusinessCheckerTime(properties[14]==null ? "" : properties[14].toString());
			odsModel.setRepairCheckerTime(properties[15]==null ? "" : properties[15].toString());
			odsModel.setPlanDate(properties[16]==null ? "" : properties[16].toString());
			odsModel.setRepairReason(properties[17]==null ? "" : properties[17].toString());
			odsModel.setRepairFeedback(properties[18]==null ? "" : properties[18].toString());
			odsModel.setSendTime(properties[19]==null ? "" : properties[19].toString());
			odsModelList.add(odsModel);
		}
		return 	odsModelList;
	}
	
	private String changestatus(String a){
		String status = "";
		if("0".equals(a)){
			status = "未送出";
		}else if("1".equals(a)){
			status = "已送出";
		}else if("2".equals(a)){
			status = "星域审核通过";
		}else if("3".equals(a)){
			status = "星域驳回";
		}else if("4".equals(a)){
			status = "计划维护";
		}else if("5".equals(a)){
			status = "生产中";
		}else if("6".equals(a)){
			status = "生产完成";
		}else if("7".equals(a)){
			status = "已发货";
		}else if("8".equals(a)){
			status = "已收货";
		}
		return status;
	}
	
	private void saveOrderRepairImg(OrderRepair repair, MultipartFile[] files) {
		if (files != null) {
			String key = null;
			for (MultipartFile multipartFile : files) {
				OrderRepairAttachment attachment = new OrderRepairAttachment();
				attachment.setOrderRepairId(repair.getId());
				key = appendOrderImage(repair,multipartFile);
				if (key != null) {
					attachment.setOssKey(key);
				}
				attachment.setFileName(multipartFile.getOriginalFilename());
				dao.save(attachment);

			}
		}
	}

	
	private String appendOrderImage(OrderRepair repair,MultipartFile file) {
		String key = null;
		if (file == null) {// 如果没有上传任何图片文件
			return key;
		}
		try {
			String newKey = "xiuyu/repair/"+ getFileFolder(new Date()) +"/"+repair.getOrderRepairCode()+"/" +UUID.randomUUID().toString().replace("-", "") +"."+ file.getOriginalFilename();
			key = fileOSSUtil.uploadPrivateFile(newKey, file.getInputStream());
			// 把上传文件保存到OSS系统
		} catch (IOException e) {// 当读取上传图片文件输入流抛出异常时
			e.printStackTrace();
		}
		return key;// 返回上传文件路径
	}

	private String getFileFolder(Date date) {

		return DateUtil.formatDate(date, "yyyy") + "/" + DateUtil.formatDate(date, "MM") + "/"
				+ DateUtil.formatDate(date, "dd") ;
	}

	/**
	 * 删除上传到oss的文件
	 * 
	 * @param delKey
	 * @return
	 */
	public void delOSSFile(String delKey) {
		if (delKey != null) {
			String removeImgKey = fileOSSUtil.fromUrlToKey(delKey);
			// 在oss 删除 文件
			fileOSSUtil.deletePrivateFile(removeImgKey);
		}

	}
	
}
