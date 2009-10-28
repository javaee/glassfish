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
    @Probe(name="method1")
    public void method1(
        @ProbeParam("param1") String string1,
        @ProbeParam("param2") int int2) {
        System.out.println("HELLO FROM PPTester.method1  ARgs:" + string1 +", " + int2);
    }

    @Probe(name="method2")
     public void method2(
            @ProbeParam("s") String s23,
            @ProbeParam("x") int x,
            @ProbeParam("y") int y,
            @ProbeParam("d") String date) {
        System.out.println("HELLO FROM PPTester.method2!");

        /*


         String lout = (li == null) ? "null list" : Arrays.toString(li.toArray(new Integer[0]));
        String dout = (date == null) ? "null date" : date.toString();


        System.out.println("HELLO FROM PPTester.method2!  My Args ==>\n " +
                "s23=" + s23 +
                ", x=" + x +
                ", y=" + y +
                ", date=" + dout +
                ", li=" + lout);


         */
     }

    @Probe(name="method3")
     public void method3(
            @ProbeParam("s") String s){
        System.out.println("HELLO FROM PPTester.method3!  My Arg ==>" + s);
     }
}
