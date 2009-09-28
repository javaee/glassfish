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
public class Probe {
    String probeName = null;
    String probeMethod = null;
    List<ProbeParam> probeParams = null;
    boolean hasSelf = false;
    boolean isHidden = false;

    public String getProbeName() {
        return probeName;
    }

    public String getProbeMethod() {
        return probeMethod;
    }

    public List<ProbeParam> getProbeParams() {
        return probeParams;
    }

    public boolean hasSelf() {
        return hasSelf;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public Probe(String probeName, String method, List<ProbeParam> params, boolean hasSelf, boolean isHidden) {
        this.probeName = probeName;
        probeMethod = method;
        probeParams = params;
        this.hasSelf = hasSelf;
        this.isHidden = isHidden;

    }

    @Override
    public String toString() {
        String paramsStr = "     \n";
        for (ProbeParam param : probeParams) {
            paramsStr += "         , Param " + param.toString();
        }
        return (" Probe name = " + probeName +
                " , method = " + probeMethod + paramsStr);
    }
}
