/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.admin;

import java.util.*;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

/**
 *
 * @author bnevins
 */
public class PPListener {
    @ProbeListener("glassfish:kernel:PPTester:method1")
    public void listen1(
        @ProbeParam("param1") String string1,
        @ProbeParam("param2") int int2) {
        System.out.println("@@@@@@@@@@@@@@@@@@@  PPTester Listener 1   @@@@@@@@@@@@@@@@@@@@@@@");
    }

    @ProbeListener("glassfish:kernel:PPTester:method2")
    public void listen2(
            @ProbeParam("s") String s23,
            @ProbeParam("x") int x,
            @ProbeParam("y") int y,
            @ProbeParam("d") String date) {
        System.out.println("@@@@@@@@@@@@@@@@@@@  PPTester Listener 2   @@@@@@@@@@@@@@@@@@@@@@@");
    }

    @ProbeListener("glassfish:kernel:PPTester:method3")
    public void listen3(
            @ProbeParam("s") String s){
        System.out.println("@@@@@@@@@@@@@@@@@@@  PPTester Listener 3   @@@@@@@@@@@@@@@@@@@@@@@");
    }
}