/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.acme;

import javax.ejb.*;
import javax.jms.*;

/**
 *
 * @author marina vatkina
 */

@TransactionManagement(TransactionManagementType.BEAN)
@MessageDriven(mappedName="jms/ejb_mdb_InQueue", description="mymessagedriven bean description")
public class MdBean implements MessageListener {

    @EJB MyBean bean;

    public void onMessage(Message message) {
        System.out.println("Got message!!!");

        try {
          if (message instanceof TextMessage) {
            TextMessage msg = (TextMessage) message;
            String txMsg = msg.getText();
            System.out.println("mdb: txMsg=" + txMsg);
            bean.record(txMsg);
          }
        } catch (Throwable e ) {
          e.printStackTrace();
        }

    }
}
