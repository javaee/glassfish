/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
