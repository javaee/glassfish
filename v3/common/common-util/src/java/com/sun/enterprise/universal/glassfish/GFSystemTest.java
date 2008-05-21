/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.glassfish;

import com.sun.enterprise.universal.collections.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class GFSystemTest {

    public GFSystemTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of GFSystem for the case where there are multiple instances in a JVM
     */
    @Test
    public void threadTest() {
        try {
            Thread t1 = new ParentThread("xxx");
            Thread t2 = new ParentThread("yyy");
            Thread t3 = new ParentThread("zzz");
            t1.start();
            t2.start();
            t3.start();
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(GFSystemTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertFalse(failed);
    }
    public static synchronized void setFailure() {
        failed = true;
    }
    public static volatile boolean failed = false;
}

class ParentThread extends Thread {
    ParentThread(String name) {
        super(name);
    }
    @Override 
    public void run() {
        try {
            GFSystem.setProperty("foo", getName());
            Thread t = new ChildThread(getName(), getName() + "__child");
            t.start();
            String result = GFSystem.getProperty("foo");

            if (result.equals(getName())) {
                System.out.println("Parent Thread " + getName() + "--> foo = " + GFSystem.getProperty("foo"));
            } else {
                System.out.println("Expected: " + getName() + ", got: " + result);
                GFSystemTest.setFailure();
            }
            t.join();
        } catch (InterruptedException ex) {
        }
    }
}


class ChildThread extends Thread {
    ChildThread(String parentName, String name) {
        super(name);
        this.parentName = parentName;
    }
    @Override 
    public void run() {
        try {
            Thread t = new GrandChildThread(parentName, getName() + "__grandchild");
            t.start();
            String result = GFSystem.getProperty("foo");

            if (result.equals(parentName)) {
                System.out.println("Child Thread of " + parentName + " --> foo = " + GFSystem.getProperty("foo"));
            } else {
                System.out.println("ChildThread Expected: " + parentName + ", got: " + result);
                GFSystemTest.setFailure();
            }
            t.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(ChildThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    String parentName;
}

class GrandChildThread extends Thread {
    GrandChildThread(String grandParentName, String name) {
        super(name);
        this.grandParentName = grandParentName;
    }
    @Override 
    public void run() {
        String result = GFSystem.getProperty("foo");

        if(result.equals(grandParentName))
            System.out.println("GrandChild Thread of " + grandParentName + " --> foo = " + GFSystem.getProperty("foo"));
        else {
            System.out.println("GrandChildThread Expected: " + getName() + ", got: " + result);
            GFSystemTest.setFailure();
        }
    }
    String grandParentName;
}


/*
/*
    public static void main(String[] args) {
        Thread t = new TestThread("thread1");
        Thread t2 = new TestThread("thread2");
        t.start();
        t2.start();
    }

}



 */