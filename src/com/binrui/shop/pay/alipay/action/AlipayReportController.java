package com.binrui.shop.pay.alipay.action;

import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 * @author lijinfeng
 *
 */
/**
@Controller("AlipayReportController")
@RequestMapping("/alipay")
public class AlipayReportController {

	@Autowired
	AlipayReportService alipayReportService;
	
	@RequestMapping(value = "initAlipayReport", method = RequestMethod.GET)
	public String initAlipayReport(Model model) {
		log.info("检索支付宝账单列表");
		model.addAttribute("list", alipayReportService.searchAlipayReportList());
		return "manager/report/alipayReportList";
	}
	
	@RequestMapping(value = "delAlipayReport", method = RequestMethod.GET)
	public String initAlipayReport(Model model, AlipayReportForm alipayReportForm) throws SQLException {
		log.info("删除作废的支付宝账单");
		boolean result = alipayReportService.delAlipayReport(alipayReportForm);
		if (!result) {
			throw new SQLException("删除作废的支付宝账单失败！");
		}
		model.addAttribute("list", alipayReportService.searchAlipayReportList());
		return "manager/report/alipayReportList";
	}
}
**/