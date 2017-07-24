/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class ContextTest {
    private static final String NL = System.getProperty("line.separator");
    private static EJBContainer ejbContainer;
    private TestBean testBean;
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    @Rule
    public TestWatcher reportWatcher=new ReportWatcher(stat, "Naming::naming2::ContextTest");
    
    @Rule public TestName testName = new TestName();
    
    @BeforeClass public static void setUpClass() {
        ejbContainer = EJBContainer.createEJBContainer();
    }

    @AfterClass public static void tearDownClass() {
        if(ejbContainer != null)
            ejbContainer.close();
    }

    @AfterClass
    public static void printSummary(){
        stat.printSummary();
    }

    @Before public void setUp() throws NamingException {
        testBean = (TestBean) ejbContainer.getContext().lookup("java:global/classes/TestBean");
        System.out.printf("%n----------------- Starting test %s -------------------%n", testName.getMethodName());
    }

    @After public void tearDown() {
        System.out.printf("%n================= Finishing test   ================================================%n%n");
    }

    @Test public void lookupWithWLInitialContextFactory() throws NamingException {
        TestBean b = testBean.lookupWithWLInitialContextFactory("java:global/classes/TestBean");
        DataSource ds = testBean.lookupWithWLInitialContextFactory("jdbc/__default");
        System.out.println("TestBean from lookup: " + b);
        System.out.println("DataSource from lookup: " + ds);
    }
    
    @Test public void listEmptyString2() throws NamingException {
        System.out.println(testBean.listEmptyString().toString());
    }
    
    @Test public void listEmptyString() throws NamingException {
        Context context = ejbContainer.getContext();
        NamingEnumeration<NameClassPair> list = context.list("");
        assertNotNull(list);
        System.out.println("Got NameClassPair: " + toString(list));
    }
    
    @Test public void listBindingsEmptyString2() throws NamingException {
        System.out.println(testBean.listBindingsEmptyString().toString());
    }
 
    @Test public void listBindingsEmptyString() throws NamingException {
        Context context = ejbContainer.getContext();
        NamingEnumeration<Binding> list = context.listBindings("");
        assertNotNull(list);
        System.out.println("Got Binding: " + toString(list));
    }
    
    @Ignore
    @Test public void listGlobal2() throws NamingException {
        System.out.println(testBean.listGlobal().toString());
    }
    
    @Ignore //got null componentId
    @Test public void listGlobal() throws NamingException {
        Context context = ejbContainer.getContext();
        NamingEnumeration<NameClassPair> list = context.list("java:global");
        assertNotNull(list);
        System.out.println("Got NameClassPair: " + toString(list));
    }
    
    @Ignore
    @Test public void listBindingsGlobal2() throws NamingException {
        System.out.println(testBean.listBindingsGlobal().toString());
    }
    
    @Ignore
    @Test public void listBindingsGlobal() throws NamingException {
        Context context = ejbContainer.getContext();
        NamingEnumeration<Binding> list = context.listBindings("java:global");
        assertNotNull(list);
        System.out.println("Got Binding: " + toString(list));
    }
    
    @Test public void listJavaComp() throws NamingException {
        System.out.println(testBean.listJavaComp().toString());
    }
    
    @Test public void listBindingsJavaComp() throws NamingException {
        System.out.println(testBean.listBindingsJavaComp().toString());
    }
    
    @Test public void listJavaModule() throws NamingException {
        System.out.println(testBean.listJavaModule().toString());
    }
    
    @Test public void listBindingsJavaModule() throws NamingException {
        System.out.println(testBean.listBindingsJavaModule().toString());
    }
    
    @Test public void listJavaApp() throws NamingException {
        System.out.println(testBean.listJavaApp().toString());
    }
    
    @Test public void listBindingsJavaApp() throws NamingException {
        System.out.println(testBean.listBindingsJavaApp().toString());
    }
    
    @Test public void closeNamingEnumerations() throws NamingException {
        testBean.closeNamingEnumerations();
    }
    
	@Test
	public void getIsInAppClientContainerFromEJB() throws NamingException {
		Boolean isACC = testBean.getIsInAppClientContainer();
		assertFalse(isACC);
		System.out.println("get java:comp/InAppClientContainer from EJB:"
				+ isACC);
	}
	
	@Test
	public void getIsInAppClientContainerFromSEClient() throws NamingException {
		String jndiname = "java:comp/InAppClientContainer";
		Context context = new InitialContext();
		Boolean isACC = (Boolean) context.lookup(jndiname);
		assertFalse(isACC);
		System.out
				.println("get "+jndiname+" from java SE client:"
						+ isACC);
	}
	
    private String toString(NamingEnumeration<? extends NameClassPair> n) throws NamingException {
        StringBuilder sb = new StringBuilder();
        sb.append(n.toString()).append(NL);
        while(n.hasMore()) {  // test will fail with NPE if null
            NameClassPair x = n.next();
            sb.append(x).append(NL);
        }
        return sb.toString();
    }
    
}
