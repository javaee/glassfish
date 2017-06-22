/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.bmp.txtests.stateless.ejb;

import javax.ejb.*;

import com.sun.s1asdev.ejb.bmp.txtests.simple.ejb.SimpleBMPHome;
import com.sun.s1asdev.ejb.bmp.txtests.simple.ejb.SimpleBMP;
import com.sun.s1asdev.ejb.bmp.txtests.simple.ejb.CustomerInfo;

import javax.naming.*;
import javax.rmi.PortableRemoteObject;

public class SLSBEJB
    implements SessionBean 
{
	private SessionContext sessionCtx;

	public void ejbCreate() {}

	public void setSessionContext(SessionContext sc) {
		sessionCtx = sc;
	}

	// business method to create a timer
	public boolean doRollbackTest(int id) {
        boolean retVal = false;
        boolean doneRollback = false;
        try {
            String lookupName = "java:comp/env/ejb/SimpleBMPHome";
            Object objRef = (new InitialContext()).lookup(lookupName);
            SimpleBMPHome entityHome = (SimpleBMPHome)
                PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            sessionCtx.getUserTransaction().begin();
            entityHome.create(id);
            
            //This must be non null
            SimpleBMP entity = (SimpleBMP)
                entityHome.findByPrimaryKey(new Integer(id));

            int foundID = entity.getID();
            if (foundID != id) {
                return false;
            }

            sessionCtx.getUserTransaction().rollback();
            doneRollback = true;

            try {
                entity = (SimpleBMP)
                    entityHome.findByPrimaryKey(new Integer(id));
            } catch (FinderException finderEx) {
                //We must get this exception
                retVal = true;
            }
        } catch (Throwable th) {
            th.printStackTrace();
            if (! doneRollback) {
                try {
                    sessionCtx.getUserTransaction().rollback();
                } catch (Throwable rollTx) {
                }
            }
        }

		return retVal;
	}

    public boolean doReturnParamTest(int id) {

        boolean retVal = false;
        try {
            String lookupName = "java:comp/env/ejb/SimpleBMPHome";
            Object objRef = (new InitialContext()).lookup(lookupName);
            SimpleBMPHome entityHome = (SimpleBMPHome)
                PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            sessionCtx.getUserTransaction().begin();
            entityHome.create(id);
            
            //This must be non null
            SimpleBMP entity = (SimpleBMP)
                entityHome.findByPrimaryKey(new Integer(id));

            int foundID = entity.getID();
            if (foundID != id) {
                return false;
            }

            CustomerInfo customerInfo = entity.getCustomerInfo();
            retVal = (foundID == customerInfo.getCustomerID());
            
        } catch (Throwable th) {
            th.printStackTrace();
        } finally {
            try {
                sessionCtx.getUserTransaction().rollback();
            } catch (Throwable rollTx) {
            }
        }

		return retVal;
	}

	public void ejbRemove() {}

	public void ejbActivate() {}

	public void ejbPassivate() {}

}
