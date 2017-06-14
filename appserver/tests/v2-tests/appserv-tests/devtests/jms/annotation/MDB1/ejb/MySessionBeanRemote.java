package org.glassfish.test.jms.annotation.ejb;

import javax.ejb.Remote;

@Remote
public interface MySessionBeanRemote {
    public static final String RemoteJNDIName =  MySessionBean.class.getSimpleName() + "/remote";

    public void sendMessage(String text);

    public boolean checkMessage(String text);
}
