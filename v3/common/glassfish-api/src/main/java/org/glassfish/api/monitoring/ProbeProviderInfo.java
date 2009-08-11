/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.api.monitoring;

/**
 * This information needs to be shared by more than one module.
 * The bare minimum info contained in a FlashlightProbe that a value-add module needs
 * @author bnevins
 */
public interface ProbeProviderInfo {
    String      getProbeProviderName();
    String      getModuleProviderName();
    String      getModuleName();
    Class       getProviderClass();
    ProbeInfo[] getProbesInfo();
}
