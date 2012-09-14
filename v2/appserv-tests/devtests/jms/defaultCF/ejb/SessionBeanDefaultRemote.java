package org.glassfish.test.jms.defaultcf.ejb;

import javax.ejb.Remote;

/**
 *
 * @author LILIZHAO
 */
@Remote
public interface SessionBeanDefaultRemote {
    public static final String RemoteJNDIName =  SessionBeanDefault.class.getSimpleName() + "/remote";

    public void sendMessage(String text);
    
    public boolean checkMessage(String text);
}