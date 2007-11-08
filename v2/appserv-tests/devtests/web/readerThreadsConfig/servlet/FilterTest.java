package test;

import javax.servlet.*;
import javax.servlet.http.*;
public class FilterTest implements Filter{
    
    private ServletContext context;
    
    public void destroy() {
        System.out.println("[Filter.destroy]");
    }    
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws java.io.IOException, javax.servlet.ServletException {
        System.out.println("[Filter.doFilter]");
          
        ((HttpServletRequest)request).getSession().setAttribute("FILTER", "PASS");
        filterChain.doFilter(request, response);
        
    }    
    
    
    public void init(javax.servlet.FilterConfig filterConfig) throws javax.servlet.ServletException {
        System.out.println("[Filter.init]");
        context = filterConfig.getServletContext();
    }
    
}
