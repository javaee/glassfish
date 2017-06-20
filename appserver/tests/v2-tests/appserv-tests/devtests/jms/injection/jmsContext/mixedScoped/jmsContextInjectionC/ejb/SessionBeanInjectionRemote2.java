package org.glassfish.test.jms.injection.ejb;

import javax.ejb.Remote;

/**
 *
 * @author JIGWANG
 */
@Remote
public interface SessionBeanInjectionRemote2 {
    public static final String RemoteJNDIName =  SessionBeanInjection2.class.getSimpleName() + "/remote2";

    public String sendMessage(String text);

}