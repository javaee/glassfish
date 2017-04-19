package com.sun.s1asdev.security.simpleMultiRoleMapping.ejb1;

import javax.ejb.Local;

/**
 * This is the business interface for Message enterprise bean.
 */
@Local
public interface MessageLocal1 {
    String getMessage();
    
}
