/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admin.amx.internal;

import java.util.Map;

/**
 * Why are tests in the API?  For several reasons:
 * <ul>
 * <li>They provide sample code for the users of the API</li>
 * <li>The unit tests need a serve-side target for certain tests</li>
 * <li>The test code can be run either in process with the server, or
 *  on the client side.  Both scenarios are used by clients, and testing
 * both ways is needed.</li>
 * <li>Efficiency: the ability to run the tests in the server itself means
 * that more involved tests can run much faster, making it feasible to do
 * extensive testing.</li>
 * <li>Code factoring: this code can be run in Quicklook, standalone or
 * in the server to validate things in debug mode.</li>
 * </ul>
 * <em>Code here is subject to change at any time. It is not to be used
 * by any clients as an API</em>
 <p>Any method that starts with "test" is considered to be a test 
 * @author lloyd
 */
public interface SanityChecksMBean {

    /** run all the tests, returning a Map whose key is the test name and whose value is
        test output or a Throwable (if failed) */
    public Map<String,Object> runAllTests();
    
    /** iterate through all AMX MBeans doing basic sanity checks */
    public String testBasics();

    /** verify all Container/Containee relationships */
    public String testParentChild();

    /** verify that MBeans implementing Container function properly */
    public String testContainer();

    /** verify that MBeans supporting DefaultValues function properly */
    public String testDefaultValues();

    /** verify AttributeResolve functionality works properly */
    public String testAttributeResolver();

    /** verify that the SystemStatus MBean works properly */
    public String testSystemStatus();
}



