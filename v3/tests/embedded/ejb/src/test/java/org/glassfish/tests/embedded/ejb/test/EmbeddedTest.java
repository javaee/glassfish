package org.glassfish.tests.embedded.ejb.test;

import org.junit.Test;
import org.junit.Assert;
import org.glassfish.tests.embedded.ejb.SampleEjb;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

/**
 * this test will use the ejb API testing.
 *
 * @author Jerome Dochez
 */
public class EmbeddedTest {

    @Test
    public void test() {
        Map<String, Object> p = new HashMap<String, Object>();
        File f = new File(System.getProperty("basedir"), "target");
        f = new File(f, "classes");
        p.put(EJBContainer.MODULES, f);

        try {
            EJBContainer c = EJBContainer.createEJBContainer(p);
            Context ic = c.getContext();
            try {
                System.out.println("Looking up EJB...");
                SampleEjb ejb = (SampleEjb) ic.lookup("java:global/classes/SampleEjb");
                if (ejb!=null) {
                    System.out.println("Invoking EJB...");
                    System.out.println(ejb.saySomething());
                    Assert.assertEquals(ejb.saySomething(), "Hello World");
                }
            } catch (Exception e) {
                System.out.println("ERROR calling EJB:");
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            System.out.println("Done calling EJB");
            c.close();            
        } catch(Exception e) {
            System.out.println("Error setting up EJB container");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
