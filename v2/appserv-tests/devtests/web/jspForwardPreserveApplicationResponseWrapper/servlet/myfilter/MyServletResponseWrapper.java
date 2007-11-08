package myfilter;

import javax.servlet.*;
import javax.servlet.http.*;

public class MyServletResponseWrapper extends HttpServletResponseWrapper {

    public MyServletResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    // Leverage all superclass methods
}
