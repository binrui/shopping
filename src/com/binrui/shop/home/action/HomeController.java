package com.binrui.shop.home.action;

import org.apache.log4j.Logger;  
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * Home Page
 * @author lijinfeng
 * @version 1.0
 * @date 2015-08-02 16:22
 * @title This is home page
 */
@Controller
public class HomeController {
	/**
	 * log4j
	 */
    private static final Logger logger = Logger.getLogger(HomeController.class);

    @RequestMapping("/")
    public String home(SitePreference sitePreference, Model model) {
        if (sitePreference == SitePreference.NORMAL) {
            logger.info("Site preference is normal");
            return "home";
        } else if (sitePreference == SitePreference.MOBILE) {
            logger.info("Site preference is mobile");
            return "home-mobile";
        } else if (sitePreference == SitePreference.TABLET) {
            logger.info("Site preference is tablet");
            return "home-tablet";
        } else {
            logger.info("no site preference");
            return "home";
        }
    }
}
