package org.glassfish.test.jms.injection.ejb;

import javax.ejb.Remote;

/**
 *
 * @author LILIZHAO
 */
@Remote
public interface SessionBeanInjectionRemote {
    public static final String RemoteJNDIName =  SessionBeanInjection.class.getSimpleName() + "/remote";

    public Boolean sendMessage(String text);
}