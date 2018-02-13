/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.connector.cci;

import java.math.BigDecimal;
import java.util.*;
import javax.ejb.*;
import javax.resource.cci.*;
import javax.resource.ResourceException;
import javax.naming.*;
import com.sun.connector.cciblackbox.*;

public class CoffeeBean implements SessionBean {

    private SessionContext sc;
    private String user;
    private String password;
    private ConnectionFactory cf;


    public void ejbCreate() throws CreateException {
    }

    public void setSessionContext(SessionContext sc) {
        try {
            this.sc = sc;
            Context ic = new InitialContext();
            user = (String) ic.lookup("java:comp/env/user");
            password = (String) ic.lookup("java:comp/env/password");
            cf = (javax.resource.cci.ConnectionFactory) ic.lookup("java:comp/env/eis/CCIEIS");
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
    }

    public int getCoffeeCount() {
        int count = -1;
        try {
            Connection con = getCCIConnection();
            Interaction ix = con.createInteraction();
            CciInteractionSpec iSpec = new CciInteractionSpec();
            iSpec.setSchema(user);
            iSpec.setCatalog(null);
            iSpec.setFunctionName("COUNTCOFFEE");
            RecordFactory rf = cf.getRecordFactory();
            IndexedRecord iRec = rf.createIndexedRecord("InputRecord");
            Record oRec = ix.execute(iSpec, iRec);
            Iterator iterator = ((IndexedRecord)oRec).iterator();
            while(iterator.hasNext()) {   
                Object obj = iterator.next();
                if(obj instanceof Integer) {
                    count = ((Integer)obj).intValue();
                }
                else if(obj instanceof BigDecimal) {
                    count = ((BigDecimal)obj).intValue();
                }
            }
            closeCCIConnection(con);
        }catch(ResourceException ex) {
            ex.printStackTrace();
        }
        return count;
    }


    public void insertCoffee(String name, int qty) {
        try {
            Connection con = getCCIConnection();
            Interaction ix = con.createInteraction();
            CciInteractionSpec iSpec = new CciInteractionSpec();
            iSpec.setFunctionName("INSERTCOFFEE");
            iSpec.setSchema(user);
            iSpec.setCatalog(null);
            RecordFactory rf = cf.getRecordFactory();
            IndexedRecord iRec = rf.createIndexedRecord("InputRecord");
            boolean flag = iRec.add(name);
            flag = iRec.add(new Integer(qty));
            ix.execute(iSpec, iRec);
            closeCCIConnection(con);
        }catch(ResourceException ex) {
            ex.printStackTrace();
        }
    }

    private Connection getCCIConnection() {
        Connection con = null;

	System.out.println("<========== IN get cci Connection ===========>");
        try {
            ConnectionSpec spec = new CciConnectionSpec("DBUSER", "DBPASSWORD");
	System.out.println("<========== Created ISpec ===========>");
	System.out.println("CF value : " + cf);
            con = cf.getConnection(spec);
        } catch (ResourceException ex) {
            ex.printStackTrace();
        }
        return con;
    }

    private void closeCCIConnection(Connection con) {
        try {
            con.close();
        } catch (ResourceException ex) {
            ex.printStackTrace();
        }
    }

    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}

} 
