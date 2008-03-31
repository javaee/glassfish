package jsfinjection;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;
import javax.naming.*;

public class FilterTest implements Filter{
    
    private ServletContext context;
    private @Resource(name="jdbc/__default") DataSource ds;
//    private DataSource ds;
    
    public void destroy() {
        System.out.println("[Filter.destroy]");
    }    
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws java.io.IOException, javax.servlet.ServletException {
        System.out.println("[Filter.doFilter]");

        String msg = "PASS";
/*
        try {
            InitialContext ic = new InitialContext();
            ic.lookup("jdbc/__default");
            msg += "=:iclookup";
            System.out.println("XXX ic lookup DONE");
        } catch(Exception ex) {
        }
*/
        if (ds != null) {
            try {
                msg = "PASS-:" + ds.getLoginTimeout();
            } catch(Throwable ex) {
                msg = "FAIL-:" + ex.toString();
            }
        } else {
            msg = "FAIL-: ds is null";
        }
        System.out.println("[Filter.doFilter.msg = " + msg + "]");

        ((HttpServletRequest)request).getSession().setAttribute("FILTER", msg);
        filterChain.doFilter(request, response);
        
    }    
    
    
    public void init(javax.servlet.FilterConfig filterConfig) throws javax.servlet.ServletException {
        System.out.println("[Filter.init]");
        context = filterConfig.getServletContext();
    }
    
}
