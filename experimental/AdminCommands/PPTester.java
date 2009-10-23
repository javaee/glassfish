/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.admin;

import java.util.*;
import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 *
 * @author bnevins
 */
@ProbeProvider (moduleProviderName="glassfish", moduleName="kernel", probeProviderName="PPTester")
public class PPTester {
    @Probe(name="event1")
    public void method1(
        @ProbeParam("param1") String string1,
        @ProbeParam("param2") int int2) {
        System.out.println("HELLO FROM PPTester.method1  ARgs:" + string1 +", " + int2);
    }

    @Probe(name="event2")
     public void method2(String s23, int x, int y, Date date, List<Integer> li) {
        System.out.println("HELLO FROM PPTester.method2!  My Arg ==>" + s23);
     }
}
