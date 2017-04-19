/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.acme.servlet;


import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.ejb.api.Hello;
import com.acme.util.TestDatabase;

@WebServlet(urlPatterns = "/HelloServlet", loadOnStartup = 1)

@SuppressWarnings("serial")
public class HelloServlet extends HttpServlet {
    String msg = "";
    
    @EJB(name = "java:module/m1", beanName = "HelloSingleton", beanInterface = Hello.class)
    Hello h;
    
    @PersistenceUnit(unitName = "pu1")
    @TestDatabase
    private EntityManagerFactory emf;

//    @Inject
//    private ResourcesProducer rp;
//    
//    @Inject
//    private TestDependentBeanInLib fb;
    
//    @Inject
//    private TestManagedBean tmb;
    
//    @Inject
//    private TestSessionScopedBeanInLib tssil;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("In HelloServlet::doGet");
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        
        checkForNull(emf, "Injection of EMF failed in Servlet");
        //ensure EMF works!
        emf.createEntityManager();
        
        //call Singleton EJB
        String response = h.hello();
        if(!response.equals(Hello.ALL_OK_STRING))
            msg += "Invocation of Hello Singeton EJB failed:msg=" + response;
        
//        if (!rp.isInjectionSuccessful())
//            msg += "Injection of a bean in lib directory into another " +
//            		"Bean in lib directory failed";
//        checkForNull(fb, "Injection of a bean that is placed in lib directory " +
//        		"into a Servlet that is placed in a WAR failed");
//        checkForNull(tmb, "Injection of a Managed bean that is placed in lib directory " +
//        "into a Servlet that is placed in a WAR failed");
//        checkForNull(tssil, "Injection of a session scoped Bean placed in lib dir into a " +
//            "into a servlet in that ear failed");
        
//        if (!rp.isInjectionSuccessful())
//            msg += "Injection of a bean in lib directory into another Bean " +
//            		"in lib directory failed";
        
//        if (!tmb.isInjectionSuccessful())
//            msg += "Injection of a Bean placed in lib dir into a " +
//            		"ManagedBean placed in lib dir failed";
        
        out.println(msg);
    }

    protected void checkForNull(Object o, String errorMessage){
        System.out.println("o=" + o);
        if (o == null) msg += " " + errorMessage;
    }
}
