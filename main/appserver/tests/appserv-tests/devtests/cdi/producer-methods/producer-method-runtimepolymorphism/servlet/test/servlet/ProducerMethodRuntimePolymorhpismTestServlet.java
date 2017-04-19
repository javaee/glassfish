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
package test.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.CreditCardPaymentStrategy;
import test.beans.PaymentStrategy;
import test.beans.Preferred_CreatedProgrammatically;
import test.beans.Preferred_CreatedViaInjection;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class ProducerMethodRuntimePolymorhpismTestServlet extends HttpServlet {

    @Inject
    @Preferred_CreatedProgrammatically
    PaymentStrategy payCreate;

    @Inject
    @Preferred_CreatedViaInjection
    PaymentStrategy payInject;

    @Inject
    @Preferred_CreatedViaInjection
    PaymentStrategy payInject2;
    
    @Inject
    CreditCardPaymentStrategy ccps;// This should be the request-scoped
                                   // instance.

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        if (payCreate == null)
            msg += "Bean injection into Servlet of Bean created through "
                    + "programmatic instantiation in producer method failed";

        if (payInject == null)
            msg += "Bean injection into Servlet of Bean created through "
                    + "injection into producer method failed";

        if (!(payCreate instanceof PaymentStrategy))
            msg += "Bean runtime polymorphism in producer method " +
            		"in Preferences failed";
        if (!(payInject instanceof PaymentStrategy))
            msg += "Bean runtime polymorphism in producer method(dep injection " +
            		"in method parameters in Preferences failed";

        if (areInjectedInstancesEqual(ccps, payInject))
            msg += "Use of @New to create new Dependent object while injecting " +
            		"in producer method failed";

        if (!areInjectedInstancesEqual(payInject, payInject2))
            msg += "Session-scoped producer method created Bean injected in " +
            		"different injection points are not equal";

        writer.write(msg + "\n");
    }

    private boolean areInjectedInstancesEqual(Object o1, Object o2) {
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
