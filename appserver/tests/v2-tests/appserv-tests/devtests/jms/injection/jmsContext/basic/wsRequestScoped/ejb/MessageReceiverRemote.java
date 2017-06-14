package org.glassfish.test.jms.injection.ejb;

import javax.ejb.Remote;

/**
 *
 * @author LILIZHAO
 */
@Remote
public interface MessageReceiverRemote {
    public static final String RemoteJNDIName =  MessageReceiverBean.class.getSimpleName() + "/remote";
    
    public boolean checkMessage(String text);
}