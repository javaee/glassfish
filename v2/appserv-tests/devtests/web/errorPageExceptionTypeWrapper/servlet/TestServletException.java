import javax.servlet.ServletException;

public class TestServletException extends ServletException {

    public TestServletException(String message) {
        super(message);
    }

    public TestServletException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public TestServletException(Throwable rootCause) {
        super(rootCause);
    }
}
