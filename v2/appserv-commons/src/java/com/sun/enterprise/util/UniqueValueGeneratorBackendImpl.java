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
package com.sun.enterprise.util;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import java.util.Hashtable;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742


/**
 * Doles out blocks of unique numbers for each context.  A single instance
 * of this object is registered per server instance(single-vm mode or
 * multi-vm mode).  
 *
 * @author Kenneth Saks
 */
public class UniqueValueGeneratorBackendImpl extends PortableRemoteObject implements UniqueValueGeneratorBackend {

    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
    // END OF IASRI 4660742

    // START OF IASRI 4679641
    // private static final boolean debug = false;
    private static final boolean debug = com.sun.enterprise.util.logging.Debug.enabled;
    // END OF IASRI 4679641

    private static final long BLOCK_SIZE = 100;
    private static final long NUM_BLOCKS_PER_CONTEXT = 
        (Long.MAX_VALUE / BLOCK_SIZE);

    private String id_;
    private Hashtable contexts_;

    public UniqueValueGeneratorBackendImpl() throws RemoteException {
        contexts_ = new Hashtable();
        id_       = System.currentTimeMillis() + "";
    }

    public String getGeneratorId() throws RemoteException {
        return id_;
    }

    public UniqueValueBlock getNextValueBlock(String context) 
        throws RemoteException {

        int blockIndex = 0;

        synchronized( this ) {
            if( contexts_.containsKey(context) ) {
                Integer currentBlock = (Integer) contexts_.get(context);
                blockIndex = currentBlock.intValue();
            } 
            contexts_.put(context, Integer.valueOf(blockIndex + 1));
        }

        if( blockIndex >= NUM_BLOCKS_PER_CONTEXT ) {
            throw new RemoteException("Block overflow");
        } 

        if( debug ) {
	          /** IASRI 4660742
            System.out.println("Returning block " + blockIndex + 
                               " of size " + BLOCK_SIZE + " for context " +
                               context);
	          **/
	          // START OF IASRI 4660742
	          if (_logger.isLoggable(Level.FINE)) {
	              _logger.log(Level.FINE,"Returning block " + blockIndex +
                       " of size " + BLOCK_SIZE + " for context " + context);

	           }
	          // END OF IASRI 4660742

        }
        return new UniqueValueBlock((blockIndex * BLOCK_SIZE), BLOCK_SIZE);
    }

}
