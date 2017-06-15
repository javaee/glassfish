package com.sun.s1peqe.ejb.mdb.simple.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.CreateException;
import javax.naming.*;
import javax.jms.*;
import java.util.*;

public class SimpleMessageBean implements MessageDrivenBean,
    MessageListener {

    private transient MessageDrivenContext mdc = null;
    private Context context;
    private TextMessage msg = null;
    private ArrayList messageList=new ArrayList();
    public javax.naming.Context jndiContext;
    private static int beancount=0;
    public static final String  TOPICCONFAC = "jms/TCFactory";
    public SimpleMessageBean() {
        beancount++;
        System.out.println("MESSAGE BEAN:["+beancount+"].SimpleMessageBean()");        
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        System.out.println("In "
            + "MESSAGE BEAN:["+beancount+"].setMessageDrivenContext()");
	this.mdc = mdc;
         try {
            jndiContext=new javax.naming.InitialContext();
             }catch(Throwable e) {
          
          System.out.println(e.toString());
    }
    }

    public void ejbCreate() {
	System.out.println("MESSAGE BEAN: SimpleMessageBean.ejbCreate()");
    }

    public void onMessage(Message inMessage) {
        try {
            //inMessage.acknowledge();
            if (inMessage instanceof TextMessage) {
                msg = (TextMessage) inMessage;
                System.out.println("MESSAGE BEAN: Message received: "
                + msg.getText());
            } else {
                System.out.println("Message of wrong type: "
                + inMessage.getClass().getName());
            }
            messageList.add(msg);
            sendMessage(msg);
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (Throwable te) {
            te.printStackTrace();
        }
    }  // onMessage
    
    public void sendMessage(Message message) {
        System.out.println("MESSAGE BEAN: sendMessage back to appclient");        
        try{
            
            TopicConnectionFactory topicfactory=(TopicConnectionFactory)jndiContext.lookup(TOPICCONFAC);
            Topic topic=(Topic)jndiContext.lookup("java:comp/env/jms/SampleTopic");          
                        
            TopicConnection 
            
            connect = topicfactory.createTopicConnection();
            
            TopicSession session = connect.createTopicSession(false,0);
            
            TopicPublisher publisher=session.createPublisher(topic);
            Thread.sleep(3000);
            publisher.publish(message);                                
            System.out.println("<<Sent Message back to appclient >>");
            
        }catch(Throwable e) {
            System.out.println("!!!!MESSAGE BEAN: sendMessage Exception");
            e.printStackTrace();
        }
    }  
      
    

    public void ejbRemove() {
        System.out.println("In SimpleMessageBean.remove()");
    }
} // class
