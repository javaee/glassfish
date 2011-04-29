/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elf.enterprise.monitoring;

import org.glassfish.external.probe.provider.annotations.ProbeListener;

public class MyProbeListener {
    @ProbeListener("fooblog:samples:ProbeServlet:myProbe")
    public void probe(String s) {
        System.out.println("PROBE LISTENER HERE.  Called with this arg: " + s);
    }

    @ProbeListener("fooblog:samples:ProbeInterface:myProbe2")
    public void probe2(String s1, String s2) {
        System.out.println("PROBE INTERFACE LISTENER HERE.  Called with thes args: " + s1 + ", " + s2);
    }
}
