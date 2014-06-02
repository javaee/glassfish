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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.inject.Inject;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.naming.InitialContext;

@WebServlet(name="mytest",
        urlPatterns={"/myurl"},
        initParams={ @WebInitParam(name="n1", value="v1"), @WebInitParam(name="n2", value="v2") } )
public class TestServlet extends HttpServlet {
    @Inject TestBean tb;
    @Inject BeanManager bm;
    @Inject TestBeanInWebInfLib tbiwil;

    BeanManager bm1;
    
    @Inject 
    private transient org.jboss.logging.Logger log;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0. ");
        String msg = "n1=" + getInitParameter("n1") +
            ", n2=" + getInitParameter("n2");

        if (tb == null) msg += "Bean injection into Servlet failed";
        if (tbiwil == null) msg += "Bean injection of a TestBean in WEB-INF/lib into Servlet failed";
        System.out.println("Test Bean from WEB-INF/lib=" + tbiwil);

        System.out.println("BeanManager is " + bm);
        System.out.println("BeanManager via lookup is " + bm1);
        if (bm == null) msg += "BeanManager Injection via @Inject failed";
        try {
            bm1 = (BeanManager)((new InitialContext()).lookup("java:comp/BeanManager"));
        } catch (Exception ex) {
            ex.printStackTrace();
            msg += "BeanManager Injection via component environment lookup failed";
        }
        if (bm1 == null) msg += "BeanManager Injection via component environment lookup failed";

        //Check if Beans in WAR(WEB-INF/classes) and WEB-INF/lib/*.jar are visible
        //via BeanManager of WAR
        Set warBeans = bm.getBeans(TestBean.class,new AnnotationLiteral<Any>() {});
        if (warBeans.size() != 1) msg += "TestBean in WAR is not available via the WAR BeanManager";
        
        Set webinfLibBeans = bm.getBeans(TestBeanInWebInfLib.class,new AnnotationLiteral<Any>() {});
        if (webinfLibBeans.size() != 1) msg += "TestBean in WEB-INF/lib is not available via the WAR BeanManager";
        System.out.println("Test Bean from WEB-INF/lib via BeanManager:" + webinfLibBeans);
        
        //Test injection into WEB-INF/lib beans
        msg += tbiwil.testInjection();
        
        writer.write("initParams: " + msg + "\n");
    }
}
