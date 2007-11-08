package com.sun.enterprise.config;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.2 $
 */

public class ConfigBeanFactoryTest extends TestCase {

    public ConfigBeanFactoryTest(String name){
        super(name);
    }
    protected void setUp() throws Exception {
        Runtime.getRuntime().exec("cp domain.orig.xml. domain.xml");
    }

    protected void tearDown() throws Exception {
        Runtime.getRuntime().exec("rm -f domain.xml");
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            TestRunner.run(ConfigBeanFactoryTest.class);
        } else {
            TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new ConfigBeanFactoryTest(args[i]));
        }
        return ts;
    }
}
