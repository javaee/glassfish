/*
 * ReadMe.java
 *
 * Created on July 19, 2006, 11:46 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.gf.jmx.utils;

/**
 *
 * @author Administrator
 */
public class ReadMe {
    
    private ReadMe() {
    }
    public static void main(final String args[]) {
        System.out.println("java -cp JmxUtils.jar org.gf.jmx.utils.JustConnect jmxserviceurl username password\n" +
                "-- just connects to a JMX backend with given service url and username and password");
    }
}
