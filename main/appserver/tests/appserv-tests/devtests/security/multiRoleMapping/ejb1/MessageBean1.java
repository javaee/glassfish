package com.sun.s1asdev.security.simpleMultiRoleMapping.ejb1;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;

@Stateless
@Local({MessageLocal1.class})
public class MessageBean1 implements MessageLocal1 {
    
    //@RolesAllowed("ejbrole")
    public String getMessage() {
        return "Hello from ejb";
    }
    
}
