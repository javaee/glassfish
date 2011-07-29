/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elf.enterprise.monitoring;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.ManagedAttribute;

public class MyProbeListener {
    private static final String SERVLET_REQUEST_COUNT_DESCRIPTION =
        "Cumulative value of the servlet request count";

     private CountStatisticImpl servletRequestCount = new CountStatisticImpl("ServletRequestCount",
            CountStatisticImpl.UNIT_COUNT, SERVLET_REQUEST_COUNT_DESCRIPTION);

    @ManagedAttribute
    public CountStatistic getServletRequestCount(){
         return servletRequestCount;
    }

    @ProbeListener("fooblog:samples:ProbeServlet:myProbe")
    public void probe(String s) {
        servletRequestCount.increment();
        System.out.println("PROBE LISTENER HERE.  Called with this arg: " + s);
    }

    @ProbeListener("fooblog:samples:ProbeInterface:myProbe2")
    public void probe2(String s1, String s2) {
        System.out.println("PROBE INTERFACE LISTENER HERE.  Called with thes args: " + s1 + ", " + s2);
    }

}
