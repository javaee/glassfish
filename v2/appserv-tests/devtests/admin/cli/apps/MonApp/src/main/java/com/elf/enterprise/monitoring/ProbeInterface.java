/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elf.enterprise.monitoring;

import org.glassfish.external.probe.provider.annotations.ProbeProvider;
import org.glassfish.external.probe.provider.annotations.Probe;

/**
 *
 * @author Byron Nevins
 */

@ProbeProvider(moduleProviderName = "fooblog", moduleName = "samples", probeProviderName = "ProbeInterface")

public interface ProbeInterface {
    @Probe(name = "myProbe2")
    public void myProbe2(String s1, String s2);
}
