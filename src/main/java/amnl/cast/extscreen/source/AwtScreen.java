package amnl.cast.extscreen.source;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Arno Moonen
 * @since 2015-03-21
 */
public class AwtScreen implements Screen {

    private final GraphicsDevice device;
    private final Robot robot;
    private final String identifier;

    public AwtScreen(GraphicsDevice device) throws AWTException {
        this.device = device;
        robot = new Robot(device);
        identifier = Base64.getUrlEncoder().encodeToString(device.getIDstring().getBytes(Charset.defaultCharset()));
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getWidthInPixels() {
        return device.getDisplayMode().getWidth();
    }

    public int getHeightInPixels() {
        return device.getDisplayMode().getHeight();
    }

    public BufferedImage captureScreen() {
        final Rectangle fullScreen = new Rectangle(getWidthInPixels(), getHeightInPixels());
        return robot.createScreenCapture(fullScreen);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof AwtScreen)) {
            return false;
        }

        AwtScreen awtScreen = (AwtScreen) object;
        return device.equals(awtScreen.device);
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }

    public static Set<Screen> getLocalScreens() {
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

        final Set<Screen> screens = new HashSet<>(devices.length);
        for (GraphicsDevice device : devices) {
            try {
                screens.add(new AwtScreen(device));
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }

        return screens;
    }
}