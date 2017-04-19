package test.servlet;
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Sun Microsystems, Inc. All rights reserved.
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
import test.beans.wbinflib.AnotherTestBeanInWebInfLib;
import test.beans.wbinflib.TestAlternativeBeanInWebInfLib;
import test.beans.wbinflib.TestBeanInWebInfLib;
import test.util.JpaTest;

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
    //We are injecting TestBeanInWebInfLib directly above. Since the alternative
    //TestBean is not enabled in the WAR's BDA(beans.xml), 
    //TestBeanInWebInfLib must be injected 
    
    
    @Inject AnotherTestBeanInWebInfLib atbiwil;
    //However in this case, when AnotherTestBeanInWebInfLib tries to inject
    //TestBeanInWebInfLib in its bean, it must inject TestAlternativeBeanInWebInfLib
    //as the alternative bean is enabled in the WEB-INF/lib's BDA (beans.xml) 

    /* Test lookup of BeanManager*/
    BeanManager bm_lookup;

    
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        String msg = "";
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

        //Ensure Alternative Beans enabled only in the context of web-inf/lib is
        //not visible in WAR's BM
        Set webinfLibAltBeans = bm_at_inj.getBeans(TestAlternativeBeanInWebInfLib.class,new AnnotationLiteral<Any>() {});
        if (webinfLibAltBeans.size() != 0) msg += "TestAlternativeBean in WEB-INF/lib is available via the WAR BeanManager";
        System.out.println("Test Bean from WEB-INF/lib via BeanManager:" + webinfLibAltBeans);
        
        //Test injection of a Bean in WEB-INF/lib beans into Servlet
        //and check that the Alternative bean is not called.
        //The alternative bean in web-inf/lib is not enabled in the WAR's beans.xml
        //and hence must not be visible.
        TestAlternativeBeanInWebInfLib.clearStatus(); //clear status
        
        String injectionOfBeanInWebInfLibResult = tbiwil.testInjection();
        System.out.println("injectionWithAlternative returned: " + injectionOfBeanInWebInfLibResult);
        if (injectionOfBeanInWebInfLibResult.equals ("Alternative")) {
            msg += "Expected that the original TestBeanInWebInfLib is called, " +
            		"but instead got " + injectionOfBeanInWebInfLibResult + " instead";
        } 
        
        if(TestAlternativeBeanInWebInfLib.ALTERNATIVE_BEAN_HAS_BEEN_CALLED) {
            msg += "Alternate Bean is called even though it is not enabled in the WAR's beans.xml";
        }

        //Test injection into a bean in web-inf/lib
        //In this case the alternative bean must be called, as it is enabled
        //in the library jar's beans.xml and the injection of the Bean
        //happens in the context of the library jar
        TestAlternativeBeanInWebInfLib.clearStatus(); //clear status
        String injectionWithAlternative2 = atbiwil.testInjection();
        System.out.println("injectionWithAlternative returned: " + injectionWithAlternative2);
        if (injectionWithAlternative2.equals ("Alternative")) {
            //test injection successful
        } else {
            msg += "Expected alternative, but got " + injectionWithAlternative2 + " instead";
        }

        if (!TestAlternativeBeanInWebInfLib.ALTERNATIVE_BEAN_HAS_BEEN_CALLED) {
            msg += "Alternative Bean enabled in WEB-INF/lib was not called " +
            		"when the injection happened in the context of a " +
            		"Bean in WEB-INF/lib where the alternative Bean was enabled";
        }

        
        msg += testEMInjection(req);
        
        writer.write(msg + "\n");
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
