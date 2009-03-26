package test;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter("/mytest")
public class TestFilter implements Filter {
    String mesg = null;
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println(">>> filter.init");
        mesg = filterConfig.getInitParameter("mesg");
    }   

    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        System.out.println(">>> filter.doFilter");
        req.setAttribute("filterMessage", mesg);
        chain.doFilter(req, res);
    }

    public void destroy() {
        System.out.println(">>> filter.destroy");
    }
}
