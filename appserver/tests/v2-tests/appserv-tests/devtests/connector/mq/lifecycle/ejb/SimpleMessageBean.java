package com.sun.s1peqe.mq.cmt.excpt.ejb;

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

	updateDB();

        try {
            if (inMessage instanceof TextMessage) {
                msg = (TextMessage) inMessage;
                System.out.println("MESSAGE BEAN: Message received: "
                    + msg.getText());
            } else {
                System.out.println("Message of wrong type: "
                    + inMessage.getClass().getName());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (Throwable te) {
            te.printStackTrace();
        }
	throw new RuntimeException("Test exception");
    }  // onMessage

    private void updateDB() {
        try {
            //Class.forName("com.inet.ora.OraDriver");
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            ////String url = "jdbc:inetora::wrx.india.sun.com:1521:dbsmpl1";
            String url = "jdbc:derby://localhost:1527/testdb;create=true;";
            java.sql.Connection con = DriverManager.getConnection(url,"dbuser", "dbpassword");
            //java.sql.Connection con = DriverManager.getConnection(url,"connector", "connector");
            ResultSet rs = con.createStatement().executeQuery("select exCount from mq_cmt_excpt");
	    int count = 0;
	    while (rs.next()){
	        count = rs.getInt(1);
	    }
	    rs.close();
	    count++;
	    String qry = "update mq_cmt_excpt set exCount="+ count ;
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
