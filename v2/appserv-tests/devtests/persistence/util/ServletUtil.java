/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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

/** ServletUtil.java
  * This program is generic servlet which process the 
  * HTTP request and invoke the 
  * proper methods based on the request parameters.
  *
  * @author      Sarada Kommalapati
  */


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
