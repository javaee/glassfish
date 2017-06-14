package org.glassfish.test.jms.injection.ejb;

import javax.ejb.Remote;

/**
 *
 * @author JIGWANG
 */
@Remote
public interface SessionBeanInjectionRemote1 {
    public static final String RemoteJNDIName =  SessionBeanInjection1.class.getSimpleName() + "/remote1";

    public Boolean sendMessage(String text);
    
}