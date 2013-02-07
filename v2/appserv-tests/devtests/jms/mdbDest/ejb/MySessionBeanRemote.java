package org.glassfish.test.jms.mdbdest.ejb;

import javax.ejb.Remote;

/**
 *
 * @author LILIZHAO
 */
@Remote
public interface MySessionBeanRemote {
    public static final String RemoteJNDIName =  MySessionBean.class.getSimpleName() + "/remote";
    
    public void sendMessage(String text);
    
    public int checkMessage(String text, int expectedCount);
}