package com.binrui.shop.pay.alipay.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mobile.device.Device;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.binrui.shop.pay.alipay.config.AlipayConfig;
import com.binrui.shop.pay.alipay.util.AlipayNotify;
import com.binrui.shop.pay.alipay.util.AlipaySubmit;
/**
@Controller("AlipayController")
@RequestMapping("/")
@PropertySource("classpath:system.properties")
public class AlipayController {

	@Autowired
	CartService cartService;
	
	@Autowired
	AlipayService alipayService;
	
	@Autowired
	private Environment env;
	
	@RequestMapping(value = "alipaySubmit", method = RequestMethod.POST)
	public String executeAlipaySubmit(Model model, HttpSession session, @Valid @ModelAttribute("alipayForm") AlipayForm alipayForm, BindingResult results, Device device) throws SQLException {
		GoodsForm goodsForm=new GoodsForm();
		goodsForm.setType("粮食");
		model.addAttribute("goodsForm", goodsForm);
		log.info("修改购物车信息为已付款");
		UVO uvo = (UVO)session.getAttribute("UVO");
		if (uvo == null || StringUtils.isEmpty(uvo.getGuestId())) {
    		return "redirect:/initGuestLogin";
    	}
		alipayForm.setUpdateUser(uvo.getGuestName());
		Date date = new Date();
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		alipayForm.setUpdateTime(dateformat.format(date));
		alipayForm.setGuestId(uvo.getGuestId());
		boolean hisResult = cartService.addAlipayHistory(alipayForm);
		if(!hisResult) {
			throw new SQLException("添加支付宝账单失败！");
		}
		CartForm cartForm = new CartForm();
    	cartForm.setUpdateUser(uvo.getGuestName());
		cartForm.setUpdateTime(dateformat.format(date));
    	cartForm.setGuestId(uvo.getGuestId());
    	boolean result = cartService.editCartStatus(cartForm);
		if(!result) {
			throw new SQLException("修改购物车信息失败！");
		}
		if(device.isNormal()) {
	        model.addAttribute("sHtmlText", alipayRequestWeb(alipayForm));
		} else {
	        model.addAttribute("sHtmlText", alipayRequestMobile(alipayForm));
		}
		return "manager/charge/alipay";
	}
	
	@RequestMapping(value = "replayAlipaySubmit", method = RequestMethod.POST)
	public String executeReplayAlipaySubmit(Model model, HttpSession session, @Valid @ModelAttribute("alipayForm") AlipayForm alipayForm, BindingResult results, Device device) throws SQLException {
		GoodsForm goodsForm = new GoodsForm();
		goodsForm.setType("粮食");
		model.addAttribute("goodsForm", goodsForm);
		log.info("重新向支付宝发起支付请求。");
		if (device.isNormal()) {
	        model.addAttribute("sHtmlText", alipayRequestWeb(alipayForm));
		} else {
	        model.addAttribute("sHtmlText", alipayRequestMobile(alipayForm));
		}
		return "manager/charge/alipay";
	}
	
	@RequestMapping(value = "initDistributorAlipayComfirm", method = RequestMethod.GET)
	public String initDistributorAlipayComfirm(Model model,HttpSession session, AlipayForm alipayForm) {
		log.info("由分销商直接推荐的商品销售页面初始化。");
		List<CartForm> cartList = new ArrayList<>();
		model.addAttribute("cartList", cartList);
		GoodsForm goodsForm = new GoodsForm();
		goodsForm.setType("粮食");
		model.addAttribute("goodsForm", goodsForm);
		UVO uvo = new UVO();
		session.setAttribute("UVO", uvo);
		model.addAttribute("alipayForm", alipayService.searchAlipay(alipayForm));
		return "mobile/alipay/distributorAlipayConfirm";
	}
	
	@RequestMapping(value = "distributorAlipaySubmit", method = RequestMethod.POST)
	public String executeDistributorAlipaySubmit(Model model,@Valid @ModelAttribute("alipayForm") AlipayForm alipayForm, BindingResult results) throws SQLException {
		log.info("由分销商直接推荐的商品向支付宝发起支付请求。");
		GoodsForm goodsForm = new GoodsForm();
		goodsForm.setType("粮食");
		model.addAttribute("goodsForm", goodsForm);
		if (results.hasErrors()) {
			log.info("内容验证出错");
			model.addAttribute("message", "该画面所有项目都是必填项，请认真填写！");
			model.addAttribute("alipayForm", alipayService.searchAlipay(alipayForm));
			return "mobile/alipay/distributorAlipayConfirm";
		}
		alipayForm.setUpdateUser(alipayForm.getGuestId());
		Date date = new Date();
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		alipayForm.setUpdateTime(dateformat.format(date));
		boolean hisResult = cartService.addAlipayHistory(alipayForm);
		if(!hisResult) {
			throw new SQLException("添加支付宝账单失败！");
		}
		alipayForm.setCount("1");
		boolean result = alipayService.editStock(alipayForm);
		if(!result) {
			model.addAttribute("message", "该商品为促销打折商品，目前已经卖完了，请关注我们下次活动，谢谢您的参与！");
			model.addAttribute("alipayForm", alipayService.searchAlipay(alipayForm));
			return "mobile/alipay/distributorAlipayConfirm";
		}
        model.addAttribute("sHtmlText", alipayRequestMobile(alipayForm));
        return "manager/charge/alipay";
	}
	
	@RequestMapping(value = "guestAlipaySubmit", method = RequestMethod.POST)
	public String executeGuestAlipaySubmit(Device device, Model model, @Valid @ModelAttribute("alipayForm") AlipayForm alipayForm, BindingResult results) throws SQLException {
		log.info("由匿名用户购买商品向支付宝发起支付请求。");
		List<CartForm> cartList = new ArrayList<>();
		model.addAttribute("cartList", cartList);
		GoodsForm goodsForm = new GoodsForm();
		goodsForm.setType("粮食");
		model.addAttribute("goodsForm", goodsForm);
		if (results.hasErrors()) {
			log.info("内容验证出错");
			model.addAttribute("message", "该画面所有项目都是必填项，请认真填写！");
			CartForm cartForm = new CartForm();
			cartForm.setCommodityId(alipayForm.getCommodityId());
			cartForm.setGuestId(alipayForm.getGuestId());
			cartForm.setCount(alipayForm.getCount());
			model.addAttribute("alipayForm", cartService.searchAlipay(cartForm));
			if (device.isNormal()) {
				
				return "shop/alipay/guestAlipayConfirm";
			} else {
				return "mobile/alipay/guestAlipayConfirm";
			}
		}
		
		alipayForm.setUpdateUser(alipayForm.getGuestId());
		Date date = new Date();
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		alipayForm.setUpdateTime(dateformat.format(date));
		String tempCommodityId = alipayForm.getCommodityId();
		alipayForm.setCommodityId(null);
		boolean hisResult = cartService.addAlipayHistory(alipayForm);
		if(!hisResult) {
			throw new SQLException("添加支付宝账单失败！");
		}
		alipayForm.setCommodityId(tempCommodityId);
		boolean result = alipayService.editStock(alipayForm);
		if(!result) {
			model.addAttribute("message", "该商品刚刚被卖完了！");
			model.addAttribute("alipayForm", alipayService.searchAlipay(alipayForm));
			if (device.isNormal()) {
				return "shop/alipay/guestAlipayConfirm";
			} else {
				return "mobile/alipay/guestAlipayConfirm";
			}
		}
		if (device.isNormal()) {
			model.addAttribute("sHtmlText", alipayRequestWeb(alipayForm));
		} else {
	        model.addAttribute("sHtmlText", alipayRequestMobile(alipayForm));
		}
		return "manager/charge/alipay";
	}
	
	private String alipayRequestWeb(AlipayForm alipayForm) {
		// 支付类型
	    String payment_type = "1";
	    // 必填，不能修改
	    // 服务器异步通知页面路径
	    String host = env.getProperty("host.web");
	    String notify_url = host + "/initReturn";
	    // 需http://格式的完整路径，不能加?id=123这类自定义参数

	    // 页面跳转同步通知页面路径
	    String return_url = host + "/initPayResult";

	    // 需http://格式的完整路径，不能加?id=123这类自定义参数，不能写成http://localhost/
	    
	    // 商户订单号
	    String out_trade_no = alipayForm.getOutTradeNo();
	    // 订单名称
	    String subject = alipayForm.getSubject();
	    // 付款金额
        String total_fee = alipayForm.getPrice();

        // 订单描述
        String body = alipayForm.getBody();

        // 商品展示地址
        String show_url = alipayForm.getShowUrl();
        // 需以http://开头的完整路径，如：http://www.商户网站.com/myorder.html
        
		//防钓鱼时间戳
		String anti_phishing_key = "";
		//若要使用请调用类文件submit中的query_timestamp函数

		//客户端的IP地址
		String exter_invoke_ip = "";
		//非局域网的外网IP地址，如：221.0.0.1
        
        // 收货人姓名
        String receive_name = alipayForm.getReceiveName();
        // 收货人地址
        String receive_address = alipayForm.getReceiveAddress();
        // 收货人邮编
        String receive_zip = alipayForm.getReceiveZip();
        // 收货人电话号码
        String receive_phone = alipayForm.getReceivePhone();
        // 收货人手机号码
        String receive_mobile = alipayForm.getReceiveMobile();

        body = body + ";" + receive_name + ";" + receive_address + ";" + receive_zip + ";" + receive_phone + ";" + receive_mobile;
        
        Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("service", "create_direct_pay_by_user");
        sParaTemp.put("partner", AlipayConfig.partner);
        sParaTemp.put("seller_email", AlipayConfig.seller_email);
        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
        sParaTemp.put("payment_type", payment_type);
        sParaTemp.put("notify_url", notify_url);
        sParaTemp.put("return_url", return_url);
        sParaTemp.put("out_trade_no", out_trade_no);
        sParaTemp.put("subject", subject);
        sParaTemp.put("total_fee", total_fee);
        sParaTemp.put("body", body);
        sParaTemp.put("show_url", show_url);
        sParaTemp.put("anti_phishing_key", anti_phishing_key);
		sParaTemp.put("exter_invoke_ip", exter_invoke_ip);

        String sHtmlText = AlipaySubmit.buildRequest(sParaTemp, "get", "确认");
        return sHtmlText;
	}
	
	private String alipayRequestMobile(AlipayForm alipayForm) {
		// 支付类型
	    String payment_type = "1";
	    // 必填，不能修改
	    // 服务器异步通知页面路径
	    String host = env.getProperty("host.mobile");
	    String notify_url = host + "/initReturn";
	    // 需http://格式的完整路径，不能加?id=123这类自定义参数

	    // 页面跳转同步通知页面路径
	    String return_url = host + "/initPayResult";

	    // 需http://格式的完整路径，不能加?id=123这类自定义参数，不能写成http://localhost/
	    
	    // 商户订单号
	    String out_trade_no = alipayForm.getOutTradeNo();
	    // 订单名称
	    String subject = alipayForm.getSubject();
	    // 付款金额
        String total_fee = alipayForm.getPrice();

        // 订单描述
        String body = alipayForm.getBody();

        // 商品展示地址
        String show_url = alipayForm.getShowUrl();
        // 需以http://开头的完整路径，如：http://www.商户网站.com/myorder.html
        
		//超时时间
		String it_b_pay = "";
		//选填

		//钱包token
		String extern_token = "";
		//选填
		
        // 收货人姓名
        String receive_name = alipayForm.getReceiveName();
        // 收货人地址
        String receive_address = alipayForm.getReceiveAddress();
        // 收货人邮编
        String receive_zip = alipayForm.getReceiveZip();
        // 收货人电话号码
        String receive_phone = alipayForm.getReceivePhone();
        // 收货人手机号码
        String receive_mobile = alipayForm.getReceiveMobile();

        body = body + ";" + receive_name + ";" + receive_address + ";" + receive_zip + ";" + receive_phone + ";" + receive_mobile;
        
        Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.wap.create.direct.pay.by.user");
        sParaTemp.put("partner", AlipayConfig.partner);
        sParaTemp.put("seller_id", AlipayConfig.seller_id);
        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("payment_type", payment_type);
		sParaTemp.put("notify_url", notify_url);
		sParaTemp.put("return_url", return_url);
		sParaTemp.put("out_trade_no", out_trade_no);
		sParaTemp.put("subject", subject);
		sParaTemp.put("total_fee", total_fee);
		sParaTemp.put("show_url", show_url);
		sParaTemp.put("body", body);
		sParaTemp.put("it_b_pay", it_b_pay);
		sParaTemp.put("extern_token", extern_token);

        String sHtmlText = AlipaySubmit.buildRequest(sParaTemp, "get", "确认");
        return sHtmlText;
	}
	
	@RequestMapping(value = "initReturn", method = RequestMethod.POST)
	public void executeInitReturn(Model model, ReturnForm returnForm, HttpServletResponse response) throws SQLException, IOException {
		log.info("这是一个支付宝主动调用商家网站信息的日志");
		log.info(returnForm.getOut_trade_no());
		log.info(returnForm.getTrade_no());
		log.info(returnForm.getTrade_status());
		log.info(returnForm.getOut_trade_no());
		log.info(returnForm.getTrade_no());
		log.info(returnForm.getTrade_status());
		log.info(returnForm.getNotify_id());
		log.info(returnForm.getSign());
		log.info("sign_type:" + returnForm.getSign_type());
		Map<String,String> params = new HashMap<String,String>();
		params.put("out_trade_no", returnForm.getOut_trade_no());
		params.put("trade_no", returnForm.getTrade_no());
		params.put("trade_status", returnForm.getTrade_status());
		params.put("notify_id", returnForm.getNotify_id());
		params.put("sign", returnForm.getSign());
		params.put("sign_type", returnForm.getSign_type());
		params.put("discount", returnForm.getDiscount());
		params.put("payment_type", returnForm.getPayment_type());
		params.put("subject", returnForm.getSubject());
		params.put("buyer_email", returnForm.getBuyer_email());
		params.put("gmt_create", returnForm.getGmt_create());
		params.put("notify_type", returnForm.getNotify_type());
		params.put("quantity", returnForm.getQuantity());
		params.put("seller_id", returnForm.getSeller_id());
		params.put("notify_time", returnForm.getNotify_time());
		params.put("body", returnForm.getBody());
		params.put("is_total_fee_adjust", returnForm.getIs_total_fee_adjust());
		params.put("total_fee", returnForm.getTotal_fee());
		params.put("gmt_payment", returnForm.getGmt_payment());
		params.put("seller_email", returnForm.getSeller_email());
		params.put("price", returnForm.getPrice());
		params.put("buyer_id", returnForm.getBuyer_id());
		params.put("use_coupon", returnForm.getUse_coupon());
		PrintWriter out=response.getWriter();
		if(AlipayNotify.verify(params)){
			out.print("success");
			log.info("success");
			boolean result = alipayService.editPayment(returnForm);
			if (!result) {
				throw new SQLException("付款标记修改失败！");
			}
		} else {
			out.print("fail");
			log.info("fail");
		}
	}
	
	@RequestMapping(value = "initPayResult", method = RequestMethod.GET)
	public String initPayResult(Model model, Device device) {
		log.info("支付宝处理完毕后返回商户网站");
		if (device.isNormal()) {
			return "shop/alipay/payResult";
		} else {
			return "mobile/alipay/payResult";
		}
	}
}**/
