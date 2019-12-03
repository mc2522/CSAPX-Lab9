import java.io.*;
import java.util.*;

/**
 * This class represents the Quadtree data structure used to compress raw
 * grayscale images and uncompress back.  Conceptually, the tree is
 * a collection of QTNode's.  A QTNode either holds a grayscale image
 * value (0-255), or QUAD_SPLIT, meaning the node is split into four
 * sub-nodes that are equally sized sub-regions that divide up the
 * current space.
 *
 * To learn more about quadtrees:
 *      https://en.wikipedia.org/wiki/Quadtree
 *
 * @author Sean Strout @ RIT
 * @author Mike Cao
 */
public class QTree {
    /** the value of a node that indicates it is split into 4 sub-regions */
    public final static int QUAD_SPLIT = -1;

    /** the root node in the tree */
    private QTNode root;

    /** the square dimension of the tree */
    private int DIM;

    /**  the raw image */
    private int image[][];

    /** the size of the raw image */
    private int rawSize;

    /** the size of the compressed image */
    private int compressedSize;

    /**
     * Create an initially empty tree.
     */
    public QTree() {
        this.root = null;
        this.DIM = 0;
        this.image = null;
        this.rawSize = 0;
        this.compressedSize = 0;
    }

    /**
     * Get the images square dimension.
     *
     * @return the square dimension
     */
    public int getDim() { return this.DIM; }

    /** Get the raw image.
     *
     * @return the raw image
     */
    public int[][] getImage(){ return this.image; }

    /**
     * Get the size of the raw image.
     *
     * @return raw image size
     */
    public int getRawSize() { return this.rawSize; }

    /**
     * Get the size of the compressed image.
     *
     * @return compressed image size
     */
    public int getCompressedSize() { return this.compressedSize; }

    /**
     * A private helper routine for parsing the compressed image into
     * a tree of nodes.  When parsing through the values, there are
     * two cases:
     *
     * 1. The value is a grayscale color (0-255).  In this case
     * return a node containing the value.
     *
     * 2. The value is QUAD_SPLIT.  The node must be split into
     * four sub-regions.  Each sub-region is attained by recursively
     * calling this routine.  A node containing these four sub-regions
     * is returned.
     *
     * @param values the values in the compressed image
     * @return a node that encapsulates this portion of the compressed
     * image
     * @throws QTException if there are not enough values in the
     * compressed image
     */
    private QTNode parse(List<Integer> values) throws QTException {
        if (values.size() == 0) {
            throw new QTException("QTException: Error uncompressing. Not enough data.");
        }

        //Gets the first value of the list.
        int value = values.remove(0);

        //If the value is not -1, return a node of that value, otherwise, parse recursively.
        if (value != QUAD_SPLIT) {
            return new QTNode(value);
        } else {
            return new QTNode(value, parse(values), parse(values), parse(values), parse(values));
        }
    }

    /**
     * This is the core routine for uncompressing an image stored in a tree
     * into its raw image (a 2-D array of grayscale values (0-255).
     * It is called by the public uncompress routine.
     * The main idea is that we are working with a tree whose root represents the
     * entire 2^n x 2^n image.  There are two cases:
     *
     * 1. The node is not split.  We can write out the corresponding
     * "block" of values into the raw image array based on the size
     * of the region
     *
     * 2. The node is split.  We must recursively call ourselves with the
     * the four sub-regions.  Take note of the pattern for representing the
     * starting coordinate of the four sub-regions of a 4x4 grid:
     *      - upper left: (0, 0)
     *      - upper right: (0, 1)
     *      - lower left: (1, 0)
     *      - lower right: (1, 1)
     * We can generalize this pattern by computing the offset and adding
     * it to the starting row and column in the appropriate places
     * (there is a 1).
     *
     * @param node the node to uncompress
     * @param size the size of the square region this node represents
     * @param start the starting coordinate this row represents in the image
     */
    private void uncompress(QTNode node, int size, Coordinate start) {
        //If the node value is not -1, set the image pixels to the value.
        if (node.getVal() != QUAD_SPLIT) {
            for (int r = start.getRow(); r < start.getRow() + size; r++) {
                for (int c = start.getCol(); c < start.getCol() + size; c++) {
                    image[r][c] = node.getVal();
                }
            }
            //Return so the method doesn't continue.
            return;
        }

        //Uncompress recursively if the node value is -1.

        //Upper left
        uncompress(node.getUpperLeft(), size/2, new Coordinate(start.getRow(), start.getCol()));
        //Upper right
        uncompress(node.getUpperRight(), size/2, new Coordinate(start.getRow(), start.getCol() + size/2));
        //Lower left
        uncompress(node.getLowerLeft(), size/2, new Coordinate(start.getRow() + size/2, start.getCol()));
        //Lower right
        uncompress(node.getLowerRight(), size/2, new Coordinate(start.getRow() + size/2, start.getCol() + size/2));
    }

    /**
     * Uncompress a RIT compressed file.  This is the public facing routine
     * meant to be used by a client to uncompress an image for displaying.
     *
     * The file is expected to be 2^n x 2^n pixels.  The first line in
     * the file is its size (number of values).  The remaining lines are
     * the values in the compressed image, one per line, of "size" lines.
     *
     * Once this routine completes, the raw image of grayscale values (0-255)
     * is stored internally and can be retrieved by the client using getImage().
     *
     * @param filename the name of the compressed file
     * @throws IOException if there are issues working with the compressed file
     * @throws QTException if there are issues parsing the data in the file
     */
    public void uncompress(String filename) throws IOException, QTException {
        try {
            //Bufferedreader reading the filename.
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            //Reads the first line, which is the compressed size.
            this.rawSize = Integer.parseInt(reader.readLine());
            //Set compressed size to 1 since first line is read already.
            this.compressedSize = 1;
            //Dimension of the image.
            this.DIM = (int) Math.sqrt(this.rawSize);

            //Creates an arraylist to put all the values in the file in.
            ArrayList<Integer> numbers = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                numbers.add(Integer.parseInt(line));
            }

            //Sets to root to the method call of numbers.
            this.root = parse(numbers);

            //Sets the image to a new 2D array of size DIMxDIM.
            this.image = new int[this.DIM][this.DIM];
            //Run the uncompress procedures.
            uncompress(this.root, this.DIM, new Coordinate(0, 0));

            //Close the bufferedreader.
            reader.close();
        } catch (FileNotFoundException e) {
            System.err.println("java.io.FileNotFoundException: data-file (No such file or directory");
            System.exit(-1);
        }
    }

    /**
     * The private writer is a recursive helper routine that writes out the
     * compressed image.  It goes through the tree in preorder fashion
     * writing out the values of each node as they are encountered.
     *
     * @param node the current node in the tree
     * @param writer the writer to write the node data out to
     * @throws IOException if there are issues with the writer
     */
    private void write(QTNode node, BufferedWriter writer) throws IOException {
        //Add one to compressedSize everytime something is written to the outFile.
        compressedSize++;

        //Write the node value.
        writer.write(node.getVal() + "\n");
        writer.flush();

        ///If the node value is -1, recurses itself with upper left, upper right, lower left, lower right.
        if (node.getVal() == QUAD_SPLIT) {
            write(node.getUpperLeft(), writer);
            write(node.getUpperRight(), writer);
            write(node.getLowerLeft(), writer);
            write(node.getLowerRight(), writer);
        }
    }

    /**
     * Write the compressed image to the output file.  This routine is meant to be
     * called from a client after it has been compressed
     *
     * @rit.pre client has called compress() to compress the input file
     * @param outFile the name of the file to write the compressed image to
     * @throws IOException any errors involved with writing the file out
     * @throws QTException if the file has not been compressed yet
     */
    public void write(String outFile) throws IOException, QTException {
        if (this.root == null)
            throw new QTException("QTException: Error writing compressed file. File has not been compressed.");

        try {
            //Writes to the outFile.
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

            //Writes the size of the image.
            writer.write((this.DIM * this.DIM) + "\n");
            compressedSize++;
            writer.flush();

            //Calls the write method with the root.
            write(this.root, writer);

            //Closes the BufferedWriter.
            writer.close();
        } catch (FileNotFoundException e) {
            System.err.println("java.io.FileNotFoundException: out-file (Permission denied)");
            System.exit(-1);
        }
    }

    /**
     * Check to see whether a region in the raw image contains the same value.
     * This routine is used by the private compress routine so that it can
     * construct the nodes in the tree.
     *
     * @param start the starting coordinate in the region
     * @param size the size of the region
     * @return whether the region can be compressed or not
     */
    private boolean canCompressBlock(Coordinate start, int size) {
        //The dimensions of the image.
        int DIM = (int)Math.sqrt(size);

        //Gets the first pixel of the image.
        int initialValue = image[start.getRow()][start.getCol()];

        //Iterates through the image and checks if all the values are the same. If not, return false.
        for (int r = start.getRow(); r < start.getRow() + DIM; r++) {
            for (int c = start.getCol(); c < start.getCol() + DIM; c++) {
                if (image[r][c] != initialValue)
                    return false;
            }
        }
        return true;
    }

    /**
     * This is the core compression routine.  Its job is to work over a region
     * of the image and compress it.  It is a recursive routine with two cases:
     *
     * 1. The entire region represented by this image has the same value, or
     * we are down to one pixel.  In either case, we can now create a node
     * that represents this.
     *
     * 2. If we can't compress at this level, we need to divide into 4
     * equally sized sub-regions and call ourselves again.  Just like with
     * uncompressing, we can compute the starting point of the four sub-regions
     * by using the starting point and size of the full region.
     *
     * @param start the start coordinate for this region
     * @param size the size this region represents
     * @return a node containing the compression information for the region
     */
    private QTNode compress(Coordinate start, int size) {
        //The dimensions of the image.
        int DIM = (int)Math.sqrt(size);

        //Return a node of the pixel if it passes canCompressBlock. Otherwise, return a new node with four new nodes of upper left, upper right, lower left, lower right.
        if (canCompressBlock(start, size))
            return new QTNode(this.image[start.getRow()][start.getCol()]);
        else {
            return new QTNode(
                    QUAD_SPLIT,
                    compress(new Coordinate(start.getRow(), start.getCol()), size/4),
                    compress(new Coordinate(start.getRow(), (start.getCol() + DIM/2)), size/4),
                    compress(new Coordinate((start.getRow() + DIM/2), start.getCol()), size/4),
                    compress(new Coordinate((start.getRow() + DIM/2), (start.getCol() + DIM/2)), size/4)
            );
        }
    }

    /**
     * Compress a raw image into the RIT format.  This routine is meant to be
     * called by a client.  It is expected to be passed a file which represents
     * the raw image.  It is ASCII formatted and contains a series of grayscale
     * values (0-255).  There is one value per line, and 2^n x 2^n total lines.
     *
     * @param inputFile the raw image file name
     * @throws IOException if there are issues working with the file
     */
    public void compress(String inputFile) throws IOException {
        try {
            Scanner scan = new Scanner(new File(inputFile));

            //List of numbers in the inputFile.
            ArrayList<Integer> numbers = new ArrayList<>();

            //Adds all the numbers in the inputFile to the numbers list.
            while(scan.hasNextInt()) {
                numbers.add(scan.nextInt());
            }

            //Sets the rawSize to the size of the numbers list.
            this.rawSize = numbers.size();

            //Creates the dimension of the image by square rooting the size.
            this.DIM = (int)Math.sqrt(this.rawSize);

            //Creates an image of DIMxDIM.
            this.image = new int[this.DIM][this.DIM];

            //Assigns the values in numbers in order to the image pixels.
            for (int r = 0; r < this.DIM; r++) {
                for (int c = 0; c < this.DIM; c++) {
                    this.image[r][c] = numbers.remove(0);
                }
            }

            scan.close();
        } catch (FileNotFoundException e) {
            System.err.println("java.io.FileNotFoundException: in-file (No such file or directory");
            System.exit(-1);
        }

        //Assigns the root to the method call of compress.
        this.root = compress(new Coordinate(0, 0), this.rawSize);
    }

    /**
     * A preorder (parent, left, right) traversal of a node.  It returns
     * a string which is empty if the node is null.  Otherwise
     * it returns a string that concatenates the current node's value
     * with the values of the 4 sub-regions (with spaces between).
     *
     * @param node the node being traversed on
     * @return the string of the node
     */
    private String preorder(QTNode node) {
        //If the node is empty, return an empty string. If the node value is not -1, return the value. Otherwise, calls preorder recursively.
        if (node == null) {
            return "";
        } else if (node.getVal() != QUAD_SPLIT) {
            return Integer.toString(node.getVal());
        } else {
            return Integer.toString(node.getVal()) +
                    " " +
                    preorder(node.getUpperLeft()) +
                    " " +
                    preorder(node.getUpperRight()) +
                    " " +
                    preorder(node.getLowerLeft()) +
                    " " +
                    preorder(node.getLowerRight());
        }
    }

    /**
     * Returns a string which is a preorder traversal of the tree.
     *
     * @return the qtree string representation
     */
    @Override
    public String toString() {
        return "QTree: " + preorder(this.root);
    }
}