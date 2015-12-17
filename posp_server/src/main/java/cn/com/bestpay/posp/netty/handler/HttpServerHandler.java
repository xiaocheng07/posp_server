package cn.com.bestpay.posp.netty.handler;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.com.bestpay.posp.netty.logic.DataAnalysis;
import cn.com.bestpay.posp.netty.module.Contants;
import cn.com.bestpay.posp.netty.module.NettyChannelMap;
import cn.com.bestpay.posp.netty.module.PospTcpChannel;
import cn.com.bestpay.posp.system.entity.TranDatas;

@SuppressWarnings("deprecation")
public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {

	private static final Logger logger = Logger.getLogger(HttpServerHandler.class);

	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk

	private HttpPostRequestDecoder decoder;
	Map<String, String> postMap = new HashMap<String, String>();

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (decoder != null) {
			decoder.cleanFiles();
		}
	}
	public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
		HttpRequest fullHttpRequest = null;
		try {
			fullHttpRequest = (HttpRequest) msg;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("收到非Https的请求");
		}
		if (fullHttpRequest == null) {
			return;
		}
		String getUrl = "";
		try{
			QueryStringDecoder decoderQuery = new QueryStringDecoder(fullHttpRequest.getUri());
			Map<String, List<String>> uriAttributes = decoderQuery.parameters();
			for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
				for (String attrVal : attr.getValue()) {
					if(StringUtils.equals(attr.getKey(), "method")){
						getUrl = attrVal;
						break;
					}
				}
			}
			
			if (fullHttpRequest.getMethod().equals(HttpMethod.POST)) {// POST请求
				decoder = new HttpPostRequestDecoder(factory, fullHttpRequest);
				for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
					if (data != null) {
						try {
							postMap.put(data.getName(), getHttpData(data));
						} finally {
							data.release();
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			logger.error("Http请求解析出错");
		}
		String clientId = DataAnalysis.getPospCliendId(postMap);
		PospTcpChannel ptc = NettyChannelMap.get(clientId);
		if(ptc==null){
			logger.error(String.format("[%s]： 银联回调没有找到原交易! respData:%s",clientId,postMap.toString()));
		}else{
			if(StringUtils.equals(getUrl, "back")){
				logger.debug("收到银联后台通知：" + postMap.toString());
				ptc.setBackGet(true);
			}else if(StringUtils.equals(getUrl, "front")){
				logger.debug("收到银联前台通知：" + postMap.toString());
				ptc.setFrontGet(true);
			}
			logger.info(String.format("[%s]交易完成",clientId));
			SocketChannel sc = ptc.getChannel();
			TranDatas tr = new TranDatas();
			tr.setData(postMap);
			tr.setClient(Contants.backRes);
			sc.writeAndFlush(tr);
		}
		writeSusResponse(ctx.channel(),fullHttpRequest);
		ctx.channel().close();
	}
	/**
	 * 成功响应
	 *
	 * @author yzh
	 *
	 * @time 2015-12-1 下午4:11:57
	 * @param channel
	 * @param fullHttpRequest 
	 */
	private void writeSusResponse(Channel channel, HttpRequest fullHttpRequest) {
		StringBuffer responseContent = new StringBuffer("requested is available");
        ByteBuf buf = copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
        responseContent.setLength(0);
        boolean close = fullHttpRequest.headers().contains(CONNECTION, HttpHeaders.Values.CLOSE, true)
                || fullHttpRequest.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !fullHttpRequest.headers().contains(CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        if (!close) {
            response.headers().set(CONTENT_LENGTH, buf.readableBytes());
        }
        Set<Cookie> cookies;
        String value = fullHttpRequest.headers().get(COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = CookieDecoder.decode(value);
        }
        if (!cookies.isEmpty()) {
            for (Cookie cookie : cookies) {
                response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
            }
        }
        response.setStatus(HttpResponseStatus.OK);
        ChannelFuture future = channel.writeAndFlush(response);
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
	}
	private String getHttpData(InterfaceHttpData data) throws IOException {
		/**
		 * HttpDataType有三种类型 Attribute, FileUpload, InternalAttribute
		 */
		if (data.getHttpDataType() == HttpDataType.Attribute) {
			Attribute attribute = (Attribute) data;
			return attribute.getValue();
		} else {
			return "";
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("http请求处理异常");
		ctx.channel().close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		messageReceived(ctx, msg);
	}
}