/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.amx.intf.config;

/**
 *
 * @author llc
 */
public final class AMXConfigConstants {

    private AMXConfigConstants() {
    }

    /** feature stating that the AMXConfig is ready for use after having been started.  Data is the ObjectName of the DomainConfig MBean */
    public static final String AMX_CONFIG_READY_FEATURE   = "AMXConfigReady";

}
