/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.mdb.msgbean;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;

public class OperationTestMsgBean implements MessageDrivenBean, 
    MessageListener {

    private MessageDrivenContext mdc = null;
    private OperationTest opTest;
    private boolean beanManagedTx = false;

    public OperationTestMsgBean() {
        System.out.println("In OperationTestMsgBean ctor()");
        opTest = new OperationTest();
        runTest(OperationTest.CTOR);
    };

    public void ejbCreate() {
        System.out.println("In OperationTestMsgBean::ejbCreate() !!");
        try {
            Context context = new InitialContext();
            beanManagedTx = 
                ((Boolean) context.lookup("java:comp/env/beanManagedTx")).booleanValue();

            if( beanManagedTx ) {
                System.out.println("BEAN MANAGED TRANSACTIONS");
            } else {
                System.out.println("CONTAINER MANAGED TRANSACTIONS");
            }

            runTest(OperationTest.EJB_CREATE);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void onMessage(Message recvMsg) {
        System.out.println("In OperationTestMsgBean::onMessage() : " 
                           + recvMsg);

        runTest(OperationTest.ON_MESSAGE);
    }

    public void setMessageDrivenContext(MessageDrivenContext mdc) {
        System.out.println
            ("In OperationTestMsgBean::setMessageDrivenContext()!!");
	this.mdc = mdc;
        runTest(OperationTest.SET_CONTEXT);
    }

    public void ejbRemove() {
        System.out.println("In OperationTestMsgBean::remove()!!");
        runTest(OperationTest.EJB_REMOVE);
    }

    private void runTest(int methodType) {
        int txType = beanManagedTx ? OperationTest.BMT : OperationTest.CMT;
        opTest.doTest(txType, methodType, mdc);
    }
}
