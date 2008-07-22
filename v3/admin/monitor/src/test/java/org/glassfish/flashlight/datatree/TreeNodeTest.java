/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.datatree;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.flashlight.datatree.factory.TreeNodeFactory;
import org.glassfish.flashlight.statistics.Average;
import org.glassfish.flashlight.statistics.Counter;
import org.glassfish.flashlight.statistics.TimeStats;
import org.glassfish.flashlight.statistics.factory.AverageFactory;
import org.glassfish.flashlight.statistics.factory.CounterFactory;
import org.glassfish.flashlight.statistics.factory.TimeStatsFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hsingh
 */
public class TreeNodeTest {

    public TreeNodeTest() {
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

    @Test
    public void testSimpleTree() {
        System.out.println("test:simpleTree");
        TreeNode wto = setupSimpleTree ();
        TreeNode grandson = wto.getNode("wto.wtoson.wtograndson");
        assertEquals ("wtograndson", grandson.getName());
    }
    
    @Test
    public void testSimpleTreeWrongElement (){
        System.out.println("test:simpleTreeWrongElement");
        TreeNode wto = setupSimpleTree ();
        TreeNode grandson = wto.getNode("wto.foobar.wtograndson");
        assertNull (grandson);    
    }


    @Test 
    public void testSimpleTreeWithMethodInvoker (){
        try {
            System.out.println("test:simpleTreeWithMethodInvoker");
            TreeNode wto = setupSimpleTree();
            Method m = this.getClass().getMethod("helloWorld", (Class[])null);
            TreeNode methodInv = 
                    TreeNodeFactory.createMethodInvoker("helloWorld", this,
                    "categoryName", m);
            wto.addChild (methodInv);
            
            TreeNode child = wto.getNode("wto:helloWorld");
            System.out.println ("Invoking hello world. Got Value: " + child.getValue ());
            assertEquals(child.getValue(), "Hello World");
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(TreeNodeTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(TreeNodeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public String helloWorld (){
        return "Hello World";
    }

    @Test
    public void testCounterInTree (){
        System.out.println ("test:testCounterInTree");
        long returnValue = 13;
        TreeNode wto = setupSimpleTree ();
        Counter counter = CounterFactory.createCount(10);
        TreeNode counterNode = (TreeNode) counter;
        for (int i=0; i<3; i++)
            counter.increment();
        
        TreeNode grandson = wto.getNode ("wto.wtoson.wtograndson");
        grandson.addChild(counterNode);
        
        TreeNode greatGrandSon = wto.getNode ("wto.wtoson.wtograndson.counter");
        assertEquals (returnValue, greatGrandSon.getValue());
        
        
    }
    
    @Test 
    public void testAverageInTree (){
        System.out.println ("test:testAverageInTree");
        long returnValue = 6;
        TreeNode wto = setupSimpleTree ();
        Average average = AverageFactory.createAverage(3);
        TreeNode averageNode = (TreeNode) average;
        for (int i=0; i<3; i++)
            average.addDataPoint((i+1)*3);
        
        
        TreeNode grandson = wto.getNode ("wto.wtoson.wtograndson");
        grandson.addChild(averageNode);
        
        System.out.println ("Running average: "+average.getRunningAverage());
        System.out.println ("Minimum : "+ average.getMin()+ " Maximum : "+
                average.getMax());
        TreeNode greatGrandSon = wto.getNode ("wto.wtoson.wtograndson.average");
        assertEquals (returnValue, greatGrandSon.getValue());
        
    
    }
    
   
    @Test 
    public void testTimeStatsInTree (){
        System.out.println ("test:testTimeStatsInTree");
        long returnValue = 2000;
        TreeNode wto = setupSimpleTree ();
        TimeStats timeStats = TimeStatsFactory.createTimeStatsMilli();
        
        TreeNode timeStatsNode = (TreeNode) timeStats;
        timeStats.entry();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
       // Logger.getLogger(TreeNodeTest.class.getName()).log(Level.SEVERE, null, ex);
        }
         timeStats.exit();
         
        TreeNode grandson = wto.getNode ("wto.wtoson.wtograndson");
        grandson.addChild(timeStatsNode);
        
        System.out.println ("TimeStats: "+timeStats.getTime());
 
        TreeNode greatGrandSon = wto.getNode ("wto.wtoson.wtograndson.timeStatsMillis");
        assertEquals (returnValue, greatGrandSon.getValue());
        
    
    }    
    private TreeNode setupSimpleTree (){
        TreeNode wto = TreeNodeFactory.createTreeNode("wto", this, "web");
        TreeNode wtoson = TreeNodeFactory.createTreeNode ("wtoson", this, "web");
        TreeNode wtograndson = TreeNodeFactory.createTreeNode ("wtograndson", 
                this, "web");
        wtoson.addChild(wtograndson);
        wto.addChild(wtoson);
        return wto;
    }
}