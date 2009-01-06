package wftest;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.ServletFilter;

@ServletFilter(urlPatterns={ "/" }, dispatcherTypes= { DispatcherType.REQUEST })
public class WFTestFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println(">>> WFTestFilter.init");
    }   

    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        System.out.println(">>> WFTestFilter.doFilter");
        req.setAttribute("filterMessage", "WFTestFilterMesg");
        chain.doFilter(req, res);
    }

    public void destroy() {
        System.out.println(">>> WFTestFilter.destroy");
    }
}
