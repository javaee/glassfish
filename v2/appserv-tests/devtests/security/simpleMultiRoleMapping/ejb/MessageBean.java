package com.sun.s1asdev.security.simpleMultiRoleMapping.ejb;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;

@Stateless
@Local({MessageLocal.class})
public class MessageBean implements MessageLocal {
    
    @RolesAllowed("ejbrole")
    public String getMessage() {
        return "Hello from ejb";
    }
    
}
