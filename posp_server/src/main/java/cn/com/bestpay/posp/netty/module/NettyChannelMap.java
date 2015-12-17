package cn.com.bestpay.posp.netty.module;

import io.netty.channel.socket.SocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * 存放POSP请求对象
 * 
 * @author yzh
 * 
 */
public class NettyChannelMap {
	private static final Logger logger = Logger
			.getLogger(NettyChannelMap.class);
	/**
	 * 请求对象容器
	 */
	private static Map<String, PospTcpChannel> map = new ConcurrentHashMap<String, PospTcpChannel>();
	/**
	 * 线程池最大值
	 */
	private static int THREAD_MAXCOUNT = Contants.TCP_BIZTHREADSIZE;

	/**
	 * 请求增加
	 * 
	 * @param clientId
	 *            标识符
	 * @param socketChannel
	 *            请求通道
	 */
	public static void add(String clientId, PospTcpChannel socketChannel) {
		if(isExists(socketChannel.getChannel())){
			return;
		}
		if (map.size() > THREAD_MAXCOUNT) {
			logger.error(String.format("[%s]： 线程池超载! ", clientId));
			socketChannel.getChannel().close();
		} else {
			map.put(clientId, socketChannel);
		}
	}
	/**
	 * 判断连接是否已经存在
	 * @param socketChannel
	 * @return
	 */
	public static boolean isExists(SocketChannel socketChannel){
		for (Map.Entry<String, PospTcpChannel> entry : map.entrySet()) {
			if (entry.getValue().getChannel() == socketChannel) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取指定请求
	 * 
	 * @param clientId
	 * @return
	 */
	public static PospTcpChannel get(SocketChannel socketChannel) {
		for (Map.Entry<String, PospTcpChannel> entry : map.entrySet()) {
			if (entry.getValue().getChannel() == socketChannel) {
				map.remove(entry.getKey());
				return entry.getValue();
			}
		}
		return null;
	}
	/**
	 * 获取指定请求
	 * 
	 * @param clientId
	 * @return
	 */
	public static PospTcpChannel get(String clientId) {
		return map.get(clientId);
	}
	public static Map<String, PospTcpChannel> getAllChannel(){
		return map;
	}
	/**
	 * 获取连接的标志
	 * @param socketChannel
	 * @return
	 */
	public static String getKey(SocketChannel socketChannel){
		for (Map.Entry<String, PospTcpChannel> entry : map.entrySet()) {
			if (entry.getValue().getChannel() == socketChannel) {
				return entry.getKey();
			}
		}
		return "channel not found";
	}
	/**
	 * 移除请求(在响应完成/超时断开/客户端断开时调用)
	 * 
	 * @param socketChannel
	 */
	public static String remove(SocketChannel socketChannel) {
		
		for (Map.Entry<String, PospTcpChannel> entry : map.entrySet()) {
			if (entry.getValue().getChannel() == socketChannel) {
				logger.info(String.format("[%s] info： remove channel! ", entry.getKey()));
				map.remove(entry.getKey());
				return entry.getKey();
			}
		}
		return "channel not found";
	}

	/**
	 * 关闭通道
	 * 
	 * @param socketChannel
	 */
	public static void close(SocketChannel socketChannel) {
		socketChannel.close();
		remove(socketChannel);
	}
}
