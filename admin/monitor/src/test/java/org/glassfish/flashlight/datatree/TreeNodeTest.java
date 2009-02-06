/**
 * @author Harpreet Singh
 */
package org.glassfish.flashlight.datatree;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
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
import org.junit.Ignore;
import static org.junit.Assert.*;

/**
 *
 * @author hsingh
 */
@Ignore
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
        TreeNode server = setupSimpleTree ();
        TreeNode grandson = server.getNode("wto.wtoson.wtograndson");
//        System.out.println ("Retreived :"+grandson.getName()+ " should be "+
//                "wtograndson");
        assertEquals ("wtograndson", grandson.getName());
    }
    
    @Test
    public void testSimpleTreeWrongElement (){
        System.out.println("test:simpleTreeWrongElement");
        TreeNode server = setupSimpleTree ();
        TreeNode grandson = server.getNode("wto.foobar.wtograndson");
        assertNull (grandson);    
    }


    @Test 
    public void testSimpleTreeWithMethodInvoker (){
        try {
            System.out.println("test:simpleTreeWithMethodInvoker");
            TreeNode server = setupSimpleTree();
            Method m = this.getClass().getMethod("helloWorld", (Class[])null);
            TreeNode methodInv = 
                    TreeNodeFactory.createMethodInvoker("helloWorld", this,
                    "categoryName", m);
            TreeNode wto = server.getNode ("wto");
            wto.addChild (methodInv);
            
            TreeNode child = server.getNode("wto.helloWorld");
//            System.out.println ("Invoking hello world. Got Value: " + child.getValue ());
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
        TreeNode server = setupSimpleTree ();
        Counter counter = CounterFactory.createCount(10);
        for (int i=0; i<3; i++)
            counter.increment();
        
        TreeNode grandson = server.getNode ("wto.wtoson.wtograndson");
        grandson.addChild((TreeNode)counter);
        
        TreeNode counterNode = server.getNode ("wto.wtoson.wtograndson.counter");
        assertEquals (returnValue, counterNode.getValue());
        
        
    }
    
    @Test 
    public void testAverageInTree (){
        System.out.println ("test:testAverageInTree");
        long returnValue = 6;
        TreeNode server = setupSimpleTree ();
        Average average = AverageFactory.createAverage();
        for (int i=0; i<3; i++)
            average.addDataPoint((i+1)*3);
        
        
        TreeNode grandson = server.getNode ("wto.wtoson.wtograndson");
        grandson.addChild((TreeNode)average);
        
//        System.out.println ("Average: "+average.getAverage());
//        System.out.println ("Minimum : "+ average.getMin()+ " Maximum : "+
//                average.getMax());
        TreeNode averageNode = server.getNode ("wto.wtoson.wtograndson.average");
        assertEquals (returnValue, averageNode.getValue());
        
    
    }
    
    @Test
    public void testIncorrectGetCompletePathName() {
        System.out.println("test:IncorrectGetCompletePathName");
        TreeNode server = setupSimpleTree ();
        TreeNode grandson = server.getNode("wto:wtoson.wtograndson");
        assertNull (grandson);
    }
      @Test
    public void testGetCompletePathName() {
        System.out.println("test:getCompletePathName");
        TreeNode server = setupSimpleTree ();
        TreeNode grandson = server.getNode("wto.wtoson.wtograndson");
         assertEquals ("server.wto.wtoson.wtograndson", 
                grandson.getCompletePathName());
    }
  
    @Test
    public void testTraverse() {
        System.out.println("test:traverse");
        TreeNode server = setupComplexTree ();
        List<TreeNode> list = server.traverse(false);
        String[] expected = new String [7];
        expected[0] = new String ("server");
        expected[1] = new String ("server.wto");
        expected[2] = new String ("server.wto.wtoson");
        expected[3] = new String ("server.wto.wtoson.wtosonsdaughter");
        expected[4] = new String ("server.wto.wtoson.wtosonsson");
        expected[5] = new String ("server.wto.wtodaughter");
        expected[6] = new String ("server.wto.wtodaughter.wtodaughtersdaughter");
        // System.out.println ("---- Printing Traversed Tree ---");
        String[] actual = new String[7];
      //  int i=0;
      //  for (TreeNode node:list){
      //      System.out.println ("Node: "+ node.getName()+ 
      //              " Complete Path: "+node.getCompletePathName());
      //      actual[i++] = new String (node.getCompletePathName());
      //  }
        assertEquals (expected.length, list.size() );
    }

    @Test
    public void testTraverseIgnoreDisabled() {
        System.out.println("test:traverseIgnoreDisabled");
        TreeNode server = setupComplexTree ();
        TreeNode wtoson = server.getNode ("wto.wtoson");
        wtoson.setEnabled(false);
        List<TreeNode> list = server.traverse(true);
        String[] expected = new String [4];
        expected[0] = new String ("server");
        expected[1] = new String ("server.wto");
        expected[2] = new String ("server.wto.wtodaughter");
        expected[3] = new String ("server.wto.wtodaughter.wtodaughtersdaughter");
        // System.out.println ("---- Printing Traversed Tree ---");
 /*       String[] actual = new String[4];
          int i=0;
          for (TreeNode node:list){
              System.out.println ("Node: "+ node.getName()+
                      " Complete Path: "+node.getCompletePathName());
              actual[i++] = new String (node.getCompletePathName());
          }
    */
        assertEquals (expected.length, list.size() );
    }

    @Test
    public void testV2Compatible (){
        System.out.println ("test:testV2Compatible");
        TreeNode server = setupComplexTree ();
        List<TreeNode> list = server.getNodes("*wtodaughter*", false, true);
        int expectedLength = 2;
        assertEquals (expectedLength, list.size());

    }

    @Test
    public void testGetAll() {
        System.out.println("test:getAll");
        TreeNode server = setupComplexTree ();
        List<TreeNode> list = server.getNodes("*", false, true);
        String[] expected = new String [7];
        expected[0] = new String ("server");
        expected[1] = new String ("server.wto");
        expected[2] = new String ("server.wto.wtoson");
        expected[3] = new String ("server.wto.wtoson.wtosonsdaughter");
        expected[4] = new String ("server.wto.wtoson.wtosonsson");
        expected[5] = new String ("server.wto.wtodaughter");
        expected[6] = new String ("server.wto.wtodaughter.wtodaughtersdaughter");
        // System.out.println ("---- Printing Traversed Tree ---");
/*        String[] actual = new String[7];
        int i=0;
        for (TreeNode node:list){
            System.out.println (" Expected Node : " + node.getName() +
                    " Complete Path: "+ node.getCompletePathName ());
            System.out.println (" Actual Node   : "+ node.getName()+ 
                    " Complete Path: "+node.getCompletePathName());
            actual[i++] = new String (node.getCompletePathName());
        }
  */
        assertEquals (expected.length, list.size() );
    }

   @Test
    public void testGetSonsAndGrandSons() {
        System.out.println("test:GetSonsAndGrandSons");
        TreeNode server = setupComplexTree ();
        List<TreeNode> list = server.getNodes(".*son.*", false, false);
        int expectedCount = 3;
        int actualCount = 0;
        for (TreeNode node:list){
            if (node.getCompletePathName().contains("son"))
                actualCount++;
        }
        assertEquals (expectedCount, actualCount);
       }
   
    @Test
    public void testGetDaughter() {
        System.out.println("test:GetDaughter");
        TreeNode server = setupComplexTree ();
        List<TreeNode> list = server.getNodes(".*wtodaughter", false, false);
        int expectedCount = 1;
        int actualCount = 0;
        for (TreeNode node:list){
            if (node.getCompletePathName().contains("wtodaughter"))
                actualCount++;
        }
        assertEquals (expectedCount, actualCount);
       }

    @Test
    public void testTimeStatsMillis (){
        System.out.println ("test:timeStatsMillis");
        TimeStats timeStat = TimeStatsFactory.createTimeStatsMilli();
        long min = 1000;
        long mid = 2000;
        long max = 4000;
        long count = 3;
        double average = (min+mid+max)/3.0;
        timeStat.setTime (min);
        timeStat.setTime (mid);
        timeStat.setTime (max);
        assertEquals (min, timeStat.getMinimumTime());
        assertEquals (average, timeStat.getTime());
        assertEquals (max, timeStat.getMaximumTime());
        assertEquals (count, timeStat.getCount());
    }


    // Setup Methods

    private TreeNode setupSimpleTree (){
        TreeNode server = TreeNodeFactory.createTreeNode ("server", this, "server");
        TreeNode wto = TreeNodeFactory.createTreeNode("wto", this, "web");
        TreeNode wtoson = TreeNodeFactory.createTreeNode ("wtoson", this, "web");
        TreeNode wtograndson = TreeNodeFactory.createTreeNode ("wtograndson", 
                this, "web");
        wtoson.addChild(wtograndson);
        wto.addChild(wtoson);
        server.addChild(wto);
        return server;
    }
    
        private TreeNode setupComplexTree (){
        TreeNode server = TreeNodeFactory.createTreeNode ("server", this, "server");

        
        TreeNode wto = TreeNodeFactory.createTreeNode("wto", this, "web");
        server.addChild(wto);
        
        TreeNode wtoson = TreeNodeFactory.createTreeNode ("wtoson", this, "web");
        wto.addChild(wtoson);
        
        TreeNode wtodaughter = TreeNodeFactory.createTreeNode ("wtodaughter", this, "web");
        wto.addChild(wtodaughter);
        
        TreeNode wtosonsson = TreeNodeFactory.createTreeNode ("wtosonsson", 
                this, "web");
                
        wtoson.addChild(wtosonsson);
        
        TreeNode wtodaughtersdaughter = TreeNodeFactory.
                createTreeNode ("wtodaughtersdaughter", 
                this, "web");
        
        wtodaughter.addChild(wtodaughtersdaughter);
        
        TreeNode wtosonsdaughter = TreeNodeFactory.createTreeNode ("wtosonsdaughter", 
                this, "web");
       
        wtoson.addChild(wtosonsdaughter);
        
        return server;
    }
}
