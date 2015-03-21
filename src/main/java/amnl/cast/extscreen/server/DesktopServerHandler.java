package amnl.cast.extscreen.server;

import amnl.cast.extscreen.source.Screen;
import amnl.cast.extscreen.source.ScreenGrabber;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.URI;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Arno Moonen
 * @since 2015-03-21
 */
public class DesktopServerHandler extends ChannelInboundHandlerAdapter {

    public DesktopServerHandler() {
        System.out.println("DesktopServerHandler constructed");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            final URI uri = URI.create(req.getUri());
            final String path = uri.getPath();
            System.out.println(req.getMethod().name() + ' ' + path);

            final String prefix = "/screen/";
            if (!path.startsWith(prefix)) {
                System.err.println("Invalid URI");
                sendError(ctx, NOT_IMPLEMENTED);
                return;
            }

            if (HttpHeaders.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }

            final int extensionDot = path.lastIndexOf('.');
            if (extensionDot < prefix.length() || extensionDot == path.length()) {
                sendError(ctx, FORBIDDEN);
                return;
            }
            final String screenId = path.substring(prefix.length(), extensionDot);
            final String format = path.substring(extensionDot + 1).toLowerCase();

            final ScreenGrabber grabber = ScreenGrabber.getInstance();
            final Screen screen = grabber.getScreen(screenId);
            if (screen == null) {
                sendError(ctx, NOT_FOUND);
                for (final String identifier : grabber.getScreens().keySet()) {
                    System.out.println("Screen ID: " + identifier);
                }
                return;
            }

            final byte[] capture;
            try {
                capture = grabber.getScreenCapture(screen, format);
            } catch (IOException e) {
                sendError(ctx, INTERNAL_SERVER_ERROR);
                return;
            }

            boolean keepAlive = HttpHeaders.isKeepAlive(req);

            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(capture));
            response.headers().set(CONTENT_TYPE, "image/" + format);
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                ctx.write(response);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}