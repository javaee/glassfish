/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.persistence.support;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/** The <code>Query</code> interface allows applications to obtain persistent instances
 * from the data store.
 *
 * The {@link PersistenceManager} is the factory for <code>Query</code> instances.  There
 * may be many <code>Query</code> instances associated with a <code>PersistenceManager</code>.
 * Multiple queries might be executed simultaneously by different threads, but the
 * implementation might choose to execute them serially.  In either case, the
 * implementation must be thread safe.
 *
 * <P>There are three required elements in a <code>Query</code>: the class of the results,
 * the candidate collection of instances, and the filter.
 *
 * <P>There are optional elements: parameter declarations, variable
 * declarations, import statements, and an ordering specification.
 * <P>The query namespace is modeled after methods in Java:
 * <ul>
 * <li><code>setClass</code> corresponds to the class definition
 * <li><code>declareParameters</code> corresponds to formal parameters of a method
 * <li><code>declareVariables</code> corresponds to local variables of a method
 * <li><code>setFilter</code> and <code>setOrdering</code> correspond to the method body
 * </ul>
 * <P>There are two namespaces in queries. Type names have their own
 * namespace that is separate from the namespace for fields, variables
 * and parameters.
 * <P>The method <code>setClass</code> introduces the name of the candidate class in
 * the type namespace. The method <code>declareImports</code> introduces the names of
 * the imported class or interface types in the type namespace. Imported
 * type names must be unique. When used (e.g. in a parameter declaration,
 * cast expression, etc.) a type name must be the name of the candidate
 * class, the name of a class or interface imported by method
 * <code>declareImports</code>, or denote a class or interface from the same
 * package as the candidate class.
 * <P>The method <code>setClass</code> introduces the names of the candidate class fields.
 * <P>The method <code>declareParameters</code> introduces the names of the
 * parameters. A name introduced by <code>declareParameters</code> hides the name
 * of a candidate class field if equal. Parameter names must be unique.
 * <P>The method <code>declareVariables</code> introduces the names of the variables.
 * A name introduced by <code>declareVariables</code> hides the name of a candidate
 * class field if equal. Variable names must be unique and must not
 * conflict with parameter names.
 * <P>A hidden field may be accessed using the 'this' qualifier:
 * <code>this.fieldName</code>.
 * <P>The <code>Query</code> interface provides methods which execute the query
 * based on the parameters given. They return a <code>Collection</code> which the
 * user can iterate to get results. For future extension, the signature
 * of the <code>execute</code> methods specifies that they return an <code>Object</code> which
 * must be cast to <code>Collection</code> by the user.
 * <P>Any parameters passed to the <code>execute</code> methods are used only for
 * this execution, and are not remembered for future execution.
 * @author Craig Russell
 * @version 1.0
 */

public interface Query extends Serializable 
{
   /** Set the class of the candidate instances of the query.
    * <P>The class specifies the class
    * of the candidates of the query.  Elements of the candidate collection
    * that are of the specified class are filtered before being
    * put into the result <code>Collection</code>.
    *
    * @param cls the <code>Class</code> of the candidate instances.
    */
void setClass(Class cls);
    
    /** Set the candidate <code>Extent</code> to query.
     * @param pcs the candidate <code>Extent</code>.
     */
    void setCandidates(Extent pcs);
    
    /** Set the candidate <code>Collection</code> to query.
     * @param pcs the candidate <code>Collection</code>.
     */
    void setCandidates(Collection pcs);
    
    /** Set the filter for the query.
     *
     * <P>The filter specification is a <code>String</code> containing a Boolean
     * expression that is to be evaluated for each of the instances
     * in the candidate collection. If the filter is not specified,
     * then it defaults to "true", which has the effect of filtering
     * the input <code>Collection</code> only for class type.
     * <P>An element of the candidate collection is returned in the result if:
     * <ul><li>it is assignment compatible to the candidate <code>Class</code> of the <code>Query</code>; and
     * <li>for all variables there exists a value for which the filter
     * expression evaluates to <code>true</code>.
     * </ul>
     * <P>The user may denote uniqueness in the filter expression by
     * explicitly declaring an expression (for example, <code>e1 != e2</code>).
     * <P>Rules for constructing valid expressions follow the Java
     * language, except for these differences:
     * <ul>
     * <li>Equality and ordering comparisons between primitives and instances
     * of wrapper classes are valid.
     * <li>Equality and ordering comparisons of <code>Date</code> fields and <code>Date</code>
     * parameters are valid.
     * <li>White space (non-printing characters space, tab, carriage
     * return, and line feed) is a separator and is otherwise ignored.
     * <li>The assignment operators <code>=</code>, <code>+=</code>, etc. and pre- and post-increment
     * and -decrement are not supported. Therefore, there are no side
     * effects from evaluation of any expressions.
     * <li>Methods, including object construction, are not supported, except
     * for <code>Collection.contains(Object o)</code>, <code>Collection.isEmpty()</code>,
     * <code>String.startsWith(String s)</code>, and <code>String.endsWith(String e)</code>.
     * Implementations might choose to support non-mutating method
     * calls as non-standard extensions.
     * <li>Navigation through a <code>null</code>-valued field, which would throw
     * <code>NullPointerException</code>, is treated as if the filter expression
     * returned <code>false</code> for the evaluation of the current set of variable
     * values. Other values for variables might still qualify the candidate
     * instance for inclusion in the result set.
     * <li>Navigation through multi-valued fields (<code>Collection</code> types) is
     * specified using a variable declaration and the
     * <code>Collection.contains(Object o)</code> method.
     * </ul>
     * <P>Identifiers in the expression are considered to be in the name
     * space of the specified class, with the addition of declared imports,
     * parameters and variables. As in the Java language, <code>this</code> is a reserved
     * word which means the element of the collection being evaluated.
     * <P>Navigation through single-valued fields is specified by the Java
     * language syntax of <code>field_name.field_name....field_name</code>.
     * <P>A JDO implementation is allowed to reorder the filter expression
     * for optimization purposes.
     * @param filter the query filter.
     */
    void setFilter(String filter);
    
    /** Set the import statements to be used to identify the fully qualified name of
     * variables or parameters.  Parameters and unbound variables might 
     * come from a different class from the candidate class, and the names 
     * need to be declared in an import statement to eliminate ambiguity. 
     * Import statements are specified as a <code>String</code> with semicolon-separated 
     * statements. 
     * <P>The <code>String</code> parameter to this method follows the syntax of the  
     * import statement of the Java language.
     * @param imports import statements separated by semicolons.
     */
    void declareImports(String imports);
    
    /** Declare the list of parameters query execution.
     *
     * The parameter declaration is a <code>String</code> containing one or more query 
     * parameter declarations separated with commas. Each parameter named 
     * in the parameter declaration must be bound to a value when 
     * the query is executed.
     * <P>The <code>String</code> parameter to this method follows the syntax for formal 
     * parameters in the Java language. 
     * @param parameters the list of parameters separated by commas.
     */
    void declareParameters(String parameters);
    
    /** Declare the unbound variables to be used in the query. Variables 
     * might be used in the filter, and these variables must be declared 
     * with their type. The unbound variable declaration is a <code>String</code> 
     * containing one or more unbound variable declarations separated 
     * with semicolons. It follows the syntax for local variables in 
     * the Java language.
     * @param variables the variables separated by semicolons.
     */
    void declareVariables(String variables);
    
    /** Set the ordering specification for the result <code>Collection</code>.  The
     * ordering specification is a <code>String</code> containing one or more ordering
     * declarations separated by commas.
     *
     * <P>Each ordering declaration is the name of the field on which
     * to order the results followed by one of the following words:
     * "<code>ascending</code>" or "<code>descending</code>".
     *
     *<P>The field must be declared in the candidate class or must be
     * a navigation expression starting with a field in the candidate class.
     *
     *<P>Valid field types are primitive types except <code>boolean</code>; wrapper types 
     * except <code>Boolean</code>; <code>BigDecimal</code>; <code>BigInteger</code>;
     * <code>String</code>; and <code>Date</code>.
     * @param ordering the ordering specification.
     */
    void setOrdering(String ordering);
    
    /** Set the ignoreCache option.  The default value for this option was
     * set by the <code>PersistenceManagerFactory</code> or the
     * <code>PersistenceManager</code> used to create this <code>Query</code>.
     *
     * The ignoreCache option setting specifies whether the query should execute
     * entirely in the back end, instead of in the cache.  If this flag is set
     * to <code>true</code>, an implementation might be able to optimize the query
     * execution by ignoring changed values in the cache.  For optimistic
     * transactions, this can dramatically improve query response times.
     * @param ignoreCache the setting of the ignoreCache option.
     */
    void setIgnoreCache(boolean ignoreCache);   
    
    /** Get the ignoreCache option setting.
     * @return the ignoreCache option setting.
     * @see #setIgnoreCache
     */
    boolean getIgnoreCache();
    
    /** Verify the elements of the query and provide a hint to the query to
     * prepare and optimize an execution plan.
     */
    void compile();
    
    /** Execute the query and return the filtered Collection.
     * @return the filtered <code>Collection</code>.
     * @see #executeWithArray(Object[] parameters)
     */
    Object execute();
    
    /** Execute the query and return the filtered <code>Collection</code>.
     * @return the filtered <code>Collection</code>.
     * @see #executeWithArray(Object[] parameters)
     * @param p1 the value of the first parameter declared.
     */
    Object execute(Object p1);
    
    /** Execute the query and return the filtered <code>Collection</code>.
     * @return the filtered <code>Collection</code>.
     * @see #executeWithArray(Object[] parameters)
     * @param p1 the value of the first parameter declared.
     * @param p2 the value of the second parameter declared.
     */
    Object execute(Object p1, Object p2);
    
    /** Execute the query and return the filtered <code>Collection</code>.
     * @return the filtered <code>Collection</code>.
     * @see #executeWithArray(Object[] parameters)
     * @param p1 the value of the first parameter declared.
     * @param p2 the value of the second parameter declared.
     * @param p3 the value of the third parameter declared.
     */
    Object execute(Object p1, Object p2, Object p3);
    
    /** Execute the query and return the filtered <code>Collection</code>.  The query
     * is executed with the parameters set by the <code>Map</code> values.  Each <code>Map</code> entry
     * consists of a key which is the name of the parameter in the 
     * <code>declareParameters</code> method, and a value which is the value used in 
     * the <code>execute</code> method.  The keys in the <code>Map</code> and the declared parameters 
     * must exactly match or a <code>JDOUserException</code> is thrown.
     * @return the filtered <code>Collection</code>.
     * @see #executeWithArray(Object[] parameters)
     * @param parameters the <code>Map</code> containing all of the parameters.
     */
    Object executeWithMap (Map parameters);
    
    /** Execute the query and return the filtered <code>Collection</code>.
     *
     * <P>The execution of the query obtains the values of the parameters and
     * matches them against the declared parameters in order.  The names
     * of the declared parameters are ignored.  The type of
     * the declared parameters must match the type of the passed parameters,
     * except that the passed parameters might need to be unwrapped to get
     * their primitive values.
     *
     * <P>The filter, import, declared parameters, declared variables, and
     * ordering statements are verified for consistency.
     *
     * <P>Each element in the candidate <code>Collection</code> is examined to see that it
     * is assignment compatible to the <code>Class</code> of the query.  It is then evaluated
     * by the Boolean expression of the filter.  The element passes the filter
     * if there exist unique values for all variables for which the filter
     * expression evaluates to <code>true</code>.
     * @return the filtered <code>Collection</code>.
     * @param parameters the <code>Object</code> array with all of the parameters.
     */
    Object executeWithArray (Object[] parameters);
    
    /** Get the <code>PersistenceManager</code> associated with this <code>Query</code>.
     *
     * <P>If this <code>Query</code> was restored from a serialized form, it has no 
     * <code>PersistenceManager</code>, and this method returns <code>null</code>.
     * @return the <code>PersistenceManager</code> associated with this <code>Query</code>.
     */
    PersistenceManager getPersistenceManager();
  
    /** Close a query result and release any resources associated with it.  The
     * parameter is the return from <code>execute(...)</code> and might have iterators open on it.
     * Iterators associated with the query result are invalidated: they return <code>false</code>
     * to <code>hasNext()</code> and throw <code>NoSuchElementException</code> to <code>next()</code>.
     * @param queryResult the result of <code>execute(...)</code> on this <code>Query</code> instance.
     */    
    void close (Object queryResult);
    
    /** Close all query results associated with this <code>Query</code> instance, and release all
     * resources associated with them.  The query results might have iterators open
     * on them.  Iterators associated with the query results are invalidated:
     * they return <code>false</code> to <code>hasNext()</code> and throw
     * <code>NoSuchElementException</code> to <code>next()</code>.
     */    
    void closeAll ();
}

