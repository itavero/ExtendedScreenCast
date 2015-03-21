package amnl.cast.extscreen;

import amnl.cast.extscreen.server.DesktopServer;
import amnl.cast.extscreen.source.ScreenGrabber;
import su.litvak.chromecast.api.v2.Application;
import su.litvak.chromecast.api.v2.ChromeCast;
import su.litvak.chromecast.api.v2.Status;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author Arno Moonen
 * @since 2015-03-21
 */
public class SomeApp {

    public static void main(String[] args) throws IOException {
        final int serverPort = 7654;
        final DesktopServer desktopServer = new DesktopServer(false, serverPort);
        final Thread desktopServerThread = new Thread(desktopServer);
        desktopServerThread.start();

        System.out.println("Chromecast test app");

        ChromeCast chromecast = new ChromeCast("10.10.11.15");
        try {
            chromecast.connect();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Connected to " + chromecast.getName());

        final Status status = chromecast.getStatus();
        final String defaultMediaPlayer = "CC1AD845";
        final Application app;
        if (chromecast.isAppAvailable(defaultMediaPlayer)) {
            app = chromecast.launchApp(defaultMediaPlayer);
        } else {
            System.out.println("Can not start app");
            return;
        }

        System.out.println("Application loaded: " + app.name);

        final ScreenGrabber grabber = ScreenGrabber.getInstance();
        String screenIdentifier = null;
        for (final String id : grabber.getScreens().keySet()) {
            System.out.println("Available screen: " + id);
            screenIdentifier = id;
        }

        if (screenIdentifier != null) {

            final String screenAddress;
            {
                final StringBuilder builder = new StringBuilder("http://");
//                builder.append(InetAddress.getLocalHost().getHostAddress());
                builder.append("10.10.11.5");
                builder.append(':');
                builder.append(serverPort);
                builder.append("/screen/");
                builder.append(screenIdentifier);
                builder.append(".png");
                screenAddress = builder.toString();
            }
            System.out.println("Address: " + screenAddress);

            while (!Thread.currentThread().isInterrupted()) {
                chromecast.load(screenAddress + "?t=" + System.currentTimeMillis());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                }
            }

        } else {
            System.err.println("No screen address!");
        }

        try {
            desktopServerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        chromecast.stopApp();

        chromecast.disconnect();
    }
}