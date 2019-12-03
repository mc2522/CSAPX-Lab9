import javax.swing.*;
import java.awt.*;

/**
 * A class that visually displays a compressed image that was uncompressed
 * using QTree.
 *
 * @author Sean Strout @ RIT
 */
public class RITViewer extends JPanel {
    /**
     * the raw image of grayscale values (0-255)
     */
    private int image[][];

    /**
     * the square dimension of the image
     */
    private final int DIM;

    /**
     * Construct the viewer
     *
     * @param image the raw image
     * @param dim   the square dimension of image
     */
    public RITViewer(int image[][], int dim) {
        this.image = image;
        this.DIM = dim;
    }

    /**
     * Display the following image.  This causes paintComponent
     * to get called to load the image.
     *
     * @param title the title of the window
     */
    public void display(String title) {
        setPreferredSize(new Dimension(this.DIM, this.DIM));
        JFrame f = new JFrame();
        f.setTitle(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(this);
        f.pack();
        f.setVisible(true);
    }

    /**
     * Set the pixel values in the graphics context to the color
     * values in the image.
     *
     * @param g the graphics context we are drawing into
     */
    public void paintComponent(Graphics g) {
        for (int row = 0; row < this.DIM; row++) {
            for (int col = 0; col < this.DIM; col++) {
                int c = image[row][col];
                Color color = new Color(c, c, c);
                g.setColor(color);
                g.fillRect(col, row, 1, 1);
            }
        }
    }
}
