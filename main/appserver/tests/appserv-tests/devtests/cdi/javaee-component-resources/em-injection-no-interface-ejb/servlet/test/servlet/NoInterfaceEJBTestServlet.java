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

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import test.beans.TestBeanInterface;
import test.beans.artifacts.InjectViaAtEJB;
import test.beans.artifacts.InjectViaAtInject;
import test.util.JpaTest;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class NoInterfaceEJBTestServlet extends HttpServlet {

    @Inject
    @InjectViaAtInject
    TestBeanInterface testBeanInject;

    @Inject
    @InjectViaAtEJB
    TestBeanInterface testBeanEJB;

    @Inject
    private EntityManager em;

    private @Resource
    UserTransaction utx;


    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {
        PrintWriter writer = response.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";
        //test EJB injection via @EJB
        if (!testBeanEJB.m2())
            msg += "Invocation on no-interface EJB -- obtained through @EJB -- (method defined in EJB) failed";
        if (!testBeanEJB.m1())
            msg += "Invocation on no-interface EJB -- obtained through @EJB -- (method defined in super class) failed";

        //test EJB injection via @Inject
        if (!testBeanInject.m2())
            msg += "Invocation on no-interface EJB -- obtained through @Inject -- (method defined in EJB)  failed";

        //TODO: This fails currently
        if (!testBeanInject.m1())
            msg += "Invocation on no-interface EJB -- obtained through @Inject -- (method defined in super class) failed";

        JpaTest jt = new JpaTest(em, utx);
        boolean status = jt.lazyLoadingInit();
        if (!status) msg += "Injection and use of EntityMaanger failed";
        status = jt.lazyLoadingByFind(1);
        if (!status) msg += "Injection and use of EntityMaanger lazy loading test failed";
        
        writer.write(msg + "\n");

    }
}
