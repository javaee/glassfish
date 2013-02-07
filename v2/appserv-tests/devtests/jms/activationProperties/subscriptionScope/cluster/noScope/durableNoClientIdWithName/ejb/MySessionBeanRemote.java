package org.glassfish.test.jms.activationproperties.ejb;

import javax.ejb.Remote;

/**
 *
 * @author LILIZHAO
 */
@Remote
public interface MySessionBeanRemote {
    public static final String RemoteJNDIName =  MySessionBean.class.getSimpleName() + "/remote";
    
    public void sendMessage(String text);
    
    public String checkMessage(String text);
}