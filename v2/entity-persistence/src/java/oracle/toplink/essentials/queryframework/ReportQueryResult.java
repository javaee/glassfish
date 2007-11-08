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

import java.io.*;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedGetConstructorFor;
import oracle.toplink.essentials.internal.security.PrivilegedInvokeConstructor;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.sessions.Session;

/**
 * <b>Purpose</b>: A single row (type) result for a ReportQuery<p>
 *
 * <b>Description</b>: Represents a single row of attribute values (converted using mapping) for
 * a ReportQuery. The attributes can be from various objects.
 *
 * <b>Responsibilities</b>:<ul>
 * <li> Converted field values into object attribute values.
 * <li> Provide acces to values by index or item name
 * </ul>
 *
 * @author Doug Clarke
 * @since TOPLink/Java 2.0
 */
public class ReportQueryResult implements Serializable, Map {

    /** Item names to lookup result values */
    protected Vector names;

    /** Actual converted attribute values */
    protected Vector results;

    /** PK values if the retrievPKs flag was set on the ReportQuery. These can be used to get the actual object */
    protected Vector primaryKeyValues;
    
    /** If an objectLevel distinct is used then generate unique key for this result */
    // GF_ISSUE_395
    protected StringBuffer key;

    /**
     * INTERNAL:
     * Used to create test results
     */
    public ReportQueryResult(Vector results, Vector primaryKeyValues) {
        this.results = results;
        this.primaryKeyValues = primaryKeyValues;
    }

    public ReportQueryResult(ReportQuery query, AbstractRecord row, Vector toManyResults) {
        super();
        this.names = query.getNames();
        buildResult(query, row, toManyResults);
    }

    /**
     * INTERNAL:
     * Create an array of attribute values (converted from raw field values using the mapping).
     */
    protected void buildResult(ReportQuery query, AbstractRecord row, Vector toManyData) {
        //GF_ISSUE_395
        if (query.shouldDistinctBeUsed()){
            this.key = new StringBuffer();
        }
        //end GF_ISSUE
        int numberOfPrimaryKeyFields = 0;
        Vector results = new Vector(query.getItems().size());

        if (query.shouldRetrievePrimaryKeys()) {
            numberOfPrimaryKeyFields = query.getDescriptor().getPrimaryKeyFields().size();
            setPrimaryKeyValues(query.getDescriptor().getObjectBuilder().extractPrimaryKeyFromRow(row, query.getSession()));
            // For bug 3115576 this is only used for EXISTS subselects so no result is needed.
        } else if (query.shouldRetrieveFirstPrimaryKey()) {
            numberOfPrimaryKeyFields = 1;
        }

        // CR 4240
        // rowIndex is seperate as there may be place holders in the query that are not in the
        // result set. So we can not compare the index to row size as there may be less
        // objects in the row then there will be in the result set.
        
        for (int index = 0; index < query.getItems().size(); index++) {
            ReportItem item = (ReportItem)query.getItems().elementAt(index);
            if (item.isContructorItem()){
                ConstructorReportItem citem = (ConstructorReportItem)item;
                Class[] constructorArgTypes = citem.getConstructorArgTypes();
                List constructorMappings = citem.getConstructorMappings();
                int numberOfItems = citem.getReportItems().size();
                Object[] constructorArgs = new Object[numberOfItems];
                if (constructorArgTypes==null){
                    constructorArgTypes = new Class[numberOfItems];
                }
                
                for (int i=0;i<numberOfItems;i++){
                    ReportItem ritem = (ReportItem)citem.getReportItems().get(i);
                    if (constructorArgTypes[i]==null){
                        if((constructorMappings != null)&&(constructorMappings.get(i)!=null)){
                            constructorArgTypes[i] = ((DatabaseMapping)constructorMappings.get(i)).getAttributeClassification();
                        }else if (ritem.getResultType() != null) {
                            constructorArgTypes[i] = ritem.getResultType();
                        }else if (ritem.getDescriptor() != null) {
                            constructorArgTypes[i] = ritem.getDescriptor().getJavaClass();
                        }
                    }
                    Object result = processItem(query, row, toManyData, (ReportItem)citem.getReportItems().get(i));
                    constructorArgs[i] = ConversionManager.getDefaultManager().convertObject(result, constructorArgTypes[i]);
                    //no type was specified, so use the object class itself.
                    if (constructorArgTypes[i]==null){
                        constructorArgTypes[i] = constructorArgs[i].getClass();
                    }
                }
                try{
                    java.lang.reflect.Constructor constructor = null;
                    Object returnValue = null;
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try {
                            constructor = (Constructor)AccessController.doPrivileged(new PrivilegedGetConstructorFor(citem.getResultType(), constructorArgTypes, true));
                            returnValue = AccessController.doPrivileged(new PrivilegedInvokeConstructor(constructor, constructorArgs));
                        } catch (PrivilegedActionException exception) {
                            throw QueryException.exceptionWhileUsingConstructorExpression(exception.getException(), query);                       }
                    } else {
                        constructor = PrivilegedAccessHelper.getConstructorFor(citem.getResultType(), constructorArgTypes, true);
                        returnValue = PrivilegedAccessHelper.invokeConstructor(constructor, constructorArgs);
                    }
                    results.addElement(returnValue);
                } catch (NoSuchMethodException exc){
                    throw QueryException.exceptionWhileUsingConstructorExpression(exc, query);
                } catch (IllegalAccessException exc){
                    throw QueryException.exceptionWhileUsingConstructorExpression(exc, query);
                } catch (java.lang.reflect.InvocationTargetException exc){
                    throw QueryException.exceptionWhileUsingConstructorExpression(exc, query);
                } catch (InstantiationException exc){
                    throw QueryException.exceptionWhileUsingConstructorExpression(exc, query);
                }
                
            }else{
                Object value = processItem(query, row, toManyData, item);
                results.addElement(value);
            }
        }

        setResults(results);
    }
    
    /**
     * INTERNAL:
     * Return a value from an item and database row (converted from raw field values using the mapping).
     */
    protected Object processItem(ReportQuery query, AbstractRecord row, Vector toManyData, ReportItem item) {
        JoinedAttributeManager joinManager = item.getJoinedAttributeManager();
        if (joinManager.isToManyJoin()){
                    joinManager.setDataResults(toManyData, query.getSession());
        }
        DatabaseMapping mapping = item.getMapping();
        Object value = null;
        if (!item.isPlaceHolder()) {
            if (mapping != null){
                //if mapping is not null then it must be a direct mapping - see Reportitem.init
                value = row.getValues().get(item.getResultIndex());
                value = ((AbstractDirectMapping)mapping).getAttributeValue(value, query.getSession());
                // GF_ISSUE_395
                if (this.key != null){
                    this.key.append(value);
                    this.key.append("_");
                }
                // end GF_ISSUE
            }else if (item.getDescriptor() != null){
                //item is for an object result.
                if (item.getDescriptor().getAllFields().size() + item.getResultIndex() > row.size()) {
                    throw QueryException.reportQueryResultSizeMismatch(item.getDescriptor().getAllFields().size() + item.getResultIndex(), row.size());
                }
                Vector trimedFields = Helper.copyVector(row.getFields(), item.getResultIndex(), row.size());
                Vector trimedValues = Helper.copyVector(row.getValues(), item.getResultIndex(), row.size());
                AbstractRecord subRow = new DatabaseRecord(trimedFields, trimedValues);
                value = item.getDescriptor().getObjectBuilder().buildObject(query, subRow, joinManager);
                // GF_ISSUE_395
                if (this.key != null){
                    List list = item.getDescriptor().getObjectBuilder().extractPrimaryKeyFromRow(subRow, query.getSession());
                    if(list!=null){//GF bug3233, Distinct Processing fails with NPE when referenced target is null in database. 
	                    for (Iterator iterator = list.iterator(); iterator.hasNext();){
	                        this.key.append(iterator.next());
	                        this.key.append("-");
	                    }
                    }
                    this.key.append("_");
                }
                // end GF_ISSUE
            }else{
                value = row.getValues().get(item.getResultIndex());
                // GF_ISSUE_395
                if (this.key != null){
                    this.key.append(value);
                }
                // end GF_ISSUE
            }
        }
        return value;
    }
    

    /**
     * PUBLIC:
     * Clear the contents of the result.
     */
    public void clear() {
        this.names = new Vector();
        this.results = new Vector();
    }

    /**
     * PUBLIC:
     * Check if the value is contained in the result.
     */
    public boolean contains(Object value) {
        return containsValue(value);
    }

    /**
     * PUBLIC:
     * Check if the key is contained in the result.
     */
    public boolean containsKey(Object key) {
        return getNames().contains(key);
    }

    /**
     * PUBLIC:
     * Check if the value is contained in the result.
     */
    public boolean containsValue(Object value) {
        return getResults().contains(value);
    }

    /**
     * PUBLIC:
     * Return an enumeration of the result values.
     */
    public Enumeration elements() {
        return getResults().elements();
    }

    /**
     * PUBLIC:
     * Returns a set of the keys.
     */
    public Set entrySet() {
        // bug 2669127
        // implemented this method exactly the same way as DatabaseRow.entrySet()
        int size = this.size();
        Map tempMap = new HashMap(size);
        for (int i = 0; i < size; i++) {
            tempMap.put(this.getNames().elementAt(i), this.getResults().elementAt(i));
        }
        return tempMap.entrySet();
    }

    /**
     * PUBLIC:
     * Compare if the two results are equal.
     */
    public boolean equals(Object anObject) {
        if (anObject instanceof ReportQueryResult) {
            return equals((ReportQueryResult)anObject);
        }

        return false;
    }

    /**
     * INTERNAL:
     * Used in testing to compare if results are correct.
     */
    public boolean equals(ReportQueryResult result) {
        if (this == result) {
            return true;
        }
        if (!Helper.compareOrderedVectors(getResults(), result.getResults())) {
            return false;
        }

        // Compare PKs
        if (getPrimaryKeyValues() != null) {
            if (result.getPrimaryKeyValues() == null) {
                return false;
            }
            return Helper.compareOrderedVectors(getPrimaryKeyValues(), result.getPrimaryKeyValues());
        }

        return true;
    }

    /**
     * PUBLIC:
     * Return the value for given item name.
     */
    public Object get(Object name) {
        if (name instanceof String) {
            return get((String)name);
        }

        return null;
    }

    /**
     * PUBLIC:
     * Return the value for given item name.
     */
    public Object get(String name) {
        int index = getNames().indexOf(name);
        if (index == -1) {
            return null;
        }

        return getResults().elementAt(index);
    }

    /**
     * PUBLIC:
     * Return the indexed value from result.
     */
    public Object getByIndex(int index) {
        return getResults().elementAt(index);
    }

    /**
     * INTERNAL:
     * Return the unique key for this result
     */
    public String getResultKey(){
        if (this.key != null){
            return this.key.toString();
        }
        return null;
    }
     
    
    /**
     * PUBLIC:
     * Return the names of report items, provided to ReportQuery.
     */
    public Vector getNames() {
        return names;
    }

    /**
     * PUBLIC:
     * Return the PKs for the corresponding object or null if not requested.
     */
    public Vector getPrimaryKeyValues() {
        return primaryKeyValues;
    }

    /**
     * PUBLIC:
     * Return the results.
     */
    public Vector getResults() {
        return results;
    }

    /**
     * PUBLIC:
     * Return if the result is empty.
     */
    public boolean isEmpty() {
        return getNames().isEmpty();
    }

    /**
     * PUBLIC:
     * Return an enumeration of the result names.
     */
    public Enumeration keys() {
        return getNames().elements();
    }

    /**
     * PUBLIC:
     * Returns a set of the keys.
     */
    public Set keySet() {
        return new HashSet(getNames());
    }

    /**
     * ADVANCED:
     * Set the value for given item name.
     */
    public Object put(Object name, Object value) {
        int index = getNames().indexOf(name);
        if (index == -1) {
            getNames().addElement(name);
            getResults().addElement(value);
            return null;
        }

        Object oldValue = getResults().elementAt(index);
        getResults().setElementAt(value, index);
        return oldValue;
    }

    /**
     * PUBLIC:
     * Add all of the elements.
     */
    public void putAll(Map map) {
        Iterator entriesIterator = map.entrySet().iterator();
        while (entriesIterator.hasNext()) {
            Map.Entry entry = (Map.Entry)entriesIterator.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * PUBLIC:
     * If the PKs were retrieved with the attributes then this method can be used to read the real object from the database.
     */
    public Object readObject(Class javaClass, Session session) {
        if (getPrimaryKeyValues() == null) {
            throw QueryException.reportQueryResultWithoutPKs(this);
        }

        ReadObjectQuery query = new ReadObjectQuery(javaClass);
        query.setSelectionKey(getPrimaryKeyValues());

        return session.executeQuery(query);
    }

    /**
     * INTERNAL:
     * Remove the name key and value from the result.
     */
    public Object remove(Object name) {
        int index = getNames().indexOf(name);
        if (index >= 0) {
            getNames().removeElementAt(index);
            Object value = getResults().elementAt(index);
            getResults().removeElementAt(index);
            return value;
        }
        return null;
    }

    protected void setNames(Vector names) {
        this.names = names;
    }

    /**
     * INTERNAL:
     * Set the PK values for the result row's object.
     */
    protected void setPrimaryKeyValues(Vector primaryKeyValues) {
        this.primaryKeyValues = primaryKeyValues;
    }

    /**
     * INTERNAL:
     * Set the results.
     */
    public void setResults(Vector results) {
        this.results = results;
    }

    /**
     * PUBLIC:
     * Return the number of name/value pairs in the result.
     */
    public int size() {
        return getNames().size();
    }

    /**
     * INTERNAL:
     * Converts the ReportQueryResult to a simple array of values.
     */
    public Object[] toArray(){
       List list = getResults();
       return (list == null) ? null : list.toArray();
    }

    /**
     * INTERNAL:
     * Converts the ReportQueryResult to a simple list of values.
     */
    public List toList(){
        return this.getResults();
    }
    
    public String toString() {
        java.io.StringWriter writer = new java.io.StringWriter();
        writer.write("ReportQueryResult(");
        for (int index = 0; index < getResults().size(); index++) {
            writer.write(String.valueOf(getResults().elementAt(index)));
            if (index < (getResults().size() - 1)) {
                writer.write(", ");
            }
        }
        writer.write(")");
        return writer.toString();
    }

    /**
     * PUBLIC:
     * Returns an collection of the values.
     */
    public Collection values() {
        return getResults();
    }
}
