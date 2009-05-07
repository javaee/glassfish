/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.cli;

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
public class AsadminMainTest {

    public AsadminMainTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println(" ***************** AsadminMain Unit Tests *****************");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println(" ***************** AsadminMain Unit Tests *****************");
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class AsadminMain.
     */
    @Test
    public void testPositive() {
        AsadminMain main = new AsadminMain("version");
        int exitCode = main.runCommand();
        Throwable t = main.getErrorThrowable();

        if(t != null) {
            t.printStackTrace();
            fail("AsadminMain FAILED with \"version\" as the command." + main.getErrorMessage());
        }
    }


    /*
     * make sure that no Exceptions are thrown from the run command -- that is
     * the contract with API user..
     */
    @Test
    public void testNoThrow() {
        try {
            AsadminMain main = new AsadminMain("total_baloney_no_exist");
            int exitCode = main.runCommand();
            Throwable t = main.getErrorThrowable();

            if(t != null) {
                t.printStackTrace();
            }
        }
        catch(Exception e) {
            fail("Should never get an Exception!!!");
        }
    }

}
