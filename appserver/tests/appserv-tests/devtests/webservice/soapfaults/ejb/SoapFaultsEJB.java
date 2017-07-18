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

package soapfaultsejb;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.xml.rpc.handler.MessageContext;

public class SoapFaultsEJB implements SessionBean {
    private SessionContext sc;

    public SoapFaultsEJB(){}
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In SoapFaultsEJB::ejbCreate !!");
    }

    public Test1ResponseType test1(String a, String b,
                     Test2RequestType c)
        throws FaultOne, FaultThree, FaultTwo, java.rmi.RemoteException 
    {
        MessageContext msgContext = sc.getMessageContext();
        System.out.println("msgContext = " + msgContext);

        System.out.println("SoapFaultsEJB.test1() called with ");
        System.out.println("a = " + a);
        System.out.println("b = " + b);

        System.out.println("Test2RequestType.a = " + c.getTest2RequestParamA());
        System.out.println("Test2RequestType.b = " + c.getTest2RequestParamB());

        if ("1".equals(a)) {
            System.out.println("SoapFaultsEJB... throwing FaultOne Exception");
            throw new FaultOne("1", "I need a life.");
        }

        if ("2".equals(a)) {
            System.out.println("SoapFaultsEJB... throwing FaultTwo Exception");
            throw new FaultTwo("2", "I am so tired");
        }

        if ("3".equals(a)) {
            System.out.println("SoapFaultsEJB... throwing FaultThree Exception");
            throw new FaultThree("3", "I love fortune cookies");
        }

        Test1ResponseType t = new Test1ResponseType(1,2);
        return t;
    }
        
    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}

}
