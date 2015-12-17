package cn.com.bestpay.posp.system.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * 传递数据对象
 * 
 * @author yzh
 * 
 * @time 2015-11-30 上午8:51:51
 */
public class TranDatas implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7905685707294440935L;
	
	private Map<String, String> data;
	private String client;

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	
}
