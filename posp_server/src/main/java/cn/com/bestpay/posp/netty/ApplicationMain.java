package cn.com.bestpay.posp.netty;


import io.netty.channel.socket.SocketChannel;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import cn.com.bestpay.posp.netty.httpserver.BackHttpServer;
import cn.com.bestpay.posp.netty.module.NettyChannelMap;
import cn.com.bestpay.posp.netty.module.PospTcpChannel;
import cn.com.bestpay.posp.netty.tcpserver.PospTCPServer;

public class ApplicationMain{
	private static final Logger logger = Logger.getLogger(ApplicationMain.class);
	public static boolean hasTrans = false;
	private static boolean needInfo = true;
	
	/**
	 * 设置是否有交易
	 *
	 * @author yzh
	 *
	 * @time 2015-12-4 上午9:44:43
	 * @param flag
	 */
	public static void setTrans(boolean flag){
		hasTrans = flag;
		needInfo = true;
	}
	
	public static void main(String[] args) throws InterruptedException{
		new Thread(new BackHttpServer()).run();
		new Thread(new PospTCPServer()).run();
		
		while (true) {//每10秒检测
			TimeUnit.SECONDS.sleep(10);
			if(!hasTrans){
				if(needInfo){
					logger.info("暂时没有交易...");
				}
				needInfo = false;
				continue;
			}
			Map<String, PospTcpChannel> clientMap = NettyChannelMap.getAllChannel();
			logger.info(String.format("Client alive count:[%s]! ",clientMap.size()));
			if(clientMap.size()==0){//没有真正进行中的交易
				setTrans(false);
			}
			for (Map.Entry<String, PospTcpChannel> entry : clientMap.entrySet()) {
				SocketChannel channel = entry.getValue().getChannel();
				if (channel != null) {
//					channel.writeAndFlush(" 0 ");
				}
				if(!(channel.isActive()&&channel.isOpen())){//断开
					NettyChannelMap.remove(channel);
				}
				long start = entry.getValue().getStartTime();
				long now = System.currentTimeMillis();
				if((now-start)/1000>61){//60秒关闭
					NettyChannelMap.close(channel);
				}
			}
		}
	}

}
