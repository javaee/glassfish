package com.sun.s1asdev.ejb.util.methodmap.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import com.sun.ejb.containers.util.MethodMap;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Iterator;

public class MethodMapTest {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");
    
    private static final Class[] TEST_CLASSES = {
        java.lang.Object.class,
        MethodMapTest1.class, MethodMapTest2.class, MethodMapTest3.class, 
        MethodMapTest4.class, MethodMapTest3.class, MethodMapTest6.class,
        MethodMapTest.class, javax.ejb.EJBHome.class, 
        javax.ejb.EJBObject.class, javax.ejb.EJBLocalObject.class,
        javax.ejb.EJBLocalHome.class, javax.ejb.SessionBean.class,
        javax.ejb.EnterpriseBean.class, java.util.Map.class,
        javax.jms.QueueConnection.class, javax.jms.QueueSession.class,
        javax.jms.Session.class, java.util.Date.class,
        javax.swing.Action.class, javax.swing.AbstractAction.class,
        javax.swing.JComboBox.class, javax.swing.JTextArea.class        

    };

    public static void main (String[] args) {

        stat.addDescription("ejb-util-methodmap");
        MethodMapTest test = new MethodMapTest(args);
        test.doTest();
        stat.printSummary("ejb-util-methodmapID");
    }  
    
    public MethodMapTest (String[] args) {               
    }

    public void doTest() {

        for(int i = 0; i < TEST_CLASSES.length; i++) {
            Class c = TEST_CLASSES[i];
            System.out.println("Doing methodmap test for " + c);
            boolean result = testClass(c);
            if( result ) {
                stat.addStatus("methodmapclient main", stat.PASS);            
            } else {
                stat.addStatus("methodmapclient main", stat.FAIL); 
            }
        }        
        
        testCtor();
        testUnsupportedOperations();

        
        
    }

    private void testCtor() {

        // negative bucket value is illegal
        boolean ctorTest1Passed = false;
        try {
            new MethodMap(new HashMap(), -1);
            System.out.println("bucketSize -1 should have thrown exception");
        } catch(IllegalArgumentException e) {         
            ctorTest1Passed = true;
        } catch(Exception e) {
            e.printStackTrace();
        }

        // bucket value 0 is illegal
        boolean ctorTest2Passed = false;
        try {
            new MethodMap(new HashMap(), 0);
            System.out.println("bucketSize 0 should have thrown exception");
        } catch(IllegalArgumentException e) {
            ctorTest2Passed = true;
        } catch(Exception e) {
            e.printStackTrace();
        }

        // make sure empty hashmap doesn't cause problems
        boolean ctorTest3Passed = false;
        try {
            new MethodMap(new HashMap());
            ctorTest3Passed = true;
        } catch(Exception e) {
            e.printStackTrace();
        }        

        // pass map containing something other than Method objects
        Map otherMap = new HashMap();
        otherMap.put("foo", "bar");
        boolean ctorTest4Passed = false;
        try {
            new MethodMap(otherMap);            
        } catch(Exception e) {
            ctorTest4Passed = true;
        }

        if( ctorTest1Passed && ctorTest2Passed & ctorTest3Passed 
            && ctorTest4Passed ) {
            stat.addStatus("methodmapclient ctor", stat.PASS);  
        } else {
            stat.addStatus("methodmapclient ctor", stat.FAIL);  
        }

    }

    private void testUnsupportedOperations() {
        Map map = new HashMap();

        Method[] methods = this.getClass().getDeclaredMethods();
        Method method1 = methods[0];
        Method method2 = methods[1];

        map.put(method1, method1.toString());
        Map methodMap = new MethodMap(map);

        boolean uoeTest1Passed = false;
        try {
            methodMap.put(method1, method1.toString());
            System.out.println("put should have failed");
        } catch(UnsupportedOperationException uoe) {
            uoeTest1Passed = true;
        } catch(Exception e) {
            e.printStackTrace();
        }

        boolean uoeTest2Passed = false;
        try {
            methodMap.putAll(map);
            System.out.println("putAll should have failed");
        } catch(UnsupportedOperationException uoe) {
            uoeTest2Passed = true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        boolean uoeTest3Passed = false;
        try {
            methodMap.remove(method1);
            System.out.println("remove should have failed");
        } catch(UnsupportedOperationException uoe) {
            uoeTest3Passed = true;
        } catch(Exception e) {
            e.printStackTrace();
        }

        boolean uoeTest4Passed = false;
        try {
            map.put(null, "foo");
            Map illegalMethodMap = new MethodMap(map);
            System.out.println("MethodMap ctor should have failed b/c of " +
                               " null method value");
        } catch(Exception e) {
            uoeTest4Passed = true;
        } 

        if( uoeTest1Passed && uoeTest2Passed && uoeTest3Passed &&
            uoeTest4Passed ) {
            stat.addStatus("methodmapclient unsupportedop", stat.PASS); 
        } else {
            stat.addStatus("methodmapclient unsupportedop", stat.FAIL); 
        }

    }

    private boolean testClass(Class c) {
      
        Map regularMap = new HashMap();

        Method[] methods = c.getDeclaredMethods();

        for(int i = 0; i < methods.length; i++) {
            regularMap.put(methods[i], methods[i].toString());
        }
        
        Map defaultMethodMap = new MethodMap(regularMap);
        System.out.println("Testing " + c + " with default MethodMap");
        boolean defaultTest = 
            testClass(c, methods, false, regularMap, defaultMethodMap);
        boolean defaultTestOpt = 
            testClass(c, methods, true, regularMap, defaultMethodMap);

        // test degenerate case where there is only 1 bucket in MethodMap.
        // unless there are less than 2 methods in the map, this should 
        // always defer to parent.
        Map oneBucketMethodMap = new MethodMap(regularMap, 1);
        System.out.println("Testing " + c + " with 1-bucket MethodMap");
        boolean oneBucketTest = 
            testClass(c, methods, false, regularMap, oneBucketMethodMap);
        boolean oneBucketTestOpt =
            testClass(c, methods, true, regularMap, oneBucketMethodMap);

        Map seventeenBucketMethodMap = new MethodMap(regularMap, 17);
        System.out.println("Testing " + c + " with 17-bucket MethodMap");
        boolean seventeenBucketTest = 
            testClass(c, methods, false, regularMap, seventeenBucketMethodMap); 
        boolean seventeenBucketTestOpt = 
            testClass(c, methods, true, regularMap, seventeenBucketMethodMap);  
    

        boolean clearTest = false;
        try {
            seventeenBucketMethodMap.clear();
            if( methods.length > 0 ) {
                Object obj = seventeenBucketMethodMap.get(methods[0]);
                if( obj == null ) {
                    // make sure another clear op doesn't cause exception.
                    seventeenBucketMethodMap.clear();
                    clearTest = true;
                } else {
                    System.out.println("Clear() should have returned null for " +
                                       methods[0] 
                                       + " after clear().  instead it " +
                                       " returned " + obj);
                }
            } else {
                clearTest = true;
            }
        } catch(Exception e) {
            clearTest = false;
            System.out.println("clear() threw an exception");
            e.printStackTrace();
        }
        
        return (defaultTest && defaultTestOpt && 
                oneBucketTest && oneBucketTestOpt &&
                seventeenBucketTest && seventeenBucketTestOpt &&
                clearTest);
    }

    private boolean testClass(Class c, Method[] methods, boolean useParamOpt,
                              Map regularMap, Map methodMap) {
        
        for(int i = 0; i < methods.length; i++) {
            Method next = methods[i];
            int numParams = next.getParameterTypes().length;
            Object regularLookup = regularMap.get(next);

            Object methodMapLookup = null;
            if( useParamOpt ) {
                MethodMap mm = (MethodMap) methodMap;
                methodMapLookup = mm.get(next, numParams);
            } else {
                methodMapLookup = methodMap.get(next);
            }
            
            if( regularLookup != methodMapLookup ) {
                System.out.println("Error.  regularLookup = " + regularLookup
                                   + " methodMapLookup = " + methodMapLookup
                                   + " for Method " + next.toString());
                return false;
            } 

            //            System.out.println("Got " + regularLookup + " for method " + 
            //                 next.toString());

        }

        return true;

    }


    private interface MethodMapTest1 {}

    private interface MethodMapTest2 {
        // overloadeds methods
        void foo();
        void foo(int i);

        // short method names
        void a();

        // long method names
        void aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa();

        void ab();

        // method that throws exception
        void _ab() throws java.rmi.RemoteException;

        // array params
        int abc(int[] a);
        int def(int[][][][][][][][] b);        

        // arbitrary unicode chars
        void _B\u8001$();
        void _CCMcx\u04e3\u05E90123();

        
    }

    private interface MethodMapTest3 {

        void A();
        
    }

    private interface MethodMapTest4 extends MethodMapTest3 {}

    private interface MethodMapTest5 extends MethodMapTest3 {
        void A();
        void B();
        void B(String c);
    }

    private interface MethodMapTest6 extends MethodMapTest5 {
        void B(String c);
    }

}
