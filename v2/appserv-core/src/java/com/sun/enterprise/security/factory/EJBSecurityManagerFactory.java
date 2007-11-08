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

/*
 * EJBSecurityManagerFactory.java
 *
 * Created on June 9, 2003, 5:42 PM
 */

package com.sun.enterprise.security.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sun.enterprise.SecurityManager;
import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.security.application.EJBSecurityManager;

import java.util.logging.Level;
/**
 * EJB Security Manager Factory Implementation
 * @author  Harpreet Singh
 */
public final class EJBSecurityManagerFactory
    extends AbstractSecurityManagerFactory {

    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static SecurityManagerFactory _theFactory;

    private Map CONTEXT_ID = new HashMap();

    /** Creates a new instance of EJBSecurityManagerFactory */
    private EJBSecurityManagerFactory() {
    }
    
    public static SecurityManagerFactory getInstance() {
        try {
            rwLock.readLock().lock();
            if (_theFactory != null) {
                return _theFactory;
            }
        } finally {
            rwLock.readLock().unlock();
        }

        try {
            rwLock.writeLock().lock();
            if (_theFactory == null) {
                _theFactory =
                    (SecurityManagerFactory)new EJBSecurityManagerFactory();
            }
            return _theFactory;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public SecurityManager getSecurityManager(String contextId){            
        if (_poolHas(contextId)){
            return (SecurityManager)_poolGet(contextId);
        }
        return null;
    }        
    
    public SecurityManager createSecurityManager(Descriptor descriptor) {
        SecurityManager ejbSM = null;
        String contextId = null;
        String appName = null;
        try {
            ejbSM = EJBSecurityManager.getInstance(descriptor);
            // if the descriptor is not a EjbDescriptor the EJBSM will 
            // throw an exception. So the following will always work.
            EjbDescriptor ejbdes = (EjbDescriptor) descriptor;
            appName = ejbdes.getApplication().getRegistrationName();
            contextId = EJBSecurityManager.getContextID(ejbdes);
            if(_logger.isLoggable(Level.FINE)){
                _logger.log(Level.FINE,
                "[EJB-Security] EJB Security:Creating EJBSecurityManager for contextId = "
                +contextId);
            }

        } catch (Exception e){
            _logger.log(Level.FINE,
            "[EJB-Security] FATAl Exception. Unable to create EJBSecurityManager: "
            + e.getMessage() );
            throw new RuntimeException(e);
        } 

	synchronized (CONTEXT_ID) {
	    List lst = (List)CONTEXT_ID.get(appName);
	    if(lst == null){
		lst = new ArrayList();
		CONTEXT_ID.put(appName, lst);
	    }
	    if (!lst.contains(contextId)) {
		lst.add(contextId);
	    }
	}

        _poolPut(contextId, ejbSM);
        return ejbSM;     
    }

    public String[] getAndRemoveContextIdForEjbAppName(String appName){
	synchronized(CONTEXT_ID) {
            List contextId = (List) CONTEXT_ID.get(appName);
            if (contextId == null) {
                return null;
            }
            String[] rvalue = new String[contextId.size()];
            rvalue = (String[])contextId.toArray(rvalue);

	    CONTEXT_ID.remove(appName);
	    return rvalue;
	}
    }
}
