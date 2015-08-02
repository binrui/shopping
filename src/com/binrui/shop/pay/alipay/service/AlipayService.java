package com.binrui.shop.pay.alipay.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.binrui.shop.pay.alipay.dao.AlipayDao;

/**
@Service
@PropertySource("classpath:system.properties")
public class AlipayService {

	@Autowired
	AlipayDao queryDao;

	
	@Autowired
	private Environment env;
	
	public AlipayForm searchAlipay(AlipayForm frm) {
		DistributorPriceForm distributorPriceForm = queryDao.executeForObject("Alipay.selectDistributorPrice", frm, DistributorPriceForm.class);
		AlipayForm alipayForm = new AlipayForm();
		Date date = new Date();
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmmss");
		
		alipayForm.setOutTradeNo(distributorPriceForm.getDistributorId() + dateformat.format(date));
		alipayForm.setSubject(distributorPriceForm.getDistributorId() +"推荐的商品订单");
		Double price = Double.valueOf(distributorPriceForm.getRetailPrice());
		// 不满88元加8元邮费
		if (price < 88) {
			price = price + 8;
		}
		alipayForm.setPrice(String.valueOf(price));
		alipayForm.setBody(distributorPriceForm.getCommodityName());
		String host = env.getProperty("host.mobile");
		alipayForm.setShowUrl(host + "/initDistributorAlipayComfirm?distributorPriceId=" + distributorPriceForm.getDistributorPriceId());
		//alipayForm.setShowUrl("http://localhost:8080/agriculture-mvc/initDistributorAlipayComfirm?distributorPriceId=" + distributorPriceForm.getDistributorPriceId());
		alipayForm.setGuestId(distributorPriceForm.getDistributorId());
		alipayForm.setCommodityId(distributorPriceForm.getCommodityId());
		alipayForm.setDistributorName(distributorPriceForm.getDistributorName());
		alipayForm.setDistributorPriceId(distributorPriceForm.getDistributorPriceId());
		return alipayForm;
	}
	
	public boolean editStock(AlipayForm frm) {
		Integer stock = queryDao.executeForObject("Alipay.selectStock", frm, Integer.class);
		if (stock < Integer.valueOf(frm.getCount())) {
			return false;
		}
		int result = queryDao.execute("Alipay.editStock", frm);
		if (result == 1) {
			return true;
		}
		return false;
	}
	
	public boolean editPayment(ReturnForm frm) {
		if ("TRADE_SUCCESS".equals(frm.getTrade_status())) {
			int result = queryDao.execute("Alipay.editPayment", frm);
			if (result == 1) {
				return true;
			}
		}
		return false;
	}
}
**/