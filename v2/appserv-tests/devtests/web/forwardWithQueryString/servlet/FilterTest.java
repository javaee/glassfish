package test;

import javax.servlet.*;
import javax.servlet.http.*;
public class FilterTest implements Filter{
    
    private ServletContext context;
    
    public void destroy() {
    }    
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws java.io.IOException, javax.servlet.ServletException {
        filterChain.doFilter(request, response);
    }    
    
    
    public void init(javax.servlet.FilterConfig filterConfig) throws javax.servlet.ServletException {
    }
    
}
