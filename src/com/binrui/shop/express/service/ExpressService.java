package com.binrui.shop.express.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;
import com.binrui.shop.utils.ApiUtils;

/**
 * 查询物流快递情况
 * @author lijinfeng
 * @version 1.0
 * @date 2015-08-02 21:11
 * @title <p>查询物流快递情况</p>
 */
@Service("expressService")
public class ExpressService {
	/**
	 * 查询订单物流情况
	 * @param key 		身份授权key，请 kuaidi100 进行申请（大小写敏感）
	 * @param com		要查询的快递公司代码，不支持中文，对应的公司代码[参考快递100文档]
	 * @param number	快递单号码
	 * @return
	 * {"message":"ok","status":"1","state":"3","data":
     *       [{"time":"2012-07-07 13:35:14","context":"客户已签收"},
     *        {"time":"2012-07-07 09:10:10","context":"离开 [北京石景山营业厅] 派送中，递送员[温]，电话[]"},
     *        {"time":"2012-07-06 19:46:38","context":"到达 [北京石景山营业厅]"},
     *        {"time":"2012-07-06 15:22:32","context":"离开 [北京石景山营业厅] 派送中，递送员[温]，电话[]"},
     *        {"time":"2012-07-06 15:05:00","context":"到达 [北京石景山营业厅]"},
     *        {"time":"2012-07-06 13:37:52","context":"离开 [北京_同城中转站] 发往 [北京石景山营业厅]"},
     *        {"time":"2012-07-06 12:54:41","context":"到达 [北京_同城中转站]"},
     *        {"time":"2012-07-06 11:11:03","context":"离开 [北京运转中心驻站班组] 发往 [北京_同城中转站]"},
     *        {"time":"2012-07-06 10:43:21","context":"到达 [北京运转中心驻站班组]"},
     *        {"time":"2012-07-05 21:18:53","context":"离开 [福建_厦门支公司] 发往 [北京运转中心_航空]"},
     *        {"time":"2012-07-05 20:07:27","context":"已取件，到达 [福建_厦门支公司]"}
     *  ]} 
	 */
	@SuppressWarnings("static-access")
	public JSONObject queryOrderDelivery(String key,String com,String number){
		String param = "?id="+key+"&com="+com+"&nu="+number+"&show=0&muti=0&order=desc";
		try{
			URL url= new URL(ApiUtils.KUAIDI100_API_URL+param);
			URLConnection con=url.openConnection();
			con.setAllowUserInteraction(false);
			InputStream urlStream = url.openStream();
			String type = con.guessContentTypeFromStream(urlStream);
		    String charSet=null;
			if (type == null)
				type = con.getContentType();
			if (type == null || type.trim().length() == 0 || type.trim().indexOf("text/html") < 0)
				return null;

			if(type.indexOf("charset=") > 0)
			    charSet = type.substring(type.indexOf("charset=") + 8);

			byte b[] = new byte[10000];
			int numRead = urlStream.read(b);
			String content = new String(b, 0, numRead);
			while (numRead != -1) {
			numRead = urlStream.read(b);
			if (numRead != -1) {
				//String newContent = new String(b, 0, numRead);
				String newContent = new String(b, 0, numRead, charSet);
				content += newContent;
				}
			}
			//System.out.println("content:" + content);
			urlStream.close();
			if(StringUtils.isNotBlank(content)){
				JSONObject json = JSONObject.parseObject(content);
				return json;
			}
		} catch (MalformedURLException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * kuaidi100 API快递公司代码
	 * @author lijinfeng
	 * {
	 * 安信达  anxindakuaixi,
	 * 百世汇通 huitongkuaidi,
	 * 包裹平邮/挂号信 youzhengguonei,
	 * 邦送物流 bangsongwuliu
	 * 德邦物流 debangwuliu
	 * EMS快递 ems
	 * 国通快递 guotongkuaidi
	 * 汇通快递 huitongkuaidi
	 * 申通快递 shentong
	 * 顺风快递 shunfeng
	 * 2015-06-10
	 */
	public static enum ExpressCode { 
		AXD("anxindakuaixi"),BSHT("huitongkuaidi"),BGPY("youzhengguonei"),
		DBWL("debangwuliu"),EMS("ems"),GTKD("guotongkuaidi"),HTKD("huitongkuaidi"),
		STKD("shentong"),SFKD("shunfeng");
		private String code;
		ExpressCode(String code){
			this.code = code;
		}
		public String getCode() { 
			return this.code; 
		} 
	}
}
