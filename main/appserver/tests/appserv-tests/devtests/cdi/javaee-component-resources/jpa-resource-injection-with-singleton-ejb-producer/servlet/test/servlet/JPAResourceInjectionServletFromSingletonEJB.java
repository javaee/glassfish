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
import test.beans.artifacts.Preferred;
import test.beans.artifacts.ProducedViaProducerMethod;
import test.beans.artifacts.ProducedViaStaticField;
import test.util.JpaTest;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class JPAResourceInjectionServletFromSingletonEJB extends HttpServlet {

    @PersistenceUnit(unitName = "pu1")
    private EntityManagerFactory emf;

    @Inject
    @ProducedViaProducerMethod
    private EntityManagerFactory emf_producer;
    
    @Inject
    @ProducedViaStaticField
    private EntityManagerFactory emf_static;
    

    private @Resource
    UserTransaction utx;
    
    @Inject
    @Preferred
    TestBeanInterface tbi;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {
        PrintWriter writer = response.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";
        
        if (emf == null)
            msg += "Simple injection of EntityManagerFactory through " +
            		"@PersistenceUnit failed";
        String testcase = request.getParameter("testcase");
        System.out.println("testcase=" + testcase);
        
        String whichEMF = request.getParameter("whichemf");
        System.out.println("whichEMF=" + whichEMF);

        EntityManager em = null;
        if (whichEMF != null){
            EntityManagerFactory emf = null;
            if (whichEMF.equals("producer")){
                emf = emf_producer;
            } else if (whichEMF.equals("static")) {
                emf = emf_static;
            }
            //System.out.println("JPAResourceInjectionServlet::@PersistenceUnit " +
            //        "CDI EntityManagerFactory to run against=" + emf);
            em = emf.createEntityManager();
            System.out.println("JPAResourceInjectionServlet::createEM" +
                    "EntityManager=" + em);
        }
        
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

        writer.write(msg + "\n");

    }
}
