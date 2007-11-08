package com.sun.s1asdev.ejb.timer.mdbtimer;

import java.rmi.RemoteException;
import javax.jms.*;
import javax.ejb.*;
import java.io.Serializable;
import javax.naming.*;
import java.util.Date;

public class MessageBean  
    implements MessageDrivenBean, MessageListener,TimedObject {
    private MessageDrivenContext mdc;

    public MessageBean(){
    }

    public void ejbTimeout(Timer t) {
        Date now = new Date();
        Date expire = (Date) t.getInfo();
        System.out.println("In messagebean:ejbtimeout at time " +
                           now);
        if( now.after(expire) ) {
            System.out.println("Cancelling timer");
            t.cancel();

            QueueConnection connection = null;
            try {
                InitialContext ic = new InitialContext();
                Queue queue = (Queue) ic.lookup("java:comp/env/jms/MyQueue");
                QueueConnectionFactory qcFactory = (QueueConnectionFactory)
                    ic.lookup("java:comp/env/jms/MyQueueConnectionFactory");
                connection = qcFactory.createQueueConnection();
                QueueSession session = connection.createQueueSession(false,
                     Session.AUTO_ACKNOWLEDGE);
                QueueSender sender = session.createSender(queue);
                ObjectMessage message = session.createObjectMessage();
                message.setObject(now);
                System.out.println("Sending message " + message);
                sender.send(message);
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

        } else {
            System.out.println("timer lives on!!!");
        }
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
            Date now = new Date();
            Date expire = new Date(now.getTime() + 25000);
            System.out.println("Creating periodic timer at " + now);
            Timer t = mdc.getTimerService().createTimer(10000, 10000, 
                                                        expire);
            System.out.println("Timer will be cancelled on first expiration after " +
                               expire);
            QueueSender sender = session.createSender(queue);
            ObjectMessage outMessage = session.createObjectMessage();
            outMessage.setObject(expire);
            System.out.println("Sending message " + outMessage);
            sender.send(outMessage);
            System.out.println("message sent");
            
        } catch(Exception e) {
            System.out.println("got exception in message bean");
            e.printStackTrace();
        } finally {
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
