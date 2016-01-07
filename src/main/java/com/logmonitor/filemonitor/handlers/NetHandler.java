package com.logmonitor.filemonitor.handlers;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ConcurrentLinkedQueue;

public class NetHandler extends ChannelHandlerAdapter implements Handler {
	private String ip;
	private int port;
	private boolean running = false;
	private Thread thread = null;
	private ConcurrentLinkedQueue<String> dataList = new ConcurrentLinkedQueue<String>();
	
	private ChannelFuture channelFuture = null;
	
	public NetHandler(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public void notify(String data) {
		this.dataList.add(data);
	}

	public void run() {
		this.startAndKeepNettyClient();
	}
	
	private void startAndKeepNettyClient() {
		
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				socketChannel.pipeline().addLast(NetHandler.this);
			}				
		});
		try {
			this.channelFuture = bootstrap.connect(ip, port).sync();
			this.channelFuture.channel().closeFuture().sync();	
		} catch(InterruptedException e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
		}
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		while (running) {
			if (dataList.size() > 0) {
				String tmpData = dataList.poll();
				ByteBuf buf = ctx.alloc().buffer();
				buf.writeBytes(tmpData.getBytes());
				ctx.writeAndFlush(buf);
			}
		}
		ctx.close();
	}
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

	public void start() {
		if (running) {
			return;
		}
		this.thread = new Thread(this);
		running = true;
		this.thread.start();
	}

	public void stop() {
		if (!running) {
			return;
		}
		this.running = false;
	}
	
}
