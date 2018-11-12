/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package speech.asr.ws;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.TrustManagerFactory;
import java.io.Closeable;
import java.net.URI;
import java.security.KeyStore;

public final class WebSocketClient implements Closeable {
    static final String URL = System.getProperty("url", "wss://vtcc.ai/voice/api/asr/v1/ws/decode_online?content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)16000,+format=(string)S16LE,+channels=(int)1&token=anonymous");
    private static Log logger = LogFactory.getLog(WebSocketClient.class);
    private Channel channel;
    private String url;
    private EventLoopGroup executors = new NioEventLoopGroup();

    public WebSocketClient(String url) throws Exception {
        this.url = url;
        start();
    }


    public void start() throws Exception {
        URI uri = new URI(url);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            System.err.println("Only WS(S) is supported.");
            return;
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            sslCtx = SslContextBuilder.forClient().trustManager(tmf).build();
        } else {
            sslCtx = null;
        }
        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        final WsHandshakeHandler handler =
                new WsHandshakeHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

        Bootstrap b = new Bootstrap();
        b.group(executors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }
                        p.addLast(
                                new HttpClientCodec(),
                                new HttpObjectAggregator(8192),
                                WebSocketClientCompressionHandler.INSTANCE, handler);
                    }
                });
        this.channel = b.connect(uri.getHost(), port).sync().channel();
        handler.handshakeFuture().sync();
    }

    public ChannelFuture sendBinaryMessage(byte[] data, int offset, int length) {
        return channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(data, offset, length)));
    }

    public ChannelFuture sendBinaryMessage(byte[] data) {
        return channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(data)));
    }

    public void close() {
        executors.shutdownGracefully();
    }

    public void shutdownNow() {
        channel.close();
        executors.shutdownNow();
    }

    public <T> void addHandler(IResponseHandler<T> wsHandler) {
        SimpleChannelInboundHandler<T> handler = new SimpleChannelInboundHandler<T>() {
            boolean isCompleteCalled = false;

            @Override
            protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
                if (msg instanceof TextWebSocketFrame)
                    wsHandler.onMessage(msg);
                else if (msg instanceof CloseWebSocketFrame) {
                    if (!isCompleteCalled) {
                        wsHandler.onComplete();
                        isCompleteCalled = true;
                    }
                    ctx.channel().close();
                    ctx.executor().shutdownGracefully();
                }
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                wsHandler.onFailure(cause);
                ctx.close();
                ctx.executor().shutdownGracefully();
            }

            @Override
            public void channelInactive(ChannelHandlerContext ctx) {
                if (!isCompleteCalled) {
                    wsHandler.onComplete();
                    isCompleteCalled = true;
                }
            }
        };
        this.channel.pipeline().addLast(handler);
    }
}
