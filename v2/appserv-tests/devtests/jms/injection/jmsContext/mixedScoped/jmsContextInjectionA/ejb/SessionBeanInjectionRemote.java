package org.glassfish.test.jms.injection.ejb;

import javax.ejb.Remote;

/**
 *
 * @author JIGWANG
 */
@Remote
public interface SessionBeanInjectionRemote {
    public static final String RemoteJNDIName =  SessionBeanInjection.class.getSimpleName() + "/remote";

    public String sendMessage1(String text);
    
    public String sendMessage2(String text);
}