package cn.com.bestpay.posp.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.com.bestpay.posp.netty.ApplicationMain;
import cn.com.bestpay.posp.netty.client.TransPospToUnipay;
import cn.com.bestpay.posp.netty.logic.DataAnalysis;
import cn.com.bestpay.posp.netty.module.Contants;
import cn.com.bestpay.posp.netty.module.NettyChannelMap;
import cn.com.bestpay.posp.netty.module.PospTcpChannel;
import cn.com.bestpay.posp.system.entity.TranDatas;

/**
 * Tcp 请求 处理
 * 
 * @author yzh
 * 
 */
public class PospTcpServerHandler extends SimpleChannelInboundHandler<Object> {
	private static final Logger logger = Logger.getLogger(PospTcpServerHandler.class);

	@Override
	@SuppressWarnings("unchecked")
	protected void channelRead0(ChannelHandlerContext ctx, Object data) throws Exception {
		SocketChannel sc = (SocketChannel) ctx.channel();
		String clientId = "";
		logger.debug(String.format("收到POSP请求:[%s]", data.toString()));
		ApplicationMain.setTrans(true);
		Map<String, String> map = new HashMap<String, String>();
		if (data instanceof String) {
			String s = data.toString().substring(3);
			map = DataAnalysis.convertResultStringToMap((String) s);
		} else if (data instanceof Map) {
			map = (Map<String, String>) data;
		} else if (data instanceof TranDatas) {
			TranDatas td = (TranDatas) data;
			map = td.getData();
			
			//测试返回
//			td.setClient("1");
//			map.put("respCode", "00");
//			sc.writeAndFlush(td);
//			logger.debug("原报文返回");

		} else {
			map.put("respCode", "98");
//			sc.writeAndFlush(map);
//			sc.close();
			logger.error(String.format("收到POSP异常数据请求：[%s] ", data.getClass().getSimpleName()));
			return;
		}
		
		if (NettyChannelMap.isExists(sc)) {
			clientId = NettyChannelMap.getKey(sc);
		} else {
			clientId = DataAnalysis.getPospCliendId(map);
		}
		logger.info(String.format("[%s] ： 收到POSP请求! ", clientId));
		PospTcpChannel channel = new PospTcpChannel();
		channel.setChannel(sc);
		NettyChannelMap.add(clientId, channel);
		Map<String, String> res = TransPospToUnipay.transDataToUnipay(map, clientId);

		// ByteArrayOutputStream out = new ByteArrayOutputStream();
		// ObjectOutputStream ot = new ObjectOutputStream(out);
		// ot.writeObject(res);
		// ot.flush();
		// ot.close();

		TranDatas td = new TranDatas();
		td.setData(res);
		td.setClient(Contants.frontRes);
		sc.writeAndFlush(td);
		logger.info(String.format("[%s] ： 银联同步响应，respCode:%s ", clientId, res.get("respCode")));

	}

	/**
	 * 异常关闭通道，同时异常线程池
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

		String clientId = NettyChannelMap.remove((SocketChannel) ctx.channel());
		logger.error(String.format("[%s] error： 异常关闭连接! ", clientId));
		cause.printStackTrace();// 捕捉异常信息
		ctx.close();// 出现异常时关闭channel

	}

}