package cn.com.bestpay.posp.netty.module;

import io.netty.channel.socket.SocketChannel;

/**
 * 
 * @author yzh
 *
 */
public class PospTcpChannel {
	/**
	 * 连接通道
	 */
	private SocketChannel channel;
	/**
	 * 是否已经接收后台通知
	 */
	private boolean backGet = false;
	/**
	 * 是否已经接受前台通知
	 */
	private boolean frontGet = false;
	/**
	 * 收到请求的时间
	 */
	private long startTime = System.currentTimeMillis();

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public boolean isBackGet() {
		return backGet;
	}

	public boolean isFrontGet() {
		return frontGet;
	}
	public void setBackGet(boolean backGet) {
		this.backGet = backGet;
	}

	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	public void setFrontGet(boolean frontGet) {
		this.frontGet = frontGet;
	}
}
