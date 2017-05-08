/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package test.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.Preferred;
import test.beans.TestBean;
import test.beans.TransactionInterceptor;
import test.beans.Transactional;
import test.extension.MyExtension;



@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class PortableExtensionBeanManagerTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBean tb;
    
    @Inject
    BeanManager bm;
    
    String msg = "";


    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");

        if (tb == null)
            msg += "Injection of request scoped bean failed";

        tb.m1();
        if (!TransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke not called";
        tb.m2();
        if (TransactionInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + TransactionInterceptor.aroundInvokeInvocationCount;
        if (!TransactionInterceptor.errorMessage.trim().equals(""))
            msg += TransactionInterceptor.errorMessage;
        
        //check if our portable extension was called
        if (!MyExtension.beforeBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: " +
            		"beforeBeanDiscovery not called";

        if (!MyExtension.afterBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: " +
            		"afterBeanDiscovery not called or injection of BeanManager " +
            		"in an observer method failed";
        
        if (!MyExtension.processAnnotatedTypeCalled)
            msg += "Portable Extension lifecycle observer method: process " +
            		"annotated type not called";

        //BeanManager lookup
        if (bm == null)
            msg += "Injection of BeanManager into servlet failed";
        
        try {
            BeanManager bm1 = (BeanManager) (new InitialContext()).lookup("java:comp/BeanManager");
            if (bm1 == null) 
                msg += "lookup of BeanManager via component context failed";
        } catch (NamingException e) {
            e.printStackTrace();
            msg += "NamingException during lookup of BeanManager via component context";
        }
        
        //Using BeanManager
        check((bm.getBeans("test_named_bean").size() == 1), "Invalid number of Named Beans");
        check((bm.getBeans("duplicate_test_bean").size() == 0), "Invalid number of Duplicate Test Beans");
        check(bm.getELResolver() != null, "ELResolver is null");
        check(bm.isInterceptorBinding(Transactional.class), "Transactional is not an interceptor binding");
        check(bm.isNormalScope(RequestScoped.class), "RequestScoped is not normal scope");
        check(bm.isPassivatingScope(SessionScoped.class), "SessionScoped is not passivating scope");
        check(bm.isQualifier(Preferred.class), "Preferred is not a Qualifier");
        check(!(bm.isScope(Preferred.class)), "Preferred is a Scope class");
        check(bm.isScope(ConversationScoped.class), "ConversationScoped is not a Scope class");
        writer.write(msg + "\n");
    }


    private void check(boolean condition, String errorMessage) {
        if(!condition){
            msg += errorMessage;
        }
    }

}
