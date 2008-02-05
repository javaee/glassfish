/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.security;

import com.sun.enterprise.server.pluggable.SecuritySupport;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

@Service
public class SecurityServicesUtil {

    @Inject(name="PE")
    private SecuritySupport peSecSupport;
    
    public SecuritySupport getSecuritySupport() {
        //TODO:V3 Add logic to differentiate between PE and EE cases ?
        //if EE Impl class is in a different module then we can remove
        // the named injection above.
        return peSecSupport;
    }
}
