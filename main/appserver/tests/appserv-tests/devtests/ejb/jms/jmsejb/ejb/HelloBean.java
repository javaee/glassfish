package com.sun.s1asdev.ejb.jms.jmsejb;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.jms.*;
import javax.transaction.UserTransaction;

public class HelloBean implements SessionBean {
    private String str;
    private SessionContext sc;
    private boolean beanManagedTx;
    private Queue queue;
    private QueueConnectionFactory qcFactory;
    private QueueConnection savedConnection = null;
    private QueueSession savedSession = null;

    public HelloBean() {}

    public void ejbCreate(String str) throws RemoteException {
	System.out.println("In ejbCreate !!");
        this.str = str;
        try {
            Context context = new InitialContext();
            beanManagedTx = ((Boolean) context.lookup("java:comp/env/beanManagedTx")).booleanValue();

            if( beanManagedTx ) {
                System.out.println("HelloEJB has BEAN MANAGED TRANSACTIONS");
            } else {
                System.out.println("HelloEJB has CONTAINER MANAGED TX");
            }

	    String name = (String) context.lookup("java:comp/env/user");
	    Double d = (Double) context.lookup("java:comp/env/number");
            System.out.println("Hello EJB - saying hello to " + name + 
			    ",number is " + d);
            queue = (Queue) context.lookup("java:comp/env/jms/QueueName");

            qcFactory = 
                (QueueConnectionFactory) context.lookup("java:comp/env/jms/MyQueueConnectionFactory");

        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException();
        }
    }

    /**
     * Send a message.  In bmt case, create session BEFORE
     * starting tx.
     */
    public String sendMessage1(String msg) throws EJBException {
        QueueConnection connection = null;
        QueueSession session = null;
        try {
            connection = qcFactory.createQueueConnection();
            session = connection.createQueueSession(true, 0);
            if( beanManagedTx ) {
                sc.getUserTransaction().begin();
            }
            sendMessageInternal(session, msg);

            // Make sure calling commit when global tx is active
            // results in the correct exception.
            try {
                session.commit();
                throw new java.lang.IllegalStateException
                    ("Didn't get session.commit exception");
            } catch(javax.jms.TransactionInProgressException tipe) {
                System.out.println
                    ("Successfully got tx in progress excep " +
                     "after calling session.commit in a global tx");
            } catch(javax.jms.JMSException jmse) {
                System.out.println
                    ("Got JMSException - it's also ok - " +
                     "after calling session.commit in a global tx");
            } catch(java.lang.IllegalStateException e) {
                throw new JMSException
                    ("Should have gotten exception for tx-in-progress");
            }

            if( beanManagedTx ) {
                sc.getUserTransaction().commit();
            }

            // Now make sure that even though the global transaction 
            // is over, calling commit results in an exception.
            // since we're in an ejb.
            try {
                session.commit();
                throw new JMSException
                    ("Didn't get session.commit exception");
            } catch(javax.jms.JMSException jmse) {
                System.out.println("Successfully got session.commit " +
                                   "exception in ejb");
            }

            try {
                session.rollback();
                throw new JMSException
                    ("Didn't get session.rollback exception");
            } catch(javax.jms.JMSException jmse) {
                System.out.println("Successfully got session.rollback " +
                                   "exception in ejb");
            }


            System.out.println("Sent message");
        } catch(Exception e) {
            e.printStackTrace();
            if( beanManagedTx ) {
                try {
                    sc.getUserTransaction().rollback();
                } catch(Exception ne) {}
            }
            throw new EJBException(e);
        } finally {
            try {
                if( connection != null ) {
                    connection.close();

                    // no-op
                    connection.close();

                    try {
                        // no-op
                        session.close();
                        session.createObjectMessage();
                        throw new JMSException
                            ("Didn't get expected illegal state exception");
                    } catch(javax.jms.IllegalStateException j1) {
                        System.out.println
                            ("Successfully got illegal state exception " +
                             "when calling session method after close");
                    } catch(javax.jms.JMSException j2) {
                        throw new JMSException("Got wrong jmsexception");
                    }
                    
                    try {
                        session.getMessageListener();
                        throw new JMSException
                            ("Didn't get expected illegal state exception");
                    } catch(javax.jms.IllegalStateException j3) {
                        System.out.println
                            ("Successfully got illegal state exception " +
                             "when calling session method after close");
                    } catch(javax.jms.JMSException j4) {
                        throw new JMSException("Got wrong jmsexception");
                    }
                }
            } catch(Exception ne) {}
        }
        return msg;
    }

    /**
     * Send a message.  In bmt case, create session AFTER
     * starting tx.  In cmt case, there won't be any
     * difference between this method and sendMessage1
     */
    public String sendMessage2(String msg) throws EJBException {
        QueueConnection connection = null;
        try {
            if( beanManagedTx ) {
                sc.getUserTransaction().begin();
            }

            connection = qcFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession(true, 0);

            sendMessageInternal(session, msg);
            
            session.close();

            if( beanManagedTx ) {
                sc.getUserTransaction().commit();
            }

            System.out.println("Sent message");
        } catch(Exception e) {
            e.printStackTrace();
            if( beanManagedTx ) {
                try {
                    sc.getUserTransaction().rollback();
                } catch(Exception ne) {}
            }
            throw new EJBException(e);
        } finally {
            try {
                if( connection != null ) {
                    connection.close();
                }
            } catch(Exception ne) {}
        }
        return msg;
    }

    /**
     * Create a session and store it as part of 
     * the ejb's state.  Then send a message.
     * Corresponding recv method will close the session.
     */
    public String sendMessage3(String msg) throws EJBException {
        try {
            if( beanManagedTx ) {
                sc.getUserTransaction().begin();
            }

            savedConnection = qcFactory.createQueueConnection();
            savedSession = savedConnection.createQueueSession(true, 0);

            sendMessageInternal(savedSession, msg);

            if( beanManagedTx ) {
                sc.getUserTransaction().commit();
            }

            System.out.println("Sent message");
        } catch(Exception e) {
            e.printStackTrace();
            if( beanManagedTx ) {
                try {
                    sc.getUserTransaction().rollback();
                } catch(Exception ne) {}
            }
            try {
                if( savedConnection != null ) {
                    savedConnection.close();
                    savedConnection = null;
                }
            } catch(Exception ne) {}
            throw new EJBException(e);
        } 
        return msg;
    }

    /**
     * Receive a message. In bmt case, create session
     * BEFORE tx start.
     */
    public void receiveMessage1() throws EJBException {
        QueueConnection connection = null;
        try {
            connection = qcFactory.createQueueConnection();
                
            QueueSession session = connection.createQueueSession(true, 0);

            if( beanManagedTx ) {
                sc.getUserTransaction().begin();
            }
            connection.start();
            Message message = recvMessageInternal(session);
            if( beanManagedTx ) {
                sc.getUserTransaction().commit();
            }

            System.out.println("Received message " + message);
        } catch(Exception e) {
            e.printStackTrace();
            if( beanManagedTx ) {
                try {
                    sc.getUserTransaction().rollback();
                } catch(Exception ne) {}
            }
            throw new EJBException(e);
        } finally {
            try {
                if( connection != null ) {
                    connection.close();
                }
            } catch(Exception e) {}
        }
    }

    /**
     * Receive a message. In bmt case, create session AFTER
     * starting tx.  In cmt case, there won't be any
     * difference between this method and recvMessage1
     */
    public void receiveMessage2() throws EJBException {
        QueueConnection connection = null;
        try {
            if( beanManagedTx ) {
                sc.getUserTransaction().begin();
            }

            connection = qcFactory.createQueueConnection();
                
            QueueSession session = connection.createQueueSession(true, 0);

            connection.start();
            Message message = recvMessageInternal(session);

            if( beanManagedTx ) {
                sc.getUserTransaction().commit();
            }

            session.close();

            System.out.println("Received message " + message);
        } catch(Exception e) {
            e.printStackTrace();
            if( beanManagedTx ) {
                try {
                    sc.getUserTransaction().rollback();
                } catch(Exception ne) {}
            }
            throw new EJBException(e);
        } finally {
            try {
                if( connection != null ) {
                    connection.close();
                }
            } catch(Exception ne) {}
        }
    }

    /**
     * Receive a message using a session that was saved as
     * part of the ejbs state.  Then close the session
     * and its connection.
     */
    public void receiveMessage3() throws EJBException {
        if( savedConnection == null ) {
            System.out.println("saved connection is null");
            return;
        }
        try {
            if( beanManagedTx ) {
                sc.getUserTransaction().begin();
            }
            savedConnection.start();
            Message message = recvMessageInternal(savedSession);
            if( beanManagedTx ) {
                sc.getUserTransaction().commit();
            }

            System.out.println("Received message " + message);
        } catch(Exception e) {
            e.printStackTrace();
            if( beanManagedTx ) {
                try {
                    sc.getUserTransaction().rollback();
                } catch(Exception ne) {}
            }
            throw new EJBException(e);
        } finally {
            try {
                if( savedConnection != null ) {
                    savedConnection.close();
                    savedConnection = null;
                }
            } catch(Exception e) {}
        }
    }

    /**
     * Send a message but don't commit.  sendMessagePart2
     * will commit.  This method will only work with
     * when bean has bean managed tx.  
     */
    public String sendMessage4Part1(String msg) throws EJBException {
        try {
            if( beanManagedTx ) {
                sc.getUserTransaction().begin();
            } else {
                System.out.println("skipping sendMessage4Part1 b/c of CMT");
                return "skipped";
            }

            savedConnection = qcFactory.createQueueConnection();
                
            savedSession = savedConnection.createQueueSession(true, 0);
            sendMessageInternal(savedSession, msg);

        } catch(Exception e) {
            e.printStackTrace();
            if( beanManagedTx ) {
                try {
                    sc.getUserTransaction().rollback();
                } catch(Exception ne) {}
            }
            try {
                if( savedConnection != null ) {
                    savedConnection.close();
                    savedConnection = null;
                }
            } catch(Exception ne) {}
            throw new EJBException(e);
        } 
        return msg;
    }

    /**
     * Commit send that occurred in sendMessage4Part1
     */
    public String sendMessage4Part2(String msg) throws EJBException {
        try {
            if( beanManagedTx ) {
                sc.getUserTransaction().commit();
                System.out.println("Sent message " + msg);
            } else {
                System.out.println("skipping sendMessage4Part2 b/c of CMT");
                return "skipped";
            }
        } catch(Exception e) {
            e.printStackTrace();
            if( beanManagedTx ) {
                try {
                    sc.getUserTransaction().rollback();
                } catch(Exception ne) {}
            }
            try {
                if( savedConnection != null ) {
                    savedConnection.close();
                    savedConnection = null;
                }
            } catch(Exception ne) {}
            throw new EJBException(e);
        } 
        return msg;
    }

    /**
     * Send a message and then rollback.  receiveMessageRollback
     * should be called after this.
     */
    public String sendMessageRollback(String msg) throws EJBException {
        try {
            System.out.println("In sendMessageRollback");
            if( beanManagedTx ) {
                sc.getUserTransaction().begin();
            } 
            savedConnection = qcFactory.createQueueConnection();
                
            savedSession = savedConnection.createQueueSession(true, 0);
            sendMessageInternal(savedSession, msg);

            if( beanManagedTx ) {
                sc.getUserTransaction().rollback();
            } else {
                sc.setRollbackOnly();
            }
            System.out.println("Rolled back message");
        } catch(Exception e) {
            e.printStackTrace();
            if( beanManagedTx ) {
                try {
                    sc.getUserTransaction().rollback();
                } catch(Exception ne) {}
            }
            try {
                if( savedConnection != null ) {
                    savedConnection.close();
                    savedConnection = null;
                }
            } catch(Exception ne) {}
            throw new EJBException(e);
        } 
        return msg;
    }

    /**
     * Check to make sure the previous send was rolled back.
     */
    public void receiveMessageRollback() throws EJBException {
        try {
            System.out.println("In receiveMessageRollback()");

            QueueReceiver receiver = savedSession.createReceiver(queue);
            System.out.println("Checking for message on " + queue);
            Message message = receiver.receiveNoWait();

            if( message != null ) {
                throw new Exception("Shouldn't have gotten msg " + 
                                    message);
            } else {
                System.out.println("Successfully DIDN'T receive msg");
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        } finally {
            try {
                if( savedConnection != null ) {
                    savedConnection.close();
                    savedConnection = null;
                }
            } catch(Exception e) {}
        }
    }

    /**
     * Receive a message but don't commit.  recvMessagePart2
     * will commit.  This method will only work with
     * when bean has bean managed tx.  
     */
    public void receiveMessage4Part1() throws EJBException {
        try {
            if( beanManagedTx ) {
                sc.getUserTransaction().begin();
            } else {
                System.out.println("skipping recvMessage4Part1 b/c of CMT");
                return;
            }
            
            savedConnection.start();
            Message message = recvMessageInternal(savedSession);

        } catch(Exception e) {
            e.printStackTrace();
            if( beanManagedTx ) {
                try {
                    sc.getUserTransaction().rollback();
                } catch(Exception ne) {}
            }
            try {
                if( savedConnection != null ) {
                    savedConnection.close();
                    savedConnection = null;
                }
            } catch(Exception ne) {}
            throw new EJBException(e);
        } 
    }
    
    /**
     * Commit the receive that was done in recvMessagePart1
     */
    public void receiveMessage4Part2() throws EJBException {
        try {
            if( beanManagedTx ) {
                sc.getUserTransaction().commit();
                System.out.println("Received msg");
            } else {
                System.out.println("skipping recvMessage4Part2 b/c of CMT");
                return;
            }
        } catch(Exception e) {
            e.printStackTrace();
            if( beanManagedTx ) {
                try {
                    sc.getUserTransaction().rollback();
                } catch(Exception ne) {}
            }
            throw new EJBException(e);
        } finally {
            try {
                if( savedConnection != null ) {
                    savedConnection.close();
                    savedConnection = null;
                }
            } catch(Exception e) {}
        }
    }

    /**
     * Tests doing a receive that depends on a send.  They
     * must be in separate transactions.
     */
    public void sendAndReceiveMessage() throws RemoteException {
        QueueConnection connection = null;
        try {
            if( beanManagedTx ) {
                sc.getUserTransaction().begin();
            } else {
                System.out.println("Skipping sendAndReceive test b/c CMT");
                return;
            }

            connection = qcFactory.createQueueConnection();
                
            QueueSession session = connection.createQueueSession(true, 0);

            String msgText = "sendandreceive";
            sendMessageInternal(session, msgText);
            sc.getUserTransaction().commit();
            System.out.println("Sent message " + msgText);
            
            //

            connection.start();
            sc.getUserTransaction().begin();
            recvMessageInternal(session);
            sc.getUserTransaction().commit();
            
        } catch(Exception e) {
            e.printStackTrace();
            try {
                sc.getUserTransaction().rollback();
            } catch(Exception ne) {}
            throw new EJBException(e);
        } finally {
            try {
                if( connection != null ) {
                    connection.close();
                }
            } catch(Exception e) {}
        }
    }

    /**
     * Tests doing a send and receive where send is rolled back.
     */
    public void sendAndReceiveRollback() throws RemoteException {
        QueueConnection connection = null;
        try {
            if( beanManagedTx ) {
                System.out.println("In sendAndReceiveRollback");
                sc.getUserTransaction().begin();
            } else {
                System.out.println("Skipping sendAndReceiveRollback test b/c CMT");
                return;
            }

            connection = qcFactory.createQueueConnection();
                
            QueueSession session = connection.createQueueSession(true, 0);

            String msgText = "sendandreceiverollback";
            sendMessageInternal(session, msgText);
            sc.getUserTransaction().rollback();

            System.out.println("Rolled back message send : " + msgText);

            QueueReceiver receiver = session.createReceiver(queue);
            System.out.println("Waiting for message on " + queue);

            sc.getUserTransaction().begin();
            Message message = receiver.receiveNoWait();
            sc.getUserTransaction().commit();
            if( message == null ) {
                System.out.println("Successfully didn't receive msg");
            } else {
                System.out.println("Rollback exception -- received msg " +
                                   message);
                throw new Exception("Rollback exception -- received msg " +
                                    message);
            }
            //
        } catch(Exception e) {
            throw new EJBException(e);
        } finally {
            try {
                if( connection != null ) {
                    connection.close();
                }
            } catch(Exception e) {}
        }
    }

    private void sendMessageInternal(QueueSession session, String msg) throws JMSException {
        // Create a message producer.
        QueueSender sender = session.createSender(queue);

        // Send a message.
        TextMessage message = session.createTextMessage();
        message.setText(msg);
        sender.send(message);
    }

    private Message recvMessageInternal(QueueSession session) throws JMSException {
        // Create a message consumer
        QueueReceiver receiver = session.createReceiver(queue);
        System.out.println("Waiting for message on " + queue);
        Message message = receiver.receive();
        return message;
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
