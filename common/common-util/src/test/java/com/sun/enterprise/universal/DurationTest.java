/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class DurationTest {

    public DurationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("*********  START Duration Output Strings ***************");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("*********  END Duration Output Strings ***************");
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    @Test
    public void test1() {
        long msec = Duration.MSEC_PER_WEEK * 3 +
                    Duration.MSEC_PER_DAY * 6 +
                    Duration.MSEC_PER_HOUR * 23 +
                    Duration.MSEC_PER_MINUTE * 59 +
                    Duration.MSEC_PER_SECOND * 59;
        
        Duration d = new Duration(msec);
        System.out.println(d);
        assertTrue(d.numWeeks == 3);
        assertTrue(d.numDays == 6);
        assertTrue(d.numHours == 23);
        assertTrue(d.numMinutes == 59);
        assertTrue(d.numSeconds == 59);
        
    }
    @Test
    public void test2() {
        long msec = Duration.MSEC_PER_WEEK * 7 +
                    Duration.MSEC_PER_DAY * 6 +
                    Duration.MSEC_PER_HOUR * 23 +
                    Duration.MSEC_PER_MINUTE * 59 +
                    Duration.MSEC_PER_SECOND * 59 +
                    999;
                    
        
        Duration d = new Duration(msec);
        System.out.println(d);
        assertTrue(d.numWeeks == 7);
        assertTrue(d.numDays == 6);
        assertTrue(d.numHours == 23);
        assertTrue(d.numMinutes == 59);
        assertTrue(d.numSeconds == 59);
        assertTrue(d.numMilliSeconds == 999);
    }
    @Test
    public void test3() {
        long msec = System.currentTimeMillis();
        Duration d = new Duration(msec);
        System.out.println(d);
        assertTrue(d.numWeeks > 38 * 52);
    }
    @Test
    public void test4() {
        Duration d = new Duration(27188);
        System.out.println(d);
        assertTrue(d.numSeconds == 27);
        assertTrue(d.numMilliSeconds == 188);
    }
    @Test
    public void test5() {
        Duration d = new Duration(2);
        System.out.println(d);
        assertTrue(d.numSeconds == 0);
        assertTrue(d.numMilliSeconds == 2);
    }
}
    