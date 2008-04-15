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

package com.sun.enterprise.security.provider;

import javax.security.jacc.*;

import java.util.logging.*;
import com.sun.logging.LogDomains;

import java.util.*;
import java.io.File;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
 
/** 
 *  Implementation of jacc PolicyConfigurationFactory class
 * @author Harpreet Singh
 * @author Ron Monzillo
 * @version
 */
public class PolicyConfigurationFactoryImpl 
    extends PolicyConfigurationFactory{

    // Table of ContextId->PolicyConfiguration 
    static Map polConfTable = new HashMap(); 

    private static Logger logger = 
	Logger.getLogger(LogDomains.SECURITY_LOGGER);

    private static ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private static Lock rLock = rwLock.readLock();
    private static Lock wLock = rwLock.writeLock();
 
    public PolicyConfigurationFactoryImpl(){
    }

   /**
    * This method is used to obtain an instance of the provider specific
    * class that implements the PolicyConfiguration interface that
    * corresponds to the identified policy context within the provider.
    * The methods of the PolicyConfiguration interface are used to
    * define the policy statements of the identified policy context.
    * <P>
    * If at the time of the call, the identified policy context does not
    * exist in the provider, then the policy context will be created
    * in the provider and the Object that implements the context's
    * PolicyConfiguration Interface will be returned. If the state of the
    * identified context is "deleted" or "inService" it will be transitioned to
    * the "open" state as a result of the call. The states in the lifecycle
    * of a policy context are defined by the PolicyConfiguration interface.
    * <P>
    * For a given value of policy context identifier, this method
    * must always return the same instance of PolicyConfiguration
    * and there must be at most one actual instance of a
    * PolicyConfiguration with a given policy context identifier
    * (during a process context).
    * <P>
    * To preserve the invariant that there be at most one
    * PolicyConfiguration object for a given policy context,
    * it may be necessary for this method to be thread safe.
    * <P>
    * @param contextID A String identifying the policy context whose
    * PolicyConfiguration interface is to be returned. The value passed to
    * this parameter must not be null.
    * <P>
    * @param remove A boolean value that establishes whether or not the
    * policy statements of an existing policy context are to be 
    * removed before its PolicyConfiguration object is returned. If the value
    * passed to this parameter is true, the policy statements of  
    * an existing policy context will be removed. If the value is false, 
    * they will not be removed.
    *
    * @return an Object that implements the PolicyConfiguration
    * Interface matched to the Policy provider and corresponding to the
    * identified policy context.
    *
    * @throws java.lang.SecurityException
    * when called by an AccessControlContext that has not been
    * granted the "setPolicy" SecurityPermission.
    *
    * @throws javax.security.jacc.PolicyContextException
    * if the implementation throws a checked exception that has not been
    * accounted for by the getPolicyConfiguration method signature.
    * The exception thrown
    * by the implementation class will be encapsulated (during construction)
    * in the thrown PolicyContextException.
    */
    public PolicyConfiguration getPolicyConfiguration(String contextId, boolean remove)
	throws PolicyContextException {

	PolicyConfigurationImpl.checkSetPolicyPermission();
	
	if(logger.isLoggable(Level.FINE)){
	    logger.fine("JACC Policy Provider: Getting PolicyConfiguration object with id = "+ contextId);	
	}

	PolicyConfigurationImpl pci = getPolicyConfigImpl(contextId);

	// if the pc is not in the table, see if it was copied into the
        // filesystem (e.g. by the DAS)
	if (pci == null){
	    pci = getPolicyConfigurationImplFromDirectory(contextId,true,remove);
	    if (pci == null) {
		pci = new PolicyConfigurationImpl(contextId);
		putPolicyConfigurationImpl(contextId,pci);
	    }
	} else {
	    // return the policy configuration to the open state, value of
	    // remove will determine if statements are removed
	    pci.initialize(true,remove,false);
	}
	return pci;
    }

   /**
    * This method determines if the identified policy context
    * exists with state "inService" in the Policy provider
    * associated with the factory.
    * <P>
    * @param contextID A string identifying a policy context
    *
    * @return true if the identified policy context exists within the
    * provider and its state is "inService", false otherwise.
    *
    * @throws java.lang.SecurityException
    * when called by an AccessControlContext that has not been
    * granted the "setPolicy" SecurityPermission.
    *
    * @throws javax.security.jacc.PolicyContextException
    * if the implementation throws a checked exception that has not been
    * accounted for by the inService method signature. The exception thrown
    * by the implementation class will be encapsulated (during construction)
    * in the thrown PolicyContextException.
    */
    public boolean inService(String contextID) throws PolicyContextException{
	PolicyConfigurationImpl.checkSetPolicyPermission();
	PolicyConfiguration pc = getPolicyConfigImpl(contextID);

	// if the pc is not in the table, see if it was copied into the
        // filesystem (e.g. by the DAS)
	if (pc == null) {
	    pc = getPolicyConfigurationImplFromDirectory(contextID,false,false);
	}
	return pc == null ? false : pc.inService();
    }

    // finds pc copied into the filesystem (by DAS) after the repository was 
    // initialized. Will only open pc if remove is true (otherwise pc will 
    // remain in service);

    private static PolicyConfigurationImpl 
	getPolicyConfigurationImplFromDirectory(String contextId, boolean open, boolean remove){
	PolicyConfigurationImpl pci = null;
	File f = new File(PolicyConfigurationImpl.getContextDirectoryName(contextId));
	if (f.exists()) {
	    pci = new PolicyConfigurationImpl(f,open,remove);
	    if (pci != null) {
		putPolicyConfigurationImpl(contextId,pci);
	    }
	}
	return pci;
    }

    // The following package protected methods are needed to support the 
    // PolicyCongigurationImpl class.

    protected static PolicyConfigurationImpl[] getPolicyConfigurationImpls() {

        PolicyConfigurationImpl[] rvalue = null;

        try {
            rLock.lock();

            Collection c = polConfTable.values();

            if (c != null) {

                rvalue = (PolicyConfigurationImpl[]) 
                    c.toArray( new PolicyConfigurationImpl[c.size()] );
            }

        } finally {
            rLock.unlock();
        }

        return rvalue;
    }

    protected static PolicyConfigurationImpl 
        putPolicyConfigurationImpl(String contextID, PolicyConfigurationImpl pci) {
        try {
            wLock.lock(); 
            return (PolicyConfigurationImpl) polConfTable.put(contextID,pci);
        } finally {
            wLock.unlock();
        }
    }

    private static PolicyConfigurationImpl 
        getPolicyConfigImpl(String contextId) {
        try {
            rLock.lock(); 
            return (PolicyConfigurationImpl) polConfTable.get(contextId);
        } finally {
            rLock.unlock();
        }
    }

    // does not reopen PC
    protected static PolicyConfigurationImpl getPolicyConfigurationImpl(String contextId) {
	PolicyConfigurationImpl pci = getPolicyConfigImpl(contextId); 
	if (pci == null) {
	    // check if pc was copied into the filesystem after the repository 
	    // was initialized (do not open pc or remove policy statements).
	    pci = getPolicyConfigurationImplFromDirectory(contextId,false,false);
	    if (pci == null) {
		logger.log(Level.WARNING,"pc.unknown_policy_context",
			   new Object[]{contextId});
	    }
	}
	return pci;
    }

}
