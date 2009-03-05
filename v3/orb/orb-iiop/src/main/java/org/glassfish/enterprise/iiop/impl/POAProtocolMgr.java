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
package org.glassfish.enterprise.iiop.impl;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.File;

import javax.rmi.CORBA.*;

import org.glassfish.enterprise.iiop.api.ProtocolManager;
import org.glassfish.enterprise.iiop.api.RemoteReferenceFactory;

import org.glassfish.enterprise.iiop.spi.EjbContainerFacade;
import org.glassfish.enterprise.iiop.spi.EjbService;

import com.sun.enterprise.deployment.EjbDescriptor;

import com.sun.enterprise.util.Utility;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.ejb.TransactionRequiredLocalException;
import javax.ejb.ConcurrentAccessException;


import org.omg.CORBA.*;


import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager ;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;


import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService;
import com.sun.corba.ee.spi.orbutil.ORBConstants;

import org.jvnet.hk2.annotations.Service;

import java.util.logging.*;
import com.sun.logging.*;


/**
 * This class implements the ProtocolManager interface for the
 * RMI/IIOP ORB with POA (Portable Object Adapter). 
 * Note that the POA is now accessed only through the 
 * ReferenceFactoryManager for EJB.
 * 
 * @author Vivek Nagar
 */

@Service
public final class POAProtocolMgr extends org.omg.CORBA.LocalObject 
			     implements ProtocolManager
{
    private static final Logger _logger =
        LogDomains.getLogger(POAProtocolMgr.class, LogDomains.CORBA_LOGGER);
    
    private static final int MAPEXCEPTION_CODE = 9998;
    private static final String InitialObjectsDb = "initial.db";

    private static final String ORG_OMG_STUB_PREFIX = "org.omg.stub.";

    private ORB orb;

    private EjbService ejbService;
    private PresentationManager presentationMgr;

    public POAProtocolMgr() {}

    public void initialize(ORB o, EjbService ejbService)
    {

        this.orb = (ORB)o;

        this.ejbService = ejbService;
        this.presentationMgr = 
            ((com.sun.corba.ee.spi.orb.ORB) orb).getPresentationManager();

    }


    // Called in all VMs, must be called only after InitialNaming is available
    public void initializePOAs()
        throws Exception
    {   
	    // NOTE:  The RootPOA manager used to activated here.
	    ReferenceFactoryManager rfm =
	        (ReferenceFactoryManager)orb.resolve_initial_references(
		    ORBConstants.REFERENCE_FACTORY_MANAGER ) ;
	    rfm.activate() ;

	    _logger.log(Level.FINE, "POAProtocolMgr.initializePOAs: RFM resolved and activated");
    }


    // Called only in J2EE Server VM
    public void initializeNaming(File dbDir, int orbInitialPort) throws Exception
    {
	    // NOTE: The TransientNameService reference is NOT HA.
        new TransientNameService((com.sun.corba.ee.spi.orb.ORB)orb);
        _logger.log(Level.FINE, "POAProtocolMgr.initializeNaming: complete");
    }


    /**     
     * Return a factory that can be used to create/destroy remote
     * references for a particular EJB type.
     */          
    public RemoteReferenceFactory getRemoteReferenceFactory
        (EjbContainerFacade container, boolean remoteHomeView, String id)
    {
	    RemoteReferenceFactory factory = new POARemoteReferenceFactory
            (container, this, orb, remoteHomeView, id);

	    return factory;
    }

    /**
     * Connect the RMI object to the protocol.
     */
    public void connectObject(Remote remoteObj) throws RemoteException
    {
         StubAdapter.connect(remoteObj,  orb);    
    }	

    /**
     * Return true if the two object references refer to the same
     * remote object.
     */
    public boolean isIdentical(Remote obj1, Remote obj2)
    {
	    org.omg.CORBA.Object corbaObj1 = (org.omg.CORBA.Object)obj1;
	    org.omg.CORBA.Object corbaObj2 = (org.omg.CORBA.Object)obj2;

	    return corbaObj1._is_equivalent(corbaObj2);
    }

    public void validateTargetObjectInterfaces(Remote targetObj) {
        
        if( targetObj != null ) {
            // All Remote interfaces implemented by targetObj will be
            // validated as a side-effect of calling setTarget().
            // A runtime exception will be propagated if validation fails.
            Tie tie = presentationMgr.getTie();
            tie.setTarget(targetObj);
        } else {
            throw new IllegalArgumentException
                ("null passed to validateTargetObjectInterfaces");
        }

    }


    /**
     * Map the EJB/RMI exception to a protocol-specific (e.g. CORBA) exception
     */
    public Throwable mapException(Throwable exception) {

        boolean mapped = true;
        Throwable mappedException = null;

        if ( exception instanceof java.rmi.NoSuchObjectException
            || exception instanceof NoSuchObjectLocalException )
        {
            mappedException = new OBJECT_NOT_EXIST(MAPEXCEPTION_CODE,
                CompletionStatus.COMPLETED_MAYBE);
        } else if ( exception instanceof java.rmi.AccessException
            || exception instanceof javax.ejb.AccessLocalException )
        {
            mappedException = new NO_PERMISSION(MAPEXCEPTION_CODE,
                CompletionStatus.COMPLETED_MAYBE);
        } else if ( exception instanceof java.rmi.MarshalException ) {
            mappedException = new MARSHAL(MAPEXCEPTION_CODE,
                CompletionStatus.COMPLETED_MAYBE);
        } else if ( exception instanceof javax.transaction.TransactionRolledbackException
            || exception instanceof TransactionRolledbackLocalException )
        {
            mappedException = new TRANSACTION_ROLLEDBACK(MAPEXCEPTION_CODE,
                CompletionStatus.COMPLETED_MAYBE);
        } else if ( exception instanceof javax.transaction.TransactionRequiredException
            || exception instanceof TransactionRequiredLocalException )
        {
            mappedException = new TRANSACTION_REQUIRED(MAPEXCEPTION_CODE,
                CompletionStatus.COMPLETED_MAYBE);
        } else if ( exception instanceof javax.transaction.InvalidTransactionException ) {
            mappedException = new INVALID_TRANSACTION(MAPEXCEPTION_CODE,
                CompletionStatus.COMPLETED_MAYBE);
        } else {
            mapped = false;
        }
        
        return (mapped) 
            ? mappedException.initCause(exception)
            : exception;
    }


    /**
     * Called from SecurityMechanismSelector for each objref creation
     */

    public EjbDescriptor getEjbDescriptor(byte[] ejbKey)
    {
	    EjbDescriptor result = null;

	    try {
	        if(_logger.isLoggable(Level.FINE)) {
		        _logger.log(Level.FINE, "POAProtocolMgr.getEjbDescriptor->: " + ejbKey);
	        }

	        if ( ejbKey.length < POARemoteReferenceFactory.EJBID_OFFSET + 8 ) {
		        if(_logger.isLoggable(Level.FINE)) {
		            _logger.log(Level.FINE, "POAProtocolMgr.getEjbDescriptor: "
				    + ejbKey
				    + ": " + ejbKey.length + " < "
				    + POARemoteReferenceFactory.EJBID_OFFSET + 8);
		        }
		        return null;
	        }

	        long ejbId = Utility.bytesToLong(ejbKey, POARemoteReferenceFactory.EJBID_OFFSET);

	        if(_logger.isLoggable(Level.FINE)) {
		        _logger.log(Level.FINE, "POAProtocolMgr.getEjbDescriptor: "
			                + ejbKey + ": ejbId: " + ejbId);
	        }


		    result = ejbService.ejbIdToDescriptor(ejbId);

	    } finally {
	        if(_logger.isLoggable(Level.FINE)) {
		        _logger.log(Level.FINE, "POAProtocolMgr.getEjbDescriptor<-: "
			    + ejbKey + ": " + result);
	        }
	    }

        return result;
   }


}
