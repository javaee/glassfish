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

import javax.ejb.EntityContext;
import java.util.Collection;
import java.util.logging.*;
import javax.ejb.*;

import com.sun.ejte.ccl.reporter.*;



public abstract class CustomerBean extends EnterpriseBeanLogger implements javax.ejb.EntityBean
{
    AccountLocalHome accountHome=null;
    public javax.naming.Context jndiContext=null;
    Object objref=null;
    
    private static Logger logger = Logger.getLogger("bank.admin");
    private static ConsoleHandler ch = new ConsoleHandler();
    
    public abstract String getCustomerID();
    public abstract void setCustomerID(String id);
    
    public abstract String getCustomerName();
    public abstract void setCustomerName(String name);
    
    //relationship fields
    
    public abstract Collection getAccounts();
    public abstract void setAccounts(Collection account);
    

        //Business methods

    public String ejbCreateCustomer(String id,String name)
    throws javax.ejb.CreateException
    {
        
        toXML("ejbCreateCustomer","Enter");
        //if(ejbContext.isCallerInRole("ADMIN"))
        if(ejbContext.isCallerInRole("Administrator"))
            toXML("ejbCreateCustomer","isCallerInRole: "+"administrator");
        else
            toXML("ejbCreateCustomer","isCallerInRole: "+"NOT IN administrator");
        
        setCustomerID(id);
        setCustomerName(name);
        toXML("ejbCreateCustomer","Created Customer: "+id);
        java.security.Principal principal=ejbContext.getCallerPrincipal();
        toXML("ejbCreateCustomer","getCallerPrincipal: "+principal);
        toXML("ejbCreateCustomer","Exit");
        
        return null;
    }
    
    public boolean TestCallerInRole()
    {
        boolean isCallerInRole=false;
        //if(ejbContext.isCallerInRole("ADMIN"))
        if(ejbContext.isCallerInRole("Administrator"))
        {
            toXML("TestCallerInRole","isCallerInRole: "+"Administrator");
            isCallerInRole=true;
        }
        else
        {
            toXML("TestCallerInRole","isCallerInRole: "+ejbContext.getCallerPrincipal().getName());
            isCallerInRole=false;
        }
        return isCallerInRole;
        
    }

    public void addAccount(AccountDataObject ado)
    {
        toXML("addAccount","Enter");
        try
        {
            java.security.Principal principal=ejbContext.getCallerPrincipal();
            toXML("addAccount","getCallerPrincipal: "+ principal);
            
            if(ejbContext.isCallerInRole("Administrator"))
                toXML("addAccount","isCallerInRole: "+"administrator");
            else
                toXML("addAccount","isCallerInRole: "+"NOT IN administrator");
            
            logger.info("CustomerBean\t Primary key of acct being added\t"+ado.getAccountID());
            AccountLocal accountLocal=accountHome.createAccount(ado);
            getAccounts().add(accountLocal);
            toXML("addAccount","Added account by Calling CMR field-add");
        }
        catch(DuplicateKeyException e)
        {
            logLocalXMLException(e,"Account already exists");
        }
        catch(Throwable e)
        {
           logXMLException((Exception)e,"Exception occured while adding Account");
            throw new EJBException((Exception)e);
        }
        toXML("addAccount","Exit");
    }

    public void ejbPostCreateCustomer(String id,String name)
    throws javax.ejb.CreateException
    {}

    public EntityContext ejbContext;

    public void ejbActivate() {
    }

    public void ejbPassivate() {}

    public void ejbLoad() {}

    public void ejbStore() {}

    public void ejbRemove() {}

    public void setEntityContext(EntityContext ctx) {
        toXML("setEntityContext","ENTER");
        ejbContext=ctx;
        try {
            /*Creating Account Context as there was no other way to get Remote Interface!!");
            addAccount Needs Remote Interface instead of Local,stupud workaround");*/
            jndiContext=new javax.naming.InitialContext();
            toXML("setEntityContext", "got_JNDIContext");
            
            objref=jndiContext.lookup("java:comp/env/ejb/Account");
            toXML("setEntityContext","Looked_up_Account");
            
            accountHome=(AccountLocalHome)javax.rmi.PortableRemoteObject.narrow(objref,AccountLocalHome.class);
            toXML("setEntityContext","Created Account Home");
            
        }
        catch(Throwable e) {
            logXMLException((Exception)e,"SetEntityContext Failed!");
            throw new EJBException((Exception)e);
        }
        toXML("setEntityContext","Exit");
    }

    public void unsetEntityContext() {
        ejbContext=null;
    }

    public EntityContext getEJBContext() {
        return ejbContext;
    }
  }
