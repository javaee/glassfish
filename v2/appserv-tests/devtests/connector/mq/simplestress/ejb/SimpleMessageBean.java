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
    ConnectionFactory       connectionFactory = null;
    Connection              connection = null;
    Session                 session = null;
    Queue                   queue = null;
    MessageProducer         msgProducer = null;
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
            connectionFactory = (ConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/CFactory");
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
		connection =
	            connectionFactory.createConnection();
		session =
	            connection.createSession(false,
		    Session.AUTO_ACKNOWLEDGE);
		MessageProducer msgProducer= session.createProducer(queue);
		TextMessage message = session.createTextMessage();

		message.setText("Reply for : " + msg.getText());
		System.out.println("Sending message: " +
		message.getText());
		msgProducer.send(message);
            } else {
                System.out.println("Message of wrong type: "
                    + inMessage.getClass().getName());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (Throwable te) {
            te.printStackTrace();
        }
    }  // onMessage

    public void ejbRemove() {
        System.out.println("In SimpleMessageBean.remove()");
    }
} // class

