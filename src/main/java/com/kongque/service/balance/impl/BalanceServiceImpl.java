package com.kongque.service.balance.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
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
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.kongque.constants.Constants;
import com.kongque.dao.IDaoService;
import com.kongque.dto.BalanceDto;
import com.kongque.dto.OrderRepairDto;
import com.kongque.entity.balance.Balance;
import com.kongque.entity.balance.BalanceRepairRelation;
import com.kongque.entity.basics.Code;
import com.kongque.model.BalanceModel;
import com.kongque.model.OrderRepairModel;
import com.kongque.service.balance.IBalanceService;
import com.kongque.util.CodeUtil;
import com.kongque.util.DateUtil;
import com.kongque.util.ExportExcelUtil;
import com.kongque.util.JsonUtil;
import com.kongque.util.PageBean;
import com.kongque.util.Pagination;
import com.kongque.util.Result;
import com.kongque.util.StringUtils;

import net.sf.json.JSONObject;

@Service
public class BalanceServiceImpl implements IBalanceService {
	@Resource
	private IDaoService dao;

	// 根据条件分页查询结算单列表
	@Override
	public Pagination<BalanceModel> list(BalanceDto dto, PageBean pageBean) {
		if (pageBean.getPage() == null) {
			pageBean.setPage(1);
		}
		if (pageBean.getRows() == null) {
			pageBean.setRows(9999);
		}
		List<BalanceModel> lrbm = new ArrayList<>();
		Pagination<BalanceModel> pagination = new Pagination<>();
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT balance.c_id,balance.c_balance_numeber,balance.c_city,balance.c_create_time,balance.c_company_id ,balance.c_balance_status,company.c_unit_name 	 "
						+ "from t_balance balance "
						+ "left join t_xiuyu_company company on balance.c_company_id=company.c_id where 1=1 ");

		/**
		 * 城市
		 */
		if (dto.getCity() != null && !dto.getCity().isEmpty()) {
			sql.append("  and balance.c_city like '%").append(dto.getCity()).append("%'");
		}
		/**
		 * 分公司查询
		 */
		if (dto.getCompanyId() != null && !dto.getCompanyId().isEmpty()) {
			sql.append(" and balance.c_company_id =  '").append(dto.getCompanyId()).append("' ");
		}
		/**
		 * 结算时间
		 */
		if (dto.getBalanceStartTime() != null && !dto.getBalanceStartTime().isEmpty() && dto.getBalanceEndTime() != null
				&& !dto.getBalanceEndTime().isEmpty()) {// 添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and balance.c_create_time between '").append(dto.getBalanceStartTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getBalanceEndTime()).append(" 23:59:59'");
		} else if (dto.getBalanceStartTime() != null && !dto.getBalanceStartTime().isEmpty()
				&& (dto.getBalanceEndTime() == null || dto.getBalanceEndTime().isEmpty())) {// 添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and balance.c_create_time between '").append(dto.getBalanceStartTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getBalanceStartTime()).append(" 23:59:59'");
		} else if ((dto.getBalanceStartTime() == null || dto.getBalanceStartTime().isEmpty())
				&& dto.getBalanceEndTime() != null && !dto.getBalanceEndTime().isEmpty()) {// 添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and balance.c_create_time  between '").append(dto.getBalanceEndTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getBalanceEndTime()).append(" 23:59:59'");
		}

		/**
		 * 结算单号
		 */
		if (dto.getBalanceCode() != null && !dto.getBalanceCode().isEmpty()) {
			sql.append(" and balance.c_balance_numeber like '%").append(dto.getBalanceCode()).append("%'");
		}
		/**
		 * 结算单id
		 */
		if (dto.getBalanceId() != null && !dto.getBalanceId().isEmpty()) {

			sql.append(" and balance.c_id = '").append(dto.getBalanceId()).append("'");
		}

		/**
		 * 结算凭证结算状态
		 */
		if (dto.getBalanceStatus() != null && !dto.getBalanceStatus().isEmpty()) {
			sql.append(" and balance.c_balance_status = '").append(dto.getBalanceStatus()).append("'");
		}
		int total = dao.queryBySql(sql.toString()).size();
		pagination.setTotal(total);
		if (pageBean.getPage() != null && pageBean.getRows() != null) {
			sql.append(" limit " + (pageBean.getPage() - 1) * pageBean.getRows() + "," + pageBean.getRows());
		}

		List resultSet = dao.queryBySql(sql.toString());
		if (resultSet != null && resultSet.size() > 0) {
			for (Object result : resultSet) {
				BalanceModel rbm = new BalanceModel();
				Object[] properties = (Object[]) result;
				rbm.setBalanceId(properties[0] == null ? "" : properties[0].toString());
				rbm.setBalanceNumeber(properties[1] == null ? "" : properties[1].toString());
				rbm.setCity(properties[2] == null ? "" : properties[2].toString());
				rbm.setBalanceTime(formatDate(properties[3] == null ? "" : properties[3].toString()));
				rbm.setCompanyId(properties[4] == null ? "" : properties[4].toString());
				rbm.setBalanceStatus(properties[5] == null ? "" : properties[5].toString());
				rbm.setUnitName(properties[6] == null ? "" : properties[6].toString());
				lrbm.add(rbm);
			}
		}
		pagination.setRows(lrbm);

		return pagination;

	}

	// 新增或修改结算单
	@Override
	public Result saveOrUpdate(Balance dto) {

		if (StringUtils.isBlank(dto.getId())) {
			Balance balance = new Balance();
			BeanUtils.copyProperties(dto, balance);
			balance.setBalanceNumeber(CodeUtil.createBalanceCode(getBalanceMaxValue()));
			balance.setCreateTime(new Date());
			dao.save(balance);
			saveBalance(balance, dto);
		} else {
			Balance balance = dao.findById(Balance.class, dto.getId());

			if (balance != null) {
				BeanUtils.copyProperties(dto, balance);
				balance.setUpdateTime(new Date());
				if (StringUtils.isNotBlank(dto.getBalanceStatus()) && "3".equals(dto.getBalanceStatus())) {
					balance.setBalanceTime(new Date());
				}
				updateBalance(balance, dto);
				dao.update(balance);
			} else {
				return new Result(Constants.RESULT_CODE.SYS_ERROR, "没有此结算单！");
			}
		}
		return new Result("200", "操作成功！");
	}

	// 新增结算单_orderRapair
	public void saveBalance(Balance balance, Balance dto) {
		if (dto.getBalanceRepairRelations() != null && dto.getBalanceRepairRelations().size() > 0) {
			for (int i = 0; i < dto.getBalanceRepairRelations().size(); i++) {
				BalanceRepairRelation repairRelation = new BalanceRepairRelation();
				repairRelation.setBalanceId(balance.getId());
				repairRelation.setLineOrderCode(dto.getBalanceRepairRelations().get(i).getLineOrderCode());
				repairRelation.setRepairId(dto.getBalanceRepairRelations().get(i).getRepairId());
				repairRelation.setMatterCode(dto.getBalanceRepairRelations().get(i).getMatterCode());
				repairRelation.setUnitPrice(dto.getBalanceRepairRelations().get(i).getUnitPrice());
				repairRelation.setAmount(dto.getBalanceRepairRelations().get(i).getAmount());
				dao.save(repairRelation);
			}
		}

	}

	// 修改结算单-orderRapair
	public void updateBalance(Balance balance, Balance dto) {
		if (dto.getBalanceRepairRelations() != null && dto.getBalanceRepairRelations().size() > 0) {
			Criteria criteria = dao.createCriteria(BalanceRepairRelation.class);
			criteria.add(Restrictions.eq("balanceId", balance.getId()));
			@SuppressWarnings("unchecked")
			List<BalanceRepairRelation> list = criteria.list();
			if (list != null) {
				for (BalanceRepairRelation repairRelation : list) {
					dao.delete(repairRelation);
				}
			}
			for (int i = 0; i < dto.getBalanceRepairRelations().size(); i++) {
				BalanceRepairRelation repairRelation = new BalanceRepairRelation();
				repairRelation.setBalanceId(balance.getId());
				repairRelation.setLineOrderCode(dto.getBalanceRepairRelations().get(i).getLineOrderCode());
				repairRelation.setRepairId(dto.getBalanceRepairRelations().get(i).getRepairId());
				repairRelation.setMatterCode(dto.getBalanceRepairRelations().get(i).getMatterCode());
				repairRelation.setUnitPrice(dto.getBalanceRepairRelations().get(i).getUnitPrice());
				repairRelation.setAmount(dto.getBalanceRepairRelations().get(i).getAmount());
				dao.save(repairRelation);
			}
		}

	}

	// 删除结算单并删除orderRapair
	@Override
	public Result del(String id) {

		Balance balance = dao.findById(Balance.class, id);
		if (balance != null) {
			Criteria criteria = dao.createCriteria(BalanceRepairRelation.class);
			criteria.add(Restrictions.eq("balanceId", balance.getId()));
			@SuppressWarnings("unchecked")
			List<BalanceRepairRelation> list = criteria.list();
			if (list != null) {
				for (BalanceRepairRelation repairRelation : list) {
					dao.delete(repairRelation);
				}
			}
			dao.delete(balance);
			return new Result(balance);
		} else {
			return new Result(Constants.RESULT_CODE.SYS_ERROR, "没有此结算单！");
		}

	}

	// 获取结算单号
	public String getBalanceMaxValue() {
		Date date = new Date();
		Criteria criteria = dao.createCriteria(Code.class);
		criteria.add(Restrictions.between("updateTime", DateUtil.minDate(date), DateUtil.maxDate(date)));
		criteria.add(Restrictions.eq("type", "JS"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("maxValue"));
		criteria.setMaxResults(1);
		Code code = (Code) criteria.uniqueResult();
		if (code == null) {
			code = new Code();
			code.setMaxValue(1);
			code.setType("JS");
			code.setUpdateTime(date);
			dao.save(code);
		} else {
			code.setMaxValue(code.getMaxValue() + 1);
		}
		return String.format("%0" + 6 + "d", code.getMaxValue());
	}

	// 审核结算单
	@Override
	public Result checkBalance(String id, String checkInfo, String checkStatus) {
		Balance balance = dao.findById(Balance.class, id);
		if (balance != null) {
			// checkStatus: 2：已确认 3：已结算

			/**
			 * 确认：2 和审核：3后台校验
			 */
			if (StringUtils.isNotBlank(checkStatus) && Integer.parseInt(checkStatus) >= 1
					&& Integer.parseInt(checkStatus) <= 3) {
				// 结算确认校验
				if ("2".equals(checkStatus)) {
					// 结算确认只能确认待确认状态
					String dataBalanceStatus = balance.getBalanceStatus();
					if (!"1".equals(dataBalanceStatus)) {
						return new Result(Constants.RESULT_CODE.SYS_ERROR, "当前状态不能确认");
					}
					balance.setBalanceTime(null);
				}
				if ("3".equals(checkStatus)) {
					// 提交校验
					// 结算提交只能提交已确认状态
					String dataBalanceStatus = balance.getBalanceStatus();

					if (!"2".equals(dataBalanceStatus)) {
						return new Result(Constants.RESULT_CODE.SYS_ERROR, "当前状态不能提交");
					}
					balance.setBalanceTime(new Date());
				}
				// 设置结算状态
				balance.setBalanceStatus(checkStatus);
			} else {
				return new Result(Constants.RESULT_CODE.SYS_ERROR, "当前状态不能提交");
			}
			if (checkInfo != null && !checkInfo.isEmpty()) {
				balance.setCheckInfo(checkInfo);
			}
			balance.setUpdateTime(new Date());
			return new Result(balance);
		} else {
			return new Result(Constants.RESULT_CODE.SYS_ERROR, "没有此结算单！");
		}
	}

	// 打印Excel
	@Override
	public void getExcel(String id, HttpServletRequest request, HttpServletResponse response) {
		String excelFileName = "结算单明细";
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
			Balance balance = dao.findById(Balance.class, id);
			Pagination<BalanceModel> pagination = findListByBalanceidForExcel(id);
			List<BalanceModel> repairBalanceModels = pagination.getRows();

			/**
			 * 结算明细
			 */
			String[] headers = new String[] { "序号", "城市", "门店", "顾客姓名", "微调项目", "款式名", "件数", "颜色", "微调单号", "孔雀订单号",
					"单价", "金额", "发货时间", "收货时间", "发货物流单号" };
			/**
			 * 结算凭证
			 */
			/*
			 * String[] headers2 = new String[] { "收款方", "付款方", "付款方式", "货币类型", "结算金额",
			 * "当前定金总额", "本次扣除定金", "本次应收金额", "单价", "数量", "结算时间", "账期", "业务类型", "结算单号",
			 * "结算状态", "微调单来源", "税务相关信息", "备注" };
			 */
			Set<String> excludedFieldSet = new HashSet<String>();

			excludedFieldSet.add("unitName");
			excludedFieldSet.add("styleCode");
			excludedFieldSet.add("balanceTime");
			excludedFieldSet.add("createTime");
			excludedFieldSet.add("remark");

			excludedFieldSet.add("repairStatus");
			excludedFieldSet.add("checkStatus");
			excludedFieldSet.add("repairId");
			excludedFieldSet.add("logisticdeleteflag");

			excludedFieldSet.add("shopId");
			excludedFieldSet.add("companyId");

			excludedFieldSet.add("balanceNumeber");
			excludedFieldSet.add("balanceId");
			excludedFieldSet.add("balanceStatus");
			excludedFieldSet.add("lineOrderCode");
			excludedFieldSet.add("matterCode");

			out = response.getOutputStream();
			ExportExcelUtil.exportExcel(0, 0, 0, "结算单明细", headers,
					ExportExcelUtil.buildCustomizedExportedModel(repairBalanceModels, excludedFieldSet), out,
					"yyyy-MM-dd");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
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

	// 查询打印Excel数据
	public Pagination<BalanceModel> findListByBalanceidForExcel(String id) throws ParseException {

		Pagination<BalanceModel> pagination = new Pagination<BalanceModel>();
		if (StringUtils.isNotBlank(id)) {
			StringBuilder sql = new StringBuilder();
			List<BalanceModel> lrbm = new ArrayList<>();
			sql.append(
					"SELECT t_order_repair.c_city city , t_order_repair.c_shop_name shopname , t_order_repair.c_customer_name customername ,t_order_repair.c_trimming_json repairproject ,t_order_repair.c_goods_name goodsname ,t_order_repair.c_num number ,t_order_repair.c_goods_color color ,t_order_repair.c_order_repair_code repaircode ,t_order_repair.c_order_code ordercode ,t_balance_repair_relation.c_unit_price unitprice ,t_balance_repair_relation.c_amount amount ,logistics.send_time sendtime ,logistics.deliverytime expresstime ,logistics.expressnumber expressnumber  "
							+ "FROM t_balance  left join t_balance_repair_relation   on t_balance.c_id=t_balance_repair_relation.c_balance_id   "
							+ "left join t_order_repair  on t_order_repair.c_id=t_balance_repair_relation.c_repair_id   "
							+ "left join (select orderrepair.c_id, MAX(CASE logistic.c_logistic_type WHEN '0' THEN logistic.c_send_time ELSE null END )  'send_time',MAX(CASE logistic.c_logistic_type WHEN '1' THEN logistic.c_delivery_time ELSE null END ) 'deliverytime' ,MAX(CASE logistic.c_logistic_type WHEN '0' THEN logistic.c_express_number ELSE null END ) 'expressnumber'  "
							+ "From t_order_repair orderrepair   "
							+ "left join t_logistic_order logisticorder on orderrepair.c_id=logisticorder.c_order_repair_id   "
							+ "left join t_logistic logistic on logisticorder.c_logistic_id=logistic.c_id  "
							+ "where 1=1 and logistic.c_delete_flag=0    group by orderrepair.c_id  ) "
							+ "logistics on t_order_repair.c_id=logistics.c_id  " + "where 1=1 ");// 得到导出Excel的数据
			if (id != null && !id.isEmpty()) {
				sql.append(" and t_balance.c_id='").append(id).append("' ");
			}
			sql.append(
					" order by convert(t_order_repair.c_shop_name using gbk),convert(t_order_repair.c_customer_name using gbk)");

			List resultSet = dao.queryBySql(sql.toString());
			Integer num = 0;// 设置序号
			if (resultSet != null && resultSet.size() > 0) {
				for (Object result : resultSet) {// 便利查询出来的数据
					num++;// 自增
					BalanceModel rbm = new BalanceModel();
					Object[] properties = (Object[]) result;
					rbm.setNum(num.toString());
					rbm.setCity(properties[0] == null ? "" : properties[0].toString());
					rbm.setShopName(properties[1] == null ? "" : properties[1].toString());
					rbm.setCustomerName(properties[2] == null ? "" : properties[2].toString());
					rbm.setRepairProject(properties[3] == null ? "" : properties[3].toString());
					rbm.setStyleName(properties[4] == null ? "" : properties[4].toString());
					rbm.setNumber(properties[5] == null ? "" : properties[5].toString());
					rbm.setColor(properties[6] == null ? "" : properties[6].toString());
					rbm.setRepairCode(properties[7] == null ? "" : properties[7].toString());
					rbm.setOrderCode(properties[8] == null ? "" : properties[8].toString());
					rbm.setUnitPrice(properties[9] == null ? "" : properties[9].toString());
					rbm.setAmount(properties[10] == null ? "" : properties[10].toString());
					rbm.setSendTime(formatDate(properties[11] == null ? "" : properties[11].toString()));
					rbm.setExpressTime(formatDate(properties[12] == null ? "" : properties[12].toString()));
					rbm.setExpressNumber(properties[13] == null ? "" : properties[13].toString());
					lrbm.add(rbm);

				}

			}
			pagination.setRows(lrbm);
			pagination.setTotal(lrbm.size());
			return pagination;
		}

		return pagination;
	}

	/*
	 * 微调结算单明细
	 */
	@Override
	public Pagination<BalanceModel> listDetail(BalanceDto dto, PageBean pageBean) {
		if (pageBean.getPage() == null) {
			pageBean.setPage(1);
		}
		if (pageBean.getRows() == null) {
			pageBean.setRows(9999);
		}
		List<BalanceModel> lrbm = new ArrayList<>();
		Pagination<BalanceModel> pagination = new Pagination<>();
		StringBuilder sql = new StringBuilder();

		sql.append(
				"SELECT distinct balance.c_id,orderrepair.c_id orderrepairid,	orderrepair.c_city,	orderrepair.c_shop_name,company.c_unit_name,orderrepair.c_customer_name,orderrepair.c_trimming_json,orderrepair.c_goods_name, "
						+ "orderrepair.c_goods_code,orderrepair.c_goods_color,orderrepair.c_num,orderrepair.c_order_repair_code,balance.c_balance_numeber,balance.c_balance_time,balance.c_balance_status,orderrepair.c_order_repair_status, "
						+ "balancerepair.c_line_order_code,balancerepair.c_matter_code,balancerepair.c_unit_price,balancerepair.c_amount,logistics.c_send_time "
						+ "from t_balance  balance  "
						+ "left join t_balance_repair_relation   balancerepair on  balance.c_id=balancerepair.c_balance_id  "
						+ "left join t_order_repair  orderrepair on balancerepair.c_repair_id=orderrepair.c_id "
						+ "left join t_xiuyu_shop_company_relation shopcompany on orderrepair.c_shop_id=shopcompany.c_shop_id  "
						+ "left join t_xiuyu_company company  on shopcompany.c_company_id=company.c_id  "
						+ "left join  (select logistic.c_send_time,logistic.c_id,logisticorder.c_order_repair_id,logistic.c_logistic_type  from t_logistic_order logisticorder left join t_logistic logistic on logisticorder.c_logistic_id=logistic.c_id where logistic.c_logistic_type='0' and  logistic.c_delete_flag=0) logistics on logistics.c_order_repair_id=orderrepair.c_id  "
						+ "where  1=1 ");
		// and orderrepair.c_order_repair_status='已发货'
		/**
		 * 城市分公司查询
		 */
		if (dto.getCompanyId() != null && !dto.getCompanyId().isEmpty()) {
			sql.append(" and balance.c_company_id ='").append(dto.getCompanyId()).append("'");
		}
		/**
		 * 城市
		 */
		if (dto.getCity() != null && !dto.getCity().isEmpty()) {
			sql.append("  and orderrepair.c_city like '%").append(dto.getCity()).append("%'");
		}
		/**
		 * 发货起止时间
		 */

		if (dto.getStartTime() != null && !dto.getStartTime().isEmpty() && dto.getEndTime() != null
				&& !dto.getEndTime().isEmpty()) {// 添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and logistics.c_send_time between '").append(dto.getStartTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getEndTime()).append(" 23:59:59'");
		} else if (dto.getStartTime() != null && !dto.getStartTime().isEmpty()
				&& (dto.getEndTime() == null || dto.getEndTime().isEmpty())) {// 添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and  logistics.c_send_time like '%").append(dto.getStartTime()).append(" %'");
		} else if ((dto.getStartTime() == null || dto.getStartTime().isEmpty()) && dto.getEndTime() != null
				&& !dto.getEndTime().isEmpty()) {// 添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and  logistics.c_send_time like '%").append(dto.getEndTime()).append(" %'");
		}
		/**
		 * 结算时间
		 */

		if (dto.getBalanceStartTime() != null && !dto.getBalanceStartTime().isEmpty() && dto.getBalanceEndTime() != null
				&& !dto.getBalanceEndTime().isEmpty()) {// 添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and balance.c_balance_time between '").append(dto.getBalanceStartTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getBalanceEndTime()).append(" 23:59:59'");
		} else if (dto.getBalanceStartTime() != null && !dto.getBalanceStartTime().isEmpty()
				&& (dto.getBalanceEndTime() == null || dto.getBalanceEndTime().isEmpty())) {// 添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and balance.c_balance_time between '").append(dto.getBalanceStartTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getBalanceStartTime()).append(" 23:59:59'");
		} else if ((dto.getBalanceStartTime() == null || dto.getBalanceStartTime().isEmpty())
				&& dto.getBalanceEndTime() != null && !dto.getBalanceEndTime().isEmpty()) {// 添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and balance.c_balance_time  between '").append(dto.getBalanceEndTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getBalanceEndTime()).append(" 23:59:59'");
		}
		/**
		 * 门店
		 */
		if (dto.getShopId() != null && !dto.getShopId().isEmpty()) {
			sql.append(" and  orderrepair.c_shop_id = '").append(dto.getShopId()).append("'");
		}
		/**
		 * 微调项目
		 * 
		 * if (dto.getRepairProject() != null && !dto.getRepairProject().isEmpty()) {
		 * sql.append( " and repairproject like '%"
		 * ).append(dto.getRepairProject()).append("%'"); }
		 */
		/**
		 * 结算单id
		 */
		if (dto.getBalanceId() != null && !dto.getBalanceId().isEmpty()) {

			sql.append(" and balance.c_id = '").append(dto.getBalanceId()).append("'");
		}

		/**
		 * 微调单号
		 */
		if (dto.getRepairCode() != null && !dto.getRepairCode().isEmpty()) {
			sql.append(" and orderrepair.c_order_repair_code like '%").append(dto.getRepairCode()).append("%'");
		}
		/**
		 * 顾客姓名
		 */
		if (dto.getCustomerName() != null && !dto.getCustomerName().isEmpty()) {
			sql.append(" and orderrepair.c_customer_name like '%").append(dto.getCustomerName()).append("%'");
		}
		/**
		 * 结算单号
		 */
		if (dto.getBalanceCode() != null && !dto.getBalanceCode().isEmpty()) {
			sql.append(" and balance.c_balance_numeber like '%").append(dto.getBalanceCode()).append("%'");
		}
		/**
		 * 孔雀订单号
		 */
		if (dto.getOrderCode() != null && !dto.getOrderCode().isEmpty()) {
			sql.append(" and orderrepair.c_order_code like '%").append(dto.getOrderCode()).append("%'");
		}
		/**
		 * 物料编码
		 */
		if (dto.getMatterCode() != null && !dto.getMatterCode().isEmpty()) {
			sql.append(" and balancerepair.c_matter_code like '%").append(dto.getMatterCode()).append("%'");
		}
		/**
		 * 线下订单号
		 */
		if (dto.getLineOrderCode() != null && !dto.getLineOrderCode().isEmpty()) {
			sql.append(" and balancerepair.c_line_order_code like '%").append(dto.getLineOrderCode()).append("%'");
		}
		/**
		 * 微调单财务状态
		 */
		if (dto.getRepairCheckStatus() != null && !dto.getRepairCheckStatus().isEmpty()) {
			sql.append(" and orderrepair.c_order_repair_status = '").append(dto.getRepairStatus()).append("'");
		}
		/**
		 * 结算凭证结算状态
		 */
		if (dto.getBalanceStatus() != null && !dto.getBalanceStatus().isEmpty()) {
			sql.append(" and balance.c_balance_status = '").append(dto.getBalanceStatus()).append("'");
		}
		int total = dao.queryBySql(sql.toString()).size();
		pagination.setTotal(total);
		if (pageBean.getPage() != null && pageBean.getRows() != null) {
			sql.append(" limit " + (pageBean.getPage() - 1) * pageBean.getRows() + "," + pageBean.getRows());
		}
		List resultSet = dao.queryBySql(sql.toString());
		if (resultSet != null && resultSet.size() > 0) {
			for (Object result : resultSet) {
				BalanceModel rbm = new BalanceModel();
				Object[] properties = (Object[]) result;
				rbm.setBalanceId(properties[0] == null ? "" : properties[0].toString());
				rbm.setRepairId(properties[1] == null ? "" : properties[1].toString());
				rbm.setCity(properties[2] == null ? "" : properties[2].toString());
				rbm.setShopName(properties[3] == null ? "" : properties[3].toString());
				rbm.setUnitName(properties[4] == null ? "" : properties[4].toString());
				rbm.setCustomerName(properties[5] == null ? "" : properties[5].toString());
				rbm.setRepairProject(properties[6] == null ? "" : properties[6].toString());
				rbm.setStyleName(properties[7] == null ? "" : properties[7].toString());
				rbm.setStyleCode(properties[8] == null ? "" : properties[8].toString());
				rbm.setColor(properties[9] == null ? "" : properties[9].toString());
				rbm.setNum(properties[10] == null ? "" : properties[10].toString());
				rbm.setRepairCode(properties[11] == null ? "" : properties[11].toString());
				rbm.setBalanceNumeber(properties[12] == null ? "" : properties[12].toString());
				rbm.setBalanceTime(formatDate(properties[13] == null ? "" : properties[13].toString()));
				rbm.setBalanceStatus(properties[14] == null ? "" : properties[14].toString());
				rbm.setRepairStatus(properties[15] == null ? "" : properties[15].toString());
				rbm.setLineOrderCode(properties[16] == null ? "" : properties[16].toString());
				rbm.setMatterCode(properties[17] == null ? "" : properties[17].toString());
				rbm.setUnitPrice(properties[18] == null ? "" : properties[18].toString());
				rbm.setAmount(properties[19] == null ? "" : properties[19].toString());
				rbm.setSendTime(formatDate(properties[20] == null ? "" : properties[20].toString()));
				lrbm.add(rbm);

			}
		}
		pagination.setRows(lrbm);
		return pagination;

	}

	@Override
	public Result balanceById(String id) {
		if (StringUtils.isNotBlank(id)) {
			PageBean pageBean = new PageBean();
			Map<String, Object> map = new HashMap<String, Object>();
			Balance balance = dao.findById(Balance.class, id);
			BalanceDto dto = new BalanceDto();
			dto.setBalanceId(id);
			Pagination<BalanceModel> pagination = listDetail(dto, pageBean);
			if (balance != null) {
				map.put("balance", balance);
				if (pagination != null) {
					map.put("listDetail", pagination.getRows());
				}
				return new Result(map);
			} else {
				return new Result(Constants.RESULT_CODE.SYS_ERROR, "没有此结算单！");
			}

		}
		return new Result(Constants.RESULT_CODE.SYS_ERROR, "没有此结算单！");
	}

	// 替换微调项目字符串
		private String replaceStr(String str) {

			StringBuffer sb = new StringBuffer();
			if (StringUtils.isNotBlank(str)) {

				JSONObject obj = JsonUtil.objToJson(str);
				if (StringUtils.isNotBlank(obj.getString("jdgc"))) {
					sb.append("肩带过长:左右各减掉" + obj.getString("jdgc") + "CM;");
				}
				if (StringUtils.isNotBlank(obj.getString("ghjd"))) {

					if ("true".equals(obj.getString("ghjd"))) {
						sb.append("更換肩带;");

					}
				}
				if (StringUtils.isNotBlank(obj.getString("jdxh"))) {

					if ("true".equals(obj.getString("jdxh"))) {
						sb.append("肩带下滑:");
						sb.append("后面加辅助带，后U聚中调节;");
					}
				}
				if (StringUtils.isNotBlank(obj.getString("qcsggdjd"))) {

					if ("true".equals(obj.getString("qcsggdjd"))) {
						sb.append("去掉S钩固定肩带;");

					}
				}
				if (StringUtils.isNotBlank(obj.getString("yews"))) {
					
					JSONObject yews = JsonUtil.objToJson(obj.getString("yews"));
					if (StringUtils.isNotBlank(yews.getString("yw1"))) {
						sb.append("腋围1减少:" + yews.getString("yw1") + "CM;");
					}
					if (StringUtils.isNotBlank(yews.getString("yw2"))) {
						sb.append("腋围2减少:" + yews.getString("yw2") + "CM;");
					}
					if (StringUtils.isNotBlank(yews.getString("yw3"))) {
						sb.append("腋围3减少:" + yews.getString("yw3") + "CM;");
					}
					if (StringUtils.isNotBlank(yews.getString("yw123"))) {
						sb.append("腋围123整体减少:" + yews.getString("yw123") + "CM;");
					}
				}
				if (StringUtils.isNotBlank(obj.getString("yxg"))) {
					sb.append("腋下高:左右各降" + obj.getString("yxg") + "CM;");
				}
				if (StringUtils.isNotBlank(obj.getString("yxmr"))) {
					JSONObject yxmr = JsonUtil.objToJson(obj.getString("yxmr"));
					
					if (StringUtils.isNotBlank(yxmr.getString("jdyxrzc"))) {
						sb.append("腋下磨肉:降低腋下软支撑:" + yxmr.getString("jdyxrzc") + "CM;");
					}
					if (StringUtils.isNotBlank(yxmr.getString("ghcxx"))) {

						if ("true".equals(yxmr.getString("ghcxx"))) {
							sb.append("腋下磨肉:改花齿向下;");
						}

					}
					if (StringUtils.isNotBlank(yxmr.getString("gwbbfs"))) {
						if ("true".equals(yxmr.getString("gwbbfs"))) {
							sb.append("腋下磨肉:改为包边方式;");
						}

					}
				}
				if (StringUtils.isNotBlank(obj.getString("ywjgbgfr"))) {
					
					JSONObject ywjgbgfr = JsonUtil.objToJson(obj.getString("ywjgbgfr"));
					if (StringUtils.isNotBlank(ywjgbgfr.getString("q"))) {
						sb.append("腋围加高包裹副乳:前左右各加:" + ywjgbgfr.getString("q") + "CM;");
					}
					if (StringUtils.isNotBlank(ywjgbgfr.getString("h"))) {
						sb.append("腋围加高包裹副乳:后左右各加:" + ywjgbgfr.getString("h") + "CM;");
					}
				}
				if (StringUtils.isNotBlank(obj.getString("ghpk"))) {

					
					if ("true".equals(obj.getString("ghpk"))) {
						sb.append("更换排扣;");
					}
				}
				if (StringUtils.isNotBlank(obj.getString("hxxwsjd"))) {

					
					if ("true".equals(obj.getString("hxxwsjd"))) {
						sb.append("换下胸围松紧带;");
					}
				}
				if (StringUtils.isNotBlank(obj.getString("zbgd"))) {
					sb.append("罩杯过大:改小" + obj.getString("zbgd") + "个杯;");
				}
				if (StringUtils.isNotBlank(obj.getString("zbbyr"))) {

					
					if ("true".equals(obj.getString("zbbyr"))) {
						sb.append("罩杯不圆润:參照附件;");
					}
				}
				if (StringUtils.isNotBlank(obj.getString("zbsysbft"))) {
					
					JSONObject zbsysbft = JsonUtil.objToJson(obj.getString("zbsysbft"));
					if (StringUtils.isNotBlank(zbsysbft.getString("czfj"))) {
						if ("true".equals(zbsysbft.getString("czfj"))) {
							sb.append("罩杯上沿松不服贴:参照附件;");
						}
					}
					if (StringUtils.isNotBlank(zbsysbft.getString("jsgdjm"))) {
						if ("true".equals(zbsysbft.getString("jsgdjm"))) {
							sb.append("罩杯上沿松不服贴:接受囊袋加棉;");
						}
					}
				}
				if (StringUtils.isNotBlank(obj.getString("hhp"))) {
					JSONObject hhp = JsonUtil.objToJson(obj.getString("hhp"));

					
					if (StringUtils.isNotBlank(hhp.getString("ghbbm"))) {
						if ("true".equals(hhp.getString("ghbbm"))) {
							sb.append("换后片:更换表布面;");
						}
					}
					if (StringUtils.isNotBlank(hhp.getString("ghlb"))) {
						if ("true".equals(hhp.getString("ghlb"))) {
							sb.append("换后片:更换里布;");
						}
					}
				}
				if (StringUtils.isNotBlank(obj.getString("hzb"))) {
					sb.append("换罩杯：");
					if("0".equals(obj.getString("hzb"))) {
						sb.append("换大;");
					}
					if("1".equals(obj.getString("hzb"))) {
						sb.append("换小;");
					}
				}
				if (StringUtils.isNotBlank(obj.getString("zbax"))) {
					
					if ("true".equals(obj.getString("zbax"))) {
						sb.append("罩杯凹陷：修正;");
					}
				}
				if (StringUtils.isNotBlank(obj.getString("zbwcmd"))) {
					
					if ("true".equals(obj.getString("zbwcmd"))) {
						sb.append("罩杯外侧棉多:修正;");

					}
				}
				if (StringUtils.isNotBlank(obj.getString("zbsyjgls"))) {
					sb.append("罩杯上沿加高蕾丝:加高蕾丝" + obj.getString("zbsyjgls") + "CM;");
				}
				if (StringUtils.isNotBlank(obj.getString("zbqzxjgmx"))) {
					sb.append("罩杯上沿加高抹胸:加高" + obj.getString("zbqzxjgmx") + "CM;");
				}
				if (StringUtils.isNotBlank(obj.getString("hzblb"))) {
					
					if ("true".equals(obj.getString("hzblb"))) {
						sb.append("换罩杯里布;");
					}
				}
				if (StringUtils.isNotBlank(obj.getString("hgd"))) {
					sb.append("换囊袋:");
					
					if("0".equals(obj.getString("hgd"))) {
						sb.append("换大;");
					}
					if("1".equals(obj.getString("hgd"))) {
						sb.append("换小;");
					}
					if("2".equals(obj.getString("hgd"))) {
						sb.append("原大小更换;");
					}
				}
				if (StringUtils.isNotBlank(obj.getString("gdjm"))) {
					
					if ("true".equals(obj.getString("gdjm"))) {
						sb.append("囊袋下加棉;");
					}
				}
				if (StringUtils.isNotBlank(obj.getString("qdgd"))) {
					
					JSONObject qdgd = JsonUtil.objToJson(obj.getString("qdgd"));
					if (StringUtils.isNotBlank(qdgd.getString("qd"))) {
						if ("true".equals(qdgd.getString("qd"))) {
							sb.append("去掉囊袋;");
						}
					}
					if (StringUtils.isNotBlank(qdgd.getString("jhgd"))) {
						if ("true".equals(qdgd.getString("jhgd"))) {
							sb.append("寄回囊带;");
						}
					}

				}
				if (StringUtils.isNotBlank(obj.getString("gqgd"))) {
					
					if ("true".equals(obj.getString("gqgd"))) {
						sb.append("钢圈过大:换小一号钢圈;");
					}

				}
				if (StringUtils.isNotBlank(obj.getString("gqgx"))) {
					
					if ("true".equals(obj.getString("gqgx"))) {
						sb.append("钢圈过小:修正;");

					}
				}
				if (StringUtils.isNotBlank(obj.getString("qzxkkgx"))) {
					sb.append("前中心开口过小:开口改大" + obj.getString("qzxkkgx") + "CM;");
				}
				if (StringUtils.isNotBlank(obj.getString("hyxrzc"))) {
					
					JSONObject hyxrzc = JsonUtil.objToJson(obj.getString("hyxrzc"));
					if (StringUtils.isNotBlank(hyxrzc.getString("l"))) {
						if ("true".equals(hyxrzc.getString("l"))) {
							sb.append("换腋下软支撑:左;");
						}

					}
					if (StringUtils.isNotBlank(hyxrzc.getString("r"))) {
						if ("true".equals(hyxrzc.getString("r"))) {
							sb.append("换腋下软支撑:右;");
						}

					}
				}
				if (StringUtils.isNotBlank(obj.getString("qczbxrzc"))) {
					
					JSONObject qczbxrzc = JsonUtil.objToJson(obj.getString("qczbxrzc"));
					if (StringUtils.isNotBlank(qczbxrzc.getString("qc"))) {
						if ("true".equals(qczbxrzc.getString("qc"))) {
							sb.append("祛除罩杯下软支撑:去除;");
						}
					}
					if (StringUtils.isNotBlank(qczbxrzc.getString("hx"))) {
						if ("true".equals(qczbxrzc.getString("hx"))) {
							sb.append("换新罩杯下软支撑;");
						}
					}
				}
				if (StringUtils.isNotBlank(obj.getString("bbdwk"))) {
					
					if ("true".equals(obj.getString("bbdwk"))) {
						sb.append("B.B点外扩:土台收窄;");

					}
				}
				if (StringUtils.isNotBlank(obj.getString("ttkzww"))) {
					
					JSONObject ttkzww = JsonUtil.objToJson(obj.getString("ttkzww"));
					if (StringUtils.isNotBlank(ttkzww.getString("js"))) {
						sb.append("土台高度减少" + ttkzww.getString("js") + "CM;");
					}
					if (StringUtils.isNotBlank(ttkzww.getString("ls"))) {
						if ("true".equals(ttkzww.getString("ls"))) {
							sb.append("蕾丝边去掉;");
						}
					}
				}
				if (StringUtils.isNotBlank(obj.getString("xxwj"))) {
					sb.append("下胸围减:下胸围减" + obj.getString("xxwj") + "CM;");
				}
				if (StringUtils.isNotBlank(obj.getString("xxwjxjpj"))) {
					sb.append("下胸围紧需加排扣:加排扣" + obj.getString("xxwjxjpj") + "小排;");
				}
				return sb.toString();

			} else {
				return sb.toString();
			}

		}
	@Override
	public Pagination<OrderRepairModel> listRepair(OrderRepairDto dto, PageBean pageBean) {
		Pagination<OrderRepairModel> pagination = new Pagination<OrderRepairModel>();
		if (pageBean.getPage() == null) {
			pageBean.setPage(1);
		}
		if (pageBean.getRows() == null) {
			pageBean.setRows(9999);
		}
		StringBuffer sql = new StringBuffer();
		sql.append(
				"SELECT DISTINCT a.c_id,a.c_city,d.c_unit_name,a.c_shop_name,"
				+"a.c_order_repair_code,a.c_customer_name, a.c_goods_code,a.c_goods_name,"
				+"a.c_goods_color,a.c_num,f.c_send_time,b.c_goods_sn "
				+"FROM t_order_repair  a  left join t_order_detail b on a.c_order_detail_id = b.c_id "
				+"left join t_xiuyu_shop_company_relation c on a.c_shop_id = c.c_shop_id left join t_xiuyu_company d on c.c_company_id = d.c_id "
				+"left join t_logistic_order e on a.c_id = e.c_order_repair_id left join t_logistic f on e.c_logistic_id=f.c_id  "
				+"where a.c_order_repair_status='7'  ");
//				"SELECT distinct orderrepair.c_id,orderrepair.c_city,company.c_unit_name,orderrepair.c_shop_name,orderrepair.c_order_repair_code,orderrepair.c_customer_name,   "
//						+ "orderrepair.c_goods_code,orderrepair.c_goods_name,orderrepair.c_goods_color,orderrepair.c_num,logistics.c_send_time,orderdetail.c_goods_sn    "
//						+ "from (SELECT ID FROM (select balancerepair.c_repair_id ID from t_balance_repair_relation balancerepair  group by balancerepair.c_repair_id UNION ALL select c_id ID from t_order_repair) A GROUP BY ID HAVING COUNT(ID) = 1) allID    "
//						+ "left join t_order_repair  orderrepair ON allID.ID=orderrepair.c_id   "
//						+ "left join t_xiuyu_shop_company_relation shopcompany on orderrepair.c_shop_id=shopcompany.c_shop_id      "
//						+ "left join t_xiuyu_company company  on shopcompany.c_company_id=company.c_id      "
//						+ "left join  (select logistic.c_send_time,logistic.c_id,logisticorder.c_order_repair_id,logistic.c_logistic_type  from t_logistic_order logisticorder left join t_logistic logistic on logisticorder.c_logistic_id=logistic.c_id where logistic.c_logistic_type='0' and  logistic.c_delete_flag=0) logistics on logistics.c_order_repair_id=orderrepair.c_id    "
//						+ "left join ( select c_goods_code,c_goods_sn from  t_order_detail  group by c_goods_code) orderdetail on orderrepair.c_goods_code =orderdetail.c_goods_code      "
//						+ "WHERE 1=1   and orderrepair.c_order_repair_status=7");
		if (StringUtils.isNotBlank(dto.getOrderRepairCode())) {
			sql.append(" and a.c_order_repair_code ='" + dto.getOrderRepairCode() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getCustomerInfo())) {
			sql.append(" and ((a.c_customer_name like '%" + dto.getCustomerInfo()
					+ "%')  or  (a.c_customer_code like '%" + dto.getCustomerInfo() + "%')) ");
		}
		if (StringUtils.isNotBlank(dto.getCity())) {
			sql.append(" and a.c_city ='" + dto.getCity() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getShopId())) {
			sql.append(" and a.c_shop_id ='" + dto.getShopId() + "' ");
		}
		if (StringUtils.isNotBlank(dto.getCompanyId())) {
			sql.append(" and d.c_id ='" + dto.getCompanyId() + "' ");
		}
		// 发货时间
		if (dto.getSendStartTime() != null && !dto.getSendStartTime().isEmpty() && dto.getSendEndTime() != null
				&& !dto.getSendEndTime().isEmpty()) {// 添加同时包含起始订单日期和截止订单日期的限定条件
			sql.append(" and f.c_send_time between '").append(dto.getSendStartTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getSendEndTime()).append(" 23:59:59'");
		} else if (dto.getSendStartTime() != null && !dto.getSendStartTime().isEmpty()
				&& (dto.getSendEndTime() == null || dto.getSendEndTime().isEmpty())) {// 添加只包含起始订单日期而不包含截止订单日期的限定条件
			sql.append(" and f.c_send_time between '").append(dto.getSendStartTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getSendStartTime()).append(" 23:59:59'");
		} else if ((dto.getSendStartTime() == null || dto.getSendStartTime().isEmpty()) && dto.getSendEndTime() != null
				&& !dto.getSendEndTime().isEmpty()) {// 添加只包含截止订单日期而不包含起始订单日期的限定条件
			sql.append(" and f.c_send_time  between '").append(dto.getSendEndTime()).append(" 00:00:00'")
					.append(" and '").append(dto.getSendEndTime()).append(" 23:59:59'");
		}
		int total = dao.queryBySql(sql.toString()).size();
		pagination.setTotal(total);
		if (pageBean.getPage() != null && pageBean.getRows() != null) {
			sql.append("  limit " + (pageBean.getPage() - 1) * pageBean.getRows() + "," + pageBean.getRows());
		}
		List<OrderRepairModel> lrbm = new ArrayList<>();
		List resultSet = dao.queryBySql(sql.toString());
		if (resultSet != null && resultSet.size() > 0) {
			for (Object result : resultSet) {
				OrderRepairModel rbm = new OrderRepairModel();
				Object[] properties = (Object[]) result;
				rbm.setId(properties[0] == null ? "" : properties[0].toString());
				rbm.setCity(properties[1] == null ? "" : properties[1].toString());
				rbm.setCompanyName(properties[2] == null ? "" : properties[2].toString());
				rbm.setShopName(properties[3] == null ? "" : properties[3].toString());
				rbm.setOrderRepairCode(properties[4] == null ? "" : properties[4].toString());
				rbm.setCustomerName(properties[5] == null ? "" : properties[5].toString());
				rbm.setGoodsCode(properties[6] == null ? "" : properties[6].toString());
				rbm.setGoodsName(properties[7] == null ? "" : properties[7].toString());
				rbm.setGoodsColor(properties[8] == null ? "" : properties[8].toString());
				rbm.setNum(properties[9] == null ? "" : properties[9].toString());
				rbm.setSendExpressNumber(properties[10] == null ? "" : properties[10].toString());
				rbm.setGoodsSn(properties[11] == null ? "" : properties[11].toString());
				lrbm.add(rbm);
			}
		}
		pagination.setRows(lrbm);

		return pagination;
	}

	private String formatDate(String time) {
		String newTime = null;
		if (StringUtils.isNotBlank(time)) {
			newTime = time.substring(0, 19);
		}
		return newTime;

	}

}
