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
package oracle.toplink.essentials.internal.queryframework;

import java.util.ArrayList;
import java.util.List;
import oracle.toplink.essentials.expressions.Expression;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.internal.expressions.QueryKeyExpression;
import oracle.toplink.essentials.internal.expressions.ObjectExpression;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.mappings.ForeignReferenceMapping;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import java.util.Iterator;
import oracle.toplink.essentials.internal.descriptors.ObjectBuilder;
import oracle.toplink.essentials.internal.expressions.ForUpdateOfClause;
import oracle.toplink.essentials.exceptions.QueryException;
import oracle.toplink.essentials.queryframework.ObjectBuildingQuery;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;

/**
 * <p><b>Purpose</b>:
 * A common class to be used by ObjectLevelReadQueries and ReportItems.  This
 * Class will be used to store Joined Attribute Expressions.  It will also
 * store the indexes for object construction.
 *
 * @author Gordon Yorke
 * @since EJB3.0 RI
 */

public class JoinedAttributeManager implements Cloneable{

    /** Stores the joined attributes added through the query */
    protected ArrayList joinedAttributeExpressions_;
    
    /** Stores the joined attributes as specified in the descriptor */
    protected ArrayList joinedMappingExpressions_;
    
    /** PERF: Cache the local joined attribute names. */
    protected ArrayList joinedAttributes_;
    
    /** Used to determine if -m joining has been used. */
    protected boolean isToManyJoin = false;

    /** PERF: Used to avoid null checks for inner attribute joining. */
    protected boolean hasOuterJoinedAttribute = true;
    
    /** Used internally for joining. */
    protected transient HashMap joinedMappingIndexes_;

    /** Used internally for joining. */
    protected transient HashMap joinedMappingQueries_;

    /** Stored all row results to -m joining. */
    protected List dataResults;
    
    /** Stores the descriptor that these joins apply on */
    protected ClassDescriptor descriptor;
    
    /** Stores the base builder for resolving joined attributes by name */
    protected ExpressionBuilder baseExpressionBuilder;
    
    /** Stores the baseQuery */
    protected ObjectBuildingQuery baseQuery;
    
    /** Stores the result index of the parent, used for oneToMany joins */
    protected int parentResultIndex;
    
    public JoinedAttributeManager(){
    }
    
    public JoinedAttributeManager(ClassDescriptor descriptor, ExpressionBuilder baseBuilder, ObjectBuildingQuery baseQuery){
        this.descriptor = descriptor;
        this.baseQuery = baseQuery;
        this.baseExpressionBuilder = baseBuilder;
        this.parentResultIndex = 0;
    }
    
    /**
     * INTERNAL:
     */
    public void addJoinedAttribute(String attributeExpression) {
        this.getJoinedAttributes().add(attributeExpression);
    }

    /**
     * INTERNAL:
     */
    public void addJoinedAttributeExpression(Expression attributeExpression) {
        getJoinedAttributeExpressions().add(attributeExpression);
    }
    
    /**
    * INTERNAL:
    * Add an attribute represented by the given attribute name to the list of joins
    * for this query.
    * Note: Mapping level joins are represented separately from query level joins
    */
    public void addJoinedMappingExpression(Expression mappingExpression) {
        getJoinedMappingExpressions().add(mappingExpression);
    }

    /**
     * INTERNAL:
     * Add an attribute represented by the given attribute name to the list of joins
     * for this query.
     * Note: Mapping level joins are represented separately from query level joins
     */
    public void addJoinedMapping(String attributeName) {
        addJoinedMappingExpression(this.baseExpressionBuilder.get(attributeName));
    }


    /**
     * INTERNAL:
     * Clones the Joined Attribute Manager.  Generally called from Query.clone()
     */
    public Object clone(){
        JoinedAttributeManager joinManager = new JoinedAttributeManager();
        joinManager.baseExpressionBuilder = this.baseExpressionBuilder;
        joinManager.baseQuery = this.baseQuery;
        joinManager.descriptor = this.descriptor;
        if (this.joinedAttributeExpressions_ != null){
            joinManager.joinedAttributeExpressions_ = (ArrayList)this.joinedAttributeExpressions_.clone();
        }
        if (this.joinedMappingExpressions_ != null){
            joinManager.joinedMappingExpressions_ = (ArrayList)this.joinedMappingExpressions_.clone();
        }
        if (this.joinedAttributes_ != null){
            joinManager.joinedAttributes_ = (ArrayList)this.joinedAttributes_.clone();
        }
        if (this.joinedMappingIndexes_ != null){
            joinManager.joinedMappingIndexes_ = (HashMap)this.joinedMappingIndexes_.clone();
        }
        if (this.joinedMappingQueries_ != null){
            joinManager.joinedMappingQueries_ = (HashMap)this.joinedMappingQueries_.clone();
        }
        joinManager.isToManyJoin = this.isToManyJoin;
        joinManager.hasOuterJoinedAttribute = this.hasOuterJoinedAttribute;
        return joinManager;
        
    }

    /**
     * INTERNAL:
     * For joining the resulting rows include the field/values for many objects.
     * As some of the objects may have the same field names, these row partitions need to be calculated.
     * The indexes are stored in the query and used later when building the objects.
     */
    public int computeJoiningMappingIndexes(boolean includeAllSubclassFields, AbstractSession session, int offset) {
        if (!hasJoinedExpressions()) {
            return offset;
        }
        setJoinedMappingIndexes_(new HashMap(getJoinedAttributeExpressions().size() + getJoinedMappingExpressions().size()));
        int fieldIndex = 0;
        if (includeAllSubclassFields) {
            fieldIndex = getDescriptor().getAllFields().size();
        } else {
            fieldIndex = getDescriptor().getFields().size();
        }
        fieldIndex += offset;
        fieldIndex = computeIndexesForJoinedExpressions(getJoinedAttributeExpressions(), fieldIndex, session);
        fieldIndex = computeIndexesForJoinedExpressions(getJoinedMappingExpressions(), fieldIndex, session);
        return fieldIndex;
    }

    /**
     * INTERNAL:
     * This method is used when computing the nested queries for joined mappings.
     * It recurses computing the nested mapping queries and their join indexes.
     */
    protected void computeNestedQueriesForJoinedExpressions(List joinedExpressions, AbstractSession session, ObjectLevelReadQuery readQuery) {
        for (int index = 0; index < joinedExpressions.size(); index++) {
            ObjectExpression objectExpression = (ObjectExpression)joinedExpressions.get(index);
            
            // Expression may not have been initialized.
            objectExpression.getBuilder().setSession(session.getRootSession(null));
            if (objectExpression.getBuilder().getQueryClass() == null){
                objectExpression.getBuilder().setQueryClass(descriptor.getJavaClass());
            }
            
            // PERF: Cache join attribute names.
            ObjectExpression baseExpression = objectExpression;
            while (!baseExpression.getBaseExpression().isExpressionBuilder()) {
                baseExpression = (ObjectExpression)((QueryKeyExpression)baseExpression).getBaseExpression();
            }
            this.addJoinedAttribute(baseExpression.getName());
            
            // Ignore nested
            if ((objectExpression.getBaseExpression() == objectExpression.getBuilder()) && objectExpression.getMapping().isForeignReferenceMapping()) {
                ForeignReferenceMapping mapping = (ForeignReferenceMapping)objectExpression.getMapping();

                // A nested query must be built to pass to the descriptor that looks like the real query execution would.
                ObjectLevelReadQuery nestedQuery = mapping.prepareNestedJoins(this, session);

                // Register the nested query to be used by the mapping for all the objects.
                getJoinedMappingQueries_().put(mapping, nestedQuery);
            }
        }
    }

    /**
     * INTERNAL:
     * Used to optimize joining by pre-computing the nested join queries for the mappings.
     */
    public void computeJoiningMappingQueries(AbstractSession session) {
        if (hasJoinedExpressions()) {
            this.joinedAttributes_ = new ArrayList(getJoinedAttributeExpressions().size() + getJoinedMappingExpressions().size());
            setJoinedMappingQueries_(new HashMap(getJoinedAttributeExpressions().size() + getJoinedMappingExpressions().size()));
            computeNestedQueriesForJoinedExpressions(getJoinedAttributeExpressions(), session, (ObjectLevelReadQuery)this.baseQuery);
            computeNestedQueriesForJoinedExpressions(getJoinedMappingExpressions(), session, (ObjectLevelReadQuery)this.baseQuery);
        }
    }

    /**
     * INTERNAL:
     * This method is used when computing the indexes for joined mappings.
     * It iterates through a list of join expressions and adds an index that represents where the
     * fields represented by that expression will appear in the row returned by a read query.
     *
     * @see #computeJoiningMappingIndexes(boolean, AbstractSession)
     */
    protected int computeIndexesForJoinedExpressions(List joinedExpressions, int currentIndex, AbstractSession session) {
        for (int index = 0; index < joinedExpressions.size(); index++) {
            ObjectExpression objectExpression = (ObjectExpression)joinedExpressions.get(index);

            // Ignore nested
            if ((objectExpression.getBaseExpression() == objectExpression.getBuilder()) && objectExpression.getMapping() != null && objectExpression.getMapping().isForeignReferenceMapping()) {
                getJoinedMappingIndexes_().put(objectExpression.getMapping(), new Integer(currentIndex));
            }
            ClassDescriptor descriptor = objectExpression.getMapping().getReferenceDescriptor();
            int nFields;
            if(objectExpression.isQueryKeyExpression() && ((QueryKeyExpression)objectExpression).isUsingOuterJoinForMultitableInheritance()) {
                nFields = descriptor.getAllFields().size();
            } else {
                nFields = descriptor.getFields().size();
            }
            currentIndex = currentIndex + nFields;
        }
        return currentIndex;
    }

    /**
     * INTERNAL:
     * Returns the base expression builder for this query.
     */
    public ExpressionBuilder getBaseExpressionBuilder(){
        return this.baseExpressionBuilder;
    }
    
    /**
     * INTERNAL:
     * Returns the base query.
     */
    public ObjectBuildingQuery getBaseQuery(){
        return this.baseQuery;
    }
    
    /**
     * INTERNAL:
     * Return  all of the rows fetched by the query, used for 1-m joining.
     */
    public List getDataResults_() {
        return dataResults;
    }

    /**
     * INTERNAL:
     */
    public ClassDescriptor getDescriptor(){
        return this.descriptor;
    }
    
    /**
     * INTERNAL:
     * Return the attributes that must be joined.
     */
    public List getJoinedAttributes() {
        if (this.joinedAttributes_ == null){
            this.joinedAttributes_ = new ArrayList();
        }
        return this.joinedAttributes_;
    }
    
    /**
     * INTERNAL:
     * Return the attributes that must be joined.
     */
    public List getJoinedAttributeExpressions() {
        if (this.joinedAttributeExpressions_ == null){
            this.joinedAttributeExpressions_ = new ArrayList();
        }
        return joinedAttributeExpressions_;
    }
    
    /**
     * INTERNAL:
     * Get the list of expressions that represent elements that are joined because of their
     * mapping for this query.
     */
    public List getJoinedMappingExpressions() {
        if (this.joinedMappingExpressions_ == null){
            this.joinedMappingExpressions_ = new ArrayList();
        }
        return joinedMappingExpressions_;
    }

    /**
     * INTERNAL:
     * Return the attributes that must be joined.
     */
    public boolean hasJoinedAttributeExpressions() {
        return this.joinedAttributeExpressions_ != null && !this.joinedAttributeExpressions_.isEmpty();
    }

    /**
     * INTERNAL:
     * THis methos checks bot attribute expressions and mapping expressions and
     * determines if there are any joins to be made
     */
    public boolean hasJoinedExpressions() {
        return hasJoinedAttributeExpressions() || hasJoinedMappingExpressions();
    }
    
    /**
     * INTERNAL:
     * Return the attributes that must be joined.
     */
    public boolean hasJoinedMappingExpressions() {
        return this.joinedMappingExpressions_ != null && !this.joinedMappingExpressions_.isEmpty();
    }

    /**
     * INTERNAL:
     * Return if any attributes are joined.  This is a convience method that 
     * is only valid after prepare.
     */
    public boolean hasJoinedAttributes() {
        return this.joinedAttributes_ != null && !this.joinedAttributes_.isEmpty();
    }

    /**
     * INTERNAL:
     * PERF: Return if the query uses any outer attribute joins, used to avoid null checks in building objects.
     */
    public boolean hasOuterJoinedAttributeQuery() {
        return this.hasOuterJoinedAttribute;
    }

    /**
     * INTERNAL:
     * Return if the query uses any -m joins, and thus return duplicate/multiple rows.
     */
    public boolean isToManyJoin() {
        return this.isToManyJoin;
    }
    
    /**
     * INTERNAL:
     * Return if the attribute is specified for joining.
     */
    public boolean isAttributeJoined(ClassDescriptor mappingDescriptor, String attributeName) {
        // Since aggregates share the same query as their parent, must avoid the aggregate thinking
        // the parents mappings is for it, (queries only share if the aggregate was not joined).
        if (mappingDescriptor.isAggregateDescriptor() && (mappingDescriptor != getDescriptor())) {
            return false;
        }
        if (this.hasJoinedAttributes()) {
            return this.joinedAttributes_.contains(attributeName);
        }
        return isAttributeExpressionJoined(attributeName) || isAttributeMappingJoined(attributeName);
    }

    /**
     *  Iterate through a list of expressions searching for the given attribute name.
     *  Return true if it is found, false otherwise.
     */
    protected boolean isAttributeNameInJoinedExpressionList(String attributeName, List joinedExpressionList) {
        for (Iterator joinEnum = joinedExpressionList.iterator(); joinEnum.hasNext();) {
            QueryKeyExpression expression = (QueryKeyExpression)joinEnum.next();
            while (!expression.getBaseExpression().isExpressionBuilder()) {
                expression = (QueryKeyExpression)expression.getBaseExpression();
            }
            if (expression.getName().equals(attributeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * INTERNAL:
     * Return if the attribute is specified for joining.
     */
    protected boolean isAttributeExpressionJoined(String attributeName) {
        return isAttributeNameInJoinedExpressionList(attributeName, getJoinedAttributeExpressions());
    }

    /**
     * INTERNAL:
     * Return whether the given attribute is joined as a result of a join on a mapping
     */
    protected boolean isAttributeMappingJoined(String attributeName) {
        return isAttributeNameInJoinedExpressionList(attributeName, getJoinedMappingExpressions());
    }

    /**
     * INTERNAL:
     * Set the list of expressions that represent elements that are joined because of their
     * mapping for this query.
     */
    public void setJoinedAttributeExpressions_(List joinedExpressions) {
        this.joinedAttributeExpressions_ = new ArrayList(joinedExpressions);
    }

    /**
     * INTERNAL:
     * Set the list of expressions that represent elements that are joined because of their
     * mapping for this query.
     */
    public void setJoinedMappingExpressions_(List joinedMappingExpressions) {
        this.joinedMappingExpressions_ = new ArrayList(joinedMappingExpressions);
    }

    /**
     * INTERNAL:
     * Return the joined mapping indexes, used to compute mapping row partitions.
     */
    public Map getJoinedMappingIndexes_() {
        return joinedMappingIndexes_;
    }

    /**
     * INTERNAL:
     * Return the joined mapping queries, used optimize joining, only compute the nested queries once.
     */
    public Map getJoinedMappingQueries_() {
        return joinedMappingQueries_;
    }

    /**
     * INTERNAL:
     * Set the joined mapping queries, used optimize joining, only compute the nested queries once.
     */
    protected void setJoinedMappingQueries_(HashMap joinedMappingQueries) {
        this.joinedMappingQueries_ = joinedMappingQueries;
    }

    /**
     * INTERNAL:
     * Set the joined mapping indexes, used to compute mapping row partitions.
     */
    public void setJoinedMappingIndexes_(HashMap joinedMappingIndexes) {
        this.joinedMappingIndexes_ = joinedMappingIndexes;
    }

    /**
     * INTERNAL:
     * Set the attributes that must be joined.
     */
/*    public void setJoinedAttributeExpressions(List joinedAttributeExpressions) {
        this.joinedAttributeExpressions = joinedAttributeExpressions;
        setIsPrePrepared(false);
    }
*/
    /**
     * INTERNAL:
     * PERF: Set if the query uses any outer attribute joins, used to avoid null checks in building objects.
     */
    protected void setIsOuterJoinedAttributeQuery(boolean isOuterJoinedAttribute) {
        this.hasOuterJoinedAttribute = isOuterJoinedAttribute;
    }
    
    /**
     * INTERNAL:
     * Set if the query uses any -m joins, and thus return duplicate/multiple rows.
     */
    protected void setIsToManyJoinQuery(boolean isToManyJoin) {
        this.isToManyJoin = isToManyJoin;
    }
    

    /**
     * INTERNAL:
     * Validate and prepare join expressions.
     */
    public void prepareJoinExpressions(AbstractSession session) {
        // The prepareJoinExpression check for outer-joins to set this to true.
        setIsOuterJoinedAttributeQuery(false);
        for (int index = 0; index < getJoinedAttributeExpressions().size(); index++) {
            Expression expression = (Expression)getJoinedAttributeExpressions().get(index);
            if(expression.isObjectExpression()) {
                ((ObjectExpression)expression).setShouldUseOuterJoinForMultitableInheritance(true);
            }
            prepareJoinExpression(expression, session);
        }
        for (int index = 0; index < getJoinedMappingExpressions().size(); index++) {
            Expression expression = (Expression)getJoinedMappingExpressions().get(index);
            if(expression.isObjectExpression()) {
                ((ObjectExpression)expression).setShouldUseOuterJoinForMultitableInheritance(true);
            }
            prepareJoinExpression(expression, session);
        }
        computeJoiningMappingQueries(session);
    }

    /**
     * Validate and prepare the join expression.
     */
    protected void prepareJoinExpression(Expression expression, AbstractSession session) {
        // Must be query key expression.
        if (!expression.isQueryKeyExpression()) {
            throw QueryException.mappingForExpressionDoesNotSupportJoining(expression);
        }
        QueryKeyExpression objectExpression = (QueryKeyExpression)expression;

        // Expression may not have been initialized.
        objectExpression.getBuilder().setSession(session.getRootSession(null));
        if (objectExpression.getBuilder().getQueryClass() == null){
            objectExpression.getBuilder().setQueryClass(descriptor.getJavaClass());
        }
        // Can only join relationships.
        if ((objectExpression.getMapping() == null) || (!objectExpression.getMapping().isJoiningSupported())) {
            throw QueryException.mappingForExpressionDoesNotSupportJoining(objectExpression);
        }

        // Search if any of the expression traverse a 1-m.
        ObjectExpression baseExpression = objectExpression;
        while (!baseExpression.isExpressionBuilder()) {
            if (((QueryKeyExpression)baseExpression).shouldQueryToManyRelationship()) {
                setIsToManyJoinQuery(true);
            }
            if (((QueryKeyExpression)baseExpression).shouldUseOuterJoin()) {
                setIsOuterJoinedAttributeQuery(true);
            }
            baseExpression = (ObjectExpression)((QueryKeyExpression)baseExpression).getBaseExpression();
        }
    }

    /**
     * INTERNAL:
     * This method collects the Joined Mappings from the descriptor and initializes
     * them
     */
    public void processJoinedMappings(){
        ObjectBuilder objectBuilder = getDescriptor().getObjectBuilder();
        if (objectBuilder.hasJoinedAttributes()) {
            Vector mappingJoinedAttributes = objectBuilder.getJoinedAttributes();
            if (!hasJoinedExpressions()) {
                for (int i = 0; i < mappingJoinedAttributes.size(); i++) {
                    addJoinedMapping((String)mappingJoinedAttributes.get(i));
                }
            } else {
                for (int i = 0; i < mappingJoinedAttributes.size(); i++) {
                    String attribute = (String)mappingJoinedAttributes.get(i);
                    if (!isAttributeExpressionJoined(attribute)) {
                        addJoinedMapping(attribute);
                    }
                }
            }
        }
    }
    
    /**
     * INTERNAL:
     * Reset the JoinedAttributeManager.  This will be called when the Query is re-prepared
     */
    public void reset(){
        this.joinedMappingExpressions_ = null;
        this.joinedAttributes_ = null;
        this.isToManyJoin = false;
        this.hasOuterJoinedAttribute = true;
        this.joinedMappingIndexes_ = null;
        this.joinedMappingQueries_ = null;
        this.dataResults = null;
    }
    
    /**
     * INTERNAL:
     * This method is called from within this package it is used when 
     * initializing a report Item
     */
    public void setBaseQuery(ObjectLevelReadQuery query){
        this.baseQuery = query;
    }
    
    /**
     * INTERNAL:
     * This method is called from within this package, it is used when
     * initializing a ReportItem
     */
    protected void setBaseExpressionBuilder(ExpressionBuilder builder){
        this.baseExpressionBuilder = builder;
    }
    
    /**
     * INTERNAL:
     * Set all of the rows fetched by the query, used for 1-m joining.
     */
    public void setDataResults(List dataResults, AbstractSession session) {
        this.dataResults = dataResults;
        if(getJoinedMappingQueries_() != null &&  !getJoinedMappingQueries_().isEmpty() && dataResults != null && !dataResults.isEmpty()) {
            Iterator  it =  getJoinedMappingQueries_().entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                ObjectLevelReadQuery nestedQuery = (ObjectLevelReadQuery)entry.getValue();
                if(nestedQuery.getJoinedAttributeManager().isToManyJoin()) {
                    ForeignReferenceMapping frMapping = (ForeignReferenceMapping)entry.getKey();
                    Object indexObject = getJoinedMappingIndexes_().get(entry.getKey());
                    List nestedDataResults = new ArrayList(dataResults.size());
                    for(int i=0; i < dataResults.size(); i++) {
                        AbstractRecord row = (AbstractRecord)dataResults.get(i);                        
                        nestedDataResults.add(frMapping.trimRowForJoin(row, indexObject, session));
                    }
                    nestedQuery.getJoinedAttributeManager().setDataResults(nestedDataResults, session);
                }
            }
         }
    }

    /**
     * INTERNAL:
     * Called to set the descriptor on a Join Managerwith in a ReportItem, durring
     *  initialization, and durring DatabaseQuery.checkDescriptor
     */
    public void setDescriptor (ClassDescriptor descriptor){
        this.descriptor = descriptor;
    }
    
    /**
     * INTERNAL:
     * Used for joining in conjunction with pessimistic locking
     * Iterate through a list of joined expressions and ensure expression is set on the locking
     * clause for each expression that represents a pessimisically locked descriptor.
     */
    public ForUpdateOfClause setupLockingClauseForJoinedExpressions(ForUpdateOfClause lockingClause, AbstractSession session) {
        if (hasJoinedAttributeExpressions()){
            setupLockingClauseForJoinedExpressions(getJoinedAttributeExpressions(), session);
        }
        if (hasJoinedMappingExpressions()){
            setupLockingClauseForJoinedExpressions(getJoinedMappingExpressions(), session);
        }
        return lockingClause;
    }

    /**
     * INTERNAL:
     * Used for joining in conjunction with pessimistic locking
     * Iterate through a list of joined expressions and ensure expression is set on the locking
     * clause for each expression that represents a pessimisically locked descriptor.
     */
    private void setupLockingClauseForJoinedExpressions(List joinedExpressions, AbstractSession session) {
        // Must iterate over all of the joined attributes, just check
        // if any of them have pessimistic locking defined on the descriptor.
        for (Iterator e = joinedExpressions.iterator(); e.hasNext();) {
            Expression expression = (Expression)e.next();

            // Expression has not yet been validated.
            if (expression.isObjectExpression()) {
                ObjectExpression joinedAttribute = (ObjectExpression)expression;

                // Expression may not have been initialized.
                joinedAttribute.getBuilder().setSession(session.getRootSession(null));
                if (joinedAttribute.getBuilder().getQueryClass() == null){
                    joinedAttribute.getBuilder().setQueryClass(descriptor.getJavaClass());
                }
                
                ClassDescriptor nestedDescriptor = null;// joinedAttribute.getDescriptor();

                // expression may not be valid, no descriptor, validation occurs later.
                if (nestedDescriptor == null) {
                    return;
                }
            }
        }
        return;
    }

    public void setParentResultIndex(int parentsResultIndex) {
        this.parentResultIndex = parentsResultIndex;
    }

    public int getParentResultIndex() {
        return parentResultIndex;
    }
}
