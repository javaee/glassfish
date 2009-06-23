/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;

/**
 *
 * @author anilam
 */
public final class AMX {
    private AMX()
    {
    }

    //
    public static final String DOMAIN_ROOT = "v3:pp=,type=domain-root,name=v3";

    public static final String RUNTIME="v3:pp=/ext,type=runtime";

    //TODO: don't hardcode config name.
    public static final String SECURITY_SERVICE="v3:pp=/domain/configs/config[server-config],type=security-service";

    public static final String ADMIN_LISTENER="v3:pp=/domain/configs/config[server-config]/network-config/network-listeners,type=network-listener,name=admin-listener";

}
