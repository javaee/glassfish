package org.glassfish.test.jms.jmsxdeliverycount.ejb;

import javax.ejb.Remote;

/**
 *
 * @author LILIZHAO
 */
@Remote
public interface MySessionBeanRemote {
    public static final String RemoteJNDIName =  MySessionBean.class.getSimpleName() + "/remote";
    
    public void sendMessage(String text);
    
    public boolean checkMessage(String text);
}