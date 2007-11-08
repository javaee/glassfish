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
package com.sun.enterprise.ee.synchronization;

import java.util.List;
import java.util.ArrayList;

import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.synchronization.tx.Transaction;
import com.sun.enterprise.ee.synchronization.cleaner.Cleaner;
import com.sun.enterprise.ee.synchronization.cleaner.GroupRequestCleaner;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;

/**
 * Executes a group of synchronization requests in one thread.
 *
 * @author Nazrul Islam
 */
public abstract class BaseGroupRequestMediator implements Runnable {
    
    /**
     * Executes all the requests in a sequence.
     */
    public void run() {

        try {
            for (int i=0; i<_mediators.length; i++) {
                _mediators[i].execute();
            }

            _tx.voteCommit();
            _logger.log(Level.FINE, "synchronization.vote_commit");
        } catch (Exception e) {
            _tx.voteRollback();
            _logger.log(Level.FINE, "synchronization.vote_rollback");

            this._exception   = e;
            this._isException = true;

        } finally {
            for (int i=0; i<_mediators.length; i++) {
                _mediators[i].commit();
            }
            _logger.log(Level.FINE, "synchronization.tran_commit");

            // cleans the dirs if synchronization is successful
            if (_tx.isCommited()) {
                Cleaner ac = new GroupRequestCleaner(_mediators);
                ac.gc();
            }
        }
    }

    /**
     * Returns true if there is an exception.
     *
     * @return  true if an error occurred
     */
    public boolean isException() {
        return this._isException;
    }

    /**
     * Returns the exception that orrurred during execution or null.
     *
     * @return  the exception during the synchronization or null 
     */
    public Exception getException() { 
        return this._exception; 
    }

    // ---- VARIABLE(S) - PRIVATE -------------------------------------
    protected BaseRequestMediator[] _mediators  = null;
    protected Exception _exception              = null;
    protected boolean _isException              = false;
    protected Transaction _tx                   = null;
    protected final static Logger _logger             = 
                Logger.getLogger(EELogDomains.SYNCHRONIZATION_LOGGER);
}
