package amnl.cast.extscreen.source;

import java.awt.image.BufferedImage;

/**
 * @author Arno Moonen
 * @since 2015-03-21
 */
public interface Screen {

    public String getIdentifier();

    public int getWidthInPixels();

    public int getHeightInPixels();

    public BufferedImage captureScreen();

}