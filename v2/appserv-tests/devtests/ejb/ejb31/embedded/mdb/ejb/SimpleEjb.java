package org.glassfish.tests.ejb.mdb;

import javax.ejb.Singleton;
import javax.jms.*;
import javax.annotation.Resource;


/**
 * @author Marina Vatkina
 */
@Singleton
public class SimpleEjb {


    @Resource(name="jms/MyQueueConnectionFactory", mappedName="jms/ejb_mdb_QCF")
    QueueConnectionFactory fInject;

    @Resource(mappedName="jms/ejb_mdb_Queue")
    Queue qInject;

    boolean mdbCalled = false;

    public String saySomething() throws Exception {
        send();
        return "hello";
    }

    public void ack() {
        mdbCalled = true;
    }

    public boolean getAck() {
        return mdbCalled;
    }

    private void send() throws Exception {
        QueueConnection qConn = fInject.createQueueConnection();
        QueueSession qSession = qConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        QueueSender qSender = qSession.createSender(qInject);
        TextMessage tMessage = null;

        tMessage = qSession.createTextMessage("MY-MESSAGE");
        qSender.send(tMessage);

        qSession.close();
        qConn.close();
        System.err.println("Sent successfully");
    }

}
