package cn.com.bestpay.posp.netty.client;

import java.util.Map;

import javax.management.RuntimeErrorException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.com.bestpay.posp.netty.logic.DataAnalysis;
import cn.com.bestpay.posp.netty.module.Contants;

/**
 * 转发Posp请求至银联
 * 
 * @author yzh
 * @date 2015-11-26
 * 
 */
public class TransPospToUnipay {
	private static final Logger logger = Logger.getLogger(TransPospToUnipay.class);
	/**
	 * 默认编码
	 */
	public static String encoding = "UTF-8";

	/**
	 * 转发信息至银联
	 * 
	 * @author yzh
	 * @date 2015-11-26
	 * @param msg
	 * @return
	 */
	public static Map<String, String> transDataToUnipay(Map<String, String> msg, String clientId) {

		String requestUrl = msg.get("requestUrl");
		if (StringUtils.isBlank(requestUrl)) {
			msg.put("respCode", Contants.SEND_RES_FAIL);
			logger.error(String.format("[%s]： 请求中没有银联地址!", clientId));
			return msg;
		}
		String en = msg.get("encoding");
		HttpsClient hc = new HttpsClient(requestUrl, 30000, 30000);
		try {
			String resultString = "";
			int status;
			msg.remove("requestUrl");
			msg.remove("respCode");
			if (StringUtils.isNotBlank(en)) {
				status = hc.send(msg, en);
			} else {
				status = hc.send(msg, encoding);
			}
			resultString = hc.getResult();
			logger.debug(String.format("[%s] ： 银联同步返回数据[status:%s]:%s", clientId, status, resultString));
			Map<String, String> res = DataAnalysis.convertResultStringToMap(resultString);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(String.format("[%s] ： 请求发送银联失败!", clientId));
			throw new RuntimeErrorException(new Error(),e.getMessage());
		}
	}
}
