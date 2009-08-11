/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.api.monitoring;

/**
 * The bare minimum info contained in a FlashlightProbe that a value-add module needs
 * The names look weird because they match pre-existing methods.  Those methods were
 * already declared so they can not change...
 * @author bnevins
 */
public interface ProbeInfo {
    Class[]     getParamTypes();
    String      getProviderJavaMethodName();
    String      getProbeName();
    int         getId();
    String      getModuleName();
    String      getModuleProviderName();
    String      getProbeProviderName();
    String[]    getProbeParamNames();
}
