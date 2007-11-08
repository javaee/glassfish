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
package oracle.toplink.essentials.queryframework;

import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>:
 * Abstract class for all read queries.
 *
 * <p><b>Responsibilities</b>:
 * <ul>
 * <li> Caches result of query if flag is set.
 * </ul>
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public abstract class ReadQuery extends DatabaseQuery {

    /** Used for retrieve limited rows through the query. */
    protected int maxRows;

    /** Used to start query results at a specific result */
    protected int firstResult;

    /* used on read queries to stamp the object to determine the last time it was refreshed to
     * reduce work and prevent inifinite recursion on Refreshes
     *CR #4365 - used to prevent infinit recursion on refresh object cascade all
     * CR #2698903 - fix for the previous fix. No longer using millis but ids now.
     */
    protected long queryId;

    /**
     * PUBLIC:
     * Initialize the state of the query
     */
    public ReadQuery() {
        this.maxRows = 0;
        this.firstResult = 0;
        this.queryId = 0;
    }

    /**
     * INTERNAL:
     * By default return the row.
     * Used by cursored stream.
     */
    public Object buildObject(AbstractRecord row) {
        return row;
    }

    /**
     * INTERNAL
     * Used to give the subclasses oportunity to copy aspects of the cloned query
     * to the original query.
     */
    protected void clonedQueryExecutionComplete(DatabaseQuery query, AbstractSession session) {
        // Nothing by default.
    }

    /**
     * PUBLIC:
     * Return the value that will be set for the firstResult in the returned result set
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * INTERNAL:
     * This method is used to get the time in millis that this query is being executed at.
     * it is set just prior to executing the SQL and will be used to determine which objects should be refreshed.
     * CR #4365
     * CR #2698903 ... instead of using millis we will now use id's instead. Method
     * renamed appropriately.
     */
    public long getQueryId() {
        return this.queryId;
    }

    /**
     * PUBLIC:
     * Return the limit for the maximum number of rows that any ResultSet can contain to the given number.
     */
    public int getMaxRows() {
        return this.maxRows;
    }

    /**
     * PUBLIC:
     * Return if this is a read query.
     */
    public boolean isReadQuery() {
        return true;
    }

    /**
     * PUBLIC:
     * Used to set the first result in any result set that is returned for this query.
     * This method should only be set once per query.  To change the firstReslt use another query.
     * This method will call the absolute method on the JDBC result set to move the initial row
     * used by TopLink.  Note: The set of results returned from the database will still include
     * the results before the first result.  TopLink will just not use them for object building.
     */
    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        setIsPrepared(false);
        shouldCloneCall=true;
    }

    /**
     * INTERNAL:
     * This method is used to set the current system time in millis that this query is being executed at.
     * it is set just prior to executing the SQL and will be used to determine which objects should be refreshed.
     * CR #4365
     * CR #2698903 ... instead of using millis we will now use id's instead. Method
     * renamed appropriately.
     */
    public void setQueryId(long id) {
        this.queryId = id;
    }

    /**
    * PUBLIC:
    * Used to set the limit for the maximum number of rows that any ResultSet can contain to the given number.
    * This method should only be set once per query.  To change the max rows use another query.
    * This method limits the number of candidate results returned to TopLink that can be used to build objects
    */
    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
        setIsPrepared(false);
        shouldCloneCall=true;
    }
}
