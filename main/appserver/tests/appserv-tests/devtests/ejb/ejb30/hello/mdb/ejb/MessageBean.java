package com.sun.s1asdev.ejb.ejb30.hello.mdb;

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

@javax.interceptor.Interceptors(InterceptorA.class)
@TransactionManagement(TransactionManagementType.BEAN)
@MessageDriven(mappedName="jms/ejb_ejb30_hello_mdb_InQueue", description="mymessagedriven bean description")
 public class MessageBean implements MessageListener {

    @EJB private Hello1 hello1;
    @EJB private Hello2 hello2;

    @Resource(name="jms/MyQueueConnectionFactory", 
              mappedName="jms/ejb_ejb30_hello_mdb_QCF") 
    QueueConnectionFactory qcFactory;

    String mname = null;

    @Resource(mappedName="jms/ejb_ejb30_hello_mdb_OutQueue") Queue clientQueue;

    public void onMessage(Message message) {
        System.out.println("Got message!!!");
        try {
            if (mname == null || !mname.equals("onMessage"))
                throw new EJBException("Expecting method named onMessage got " + mname);
        } finally {
            mname = null;
        }


        QueueConnection connection = null;
        try {
            
            System.out.println("Calling hello1 stateless bean");
            hello1.hello("local ejb3.0 stateless");
            System.out.println("Calling hello2 stateful bean");
            hello2.hello("local ejb3.0 stateful");
            hello2.removeMethod();
            try {
                hello2.hello("this call should not go through");
                throw new Exception("bean should have been removed " +
                                    "after removeMethod()");
            } catch(NoSuchEJBException e) {
                System.out.println("Successfully caught EJBException after " +
                                   " accessing removed SFSB");
            }

            connection = qcFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession(false,
                                   Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(clientQueue);
            TextMessage tmessage = session.createTextMessage();
            tmessage.setText("mdb() invoked");
            System.out.println("Sending message");
            sender.send(tmessage);
            System.out.println("message sent");

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
