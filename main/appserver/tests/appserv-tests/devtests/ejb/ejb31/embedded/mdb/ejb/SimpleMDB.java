/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.tests.ejb.mdb;

import javax.ejb.*;
import javax.jms.*;

/**
 *
 * @author marina vatkina
 */

@TransactionManagement(TransactionManagementType.BEAN)
@MessageDriven(mappedName="jms/ejb_mdb_Queue", description="mymessagedriven bean description")
public class SimpleMDB implements MessageListener {

    @EJB SimpleEjb ejb;

    public void onMessage(Message message) {
        System.err.println("Got message!!!");

        try {
          if (message instanceof TextMessage) {
            TextMessage msg = (TextMessage) message;
            String txMsg = msg.getText();
            System.err.println("mdb: txMsg=" + txMsg);
            ejb.ack();
          }
        } catch (Throwable e ) {
          e.printStackTrace();
        }

    }
}
