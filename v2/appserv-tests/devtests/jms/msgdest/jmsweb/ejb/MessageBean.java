package com.sun.s1asdev.jms.msgdest.jmsweb;

import java.rmi.RemoteException;
import javax.jms.*;
import javax.ejb.*;
import java.io.Serializable;
import javax.naming.*;

public class MessageBean  
    implements MessageDrivenBean, MessageListener {
    private MessageDrivenContext mdc;

    public MessageBean(){
    }

    public void onMessage(Message message) {
        System.out.println("Got message!!!"  +message);
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
	this.mdc = mdc;
	System.out.println("In MessageDrivenEJB::setMessageDrivenContext !!");
    }

    public void ejbCreate() throws RemoteException {
	System.out.println("In MessageDrivenEJB::ejbCreate !!");
    }

    public void ejbRemove() {
	System.out.println("In MessageDrivenEJB::ejbRemove !!");
    }

}
