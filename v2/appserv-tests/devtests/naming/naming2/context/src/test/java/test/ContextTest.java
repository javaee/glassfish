package test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import static org.junit.Assert.assertNotNull;

public class ContextTest {
    private static final String NL = System.getProperty("line.separator");
    private static EJBContainer ejbContainer;
    private TestBean testBean;
    
    @Rule public TestName testName = new TestName();
    
    @BeforeClass public static void setUpClass() {
        ejbContainer = EJBContainer.createEJBContainer();
    }

    @AfterClass public static void tearDownClass() {
        if(ejbContainer != null)
            ejbContainer.close();
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
