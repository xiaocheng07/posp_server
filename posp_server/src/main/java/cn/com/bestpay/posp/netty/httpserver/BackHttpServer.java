package cn.com.bestpay.posp.netty.httpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.log4j.Logger;

import cn.com.bestpay.posp.netty.initializer.FrontHttpInitializer;
import cn.com.bestpay.posp.netty.module.Contants;

/**
 * 前台通知 接收 http服务
 * 
 * @author yzh
 * 
 */
public class BackHttpServer  implements Runnable{
	/**
	 * 是否使用安全网络连接
	 */
	public static boolean isSSL = false;
	private static final Logger logger = Logger.getLogger(BackHttpServer.class);
	/** 用于分配处理业务线程的线程组个数 */
	protected static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors(); // 默认
	/** 业务出现线程大小 */
	protected static final int BIZTHREADSIZE = Contants.TCP_BIZTHREADSIZE;
	private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
	private static final EventLoopGroup workerGroup = new NioEventLoopGroup(BIZTHREADSIZE);

	public BackHttpServer() {
		
	}

	public void run(){
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new FrontHttpInitializer());
			// 绑定端口、同步等待
			b.bind(Contants.HTTP_BACK_PORT).sync();
			logger.info("后台通知服务启动");
		}catch(Exception e){
			e.printStackTrace();
			logger.info("后台通知服务启动失败");
		}
	}
}
