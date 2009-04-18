package wftest4;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter(urlPatterns={ "/" }, dispatcherTypes= { DispatcherType.REQUEST })
public class WFTestFilter4 implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println(">>> WFTestFilter4.init");
    }   

    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        System.out.println(">>> WFTestFilter4.doFilter");
        String filterMessage = (String)req.getAttribute("filterMessage");
        if (filterMessage == null) {
            filterMessage = "";
        }
        filterMessage += "C";

        req.setAttribute("filterMessage", filterMessage);
        chain.doFilter(req, res);
    }

    public void destroy() {
        System.out.println(">>> WFTestFilter4.destroy");
    }
}
