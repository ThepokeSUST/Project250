import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class FXUtils {

    private static BufferedImage matToBufferedImage(Mat original) {
        MatOfByte mob = new MatOfByte();
        // Encodes the Mat into a byte array representing a JPG image
        Imgcodecs.imencode(".jpg", original, mob);
        try {
            // Reads the image from the byte array stream
            return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
        } catch (IOException e) {
            System.err.println("Error converting Mat to BufferedImage: " + e.getMessage());
            return null;
        }
    }

    public static Image matToImage(Mat mat) {
        BufferedImage bufferedImage = matToBufferedImage(mat);
        // Converts the AWT BufferedImage to a JavaFX Image
        return bufferedImage != null ? SwingFXUtils.toFXImage(bufferedImage, null) : null;
    }
}