/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.xml;

import java.util.*;

/**
 *
 * @author Mahesh Meswani
 */
public class Provider {
    private String moduleProviderName = null;
    private String moduleName = null;
    private String probeProviderName = null;
    private String probeProviderClass = null;
    private List<Probe> probes = null;

    public String getModuleName() {
        return moduleName;
    }

    public String getModuleProviderName() {
        return moduleProviderName;
    }

    public String getProbeProviderName() {
        return probeProviderName;
    }

    public String getProbeProviderClass() {
        return probeProviderClass;
    }

    public List<Probe> getProbes() {
        return probes;
    }

    public Provider(String moduleProviderName, String moduleName,
                    String probeProviderName, String providerClass,
                    List<Probe> probes) {
        this.moduleProviderName = moduleProviderName;
        this.moduleName = moduleName;
        this.probeProviderName = probeProviderName;
        this.probeProviderClass = providerClass;
        this.probes = probes;

    }

    @Override
    public String toString() {
        String probeStr = "moduelProviderName=" + moduleProviderName + " moduleName=" +
                moduleName + " probeProvidername=" + probeProviderName + " probeProviderClass=" + probeProviderClass;
        for (Probe probe : probes) {
            probeStr += "\n    " + probe.toString();
        }
        return (probeStr);
    }
}
