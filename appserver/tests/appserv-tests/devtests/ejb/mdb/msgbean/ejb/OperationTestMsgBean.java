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
