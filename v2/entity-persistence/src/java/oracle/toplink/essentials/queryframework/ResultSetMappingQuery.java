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

import oracle.toplink.essentials.internal.localization.ExceptionLocalization;
import oracle.toplink.essentials.exceptions.QueryException;
import oracle.toplink.essentials.exceptions.DatabaseException;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.sessions.DatabaseRecord;

/**
 * <p><b>Purpose</b>:
 * Concrete class to perform read using raw SQL and the SQLResultSetMapping.
 * <p>
 * <p><b>Responsibilities</b>:
 * Execute a selecting raw SQL string.
 * Returns a List of results.  Each item in the list will be another list
 * consisting of the expected populated return types in the order they were
 * specified in the SQLResultSetMapping
 *
 * @see SQLResultSetMapping
 * @author Gordon Yorke
 * @since TopLink Java Essentials
 */

public class ResultSetMappingQuery extends ObjectBuildingQuery {
    
    protected String resultSetMappingName;
    
    protected SQLResultSetMapping resultSetMapping;
    
    /**
     * PUBLIC:
     * Initialize the state of the query.
     */
    public ResultSetMappingQuery() {
        super();
   }

    /**
     * PUBLIC:
     * Initialize the query to use the specified call.
     */
    public ResultSetMappingQuery(Call call) {
        this();
        setCall(call);
    }

    /**
     * PUBLIC:
     * Initialize the query to use the specified call and SQLResultSetMapping
     */
    public ResultSetMappingQuery(Call call, String sqlResultSetMappingName) {
        this();
        setCall(call);
        this.resultSetMappingName = sqlResultSetMappingName;
    }

   /**
     * INTERNAL:
     * Clone the query.
     */
    public Object clone() {
        ResultSetMappingQuery cloneQuery = (ResultSetMappingQuery)super.clone();
        cloneQuery.resultSetMapping = this.resultSetMapping;
        cloneQuery.resultSetMappingName = this.resultSetMappingName;
        return cloneQuery;
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this ResultSetMapping to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        resultSetMapping.convertClassNamesToClasses(classLoader);
    };  

    /**
     * PUBLIC:
     * Used to define a store procedure or SQL query.
     */
/*    public void setCall(Call call) {
        if (call instanceof SQLCall){
            ((SQLCall)call).setSQLString(((SQLCall)call).getCallString().replace('?','#'));
        }
        super.setCall(call);
    }
*/
    /**
     * PUBLIC:
     * This will be the SQLResultSetMapping that is used by this query to process
     * the database results
     */
    public void setSQLResultSetMapping(SQLResultSetMapping resultSetMapping){
        this.resultSetMapping = resultSetMapping;
        this.resultSetMappingName = resultSetMapping.getName();
    }

    /**
     * PUBLIC:
     * This will be the SQLResultSetMapping that is used by this query to process
     * the database results
     */
    public void setSQLResultSetMappingName(String name){
        if (name == null && this.resultSetMapping == null){
            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("null_sqlresultsetmapping_in_query"));
        }
        this.resultSetMappingName = name;
        
    }
    
    /**
     * INTERNAL:
     * This method is used to build the results.  Interpreting the 
     * SQLResultSetMapping.
     */
    protected List buildObjectsFromRecords(List databaseRecords){
        List results = new ArrayList(databaseRecords.size() );
        SQLResultSetMapping mapping = this.getSQLResultSetMapping();
        for (Iterator iterator = databaseRecords.iterator(); iterator.hasNext();){
            if (mapping.getResults().size()>1){
                Object[] resultElement = new Object[mapping.getResults().size()];
                DatabaseRecord record = (DatabaseRecord)iterator.next();
                for (int i = 0;i<mapping.getResults().size();i++){
                    resultElement[i] = ((SQLResult)mapping.getResults().get(i)).getValueFromRecord(record, this);
                }
                results.add(resultElement);
            }else if (mapping.getResults().size()==1) {
                DatabaseRecord record = (DatabaseRecord)iterator.next();
                results.add( ((SQLResult)mapping.getResults().get(0)).getValueFromRecord(record, this));
            }else {
                return results;
            }
        }
        return results;
        
    }

    /**
     * INTERNAL:
     * Executes the prepared query on the datastore.
     */
    public Object executeDatabaseQuery() throws DatabaseException {
        if (getSession().isUnitOfWork()) {
            UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)getSession();

            // Note if a nested unit of work this will recursively start a
            // transaction early on the parent also.
            if (isLockQuery()) {
                if ((!unitOfWork.getCommitManager().isActive()) && (!unitOfWork.wasTransactionBegunPrematurely())) {
                    unitOfWork.beginTransaction();
                    unitOfWork.setWasTransactionBegunPrematurely(true);
                }
            }
            if (unitOfWork.isNestedUnitOfWork()) {
                // execute in parent UOW then register normally here.
                UnitOfWorkImpl nestedUnitOfWork = (UnitOfWorkImpl)getSession();
                setSession(nestedUnitOfWork.getParent());
                Object result = executeDatabaseQuery();
                setSession(nestedUnitOfWork);
                Object clone = registerIndividualResult(result, unitOfWork, false, null);

                if (shouldUseWrapperPolicy()) {
                    clone = getDescriptor().getObjectBuilder().wrapObject(clone, unitOfWork);
                }
                return clone;
            }
        }
        session.validateQuery(this);// this will update the query with any settings

        if (getQueryId() == 0) {
            setQueryId(getSession().getNextQueryId());
        }

        Vector rows = getQueryMechanism().executeSelect();
        setExecutionTime(System.currentTimeMillis());
        // If using 1-m joins, must set all rows.
        return buildObjectsFromRecords(rows);
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepare() {
        if ((!shouldMaintainCache()) && shouldRefreshIdentityMapResult()) {
            throw QueryException.refreshNotPossibleWithoutCache(this);
        }

        getQueryMechanism().prepare();

        getQueryMechanism().prepareExecuteSelect();
    }

    /**
     * PUBLIC:
     * This will be the SQLResultSetMapping that is used by this query to process
     * the database results
     */
    public SQLResultSetMapping getSQLResultSetMapping(){
        if (this.resultSetMapping == null && this.resultSetMappingName != null){
            this.resultSetMapping = this.getSession().getProject().getSQLResultSetMapping(this.resultSetMappingName);
        }
        return this.resultSetMapping;
    }

    /**
     * PUBLIC:
     * Return the result set mapping name.
     */
    public String getSQLResultSetMappingName() {
        return this.resultSetMappingName;
    }
}
