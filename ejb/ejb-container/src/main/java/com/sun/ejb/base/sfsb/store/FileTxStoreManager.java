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

package com.sun.ejb.base.sfsb.store;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import com.sun.ejb.spi.sfsb.store.SFSBTxStoreManager;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;
import com.sun.ejb.spi.sfsb.store.SFSBBeanState;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManagerException;


/**
 * A SFSBTxStoreManager that <b>CANNOT</b> save multiple SFSBBeanStates
 *  as a single transactional unit (Example file system).
 *
 * This implementation simply stores each BeanState separately by
 *  calling the appropriate StoreManager's checkpointSave method
 *
 * @author Mahesh Kannan
 */
public class FileTxStoreManager
    implements SFSBTxStoreManager
{

    private static final Level TRACE_LEVEL = Level.FINE;

    protected static final Logger _logger =
        LogDomains.getLogger(FileTxStoreManager.class, LogDomains.EJB_LOGGER);

    public FileTxStoreManager() {
    }

    public void checkpointSave(SFSBBeanState[] beanStates)
        throws SFSBStoreManagerException
    {
	int sz = beanStates.length;
	for (int i=0; i<sz; i++) {
	    SFSBStoreManager manager = beanStates[i].getSFSBStoreManager();
	    try {
		if (manager == null) {
		    _logger.log(Level.WARNING,
			"StoreManager is null. Cannot checkpoint");
		} else {
		    manager.checkpointSave(beanStates[i]);
		    if (_logger.isLoggable(TRACE_LEVEL)) {
			_logger.log(TRACE_LEVEL, "Successfully txCheckpointed "
			    + beanStates.length + " beans...");
		    }
		}
	    } catch (SFSBStoreManagerException smEx) {
		_logger.log(Level.WARNING,
			"StoreManagerException during checkpointSave", smEx);
	    } catch (Throwable th) {
		_logger.log(Level.WARNING,
			"Exception during checkpointSave", th);
	    }
	}
    }

}
