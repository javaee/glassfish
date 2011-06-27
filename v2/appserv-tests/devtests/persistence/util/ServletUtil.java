/*
 * ServletUtil.java
 */

package util;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;

import javax.persistence.*;
import javax.transaction.*;
import javax.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class ServletUtil extends HttpServlet{

    public String tc;

    protected void doGet(HttpServletRequest request, 
			 HttpServletResponse response)
        throws ServletException, IOException {
        processAction(request, response);
    }
    
    protected void doPost(HttpServletRequest request, 
			  HttpServletResponse response)
        throws ServletException, IOException {
        processAction(request, response);
    }
    
    public boolean processParams(HttpServletRequest request) {
      try {
        if (request.getParameter("case") != null) {
           tc = request.getParameter("case");
	}
	return true;
     } catch(Exception ex) {
        System.err.println("Exception when processing the request params");
	ex.printStackTrace();
        return false;
     }

    }


    public void processAction(HttpServletRequest request,
			       HttpServletResponse response) 
      throws IOException{
    
        System.out.println("processing test driver request ... ");
        
	processParams(request);
        boolean status = false;
        System.out.println("tc:"+tc);

        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();
	out.println("TestCase: "+tc);   

        if (tc != null) {

           try {
               Class<?> c = getClass();
               Object t = this;

               Method[] allMethods = c.getDeclaredMethods();
               for ( Method m: allMethods ) {
                   String mname= m.getName();
                   if ( !mname.equals(tc.trim() ) ) {
                       continue;
                   }
 
                System.out.println("Invoking : " + mname );
                try {
                   m.setAccessible( true);
                   Object o = m.invoke( t );
                   System.out.println("Returned => " + (Boolean)o );
		   status = new Boolean((Boolean)o).booleanValue(); 
                   //Handle any methods thrown by method to be invoked
                } catch ( InvocationTargetException x ) {
                   Throwable cause = x.getCause();
 
                   System.err.format("invocation of %s failed: %s%n", mname, cause.getMessage() );
            } catch ( IllegalAccessException x ) {
               x.printStackTrace();
            }
  
           }
	   } catch ( Exception ex ) {
	      ex.printStackTrace();
            }

	  if (status) {
	    out.println(tc+":pass");
	  } else {
	    out.println(tc+":fail");
	  }

      }
    }
    
}
