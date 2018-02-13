/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import java.util.HashMap;
import javax.ejb.EntityContext;
import java.util.logging.*;
import com.sun.ejte.ccl.reporter.*;

public abstract class AccountBean extends EnterpriseBeanLogger implements javax.ejb.EntityBean
{
    
    private static Logger logger = Logger.getLogger("bank.admin");
    private static ConsoleHandler ch = new ConsoleHandler();
    
    public abstract String getAccountID();
    public abstract void setAccountID(String id);
    
    public abstract Double getAmount();
    public abstract void setAmount(Double amt);
    
    public abstract HashMap getPrivileges();
    public abstract void setPrivileges(HashMap privileges);
    

        //Business methods

    public String ejbCreateAccount(AccountDataObject ado)
    throws javax.ejb.CreateException
    {

        toXML("ejbCreateAccount","Enter");
        setAccountID(ado.getAccountID());
        setAmount(ado.getAmount());
        setPrivileges(ado.getPermissionList());
        toXML("ejbCreateAccount","Created Account: "+ ado);
        toXML("ejbCreateAccount","Exit");

        return null;
    }


    public void ejbPostCreateAccount(AccountDataObject ado)
    throws javax.ejb.CreateException
    {}

    public AccountDataObject getDAO()
    {
        return new AccountDataObject(getAccountID(),getAmount(),getPrivileges());
    }

    public EntityContext ejbContext;

    public void ejbActivate() {
    }

    public void ejbPassivate() {}

    public void ejbLoad() {
        toXML("ejbLoad","CMP Account");
    }

    
    public void ejbStore() {
        toXML("ejbStore","CMP Account");
    }

    public void ejbRemove() {
        toXML("ejbRemove","CMP Account");
    }

    public void setEntityContext(EntityContext ctx) {
        ejbContext=ctx;
    }

    public void unsetEntityContext() {
        ejbContext=null;
    }

    public EntityContext getEJBContext() {
        return ejbContext;
    }


  }
