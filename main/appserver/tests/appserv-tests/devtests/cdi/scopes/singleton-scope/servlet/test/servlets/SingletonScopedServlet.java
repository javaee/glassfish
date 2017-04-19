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
package test.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.TestSessionScopedBean;
import test.beans.TestSingletonScopedBean;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class SingletonScopedServlet extends HttpServlet {
    @Inject
    TestSingletonScopedBean tb;
    @Inject
    TestSingletonScopedBean anotherInjectedSingletonInstance;

    @Inject
    BeanManager bm;
    BeanManager bm1;

    @Inject
    TestSessionScopedBean tssb;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";
        if (tb == null)
            msg += "Singleton pseudo-scope Bean injection into Servlet failed";
        if (bm == null)
            msg += "BeanManager Injection via @Inject failed";
        try {
            bm1 = (BeanManager) ((new InitialContext())
                    .lookup("java:comp/BeanManager"));
        } catch (Exception ex) {
            ex.printStackTrace();
            msg += "BeanManager Injection via component environment lookup failed";
        }
        if (bm1 == null)
            msg += "BeanManager Injection via component environment lookup failed";
        if (tb.getInstancesCount() > 1) 
            msg += "Singleton scoped bean must be created only once";
        if (!areInjectecedInstancesEqual(tb, anotherInjectedSingletonInstance))
            msg += "Two different injection of a Singleton pseudo-scoped Bean must point to the same bean instance";
        if (testIsClientProxy(tb, TestSingletonScopedBean.class))
            msg += "Beans with Singleton pseudo-scope should not have a proxy";
        if (testIsClientProxy(tssb.getSingletonScopedBean(),
                TestSingletonScopedBean.class))
            msg += "Non-serializable Beans with Singleton pseudo-scope injected via Instance<T> into a session scoped bean should not have a proxy";

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
