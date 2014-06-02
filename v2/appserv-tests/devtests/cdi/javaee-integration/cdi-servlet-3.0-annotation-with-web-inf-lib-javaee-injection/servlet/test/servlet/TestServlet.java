package test.servlet;
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
import java.util.Set;

import javax.annotation.Resource;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import test.beans.TestBean;
import test.beans.TestBeanInterface;
import test.beans.artifacts.Preferred;
import test.util.JpaTest;

import test.beans.wbinflib.TestBeanInWebInfLib;

@WebServlet(name="mytest",
        urlPatterns={"/myurl"},
        initParams={ @WebInitParam(name="n1", value="v1"), @WebInitParam(name="n2", value="v2") } )
public class TestServlet extends HttpServlet {

    /* Normal injection of Beans */
    @Inject 
    private transient org.jboss.logging.Logger log;
    @Inject BeanManager bm_at_inj;

    /*Injection of Java EE resources*/
    @PersistenceUnit(unitName = "pu1")
    private EntityManagerFactory emf_at_pu;

    @Inject //@TestDatabase
    private EntityManager emf_at_inj;

    private @Resource
    UserTransaction utx;
    
    @Inject @Preferred
    TestBeanInterface tbi;
    
    /* Injection of Beans from WEB-INF/lib */
    @Inject TestBeanInWebInfLib tbiwil;
    
    /* Test lookup of BeanManager*/
    BeanManager bm_lookup;

    
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0. ");
        String msg = "n1=" + getInitParameter("n1") +
            ", n2=" + getInitParameter("n2");

        if (tbi == null) msg += "Bean injection into Servlet failed";
        if (tbiwil == null) msg += "Bean injection of a TestBean in WEB-INF/lib into Servlet failed";
        System.out.println("Test Bean from WEB-INF/lib=" + tbiwil);

        System.out.println("BeanManager is " + bm_at_inj);
        System.out.println("BeanManager via lookup is " + bm_lookup);
        if (bm_at_inj == null) msg += "BeanManager Injection via @Inject failed";
        try {
            bm_lookup = (BeanManager)((new InitialContext()).lookup("java:comp/BeanManager"));
        } catch (Exception ex) {
            ex.printStackTrace();
            msg += "BeanManager Injection via component environment lookup failed";
        }
        if (bm_lookup == null) msg += "BeanManager Injection via component environment lookup failed";

        //Check if Beans in WAR(WEB-INF/classes) and WEB-INF/lib/*.jar are visible
        //via BeanManager of WAR
        Set warBeans = bm_at_inj.getBeans(TestBean.class,new AnnotationLiteral<Any>() {});
        if (warBeans.size() != 1) msg += "TestBean in WAR is not available via the WAR BeanManager";
        
        Set webinfLibBeans = bm_at_inj.getBeans(TestBeanInWebInfLib.class,new AnnotationLiteral<Any>() {});
        if (webinfLibBeans.size() != 1) msg += "TestBean in WEB-INF/lib is not available via the WAR BeanManager";
        System.out.println("Test Bean from WEB-INF/lib via BeanManager:" + webinfLibBeans);
        
        //Test injection into WEB-INF/lib beans
        msg += tbiwil.testInjection();
        
        msg += testEMInjection(req);
        
        writer.write("initParams: " + msg + "\n");
    }


    private String testEMInjection(HttpServletRequest request) {
        String msg = "";
        EntityManager em = emf_at_inj;
        System.out.println("JPAResourceInjectionServlet::createEM" +
                "EntityManager=" + em);
        String testcase = request.getParameter("testcase");
        System.out.println("testcase=" + testcase);

        if (testcase != null) {
            JpaTest jt = new JpaTest(em, utx);
            boolean status = false;
            if ("llinit".equals(testcase)) {
                status = jt.lazyLoadingInit();
            } else if ("llfind".equals(testcase)) {
                status = jt.lazyLoadingByFind(1);
            } else if ("llquery".equals(testcase)) {
                status = jt.lazyLoadingByQuery("Carla");
            } else if ("llinj".equals(testcase)){
                status = ((tbi != null) && 
                        (tbi.testDatasourceInjection().trim().length()==0));
            }
            if (status) {
                msg += "";// pass
            } else {
                msg += (testcase + ":fail");
            }
        }
        return msg;
    }
}
