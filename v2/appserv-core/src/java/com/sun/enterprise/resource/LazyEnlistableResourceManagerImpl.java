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
package com.sun.enterprise.resource;

import javax.transaction.*;
import javax.resource.spi.ManagedConnection;
import java.util.logging.Level;
import java.util.List;
import com.sun.enterprise.distributedtx.J2EETransactionManagerImpl;
import com.sun.enterprise.J2EETransactionManager;
import com.sun.enterprise.Switch;
import javax.resource.ResourceException;
import com.sun.enterprise.PoolManager;
import com.sun.enterprise.ComponentInvocation;
import java.util.ListIterator;

/**
 * This class is used for lazy enlistment of a resource
 *
 * @author Aditya Gore
 */
public class LazyEnlistableResourceManagerImpl extends ResourceManagerImpl {
    

    protected void enlist( J2EETransactionManager tm, Transaction tran,
        ResourceHandle h ){
        //do nothing
    }

    /**
     * Overridden to suspend lazyenlistment.
     * @param handle
     * @throws PoolingException
     */
      public void registerResource(ResourceHandle handle)
            throws PoolingException {
            handle.setEnlistmentSuspended(true);
            super.registerResource(handle);
     }
    /**
     * This is called by the PoolManager (in turn by the LazyEnlistableConnectionManager)
     * when a lazy enlistment is sought.
     * @param mc ManagedConnection
     * @throws ResourceException 
     */
    public void lazyEnlist( ManagedConnection mc ) throws ResourceException {
        if ( _logger.isLoggable(Level.FINE) ) {
            _logger.fine("Entering lazyEnlist");
        }

        J2EETransactionManager tm = Switch.getSwitch().getTransactionManager();
                        
        Transaction tran = null;
        
        try {
            tran = tm.getTransaction();
            if ( tran == null ) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine(" Transaction null - not enlisting ");
                }

                return;
            }
        } catch( SystemException se ) {
            ResourceException re = new ResourceException( se.getMessage() );
            re.initCause( se );
            throw re;
        }

        List invList = Switch.getSwitch().getInvocationManager().getAllInvocations();

        ResourceHandle h = null;
        for ( int j = invList.size(); j > 0; j-- ) {
            ComponentInvocation inv = (ComponentInvocation) invList.get( j - 1 );
            Object comp = inv.getInstance();

            List l = ((J2EETransactionManagerImpl)tm).getResourceList( comp, inv );
            
            ListIterator it = l.listIterator();
            while( it.hasNext()) {
                ResourceHandle hand = (ResourceHandle) it.next();
                ManagedConnection toEnlist = (ManagedConnection) hand.getResource();
                if ( mc.equals( toEnlist ) ) {
                    h = hand;
                    break;
                }
            }
        }
        
        //NOTE: Notice that here we are always assuming that the connection we
        //are trying to enlist was acquired in this component only. This
        //might be inadequate in situations where component A acquires a connection
        //and passes it on to a method of component B, and the lazyEnlist is
        //triggered in B
        //At this point however, we will only support the straight and narrow 
        //case where a connection is acquired and then used in the same component.
        //The other case might or might not work
        if( h != null && h.getResourceState().isUnenlisted()) {
            try {
                //Enable the suspended lazyenlistment so as to enlist the resource.
                    h.setEnlistmentSuspended(false);
                    tm.enlistResource( tran, h );
                //Suspend it back 
                    h.setEnlistmentSuspended(true);
            } catch( Exception e ) {
                //In the rare cases where enlistResource throws exception, we
    	        //should return the resource to the pool
                    PoolManager mgr = Switch.getSwitch().getPoolManager();
    	            mgr.putbackDirectToPool( h, h.getResourceSpec().getConnectionPoolName());
    	        _logger.log(Level.WARNING, "poolmgr.err_enlisting_res_in_getconn");
    	        if (_logger.isLoggable(Level.FINE) ) {
    	            _logger.fine("rm.enlistResource threw Exception. Returning resource to pool");
    	        }
    	        //and rethrow the exception
    	        throw new ResourceException( e );
            }
        }
    }

} 
