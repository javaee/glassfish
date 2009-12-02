package com.sun.blogs.foo.samples.ProbeApp;

import org.glassfish.external.probe.provider.annotations.ProbeListener;

/**
 * @author Byron Nevins
 */

public class MyProbeListener {
    @ProbeListener("fooblog:samples:ProbeServlet:myProbe1")
    public void probe1(String s, int i) {
        System.out.println("@@@@@@@@@@@@@@@@@@@  PROBE LISTENER HERE!!!!   @@@@@@@@@@@@@@@@@@@@@@@");
    }
}
