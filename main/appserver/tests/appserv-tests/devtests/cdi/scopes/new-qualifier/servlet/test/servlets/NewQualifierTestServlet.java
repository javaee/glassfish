/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Sun Microsystems, Inc. All rights reserved.
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
package test.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.enterprise.inject.New;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.TestRequestScopedBean;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class NewQualifierTestServlet extends HttpServlet {

    @Inject
    TestRequestScopedBean trsb;
    
    @Inject
    TestRequestScopedBean anotherRefToRequestScopedBean;

    @Inject
    @New
    TestRequestScopedBean newRequestScopedBean;

    @Inject
    @New
    Instance<TestRequestScopedBean> newRequestScopedBeanProgrammaticLookup;
    
    @Inject
    @New(TestRequestScopedBean.class)
    Instance<TestRequestScopedBean> newRequestScopedBeanProgrammaticLookup2;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        // 2 to account for the two injections in this servlet
        // XXX: For some weld instantiates the request scoped bean thrice(twice
        // during
        // the instantiation of the servlet and once during the servicing of the
        // servlet request
        if (!(trsb.getInstantiationCount() == 3))
            msg += "Request scoped bean created more than the expected number of times";

        if (!areInjectecedInstancesEqual(trsb, anotherRefToRequestScopedBean)) 
            msg += "Two references to the same request scoped bean are not equal";
        
        if (areInjectecedInstancesEqual(trsb, newRequestScopedBean))
            msg += "Request scoped Bean injected with @New qualifier must not be equal to the normal Request scoped bean";

        if (!testIsClientProxy(trsb, TestRequestScopedBean.class))
            msg += "Request scoped beans must be injected as a client proxy";
        
        if(newRequestScopedBeanProgrammaticLookup.get() == null) 
            msg += "A new instance of Request Scoped Bean obtained through programmatic lookup failed";
        
        if(newRequestScopedBeanProgrammaticLookup2.get() == null) 
            msg += "A new(complex type specification scenario) instance of Request Scoped Bean obtained through programmatic lookup failed";

        writer.write(msg + "\n");
    }

    private boolean areInjectecedInstancesEqual(Object o1, Object o2) {
        return (o1.equals(o2)) & (o1 == o2);
    }

    // Tests if the bean instance is a client proxy
    private boolean testIsClientProxy(Object beanInstance, Class beanType) {
        boolean isSameClass = beanInstance.getClass().equals(beanType);
        boolean isProxyAssignable = beanType.isAssignableFrom(beanInstance
                .getClass());
        System.out.println(beanInstance + "whose class is "
                + beanInstance.getClass() + " is same class of " + beanType
                + " = " + isSameClass);
        System.out.println(beanType + " is assignable from " + beanInstance
                + " = " + isProxyAssignable);
        boolean isAClientProxy = !isSameClass && isProxyAssignable;
        return isAClientProxy;
    }

}
