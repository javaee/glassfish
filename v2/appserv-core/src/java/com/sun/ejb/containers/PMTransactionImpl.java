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

package com.sun.ejb.containers;

import java.util.*;
import javax.transaction.*;
import javax.transaction.xa.*;
import com.sun.enterprise.*;


/**
 * A "wrapper" implementation of javax.transaction.Transaction
 * used by the container to communicate with the PersistenceManager.
 * The PM can only call getStatus, registerSynchronization, setRollbackOnly
 * methods. The Synchronization objects registered by the PM must be
 * called AFTER the Synchronization objects registered by the container,
 * to ensure that all ejbStores are called before the PM flushes state to 
 * the database.
 *
 */

public class PMTransactionImpl implements Transaction {

    private Transaction tx;

    PMTransactionImpl(Transaction tx)
    {
	this.tx = tx;
    }
    
    public int getStatus() throws SystemException {
	return tx.getStatus();
    }

   
    public void registerSynchronization(Synchronization pmsync) 
        throws RollbackException, IllegalStateException,
        SystemException {

	// to make sure ejbStores are called before pmsync.beforeCompletion
	Switch theSwitch = Switch.getSwitch();
	ContainerFactoryImpl cf = (ContainerFactoryImpl)theSwitch.getContainerFactory();
	cf.getContainerSync(tx).addPMSynchronization(pmsync);	
    }


    public void setRollbackOnly() 
        throws IllegalStateException, SystemException {

	tx.setRollbackOnly();
    }            

    public boolean equals(Object object) {
        if (object == this)
            return true;
        else if ( !(object instanceof PMTransactionImpl) )
            return false;
        else
            return tx.equals(((PMTransactionImpl)object).tx);
    }

    public int hashCode() {
        return tx.hashCode();
    }
 

    public void commit() throws RollbackException, 
	HeuristicMixedException, HeuristicRollbackException,
        SecurityException, SystemException
    {
        throw new IllegalStateException("Operation not allowed");
    }


    public void rollback() 
        throws IllegalStateException, SystemException {
        
        throw new IllegalStateException("Operation not allowed");
    }

    public boolean enlistResource(XAResource res)
        throws RollbackException, IllegalStateException,
            SystemException {

        throw new IllegalStateException("Operation not allowed");
    }

    public boolean delistResource(XAResource res, int flags)
        throws IllegalStateException, SystemException {

        throw new IllegalStateException("Operation not allowed");
    }


}
