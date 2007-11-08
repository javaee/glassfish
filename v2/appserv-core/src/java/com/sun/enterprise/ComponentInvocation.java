/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.enterprise;

import javax.transaction.Transaction;
import javax.transaction.SystemException;
import org.apache.catalina.Context;
import com.sun.ejb.Container;
import com.sun.enterprise.security.SecurityContext;
// IASRI 4660742 START
import java.util.logging.*;
import com.sun.logging.*;
// IASRI 4660742 END

import java.lang.reflect.Method;
import com.sun.xml.rpc.spi.runtime.Tie;

import com.sun.ejb.ComponentContext;

/**
 * represents a general component invocation
 *
 * @author Tony Ng
 */
public class ComponentInvocation {
// IASRI 4660742 START
    private static Logger _logger=null;
    static{
       _logger=LogDomains.getLogger(LogDomains.ROOT_LOGGER);
        }
// IASRI 4660742 END

    static final public int SERVLET_INVOCATION = 0;
    static final public int EJB_INVOCATION = 1;
    static final public int APP_CLIENT_INVOCATION = 2;
    static final public int UN_INITIALIZED = 3;
    static final public int SERVICE_STARTUP = 4;

    private int invocationType = UN_INITIALIZED;


    // the component instance, type Servlet, Filter or EnterpriseBean
    public Object instance;

    // ServletContext for servlet, Container for EJB
    public Object container;

    public Transaction transaction;

    // EJB Context for txn mgr. TODO: rename context as ejbContext
    public ComponentContext context = null;

    //  security context coming in on a call
    // security context changes on a runas call - on a run as call
    // the old logged in security context is stored in here.
    public SecurityContext oldSecurityContext;

    public Boolean auth = null;
    public boolean preInvokeDone = false;

    // true if transaction commit or rollback is
    // happening for this invocation context
    private boolean transactionCompleting = false;

    /**
     * Used by container within handler processing code.
     */ 
    private Tie webServiceTie;
    private Method webServiceMethod;
    
    public ComponentInvocation() 
    {}

    public ComponentInvocation(int invocationType) 
    {
        this.invocationType = invocationType;
    }

    public ComponentInvocation(Object instance, Object container) 
    {
	this.instance = instance;
	this.container = container;
    }
    
    public ComponentInvocation(Object instance, Object container, ComponentContext context)
    {
        this.instance = instance;
        this.container = container;
        this.context = context;
    }
    

    public int getInvocationType() {
        if (invocationType == UN_INITIALIZED) {
            if (container instanceof Context) {
                invocationType = SERVLET_INVOCATION;
                return SERVLET_INVOCATION;
            } else if (container instanceof Container) {
                invocationType = EJB_INVOCATION;
                return EJB_INVOCATION;
            } else {
                invocationType = APP_CLIENT_INVOCATION;
                return APP_CLIENT_INVOCATION;
            }
        }
        else
            return invocationType;
    }


    public Object getInstance() {
        return instance;
    }

    /**
     * Return the Container/ServletContext
     */
    public Object getContainerContext() {
        return container;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction tran) {
        this.transaction = tran;
    }
    /** 
     * Sets the security context of the call coming in
     */
    public void setOldSecurityContext (SecurityContext sc){
	this.oldSecurityContext = sc;
    }
    /**
     * gets the security context of the call that came in
     * before a new context for runas is made
     */
    public SecurityContext getOldSecurityContext (){
	return oldSecurityContext;
    }

    public boolean isTransactionCompleting() {
        return transactionCompleting;
    }

    public void setTransactionCompeting(boolean value) {
        transactionCompleting = value;
    }

    
    public void setWebServiceTie(Tie tie) {
        webServiceTie = tie;
    }

    public Tie getWebServiceTie() {
        return webServiceTie;
    }

    public void setWebServiceMethod(Method method) {
        webServiceMethod = method;
    }

    public Method getWebServiceMethod() {
        return webServiceMethod;
    }
    
    public String toString() {
        String str = instance + "," + container;
        if (transaction != null) {
            try {
                str += "," + transaction.getStatus();
            } catch (SystemException ex) {
// IASRI 4660742                ex.printStackTrace();
// START OF IASRI 4660742
            _logger.log(Level.SEVERE,"enterprise.system_exception",ex);
// END OF IASRI 4660742
            }
        } else {
            str += ",no transaction";
        }
        return str;
    }
}
