import java.net.MalformedURLException;
import java.net.URL;

public class ImageProducer {
    /**
     * Returns an Image object that can then be painted on the screen.
     * The url argument must specify an absolute URL. The name
     * argument is a specifier that is relative to the url argument.
     * <p>
     * This method always returns immediately, whether or not the
     * image exists. When this applet attempts to draw the image on
     * the screen, the data will be loaded. The graphics primitives
     * that draw the image will incrementally paint on the screen.
     *
     * @param  url  an absolute URL giving the base location of the image
     * @param  name the location of the image, relative to the url argument
     * @see Image
     */
    public Image getImage(URL url, String name) {
        try { // the image at the specified URL
            return getImage(new URL(url, name));
        } catch (MalformedURLException e) { // if url is invalid
            return null;
        }
    }

    private Image getImage(URL url) throws MalformedURLException {
        throw new MalformedURLException();
    }

    /**
     * @param  url  an absolute URL giving the base location of the image
     * @param  name the location of the image, relative to the url argument
     * @see Image
     */
    public Image getImageWithValidate(URL url, String name) {
        var exception = new IllegalArgumentException();
        if (name == null) { // if name is null
            throw exception;
        }
        if (url == null) { // if url is null
            throw exception;
        }
        try { // the image at the specified URL
            return getImage(new URL(url, name));
        } catch (MalformedURLException e) { // if url is invalid
            return null;
        }
    }


}
