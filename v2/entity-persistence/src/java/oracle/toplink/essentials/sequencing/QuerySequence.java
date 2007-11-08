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
package oracle.toplink.essentials.sequencing;

import java.util.Vector;
import java.math.BigDecimal;
import oracle.toplink.essentials.sessions.Record;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.databaseaccess.Accessor;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: An abstract class adding queries to StandardSequence
 * <p>
 */
public class QuerySequence extends StandardSequence {
    protected ValueReadQuery selectQuery;
    protected DataModifyQuery updateQuery;
    protected boolean shouldAcquireValueAfterInsert;
    protected boolean shouldUseTransaction;
    protected boolean shouldSkipUpdate;
    protected boolean shouldSelectBeforeUpdate;
    protected boolean wasSelectQueryCreated;
    protected boolean wasUpdateQueryCreated;

    public QuerySequence() {
        super();
    }

    public QuerySequence(String name) {
        super(name);
    }

    public QuerySequence(String name, int size) {
        super(name, size);
    }

    public QuerySequence(String name, int size, int initialValue) {
        super(name, size, initialValue);
    }
    
    public QuerySequence(boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super();
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }

    public QuerySequence(String name, boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super(name);
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }

    public QuerySequence(String name, int size, boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super(name, size);
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }
    
    public QuerySequence(String name, int size, int initialValue, 
            boolean shouldAcquireValueAfterInsert, boolean shouldUseTransaction) {
        super(name, size, initialValue);
        setShouldAcquireValueAfterInsert(shouldAcquireValueAfterInsert);
        setShouldUseTransaction(shouldUseTransaction);
    }    

    public boolean equals(Object obj) {
        if (obj instanceof QuerySequence && super.equals(obj)) {
            QuerySequence other = (QuerySequence)obj;
            return (getSelectQuery() == other.getSelectQuery()) && (getUpdateQuery() == other.getUpdateQuery()) && (shouldAcquireValueAfterInsert() == other.shouldAcquireValueAfterInsert()) && (shouldUseTransaction() == other.shouldUseTransaction()) && (shouldSkipUpdate() == other.shouldSkipUpdate()) && (shouldSelectBeforeUpdate() == other.shouldSelectBeforeUpdate());

        } else {
            return false;
        }
    }

    /**
    * PUBLIC:
    */
    public boolean shouldAcquireValueAfterInsert() {
        return shouldAcquireValueAfterInsert;
    }

    /**
    * PUBLIC:
    */
    public void setShouldAcquireValueAfterInsert(boolean shouldAcquireValueAfterInsert) {
        this.shouldAcquireValueAfterInsert = shouldAcquireValueAfterInsert;
    }

    /**
    * PUBLIC:
    */
    public boolean shouldUseTransaction() {
        return shouldUseTransaction;
    }

    /**
    * PUBLIC:
    */
    public void setShouldUseTransaction(boolean shouldUseTransaction) {
        this.shouldUseTransaction = shouldUseTransaction;
    }

    /**
    * PUBLIC:
    */
    public void setSelectQuery(ValueReadQuery query) {
        selectQuery = query;
    }

    /**
    * PUBLIC:
    */
    public ValueReadQuery getSelectQuery() {
        return selectQuery;
    }

    /**
    * PUBLIC:
    */
    public void setUpdateQuery(DataModifyQuery query) {
        updateQuery = query;
    }

    /**
    * PUBLIC:
    */
    public DataModifyQuery getUpdateQuery() {
        return updateQuery;
    }

    /**
    * PUBLIC:
    */
    public void setShouldSkipUpdate(boolean shouldSkipUpdate) {
        this.shouldSkipUpdate = shouldSkipUpdate;
    }

    /**
    * PUBLIC:
    */
    public boolean shouldSkipUpdate() {
        return shouldSkipUpdate;
    }

    /**
    * PUBLIC:
    */
    public void setShouldSelectBeforeUpdate(boolean shouldSelectBeforeUpdate) {
        this.shouldSelectBeforeUpdate = shouldSelectBeforeUpdate;
    }

    /**
    * PUBLIC:
    */
    public boolean shouldSelectBeforeUpdate() {
        return shouldSelectBeforeUpdate;
    }

    /**
    * INTERNAL:
    */
    protected ValueReadQuery buildSelectQuery() {
        return null;
    }

    /**
    * INTERNAL:
    */
    protected DataModifyQuery buildUpdateQuery() {
        return null;
    }

    /**
    * INTERNAL:
    */
    protected ValueReadQuery buildSelectQuery(String seqName, Integer size) {
        return null;
    }

    /**
    * INTERNAL:
    */
    protected DataModifyQuery buildUpdateQuery(String seqName, Number sizeOrNewValue) {
        return null;
    }

    /**
    * INTERNAL:
    */
    public void onConnect() {
        super.onConnect();
        if (getSelectQuery() == null) {
            setSelectQuery(buildSelectQuery());
            wasSelectQueryCreated = getSelectQuery() != null;
        }
        if ((getUpdateQuery() == null) && !shouldSkipUpdate()) {
            setUpdateQuery(buildUpdateQuery());
            wasUpdateQueryCreated = getUpdateQuery() != null;
        }
    }

    /**
    * INTERNAL:
    */
    public void onDisconnect() {
        if (wasSelectQueryCreated) {
            setSelectQuery(null);
            wasSelectQueryCreated = false;
        }
        if (wasUpdateQueryCreated) {
            setUpdateQuery(null);
            wasUpdateQueryCreated = false;
        }
        super.onDisconnect();
    }

    /**
    * INTERNAL:
    */
    protected Number updateAndSelectSequence(Accessor accessor, AbstractSession writeSession, String seqName, int size) {
        Integer sizeInteger = new Integer(size);
        if (shouldSkipUpdate()) {
            return (Number)select(accessor, writeSession, seqName, sizeInteger);
        } else {
            if (shouldSelectBeforeUpdate()) {
                Object result = select(accessor, writeSession, seqName, sizeInteger);
                BigDecimal currentValue;
                if (result instanceof Number) {
                    currentValue = new BigDecimal(((Number)result).longValue());
                } else if (result instanceof String) {
                    currentValue = new BigDecimal((String)result);
                } else if (result instanceof Record) {
                    Object val = ((Record)result).get("text()");
                    currentValue = new BigDecimal((String)val);
                } else {
                    // DatabaseException.errorPreallocatingSequenceNumbers() is thrown by the superclass
                    return null;
                }

                // Increment value
                BigDecimal newValue = currentValue.add(new BigDecimal(size));

                update(accessor, writeSession, seqName, newValue);
                return newValue;
            } else {
                update(accessor, writeSession, seqName, sizeInteger);
                return (Number)select(accessor, writeSession, seqName, sizeInteger);
            }
        }
    }

    /**
    * INTERNAL:
    */
    protected Object select(Accessor accessor, AbstractSession writeSession, String seqName, Integer size) {
        ValueReadQuery query = getSelectQuery();
        if (query != null) {
            if (accessor != null) {
                // PERF: Prepare the query before being cloned.
                // Also BUG: SQLCall could not be prepared concurrently by different queries.
                // Setting user define allow custom SQL query to be prepared without translation row.
                query.setIsUserDefined(true);
                query.checkPrepare(writeSession, null);
                query = (ValueReadQuery)query.clone();
                query.setAccessor(accessor);
            }
        } else {
            query = buildSelectQuery(seqName, size);
            if (accessor != null) {
                query.setAccessor(accessor);
            }
        }
        Vector args = createArguments(query, seqName, size);
        if (args != null) {
            return writeSession.executeQuery(query, args);
        } else {
            return writeSession.executeQuery(query);
        }
    }

    /**
    * INTERNAL:
    */
    protected void update(Accessor accessor, AbstractSession writeSession, String seqName, Number sizeOrNewValue) {
        DataModifyQuery query = getUpdateQuery();
        if (query != null) {
            if (accessor != null) {
                // PERF: Prepare the query before being cloned.
                // Also BUG: SQLCall could not be prepared concurrently by different queries.
                // Setting user define allow custom SQL query to be prepared without translation row.
                query.setIsUserDefined(true);
                query.checkPrepare(writeSession, null);
                query = (DataModifyQuery)query.clone();
                query.setAccessor(accessor);
            }
        } else {
            query = buildUpdateQuery(seqName, sizeOrNewValue);
            if (query == null) {
                return;
            }
            if (accessor != null) {
                query.setAccessor(accessor);
            }
        }
        Vector args = createArguments(query, seqName, sizeOrNewValue);
        if (args != null) {
            writeSession.executeQuery(query, args);
        } else {
            writeSession.executeQuery(query);
        }
    }

    /**
    * INTERNAL:
    */
    protected Vector createArguments(DatabaseQuery query, String seqName, Number sizeOrNewValue) {
        int nArgs = query.getArguments().size();
        if (nArgs > 0) {
            Vector args = new Vector(nArgs);
            args.addElement(seqName);
            if (nArgs > 1) {
                args.addElement(sizeOrNewValue);
            }
            return args;
        } else {
            return null;
        }
    }
}
