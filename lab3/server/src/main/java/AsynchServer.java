import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;

public class AsynchServer {

    private int port;

    public AsynchServer(int port) {
        this.port = port;
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(8192));
                            ch.pipeline().addLast(new MessageDecoder());
                            //ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new ServerInputHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            //  Bind and start to accept incoming connections
            ChannelFuture f = bootstrap.bind(port).sync();
            System.out.println("Сервер запущен");


            //  Wait until the server socket is closed.
            //  In this example, this does not happen, but you can do that to gracefully
            //  shut down your server.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
