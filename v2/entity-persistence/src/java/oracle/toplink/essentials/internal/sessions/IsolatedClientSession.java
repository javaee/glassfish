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
package oracle.toplink.essentials.internal.sessions;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.threetier.*;
import oracle.toplink.essentials.internal.identitymaps.IdentityMapManager;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;

/**
 * Provides isolation support by allowing a client session
 * to have a local cache of the subset of the classes.
 * This can be used to avoid caching frequently changing data,
 * or for security or VPD purposes.
 */
public class IsolatedClientSession extends oracle.toplink.essentials.threetier.ClientSession {
    public IsolatedClientSession(ServerSession parent, ConnectionPolicy connectionPolicy) {
        super(parent, connectionPolicy);
    }

    /**
    * INTERNAL:
    * Set up the IdentityMapManager.  This method allows subclasses of Session to override
    * the default IdentityMapManager functionality.
    */
    public void initializeIdentityMapAccessor() {
        this.identityMapAccessor = new IsolatedClientSessionIdentityMapAccessor(this, new IdentityMapManager(this));
    }

    /**
    * INTERNAL:
    * Helper method to calculate whether to execute this query locally or send
    * it to the server session.
    */
    protected boolean shouldExecuteLocally(DatabaseQuery query) {
        if (isIsolatedQuery(query)) {
            return true;
        }
        return isInTransaction();
    }

    /**
    * INTERNAL: Answers if this query is an isolated query and must be
    * executed locally.
    */
    protected boolean isIsolatedQuery(DatabaseQuery query) {
        query.checkDescriptor(this);
        if (query.isDataModifyQuery() || ((query.getDescriptor() != null) && query.getDescriptor().isIsolated())) {
            // For CR#4334 if in transaction stay on client session.
            // That way client's write accessor will be used for all queries.
            // This is to preserve transaction isolation levels.
            // also if this is an isolated class and we are in an isolated session
            //load locally. 
            return true;
        }
        return false;

    }

    /**
    * INTERNAL:
    * Gets the next link in the chain of sessions followed by a query's check
    * early return, the chain of sessions with identity maps all the way up to
    * the root session.
    * <p>
    * Used for session broker which delegates to registered sessions, or UnitOfWork
    * which checks parent identity map also.
    * @param canReturnSelf true when method calls itself.  If the path
    * starting at <code>this</code> is acceptable.  Sometimes true if want to
    * move to the first valid session, i.e. executing on ClientSession when really
    * should be on ServerSession.
    * @param terminalOnly return the session we will execute the call on, not
    * the next step towards it.
    * @return this if there is no next link in the chain
    */
    public AbstractSession getParentIdentityMapSession(DatabaseQuery query, boolean canReturnSelf, boolean terminalOnly) {
        if ((query != null) && isIsolatedQuery(query)) {
            return this;
        } else {
            return getParent().getParentIdentityMapSession(query, canReturnSelf, terminalOnly);
        }
    }

    /**
    * INTERNAL:
    * Gets the session which this query will be executed on.
    * Generally will be called immediately before the call is translated,
    * which is immediately before session.executeCall.
    * <p>
    * Since the execution session also knows the correct datasource platform
    * to execute on, it is often used in the mappings where the platform is
    * needed for type conversion, or where calls are translated.
    * <p>
    * Is also the session with the accessor.  Will return a ClientSession if
    * it is in transaction and has a write connection.
    * @return a session with a live accessor
    * @param query may store session name or reference class for brokers case
    */
    public AbstractSession getExecutionSession(DatabaseQuery query) {
        if (shouldExecuteLocally(query)) {
            return this;
        } else {
            return getParent().getExecutionSession(query);
        }
    }

    /**
    * INTERNAL:
    * Isolated sessions must forward call execution to its parent, unless in a transaction.
    * This is required as isolated sessions are always the execution session for isolated classes.
    */
    public Object executeCall(Call call, AbstractRecord translationRow, DatabaseQuery query) throws DatabaseException {
        if (isInTransaction()) {
            return super.executeCall(call, translationRow, query);
        }
        return getParent().executeCall(call, translationRow, query);
    }
}
