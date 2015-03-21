package amnl.cast.extscreen.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * @author Arno Moonen
 * @since 2015-03-21
 */
public class DesktopServer implements Runnable {

    private final boolean useSsl;
    private final int portNumber;

    public DesktopServer(boolean useSsl, int portNumber) {
        this.useSsl = useSsl;
        this.portNumber = portNumber;
    }

    private SslContext getSslContext() {
        if (!useSsl) {
            return null;
        }
        SslContext context = null;
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            context = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
        } catch (CertificateException | SSLException e) {
            e.printStackTrace();
        }
        return context;
    }

    @Override
    public void run() {
        final SslContext sslCtx = getSslContext();

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new DesktopServerInitializer(sslCtx));

            Channel ch = b.bind(portNumber).sync().channel();
            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
