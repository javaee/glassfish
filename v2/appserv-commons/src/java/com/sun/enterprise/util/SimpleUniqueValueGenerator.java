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

import java.rmi.RemoteException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import com.sun.enterprise.log.Log;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742


/**
 *
 * @author Kenneth Saks
 */
class SimpleUniqueValueGenerator implements UniqueValueGenerator {

    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
    // END OF IASRI 4660742

    // Map context name to value block.
    private static Hashtable contextBlocks_;

    // Remote object that provides the unique number service.
    private static UniqueValueGeneratorBackend generatorBackend_;

    // Id of remote generator that is guaranteed to be unique
    // across invocations of a single j2ee server.
    private static String generatorBackendId_;

    // Namespace within which unique values will be generated.
    private String context_;

    static {
        generatorBackendId_ = null;
        generatorBackend_   = null;
        contextBlocks_      = new Hashtable();
    }

    /**
     * Use package-level access since clients should use a
     * UniqueValueGeneratorFactory to create concrete instances
     * this class.
     */
    SimpleUniqueValueGenerator(String context) {
        context_    = context;
    }

    private static synchronized 
        UniqueValueGeneratorBackend getBackendGenerator() 
        throws Exception {
        if( generatorBackend_ == null ) {
            InitialContext jndiContext = new InitialContext();
            generatorBackend_   = (UniqueValueGeneratorBackend) 
                jndiContext.lookup(UniqueValueGeneratorBackend.JNDI_NAME);
        }
        return generatorBackend_;
    }

    private static synchronized String getGeneratorBackendId() 
        throws UniqueValueGeneratorException {
        if( generatorBackendId_ == null ) {
            try {
                UniqueValueGeneratorBackend backend = getBackendGenerator();
                generatorBackendId_ = backend.getGeneratorId();
            } catch(Exception e) {
                /** IASRI 4660742
                Log.err.println(e); 
                **/
		            // START OF IASRI 4660742
                _logger.log(Level.SEVERE,"enterprise_util.excep_suidgen_getgenbackendid",e);
                // END OF IASRI 4660742

                throw new UniqueValueGeneratorException(e.getMessage());
            }
        }        
        return generatorBackendId_;
    }

    private static synchronized long nextNumberInternal(String context) 
        throws UniqueValueGeneratorException {

        UniqueValueBlock valueBlock = null;
        try {
            UniqueValueGeneratorBackend generatorBackend = 
                getBackendGenerator();

            valueBlock = (UniqueValueBlock) contextBlocks_.get(context);
            
            if( (valueBlock == null) || (!valueBlock.hasNext()) ) {
                valueBlock = generatorBackend.getNextValueBlock(context);
                contextBlocks_.put(context, valueBlock);
            }
        } catch(Exception e) {
            /** IASRI 4660742
            Log.err.println(e); 
            **/
	          // START OF IASRI 4660742
            _logger.log(Level.SEVERE,"enterprise_util.excep_suidgen_nextnuminternal",e);
            // END OF IASRI 4660742
            throw new UniqueValueGeneratorException(e.getMessage());
        }
        
        return valueBlock.next();
    }

    public long nextNumber() throws UniqueValueGeneratorException {
        return nextNumberInternal(getContext());
    }
    
    public String nextId() throws UniqueValueGeneratorException {
        return getGeneratorBackendId() + "_" + nextNumber();
    }

    public String getContext() {
        return context_;
    }
    
}
