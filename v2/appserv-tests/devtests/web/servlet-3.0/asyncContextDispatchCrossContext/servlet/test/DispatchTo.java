package test;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class DispatchTo extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        Enumeration<String> attrNames = req.getAttributeNames();
        if (attrNames == null) {
            throw new ServletException("Missing ASYNC dispatch related " +
                                       "request attributes");
        }

        int asyncRequestAttributeFound = 0;
        while (attrNames.hasMoreElements()){
            String attrName = attrNames.nextElement();
            if (AsyncContext.ASYNC_REQUEST_URI.equals(attrName)) {
                if (!"/fromContext/dispatchFrom".equals(
                        req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_REQUEST_URI +
                        " request attribute");
                }
                asyncRequestAttributeFound++;
            } else if (AsyncContext.ASYNC_CONTEXT_PATH.equals(attrName)) {
                if (!"/fromContext".equals(req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_CONTEXT_PATH +
                        " request attribute");
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
                if (!"/dispatchFrom".equals(
                        req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_SERVLET_PATH +
                        " request attribute");
                }
                asyncRequestAttributeFound++;
            } else if (AsyncContext.ASYNC_QUERY_STRING.equals(attrName)) {
                if (!"myname=myvalue".equals(req.getAttribute(attrName))) {
                    throw new ServletException("Wrong value for " +
                        AsyncContext.ASYNC_QUERY_STRING +
                        " request attribute");
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
