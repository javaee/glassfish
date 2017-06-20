package com.sun.s1peqe.mq.queue.test.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.CreateException;
import javax.naming.*;
import javax.jms.*;
import java.sql.*;

public class SimpleMessageBean implements MessageDrivenBean,
    MessageListener {

    private transient MessageDrivenContext mdc = null;
    private Context context;
    private static int i;

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
    }

    public void onMessage(Message inMessage) {
        TextMessage msg = null;

        try {
            if (inMessage instanceof TextMessage) {
                msg = (TextMessage) inMessage;
                System.out.println("MESSAGE BEAN: Message received: "
                    + msg.getText());
	        updateDB(msg.getText());
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

    private void updateDB(String msg) {
        try {
	    Class.forName("com.pointbase.jdbc.jdbcUniversalDriver");
            String url = "jdbc:pointbase:server://localhost:9092/sqe-samples,new";
	    java.sql.Connection con = DriverManager.getConnection(url,"DBUSER","DBPASSWORD");
	    String qry = "insert into mq_queue_test values("+ msg + ")" ;
	    con.createStatement().executeUpdate(qry);
	    con.close();
	} catch(Exception e) {
           System.out.println("Error:" + e.getMessage());
	} 
    }

    public void ejbRemove() {
        System.out.println("In SimpleMessageBean.remove()");
    }
} // class
