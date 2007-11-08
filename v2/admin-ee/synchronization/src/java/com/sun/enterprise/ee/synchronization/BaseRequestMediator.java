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
import java.io.File;

import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.synchronization.store.SynchronizationMemento;
import com.sun.enterprise.ee.synchronization.tx.Transaction;
import com.sun.enterprise.ee.synchronization.store.StoreException;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Encapsulates the collective behavior of a synchronization 
 * request operation.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public abstract class BaseRequestMediator implements RequestMediator {

    protected final static Logger _logger = Logger.getLogger(EELogDomains.
                SYNCHRONIZATION_LOGGER);

    /**
     * Returns the synchronization memento for this request.
     *
     * @return   synchronization memento
     */
    public SynchronizationMemento getMemento() {
        return _memento;
    }

    /**
     * Returns a concrete implementation of Get command. This command 
     * is responsible to send the synchronization request to DAS 
     * and get the result set.
     */
    protected abstract Command getGetCommand();

    /**
     * Returns a concreate implementation of response processing command.
     * This command is responsible for processing the response such that
     * remote server instance can understand.
     */
    protected abstract Command getResponseCommand();

    protected void execute() throws Exception {

        // requests the info from DAS
        Command cGet = getGetCommand();
        cGet.execute();
        _response = (SynchronizationResponse) cGet.getResult();
        
        // saves the previous state before rendering the new response
        _memento = new SynchronizationMemento(_response);
        _memento.saveState();

        // renders the response into the local file cache
        Command cProcess = getResponseCommand();
        cProcess.execute();
    }

    /**
     * Performs a complete synchronization request operation.
     */
    public void run() {
        try {
            execute();

            // everything on this thread is okay, vote to commit
            _tx.voteCommit();

            _logger.log(Level.FINE,
                    "synchronization.vote_commit", _request.getMetaFileName());

        } catch (Exception e) {

            // error occurred; vote to rollback
            _tx.voteRollback();
            _logger.log(Level.INFO,
                 "synchronization.vote_rollback", _request.getMetaFileName());

            this._exception   = e;
            this._isException = true;

        } finally {
            commit();
        }
    }

    protected void commit() {

        // finalize the state changes if the transaction is commited
        if (_tx.isCommited()) {
            // commit
            try {
                if (_memento != null) {
                    _memento.commit();
                }

                _logger.log(Level.FINE,
                    "synchronization.tran_commit", _request.getMetaFileName());

            } catch (StoreException se) {
                _logger.log(Level.INFO, 
                    "synchronization.store_commit_error", se);
            }

            try {
                // creates/updates the time stamp files
                Command cTimestamp = new TimestampCommand(_request, _response);
                cTimestamp.execute();
            } catch (SynchronizationException e) {
                _logger.log(Level.INFO, 
                    "synchronization.timestamp_command_error", e);
            }

        } else  { // synchronization failed

            // rollback the changes
            try {
                if (_memento != null) {
                    _memento.rollback();
                }
            } catch (StoreException se) {
                _logger.log(Level.INFO, 
                    "synchronization.store_rollback_error", se);
            }

            // removes the TS file
            try {
                Command cTSRemove = 
                    new TimestampRemoveCommand(_request, _response);
                cTSRemove.execute();
            } catch (SynchronizationException e) {
                _logger.log(Level.INFO, 
                    "synchronization.timestamp_command_error", e);
            }
        }
    }

    /**
     * Returns the status code of this operation.
     *
     * @return  status code
     */
    public int getStatusCode() {
        int status = 1;

        // set status to zero if there are no errors
        if (!isException()) {
            status = 0;
        }
        return status;
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

    /**
     * Returns the synchronization request this is executing.
     * 
     * @return  synchronization request for this object
     */
    public SynchronizationRequest getRequest() {
        return _request;
    }

    /**
     * Returns the synchronization response for this mediator.
     *
     * @return  synchronization response
     */
    public SynchronizationResponse getResponse() {
        return _response;
    }

    /**
     * Returns the directory for this request mediator. If request represents
     * a file, it returns null.
     *
     * @return   directory for this request mediator or null
     */
    File getRequestDir() {
        File reqDir = null;
        try {
            if (_response != null) {
                SynchronizationRequest[] reqs = _response.getReply();
                if (reqs[0] != null) {
                    String file = reqs[0].getFileName();
                    File f = new File(file);
                    if (f.isDirectory()) {
                        reqDir = f;
                    }
                }
            }
        } catch (Exception e) {
          // ignore
        }

        return reqDir;
    }

    /**
     * Returns the inventory from central repository. 
     *
     * @return  list of files available in central repository
     */
    public List getCRInventory() {

        List crList = null;
        try {
            if (_response != null) {
                SynchronizationRequest[] reqs = _response.getReply();
                if (reqs[0] != null) {
                    crList = reqs[0].getInventory();
                }
            }
        } catch (Exception e) {
          // ignore
        }

        return crList;
    }

    // ---- VARIABLE(S) - PRIVATE -------------------------------------
    protected SynchronizationRequest _request    = null;
    protected Exception _exception               = null;
    protected DASPropertyReader _dasProperties   = null;
    protected boolean _isException               = false;
    protected Transaction _tx                    = null;
    protected SynchronizationResponse _response  = null;
    protected SynchronizationMemento _memento    = null;
}
