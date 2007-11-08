package com.sun.s1asdev.security.simpleMultiRoleMapping.ejb2;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;

@Stateless
@Local({MessageLocal2.class})
public class MessageBean2 implements MessageLocal2 {
    
    //@RolesAllowed("ejbrole")
    public String getMessage() {
        return "Hello from ejb";
    }
    
}
