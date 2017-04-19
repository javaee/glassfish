package com.sun.s1asdev.ejb.mdb.cmt;

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
        System.out.println("Got message!!!");

        QueueConnection connection = null;
        try {
            InitialContext ic = new InitialContext();
            Queue queue = (Queue) ic.lookup("java:comp/env/jms/MyQueue");
            QueueConnectionFactory qcFactory = (QueueConnectionFactory)
                ic.lookup("java:comp/env/jms/MyQueueConnectionFactory");
            connection = qcFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession(false,
                                   Session.AUTO_ACKNOWLEDGE);
            connection.start();
            QueueSender sender = session.createSender(queue);
            TextMessage tmessage = session.createTextMessage();
            tmessage.setText("mdb() invoked");
            System.out.println("Sending message");
            sender.send(tmessage);
            System.out.println("message sent");
        } catch(NamingException e) {
            e.printStackTrace();
        }
        catch(JMSException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(connection != null) {
                    connection.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

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
