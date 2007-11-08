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
package oracle.toplink.essentials.exceptions.i18n;

import java.util.ListResourceBundle;

/**
 * INTERNAL: English ResourceBundle for QueryException messages.
 *
 * @author: Xi Chen
 */
public class QueryExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "6001", "Cursored SQL queries must provide an additional query to retrieve the size of the result set." },
                                           { "6002", "Aggregated objects cannot be written/deleted independently from their owners. {1}Descriptor: [{0}]" },
                                           { "6003", "The number of arguments provided to the query for execution does not match the number of arguments in the query definition." },
                                           { "6004", "The object [{0}], of class [{1}], with identity hashcode (System.identityHashCode()) [{2}], {3}is not from this UnitOfWork object space, but the parent session''s.  The object was never registered in this UnitOfWork, {3}but read from the parent session and related to an object registered in the UnitOfWork.  Ensure that you are correctly" + "{3}registering your objects.  If you are still having problems, you can use the UnitOfWork.validateObjectSpace() method to {3}help debug where the error occurred.  For more information, see the manual or FAQ." },
                                           { "6005", "The object [{0}], of class [{1}], with identity hashcode (System.identityHashCode()) [{2}], {3}is the original to a registered new object.  The UnitOfWork clones registered new objects, so you must ensure that an object {3}is registered before it is referenced by another object.  If you do not want the new object to be cloned, use the" + "{3}UnitOfWork.registerNewObject(Object) API.  If you are still having problems, you can use the UnitOfWork.validateObjectSpace() {3}method to help debug where the error occurred.  For more information, see the manual or FAQ." },
                                           { "6006", "The mapping [{0}] does not support batch reading." },
                                           { "6007", "Missing descriptor for [{0}]." },
                                           { "6008", "Missing descriptor for [{0}] for query named [{1}]." },
                                           { "6013", "Incorrect size query given to CursoredStream." },
                                           { "6014", "Objects cannot be written during a UnitOfWork, they must be registered." },
                                           { "6015", "Invalid query key [{0}] in expression." },
                                           { "6016", "Objects or the database cannot be changed through a ServerSession.  All changes must be done through a ClientSession''s UnitOfWork." },
                                           { "6020", "No concrete class indicated for the type in the row [{0}]." },
                                           { "6021", "Cursors are not supported for interface descriptors, or abstract class multiple table descriptors using expressions.  Consider using custom SQL or multiple queries." },
                                           { "6023", "The list of fields to insert into the table [{0}] is empty.  You must define at least one mapping for this table." },
                                           { "6024", "Modify queries require an object to modify." },
                                           { "6026", "Query named [{0}] is not defined. Domain class: [{1}]" },
                                           { "6027", "Query sent to a unactivated UnitOfWork." },
                                           { "6028", "An attempt to read beyond the end of stream occurred." },
                                           { "6029", "A reference class must be provided." },
                                           { "6030", "Refreshing is not possible if caching is not enabled." },
                                           { "6031", "size() is only supported on expression queries, unless a size query is given." },
                                           { "6032", "The SQL statement has not been properly set." },
                                           { "6034", "Invalid query item expression [{0}]." },
                                           { "6041", "The selection object passed to a ReadObjectQuery was null." },
                                           { "6042", "A session name must be specified for non-object-level queries.  See the setSessionName(String) method." },
                                           { "6043", "ReportQueries without primary keys cannot use readObject(). {1}ReportQueryResult: [{0}]." },
                                           { "6044", "The primary key read from the row [{0}] during the execution of the query was detected to be null.  Primary keys must not contain null." },
                                           { "6045", "The subclass [{0}], indicated in the row while building the object, has no descriptor defined for it." },
                                           { "6046", "Cannot delete an object of a read-only class.  The class [{0}] is declared as read-only in this UnitOfWork." },
                                           { "6047", "Invalid operator [{0}] in expression." },
                                           { "6048", "Illegal use of getField() [{0}] in expression." },
                                           { "6049", "Illegal use of getTable() [{0}] in expression." },
                                           { "6050", "ReportQuery result size mismatch.  Expecting [{0}], but retrieved [{1}]" },
                                           { "6051", "Partial object queries are not allowed to maintain the cache or be edited.  You must use dontMaintainCache()." },
                                           { "6052", "An outer join (getAllowingNull or anyOfAllowingNone) is only valid for OneToOne, OneToMany, ManyToMany, AggregateCollection and DirectCollection Mappings, and cannot be used for the mapping [{0}]." },
                                           { "6054", "Cannot add the object [{0}], of class [{1}], to container class [{2}] using policy [{3}]." },
                                           { "6055", "The method invocation of the method [{0}] on the object [{1}], of class [{2}], triggered an exception." },
                                           { "6056", "Cannot create a clone of object [{0}], of class [{1}], using [{2}]." },
                                           { "6057", "The method [{0}] is not a valid method to call on object [{1}]." },
                                           { "6058", "The method [{0}] was not found in class [{1}]." },
                                           { "6059", "The class [{0}] cannot be used as the container for the results of a query because it cannot be instantiated." },
                                           { "6060", "Could not use object [{0}] of type [{1}] as a key into [{2}] of type [{3}].  The key cannot be compared with the keys currently in the Map." },
                                           { "6061", "Cannot reflectively access the method [{0}] for object [{1}], of class [{2}]." },
                                           { "6062", "The method [{0}], called reflectively on object [{1}], of class [{2}], triggered an exception." },
                                           { "6063", "Invalid operation [{0}] on cursor." },
                                           { "6064", "Cannot remove the object [{0}], of class [{1}], from container class [{2}] using policy [{3}]." },
                                           { "6065", "Cannot add the object [{0}], of class [{1}], to container [{2}]." },
                                           { "6066", "The object [{0}], of class [{1}], with identity hashcode (System.identityHashCode()) [{2}], {3}has been deleted, but still has references.  Deleted objects cannot be referenced after being deleted. {3}Ensure that you are correctly registering your objects.  If you are still having problems, you can use the UnitOfWork.validateObjectSpace() {3}method to help debug where the error occurred.  For more information, see the manual or FAQ." },
                                           { "6067", "Cannot reflectively access the field [{0}] for object [{1}], of class [{2}]." },
                                           { "6068", "Cannot compare table reference to [{0}] in expression." },
                                           { "6069", "The field [{0}] in this expression has an invalid table in this context." },
                                           { "6070", "Invalid use of a query key [{0}] representing a \"to-many\" relationship in an expression.  Use anyOf() rather than get()." },
                                           { "6071", "Invalid use of anyOf() for a query key [{0}] not representing a to-many relationship in an expression.  Use get() rather than anyOf()." },
                                           { "6072", "Querying across a VariableOneToOneMapping is not supported. {2}Descriptor: [{0}] {2}Mapping: [{1}]" },
                                           { "6073", "Malformed expression in query.  Attempting to print an object reference into an SQL statement for query key [{0}]." },
                                           { "6074", "This expression cannot determine if the object conforms in memory.  You must set the query to check the database." },
                                           { "6075", "Object comparisons can only use the equal() or notEqual() operators.  Other comparisons must be done through query keys or direct attribute level comparisons. {1}Expression: [{0}]" },
                                           { "6076", "Object comparisons can only be used with OneToOneMappings.  Other mapping comparisons must be done through query keys or direct attribute level comparisons. {2}Mapping: [{0}] {2}Expression: [{1}]" },
                                           { "6077", "Object comparisons cannot be used in parameter queries.  You must build the expression dynamically. {1}Expression: [{0}]" },
                                           { "6078", "The class of the argument for the object comparison is incorrect. {3}Expression: [{0}] {3}Mapping: [{1}] {3}Argument: [{2}]" },
                                           { "6079", "Object comparison cannot be used for target foreign key relationships.  Query on the source primary key instead. {3}Expression: [{0}] {3}Mapping: [{1}] {3}Argument: [{2}]" },
                                           { "6080", "Invalid database call [{0}].  The call must be an instance of DatabaseCall." },
                                           { "6081", "Invalid database accessor [{0}].  The accessor must be an instance of DatabaseAccessor." },
                                           { "6082", "The method [{0}] with argument types [{1}] cannot be invoked on Expression." },
                                           { "6083", "Queries using in() cannot be parameterized.  Disable either query preparation or binding." },
                                           { "6084", "The redirection query was not configured properly.  The class or method name was not set." },
                                           { "6085", "The redirection query''s method is not defined or defined with the wrong arguments.  It must be declared \"public static\" and have arguments (DatabaseQuery, Record, Session) or (Session, Vector). {2}Class: [{0}] {2}Method: [{1}]" },
                                           { "6086", "The redirection query''s method invocation triggered an exception." },
                                           { "6087", "The example object class [{0}] does not match the reference object class [{1}]." },
                                           { "6088", "There are no attributes for the ReportQuery." },
                                           { "6089", "The expression has not been initialized correctly.  Only a single ExpressionBuilder should be used for a query. {1}For parallel expressions, the query class must be provided to the ExpressionBuilder constructor, and the query''s ExpressionBuilder must {1}always be on the left side of the expression. {1}Expression: [{0}]" },
                                           { "6090", "Cannot set ReportQuery to \"check cache only\"." },
                                           { "6091", "The type of the constant [{0}], used for comparison in the expression, does not match the type of the attribute [{1}]." },
                                           { "6092", "Uninstantiated ValueHolder detected. You must instantiate the relevant Valueholders to perform this in-memory query." },
                                           { "6094", "The parameter name [{0}] in the query''s selection criteria does not match any parameter name defined in the query." },
                                           { "6095", "Public clone method is required." },
                                           { "6096", "Clone method is inaccessible." },
                                           { "6097", "clone method threw an exception: {0}." },
                                           { "6098", "Unexpected Invocation Exception: {0}." },
                                           { "6099", "Joining across inheritance class with multiple table subclasses not supported: {0}, {1}" },
                                           { "6100", "Multiple values detected for single-object read query." },
                                           { "6101", "Executing this query could violate the integrity of the global session cache which must contain only the latest versions of objects.  In order to execute a query that returns objects as of a past time, try one of the following: Use a HistoricalSession (acquireSessionAsOf), all objects read will be cached and automatically read as of the same time.  This will apply even to triggering object relationships.  Set shouldMaintainCache to false.  You may make any object expression as of a past time, " + "provided none of its fields are represented in the result set (i.e. used only in the where clause)." },
                                           { "6102", "At present historical queries only work with Oracle 9R2 or later databases, as it uses Oracle's Flashback feature." },
                                           { "6103", "You may not execute a WriteQuery from inside a read-only HistoricalSession.  To restore past objects, try the following: read the same object as it is now with a UnitOfWork and commit the UnitOfWork." },
                                           { "6104", "The object, {0}, does not exist in the cache." },
                                           { "6105", "Query has to be reinitialised with a cursor stream policy." },
                                           { "6106", "The object of type [{0}] with primary key [{1}] does not exist in the cache." },
                                           { "6107", "Missing update statements on UpdateAllQuery." },
                                           { "6108", "Update all query does not support inheritance with multiple tables" },
                                           { "6109", "The named fetch group ({0}) is not defined at the dscriptor level." },
                                           { "6110", "Read query cannot conform the unfetched attribute ({0}) of the partially fetched object in the unit of work identity map." },
                                           { "6111", "The fetch group attribute ({0}) is not defined or not mapped." },
                                           { "6112", "Fetch group cannot be set on report query." },
                                           { "6113", "Fetch group cannot be used along with partial attribute reading." },
                                           { "6114", "You must define a fetch group manager at descriptor ({0}) in order to set a fetch group on the query ({1})" },
                                           { "6115", "Queries on isolated classes, or queries set to use exclusive connections, must not be executed on a ServerSession or, in CMP, outside of a transaction." },
                                           { "6116", "No Call or Interaction was specified for the attempted operation." },
                                           { "6117", "Can not set a query, that uses a cursored result, to cache query results." },
                                           { "6118", "A query on an Isolated class must not cache query results on the query." },
                                           { "6119", "The join expression {0} is not valid, or for a mapping type that does not support joining." },
                                           { "6120", "The partial attribute {0} is not a valid attribute of the class {1}." },
                                           { "6121", "The query has not been defined correctly, the expression builder is missing.  For sub and parallel queries ensure the queries builder is always on the left." },
                                           { "6122", "The expression is not a valid expression. {0}" },
                                           { "6123", "The container class specified [{0}] cannot be used because the container needs to implement {1}." },
                                           { "6124", "Required query of {0}, found {1}" },
                                           { "6125", "ReadQuery.clearQueryResults() can no longer be called. The call to clearQueryResults now requires that the session be provided. clearQueryResults(session) should be called." },
                                           { "6126", "A query is being executed that uses both conforming and cached query results.  These two settings are incompatible." },
                                           { "6127", "A reflective call failed on the TopLink class {0}, your environment must be set up to allow Java reflection." },
                                           { "6128", "Batch Reading is not supported on Queries using custom Calls."},
                                           { "6129", "Refreshing is not possible if the query does not go to the database." },
                                           { "6130", "Custom SQL failed to provide descriminator column : {0}, as defined in SQLResultSetMapping : {1}."},
                                           { "6131", "DeleteAllQuery that defines objects to be deleted using setObjects method with non-null argument must also define the corresponding selection criteria. {1}Objects: [{2}]{1}Descriptor: [{0}]" },
                                           { "6132", "Query argument {0} not found in list of parameters provided during query execution."},
                                           { "6133", "First argument of addUpdate method defines a field to be assigned a new value - it can't be null."},
                                           { "6134", "Attribute name or expression passed as a first parameter to addUpdate method doesn''t define a field. {1}Attribute name or Expression: [{2}]{1}Descriptor: [{0}]" },
                                           { "6135", "Attribute name or expression passed as a first parameter to addUpdate method defines a field from a table that''s not mapped to query descriptor. {1}Attribute name or Expression: [{2}]{1}Wrong field: [{3}]{1}Descriptor: [{0}]" },
                                           { "6136", "Classes mapped with multi table inheritance can not be ReportQuery items. Item: {0}, Expression: {1}."},
                                           { "6137", "An Exception was thrown while executing a ReportQuery with a constructor expression: {0}" },
                                           { "6138", "Query requires temporary storage, but {0} doesn''t support temporary tables." },
                                           { "6139", "Problem finding mapping for {0} defined in field result named {1}" },
                                           { "6140", "You have attempted to assign join expressions to the Report Item {1} of type {0}.  Join expressions are only applicable on Items that return an Persistent Object."},
                                           { "6141", "Count distinct on an outer joined class [{0}] having a composite primary key is not supported. Descriptor [{1}] "}
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}
