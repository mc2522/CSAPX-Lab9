/**
 * A class used to communicate errors with operations involving the QTree
 * and the files it uses for compressing and uncompressing.
 *
 * @author Sean Strout @ RIT
 */
public class QTException extends Exception {
    /**
     * Create a new QTException
     * @param msg the message associated with the exception
     */
    public QTException(String msg) {
        super(msg);
    }
}
