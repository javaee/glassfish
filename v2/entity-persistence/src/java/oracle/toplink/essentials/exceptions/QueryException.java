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
package oracle.toplink.essentials.exceptions;

import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator;
import oracle.toplink.essentials.sessions.Record;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>: This exception is used for any problem that is detected with a query.
 */
public class QueryException extends ValidationException {
    protected transient DatabaseQuery query;
    protected transient AbstractRecord queryArguments;
    public final static int ADDITIONAL_SIZE_QUERY_NOT_SPECIFIED = 6001;
    public final static int AGGREGATE_OBJECT_CANNOT_BE_DELETED = 6002;
    public final static int ARGUMENT_SIZE_MISMATCH_IN_QUERY_AND_QUERY_DEFINITION = 6003;
    public final static int BACKUP_CLONE_IS_ORIGINAL_FROM_PARENT = 6004;
    public final static int BACKUP_CLONE_IS_ORIGINAL_FROM_SELF = 6005;
    public final static int BATCH_READING_NOT_SUPPORTED = 6006;
    public final static int DESCRIPTOR_IS_MISSING = 6007;
    public final static int DESCRIPTOR_IS_MISSING_FOR_NAMED_QUERY = 6008;
    public final static int INCORRECT_SIZE_QUERY_FOR_CURSOR_STREAM = 6013;
    public final static int INVALID_QUERY = 6014;
    public final static int INVALID_QUERY_KEY_IN_EXPRESSION = 6015;
    public final static int INVALID_QUERY_ON_SERVER_SESSION = 6016;
    public final static int NO_CONCRETE_CLASS_INDICATED = 6020;
    public final static int NO_CURSOR_SUPPORT = 6021;
    public final static int OBJECT_TO_INSERT_IS_EMPTY = 6023;
    public final static int OBJECT_TO_MODIFY_NOT_SPECIFIED = 6024;
    public final static int QUERY_NOT_DEFINED = 6026;
    public final static int QUERY_SENT_TO_INACTIVE_UNIT_OF_WORK = 6027;
    public final static int READ_BEYOND_QUERY = 6028;
    public final static int REFERENCE_CLASS_MISSING = 6029;
    public final static int REFRESH_NOT_POSSIBLE_WITHOUT_CACHE = 6030;
    public final static int SIZE_ONLY_SUPPORTED_ON_EXPRESSION_QUERIES = 6031;
    public final static int SQL_STATEMENT_NOT_SET_PROPERLY = 6032;
    public final static int INVALID_QUERY_ITEM = 6034;
    public final static int SELECTION_OBJECT_CANNOT_BE_NULL = 6041;
    public final static int UNNAMED_QUERY_ON_SESSION_BROKER = 6042;
    public final static int REPORT_RESULT_WITHOUT_PKS = 6043;
    public final static int NULL_PRIMARY_KEY_IN_BUILDING_OBJECT = 6044;
    public final static int NO_DESCRIPTOR_FOR_SUBCLASS = 6045;
    public final static int CANNOT_DELETE_READ_ONLY_OBJECT = 6046;
    public final static int INVALID_OPERATOR = 6047;
    public final static int ILLEGAL_USE_OF_GETFIELD = 6048;
    public final static int ILLEGAL_USE_OF_GETTABLE = 6049;
    public final static int REPORT_QUERY_RESULT_SIZE_MISMATCH = 6050;
    public final static int CANNOT_CACHE_PARTIAL_OBJECT = 6051;
    public final static int OUTER_JOIN_ONLY_VALID_FOR_ONE_TO_ONE = 6052;
    public final static int CANNOT_ADD_TO_CONTAINER = 6054;
    public static final int METHOD_INVOCATION_FAILED = 6055;
    public static final int CANNOT_CREATE_CLONE = 6056;
    public static final int METHOD_NOT_VALID = 6057;
    public static final int METHOD_DOES_NOT_EXIST_IN_CONTAINER_CLASS = 6058;
    public static final int COULD_NOT_INSTANTIATE_CONTAINER_CLASS = 6059;
    public static final int MAP_KEY_NOT_COMPARABLE = 6060;
    public static final int CANNOT_ACCESS_METHOD_ON_OBJECT = 6061;
    public static final int CALLED_METHOD_THREW_EXCEPTION = 6062;
    public final static int INVALID_OPERATION = 6063;
    public final static int CANNOT_REMOVE_FROM_CONTAINER = 6064;
    public final static int CANNOT_ADD_ELEMENT = 6065;
    public static final int BACKUP_CLONE_DELETED = 6066;
    public final static int CANNOT_ACCESS_FIELD_ON_OBJECT = 6067;
    public final static int CANNOT_COMPARE_TABLES_IN_EXPRESSION = 6068;
    public final static int INVALID_TABLE_FOR_FIELD_IN_EXPRESSION = 6069;
    public final static int INVALID_USE_OF_TO_MANY_QUERY_KEY_IN_EXPRESSION = 6070;
    public final static int INVALID_USE_OF_ANY_OF_IN_EXPRESSION = 6071;
    public final static int CANNOT_QUERY_ACROSS_VARIABLE_ONE_TO_ONE_MAPPING = 6072;
    public final static int ILL_FORMED_EXPRESSION = 6073;
    public final static int CANNOT_CONFORM_EXPRESSION = 6074;
    public final static int INVALID_OPERATOR_FOR_OBJECT_EXPRESSION = 6075;
    public final static int UNSUPPORTED_MAPPING_FOR_OBJECT_COMPARISON = 6076;
    public final static int OBJECT_COMPARISON_CANNOT_BE_PARAMETERIZED = 6077;
    public final static int INCORRECT_CLASS_FOR_OBJECT_COMPARISON = 6078;
    public final static int CANNOT_COMPARE_TARGET_FOREIGN_KEYS_TO_NULL = 6079;
    public final static int INVALID_DATABASE_CALL = 6080;
    public final static int INVALID_DATABASE_ACCESSOR = 6081;
    public final static int METHOD_DOES_NOT_EXIST_ON_EXPRESSION = 6082;
    public final static int IN_CANNOT_BE_PARAMETERIZED = 6083;
    public final static int REDIRECTION_CLASS_OR_METHOD_NOT_SET = 6084;
    public final static int REDIRECTION_METHOD_NOT_DEFINED_CORRECTLY = 6085;
    public final static int REDIRECTION_METHOD_ERROR = 6086;
    public final static int EXAMPLE_AND_REFERENCE_OBJECT_CLASS_MISMATCH = 6087;
    public final static int NO_ATTRIBUTES_FOR_REPORT_QUERY = 6088;
    public final static int NO_EXPRESSION_BUILDER_CLASS_FOUND = 6089;
    public final static int CANNOT_SET_REPORT_QUERY_TO_CHECK_CACHE_ONLY = 6090;
    public final static int TYPE_MISMATCH_BETWEEN_ATTRIBUTE_AND_CONSTANT_ON_EXPRESSION = 6091;
    public final static int MUST_INSTANTIATE_VALUEHOLDERS = 6092;
    public final static int PARAMETER_NAME_MISMATCH = 6094;
    public final static int CLONE_METHOD_REQUIRED = 6095;
    public final static int CLONE_METHOD_INACCESSIBLE = 6096;
    public final static int CLONE_METHOD_THORW_EXCEPTION = 6097;
    public final static int UNEXPECTED_INVOCATION = 6098;
    public final static int JOINING_ACROSS_INHERITANCE_WITH_MULTIPLE_TABLES = 6099;
    public final static int MULTIPLE_ROWS_DETECTED_FROM_SINGLE_OBJECT_READ = 6100;
    public final static int HISTORICAL_QUERIES_MUST_PRESERVE_GLOBAL_CACHE = 6101;
    public final static int HISTORICAL_QUERIES_ONLY_SUPPORTED_ON_ORACLE = 6102;
    public final static int INVALID_QUERY_ON_HISTORICAL_SESSION = 6103;
    public final static int OBJECT_DOES_NOT_EXIST_IN_CACHE = 6104;
    public final static int MUST_USE_CURSOR_STREAM_POLICY = 6105;
    public final static int CLASS_PK_DOES_NOT_EXIST_IN_CACHE = 6106;
    public final static int UPDATE_STATEMENTS_NOT_SPECIFIED = 6107;
    public final static int INHERITANCE_WITH_MULTIPLE_TABLES_NOT_SUPPORTED = 6108;
    public final static int QUERY_FETCHGROUP_NOT_DEFINED_IN_DESCRIPTOR = 6109;
    public final static int CANNOT_CONFORM_UNFETCHED_ATTRIBUTE = 6110;
    public final static int FETCH_GROUP_ATTRIBUTE_NOT_MAPPED = 6111;
    public final static int FETCH_GROUP_NOT_SUPPORT_ON_REPORT_QUERY = 6112;
    public final static int FETCH_GROUP_NOT_SUPPORT_ON_PARTIAL_ATTRIBUTE_READING = 6113;
    public final static int FETCHGROUP_VALID_ONLY_IF_FETCHGROUP_MANAGER_IN_DESCRIPTOR = 6114;
    public final static int ISOLATED_QUERY_EXECUTED_ON_SERVER_SESSION = 6115;
    public final static int NO_CALL_OR_INTERACTION_SPECIFIED = 6116;
    public final static int CANNOT_CACHE_CURSOR_RESULTS_ON_QUERY = 6117;
    public final static int CANNOT_CACHE_ISOLATED_DATA_ON_QUERY = 6118;
    public final static int MAPPING_FOR_EXPRESSION_DOES_NOT_SUPPORT_JOINING = 6119;
    public final static int SPECIFIED_PARTIAL_ATTRIBUTE_DOES_NOT_EXIST = 6120;
    public final static int INVALID_BUILDER_IN_QUERY = 6121;
    public final static int INVALID_EXPRESSION = 6122;
    public final static int INVALID_CONTAINER_CLASS = 6123;
    public final static int INCORRECT_QUERY_FOUND = 6124;
    public final static int CLEAR_QUERY_RESULTS_NOT_SUPPORTED = 6125;
    public final static int CANNOT_CONFORM_AND_CACHE_QUERY_RESULTS = 6126;
    public final static int REFLECTIVE_CALL_ON_TOPLINK_CLASS_FAILED = 6127;
    public final static int BATCH_READING_NOT_SUPPORTED_WITH_CALL = 6128;
    public final static int REFRESH_NOT_POSSIBLE_WITH_CHECK_CACHE_ONLY = 6129;
    public final static int DISCRIMINATOR_COLUMN_NOT_SELECTED = 6130;
    public final static int DELETE_ALL_QUERY_SPECIFIES_OBJECTS_BUT_NOT_SELECTION_CRITERIA = 6131;
    public final static int NAMED_ARGUMENT_NOT_FOUND_IN_QUERY_PARAMETERS = 6132;
    public final static int UPDATE_ALL_QUERY_ADD_UPDATE_FIELD_IS_NULL = 6133;
    public final static int UPDATE_ALL_QUERY_ADD_UPDATE_DOES_NOT_DEFINE_FIELD = 6134;
    public final static int UPDATE_ALL_QUERY_ADD_UPDATE_DEFINES_WRONG_FIELD = 6135;
    public final static int POLYMORPHIC_REPORT_ITEM_NOT_SUPPORTED = 6136;
    public final static int EXCEPTION_WHILE_USING_CONSTRUCTOR_EXPRESSION = 6137;
    public final static int TEMP_TABLES_NOT_SUPPORTED = 6138;
    public final static int MAPPING_FOR_FIELDRESULT_NOT_FOUND = 6139;
    public final static int JOIN_EXPRESSIONS_NOT_APPLICABLE_ON_NON_OBJECT_REPORT_ITEM = 6140;
    public final static int DISTINCT_COUNT_ON_OUTER_JOINED_COMPOSITE_PK = 6141;
    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected QueryException(String message) {
        super(message);
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected QueryException(String message, DatabaseQuery query) {
        super(message);
        this.query = query;
    }

    public static QueryException additionalSizeQueryNotSpecified(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, ADDITIONAL_SIZE_QUERY_NOT_SPECIFIED, args), query);
        queryException.setErrorCode(ADDITIONAL_SIZE_QUERY_NOT_SPECIFIED);
        return queryException;
    }

    public static QueryException aggregateObjectCannotBeDeletedOrWritten(ClassDescriptor descriptor, DatabaseQuery query) {
        Object[] args = { descriptor.toString(), CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, AGGREGATE_OBJECT_CANNOT_BE_DELETED, args), query);
        queryException.setErrorCode(AGGREGATE_OBJECT_CANNOT_BE_DELETED);
        return queryException;
    }

    public static QueryException argumentSizeMismatchInQueryAndQueryDefinition(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, ARGUMENT_SIZE_MISMATCH_IN_QUERY_AND_QUERY_DEFINITION, args), query);
        queryException.setErrorCode(ARGUMENT_SIZE_MISMATCH_IN_QUERY_AND_QUERY_DEFINITION);
        return queryException;
    }

    public static QueryException namedArgumentNotFoundInQueryParameters(String argumentName) {
        Object[] args = {argumentName};
        
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, NAMED_ARGUMENT_NOT_FOUND_IN_QUERY_PARAMETERS, args));
        queryException.setErrorCode(NAMED_ARGUMENT_NOT_FOUND_IN_QUERY_PARAMETERS);
        return queryException;
    }
    
    public static QueryException backupCloneIsDeleted(Object clone) {
        Object[] args = { clone, clone.getClass(), new Integer(System.identityHashCode(clone)), CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, BACKUP_CLONE_DELETED, args));
        queryException.setErrorCode(BACKUP_CLONE_DELETED);
        return queryException;
    }

    public static QueryException backupCloneIsOriginalFromParent(Object clone) {
        // need to be verified
        Object[] args = { clone, clone.getClass(), new Integer(System.identityHashCode(clone)), CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, BACKUP_CLONE_IS_ORIGINAL_FROM_PARENT, args));
        queryException.setErrorCode(BACKUP_CLONE_IS_ORIGINAL_FROM_PARENT);
        return queryException;
    }

    public static QueryException backupCloneIsOriginalFromSelf(Object clone) {
        Object[] args = { clone, clone.getClass(), new Integer(System.identityHashCode(clone)), CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, BACKUP_CLONE_IS_ORIGINAL_FROM_SELF, args));
        queryException.setErrorCode(BACKUP_CLONE_IS_ORIGINAL_FROM_SELF);
        return queryException;
    }

    public static QueryException batchReadingNotSupported(DatabaseMapping mapping, DatabaseQuery query) {
        Object[] args = { mapping };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, BATCH_READING_NOT_SUPPORTED, args), query);
        queryException.setErrorCode(BATCH_READING_NOT_SUPPORTED);
        return queryException;
    }

    public static QueryException batchReadingNotSupported(DatabaseQuery query) {
        Object[] args = { };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, BATCH_READING_NOT_SUPPORTED_WITH_CALL, args), query);
        queryException.setErrorCode(BATCH_READING_NOT_SUPPORTED_WITH_CALL);
        return queryException;
    }
    
    public static QueryException calledMethodThrewException(java.lang.reflect.Method aMethod, Object object, Exception ex) {
        Object[] args = { aMethod, object, object.getClass() };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CALLED_METHOD_THREW_EXCEPTION, args));
        queryException.setErrorCode(CALLED_METHOD_THREW_EXCEPTION);
        queryException.setInternalException(ex);
        return queryException;
    }

    public static ValidationException cannotAccessFieldOnObject(java.lang.reflect.Field aField, Object anObject) {
        Object[] args = { aField, anObject, anObject.getClass() };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_ACCESS_FIELD_ON_OBJECT, args));
        validationException.setErrorCode(CANNOT_ACCESS_FIELD_ON_OBJECT);
        return validationException;
    }
    
    public static ValidationException cannotAccessMethodOnObject(java.lang.reflect.Method aMethod, Object anObject) {
        Object[] args = { aMethod, anObject, anObject.getClass() };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_ACCESS_METHOD_ON_OBJECT, args));
        validationException.setErrorCode(CANNOT_ACCESS_METHOD_ON_OBJECT);
        return validationException;
    }

    public static QueryException cannotAddElement(Object anObject, Object aContainer, Exception ex) {
        Object[] args = { anObject, anObject.getClass(), aContainer.getClass() };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_ADD_ELEMENT, args));
        queryException.setErrorCode(CANNOT_ADD_ELEMENT);
        queryException.setInternalException(ex);
        return queryException;
    }

    public static QueryException cannotAddToContainer(Object anObject, Object aContainer, ContainerPolicy policy) {
        Object[] args = { anObject, anObject.getClass(), aContainer.getClass(), policy };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_ADD_TO_CONTAINER, args));
        queryException.setErrorCode(CANNOT_ADD_TO_CONTAINER);
        return queryException;
    }

    public static QueryException cannotCacheCursorResultsOnQuery(DatabaseQuery query) {
        Object[] args = {  };
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_CACHE_CURSOR_RESULTS_ON_QUERY, args));
        queryException.setQuery(query);
        queryException.setErrorCode(CANNOT_CACHE_CURSOR_RESULTS_ON_QUERY);
        return queryException;
    }

    public static QueryException cannotCacheIsolatedDataOnQuery(DatabaseQuery query) {
        Object[] args = {  };
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_CACHE_ISOLATED_DATA_ON_QUERY, args));
        queryException.setQuery(query);
        queryException.setErrorCode(CANNOT_CACHE_ISOLATED_DATA_ON_QUERY);
        return queryException;
    }

    public static QueryException cannotCachePartialObjects(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_CACHE_PARTIAL_OBJECT, args));
        queryException.setQuery(query);
        queryException.setErrorCode(CANNOT_CACHE_PARTIAL_OBJECT);
        return queryException;
    }

    public static QueryException cannotCompareTablesInExpression(Object data) {
        Object[] args = { data };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_COMPARE_TABLES_IN_EXPRESSION, args));
        queryException.setErrorCode(CANNOT_COMPARE_TABLES_IN_EXPRESSION);
        return queryException;
    }

    public static QueryException cannotCompareTargetForeignKeysToNull(Expression expression, Object value, DatabaseMapping mapping) {
        Object[] args = { expression, mapping, value, CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_COMPARE_TARGET_FOREIGN_KEYS_TO_NULL, args));
        queryException.setErrorCode(CANNOT_COMPARE_TARGET_FOREIGN_KEYS_TO_NULL);
        return queryException;
    }

    public static QueryException cannotConformAndCacheQueryResults(ReadQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_CONFORM_AND_CACHE_QUERY_RESULTS, args), query);
        queryException.setErrorCode(CANNOT_CONFORM_AND_CACHE_QUERY_RESULTS);
        return queryException;
    }

    public static QueryException cannotConformExpression() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_CONFORM_EXPRESSION, args));
        queryException.setErrorCode(CANNOT_CONFORM_EXPRESSION);
        return queryException;
    }

    public static QueryException cannotCreateClone(ContainerPolicy policy, Object anObject) {
        Object[] args = { anObject, anObject.getClass(), policy };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_CREATE_CLONE, args));
        queryException.setErrorCode(CANNOT_CREATE_CLONE);
        return queryException;
    }

    public static QueryException cannotDeleteReadOnlyObject(Object anObject) {
        Object[] args = { anObject.getClass().toString() };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_DELETE_READ_ONLY_OBJECT, args));
        queryException.setErrorCode(CANNOT_DELETE_READ_ONLY_OBJECT);
        return queryException;
    }

    public static QueryException cannotQueryAcrossAVariableOneToOneMapping(DatabaseMapping mapping, ClassDescriptor descriptor) {
        Object[] args = { descriptor.toString(), mapping.toString(), CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_QUERY_ACROSS_VARIABLE_ONE_TO_ONE_MAPPING, args));
        queryException.setErrorCode(CANNOT_QUERY_ACROSS_VARIABLE_ONE_TO_ONE_MAPPING);
        return queryException;
    }

    public static QueryException cannotRemoveFromContainer(Object anObject, Object aContainer, ContainerPolicy policy) {
        Object[] args = { anObject, anObject.getClass(), aContainer.getClass(), policy };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_REMOVE_FROM_CONTAINER, args));
        queryException.setErrorCode(CANNOT_REMOVE_FROM_CONTAINER);
        return queryException;
    }

    public static QueryException cannotSetShouldCheckCacheOnlyOnReportQuery() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_SET_REPORT_QUERY_TO_CHECK_CACHE_ONLY, args));
        queryException.setErrorCode(CANNOT_SET_REPORT_QUERY_TO_CHECK_CACHE_ONLY);
        return queryException;
    }

    public static QueryException couldNotInstantiateContainerClass(Class aClass, Exception exception) {
        Object[] args = { aClass.toString() };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, COULD_NOT_INSTANTIATE_CONTAINER_CLASS, args));
        queryException.setErrorCode(COULD_NOT_INSTANTIATE_CONTAINER_CLASS);
        queryException.setInternalException(exception);
        return queryException;
    }

    public static QueryException descriptorIsMissing(Class referenceClass, DatabaseQuery query) {
        Object[] args = { referenceClass };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, DESCRIPTOR_IS_MISSING, args), query);
        queryException.setErrorCode(DESCRIPTOR_IS_MISSING);
        return queryException;
    }

    public static QueryException descriptorIsMissingForNamedQuery(Class domainClass, String queryName) {
        Object[] args = { domainClass.getName(), queryName };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, DESCRIPTOR_IS_MISSING_FOR_NAMED_QUERY, args));
        queryException.setErrorCode(DESCRIPTOR_IS_MISSING_FOR_NAMED_QUERY);
        return queryException;
    }

    public static QueryException discriminatorColumnNotSelected(String expectedColumn, String sqlResultSetMapping){
        Object[] args = { expectedColumn, sqlResultSetMapping };
    
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, DISCRIMINATOR_COLUMN_NOT_SELECTED, args));
        queryException.setErrorCode(DISCRIMINATOR_COLUMN_NOT_SELECTED);
        return queryException;
    }    
    /**
     * Oct 18, 2000 JED
     * Added this method and exception value
     */
    public static QueryException exampleAndReferenceObjectClassMismatch(Class exampleObjectClass, Class referenceObjectClass, DatabaseQuery query) {
        Object[] args = { exampleObjectClass, referenceObjectClass };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, EXAMPLE_AND_REFERENCE_OBJECT_CLASS_MISMATCH, args));
        queryException.setErrorCode(EXAMPLE_AND_REFERENCE_OBJECT_CLASS_MISMATCH);
        queryException.setQuery(query);
        return queryException;
    }

    /**
     * An exception was throwing while using a ReportQuery with a constructor expession
     */
    public static QueryException exceptionWhileUsingConstructorExpression(Exception thrownException, DatabaseQuery query) {
        Object[] args = { thrownException };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, EXCEPTION_WHILE_USING_CONSTRUCTOR_EXPRESSION, args));
        queryException.setErrorCode(EXCEPTION_WHILE_USING_CONSTRUCTOR_EXPRESSION);
        queryException.setQuery(query);
        return queryException;
    }

    /**
     * PUBLIC:
     * Return the exception error message.
     * TopLink error messages are multi-line so that detail descriptions of the exception are given.
     */
    public String getMessage() {
        if (getQuery() == null) {
            return super.getMessage();
        } else {
            return super.getMessage() + cr() + getIndentationString() + ExceptionMessageGenerator.getHeader("QueryHeader") + getQuery().toString();
        }
    }

    /**
     * PUBLIC:
     * Return the query in which the problem was detected.
     */
    public DatabaseQuery getQuery() {
        return query;
    }

    /**
     * PUBLIC:
     * Return the query argements used in the original query when exception is thrown
     */
    public Record getQueryArgumentsRecord() {
        return queryArguments;
    }

    public static QueryException illegalUseOfGetField(Object data) {
        Object[] args = { data };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, ILLEGAL_USE_OF_GETFIELD, args));
        queryException.setErrorCode(ILLEGAL_USE_OF_GETFIELD);
        return queryException;
    }

    public static QueryException illegalUseOfGetTable(Object data) {
        Object[] args = { data };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, ILLEGAL_USE_OF_GETTABLE, args));
        queryException.setErrorCode(ILLEGAL_USE_OF_GETTABLE);
        return queryException;
    }

    public static QueryException illFormedExpression(oracle.toplink.essentials.expressions.Expression queryKey) {
        Object[] args = { queryKey };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, ILL_FORMED_EXPRESSION, args));
        queryException.setErrorCode(ILL_FORMED_EXPRESSION);
        return queryException;
    }

    public static QueryException inCannotBeParameterized(DatabaseQuery query) {
        Object[] args = {  };

        QueryException exception = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, IN_CANNOT_BE_PARAMETERIZED, args), query);
        exception.setErrorCode(IN_CANNOT_BE_PARAMETERIZED);
        return exception;
    }

    public static QueryException incorrectClassForObjectComparison(Expression expression, Object value, DatabaseMapping mapping) {
        Object[] args = { expression, mapping, value, CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INCORRECT_CLASS_FOR_OBJECT_COMPARISON, args));
        queryException.setErrorCode(INCORRECT_CLASS_FOR_OBJECT_COMPARISON);
        return queryException;
    }

    public static QueryException incorrectSizeQueryForCursorStream(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INCORRECT_SIZE_QUERY_FOR_CURSOR_STREAM, args), query);
        queryException.setErrorCode(INCORRECT_SIZE_QUERY_FOR_CURSOR_STREAM);
        return queryException;
    }

    public static QueryException incorrectQueryObjectFound(DatabaseQuery query, Class expectedQueryClass) {
        Object[] args = { expectedQueryClass, query.getClass() };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INCORRECT_QUERY_FOUND, args), query);
        queryException.setErrorCode(INCORRECT_QUERY_FOUND);
        return queryException;
    }

    public static QueryException invalidContainerClass(Class containerGiven, Class containerRequired) {
        Object[] args = { containerGiven, containerRequired };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_CONTAINER_CLASS, args));
        queryException.setErrorCode(INVALID_CONTAINER_CLASS);
        return queryException;
    }

    public static QueryException invalidDatabaseAccessor(oracle.toplink.essentials.internal.databaseaccess.Accessor accessor) {
        Object[] args = { accessor };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_DATABASE_ACCESSOR, args));
        queryException.setErrorCode(INVALID_DATABASE_ACCESSOR);
        return queryException;
    }

    public static QueryException invalidDatabaseCall(Call call) {
        Object[] args = { call };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_DATABASE_CALL, args));
        queryException.setErrorCode(INVALID_DATABASE_CALL);
        return queryException;
    }

    public static QueryException invalidExpressionForQueryItem(Expression expression, DatabaseQuery owner) {
        Object[] args = { expression };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_QUERY_ITEM, args), owner);
        queryException.setErrorCode(INVALID_QUERY_ITEM);
        return queryException;
    }

    public static QueryException invalidOperation(String operation) {
        Object[] args = { operation };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_OPERATION, args));
        queryException.setErrorCode(INVALID_OPERATION);
        return queryException;
    }

    public static QueryException invalidOperator(Object data) {
        Object[] args = { data };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_OPERATOR, args));
        queryException.setErrorCode(INVALID_OPERATOR);
        return queryException;
    }

    public static QueryException invalidOperatorForObjectComparison(Expression expression) {
        Object[] args = { expression, CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_OPERATOR_FOR_OBJECT_EXPRESSION, args));
        queryException.setErrorCode(INVALID_OPERATOR_FOR_OBJECT_EXPRESSION);
        return queryException;
    }

    public static QueryException invalidQuery(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_QUERY, args), query);
        queryException.setErrorCode(INVALID_QUERY);
        return queryException;
    }

    public static QueryException invalidBuilderInQuery(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_BUILDER_IN_QUERY, args), query);
        queryException.setErrorCode(INVALID_BUILDER_IN_QUERY);
        return queryException;
    }

    public static QueryException invalidQueryKeyInExpression(Object data) {
        Object[] args = { data };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_QUERY_KEY_IN_EXPRESSION, args));
        queryException.setErrorCode(INVALID_QUERY_KEY_IN_EXPRESSION);
        return queryException;
    }

    public static QueryException invalidExpression(Object expression) {
        Object[] args = { expression };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_EXPRESSION, args));
        queryException.setErrorCode(INVALID_EXPRESSION);
        return queryException;
    }

    public static QueryException mappingForExpressionDoesNotSupportJoining(Object expression) {
        Object[] args = { expression };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, MAPPING_FOR_EXPRESSION_DOES_NOT_SUPPORT_JOINING, args));
        queryException.setErrorCode(MAPPING_FOR_EXPRESSION_DOES_NOT_SUPPORT_JOINING);
        return queryException;
    }
    
    public static QueryException mappingForFieldResultNotFound(String[] attributeNames, int currentString){
        String attributeName ="";
        for(int i=0; i<attributeNames.length;i++){
            attributeName=attributeName+attributeNames[i];
        }
        Object[] args = { attributeName, attributeNames[currentString] };
    
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, MAPPING_FOR_FIELDRESULT_NOT_FOUND, args));
        queryException.setErrorCode(MAPPING_FOR_FIELDRESULT_NOT_FOUND);
        return queryException;
    }  

    public static QueryException invalidQueryOnHistoricalSession(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_QUERY_ON_HISTORICAL_SESSION, args), query);
        queryException.setErrorCode(INVALID_QUERY_ON_HISTORICAL_SESSION);
        return queryException;
    }

    public static QueryException invalidQueryOnServerSession(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_QUERY_ON_SERVER_SESSION, args), query);
        queryException.setErrorCode(INVALID_QUERY_ON_SERVER_SESSION);
        return queryException;
    }

    public static QueryException invalidTableForFieldInExpression(Object data) {
        Object[] args = { data };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_TABLE_FOR_FIELD_IN_EXPRESSION, args));
        queryException.setErrorCode(INVALID_TABLE_FOR_FIELD_IN_EXPRESSION);
        return queryException;
    }

    public static QueryException invalidUseOfAnyOfInExpression(Object data) {
        Object[] args = { data };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_USE_OF_ANY_OF_IN_EXPRESSION, args));
        queryException.setErrorCode(INVALID_USE_OF_ANY_OF_IN_EXPRESSION);
        return queryException;
    }

    public static QueryException joinExpressionsNotApplicableOnNonObjectReportItem(String expressionType, String itemName) {
        Object[] args = { expressionType, itemName };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, JOIN_EXPRESSIONS_NOT_APPLICABLE_ON_NON_OBJECT_REPORT_ITEM, args));
        queryException.setErrorCode(JOIN_EXPRESSIONS_NOT_APPLICABLE_ON_NON_OBJECT_REPORT_ITEM);
        return queryException;
    }

    public static QueryException joiningAcrossInheritanceClassWithMultipleTablesNotSupported(DatabaseQuery query, Class joinClass) {
        Object[] args = { query, joinClass };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, JOINING_ACROSS_INHERITANCE_WITH_MULTIPLE_TABLES, args));
        queryException.setErrorCode(JOINING_ACROSS_INHERITANCE_WITH_MULTIPLE_TABLES);
        return queryException;
    }

    public static QueryException invalidUseOfToManyQueryKeyInExpression(Object data) {
        Object[] args = { data };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INVALID_USE_OF_TO_MANY_QUERY_KEY_IN_EXPRESSION, args));
        queryException.setErrorCode(INVALID_USE_OF_TO_MANY_QUERY_KEY_IN_EXPRESSION);
        return queryException;
    }

    public static QueryException isolatedQueryExecutedOnServerSession() {
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, ISOLATED_QUERY_EXECUTED_ON_SERVER_SESSION, new Object[] {  }));
        queryException.setErrorCode(ISOLATED_QUERY_EXECUTED_ON_SERVER_SESSION);
        return queryException;
    }

    public static ValidationException mapKeyNotComparable(Object anObject, Object aContainer) {
        Object[] args = { anObject.toString(), anObject.getClass(), aContainer, aContainer.getClass() };

        ValidationException validationException = new ValidationException(ExceptionMessageGenerator.buildMessage(QueryException.class, MAP_KEY_NOT_COMPARABLE, args));
        validationException.setErrorCode(MAP_KEY_NOT_COMPARABLE);
        return validationException;
    }

    public static QueryException methodDoesNotExistInContainerClass(String methodName, Class aClass) {
        Object[] args = { methodName, aClass };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, METHOD_DOES_NOT_EXIST_IN_CONTAINER_CLASS, args));
        queryException.setErrorCode(METHOD_DOES_NOT_EXIST_IN_CONTAINER_CLASS);
        return queryException;
    }

    public static QueryException methodDoesNotExistOnExpression(String methodName, Class[] argTypes) {
        Object[] args = { methodName, argTypes };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, METHOD_DOES_NOT_EXIST_ON_EXPRESSION, args));
        queryException.setErrorCode(METHOD_DOES_NOT_EXIST_ON_EXPRESSION);
        return queryException;
    }

    public static QueryException methodInvocationFailed(java.lang.reflect.Method aMethod, Object anObject, Exception ex) {
        Object[] args = { aMethod, anObject, anObject.getClass() };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, METHOD_INVOCATION_FAILED, args));
        queryException.setErrorCode(METHOD_INVOCATION_FAILED);
        queryException.setInternalException(ex);
        return queryException;
    }

    public static QueryException methodNotValid(Object aReceiver, String methodName) {
        Object[] args = { methodName, aReceiver };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, METHOD_NOT_VALID, args));
        queryException.setErrorCode(METHOD_NOT_VALID);
        return queryException;
    }

    public static QueryException mustInstantiateValueholders() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, MUST_INSTANTIATE_VALUEHOLDERS, args));
        queryException.setErrorCode(MUST_INSTANTIATE_VALUEHOLDERS);
        return queryException;
    }

    /**
     * Oct 19, 2000 JED
     * Added this method and exception value
     */
    public static QueryException noAttributesForReportQuery(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, NO_ATTRIBUTES_FOR_REPORT_QUERY, args));
        queryException.setErrorCode(NO_ATTRIBUTES_FOR_REPORT_QUERY);
        queryException.setQuery(query);
        return queryException;
    }

    public static QueryException noConcreteClassIndicated(AbstractRecord row, DatabaseQuery query) {
        Object[] args = { row };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, NO_CONCRETE_CLASS_INDICATED, args), query);
        queryException.setErrorCode(NO_CONCRETE_CLASS_INDICATED);
        return queryException;
    }

    public static QueryException noCallOrInteractionSpecified() {
        Object[] args = {  };
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, QueryException.NO_CALL_OR_INTERACTION_SPECIFIED, args));
        queryException.setErrorCode(NO_CALL_OR_INTERACTION_SPECIFIED);
        return queryException;
    }

    public static QueryException noCursorSupport(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, NO_CURSOR_SUPPORT, args), query);
        queryException.setErrorCode(NO_CURSOR_SUPPORT);
        return queryException;
    }

    public static QueryException noDescriptorForClassFromInheritancePolicy(DatabaseQuery query, Class referenceClass) {
        Object[] args = { String.valueOf(referenceClass) };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, NO_DESCRIPTOR_FOR_SUBCLASS, args), query);
        queryException.setErrorCode(NO_DESCRIPTOR_FOR_SUBCLASS);
        return queryException;
    }

    public static QueryException noExpressionBuilderFound(Expression expression) {
        Object[] args = { expression, CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, NO_EXPRESSION_BUILDER_CLASS_FOUND, args));
        queryException.setErrorCode(NO_EXPRESSION_BUILDER_CLASS_FOUND);
        return queryException;
    }

    public static QueryException nullPrimaryKeyInBuildingObject(DatabaseQuery query, AbstractRecord databaseRow) {
        Object[] args = { databaseRow };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, NULL_PRIMARY_KEY_IN_BUILDING_OBJECT, args), query);
        queryException.setErrorCode(NULL_PRIMARY_KEY_IN_BUILDING_OBJECT);
        return queryException;
    }

    public static QueryException objectComparisonsCannotBeParameterized(Expression expression) {
        Object[] args = { expression, CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, OBJECT_COMPARISON_CANNOT_BE_PARAMETERIZED, args));
        queryException.setErrorCode(OBJECT_COMPARISON_CANNOT_BE_PARAMETERIZED);
        return queryException;
    }

    public static QueryException objectDoesNotExistInCache(Object object) {
        Object[] args = { object };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, OBJECT_DOES_NOT_EXIST_IN_CACHE, args));
        queryException.setErrorCode(OBJECT_DOES_NOT_EXIST_IN_CACHE);
        return queryException;
    }

    public static QueryException classPkDoesNotExistInCache(Class theClass, java.util.Vector primaryKey) {
        Object[] args = { theClass, primaryKey };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CLASS_PK_DOES_NOT_EXIST_IN_CACHE, args));
        queryException.setErrorCode(CLASS_PK_DOES_NOT_EXIST_IN_CACHE);
        return queryException;
    }

    public static QueryException clearQueryResultsNotSupported(ReadQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CLEAR_QUERY_RESULTS_NOT_SUPPORTED, args), query);
        queryException.setErrorCode(CLEAR_QUERY_RESULTS_NOT_SUPPORTED);
        return queryException;
    }

    public static QueryException objectToInsertIsEmpty(DatabaseTable table) {
        Object[] args = { table };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, OBJECT_TO_INSERT_IS_EMPTY, args));
        queryException.setErrorCode(OBJECT_TO_INSERT_IS_EMPTY);
        return queryException;
    }

    public static QueryException objectToModifyNotSpecified(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, OBJECT_TO_MODIFY_NOT_SPECIFIED, args), query);
        queryException.setErrorCode(OBJECT_TO_MODIFY_NOT_SPECIFIED);
        return queryException;
    }

    public static QueryException outerJoinIsOnlyValidForOneToOneMappings(DatabaseMapping mapping) {
        Object[] args = { mapping.toString() };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, OUTER_JOIN_ONLY_VALID_FOR_ONE_TO_ONE, args));
        queryException.setErrorCode(OUTER_JOIN_ONLY_VALID_FOR_ONE_TO_ONE);
        return queryException;
    }

    public static QueryException queryNotDefined() {
        Object[] args = { "", "" };
        return queryNotDefined(args);
    }

    public static QueryException queryNotDefined(String queryName) {
        Object[] args = { queryName, "" };
        return queryNotDefined(args);
    }

    public static QueryException queryNotDefined(String queryName, Class domainClass) {
        Object[] args = { queryName, domainClass };
        return queryNotDefined(args);
    }

    private static QueryException queryNotDefined(Object[] args) {
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, QUERY_NOT_DEFINED, args));
        queryException.setErrorCode(QUERY_NOT_DEFINED);
        return queryException;
    }

    public static QueryException querySentToInactiveUnitOfWork(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, QUERY_SENT_TO_INACTIVE_UNIT_OF_WORK, args), query);
        queryException.setErrorCode(QUERY_SENT_TO_INACTIVE_UNIT_OF_WORK);
        return queryException;
    }

    public static QueryException readBeyondStream(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, READ_BEYOND_QUERY, args), query);
        queryException.setErrorCode(READ_BEYOND_QUERY);
        return queryException;
    }

    public static QueryException redirectionClassOrMethodNotSet(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, REDIRECTION_CLASS_OR_METHOD_NOT_SET, args), query);
        queryException.setErrorCode(REDIRECTION_CLASS_OR_METHOD_NOT_SET);
        return queryException;
    }

    public static QueryException redirectionMethodError(Exception exception, DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, REDIRECTION_METHOD_ERROR, args), query);
        queryException.setInternalException(exception);
        queryException.setErrorCode(REDIRECTION_METHOD_ERROR);
        return queryException;
    }

    public static QueryException redirectionMethodNotDefinedCorrectly(Class methodClass, String methodName, Exception exception, DatabaseQuery query) {
        Object[] args = { methodClass, methodName, CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, REDIRECTION_METHOD_NOT_DEFINED_CORRECTLY, args), query);
        queryException.setInternalException(exception);
        queryException.setErrorCode(REDIRECTION_METHOD_NOT_DEFINED_CORRECTLY);
        return queryException;
    }

    public static QueryException referenceClassMissing(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, REFERENCE_CLASS_MISSING, args), query);
        queryException.setErrorCode(REFERENCE_CLASS_MISSING);
        return queryException;
    }

    public static QueryException refreshNotPossibleWithoutCache(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, REFRESH_NOT_POSSIBLE_WITHOUT_CACHE, args), query);
        queryException.setErrorCode(REFRESH_NOT_POSSIBLE_WITHOUT_CACHE);
        return queryException;
    }

    public static QueryException reportQueryResultSizeMismatch(int expected, int retrieved) {
        Object[] args = { new Integer(expected), new Integer(retrieved) };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, REPORT_QUERY_RESULT_SIZE_MISMATCH, args));
        queryException.setErrorCode(REPORT_QUERY_RESULT_SIZE_MISMATCH);
        return queryException;
    }

    public static QueryException reportQueryResultWithoutPKs(ReportQueryResult result) {
        Object[] args = { result, CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, REPORT_RESULT_WITHOUT_PKS, args), null);
        queryException.setErrorCode(REPORT_RESULT_WITHOUT_PKS);
        return queryException;
    }

    public static QueryException parameterNameMismatch(String badParameterName) {
        Object[] args = { badParameterName };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, PARAMETER_NAME_MISMATCH, args), null);
        queryException.setErrorCode(PARAMETER_NAME_MISMATCH);
        return queryException;
    }

    public static QueryException polymorphicReportItemWithMultipletableNotSupported(String itemName, Expression expression, DatabaseQuery owner) {
        Object[] args = { itemName, expression };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, POLYMORPHIC_REPORT_ITEM_NOT_SUPPORTED, args), owner);
        queryException.setErrorCode(POLYMORPHIC_REPORT_ITEM_NOT_SUPPORTED);
        return queryException;
    }

    public static QueryException selectionObjectCannotBeNull(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, SELECTION_OBJECT_CANNOT_BE_NULL, args), query);
        queryException.setErrorCode(SELECTION_OBJECT_CANNOT_BE_NULL);
        return queryException;
    }

    /**
     * INTERNAL:
     * Set the query in which the problem was detected.
     */
    public void setQuery(DatabaseQuery query) {
        this.query = query;
    }

    /**
     * INTERNAL:
     * Set the query argements used in the original query when exception is thrown
     */
    public void setQueryArguments(AbstractRecord queryArguments) {
        this.queryArguments = queryArguments;
    }

    public static QueryException sizeOnlySupportedOnExpressionQueries(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, SIZE_ONLY_SUPPORTED_ON_EXPRESSION_QUERIES, args), query);
        queryException.setErrorCode(SIZE_ONLY_SUPPORTED_ON_EXPRESSION_QUERIES);
        return queryException;
    }

    public static QueryException specifiedPartialAttributeDoesNotExist(DatabaseQuery query, String attributeName, String targetClassName) {
        Object[] args = { attributeName, targetClassName };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, SPECIFIED_PARTIAL_ATTRIBUTE_DOES_NOT_EXIST, args), query);
        queryException.setErrorCode(SPECIFIED_PARTIAL_ATTRIBUTE_DOES_NOT_EXIST);
        return queryException;
    }

    public static QueryException sqlStatementNotSetProperly(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, SQL_STATEMENT_NOT_SET_PROPERLY, args), query);
        queryException.setErrorCode(SQL_STATEMENT_NOT_SET_PROPERLY);
        return queryException;
    }

    public static QueryException typeMismatchBetweenAttributeAndConstantOnExpression(Class constantClass, Class attributeClass) {
        Object[] args = { constantClass, attributeClass };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, TYPE_MISMATCH_BETWEEN_ATTRIBUTE_AND_CONSTANT_ON_EXPRESSION, args));
        queryException.setErrorCode(TYPE_MISMATCH_BETWEEN_ATTRIBUTE_AND_CONSTANT_ON_EXPRESSION);
        return queryException;
    }

    public static QueryException unnamedQueryOnSessionBroker(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, UNNAMED_QUERY_ON_SESSION_BROKER, args), query);
        queryException.setErrorCode(UNNAMED_QUERY_ON_SESSION_BROKER);
        return queryException;
    }

    public static QueryException unsupportedMappingForObjectComparison(DatabaseMapping mapping, Expression expression) {
        Object[] args = { mapping, expression, CR };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, UNSUPPORTED_MAPPING_FOR_OBJECT_COMPARISON, args));
        queryException.setErrorCode(UNSUPPORTED_MAPPING_FOR_OBJECT_COMPARISON);
        return queryException;
    }

    public static QueryException updateStatementsNotSpecified() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, UPDATE_STATEMENTS_NOT_SPECIFIED, args));
        queryException.setErrorCode(UPDATE_STATEMENTS_NOT_SPECIFIED);
        return queryException;
    }

    public static QueryException inheritanceWithMultipleTablesNotSupported() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, INHERITANCE_WITH_MULTIPLE_TABLES_NOT_SUPPORTED, args));
        queryException.setErrorCode(INHERITANCE_WITH_MULTIPLE_TABLES_NOT_SUPPORTED);
        return queryException;
    }

    public static QueryException cloneMethodRequired() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CLONE_METHOD_REQUIRED, args));
        queryException.setErrorCode(CLONE_METHOD_REQUIRED);
        return queryException;
    }

    public static QueryException cloneMethodInaccessible() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CLONE_METHOD_INACCESSIBLE, args));
        queryException.setErrorCode(CLONE_METHOD_INACCESSIBLE);
        return queryException;
    }

    public static QueryException cloneMethodThrowException(Throwable exception) {
        Object[] args = { exception };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CLONE_METHOD_THORW_EXCEPTION, args));
        queryException.setErrorCode(CLONE_METHOD_THORW_EXCEPTION);
        return queryException;
    }

    public static QueryException unexpectedInvocation(String message) {
        Object[] args = { message };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, UNEXPECTED_INVOCATION, args));
        queryException.setErrorCode(UNEXPECTED_INVOCATION);
        return queryException;
    }

    public static QueryException multipleRowsDetectedFromReadObjectQuery() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, MULTIPLE_ROWS_DETECTED_FROM_SINGLE_OBJECT_READ, args));
        queryException.setErrorCode(MULTIPLE_ROWS_DETECTED_FROM_SINGLE_OBJECT_READ);
        return queryException;
    }

    // The following exceptions have been added for flashback...
    public static QueryException historicalQueriesMustPreserveGlobalCache() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, HISTORICAL_QUERIES_MUST_PRESERVE_GLOBAL_CACHE, args));
        queryException.setErrorCode(HISTORICAL_QUERIES_MUST_PRESERVE_GLOBAL_CACHE);
        return queryException;
    }

    public static QueryException historicalQueriesOnlySupportedOnOracle() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, HISTORICAL_QUERIES_ONLY_SUPPORTED_ON_ORACLE, args));
        queryException.setErrorCode(HISTORICAL_QUERIES_ONLY_SUPPORTED_ON_ORACLE);
        return queryException;
    }

    public static QueryException mustUseCursorStreamPolicy() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, MUST_USE_CURSOR_STREAM_POLICY, args));
        queryException.setErrorCode(MUST_USE_CURSOR_STREAM_POLICY);
        return queryException;
    }

    public static QueryException fetchGroupNotDefinedInDescriptor(String fetchGroupName) {
        Object[] args = { fetchGroupName };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, QUERY_FETCHGROUP_NOT_DEFINED_IN_DESCRIPTOR, args));
        queryException.setErrorCode(QUERY_FETCHGROUP_NOT_DEFINED_IN_DESCRIPTOR);
        return queryException;
    }

    public static QueryException cannotConformUnfetchedAttribute(String attrbuteName) {
        Object[] args = { attrbuteName };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, CANNOT_CONFORM_UNFETCHED_ATTRIBUTE, args));
        queryException.setErrorCode(CANNOT_CONFORM_UNFETCHED_ATTRIBUTE);
        return queryException;
    }

    public static QueryException fetchGroupAttributeNotMapped(String attrbuteName) {
        Object[] args = { attrbuteName };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, FETCH_GROUP_ATTRIBUTE_NOT_MAPPED, args));
        queryException.setErrorCode(FETCH_GROUP_ATTRIBUTE_NOT_MAPPED);
        return queryException;
    }

    public static QueryException fetchGroupNotSupportOnReportQuery() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, FETCH_GROUP_NOT_SUPPORT_ON_REPORT_QUERY, args));
        queryException.setErrorCode(FETCH_GROUP_NOT_SUPPORT_ON_REPORT_QUERY);
        return queryException;
    }

    public static QueryException fetchGroupNotSupportOnPartialAttributeReading() {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, FETCH_GROUP_NOT_SUPPORT_ON_PARTIAL_ATTRIBUTE_READING, args));
        queryException.setErrorCode(FETCH_GROUP_NOT_SUPPORT_ON_PARTIAL_ATTRIBUTE_READING);
        return queryException;
    }

    public static QueryException fetchGroupValidOnlyIfFetchGroupManagerInDescriptor(String descriptorName, String queryName) {
        Object[] args = { descriptorName, queryName };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, FETCHGROUP_VALID_ONLY_IF_FETCHGROUP_MANAGER_IN_DESCRIPTOR, args));
        queryException.setErrorCode(FETCHGROUP_VALID_ONLY_IF_FETCHGROUP_MANAGER_IN_DESCRIPTOR);
        return queryException;
    }

    public static QueryException reflectiveCallOnTopLinkClassFailed(String className, Exception e) {
        Object[] args = { className };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, REFLECTIVE_CALL_ON_TOPLINK_CLASS_FAILED, args));
        queryException.setErrorCode(REFLECTIVE_CALL_ON_TOPLINK_CLASS_FAILED);
        queryException.setInternalException(e);
        return queryException;
    }

    public static QueryException refreshNotPossibleWithCheckCacheOnly(DatabaseQuery query) {
        Object[] args = {  };

        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, REFRESH_NOT_POSSIBLE_WITH_CHECK_CACHE_ONLY, args), query);
        queryException.setErrorCode(REFRESH_NOT_POSSIBLE_WITH_CHECK_CACHE_ONLY);
        return queryException;
    }

    public static QueryException deleteAllQuerySpecifiesObjectsButNotSelectionCriteria(ClassDescriptor descriptor, DatabaseQuery query, String objects) {
        Object[] args = { descriptor.toString(), CR, objects };
  
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, DELETE_ALL_QUERY_SPECIFIES_OBJECTS_BUT_NOT_SELECTION_CRITERIA, args), query);
        queryException.setErrorCode(DELETE_ALL_QUERY_SPECIFIES_OBJECTS_BUT_NOT_SELECTION_CRITERIA);
        return queryException;
    }

    public static QueryException updateAllQueryAddUpdateFieldIsNull(DatabaseQuery query) {
        Object[] args = {  };
    
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, UPDATE_ALL_QUERY_ADD_UPDATE_FIELD_IS_NULL, args), query);
        queryException.setErrorCode(UPDATE_ALL_QUERY_ADD_UPDATE_FIELD_IS_NULL);
        return queryException;
    }

    public static QueryException updateAllQueryAddUpdateDoesNotDefineField(ClassDescriptor descriptor, DatabaseQuery query, String attributeNameOrExpression) {
        Object[] args = { descriptor.toString(), CR, attributeNameOrExpression };
    
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, UPDATE_ALL_QUERY_ADD_UPDATE_DOES_NOT_DEFINE_FIELD, args), query);
        queryException.setErrorCode(UPDATE_ALL_QUERY_ADD_UPDATE_DOES_NOT_DEFINE_FIELD);
        return queryException;
    }

    public static QueryException updateAllQueryAddUpdateDefinesWrongField(ClassDescriptor descriptor, DatabaseQuery query, String attributeNameOrExpression, String wrongField) {
        Object[] args = { descriptor.toString(), CR, attributeNameOrExpression, wrongField };
    
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, UPDATE_ALL_QUERY_ADD_UPDATE_DEFINES_WRONG_FIELD, args), query);
        queryException.setErrorCode(UPDATE_ALL_QUERY_ADD_UPDATE_DEFINES_WRONG_FIELD);
        return queryException;
    }

    public static QueryException tempTablesNotSupported(DatabaseQuery query, String platformClassName) {
        Object[] args = { platformClassName };
    
        QueryException queryException = new QueryException(ExceptionMessageGenerator.buildMessage(QueryException.class, TEMP_TABLES_NOT_SUPPORTED, args), query);
        queryException.setErrorCode(TEMP_TABLES_NOT_SUPPORTED);
        return queryException;
    }

    public static QueryException distinctCountOnOuterJoinedCompositePK(
        ClassDescriptor descr, DatabaseQuery query) {
        Object[] args = { descr.getJavaClass().getName(), descr.toString() };
        
        QueryException queryException = new QueryException(
            ExceptionMessageGenerator.buildMessage(
                QueryException.class, DISTINCT_COUNT_ON_OUTER_JOINED_COMPOSITE_PK, args), 
            query);
        queryException.setErrorCode(DISTINCT_COUNT_ON_OUTER_JOINED_COMPOSITE_PK);
        return queryException;
    }
}
