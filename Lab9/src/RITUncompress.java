import java.sql.SQLOutput;

/**
 * Rich Image Tool uncompressor.  This program takes a compressed RIT file,
 * uncompresses it, and then displays the image using RITViewer.
 *
 * $ java RITUncompress filename.rit
 *
 * @author Sean Strout @ RIT
 */
public class RITUncompress {

    /**
     * The main routine.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: ImageTool filename");
            return;
        }

        try {
            // initialize the tree
            QTree tree = new QTree();

            // uncompress the tree
            tree.uncompress(args[0]);

            // print the tree in preorder
            System.out.println(tree);

            // create a separate viewer and pass it the raw image data
            RITViewer view = new RITViewer(tree.getImage(), tree.getDim());

            // finally display the image
            view.display(args[0]);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}