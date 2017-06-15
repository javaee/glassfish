/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
