/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package test.jbi;

public interface JBIQuicklookTest {

    /** Performs an arbitrary test and returns the result of the same as a String.
     * The returned result must be something that is available as status string in
     * com.sun.ejte.ccl.reporter.SimpleReporterAdapter.PASS and FAIL.
     */
    public String test(AdminCli admincli) throws RuntimeException;
   
    /** Returns the name of the test.
     */
    public String getName();
    
    /** Returns the time taken by the test to execute in milliseconds
     * @return long denoting the time taken for this test to execute
     */
    public long getExecutionTime();
}