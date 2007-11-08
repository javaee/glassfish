package com.sun.s1peqe.connector.mq.simplestress.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.CreateException;
import javax.naming.*;
import javax.jms.*;

public class SimpleMessageBean implements MessageDrivenBean,
    MessageListener {

    Context                 jndiContext = null;
    QueueConnectionFactory  queueConnectionFactory = null;
    QueueConnection         queueConnection = null;
    QueueSession            queueSession = null;
    Queue                   queue = null;
    QueueSender             queueSender = null;
    final int               NUM_MSGS = 100;

    private transient MessageDrivenContext mdc = null;
    private Context context;

    public SimpleMessageBean() {
        System.out.println("In SimpleMessageBean.SimpleMessageBean()");
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        System.out.println("In "
            + "SimpleMessageBean.setMessageDrivenContext()");
	this.mdc = mdc;
    }

    public void ejbCreate() {
	System.out.println("In SimpleMessageBean.ejbCreate()");
        try {
            jndiContext = new InitialContext();
            queueConnectionFactory = (QueueConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/QCFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/clientQueue");
        } catch (NamingException e) {
            System.out.println("JNDI lookup failed: " +
                e.toString());
        }
    }

    public void onMessage(Message inMessage) {
        TextMessage msg = null;

        try {
            if (inMessage instanceof TextMessage) {
                msg = (TextMessage) inMessage;
                System.out.println("MESSAGE BEAN: Message received: "
                    + msg.getText());
		long sleepTime = msg.getLongProperty("sleeptime");
		System.out.println("Sleeping for : " + sleepTime + " milli seconds ");
		Thread.sleep(sleepTime);
		queueConnection =
	            queueConnectionFactory.createQueueConnection();
		queueSession =
	            queueConnection.createQueueSession(false,
		    Session.AUTO_ACKNOWLEDGE);
		queueSender = queueSession.createSender(queue);
		TextMessage message = queueSession.createTextMessage();

		message.setText("REPLIED:" + msg.getText());
		message.setIntProperty("replyid", msg.getIntProperty("id") );
		System.out.println("Sending message: " +
		message.getText());
		queueSender.send(message);
            } else {
                System.out.println("Message of wrong type: "
                    + inMessage.getClass().getName());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (Throwable te) {
            te.printStackTrace();
        } finally {
	    try {
	        queueSession.close();
		queueConnection.close();
	    } catch (Exception e) {
	    }
	}
    }  // onMessage

    public void ejbRemove() {
        System.out.println("In SimpleMessageBean.remove()");
    }
} // class

