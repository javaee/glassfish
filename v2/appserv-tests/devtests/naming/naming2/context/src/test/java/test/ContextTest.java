package test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import org.junit.*;
import static org.junit.Assert.*;

public class ContextTest {
    private static final String NL = System.getProperty("line.separator");
    private static EJBContainer ejbContainer;
    private TestBean testBean;
    
    @BeforeClass public static void setUpClass() {
        ejbContainer = EJBContainer.createEJBContainer();
    }

    @AfterClass public static void tearDownClass() {
        ejbContainer.close();
    }

    @Before public void setUp() throws NamingException {
        testBean = (TestBean) ejbContainer.getContext().lookup("java:global/classes/TestBean");
    }

    @After public void tearDown() {
    }
    
    
    
    @Test public void listEmptyString2() throws NamingException {
        assertNotNull(testBean.listEmptyString());
    }
    
    @Test public void listEmptyString() throws NamingException {
        Context context = ejbContainer.getContext();
        NamingEnumeration<NameClassPair> list = context.list("");
        assertNotNull(list);
        System.out.println("Got NameClassPair: " + toString(list));
    }
    
    @Test public void listBindingsEmptyString2() throws NamingException {
        assertNotNull(testBean.listBindingsEmptyString());
    }
 
    @Test public void listBindingsEmptyString() throws NamingException {
        Context context = ejbContainer.getContext();
        NamingEnumeration<Binding> list = context.listBindings("");
        assertNotNull(list);
        System.out.println("Got Binding: " + toString(list));
    }
    
    @Ignore
    @Test public void listGlobal2() throws NamingException {
        assertNotNull(testBean.listGlobal());
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
        assertNotNull(testBean.listBindingsGlobal());
    }
    
    @Ignore
    @Test public void listBindingsGlobal() throws NamingException {
        Context context = ejbContainer.getContext();
        NamingEnumeration<Binding> list = context.listBindings("java:global");
        assertNotNull(list);
        System.out.println("Got Binding: " + toString(list));
    }
    
    @Test public void listJavaComp() throws NamingException {
        assertNotNull(testBean.listJavaComp());
    }
    
    @Test public void listBindingsJavaComp() throws NamingException {
        assertNotNull(testBean.listBindingsJavaComp());
    }
    
    @Test public void listJavaModule() throws NamingException {
        assertNotNull(testBean.listJavaModule());
    }
    
    @Test public void listBindingsJavaModule() throws NamingException {
        assertNotNull(testBean.listBindingsJavaModule());
    }
    
    @Test public void listJavaApp() throws NamingException {
        assertNotNull(testBean.listJavaApp());
    }
    
    @Test public void listBindingsJavaApp() throws NamingException {
        assertNotNull(testBean.listBindingsJavaApp());
    }
    
    @Test public void closeNamingEnumerations() throws NamingException {
        testBean.closeNamingEnumerations();
    }
    
    private String toString(NamingEnumeration<? extends NameClassPair> n) throws NamingException {
        StringBuilder sb = new StringBuilder();
        while(n.hasMore()) {
            NameClassPair x = n.next();
            sb.append(x).append(NL);
        }
        return sb.toString();
    }
    
}
