package com.binrui.shop.express.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binrui.shop.express.service.ExpressService;

@Controller
@RequestMapping("/order/express")
public class ExpressController {
	/**
	 * 注入快递服务
	 */
	@Autowired
	private ExpressService expressService;
	
}
