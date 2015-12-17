package cn.com.bestpay.posp.netty.tcpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.WriteTimeoutHandler;

import org.apache.log4j.Logger;

import cn.com.bestpay.posp.netty.handler.PospTcpServerHandler;
import cn.com.bestpay.posp.netty.module.Contants;

/**
 * 与POSP链接的TCP服务
 * 
 * @author yzh
 * 
 */
public class PospTCPServer implements Runnable{
	private static final Logger logger = Logger
			.getLogger(PospTCPServer.class);
	/** 用于分配处理业务线程的线程组个数 */
	protected static final int BIZGROUPSIZE = Runtime.getRuntime()
			.availableProcessors(); // 默认
	private static final EventLoopGroup bossGroup = new NioEventLoopGroup(
			BIZGROUPSIZE);
	private static final EventLoopGroup workerGroup = new NioEventLoopGroup(
			Contants.TCP_BIZTHREADSIZE);
	/**
	 * 启动服务
	 */
	public void run() {
		try{
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.option(ChannelOption.SO_BACKLOG, 128);
			bootstrap.option(ChannelOption.TCP_NODELAY, true);
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel socketChannel)
						throws Exception {
					ChannelPipeline p = socketChannel.pipeline();
					p.addLast(new WriteTimeoutHandler(1));
					p.addLast(new ObjectEncoder());
					p.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
					p.addLast(new PospTcpServerHandler());
				}
			});
			ChannelFuture f = bootstrap.bind(Contants.TCP_SERVER_PORT).sync();
			if (f.isSuccess()) {
				logger.info(String.format("TCP Server start! "));
			}
		}catch(Exception e){
			logger.error(String.format("TCP Server start error! "));
		}
	}
}
