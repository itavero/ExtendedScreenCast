package amnl.cast.extscreen.source;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Arno Moonen
 * @since 2015-03-21
 */
public class ScreenGrabber {
    private static ScreenGrabber ourInstance = new ScreenGrabber();

    public static ScreenGrabber getInstance() {
        return ourInstance;
    }

    private final Map<String, Screen> screens;

    private ScreenGrabber() {
        Set<Screen> localScreens = AwtScreen.getLocalScreens();
        screens = new HashMap<>(localScreens.size());
        for (Screen screen : localScreens) {
            screens.put(screen.getIdentifier(), screen);
        }
    }

    public Map<String, Screen> getScreens() {
        synchronized (screens) {
            return Collections.unmodifiableMap(screens);
        }
    }

    public Screen getScreen(final String identifier) {
        synchronized (screens) {
            if (!screens.containsKey(identifier)) {
                return null;
            }
            return screens.get(identifier);
        }
    }

    public BufferedImage getScreenCapture(final String identifier) {
        final Screen screen = getScreen(identifier);
        if (screen == null) {
            return null;
        }
        return getScreenCapture(screen);
    }

    public BufferedImage getScreenCapture(final Screen screen) {
        return screen.captureScreen();
    }

    public byte[] getScreenCapture(final String identifier, final String format) throws IOException {
        final Screen screen = getScreen(identifier);
        if (screen == null) {
            return null;
        }
        return getScreenCapture(screen, format);
    }

    public byte[] getScreenCapture(final Screen screen, final String format) throws IOException {
        final BufferedImage image = getScreenCapture(screen);
        return convertImage(image, format);
    }

    private byte[] convertImage(final BufferedImage input, final String format) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(input, format, output);
        return output.toByteArray();
    }
}