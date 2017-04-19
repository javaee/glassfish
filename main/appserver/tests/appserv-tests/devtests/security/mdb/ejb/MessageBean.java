package com.sun.s1asdev.security.mdb;


import javax.ejb.AccessLocalException;
import javax.ejb.MessageDriven;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;
import javax.ejb.EJB;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.ejb.ActivationConfigProperty;

import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.QueueSender;
import javax.jms.TextMessage;
import javax.jms.Session;

import javax.annotation.Resource;
import javax.annotation.security.RunAs;

@TransactionManagement(TransactionManagementType.BEAN)
@MessageDriven(mappedName="jms/security_mdb_InQueue", description="mymessagedriven bean description")
@RunAs("javaee")

 public class MessageBean implements MessageListener {

    @EJB private Hello1 hello1;
    @EJB private Hello2 hello2;

    @Resource(name="jms/MyQueueConnectionFactory", 
              mappedName="jms/security_mdb_QCF") 
    QueueConnectionFactory qcFactory;

    @Resource(mappedName="jms/security_mdb_OutQueue") Queue clientQueue;

    public void onMessage(Message message) {
        System.out.println("Got message!!!");

        QueueConnection connection = null;
        try {
            
            System.out.println("Calling hello1 stateless bean");
            hello1.hello("local ejb3.0 stateless");

            try {
                System.out.println("Calling hello2 stateful bean");
                hello2.hello("local ejb3.0 stateful");
                throw new IllegalStateException("Illegal Access of hello2");
            } catch(AccessLocalException ex) {
                System.out.println("Expected Exception: " + ex);
            }

            hello2.removeMethod();

            connection = qcFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession(false,
                                   Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(clientQueue);
		connection.start();
 
            TextMessage tmessage = session.createTextMessage();
            tmessage.setText("mdb() invoked");
            System.out.println("Sending message");
            sender.send(tmessage);
            System.out.println("message sent");
		connection.close();

        } catch(Exception e) {
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

}
