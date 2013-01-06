package org.glassfish.test.jms.defaultcf.ejb;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;
import javax.naming.InitialContext;

/**
 *
 * @author LILIZHAO
 */
@Stateless(mappedName="SessionBeanDefault/remote")
public class SessionBeanDefault implements SessionBeanDefaultRemote {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Resource(name="myCF0", lookup="jms/__defaultConnectionFactory")
    private ConnectionFactory cf0;
    
    @Resource(name="myCF1", lookup="java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory cf1;
    
    @Resource(name="myCF2")
    private ConnectionFactory cf2;

    @Override
    public void sendMessage(String text) {
        Connection conn = null;
        Session session = null;
        try {
            InitialContext ic = new InitialContext();
            ConnectionFactory o1 = (ConnectionFactory) ic.lookup("java:comp/DefaultJMSConnectionFactory");
            ConnectionFactory o2 = (ConnectionFactory) ic.lookup("java:comp/env/jms/systemDefaultCF");
            if (o1 == null || o2 == null || cf0 == null || cf1 == null || cf2 == null)
                throw new RuntimeException("Failed to lookup up jms default connection factory.");
            conn = cf2.createConnection();
            conn.start();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            TextMessage msg = session.createTextMessage(text);
            MessageProducer p = session.createProducer(queue);
            p.send(msg);
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                    throw new EJBException(e);
                }
            }
            if (conn != null) {
                try {
                    
                    conn.close();
                } catch (Exception e) {
                    throw new EJBException(e);
                }
            }
        }
    }

    @Override
    public boolean checkMessage(String text) {
        Connection conn = null;
        Session session = null;
        try {
            conn = cf1.createConnection();
            conn.start();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer r = session.createConsumer(queue);
            Message msg = r.receive(30000L);
            if (msg instanceof TextMessage) {
                String content = ((TextMessage) msg).getText();
                if (text.equals(content))
                    return true;
            }
            return false;
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                    throw new EJBException(e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    throw new EJBException(e);
                }
            }
        }
    }
}
