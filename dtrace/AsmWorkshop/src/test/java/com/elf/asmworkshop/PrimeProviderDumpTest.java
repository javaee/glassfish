/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elf.asmworkshop;

import junit.framework.TestCase;

/**
 *
 * @author bnevins
 */
public class PrimeProviderDumpTest extends TestCase {
    
    public PrimeProviderDumpTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of dump method, of class PrimeProviderDump.
     */
    public void testDump() throws Exception {
        byte[] result = PrimeProviderDump.dump();
        // feeble tests
        assertNotNull(result);
        assertTrue(result.length > 100);
        System.out.println("Run a Java Decompiler on the class file...");
    }
}
