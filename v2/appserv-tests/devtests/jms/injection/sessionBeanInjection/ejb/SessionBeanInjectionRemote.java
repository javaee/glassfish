package org.glassfish.test.jms.injection.ejb;

import javax.ejb.Remote;

/**
 *
 * @author LILIZHAO
 */
@Remote
public interface SessionBeanInjectionRemote {
    public static final String RemoteJNDIName =  SessionBeanInjection.class.getSimpleName() + "/remote";

    public void sendMessage(String text);
    
    public boolean checkMessage(String text);
}