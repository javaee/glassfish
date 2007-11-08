import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Generic exception class to use for testing error page assertions.
 */

public class TestException extends Exception {

    /**
     * Construct an exception with no associated message.
     */
    public TestException() {
        super();
    }

    /**
     * Construct an exception with the associated message.
     *
     * @param message The associated message
     */
    public TestException(String message) {
        super(message);
    }
}
