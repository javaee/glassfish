package com.sun.enterprise.admin.mbeanapi.config;

/**
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Aug 26, 2004
 * @version $Revision: 1.1 $
 */
public class TestFailedException extends Exception {
    public TestFailedException(final String msg){
        super(msg);
    }
}
