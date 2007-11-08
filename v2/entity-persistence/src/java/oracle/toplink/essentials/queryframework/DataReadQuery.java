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

import java.util.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;

/**
 * <p><b>Purpose</b>:
 * Concrete class to perform read using raw SQL.
 * <p>
 * <p><b>Responsibilities</b>:
 * Execute a selecting raw SQL string.
 * This returns a Collection of the DatabaseRows representing the result set.
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public class DataReadQuery extends ReadQuery {
    protected ContainerPolicy containerPolicy;
    
    //** For EJB 3 support returns results without using the AbstractRecord */
    protected boolean useAbstractRecord = true;

    /**
     * PUBLIC:
     * Initialize the state of the query.
     */
    public DataReadQuery() {
        super();
        this.shouldMaintainCache = false;
        useAbstractRecord = true;
        setContainerPolicy(ContainerPolicy.buildPolicyFor(ClassConstants.Vector_class));
    }

    /**
     * PUBLIC:
     * Initialize the query to use the specified SQL string.
     */
    public DataReadQuery(String sqlString) {
        this();
        setSQLString(sqlString);
    }

    /**
     * PUBLIC:
     * Initialize the query to use the specified call.
     */
    public DataReadQuery(Call call) {
        this();
        setCall(call);
    }

    /**
     * INTERNAL:
     * Clone the query.
     */
    public Object clone() {
        DataReadQuery cloneQuery = (DataReadQuery)super.clone();
        cloneQuery.setContainerPolicy(getContainerPolicy().clone(cloneQuery));
        return cloneQuery;
    }

    /**
     * INTERNAL:
     * Execute the query.
     * Perform the work to execute the SQL string.
     * @exception DatabaseException an error has occurred on the database
     * @return a collection or cursor of DatabaseRows representing the result set
     */
    public Object executeDatabaseQuery() throws DatabaseException {
        if (getContainerPolicy().overridesRead()) {
            return getContainerPolicy().execute();
        }
        return executeNonCursor();
    }

    /**
     * INTERNAL:
     * The results are *not* in a cursor, build the collection.
     */
    protected Object executeNonCursor() throws DatabaseException {
        Vector rows = getQueryMechanism().executeSelect();
        if (useAbstractRecord ){
            Object results = getContainerPolicy().buildContainerFromVector(rows, getSession());
            return results;
        }
        ContainerPolicy containerPolicy = getContainerPolicy();
        Object reportResults = containerPolicy.containerInstance(rows.size());
        for (Iterator rowsEnum = rows.iterator(); rowsEnum.hasNext();) {
            containerPolicy.addInto( ((AbstractRecord)rowsEnum.next()).getValues()  , reportResults, getSession());
        }
        return reportResults;
    }

    /**
     * PUBLIC:
     * Return the query's ContainerPolicy.
     * @return oracle.toplink.essentials.internal.queryframework.ContainerPolicy
     */
    public ContainerPolicy getContainerPolicy() {
        return containerPolicy;
    }

    /**
     * PUBLIC:
     * Return if this is a data read query.
     */
    public boolean isDataReadQuery() {
        return true;
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepare() {
        super.prepare();
        getContainerPolicy().prepare(this, getSession());
        if (getContainerPolicy().overridesRead()) {
            return;
        }
        getQueryMechanism().prepareExecuteSelect();
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    public void prepareForExecution() throws QueryException {
        super.prepareForExecution();
        getContainerPolicy().prepareForExecution();
    }

    /**
     * PUBLIC:
     * Set the container policy.
     */
    public void setContainerPolicy(ContainerPolicy containerPolicy) {
        // Fix for BUG 3337003 - TopLink OX will try to set this to null if
        // it is not set in the deployment XML. So don't allow it to do that.
        if (containerPolicy == null) {
            return;
        }

        this.containerPolicy = containerPolicy;
    }

    /**
     * PUBLIC:
     * Configure the query to use an instance of the specified container class
     * to hold the target objects.
     * The container class must implement (directly or indirectly) the Collection interface.
     */
    public void useCollectionClass(Class concreteClass) {
        setContainerPolicy(ContainerPolicy.buildPolicyFor(concreteClass));
    }
    
    /**
     * INTERNAL:
     * Allow changing the default behaviour so that AbstractRecords are not returned as query results.  
     */
    public void setUseAbstractRecord(boolean useAbstractRecord){
        this.useAbstractRecord = useAbstractRecord;
    }
}
