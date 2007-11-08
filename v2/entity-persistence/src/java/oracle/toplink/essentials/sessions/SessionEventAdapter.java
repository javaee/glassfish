/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.sessions;


/**
 * <p><b>Purpose</b>: To provide a trivial implemetation of SessionEventListener.
 * You may subclass this class rather than implement the entire SessonEventListener
 * interface in cases where only a small subset of the interface methods are needed.
 *
 * @see SessionEventManager#addListener(SessionEventListener)
 * @see SessionEventListener
 * @see SessionEvent
 */
public abstract class SessionEventAdapter implements SessionEventListener {

    /**
     * PUBLIC:
     * This event is raised on the session if a descriptor is missing for a class being persisted.
     * This can be used to lazy register the descriptor or set of descriptors.
     */
    public void missingDescriptor(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the session after read object query detected more than a single row back from the database.
     * The "result" of the event will be the call.  Some applications may want to interpret this as an error or warning condition.
     */
    public void moreRowsDetected(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the session after update or delete SQL has been sent to the database
     * but a row count of zero was returned.
     */
    public void noRowsModified(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the session after a stored procedure call has been executed that had output parameters.
     * If the proc was used to override an insert/update/delete operation then TopLink will not be expecting any return value.
     * This event mechanism allows for a listner to be registered before the proc is call to process the output values.
     * The event "result" will contain a DatabaseRow of the output values, and property "call" will be the StoredProcedureCall.
     */
    public void outputParametersDetected(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the client session after creation/acquiring.
     */
    public void postAcquireClientSession(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on when using the server/client sessions.
     * This event is raised after a connection is acquired from a connection pool.
     */
    public void postAcquireConnection(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised when a ClientSession, with Isolated data, acquires
     * an exclusive connection.
     */
    public void postAcquireExclusiveConnection(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the unit of work after creation/acquiring.
     * This will be raised on nest units of work.
     */
    public void postAcquireUnitOfWork(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised after a database transaction is started.
     * It is not raised for nested transactions.
     */
    public void postBeginTransaction(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised after the commit has begun on the UnitOfWork but before
     * the changes are calculated.
     */
    public void preCalculateUnitOfWorkChangeSet(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised after the commit has begun on the UnitOfWork and
     * after the changes are calculated.  The UnitOfWorkChangeSet, at this point,
     * will contain changeSets without the version fields updated and without
     * IdentityField type primary keys.  These will be updated after the insert, or
     * update, of the object
     */
    public void postCalculateUnitOfWorkChangeSet(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised after a database transaction is commited.
     * It is not raised for nested transactions.
     */
    public void postCommitTransaction(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the unit of work after commit.
     * This will be raised on nest units of work.
     */
    public void postCommitUnitOfWork(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This even will be raised after a UnitOfWorkChangeSet has been merged
     * When that changeSet has been received from a distributed session
     */
    public void postDistributedMergeUnitOfWorkChangeSet(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This even will be raised after a UnitOfWorkChangeSet has been merged
     */
    public void postMergeUnitOfWorkChangeSet(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised after the session connects to the database.
     * In a server session this event is raised on every new connection established.
     */
    public void postConnect(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised after the execution of every query against the session.
     * The event contains the query and query result.
     */
    public void postExecuteQuery(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the client session after releasing.
     */
    public void postReleaseClientSession(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the unit of work after release.
     * This will be raised on nest units of work.
     */
    public void postReleaseUnitOfWork(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the unit of work after resuming.
     * This occurs after pre/postCommit.
     */
    public void postResumeUnitOfWork(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised after a database transaction is rolledback.
     * It is not raised for nested transactions.
     */
    public void postRollbackTransaction(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised before a database transaction is started.
     * It is not raised for nested transactions.
     */
    public void preBeginTransaction(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised before a database transaction is commited.
     * It is not raised for nested transactions.
     */
    public void preCommitTransaction(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the unit of work before commit.
     * This will be raised on nest units of work.
     */
    public void preCommitUnitOfWork(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised before the execution of every query against the session.
     * The event contains the query to be executed.
     */
    public void preExecuteQuery(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the unit of work after the SQL has been flushed, but the commit transaction has not been executed.
     * It is similar to the JTS prepare phase.
     */
    public void prepareUnitOfWork(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the client session before releasing.
     */
    public void preReleaseClientSession(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on when using the server/client sessions.
     * This event is raised before a connection is released into a connection pool.
     */
    public void preReleaseConnection(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is fired just before a Client Session, with isolated data,
     * releases its Exclusive Connection
     */
    public void preReleaseExclusiveConnection(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised on the unit of work before release.
     * This will be raised on nest units of work.
     */
    public void preReleaseUnitOfWork(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This even will be raised before a UnitOfWorkChangeSet has been merged
     * When that changeSet has been received from a distributed session
     */
    public void preDistributedMergeUnitOfWorkChangeSet(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This even will be raised before a UnitOfWorkChangeSet has been merged
     */
    public void preMergeUnitOfWorkChangeSet(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This event is raised before a database transaction is rolledback.
     * It is not raised for nested transactions.
     */
    public void preRollbackTransaction(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This Event is raised before the session logs in.
     */
    public void preLogin(SessionEvent event) {
    }

    /**
     * PUBLIC:
     * This Event is raised after the session logs in.
     */
    public void postLogin(SessionEvent event) {
    }
}
