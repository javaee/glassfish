package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class DispatchTarget extends HttpServlet {

    private static final String EXPECTED_ASYNC_REQUEST_URI =
        "/web-async-context-dispatch/TestServlet";

    private static final String EXPECTED_ASYNC_SERVLET_PATH =
        "/TestServlet";

    private static final String EXPECTED_ASYNC_QUERY_STRING =
        "target=DispatchTargetWithPath";

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        Enumeration<String> attrNames = req.getAttributeNames();
        if (attrNames == null) {
            throw new ServletException("Missing ASYNC dispatch related " +
                                       "request attributes");
        }

        if (!"MYVALUE".equals(req.getAttribute("MYNAME"))) {
            throw new ServletException("Missing custom request attribute");
        }

        int asyncRequestAttributeFound = 0;
        while (attrNames.hasMoreElements()){
            String attrName = attrNames.nextElement();
            if (AsyncContext.ASYNC_REQUEST_URI.equals(attrName)) {
                if (!EXPECTED_ASYNC_REQUEST_URI.equals(
                        req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_REQUEST_URI +
                        " request attribute. Found: " +
                        req.getAttribute(attrName) + ", expected: " +
                        EXPECTED_ASYNC_REQUEST_URI);
                }
                asyncRequestAttributeFound++;
            } else if (AsyncContext.ASYNC_CONTEXT_PATH.equals(attrName)) {
                if (!getServletContext().getContextPath().equals(
                        req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_CONTEXT_PATH +
                        " request attribute. Found: " +
                        req.getAttribute(attrName) + ", expected: " +
                        getServletContext().getContextPath());
                }
                asyncRequestAttributeFound++;
            } else if (AsyncContext.ASYNC_PATH_INFO.equals(attrName)) {
                if (req.getAttribute(attrName) != null) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_PATH_INFO +
                        " request attribute");
                }
                asyncRequestAttributeFound++;
            } else if (AsyncContext.ASYNC_SERVLET_PATH.equals(attrName)) {
                if (!EXPECTED_ASYNC_SERVLET_PATH.equals(
                        req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_SERVLET_PATH +
                        " request attribute. Found " +
                        req.getAttribute(attrName) + ", expected: " +
                        EXPECTED_ASYNC_SERVLET_PATH);
                }
                asyncRequestAttributeFound++;
            } else if (AsyncContext.ASYNC_QUERY_STRING.equals(attrName)) {
                if (!EXPECTED_ASYNC_QUERY_STRING.equals(
                        req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_QUERY_STRING +
                        " request attribute. Found: " +
                        req.getAttribute(attrName) + ", expected: " +
                        EXPECTED_ASYNC_QUERY_STRING);
                }
                asyncRequestAttributeFound++;
            }
        }

        if (asyncRequestAttributeFound != 5) {
            throw new ServletException("Wrong number of ASYNC dispatch " +
                                       "related request attributes");
        }

        res.getWriter().println("Hello world");
    }
}
