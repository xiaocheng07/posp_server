package cn.com.bestpay.posp.netty.module;

import org.apache.log4j.Logger;

public class Contants {
	private static final Logger logger = Logger.getLogger(Contants.class);
	/**
	 * http - 监听 银联后台 的端口
	 */
	public static int HTTP_BACK_PORT;
	/**
	 * tcp - 监听posp的端口
	 */
	public static int TCP_SERVER_PORT;
	/** 业务出现线程大小 */
	public static int TCP_BIZTHREADSIZE;
	public static boolean isSSL; 
	/**
	 * 系统配置文件名
	 */
	public static final String ENV_FILE = "unipay_env.properties";
	/**
	 * 请求发送失败
	 */
	public static final String SEND_RES_FAIL = "98";
	/**
	 * 请求发送成功
	 */
	public static final String SEND_RES_SUS = "00";
	/**
	 * 银联异步响应
	 */
	public static final String backRes="2";
	/**
	 * 银联同步响应
	 */
	public static final String frontRes="1";
	static {
		try {
			TCP_SERVER_PORT = Integer.valueOf(PropertiesUtil.getProperties(Contants.ENV_FILE).getProperty("tcpserver_port"));
			TCP_BIZTHREADSIZE = Integer.valueOf(PropertiesUtil.getProperties(Contants.ENV_FILE).getProperty("tcp_bizth_readsize"));
			HTTP_BACK_PORT = Integer.valueOf(PropertiesUtil.getProperties(Contants.ENV_FILE).getProperty("backserver_port"));
			isSSL = Boolean.valueOf(PropertiesUtil.getProperties(Contants.ENV_FILE).getProperty("HTTP_SSL"));
		} catch (Exception e) {
			logger.error("加载环境变量失败："+Contants.ENV_FILE+"\n"+e.getMessage());
		}
		logger.info("TCP端口:"+TCP_SERVER_PORT);
		logger.info("HTTP端口:"+HTTP_BACK_PORT);
	}
}
