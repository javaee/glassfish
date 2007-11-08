/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.mdb.msgbean;

import java.util.Vector;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.naming.*;
import javax.jms.*;
import java.sql.*;
import javax.sql.*;

public class MsgBean implements MessageDrivenBean, MessageListener {

    // Keep track of all messages marked for redelivery due to
    // a rollback.  This collection is shared across all instances
    // of an mdb in a single application.  NOTE : This assumption is
    // is implementation-specific and breaks the EJB programming
    // contract.  However, it makes checking the rollback logic
    // much easier...
    private static Vector rollbackMessages = new Vector();

    static {
        rollbackMessages = new Vector();
        System.out.println("Instantiating static rollback msg vector " +
                           "in MsgBean");
    }

    private Context context;

    protected MessageDrivenContext mdc = null;

    private boolean beanManagedTx = false;

    private QueueConnectionFactory queueConFactory;
    private TopicConnectionFactory topicConFactory;
    private DataSource dataSource;

    public MsgBean() {
        System.out.println("In MsgBean::MsgBean()!");
    };

    public void ejbCreate() {
        System.out.println("In MsgBean::ejbCreate() !!");

        try {
            context = new InitialContext();
            beanManagedTx = ((Boolean) context.lookup
                             ("java:comp/env/beanManagedTx")).booleanValue();
            
            if( beanManagedTx ) {
                System.out.println("BEAN MANAGED TRANSACTIONS");
            } else {
                System.out.println("CONTAINER MANAGED TRANSACTIONS");
            }
            
            dataSource = (DataSource) 
                context.lookup("java:comp/env/jdbc/AccountDB");
            
            // Create a Queue Session.
            queueConFactory = (QueueConnectionFactory) 
                context.lookup("java:comp/env/jms/MyQueueConnectionFactory");
            
            topicConFactory = (TopicConnectionFactory)
                context.lookup("java:comp/env/jms/MyTopicConnectionFactory");
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException("ejbCreate error");
        }
    }

    public void onMessage(Message recvMsg) {

        try {

            String messageID = recvMsg.getJMSMessageID();

            boolean doJms = (recvMsg.getJMSReplyTo() != null);
            boolean doJdbc = 
                recvMsg.getBooleanProperty("doJdbc");
            boolean rollbackEnabled = 
                recvMsg.getBooleanProperty("rollbackEnabled");

            System.out.println("In MsgBean::onMessage() : " + messageID);
            System.out.println("jdbc enabled : " + doJdbc + " , " +
                               "jms reply enabled : " + doJms + " , " +
                               "rollback enabled : " + rollbackEnabled);
            
            if( beanManagedTx ) {
                mdc.getUserTransaction().begin();
            } else if( rollbackEnabled ) {
                if( rollbackMessages.contains(messageID) ) {
                    if( !recvMsg.getJMSRedelivered() ) {
                        throw new RuntimeException
                            ("Received msg multiple times " +
                             "but redelivered flag not set" +
                             " : " + recvMsg);
                    } else {
                        System.out.println("Got redelivered message " + 
                                           messageID);
                    }
                }
            }

            doStuff(doJdbc, recvMsg);

            if( beanManagedTx ) {
                mdc.getUserTransaction().commit();
            } else if( rollbackEnabled ) {
                if( recvMsg.getJMSRedelivered() ) {
                    System.out.println("Got redelivered message " + 
                                       messageID);
                    // no more rollbacks -- container will commit tx
                } else {
                    rollbackMessages.add(recvMsg);
                    System.out.println("Rolling back message " + 
                                       messageID);
                    mdc.setRollbackOnly();
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected void doStuff(boolean doJdbc, Message recvMsg) throws Exception {
        
        Destination replyTo = recvMsg.getJMSReplyTo();
        
        if( replyTo != null ) {
            doJmsStuff(replyTo, recvMsg);
        }
        
        if( doJdbc) {
            doJdbcStuff();
        }
    }

    private void doJdbcStuff() {
        java.sql.Connection dbCon = null;
        try {
            dbCon = dataSource.getConnection();
            Statement stmt = dbCon.createStatement();
            String query = "SELECT balance from ejb_mdb_msgbean_accounts where accountId = 'richie rich'";
            ResultSet results = stmt.executeQuery(query);
            results.next();
            System.out.println("Richie rich has " + results.getInt("balance") + " dollars");
            results.close();
            stmt.close();
        } catch(Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if( dbCon != null ) {
                    dbCon.close();
                }
            } catch(Exception e) {}
        }
    }

    private void doJmsStuff(Destination replyTo, Message recvMsg) {

        QueueConnection queueCon = null;
        TopicConnection topicCon = null;
        
        try {
            
            if( replyTo instanceof Queue ) {
                queueCon = queueConFactory.createQueueConnection();
                
                // parameters to createQueueSession are ignored when there
                // is a tx context.  If there's a CMT unspecified tx context,
                // e.g. CMT NotSupported, jms activity must be coded 
                // in a defensive way since the container has a lot of leeway
                // in how it performs the work.  
                QueueSession queueSession = queueCon.
                    createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
                
                QueueSender sender  = queueSession.
                    createSender((Queue)replyTo);
                
                TextMessage sendMsg = queueSession.createTextMessage();
                sendMsg.setText("Reply for " + ((TextMessage)recvMsg).getText() + " " + recvMsg.getJMSMessageID());
                sender.send(sendMsg);
                System.out.println("Sent reply " + sendMsg + 
                                   " to " + replyTo);
            } else {
                topicCon = topicConFactory.createTopicConnection();
                
                TopicSession topicSession = topicCon.
                    createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                
                TopicPublisher publisher =
                    topicSession.createPublisher((Topic)replyTo);
                
                TextMessage sendMsg = topicSession.createTextMessage();
                sendMsg.setText("Reply for " + ((TextMessage)recvMsg).getText() + " " + recvMsg.getJMSMessageID());
                publisher.publish(sendMsg);
                System.out.println("Published reply " + sendMsg + 
                                   " to " + replyTo);
            }
        } catch(JMSException jmse) {
            jmse.printStackTrace();
        } finally {
            if( queueCon != null ) {
                try { queueCon.close(); } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            if( topicCon != null ) {
                try { topicCon.close(); } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
        
    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        System.out.println("In MsgBean::setMessageDrivenContext()!!");
	this.mdc = mdc;
    }

    public void ejbRemove() {
        System.out.println("In MsgBean::remove()!!");
    }
}
