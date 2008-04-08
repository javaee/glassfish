package com.sun.enterprise.naming.impl;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.JNDIBinding;
import com.sun.enterprise.naming.spi.NamingObjectFactory;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.InvocationManagerImpl;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() throws NamingException {
        NamingManager.setInitialContextFactoryBuilder(new GFInitialContextFactoryBuilder());
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

    public void testCreateNewInitialContext() {
        try {
            new InitialContext();
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testBind() {
        GlassfishNamingManager nm = null;
        try {
            InvocationManager im = new InvocationManagerImpl();
            nm = new GlassfishNamingManagerImpl();
            nm.publishObject("foo", "Hello: foo", false);
            InitialContext ic = new InitialContext();
            System.out.println("**lookup() ==> " + ic.lookup("foo"));
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        } finally {
            try {
                nm.unpublishObject("foo");
            } catch (Exception ex) {

            }
        }
    }

    public void testCachingNamingObjectFactory() {
        GlassfishNamingManager nm = null;
        try {
            InvocationManager im = new InvocationManagerImpl();
            nm = new GlassfishNamingManagerImpl();
            nm.publishObject("foo", "Hello: foo", false);
            InitialContext ic = new InitialContext();
            System.out.println("**lookup() ==> " + ic.lookup("foo"));

            NamingObjectFactory factory = new NamingObjectFactory() {
                private int counter = 1;

                public boolean isCreateResultCacheable() {
                    return true;
                }

                public Object create(Context ic) {
                    return ("FACTORY_Created: " + counter++);
                }

                public String toString() {
                    return "Huh? ";
                }
            };
            nm.publishObject("bar", factory, false);
            System.out.println("**lookup() ==> " + ic.lookup("bar"));
            System.out.println("**lookup() ==> " + ic.lookup("bar"));
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        } finally {
            try {
                nm.unpublishObject("foo");
                nm.unpublishObject("bar");
            } catch (Exception ex) {

            }
        }
    }

    private static class Binding
            implements JNDIBinding {
        String logicalName;
        Object value;

        public Binding(String logicalName, Object value) {
            this.logicalName = "java:comp/env/" + logicalName;
            this.value = value;
        }

        public String getName() {
            return logicalName;
        }

        public String getJndiName() {
            return null;
        }

        public Object getValue() {
            return value;
        }
    }

    public void testNonCachingNamingObjectFactory() {
        GlassfishNamingManagerImpl nm = null;
        InvocationManager im = new InvocationManagerImpl();
        ComponentInvocation inv = null;
        try {
            nm = new GlassfishNamingManagerImpl();
            nm.setInvocationManager(im);

            List<Binding> bindings =
                    new ArrayList<Binding>();

            NamingObjectFactory intFactory = new NamingObjectFactory() {
                private int counter = 1;

                public boolean isCreateResultCacheable() {
                    return false;
                }

                public Object create(Context ic) {
                    return new Integer(++counter);
                }

                public String toString() {
                    return "Huh? ";
                }
            };
            bindings.add(new Binding("conf/area", intFactory));
            bindings.add(new Binding("conf/location", "Santa Clara"));

            nm.bindToComponentNamespace("app1", "comp1", bindings);

            inv = new ComponentInvocation("comp1",
                    ComponentInvocation.ComponentInvocationType.EJB_INVOCATION,
                    null, null, null);
            im.preInvoke(inv);
            InitialContext ic = new InitialContext();
            System.out.println("**lookup(java:comp/env/conf/area) ==> " + ic.lookup("java:comp/env/conf/area"));
            System.out.println("**lookup(java:comp/env/conf/location) ==> " + ic.lookup("java:comp/env/conf/location"));

            NamingObjectFactory floatFactory = new NamingObjectFactory() {
                private int counter = 1;

                public boolean isCreateResultCacheable() {
                    return false;
                }

                public Object create(Context ic) {
                    return Float.valueOf(("7" + (++counter)) + "." + 2323);
                }

                public String toString() {
                    return "Huh? ";
                }
            };
            List<Binding> bindings2 =
                    new ArrayList<Binding>();
            bindings2.add(new Binding("conf/area", floatFactory));
            bindings2.add(new Binding("conf/location", "Santa Clara[14]"));

            nm.bindToComponentNamespace("app1", "comp2", bindings2);

            inv = new ComponentInvocation("comp2",
                    ComponentInvocation.ComponentInvocationType.EJB_INVOCATION,
                    null, null, null);
            im.preInvoke(inv);
            System.out.println("**lookup(java:comp/env/conf/area) ==> " + ic.lookup("java:comp/env/conf/area"));
            System.out.println("**lookup(java:comp/env/conf/location) ==> " + ic.lookup("java:comp/env/conf/location"));

            assert (true);
        } catch (InvocationException inEx) {
            inEx.printStackTrace();
            assert(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        } finally {
            try {
                im.postInvoke(inv);
                nm.unbindObjects("comp1");
            } catch (InvocationException inEx) {
                
            } catch (Exception ex) {

            }
        }
    }

}
