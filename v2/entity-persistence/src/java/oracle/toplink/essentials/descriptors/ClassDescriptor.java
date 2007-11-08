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
package oracle.toplink.essentials.descriptors;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.internal.expressions.SQLSelectStatement;
import oracle.toplink.essentials.internal.expressions.SQLStatement;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.identitymaps.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.essentials.queryframework.FetchGroup;
import oracle.toplink.essentials.querykeys.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.descriptors.copying.*;
import oracle.toplink.essentials.descriptors.changetracking.*;
import oracle.toplink.essentials.descriptors.invalidation.*;
import oracle.toplink.essentials.descriptors.FetchGroupManager;
import oracle.toplink.essentials.descriptors.InheritancePolicy;
import oracle.toplink.essentials.descriptors.DescriptorEvent;
import oracle.toplink.essentials.descriptors.DescriptorEventManager;
import oracle.toplink.essentials.descriptors.DescriptorQueryManager;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;
import oracle.toplink.essentials.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.helper.MappingCompare;
import oracle.toplink.essentials.internal.helper.DatabaseTable;
import oracle.toplink.essentials.internal.helper.DatabaseField;

/**
 * <p><b>Purpose</b>:
 * Abstract descriptor class for defining persistence information on a class.
 * This class provides the data independent behavior and is subclassed,
 * for relational, object-relational, EIS, XML, etc.
 *
 * @see RelationalDescriptor
 * @see oracle.toplink.essentials.objectrelational.ObjectRelationalDescriptor
 * @see oracle.toplink.essentials.eis.EISDescriptor
 * @see oracle.toplink.essentials.ox.XMLDescriptor
 */
public class ClassDescriptor implements Cloneable, Serializable {
    protected Class javaClass;
    protected String javaClassName;
    protected Vector<DatabaseTable> tables;
    protected transient DatabaseTable defaultTable;
    protected List<DatabaseField> primaryKeyFields;
    protected transient Map<DatabaseTable, Map<DatabaseField, DatabaseField>> additionalTablePrimaryKeyFields;
    protected transient Vector<DatabaseTable> multipleTableInsertOrder;
    protected transient Map<DatabaseTable, DatabaseTable> multipleTableForeignKeys;
    protected transient Vector<DatabaseField> fields;
    protected transient Vector<DatabaseField> allFields;
    protected Vector<DatabaseMapping> mappings;

    //used by the lock on clone process.  This will contain the foreign reference
    //mapping without indirection
    protected List<DatabaseMapping> lockableMappings;
    protected Map<String, QueryKey> queryKeys;

    // Additional properties.
    protected Class identityMapClass;
    protected int identityMapSize;
    protected String sequenceNumberName;
    protected DatabaseField sequenceNumberField;
    protected transient String sessionName;
    protected boolean shouldAlwaysRefreshCache;
    protected boolean shouldOnlyRefreshCacheIfNewerVersion;
    protected boolean shouldDisableCacheHits;
    protected transient Vector constraintDependencies;
    protected transient String amendmentMethodName;
    protected transient Class amendmentClass;
    protected transient String amendmentClassName;
    protected String alias;
    protected boolean shouldBeReadOnly;
    protected boolean shouldAlwaysConformResultsInUnitOfWork;

    // this attribute is used to determine what classes should be isolated from the shared cache
    protected boolean isIsolated;

    // for bug 2612601 allow ability not to register results in UOW.
    protected boolean shouldRegisterResultsInUnitOfWork = true;

    // Delegation objects, these perform most of the behavoir.
    protected DescriptorEventManager eventManager;
    protected DescriptorQueryManager queryManager;
    protected ObjectBuilder objectBuilder;
    protected CopyPolicy copyPolicy;
    protected InstantiationPolicy instantiationPolicy;
    protected InheritancePolicy inheritancePolicy;
    protected OptimisticLockingPolicy optimisticLockingPolicy;
    protected Vector cascadeLockingPolicies;
    protected WrapperPolicy wrapperPolicy;
    protected ObjectChangePolicy changePolicy;
    protected CMPPolicy cmpPolicy;

    //manage fetch group behaviors and operations
    protected FetchGroupManager fetchGroupManager;

    /** Additional properties may be added. */
    protected Map properties;

    // Follwing are the states the descriptor passes thru during the initialization.
    protected static final int UNINITIALIZED = 0;
    protected static final int PREINITIALIZED = 1;
    protected static final int INITIALIZED = 2;
    protected static final int POST_INITIALIZED = 3;
    protected static final int ERROR = -1;

    //redefine the descriptor types (exclusively)
    protected static final int NORMAL = 0;
    protected static final int AGGREGATE = 2;
    protected static final int AGGREGATE_COLLECTION = 3;
    protected transient int initializationStage;
    protected int descriptorType;
    protected boolean shouldOrderMappings;
    protected CacheInvalidationPolicy cacheInvalidationPolicy = null;

    /** PERF: Used to optimize cache locking to only acquire deferred locks when required (no-indirection). */
    protected boolean shouldAcquireCascadedLocks = false;

    /** PERF: Compute and store if the primary key is simple (direct-mapped) to allow fast extraction. */
    protected boolean hasSimplePrimaryKey = false;

    /**
     * PUBLIC:
     * Return a new descriptor.
     */
    public ClassDescriptor() {
        // Properties
        this.tables = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        this.mappings = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        this.primaryKeyFields = new ArrayList(2);
        this.fields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        this.allFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        this.constraintDependencies = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        this.multipleTableForeignKeys = new HashMap(5);
        this.queryKeys = new HashMap(5);
        this.initializationStage = UNINITIALIZED;
        this.shouldAlwaysRefreshCache = false;
        this.shouldOnlyRefreshCacheIfNewerVersion = false;
        this.shouldDisableCacheHits = false;
        this.identityMapSize = 100;
        this.identityMapClass = IdentityMap.getDefaultIdentityMapClass();
        this.descriptorType = NORMAL;
        this.shouldOrderMappings = true;
        this.shouldBeReadOnly = false;
        this.shouldAlwaysConformResultsInUnitOfWork = false;
        this.shouldAcquireCascadedLocks = false;
        this.hasSimplePrimaryKey = false;
        this.isIsolated = false;

        // Policies
        this.objectBuilder = new ObjectBuilder(this);
        setCopyPolicy(new InstantiationCopyPolicy());
        setInstantiationPolicy(new InstantiationPolicy());
        setEventManager(new oracle.toplink.essentials.descriptors.DescriptorEventManager());
        setQueryManager(new oracle.toplink.essentials.descriptors.DescriptorQueryManager());

        changePolicy = new DeferredChangeDetectionPolicy();
        this.cascadeLockingPolicies = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
    }

    /**
     * PUBLIC:
     * This method should only be used for interface descriptors.  It
     * adds an abstract query key to the interface descriptor.  Any
     * implementors of that interface must define the query key
     * defined by this abstract query key.
     */
    public void addAbstractQueryKey(String queryKeyName) {
        QueryKey queryKey = new QueryKey();
        queryKey.setName(queryKeyName);
        addQueryKey(queryKey);
    }
     
    /**
     * ADVANCED:
     * TopLink automatically orders database access through the foreign key information provided in 1:1 and 1:m mappings.
     * In some case when 1:1 are not defined it may be required to tell the descriptor about a constraint,
     * this defines that this descriptor has a foreign key constraint to another class and must be inserted after
     * instances of the other class.
     */
    public void addConstraintDependencies(Class dependencies) {
        getConstraintDependencies().addElement(dependencies);
    }

    /**
     * PUBLIC:
     * Add a direct mapping to the receiver. The new mapping specifies that
     * an instance variable of the class of objects which the receiver describes maps in
     * the default manner for its type to the indicated database field.
     *
     * @param String instanceVariableName is the name of an instance variable of the
     * class which the receiver describes.
     * @param String fieldName is the name of the database column which corresponds
     * with the designated instance variable.
     * @return The newly created DatabaseMapping is returned.
     */
    public DatabaseMapping addDirectMapping(String attributeName, String fieldName) {
        DirectToFieldMapping mapping = new DirectToFieldMapping();

        mapping.setAttributeName(attributeName);
        mapping.setFieldName(fieldName);

        return addMapping(mapping);
    }

    /**
     * PUBLIC:
     * Add a direct mapping to the receiver. The new mapping specifies that
     * a variable accessed by the get and set methods of the class of objects which
     * the receiver describes maps in  the default manner for its type to the indicated
     * database field.
     */
    public DatabaseMapping addDirectMapping(String attributeName, String getMethodName, String setMethodName, String fieldName) {
        DirectToFieldMapping mapping = new DirectToFieldMapping();

        mapping.setAttributeName(attributeName);
        mapping.setSetMethodName(setMethodName);
        mapping.setGetMethodName(getMethodName);
        mapping.setFieldName(fieldName);

        return addMapping(mapping);
    }

    /**
     * PUBLIC:
     * Add a query key to the descriptor. Query keys define Java aliases to database fields.
     */
    public void addDirectQueryKey(String queryKeyName, String fieldName) {
        DirectQueryKey queryKey = new DirectQueryKey();
        DatabaseField field = new DatabaseField(fieldName);

        queryKey.setName(queryKeyName);
        queryKey.setField(field);
        getQueryKeys().put(queryKeyName, queryKey);
    }

    /**
     * PUBLIC:
     * Add a database mapping to the receiver. Perform any required
     * initialization of both the mapping and the receiving descriptor
     * as a result of adding the new mapping.
     */
    public DatabaseMapping addMapping(DatabaseMapping mapping) {
        // For CR#2646, if the mapping already points to the parent descriptor then leave it.
        if (mapping.getDescriptor() == null) {
            mapping.setDescriptor(this);
        }
        getMappings().addElement(mapping);
        return mapping;
    }

    protected void validateMappingType(DatabaseMapping mapping) {
        if (!(mapping.isRelationalMapping())) {
            throw DescriptorException.invalidMappingType(mapping);
        }
    }

    /**
     * PUBLIC:
     * This protocol can be used to associate multiple tables with foreign key 
     * information. The join criteria will be generated based on the fields 
     * provided. By default TopLink associates multiple tables using a primary 
     * key join where the primary keys fields are named the same.
     */
    public void addMultipleTableForeignKeyField(DatabaseField sourceField, DatabaseField targetField) throws DescriptorException {  
        addMultipleTableForeignKeys(sourceField, targetField, true);  
    }
    
    /**
     * PUBLIC:
     * This protocol can be used to associate multiple tables with foreign key 
     * information. The join criteria will be generated based on the fields 
     * provided. By default TopLink associates multiple tables using a primary 
     * key join where the primary keys fields are named the same.
     */
    public void addMultipleTableForeignKeyFieldName(String sourceFieldName, String targetFieldName) throws DescriptorException {  
        addMultipleTableForeignKeyField(new DatabaseField(sourceFieldName), new DatabaseField(targetFieldName));  
    }
    
    /**
     * INTERNAL:
     * Add the multiple fields, if is a foreign key then add the tables to the 
     * foreign keys ordering.
     */
    protected void addMultipleTableForeignKeys(DatabaseField sourceField, DatabaseField targetField, boolean isForeignKey) throws DescriptorException {        
        // Make sure that the table is fully qualified.
        if ((!sourceField.hasTableName()) || (!targetField.hasTableName())) {
            throw DescriptorException.multipleTablePrimaryKeyMustBeFullyQualified(this);
        }
        
        DatabaseTable sourceTable = sourceField.getTable();
        DatabaseTable targetTable = targetField.getTable();
        setAdditionalTablePrimaryKeyFields(targetTable, sourceField, targetField);
        
        if (isForeignKey) {
            getMultipleTableForeignKeys().put(sourceTable, targetTable);
        } 
    }
    
    /**
     * INTERNAL:
     * Add the multiple table fields, if is a foreign key then add the tables to the 
     * foreign keys ordering.
     */
    protected void addMultipleTableForeignKeys(String fieldNameInSourceTable, String fieldNameInTargetTable, boolean isForeignKey) throws DescriptorException {        
        addMultipleTableForeignKeys(new DatabaseField(fieldNameInSourceTable), new DatabaseField(fieldNameInTargetTable), isForeignKey);
    }

    /**
     * PUBLIC:
     * This protocol can be used to map the primary key fields in a multiple 
     * table descriptor. By default TopLink assumes that all of the primary key 
     * fields are named the same.
     */
    public void addMultipleTablePrimaryKeyField(DatabaseField sourceField, DatabaseField targetField) throws DescriptorException {
        addMultipleTableForeignKeys(sourceField, targetField, false);
    }
    
    /**
     * PUBLIC:
     * This protocol can be used to map the primary key field names in a 
     * multiple table descriptor. By default TopLink assumes that all of the 
     * primary key fields are named the same.
     */
    public void addMultipleTablePrimaryKeyFieldName(String sourceFieldName, String targetFieldName) throws DescriptorException {
        addMultipleTablePrimaryKeyField(new DatabaseField(sourceFieldName), new DatabaseField(targetFieldName));
    }

    /**
     * PUBLIC:
     * Specify the primary key field of the descriptors table.
     * This should be called for each field that makes up the primary key of the table.
     * If the descriptor has many tables, this must be the primary key in the first table,
     * if the other tables have the same primary key nothing else is required, otherwise
     * a primary key/foreign key field mapping must be provided for each of the other tables.
     * @see #addMultipleTableForeignKeyFieldName(String, String);
     */
    public void addPrimaryKeyFieldName(String fieldName) {
        getPrimaryKeyFields().add(new DatabaseField(fieldName));
    }

    /**
     * ADVANCED:
     * Specify the primary key field of the descriptors table.
     * This should be called for each field that makes up the primary key of the table.
     * This can be used for advanced field types, such as XML nodes, or to set the field type.
     */
    public void addPrimaryKeyField(DatabaseField field) {
        getPrimaryKeyFields().add(field);
    }

    /**
     * PUBLIC:
     * Add a query key to the descriptor. Query keys define Java aliases to database fields.
     */
    public void addQueryKey(QueryKey queryKey) {
        getQueryKeys().put(queryKey.getName(), queryKey);
    }

    /**
     * PUBLIC:
     * Specify the table for the class of objects the receiver describes.
     * This method is used if there is more than one table.
     */
    public void addTable(DatabaseTable table) {
        getTables().addElement(table);
    }
    
    /**
     * PUBLIC:
     * Specify the table name for the class of objects the receiver describes.
     * If the table has a qualifier it should be specified using the dot notation,
     * (i.e. "userid.employee"). This method is used if there is more than one table.
     */
    public void addTableName(String tableName) {
        addTable(new DatabaseTable(tableName));
    }

    /**
     * INTERNAL:
     * Adjust the order of the tables in the multipleTableInsertOrder Vector according to the FK
     * relationship if one (or more) were previously specified. I.e. target of FK relationship should be inserted
     * before source.
     * If the multipleTableInsertOrder has been specified (presumably by the user) then do not change it.
     */
    public void adjustMultipleTableInsertOrder() {
        // Check if a user defined insert order was given.
        if ((getMultipleTableInsertOrder() == null) || getMultipleTableInsertOrder().isEmpty()) {
            setMultipleTableInsertOrder((Vector) getTables().clone());
            
            checkMultipleTableForeignKeys(false);
        } else {
            if (getMultipleTableInsertOrder().size() != getTables().size()) {
                throw DescriptorException.multipleTableInsertOrderMismatch(this);
            }
        
            checkMultipleTableForeignKeys(true);    
        }
    }
    
    /**
     * PUBLIC:
     * Used to set the descriptor to always conform in any unit of work query.
     *
     */
    public void alwaysConformResultsInUnitOfWork() {
        setShouldAlwaysConformResultsInUnitOfWork(true);
    }

    /**
     * PUBLIC:
     * This method is the equivalent of calling {@link #setShouldAlwaysRefreshCache} with an argument of <CODE>true</CODE>:
     * it configures a <CODE>Descriptor</CODE> to always refresh the cache if data is received from the database by any query.<P>
     *
     * However, if a query hits the cache, data is not refreshed regardless of how this setting is configured. For example, by
     * default, when a query for a single object based on its primary key is executed, OracleAS TopLink will first look in the
     * cache for the object. If the object is in the cache, the cached object is returned and data is not refreshed. To avoid
     * cache hits, use the {@link #disableCacheHits} method.<P>
     *
     * Also note that the {@link oracle.toplink.essentials.sessions.UnitOfWork} will not refresh its registered objects.<P>
     *
     * Use this property with caution because it can lead to poor performance and may refresh on queries when it is not desired. Normally,
     * if you require fresh data, it is better to configure a query with {@link oracle.toplink.essentials.queryframework.ObjectLevelReadQuery#refreshIdentityMapResult}.
     * To ensure that refreshes are only done when required, use this method in conjunction with {@link #onlyRefreshCacheIfNewerVersion}.
     *
     * @see #dontAlwaysRefreshCache
     */
    public void alwaysRefreshCache() {
        setShouldAlwaysRefreshCache(true);
    }

    /**
     * ADVANCED:
     * Call the descriptor amendment method.
     * This is called while loading or creating a descriptor that has an amendment method defined.
     */
    public void applyAmendmentMethod() {
        applyAmendmentMethod(null);
    }

    /**
     * INTERNAL:
     * Call the descriptor amendment method.
     * This is called while loading or creating a descriptor that has an amendment method defined.
     */
    public void applyAmendmentMethod(DescriptorEvent event) {
        if ((getAmendmentClass() == null) || (getAmendmentMethodName() == null)) {
            return;
        }

        Method method = null;
        Class[] argTypes = new Class[1];

        // BUG#2669585
        // Class argument type must be consistent, descriptor, i.e. instance may be a subclass.
        argTypes[0] = ClassDescriptor.class;
        try {
            method = Helper.getDeclaredMethod(getAmendmentClass(), getAmendmentMethodName(), argTypes);
        } catch (Exception ignore) {
            // Return type should now be ClassDescriptor.
            argTypes[0] = ClassDescriptor.class;
            try {
                method = Helper.getDeclaredMethod(getAmendmentClass(), getAmendmentMethodName(), argTypes);
            } catch (Exception exception) {
                throw DescriptorException.invalidAmendmentMethod(getAmendmentClass(), getAmendmentMethodName(), exception, this);
            }
        }

        Object[] args = new Object[1];
        args[0] = this;

        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    AccessController.doPrivileged(new PrivilegedMethodInvoker(method, null, args));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof IllegalAccessException) {
                        throw (IllegalAccessException)throwableException;
                    } else {
                        throw (InvocationTargetException)throwableException;
                    }
                }
            } else {
                PrivilegedAccessHelper.invokeMethod(method, null, args);
            }
        } catch (Exception exception) {
            throw DescriptorException.errorOccuredInAmendmentMethod(getAmendmentClass(), getAmendmentMethodName(), exception, this);
        }
    }

    /**
     * INTERNAL:
     * Used to determine if a foreign key references the primary key.
     */
    public boolean arePrimaryKeyFields(Vector fields) {
        if (!(fields.size() == (getPrimaryKeyFields().size()))) {
            return false;
        }

        for (Enumeration enumFields = fields.elements(); enumFields.hasMoreElements();) {
            DatabaseField field = (DatabaseField)enumFields.nextElement();

            if (!getPrimaryKeyFields().contains(field)) {
                return false;
            }
        }

        return true;
    }

    /**
     * INTERNAL:
     * Return a call built from a statement. Subclasses may throw an exception
     * if the statement is not appropriate.
     */
    public DatabaseCall buildCallFromStatement(SQLStatement statement, AbstractSession session) {
        return statement.buildCall(session);
    }

    /**
     * INTERNAL:
     * Extract the direct values from the specified field value.
     * Return them in a vector.
     */
    public Vector buildDirectValuesFromFieldValue(Object fieldValue) throws DatabaseException {
        throw DescriptorException.normalDescriptorsDoNotSupportNonRelationalExtensions(this);
    }

    /**
     * INTERNAL:
     * A DatabaseField is built from the given field name.
     */

    // * added 9/7/00 by Les Davis
    // * bug fix for null pointer in initialization of mappings in remote session
    public DatabaseField buildField(String fieldName) {
        DatabaseField field = new DatabaseField(fieldName);
        DatabaseTable table;

        if (field.hasTableName()) {
            table = getTable(field.getTableName());
        } else if (getDefaultTable() != null) {
            table = getDefaultTable();
        } else {
            table = getTable(getTableName());
        }

        field.setTable(table);
        return field;
    }

    /**
     * INTERNAL:
     * The table of the field is ensured to be unique from the descriptor's tables.
     * If the field has no table the default table is assigned.
     * This is used only in initialization.
     */
    public void buildField(DatabaseField field) {
        DatabaseTable table;
        if (field.hasTableName()) {
            table = getTable(field.getTableName());
        } else {
            table = getDefaultTable();
        }

        field.setTable(table);
    }

    /**
     * INTERNAL:
     * Build the appropriate field value for the specified
     * set of direct values.
     */
    public Object buildFieldValueFromDirectValues(Vector directValues, String elementDataTypeName, AbstractSession session) throws DatabaseException {
        throw DescriptorException.normalDescriptorsDoNotSupportNonRelationalExtensions(this);
    }

    /**
     * INTERNAL:
     * Build and return the appropriate field value for the specified
     * set of foreign keys (i.e. each row has the fields that
     * make up a foreign key).
     */
    public Object buildFieldValueFromForeignKeys(Vector foreignKeys, String referenceDataTypeName, AbstractSession session) throws DatabaseException {
        throw DescriptorException.normalDescriptorsDoNotSupportNonRelationalExtensions(this);
    }

    /**
     * INTERNAL:
     * Build and return the field value from the specified nested database row.
     */
    public Object buildFieldValueFromNestedRow(AbstractRecord nestedRow, AbstractSession session) throws DatabaseException {
        throw DescriptorException.normalDescriptorsDoNotSupportNonRelationalExtensions(this);
    }

    /**
     * INTERNAL:
     * Build and return the appropriate field value for the specified
     * set of nested rows.
     */
    public Object buildFieldValueFromNestedRows(Vector nestedRows, String structureName, AbstractSession session) throws DatabaseException {
        throw DescriptorException.normalDescriptorsDoNotSupportNonRelationalExtensions(this);
    }

    /**
     * INTERNAL:
     * Build and return the nested database row from the specified field value.
     */
    public AbstractRecord buildNestedRowFromFieldValue(Object fieldValue) throws DatabaseException {
        throw DescriptorException.normalDescriptorsDoNotSupportNonRelationalExtensions(this);
    }

    /**
     * INTERNAL:
     * Build and return the nested rows from the specified field value.
     */
    public Vector buildNestedRowsFromFieldValue(Object fieldValue, AbstractSession session) throws DatabaseException {
        throw DescriptorException.normalDescriptorsDoNotSupportNonRelationalExtensions(this);
    }

    /**
     *  To check that tables and fields are present in database
     */
    protected void checkDatabase(AbstractSession session) {
        if (session.getIntegrityChecker().shouldCheckDatabase()) {
            for (Enumeration enumTable = getTables().elements(); enumTable.hasMoreElements();) {
                DatabaseTable table = (DatabaseTable)enumTable.nextElement();
                if (session.getIntegrityChecker().checkTable(table, session)) {
                    // To load the fields of database into a vector
                    Vector databaseFields = new Vector();
                    Vector result = session.getAccessor().getColumnInfo(null, null, table.getName(), null, session);
                    for (Enumeration resultEnum = result.elements(); resultEnum.hasMoreElements();) {
                        AbstractRecord row = (AbstractRecord)resultEnum.nextElement();
                        databaseFields.addElement(row.get("COLUMN_NAME"));
                    }

                    // To check that the fields of descriptor are present in the database.
                    for (Enumeration row = getFields().elements(); row.hasMoreElements();) {
                        DatabaseField field = (DatabaseField)row.nextElement();
                        if (field.getTable().equals(table) && (!databaseFields.contains(field.getName()))) {
                            session.getIntegrityChecker().handleError(DescriptorException.fieldIsNotPresentInDatabase(this, table.getName(), field.getName()));
                        }
                    }
                } else {
                    session.getIntegrityChecker().handleError(DescriptorException.tableIsNotPresentInDatabase(this));
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Verify that an aggregate descriptor's inheritance tree
     * is full of aggregate descriptors.
     */
    public void checkInheritanceTreeAggregateSettings(AbstractSession session, AggregateMapping mapping) throws DescriptorException {
        if (!this.hasInheritance()) {
            return;
        }

        if (this.isChildDescriptor()) {
            Class parentClass = this.getInheritancePolicy().getParentClass();
            if (parentClass == this.getJavaClass()) {
                throw DescriptorException.parentClassIsSelf(this);
            }

            // recurse up the inheritance tree to the root descriptor
            session.getDescriptor(parentClass).checkInheritanceTreeAggregateSettings(session, mapping);
        } else {
            // we have a root descriptor, now verify it and all its children, grandchildren, etc.
            this.checkInheritanceTreeAggregateSettingsForChildren(session, mapping);
        }
    }

    /**
     * Verify that an aggregate descriptor's inheritance tree
     * is full of aggregate descriptors, cont.
     */
    private void checkInheritanceTreeAggregateSettingsForChildren(AbstractSession session, AggregateMapping mapping) throws DescriptorException {
        if (!this.isAggregateDescriptor()) {
            session.getIntegrityChecker().handleError(DescriptorException.referenceDescriptorIsNotAggregate(this.getJavaClass().getName(), mapping));
        }
        for (Enumeration stream = this.getInheritancePolicy().getChildDescriptors().elements();
                 stream.hasMoreElements();) {
            ClassDescriptor childDescriptor = (ClassDescriptor)stream.nextElement();

            // recurse down the inheritance tree to its leaves
            childDescriptor.checkInheritanceTreeAggregateSettingsForChildren(session, mapping);
        }
    }
    
    /**
     * INTERNAL:
     */
    protected void checkMultipleTableForeignKeys(boolean userSpecifiedOrder) {
        // Loop through n times to be sure that the insert order is correct. 
        // Loop n times eliminates our dependence on the order of the foreighKey 
        // relationships specified. We could do this adjustment in one pass but 
        // we would have to put the foreignKeyTables in the same order as the 
        // tables being sorted.
        Map foreignKeyTableRelationships = getMultipleTableForeignKeys();
        
        for (int i = 0; i < foreignKeyTableRelationships.size(); i++) {
            for (Iterator sourceTables = foreignKeyTableRelationships.entrySet().iterator(); sourceTables.hasNext();) {
                Map.Entry entry = (Map.Entry)sourceTables.next();
                DatabaseTable sourceTable = (DatabaseTable) entry.getKey();
                DatabaseTable targetTable = (DatabaseTable) entry.getValue();
                
                // Verify the source table is a valid.
                if (getMultipleTableInsertOrder().indexOf(sourceTable) == -1) {
                    throw DescriptorException.illegalTableNameInMultipleTableForeignKeyField(this, sourceTable);
                }                       
                
                // Verify the target table is a valid.
                if (getMultipleTableInsertOrder().indexOf(targetTable) == -1) {
                    throw DescriptorException.illegalTableNameInMultipleTableForeignKeyField(this, targetTable);
                }
                
                int sourceTableIndex = getTables().indexOf(sourceTable);
                int targetTableIndex = getTables().indexOf(targetTable);
                
                // The sourceTable must be before the targetTable to avoid 
                // foreign key constraint violations when inserting into the 
                // database.
                if (targetTableIndex < sourceTableIndex) {
                    if (userSpecifiedOrder) {
                        toggleAdditionalTablePrimaryKeyFields(targetTable, sourceTable);    
                    } else {
                        int sti = getMultipleTableInsertOrder().indexOf(sourceTable);
                        int tti = getMultipleTableInsertOrder().indexOf(targetTable);
                        
                        if (tti < sti) {
                            toggleAdditionalTablePrimaryKeyFields(targetTable, sourceTable);
                            getMultipleTableInsertOrder().removeElementAt(tti);
                            getMultipleTableInsertOrder().insertElementAt(targetTable, sti);
                        }
                    }
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Clones the descriptor
     */
    public Object clone() {
        ClassDescriptor clonedDescriptor = null;

        // clones itself
        try {
            clonedDescriptor = (ClassDescriptor)super.clone();
        } catch (Exception exception) {
            ;
        }

        Vector mappingsVector = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();

        // All the mappings
        for (Enumeration mappingsEnum = getMappings().elements(); mappingsEnum.hasMoreElements();) {
            DatabaseMapping mapping;

            mapping = (DatabaseMapping)((DatabaseMapping)mappingsEnum.nextElement()).clone();
            mapping.setDescriptor(clonedDescriptor);
            mappingsVector.addElement(mapping);
        }
        clonedDescriptor.setMappings(mappingsVector);

        Map queryKeyVector = new HashMap(getQueryKeys().size() + 2);

        // All the query keys
        for (Iterator queryKeysEnum = getQueryKeys().values().iterator();queryKeysEnum.hasNext();){
            QueryKey queryKey = (QueryKey)((QueryKey)queryKeysEnum.next()).clone();
            queryKey.setDescriptor(clonedDescriptor);
            queryKeyVector.put(queryKey.getName(), queryKey);
        }
        clonedDescriptor.setQueryKeys(queryKeyVector);

        // PrimaryKeyFields
        List primaryKeyVector = new ArrayList(getPrimaryKeyFields().size());
        List primaryKeyFields = getPrimaryKeyFields();
        for (int index = 0; index < primaryKeyFields.size(); index++) {
            DatabaseField primaryKey = (DatabaseField)((DatabaseField)primaryKeyFields.get(index)).clone();
            primaryKeyVector.add(primaryKey);
        }
        clonedDescriptor.setPrimaryKeyFields(primaryKeyVector);

        // fields.
        clonedDescriptor.setFields(oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance());

        // The inheritance policy
        if (clonedDescriptor.hasInheritance()) {
            clonedDescriptor.setInheritancePolicy((InheritancePolicy)getInheritancePolicy().clone());
            clonedDescriptor.getInheritancePolicy().setDescriptor(clonedDescriptor);
        }

        // The Object builder	
        clonedDescriptor.setObjectBuilder((ObjectBuilder)getObjectBuilder().clone());
        clonedDescriptor.getObjectBuilder().setDescriptor(clonedDescriptor);

        clonedDescriptor.setEventManager((DescriptorEventManager)getEventManager().clone());
        clonedDescriptor.getEventManager().setDescriptor(clonedDescriptor);

        // The Query manager
        clonedDescriptor.setQueryManager((DescriptorQueryManager)getQueryManager().clone());
        clonedDescriptor.getQueryManager().setDescriptor(clonedDescriptor);

        //fetch group
        if (hasFetchGroupManager()) {
            clonedDescriptor.setFetchGroupManager((FetchGroupManager)getFetchGroupManager().clone());
        }

        clonedDescriptor.setIsIsolated(this.isIsolated());

        // Bug 3037701 - clone several more elements
        clonedDescriptor.setInstantiationPolicy((InstantiationPolicy)this.instantiationPolicy.clone());
        clonedDescriptor.setCopyPolicy((CopyPolicy)this.copyPolicy.clone());
        if (getOptimisticLockingPolicy() != null) {
            clonedDescriptor.setOptimisticLockingPolicy((OptimisticLockingPolicy)this.getOptimisticLockingPolicy().clone());
        }

        return (Object)clonedDescriptor;
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this Descriptor to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        Class descriptorClass = null;
        Class amendmentClass = null;
        try{
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    descriptorClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(getJavaClassName(), true, classLoader));
                } catch (PrivilegedActionException exception) {
                    throw ValidationException.classNotFoundWhileConvertingClassNames(getAmendmentClassName(), exception.getException());
                }
            } else {
                descriptorClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(getJavaClassName(), true, classLoader);
            }
            if (getAmendmentClassName() != null){
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        amendmentClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(getAmendmentClassName(), true, classLoader));
                    } catch (PrivilegedActionException exception) {
                        throw ValidationException.classNotFoundWhileConvertingClassNames(getAmendmentClassName(), exception.getException());
                   }
                } else {
                    amendmentClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(getAmendmentClassName(), true, classLoader);
                }            
            }
        } catch (ClassNotFoundException exc){
            throw ValidationException.classNotFoundWhileConvertingClassNames(getAmendmentClassName(), exc);
        }
        setJavaClass(descriptorClass);
        if (amendmentClass != null){
            setAmendmentClass(amendmentClass);
        }
        Iterator mappings = getMappings().iterator();
        while (mappings.hasNext()){
            ((DatabaseMapping)mappings.next()).convertClassNamesToClasses(classLoader);
        }
        if (inheritancePolicy != null){
            inheritancePolicy.convertClassNamesToClasses(classLoader);
        }
        if (instantiationPolicy != null){
            instantiationPolicy.convertClassNamesToClasses(classLoader);
        }
        if (hasCMPPolicy()) {
            getCMPPolicy().convertClassNamesToClasses(classLoader);
        }
        queryManager.convertClassNamesToClasses(classLoader);
    }

    /**
     * PUBLIC:
     * Create a copy policy of the type passed in as a string.
     */
    public void createCopyPolicy(String policyType) {
        if (policyType.equals("clone")) {
            useCloneCopyPolicy();
            return;
        }
        if (policyType.equals("constructor")) {
            useInstantiationCopyPolicy();
            return;
        }
    }

    /**
     * PUBLIC:
     * Create a instantiation policy of the type passed in as a string.
     */
    public void createInstantiationPolicy(String policyType) {
        if (policyType.equals("static method")) {
            //do nothing for now
            return;
        }
        if (policyType.equals("constructor")) {
            useDefaultConstructorInstantiationPolicy();
            return;
        }
        if (policyType.equals("factory")) {
            //do nothing for now
            return;
        }
    }

    /**
     * PUBLIC:
     * Sets the descriptor to be an aggregate.
     * An aggregate descriptor is contained within another descriptor's table.
     * Aggregate descriptors are insert/updated/deleted with their owner and cannot exist without their owner as they share the same row.
     * Aggregates are not cached (they are cached as part of their owner) and cannot be read/written/deleted/registered.
     * All aggregate descriptors must call this.
     */
    public void descriptorIsAggregate() {
        setDescriptorType(AGGREGATE);
    }

    /**
     * PUBLIC:
     * Sets the descriptor to be part of an aggregate collection.
     * An aggregate collection descriptor stored in a seperate table but some of the fields (the primary key) comes from its owner.
     * Aggregate collection descriptors are insert/updated/deleted with their owner and cannot exist without their owner as they share the primary key.
     * Aggregate collections are not cached (they are cached as part of their owner) and cannot be read/written/deleted/registered.
     * All aggregate collection descriptors must call this.
     */
    public void descriptorIsAggregateCollection() {
        setDescriptorType(AGGREGATE_COLLECTION);
    }

    /**
     * PUBLIC:
     * Sets the descriptor to be normal.
     * This is the default and means the descriptor is not aggregate or for an interface.
     */
    public void descriptorIsNormal() {
        setDescriptorType(NORMAL);
    }

    /**
     * PUBLIC:
     * Allow for cache hits on primary key read object queries to be disabled.
     * This can be used with {@link #alwaysRefreshCache} or {@link #alwaysRefreshCacheOnRemote} to ensure queries always go to the database.
     */
    public void disableCacheHits() {
        setShouldDisableCacheHits(true);
    }

    /**
     * PUBLIC:
     * The descriptor is defined to not conform the results in unit of work in read query. Default.
     *
     */
    public void dontAlwaysConformResultsInUnitOfWork() {
        setShouldAlwaysConformResultsInUnitOfWork(false);
    }

    /**
     * PUBLIC:
     * This method is the equivalent of calling {@link #setShouldAlwaysRefreshCache} with an argument of <CODE>false</CODE>:
     * it ensures that a <CODE>Descriptor</CODE> is not configured to always refresh the cache if data is received from the database by any query.
     *
     * @see #alwaysRefreshCache
     */
    public void dontAlwaysRefreshCache() {
        setShouldAlwaysRefreshCache(false);
    }

    /**
     * PUBLIC:
     * Allow for cache hits on primary key read object queries.
     *
     * @see #disableCacheHits()
     */
    public void dontDisableCacheHits() {
        setShouldDisableCacheHits(false);
    }

    /**
     * PUBLIC:
     * This method is the equivalent of calling {@link #setShouldOnlyRefreshCacheIfNewerVersion} with an argument of <CODE>false</CODE>:
     * it ensures that a <CODE>Descriptor</CODE> is not configured to only refresh the cache if the data received from the database by
     * a query is newer than the data in the cache (as determined by the optimistic locking field).
     *
     * @see #onlyRefreshCacheIfNewerVersion
     */
    public void dontOnlyRefreshCacheIfNewerVersion() {
        setShouldOnlyRefreshCacheIfNewerVersion(false);
    }

    /**
     * INTERNAL:
     * The first table in the tables is always treated as default.
     */
    protected DatabaseTable extractDefaultTable() {
        if (getTables().isEmpty()) {
            if (isChildDescriptor()) {
                return getInheritancePolicy().getParentDescriptor().extractDefaultTable();
            } else {
                return null;
            }
        }

        return (DatabaseTable)getTables().firstElement();
    }

    /**
     * INTERNAL:
     * This is used to map the primary key field names in a multiple table descriptor.
     */
    public Map<DatabaseTable, Map<DatabaseField, DatabaseField>> getAdditionalTablePrimaryKeyFields() {
        if (additionalTablePrimaryKeyFields == null) {
            additionalTablePrimaryKeyFields = new HashMap(5);
        }
        return additionalTablePrimaryKeyFields;
    }

    /**
     * PUBLIC:
     * Get the alias
     */
    public String getAlias() {

        /* CR3310: Steven Vo
         *   Default alias to the Java class name if the alias is not set
         */
        if ((alias == null) && (getJavaClassName() != null)) {
            alias = oracle.toplink.essentials.internal.helper.Helper.getShortClassName(getJavaClassName());
        }
        return alias;
    }

    /**
     * INTERNAL:
     * Return all the fields which include all child class fields. 
     * By default it is initialized to the fields for the current descriptor.
     */
    public Vector<DatabaseField> getAllFields() {
        return allFields;
    }

    /**
     * PUBLIC:
     * Return the amendment class.
     * The amendment method will be called on the class before initialization to allow for it to initialize the descriptor.
     * The method must be a public static method on the class.
     */
    public Class getAmendmentClass() {
        return amendmentClass;
    }

    /**
     * INTERNAL:
     * Return amendment class name, used by the MW.
     */
    public String getAmendmentClassName() {
        if ((amendmentClassName == null) && (amendmentClass != null)) {
            amendmentClassName = amendmentClass.getName();
        }
        return amendmentClassName;
    }

    /**
     * PUBLIC:
     * Return the amendment method.
     * This will be called on the amendment class before initialization to allow for it to initialize the descriptor.
     * The method must be a public static method on the class.
     */
    public String getAmendmentMethodName() {
        return amendmentMethodName;
    }

    /**
     * PUBLIC:
     * Return this objects ObjectChangePolicy.
     */
    public ObjectChangePolicy getObjectChangePolicy() {
        // part of fix for 4410581: project xml must save change policy
        // if no change-policy XML element, field is null: lazy-init to default
        if (changePolicy == null) {
            changePolicy = new DeferredChangeDetectionPolicy();
        }
        return changePolicy;
    }

    /**
     * PUBLIC:
     * Return the CacheInvalidationPolicy for this descriptor
     * For uninitialized cache invalidation policies, this will return a NoExpiryCacheInvalidationPolicy
     * @return CacheInvalidationPolicy
     * @see oracle.toplink.essentials.descriptors.invalidation.CacheInvalidationPolicy
     */
    public CacheInvalidationPolicy getCacheInvalidationPolicy() {
        if (cacheInvalidationPolicy == null) {
            cacheInvalidationPolicy = new NoExpiryCacheInvalidationPolicy();
        }
        return cacheInvalidationPolicy;
    }
    
    /**
     * INTERNAL:
     */
     public Vector getCascadeLockingPolicies() {
        return cascadeLockingPolicies;
     }

    /**
     * ADVANCED:
     * TopLink automatically orders database access through the foreign key information provided in 1:1 and 1:m mappings.
     * In some case when 1:1 are not defined it may be required to tell the descriptor about a constraint,
     * this defines that this descriptor has a foreign key constraint to another class and must be inserted after
     * instances of the other class.
     */
    public Vector getConstraintDependencies() {
        return constraintDependencies;
    }

    /**
     * INTERNAL:
     * Returns the copy policy.
     */
    public CopyPolicy getCopyPolicy() {
        return copyPolicy;
    }

    /**
     * INTERNAL:
     * The first table in the tables is always treated as default.
     */
    public DatabaseTable getDefaultTable() {
        return defaultTable;
    }

    /**
     * ADVANCED:
     * return the descriptor type (NORMAL by default, others include INTERFACE, AGGREGATE, AGGREGATE COLLECTION)
     */
    public int getDescriptorType() {
        return descriptorType;
    }

    /**
     * INTERNAL:
     * This method is explicitly used by the XML reader.
     */
    public String getDescriptorTypeValue() {
        if (isAggregateCollectionDescriptor()) {
            return "Aggregate collection";
        } else if (isAggregateDescriptor()) {
            return "Aggregate";
        } else {
            // Default.
            return "Normal";
        }
    }

    /**
     * PUBLIC:
     * Get the event manager for the descriptor.  The event manager is responsible
     * for managing the pre/post selectors.
     */
    public DescriptorEventManager getDescriptorEventManager() {
        return getEventManager();
    }

    /**
     * PUBLIC:
     * Get the event manager for the descriptor.  The event manager is responsible
     * for managing the pre/post selectors.
     */
    public DescriptorEventManager getEventManager() {
        return eventManager;
    }

    /**
     * INTERNAL:
     * Return all the fields
     */
    public Vector<DatabaseField> getFields() {
        return fields;
    }

    /**
     * INTERNAL:
     * Return the class of identity map to be used by this descriptor.
     * The default is the "SoftCacheWeakIdentityMap".
     */
    public Class getIdentityMapClass() {
        return identityMapClass;
    }

    /**
     * PUBLIC:
     * Return the size of the identity map.
     */
    public int getIdentityMapSize() {
        return identityMapSize;
    }

    /**
     * PUBLIC:
     * The inheritance policy is used to define how a descriptor takes part in inheritance.
     * All inheritance properties for both child and parent classes is configured in inheritance policy.
     * Caution must be used in using this method as it lazy initializes an inheritance policy.
     * Calling this on a descriptor that does not use inheritance will cause problems, #hasInheritance() must always first be called.
     */
    public InheritancePolicy getDescriptorInheritancePolicy() {
        return getInheritancePolicy();
    }

    /**
     * PUBLIC:
     * The inheritance policy is used to define how a descriptor takes part in inheritance.
     * All inheritance properties for both child and parent classes is configured in inheritance policy.
     * Caution must be used in using this method as it lazy initializes an inheritance policy.
     * Calling this on a descriptor that does not use inheritance will cause problems, #hasInheritance() must always first be called.
     */
    public InheritancePolicy getInheritancePolicy() {
        if (inheritancePolicy == null) {
            // Lazy initialize to conserve space in non-inherited classes.
            setInheritancePolicy(new oracle.toplink.essentials.descriptors.InheritancePolicy(this));
        }
        return inheritancePolicy;
    }

    /**
     * INTERNAL:
     * Return the inheritance policy.
     */
    public InheritancePolicy getInheritancePolicyOrNull() {
        return inheritancePolicy;
    }

    /**
     * INTERNAL:
     * Returns the instantiation policy.
     */
    public InstantiationPolicy getInstantiationPolicy() {
        return instantiationPolicy;
    }

    /**
     * PUBLIC:
     * Return the java class.
     */
    public Class getJavaClass() {
        return javaClass;
    }

    /**
     * Return the class name, used by the MW.
     */
    public String getJavaClassName() {
        if ((javaClassName == null) && (javaClass != null)) {
            javaClassName = javaClass.getName();
        }
        return javaClassName;
    }

    /**
     * INTERNAL:
     * Returns a reference to the mappings that must be traverse when locking
     */
    public List<DatabaseMapping> getLockableMappings() {
        if (this.lockableMappings == null) {
            this.lockableMappings = new ArrayList();
        }
        return this.lockableMappings;
    }
    
    /**
     * PUBLIC:
     * Returns the mapping associated with a given attribute name.
     * This can be used to find a descriptors mapping in a amendment method before the descriptor has been initialized.
     */
    public DatabaseMapping getMappingForAttributeName(String attributeName) {
        // ** Don't use this internally, just for amendments, see getMappingForAttributeName on ObjectBuilder.
        for (Enumeration mappingsNum = mappings.elements(); mappingsNum.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappingsNum.nextElement();
            if ((mapping.getAttributeName() != null) && mapping.getAttributeName().equals(attributeName)) {
                return mapping;
            }
        }
        return null;
    }
    
    /**
     * ADVANCED:
     * Removes the locally defined mapping associated with a given attribute name.
     * This can be used in a amendment method before the descriptor has been initialized.
     */
    public DatabaseMapping removeMappingForAttributeName(String attributeName) {
        DatabaseMapping mapping = getMappingForAttributeName(attributeName);
        getMappings().remove(mapping);
        return mapping;
    }

    /**
     * PUBLIC:
     * Returns mappings
     */
    public Vector<DatabaseMapping> getMappings() {
        return mappings;
    }

    /**
     * INTERNAL:
     * Returns the foreign key relationships used for multiple tables which were specified by the user. Used
     * by the Project XML writer to output these associations
     *
     * @see #adjustMultipleTableInsertOrder()
     * @return java.util.Hashtable
     */
    public Vector getMultipleTableForeignKeyAssociations() {
        Vector associations = new Vector(getAdditionalTablePrimaryKeyFields().size() * 2);
        Iterator tablesHashtable = getAdditionalTablePrimaryKeyFields().values().iterator();
        while (tablesHashtable.hasNext()) {
            Map tableHash = (Map)tablesHashtable.next();
            Iterator fieldEnumeration = tableHash.keySet().iterator();
            while (fieldEnumeration.hasNext()) {
                DatabaseField keyField = (DatabaseField)fieldEnumeration.next();

                //PRS#36802(CR#2057) contains() is changed to containsKey()
                if (getMultipleTableForeignKeys().containsKey(keyField.getTable())) {
                    Association association = new Association(keyField.getQualifiedName(), ((DatabaseField)tableHash.get(keyField)).getQualifiedName());
                    associations.addElement(association);
                }
            }
        }
        return associations;
    }

    /**
     * INTERNAL:
     * Returns the foreign key relationships used for multiple tables which were specified by the user. The key
     * of the Map is the field in the source table of the foreign key relationship. The value is the field
     * name of the target table.
     *
     * @see #adjustMultipleTableInsertOrder()
     */
    public Map<DatabaseTable, DatabaseTable> getMultipleTableForeignKeys() {
        return multipleTableForeignKeys;
    }

    /**
     * INTERNAL:
     * Returns the Vector of DatabaseTables in the order which INSERTS should take place. This order is
     * determined by the foreign key fields which are specified by the user.
     *
     * @return java.util.Vector
     */
    public Vector<DatabaseTable> getMultipleTableInsertOrder() throws DescriptorException {
        return multipleTableInsertOrder;
    }

    /**
     * INTERNAL:
     * Returns the foreign key relationships used for multiple tables which were specified by the user. Used
     * by the Project XML writer to output these associations
     *
     * @see #adjustMultipleTableInsertOrder()
     */
    public Vector getMultipleTablePrimaryKeyAssociations() {
        Vector associations = new Vector(getAdditionalTablePrimaryKeyFields().size() * 2);
        Iterator tablesHashtable = getAdditionalTablePrimaryKeyFields().values().iterator();
        while (tablesHashtable.hasNext()) {
            Map tableHash = (Map)tablesHashtable.next();
            Iterator fieldEnumeration = tableHash.keySet().iterator();
            while (fieldEnumeration.hasNext()) {
                DatabaseField keyField = (DatabaseField)fieldEnumeration.next();

                //PRS#36802(CR#2057) contains() is changed to containsKey()
                if (!getMultipleTableForeignKeys().containsKey(keyField.getTable())) {
                    Association association = new Association(keyField.getQualifiedName(), ((DatabaseField)tableHash.get(keyField)).getQualifiedName());
                    associations.addElement(association);
                }
            }
        }
        return associations;
    }

    /**
     * INTERNAL:
     * Return the object builder
     */
    public ObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }

    /**
     * PUBLIC:
     * Returns the OptimisticLockingPolicy. By default this is an instance of VersionLockingPolicy.
     */
    public OptimisticLockingPolicy getOptimisticLockingPolicy() {
        return optimisticLockingPolicy;
    }

    /**
     * PUBLIC:
     * Return the names of all the primary keys.
     */
    public Vector<String> getPrimaryKeyFieldNames() {
        Vector<String> result = new Vector(getPrimaryKeyFields().size());
        List primaryKeyFields = getPrimaryKeyFields();
        for (int index = 0; index < primaryKeyFields.size(); index++) {
            result.addElement(((DatabaseField)primaryKeyFields.get(index)).getQualifiedName());
        }

        return result;
    }

    /**
     * INTERNAL:
     * Return all the primary key fields
     */
    public List<DatabaseField> getPrimaryKeyFields() {
        return primaryKeyFields;
    }

    /**
     * PUBLIC:
     * Returns the user defined properties.
     */
    public Map getProperties() {
        if (properties == null) {
            properties = new HashMap(5);
        }
        return properties;
    }

    /**
     * PUBLIC:
     * Returns the descriptor property associated the given String.
     */
    public Object getProperty(String name) {
        return getProperties().get(name);
    }

    /**
     * INTERNAL:
     * Return the query key with the specified name
     */
    public QueryKey getQueryKeyNamed(String queryKeyName) {
        return this.getQueryKeys().get(queryKeyName);
    }

    /**
     * PUBLIC:
     * Return the query keys.
     */
    public Map<String, QueryKey> getQueryKeys() {
        return queryKeys;
    }

    /**
     * PUBLIC:
     * Return the queryManager.
     * The query manager can be used to specify customization of the SQL
     * that TopLink generates for this descriptor.
     */
    public DescriptorQueryManager getDescriptorQueryManager() {
        return queryManager;
    }

    /**
     * PUBLIC:
     * Return the queryManager.
     * The query manager can be used to specify customization of the SQL
     * that TopLink generates for this descriptor.
     */
    public DescriptorQueryManager getQueryManager() {
        return queryManager;
    }

    /**
     * INTERNAL:
     * Get sequence number field
     */
    public DatabaseField getSequenceNumberField() {
        return sequenceNumberField;
    }

    /**
     * PUBLIC:
     * Get sequence number field name
     */
    public String getSequenceNumberFieldName() {
        if (getSequenceNumberField() == null) {
            return null;
        }
        return getSequenceNumberField().getQualifiedName();
    }

    /**
     * PUBLIC:
     * Get sequence number name
     */
    public String getSequenceNumberName() {
        return sequenceNumberName;
    }

    /**
     * INTERNAL:
     * Return the name of the session local to this descriptor.
     * This is used by the session broker.
     */
    public String getSessionName() {
        return sessionName;
    }

    /**
     * INTERNAL:
     * Checks if table name exists with the current descriptor or not.
     */
    public DatabaseTable getTable(String tableName) throws DescriptorException {
        if (getTables().isEmpty()) {
            return null;// Assume aggregate descriptor.
        }

        for (Enumeration tables = getTables().elements(); tables.hasMoreElements();) {
            DatabaseTable table = (DatabaseTable)tables.nextElement();

            if (table.getName().equals(tableName)) {
                return table;
            }
        }

        if (isAggregateDescriptor()) {
            return getDefaultTable();
        }
        throw DescriptorException.tableNotPresent(tableName, this);
    }

    /**
     * PUBLIC:
     * Return the name of the descriptor's first table.
     * This method must only be called on single table descriptors.
     */
    public String getTableName() {
        if (getTables().isEmpty()) {
            return null;
        } else {
            return ((DatabaseTable)getTables().firstElement()).getName();
        }
    }

    /**
     * PUBLIC:
     * Return the table names.
     */
    public Vector getTableNames() {
        Vector tableNames = new Vector(getTables().size());
        for (Enumeration fieldsEnum = getTables().elements(); fieldsEnum.hasMoreElements();) {
            tableNames.addElement(((DatabaseTable)fieldsEnum.nextElement()).getQualifiedName());
        }

        return tableNames;
    }

    /**
     * INTERNAL:
     * Return all the tables.
     */
    public Vector<DatabaseTable> getTables() {
        return tables;
    }

    /**
     * INTERNAL:
     * searches first descriptor than its ReturningPolicy for an equal field
     */
    public DatabaseField getTypedField(DatabaseField field) {
        boolean mayBeMoreThanOne = hasMultipleTables() && !field.hasTableName();
        DatabaseField foundField = null;
        for (int j = 0; j < getFields().size(); j++) {
            DatabaseField descField = (DatabaseField)getFields().elementAt(j);
            if (field.equals(descField)) {
                if (descField.getType() != null) {
                    foundField = descField;
                    if (!mayBeMoreThanOne || descField.getTable().equals(getDefaultTable())) {
                        break;
                    }
                }
            }
        }
        if (foundField != null) {
            foundField = (DatabaseField)foundField.clone();
            if (!field.hasTableName()) {
                foundField.setTableName("");
            }
        }

        return foundField;
    }

    /**
     * ADVANCED:
     * Return the WrapperPolicy for this descriptor.
     * This advacned feature can be used to wrap objects with other classes such as CORBA TIE objects or EJBs.
     */
    public WrapperPolicy getWrapperPolicy() {
        return wrapperPolicy;
    }

    /**
     * INTERNAL:
     * Checks if the class has any private owned parts or other dependencies, (i.e. M:M join table).
     */
    public boolean hasDependencyOnParts() {
        for (Enumeration mappings = getMappings().elements(); mappings.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.nextElement();
            if (mapping.hasDependency()) {
                return true;
            }
        }

        return false;
    }

    /**
     * INTERNAL:
     * Return if this descriptor is involved in inheritence, (is child or parent).
     */
    public boolean hasInheritance() {
        return (inheritancePolicy != null);
    }

    /**
     * INTERNAL:
     * Check if descriptor has multiple tables
     */
    public boolean hasMultipleTables() {
        return (getTables().size() > 1);
    }

    /**
     * INTERNAL:
     * Checks if the class has any private owned parts are not
     */
    public boolean hasPrivatelyOwnedParts() {
        for (Enumeration mappings = getMappings().elements(); mappings.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.nextElement();
            if (mapping.isPrivateOwned()) {
                return true;
            }
        }

        return false;
    }

    /**
     * INTERNAL:
     * Checks to see if it has a query key or mapping with the specified name or not.
     */
    public boolean hasQueryKeyOrMapping(String attributeName) {
        return (getQueryKeys().containsKey(attributeName) || (getObjectBuilder().getMappingForAttributeName(attributeName) != null));
    }

    /**
     * INTERNAL:
     * Return if a wrapper policy is used.
     */
    public boolean hasWrapperPolicy() {
        return getWrapperPolicy() != null;
    }

    /**
     * INTERNAL:
     * Initialize the mappings as a seperate step.
     * This is done as a seperate step to ensure that inheritence has been first resolved.
     */
    public void initialize(AbstractSession session) throws DescriptorException {    
        // These cached settings on the project must be set even if descriptor is initialized.
        // If defined as read-only, add to it's project's default read-only classes collection.
        if (shouldBeReadOnly() && (!session.getDefaultReadOnlyClasses().contains(getJavaClass()))) {
            session.getDefaultReadOnlyClasses().add(getJavaClass());
        }
        // Record that there is an isolated class in the project.
        if (isIsolated()) {
            session.getProject().setHasIsolatedClasses(true);
        }
        // Record that there is a non-CMP1/2 object in the project.
        if ((! hasCMPPolicy()) || getCMPPolicy().isCMP3Policy()) {
            session.getProject().setIsPureCMP2Project(false);
        }
        
        // Avoid repetitive initialization (this does not solve loops)
        if (isInitialized(INITIALIZED) || isInvalid()) {
            return;
        }

        setInitializationStage(INITIALIZED);

        // make sure that parent mappings are initialized?
        if (isChildDescriptor()) {
            getInheritancePolicy().getParentDescriptor().initialize(session);
            if (getInheritancePolicy().getParentDescriptor().isIsolated()) {
                //if the parent is isolated then the child must be isolated as well.
                this.setIsIsolated(true);
            }
        }

        // Mappings must be sorted before field are collected in the order of the mapping for indexes to work.
        // Sorting the mappings to ensure that all DirectToFields get merged before all other mappings
        // This prevents null key errors when merging maps
        if (shouldOrderMappings()) {
            Vector mappings = getMappings();
            Object[] mappingsArray = new Object[mappings.size()];
            for (int index = 0; index < mappings.size(); index++) {
                mappingsArray[index] = mappings.elementAt(index);
            }
            TOPSort.quicksort(mappingsArray, new MappingCompare());
            mappings = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(mappingsArray.length);
            for (int index = 0; index < mappingsArray.length; index++) {
                mappings.addElement(mappingsArray[index]);
            }
            setMappings(mappings);     
        }
        
        for (Enumeration mappingsEnum = getMappings().elements(); mappingsEnum.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappingsEnum.nextElement();
            validateMappingType(mapping);
            mapping.initialize(session);
            if (mapping.isAggregateObjectMapping() || ((mapping.isForeignReferenceMapping() && (!mapping.isDirectCollectionMapping())) && (!((ForeignReferenceMapping)mapping).usesIndirection()))) {
                getLockableMappings().add(mapping);
            }
            
            // Add all the fields in the mapping to myself.
            Helper.addAllUniqueToVector(getFields(), mapping.getFields());
        }
        
        // PERF: Dont initialize locking until after fields have been computed so
        // field is in correct position.
        if (!isAggregateDescriptor()) {
            if (!isChildDescriptor()) {
                // Add write lock field to getFields
                if (usesOptimisticLocking()) {
                    getOptimisticLockingPolicy().initializeProperties();
                }
            }
        }

        // All the query keys should be initialized.
        for (Iterator queryKeys = getQueryKeys().values().iterator(); queryKeys.hasNext();) {
            QueryKey queryKey = (QueryKey)queryKeys.next();
            queryKey.initialize(this);
        }

        // If this has inheritence then it needs to be initialized before all fields is set.
        if (hasInheritance()) {
            getInheritancePolicy().initialize(session);
            if (getInheritancePolicy().isChildDescriptor()) {
                for (Iterator iterator = getInheritancePolicy().getParentDescriptor().getMappings().iterator();
                         iterator.hasNext();) {
                    DatabaseMapping mapping = (DatabaseMapping)iterator.next();
                    if (mapping.isAggregateObjectMapping() || ((mapping.isForeignReferenceMapping() && (!mapping.isDirectCollectionMapping())) && (!((ForeignReferenceMapping)mapping).usesIndirection()))) {
                        getLockableMappings().add(mapping);// add those mappings from the parent.
                    }
                }
            }
        }

        // cr 4097  Ensure that the mappings are ordered after the superclasses mappings have been added.
        // This ensures that the mappings in the child class are ordered correctly
        // I am sorting the mappings to ensure that all DirectToFields get merged before all other mappings
        // This prevents null key errors when merging maps
        // This resort will change the previous sort order, only do it if has inheritance.
        if (this.hasInheritance() && shouldOrderMappings()) {
            Vector mappings = getMappings();
            Object[] mappingsArray = new Object[mappings.size()];
            for (int index = 0; index < mappings.size(); index++) {
                mappingsArray[index] = mappings.elementAt(index);
            }
            TOPSort.quicksort(mappingsArray, new MappingCompare());
            mappings = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(mappingsArray.length);
            for (int index = 0; index < mappingsArray.length; index++) {
                mappings.addElement(mappingsArray[index]);
            }
            setMappings(mappings);
        }

        // Initialize the allFields to its fields, this can be done now because the fields have been computed.
        setAllFields((Vector)getFields().clone());

        getObjectBuilder().initialize(session);
        
        if (shouldOrderMappings()) {
            // PERF: Ensure direct primary key mappings are first.
            for (int index = getObjectBuilder().getPrimaryKeyMappings().size() - 1; index >= 0; index--) {
                DatabaseMapping mapping = (DatabaseMapping) getObjectBuilder().getPrimaryKeyMappings().get(index);
                if ((mapping != null) && mapping.isDirectToFieldMapping()) {
                    getMappings().remove(mapping);
                    getMappings().add(0, mapping);
                    DatabaseField field = ((AbstractDirectMapping) mapping).getField();
                    getFields().remove(field);
                    getFields().add(0, field);
                    getAllFields().remove(field);
                    getAllFields().add(0, field);
                }
            }
        }

        if (usesOptimisticLocking() && (!isChildDescriptor())) {
            getOptimisticLockingPolicy().initialize(session);
        }
        if (hasWrapperPolicy()) {
            getWrapperPolicy().initialize(session);
        }
        getQueryManager().initialize(session);
        getEventManager().initialize(session);
        getCopyPolicy().initialize(session);
        getInstantiationPolicy().initialize(session);

        if (this.getCMPPolicy() != null) {
            this.getCMPPolicy().initialize(this, session);
        }

        //validate the fetch group setting during descriptor initialization
        if (hasFetchGroupManager() && !(Helper.classImplementsInterface(javaClass, ClassConstants.FetchGroupTracker_class))) {
            //to use fetch group, the domain class must implement FetchGroupTracker interface
            session.getIntegrityChecker().handleError(DescriptorException.needToImplementFetchGroupTracker(javaClass, this));
        }        
    }

    /**
     * INTERNAL:
     * This initialized method is used exclusively for inheritance.  It passes in
     * true if the child descriptor is isolated.
     *
     * This is needed by regular aggregate descriptors (because they are screwed up);
     * but not by SDK aggregate descriptors.
     */
    public void initializeAggregateInheritancePolicy(AbstractSession session) {
        ClassDescriptor parentDescriptor = session.getDescriptor(getInheritancePolicy().getParentClass());
        parentDescriptor.getInheritancePolicy().addChildDescriptor(this);
    }

    /**
     * INTERNAL:
     * Rebuild the multiple table primary key map.
     */
    public void initializeMultipleTablePrimaryKeyFields() {
        int additionalTablesSize = getTables().size() - 1;
        boolean isChild = hasInheritance() && getInheritancePolicy().isChildDescriptor();
        if (isChild) {
            additionalTablesSize = getTables().size() - getInheritancePolicy().getParentDescriptor().getTables().size();
        }
        if (additionalTablesSize < 1) {
            return;
        }
        ExpressionBuilder builder = new ExpressionBuilder();
        Expression joinExpression = getQueryManager().getMultipleTableJoinExpression();
        for (int index = getTables().size() - additionalTablesSize; index < getTables().size();
                 index++) {
            DatabaseTable table = (DatabaseTable)getTables().elementAt(index);
            Map oldKeyMapping = (Map)getAdditionalTablePrimaryKeyFields().get(table);
            if (oldKeyMapping != null) {
                if (!getQueryManager().hasCustomMultipleTableJoinExpression()) {
                    // Build the multiple table join expression resulting from the fk relationships.
                    for (Iterator enumtr = oldKeyMapping.keySet().iterator(); enumtr.hasNext();) {
                        DatabaseField sourceTableField = (DatabaseField)enumtr.next();
                        DatabaseField targetTableField = (DatabaseField)oldKeyMapping.get(sourceTableField);
                        DatabaseTable sourceTable = sourceTableField.getTable();
                        DatabaseTable targetTable = targetTableField.getTable();

                        // Must add this field to read, so translations work on database row, this could be either.
                        if (!getFields().contains(sourceTableField)) {
                            getFields().addElement(sourceTableField);
                        }
                        if (!getFields().contains(targetTableField)) {
                            getFields().addElement(targetTableField);
                        }

                        Expression keyJoinExpression = builder.getField(targetTableField).equal(builder.getField(sourceTableField));
                        joinExpression = keyJoinExpression.and(joinExpression);
                        
                        getQueryManager().getTablesJoinExpressions().put(targetTable, keyJoinExpression);
                        if(isChild) {
                            getInheritancePolicy().addChildTableJoinExpressionToAllParents(targetTable, keyJoinExpression);
                        }
                    }
                }
            } else {
                // If the user has specified a custom multiple table join then we do not assume that the secondary tables have identically named pk as the primary table.
                // No additional fk info was specified so assume the pk field(s) are the named the same in the additional table.
                Map newKeyMapping = new HashMap(getPrimaryKeyFields().size() + 1);
                getAdditionalTablePrimaryKeyFields().put(table, newKeyMapping);

                // For each primary key field in the primary table, add a pk relationship from the primary table's pk field to the assumed identically named secondary pk field.
                List primaryKeyFields = getPrimaryKeyFields();
                for (int pkIndex = 0; pkIndex < primaryKeyFields.size(); pkIndex++) {
                    DatabaseField primaryKeyField = (DatabaseField)primaryKeyFields.get(pkIndex);
                    DatabaseField secondaryKeyField = (DatabaseField)primaryKeyField.clone();
                    secondaryKeyField.setTable(table);
                    newKeyMapping.put(primaryKeyField, secondaryKeyField);
                    // Must add this field to read, so translations work on database row.
                    getFields().addElement(secondaryKeyField);

                    if (!getQueryManager().hasCustomMultipleTableJoinExpression()) {
                        Expression keyJoinExpression = builder.getField(secondaryKeyField).equal(builder.getField(primaryKeyField));
                        joinExpression = keyJoinExpression.and(joinExpression);

                        getQueryManager().getTablesJoinExpressions().put(table, keyJoinExpression);
                        if(isChild) {
                            getInheritancePolicy().addChildTableJoinExpressionToAllParents(table, keyJoinExpression);
                        }
                    }
                }
            }
        }
        if (joinExpression != null) {
            getQueryManager().setInternalMultipleTableJoinExpression(joinExpression);
        }
        if (getQueryManager().hasCustomMultipleTableJoinExpression()) {
            Map tablesJoinExpressions = SQLSelectStatement.mapTableToExpression(joinExpression, getTables());
            getQueryManager().getTablesJoinExpressions().putAll(tablesJoinExpressions);
            if(isChild) {
                for (int index = getTables().size() - additionalTablesSize; index < getTables().size();
                         index++) {
                    DatabaseTable table = (DatabaseTable)getTables().elementAt(index);
                    getInheritancePolicy().addChildTableJoinExpressionToAllParents(table, (Expression)tablesJoinExpressions.get(table));
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Initialize the descriptor properties such as write lock and sequecning.
     */
    protected void initializeProperties(AbstractSession session) throws DescriptorException {
        if (!isAggregateDescriptor()) {
            if (!isChildDescriptor()) {
                // Initialize the primary key fields
                List primaryKeyFields = (List)((ArrayList)getPrimaryKeyFields()).clone();
                for (int index = 0; index < primaryKeyFields.size(); index++) {
                    DatabaseField primaryKey = (DatabaseField)primaryKeyFields.get(index);
                    initializePrimaryKey(primaryKey);
                }
            }

            // build sequence number field
            if (getSequenceNumberField() != null) {
                buildField(getSequenceNumberField());
            }
        }

        // Set the local session name for the session broker.
        setSessionName(session.getName());
    }

    /**
     * INTERNAL:
     */
    protected void initializePrimaryKey(DatabaseField primaryKey) {
        buildField(primaryKey);
        if (!primaryKey.getTable().equals(getDefaultTable())) {
            getPrimaryKeyFields().remove(primaryKey);
        }
    }

    /**
     * PUBLIC:
     * Return true if this descriptor is an aggregate collection descriptor
     */
    public boolean isAggregateCollectionDescriptor() {
        return (getDescriptorType() == AGGREGATE_COLLECTION);
    }

    /**
     * PUBLIC:
     * Return true if this descriptor is an aggregate descriptor
     */
    public boolean isAggregateDescriptor() {
        return (getDescriptorType() == AGGREGATE);
    }

    /**
     * PUBLIC:
     * Return if the descriptor defines inheritence and is a child.
     */
    public boolean isChildDescriptor() {
        return hasInheritance() && getInheritancePolicy().isChildDescriptor();
    }

    /**
     * INTERNAL:
     * Check if the descriptor is finished initialization.
     */
    public boolean isFullyInitialized() {
        return this.initializationStage == POST_INITIALIZED;
    }

    /**
     * INTERNAL:
     * Check if descriptor is already initialized for the level of initialization.
     * 1 = pre
     * 2 = mapping
     * 3 = post
     */
    protected boolean isInitialized(int initializationStage) {
        return this.initializationStage >= initializationStage;
    }

    /**
     * INTERNAL:
     * Return if an error occured during initialization which should abort any further initialization.
     */
    public boolean isInvalid() {
        return this.initializationStage == ERROR;
    }

    /**
     * PUBLIC:
     * Returns true if the descriptor represents an isolated class
     */
    public boolean isIsolated() {
        return this.isIsolated;
    }

    /**
     * INTERNAL:
     * Return if this descriptor has more than one table.
     */
    public boolean isMultipleTableDescriptor() {
        return getTables().size() > 1;
    }

    /**
     * INTERNAL:
     * Indicates whether pk or some of its components
     * set after insert into the database.
     * Shouldn't be called before Descriptor has been initialized.
     */
    public boolean isPrimaryKeySetAfterInsert(AbstractSession session) {
        return (usesSequenceNumbers() && session.getSequencing().shouldAcquireValueAfterInsert(getJavaClass()));
    }

    /**
     * PUBLIC:
     * PUBLIC:
     * This method is the equivalent of calling {@link #setShouldOnlyRefreshCacheIfNewerVersion} with an argument of <CODE>true</CODE>:
     * it configures a <CODE>Descriptor</CODE> to only refresh the cache if the data received from the database by a query is newer than
     * the data in the cache (as determined by the optimistic locking field) and as long as one of the following is true:
     *
     * <UL>
     * <LI>the <CODE>Descriptor</CODE> was configured by calling {@link #alwaysRefreshCache} or {@link #alwaysRefreshCacheOnRemote},</LI>
     * <LI>the query was configured by calling {@link oracle.toplink.essentials.queryframework.ObjectLevelReadQuery#refreshIdentityMapResult}, or</LI>
     * <LI>the query was a call to {@link oracle.toplink.essentials.sessions.Session#refreshObject}</LI>
     * </UL>
     * <P>
     *
     * However, if a query hits the cache, data is not refreshed regardless of how this setting is configured. For example, by default,
     * when a query for a single object based on its primary key is executed, OracleAS TopLink will first look in the cache for the object.
     * If the object is in the cache, the cached object is returned and data is not refreshed. To avoid cache hits, use
     * the {@link #disableCacheHits} method.<P>
     *
     * Also note that the {@link oracle.toplink.essentials.sessions.UnitOfWork} will not refresh its registered objects.
     *
     * @see #dontOnlyRefreshCacheIfNewerVersion
     */
    public void onlyRefreshCacheIfNewerVersion() {
        setShouldOnlyRefreshCacheIfNewerVersion(true);
    }

    /**
     * INTERNAL:
     * Post initializations after mappings are initialized.
     */
    public void postInitialize(AbstractSession session) throws DescriptorException {
        // Avoid repetitive initialization (this does not solve loops)
        if (isInitialized(POST_INITIALIZED) || isInvalid()) {
            return;
        }

        setInitializationStage(POST_INITIALIZED);

        // Make sure that child is post initialized,
        // this initialize bottom up, unlike the two other phases that to top down.
        if (hasInheritance()) {
            for (Enumeration childEnum = getInheritancePolicy().getChildDescriptors().elements();
                     childEnum.hasMoreElements();) {
                ((ClassDescriptor)childEnum.nextElement()).postInitialize(session);
            }
        }

        // Allow mapping to perform post initialization.
        for (Enumeration mappingsEnum = getMappings().elements(); mappingsEnum.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappingsEnum.nextElement();

            // This causes post init to be called multiple times in inheritence.
            mapping.postInitialize(session);
            // PERF: computed if deferred locking is required.
            if (!shouldAcquireCascadedLocks()) {
                if ((mapping instanceof ForeignReferenceMapping) && (!((ForeignReferenceMapping)mapping).usesIndirection())) {
                    setShouldAcquireCascadedLocks(true);
                }
                if ((mapping instanceof AggregateObjectMapping) && ((AggregateObjectMapping)mapping).getDescriptor().shouldAcquireCascadedLocks()) {
                    setShouldAcquireCascadedLocks(true);
                }
            }
        }

        if (hasInheritance()) {
            getInheritancePolicy().postInitialize(session);
        }

        //PERF: Ensure that the identical primary key fields are used to avoid equals.
        for (int index = (getPrimaryKeyFields().size() - 1); index >= 0; index--) {
            DatabaseField primaryKeyField = (DatabaseField)getPrimaryKeyFields().get(index);
            int fieldIndex = getFields().indexOf(primaryKeyField);

            // Aggregate/agg-collections may not have a mapping for pk field.
            if (fieldIndex != -1) {
                primaryKeyField = (DatabaseField)getFields().get(fieldIndex);
                getPrimaryKeyFields().set(index, primaryKeyField);
            }
        }

        // Index and classify fields and primary key.
        // This is in post because it needs field classification defined in initializeMapping
        // this can come through a 1:1 so requires all descriptors to be initialized (mappings).
        // May 02, 2000 - Jon D.
        for (int index = 0; index < getFields().size(); index++) {
            DatabaseField field = getFields().elementAt(index);
            if (field.getType() == null) {
                DatabaseMapping mapping = getObjectBuilder().getMappingForField(field);
                if (mapping != null) {
                    field.setType(mapping.getFieldClassification(field));
                }
            }
            field.setIndex(index);
        }

        validateAfterInitialization(session);

        checkDatabase(session);
    }

    /**
     * INTERNAL:
     * Allow the descriptor to initialize any dependancies on this session.
     */
    public void preInitialize(AbstractSession session) throws DescriptorException {
        //3934266 move validation to the policy allowing for this to be done in the sub policies.
        getObjectChangePolicy().initialize(session, this);

        // Avoid repetitive initialization (this does not solve loops)
        if (isInitialized(PREINITIALIZED)) {
            return;
        }

        setInitializationStage(PREINITIALIZED);

        // Allow mapping pre init, must be done before validate.
        for (Enumeration mappingsEnum = getMappings().elements(); mappingsEnum.hasMoreElements();) {
            try {
                DatabaseMapping mapping = (DatabaseMapping)mappingsEnum.nextElement();
                mapping.preInitialize(session);
            } catch (DescriptorException exception) {
                session.getIntegrityChecker().handleError(exception);
            }
        }

        validateBeforeInitialization(session);

        preInitializeInheritancePolicy(session);
        
        // Make sure that parent is already preinitialized
        if (hasInheritance()) {
            // The default table will be set in this call once the duplicate
            // tables have been removed.
            getInheritancePolicy().preInitialize(session);
        } else {
            // This must be done now, after validate, before init anything else.
            setInternalDefaultTable();
        }

        verifyTableQualifiers(session.getDatasourcePlatform());
        initializeProperties(session);
        if (!isAggregateDescriptor()) {
            // Adjust before you initialize ...
            adjustMultipleTableInsertOrder();
            initializeMultipleTablePrimaryKeyFields();
        }

        getQueryManager().preInitialize(session);

    }

    /**
     * INTERNAL: 
     */
    protected void prepareCascadeLockingPolicy(DatabaseMapping mapping) {
        if (mapping.isPrivateOwned() && mapping.isForeignReferenceMapping()) {
            if (mapping.isCascadedLockingSupported()) {
                // Even if the mapping says it is supported in general, there 
                // may be conditions where it is not. Need the following checks.
                if (((ForeignReferenceMapping) mapping).hasCustomSelectionQuery()) {
                    throw ValidationException.unsupportedCascadeLockingMappingWithCustomQuery(mapping);
                } else if (isAggregateDescriptor() || isAggregateCollectionDescriptor()) {
                    throw ValidationException.unsupportedCascadeLockingDescriptor(this);
                } else {
                    mapping.prepareCascadeLockingPolicy();
                }
            } else {
                throw ValidationException.unsupportedCascadeLockingMapping(mapping);
            }
        }
    }
    
    /**
     * Hook together the inheritance policy tree.
     */
    protected void preInitializeInheritancePolicy(AbstractSession session) throws DescriptorException {
        if (isChildDescriptor() && (requiresInitialization())) {
            if (getInheritancePolicy().getParentClass().equals(getJavaClass())) {
                throw DescriptorException.parentClassIsSelf(this);
            }
            ClassDescriptor parentDescriptor = session.getDescriptor(getInheritancePolicy().getParentClass());
            parentDescriptor.getInheritancePolicy().addChildDescriptor(this);
            getInheritancePolicy().setParentDescriptor(parentDescriptor);
            parentDescriptor.preInitialize(session);
        }
    }

    /**
     * INTERNAL:
     * Rehash any hashtables based on fields.
     * This is used to clone descriptors for aggregates, which hammer field names,
     * it is probably better not to hammer the field name and this should be refactored.
     */
    public void rehashFieldDependancies(AbstractSession session) {
        getObjectBuilder().rehashFieldDependancies(session);

        for (Enumeration enumtr = getMappings().elements(); enumtr.hasMoreElements();) {
            ((DatabaseMapping)enumtr.nextElement()).rehashFieldDependancies(session);
        }
    }

    /**
     * INTERNAL:
     * A user should not be setting which attributes to join or not to join
     * after descriptor initialization; provided only for backwards compatibility.
     */
    public void reInitializeJoinedAttributes() {
        if (!isInitialized(POST_INITIALIZED)) {
            // wait until the descriptor gets initialized first
            return;
        }
        getObjectBuilder().initializeJoinedAttributes();
        if (hasInheritance()) {
            Vector children = getInheritancePolicy().getChildDescriptors();

            // use indeces to avoid synchronization.
            for (int i = 0; i < children.size(); i++) {
                InheritancePolicy child = (InheritancePolicy)children.elementAt(0);
                child.getDescriptor().reInitializeJoinedAttributes();
            }
        }
    }

    /**
     * PUBLIC:
     * Remove the user defined property.
     */
    public void removeProperty(String property) {
        getProperties().remove(property);
    }

    /**
     * INTERNAL:
     * Aggregate and Interface descriptors do not require initialization as they are cloned and
     * initialized by each mapping.
     */
    public boolean requiresInitialization() {
        return !(isAggregateDescriptor());
    }

    /**
     * INTERNAL:
     * Validate that the descriptor was defined correctly.
     * This allows for checks to be done that require the descriptor initialization to be completed.
     */
    protected void selfValidationAfterInitialization(AbstractSession session) throws DescriptorException {
        // This has to be done after, because read subclasses must be initialized.
        if (!(hasInheritance() && (getInheritancePolicy().shouldReadSubclasses() || java.lang.reflect.Modifier.isAbstract(getJavaClass().getModifiers())))) {
            if (session.getIntegrityChecker().shouldCheckInstantiationPolicy()) {
                getInstantiationPolicy().buildNewInstance();
            }
        }
        getObjectBuilder().validate(session);
    }

    /**
     * INTERNAL:
     * Validate that the descriptor's non-mapping attribute are defined correctly.
     */
    protected void selfValidationBeforeInitialization(AbstractSession session) throws DescriptorException {
        if (isChildDescriptor()) {
            ClassDescriptor parentDescriptor = session.getDescriptor(getInheritancePolicy().getParentClass());

            if (parentDescriptor == null) {
                session.getIntegrityChecker().handleError(DescriptorException.parentDescriptorNotSpecified(getInheritancePolicy().getParentClass().getName(), this));
            }
        } else {
            if (getTables().isEmpty() && (!isAggregateDescriptor())) {
                session.getIntegrityChecker().handleError(DescriptorException.tableNotSpecified(this));
            }
        }

        if (!isChildDescriptor() && !isAggregateDescriptor()) {
            if (getPrimaryKeyFieldNames().isEmpty()) {
                session.getIntegrityChecker().handleError(DescriptorException.primaryKeyFieldsNotSepcified(this));
            }
        }

        if ((getIdentityMapClass() == ClassConstants.NoIdentityMap_Class) && (getQueryManager().getDoesExistQuery().shouldCheckCacheForDoesExist())) {
            session.getIntegrityChecker().handleError(DescriptorException.identityMapNotSpecified(this));
        }

        if (((getSequenceNumberName() != null) && (getSequenceNumberField() == null)) || ((getSequenceNumberName() == null) && (getSequenceNumberField() != null))) {
            session.getIntegrityChecker().handleError(DescriptorException.sequenceNumberPropertyNotSpecified(this));
        }
    }

    /**
     * INTERNAL:
     * This is used to map the primary key field names in a multiple table 
     * descriptor.
     */
    protected void setAdditionalTablePrimaryKeyFields(DatabaseTable table, DatabaseField field1, DatabaseField field2) {
        Map tableAdditionalPKFields = (Map)getAdditionalTablePrimaryKeyFields().get(table);
            
        if (tableAdditionalPKFields == null) {
            tableAdditionalPKFields = new HashMap(2);
            getAdditionalTablePrimaryKeyFields().put(table, tableAdditionalPKFields);
        }
            
        tableAdditionalPKFields.put(field1, field2);        
    }
    
    /**
     * INTERNAL:
     * This method will be called in the case where the foreign key field is 
     * in the target table which is before the source table. In most cases,
     * this would be when the fk is on the primary table (that is, true
     * multiple table foreign key field)
     */
    protected void toggleAdditionalTablePrimaryKeyFields(DatabaseTable targetTable, DatabaseTable sourceTable) {
        Map targetTableAdditionalPKFields = (Map)getAdditionalTablePrimaryKeyFields().get(targetTable);
            
        if (targetTableAdditionalPKFields != null) {
            Iterator e = targetTableAdditionalPKFields.keySet().iterator();
        
            while (e.hasNext()) {
                DatabaseField sourceField = (DatabaseField)e.next();
                DatabaseField targetField = (DatabaseField) targetTableAdditionalPKFields.get(sourceField);
            
                setAdditionalTablePrimaryKeyFields(sourceTable, targetField, sourceField);
            }
            
            targetTableAdditionalPKFields.clear();
        }
    }
    
    /**
     * INTERNAL:
     * This is used to map the primary key field names in a multiple table 
     * descriptor.
     */
    public void setAdditionalTablePrimaryKeyFields(Map<DatabaseTable, Map<DatabaseField, DatabaseField>> additionalTablePrimaryKeyFields) {
        this.additionalTablePrimaryKeyFields = additionalTablePrimaryKeyFields;
    }

    /**
     * PUBLIC:
     * Set the alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * INTERNAL:
     * Set all the fields.
     */
    protected void setAllFields(Vector<DatabaseField> allFields) {
        this.allFields = allFields;
    }

    /**
     * PUBLIC:
     * Set the amendment class.
     * The amendment method will be called on the class before initialization to allow for it to initialize the descriptor.
     * The method must be a public static method on the class.
     */
    public void setAmendmentClass(Class amendmentClass) {
        this.amendmentClass = amendmentClass;
    }

    /**
     * INTERNAL:
     * Return the amendment class name, used by the MW.
     */
    public void setAmendmentClassName(String amendmentClassName) {
        this.amendmentClassName = amendmentClassName;
    }

    /**
     * PUBLIC:
     * Set the amendment method.
     * This will be called on the amendment class before initialization to allow for it to initialize the descriptor.
     * The method must be a public static method on the class.
     */
    public void setAmendmentMethodName(String amendmentMethodName) {
        this.amendmentMethodName = amendmentMethodName;
    }

    /**
     * PUBLIC:
     * Set the ObjectChangePolicy for this descriptor.
     */
    public void setObjectChangePolicy(ObjectChangePolicy policy) {
        this.changePolicy = policy;
    }

    /**
     * PUBLIC:
     * Set the Cache Invalidation Policy for this descriptor
     * @param CacheInvalidationPolicy
     * @see oracle.toplink.essentials.descriptors.invalidation.CacheInvalidationPolicy
     */
    public void setCacheInvalidationPolicy(CacheInvalidationPolicy policy) {
        cacheInvalidationPolicy = policy;
    }

    /**
     * ADVANCED:
     * TopLink automatically orders database access through the foreign key information provided in 1:1 and 1:m mappings.
     * In some case when 1:1 are not defined it may be required to tell the descriptor about a constraint,
     * this defines that this descriptor has a foreign key constraint to another class and must be inserted after
     * instances of the other class.
     */
    public void setConstraintDependencies(Vector constraintDependencies) {
        this.constraintDependencies = constraintDependencies;
    }

    /**
     * INTERNAL:
     * Set the copy policy.
     * This would be 'protected' but the EJB stuff in another
     * package needs it to be public
     */
    public void setCopyPolicy(CopyPolicy policy) {
        copyPolicy = policy;
        if (policy != null) {
            policy.setDescriptor(this);
        }
    }
    
    /**
     * INTERNAL:
     * The descriptors default table can be configured if the first table is not desired.
     */
    public void setDefaultTable(DatabaseTable defaultTable) {
        this.defaultTable = defaultTable;
    }

    /**
     * PUBLIC:
     * The descriptors default table can be configured if the first table is not desired.
     */
    public void setDefaultTableName(String defaultTableName) {
        setDefaultTable(new DatabaseTable(defaultTableName));
    }

    /**
     * ADVANCED:
     * set the descriptor type (NORMAL by default, others include INTERFACE, AGGREGATE, AGGREGATE COLLECTION)
     */
    public void setDescriptorType(int descriptorType) {
        this.descriptorType = descriptorType;
    }

    /**
     * INTERNAL:
     * This method is explicitly used by the XML reader.
     */
    public void setDescriptorTypeValue(String value) {
        if (value.equals("Aggregate collection")) {
            descriptorIsAggregateCollection();
        } else if (value.equals("Aggregate")) {
            descriptorIsAggregate();
        } else {
            descriptorIsNormal();
        }
    }

    /**
     * INTERNAL:
     * Set the event manager for the descriptor.  The event manager is responsible
     * for managing the pre/post selectors.
     */
    public void setEventManager(DescriptorEventManager eventManager) {
        this.eventManager = eventManager;
        if (eventManager != null) {
            eventManager.setDescriptor(this);
        }
    }

    /**
     * INTERNAL:
     * Set the existence check option from a string constant.
     */
    public void setExistenceChecking(String token) throws DescriptorException {
        getQueryManager().setExistenceCheck(token);
    }

    /**
     * INTERNAL:
     * Set the fields used by this descriptor.
     */
    public void setFields(Vector<DatabaseField> fields) {
        this.fields = fields;
    }

    /**
     * PUBLIC:
     * Set the class of identity map to be used by this descriptor.
     * The default is the "FullIdentityMap".
     */
    public void setIdentityMapClass(Class theIdentityMapClass) {
        identityMapClass = theIdentityMapClass;
    }

    /**
     * PUBLIC:
     * Set the size of the identity map to be used by this descriptor.
     * The default is the 100.
     */
    public void setIdentityMapSize(int identityMapSize) {
        this.identityMapSize = identityMapSize;
    }

    /**
     * INTERNAL:
     * Sets the inheritance policy.
     */
    public void setInheritancePolicy(InheritancePolicy inheritancePolicy) {
        this.inheritancePolicy = inheritancePolicy;
        if (inheritancePolicy != null) {
            inheritancePolicy.setDescriptor(this);
        }
    }

    /**
     * INTERNAL:
     */
    protected void setInitializationStage(int initializationStage) {
        this.initializationStage = initializationStage;
    }

    /**
     * INTERNAL:
     * Sets the instantiation policy.
     */
    public void setInstantiationPolicy(InstantiationPolicy instantiationPolicy) {
        this.instantiationPolicy = instantiationPolicy;
        if (instantiationPolicy != null) {
            instantiationPolicy.setDescriptor(this);
        }
    }
    
    /**
     * INTERNAL:
     * Set the default table if one if not already set. This method will
     * extract the default table.
     */
    public void setInternalDefaultTable() {
        if (getDefaultTable() == null) {
            setDefaultTable(extractDefaultTable());
        }
    }
    
    /**
     * INTERNAL:
     * Set the default table if one if not already set. This method will set
     * the table that is provided as the default.
     */
    public void setInternalDefaultTable(DatabaseTable defaultTable) {
        if (getDefaultTable() == null) {
            setDefaultTable(defaultTable);
        }
    }

    /**
     * PUBLIC:
     * Used to set if the class that this descriptor represents should be isolated from the
     * shared cache.
     * Note: Calling this method with true will also set the cacheSynchronizationType to DO_NOT_SEND_CHANGES
     * since isolated objects cannot be sent by TopLink cache synchronization.
     */
    public void setIsIsolated(boolean isIsolated) {
        this.isIsolated = isIsolated;
    }

    /**
    * PUBLIC:
    * Set the Java class that this descriptor maps.
    * Every descriptor maps one and only one class.
    */
    public void setJavaClass(Class theJavaClass) {
        javaClass = theJavaClass;
    }

    /**
     * INTERNAL:
     * Return the java class name, used by the MW.
     */
    public void setJavaClassName(String theJavaClassName) {
        javaClassName = theJavaClassName;
    }

    /**
     * INTERNAL:
     * Set the list of lockable mappings for this project
     * This method is provided for CMP use.  Normally, the lockable mappings are initialized
     * at descriptor initialization time.
     */
    public void setLockableMappings(List<DatabaseMapping> lockableMappings) {
        this.lockableMappings = lockableMappings;
    }

    /**
     * INTERNAL:
     * Set the mappings.
     */
    public void setMappings(Vector<DatabaseMapping> mappings) {
        // This is used from XML reader so must ensure that all mapping's descriptor has been set.
        for (Enumeration mappingsEnum = mappings.elements(); mappingsEnum.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappingsEnum.nextElement();

            // For CR#2646, if the mapping already points to the parent descriptor then leave it.
            if (mapping.getDescriptor() == null) {
                mapping.setDescriptor(this);
            }
        }
        this.mappings = mappings;
    }

    /**
     * INTERNAL:
     * This method is used by the TopLink XML Deployment Descriptor to read and write these mappings
     */
    public void setMultipleTableForeignKeyFieldNames(Vector associations) throws DescriptorException {
        Enumeration foreignKeys = associations.elements();
        while (foreignKeys.hasMoreElements()) {
            Association association = (Association)foreignKeys.nextElement();
            addMultipleTableForeignKeys((String)association.getKey(), (String)association.getValue(), true);
        }
    }

    /**
     * INTERNAL:
     *
     * @see #getMultipleTableForeignKeys
     */
    protected void setMultipleTableForeignKeys(Map<DatabaseTable, DatabaseTable> newValue) {
        this.multipleTableForeignKeys = newValue;
    }

    /**
     * ADVANCED:
     * Sets the Vector of DatabaseTables in the order which INSERTS should take place.
     * This is normally computed correctly by TopLink, however in advanced cases in it may be overridden.
     */
    public void setMultipleTableInsertOrder(Vector<DatabaseTable> newValue) {
        this.multipleTableInsertOrder = newValue;
    }

    /**
     * INTERNAL:
     * This method is used by the TopLink XML Deployment Descriptor to read and write these mappings
     */
    public void setMultipleTablePrimaryKeyFieldNames(Vector associations) throws DescriptorException {
        Enumeration foreignKeys = associations.elements();
        while (foreignKeys.hasMoreElements()) {
            Association association = (Association)foreignKeys.nextElement();
            addMultipleTableForeignKeys((String)association.getKey(), (String)association.getValue(), true);
        }
    }

    /**
     * INTERNAL:
     * Set the ObjectBuilder.
     */
    protected void setObjectBuilder(ObjectBuilder builder) {
        objectBuilder = builder;
    }

    /**
     * PUBLIC:
     * Set the OptimisticLockingPolicy.
     * This can be one of the provided locking policies or a user defined policy.
     * @see VersionLockingPolicy
     * @see TimestampLockingPolicy
     * @see FieldsLockingPolicy
     */
    public void setOptimisticLockingPolicy(OptimisticLockingPolicy optimisticLockingPolicy) {
        this.optimisticLockingPolicy = optimisticLockingPolicy;
        if (optimisticLockingPolicy != null) {
            optimisticLockingPolicy.setDescriptor(this);
        }
    }

    /**
     * PUBLIC:
     * Specify the primary key field of the descriptors table.
     * This should only be called if it is a singlton primary key field,
     * otherwise addPrimaryKeyFieldName should be called.
     * If the descriptor has many tables, this must be the primary key in all of the tables.
     *
     * @see #addPrimaryKeyFieldName(String)
     */
    public void setPrimaryKeyFieldName(String fieldName) {
        addPrimaryKeyFieldName(fieldName);
    }

    /**
     * PUBLIC:
     * User can specify a vector of all the primary key field names if primary key is composite.
     *
     * @see #addPrimaryKeyFieldName(String)
     */
    public void setPrimaryKeyFieldNames(Vector primaryKeyFieldsName) {
        setPrimaryKeyFields(new ArrayList(primaryKeyFieldsName.size()));
        for (Enumeration keyEnum = primaryKeyFieldsName.elements(); keyEnum.hasMoreElements();) {
            addPrimaryKeyFieldName((String)keyEnum.nextElement());
        }
    }

    /**
     * INTERNAL:
     * Set the primary key fields
     */
    public void setPrimaryKeyFields(List<DatabaseField> thePrimaryKeyFields) {
        primaryKeyFields = thePrimaryKeyFields;
    }

    /**
     * INTERNAL:
     * Set the user defined properties.
     */
    public void setProperties(Map properties) {
        this.properties = properties;
    }

    /**
     * PUBLIC:
     * Set the user defined property.
     */
    public void setProperty(String name, Object value) {
        getProperties().put(name, value);
    }

    /**
     * INTERNAL:
     * Set the query keys.
     */
    public void setQueryKeys(Map<String, QueryKey> queryKeys) {
        this.queryKeys = queryKeys;
    }

    /**
     * INTERNAL:
     * Set the query manager.
     */
    public void setQueryManager(DescriptorQueryManager queryManager) {
        this.queryManager = queryManager;
        if (queryManager != null) {
            queryManager.setDescriptor(this);
        }
    }

    /**
     * INTERNAL:
     * Set the sequence number field.
     */
    public void setSequenceNumberField(DatabaseField sequenceNumberField) {
        this.sequenceNumberField = sequenceNumberField;
    }

    /**
     * PUBLIC:
     * Set the sequence number field name.
     * This is the field in the descriptors table that needs its value to be generated.
     * This is normally the primary key field of the descriptor.
     */
    public void setSequenceNumberFieldName(String fieldName) {
        if (fieldName == null) {
            setSequenceNumberField(null);
        } else {
            setSequenceNumberField(new DatabaseField(fieldName));
        }
    }

    /**
     * PUBLIC:
     * Set the sequence number name.
     * This is the seq_name part of the row stored in the sequence table for this descriptor.
     * If using Oracle native sequencing this is the name of the Oracle sequence object.
     * If using Sybase native sequencing this name has no meaning, but should still be set for compatibility.
     * The name does not have to be unique among descriptors, as having descriptors share sequences can
     * improve pre-allocation performance.
     */
    public void setSequenceNumberName(String name) {
        sequenceNumberName = name;
    }

    /**
     * INTERNAL:
     * Set the name of the session local to this descriptor.
     * This is used by the session broker.
     */
    protected void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    /**
     * PUBLIC:
     * set if the descriptor is defined to always conform the results in unit of work in read query.
     *
     */
    public void setShouldAlwaysConformResultsInUnitOfWork(boolean shouldAlwaysConformResultsInUnitOfWork) {
        this.shouldAlwaysConformResultsInUnitOfWork = shouldAlwaysConformResultsInUnitOfWork;
    }

    /**
     * PUBLIC:
     * When the <CODE>shouldAlwaysRefreshCache</CODE> argument passed into this method is <CODE>true</CODE>,
     * this method configures a <CODE>Descriptor</CODE> to always refresh the cache if data is received from
     * the database by any query.<P>
     *
     * However, if a query hits the cache, data is not refreshed regardless of how this setting is configured.
     * For example, by default, when a query for a single object based on its primary key is executed, OracleAS TopLink
     * will first look in the cache for the object. If the object is in the cache, the cached object is returned and
     * data is not refreshed. To avoid cache hits, use the {@link #disableCacheHits} method.<P>
     *
     * Also note that the {@link oracle.toplink.essentials.sessions.UnitOfWork} will not refresh its registered objects.<P>
     *
     * Use this property with caution because it can lead to poor performance and may refresh on queries when it is not desired.
     * Normally, if you require fresh data, it is better to configure a query with {@link oracle.toplink.essentials.queryframework.ObjectLevelReadQuery#refreshIdentityMapResult}.
     * To ensure that refreshes are only done when required, use this method in conjunction with {@link #onlyRefreshCacheIfNewerVersion}.<P>
     *
     * When the <CODE>shouldAlwaysRefreshCache</CODE> argument passed into this method is <CODE>false</CODE>, this method
     * ensures that a <CODE>Descriptor</CODE> is not configured to always refresh the cache if data is received from the database by any query.<P>
     *
     * @see #alwaysRefreshCache
     * @see #dontAlwaysRefreshCache
     */
    public void setShouldAlwaysRefreshCache(boolean shouldAlwaysRefreshCache) {
        this.shouldAlwaysRefreshCache = shouldAlwaysRefreshCache;
    }

    /**
     * PUBLIC:
     * Define if the descriptor reference class is read-only
     */
    public void setShouldBeReadOnly(boolean shouldBeReadOnly) {
        this.shouldBeReadOnly = shouldBeReadOnly;
    }

    /**
     * PUBLIC:
     * Set the descriptor to be read-only.
     * Declaring a descriptor is read-only means that instances of the reference class will never be modified.
     * Read-only descriptor is usually used in the unit of work to gain performance as there is no need for
     * the registration, clone and merge for the read-only classes.
     */
    public void setReadOnly() {
        setShouldBeReadOnly(true);
    }

    /**
     * PUBLIC:
     * Set if cache hits on primary key read object queries should be disabled.
     *
     * @see #alwaysRefreshCache()
     */
    public void setShouldDisableCacheHits(boolean shouldDisableCacheHits) {
        this.shouldDisableCacheHits = shouldDisableCacheHits;
    }

    /**
     * PUBLIC:
     * When the <CODE>shouldOnlyRefreshCacheIfNewerVersion</CODE> argument passed into this method is <CODE>true</CODE>,
     * this method configures a <CODE>Descriptor</CODE> to only refresh the cache if the data received from the database
     * by a query is newer than the data in the cache (as determined by the optimistic locking field) and as long as one of the following is true:
     *
     * <UL>
     * <LI>the <CODE>Descriptor</CODE> was configured by calling {@link #alwaysRefreshCache} or {@link #alwaysRefreshCacheOnRemote},</LI>
     * <LI>the query was configured by calling {@link oracle.toplink.essentials.queryframework.ObjectLevelReadQuery#refreshIdentityMapResult}, or</LI>
     * <LI>the query was a call to {@link oracle.toplink.essentials.sessions.Session#refreshObject}</LI>
     * </UL>
     * <P>
     *
     * However, if a query hits the cache, data is not refreshed regardless of how this setting is configured. For example, by default,
     * when a query for a single object based on its primary key is executed, OracleAS TopLink will first look in the cache for the object.
     * If the object is in the cache, the cached object is returned and data is not refreshed. To avoid cache hits, use
     * the {@link #disableCacheHits} method.<P>
     *
     * Also note that the {@link oracle.toplink.essentials.sessions.UnitOfWork} will not refresh its registered objects.<P>
     *
     * When the <CODE>shouldOnlyRefreshCacheIfNewerVersion</CODE> argument passed into this method is <CODE>false</CODE>, this method
     * ensures that a <CODE>Descriptor</CODE> is not configured to only refresh the cache if the data received from the database by a
     * query is newer than the data in the cache (as determined by the optimistic locking field).
     *
     * @see #onlyRefreshCacheIfNewerVersion
     * @see #dontOnlyRefreshCacheIfNewerVersion
     */
    public void setShouldOnlyRefreshCacheIfNewerVersion(boolean shouldOnlyRefreshCacheIfNewerVersion) {
        this.shouldOnlyRefreshCacheIfNewerVersion = shouldOnlyRefreshCacheIfNewerVersion;
    }

    /**
     * PUBLIC:
     * This is set to turn off the ordering of mappings.  By Default this is set to true.
     * By ordering the mappings TopLink insures that object are merged in the right order.
     * If the order of the mappings needs to be specified by the developer then set this to
     * false and TopLink will use the order that the mappings were added to the descriptor
     */
    public void setShouldOrderMappings(boolean shouldOrderMappings) {
        this.shouldOrderMappings = shouldOrderMappings;
    }

    /**
     * INTERNAL:
     * Set to false to have queries conform to a UnitOfWork without registering
     * any additional objects not already in that UnitOfWork.
     * @see #shouldRegisterResultsInUnitOfWork
     * @bug 2612601
     */
    public void setShouldRegisterResultsInUnitOfWork(boolean shouldRegisterResultsInUnitOfWork) {
        this.shouldRegisterResultsInUnitOfWork = shouldRegisterResultsInUnitOfWork;
    }

    /**
     * PUBLIC:
     * Specify the table name for the class of objects the receiver describes.
     * If the table has a qualifier it should be specified using the dot notation,
     * (i.e. "userid.employee"). This method is used for single table.
     */
    public void setTableName(String tableName) throws DescriptorException {
        if (getTables().isEmpty()) {
            addTableName(tableName);
        } else {
            throw DescriptorException.onlyOneTableCanBeAddedWithThisMethod(this);
        }
    }

    /**
     * PUBLIC:
     * Specify the all table names for the class of objects the receiver describes.
     * If the table has a qualifier it should be specified using the dot notation,
     * (i.e. "userid.employee"). This method is used for multiple tables
     */
    public void setTableNames(Vector tableNames) {
        setTables(oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(tableNames.size()));
        for (Enumeration tableEnum = tableNames.elements(); tableEnum.hasMoreElements();) {
            addTableName((String)tableEnum.nextElement());
        }
    }

    /**
     * PUBLIC: Set the table Qualifier for this descriptor.  This table creator will be used for
     * all tables in this descriptor
     */
    public void setTableQualifier(String tableQualifier) {
        for (Enumeration enumtr = getTables().elements(); enumtr.hasMoreElements();) {
            DatabaseTable table = (DatabaseTable)enumtr.nextElement();
            table.setTableQualifier(tableQualifier);
        }
    }

    /**
     * INTERNAL:
     * Sets the tables
     */
    public void setTables(Vector<DatabaseTable> theTables) {
        tables = theTables;
    }

    /**
     * ADVANCED:
     * Sets the WrapperPolicy for this descriptor.
     * This advacned feature can be used to wrap objects with other classes such as CORBA TIE objects or EJBs.
     */
    public void setWrapperPolicy(WrapperPolicy wrapperPolicy) {
        this.wrapperPolicy = wrapperPolicy;

        // For bug 2766379 must be able to set the wrapper policy back to default
        // which is null.
        if (wrapperPolicy != null) {
            wrapperPolicy.setDescriptor(this);
        }
    }

    /**
     * PUBLIC:
     * Return if the descriptor is defined to always conform the results in unit of work in read query.
     *
     */
    public boolean shouldAlwaysConformResultsInUnitOfWork() {
        return shouldAlwaysConformResultsInUnitOfWork;
    }

    /**
     * PUBLIC:
     * This method returns <CODE>true</CODE> if the <CODE>Descriptor</CODE> is configured to always refresh
     * the cache if data is received from the database by any query. Otherwise, it returns <CODE>false</CODE>.
     *
     * @see #setShouldAlwaysRefreshCache
     */
    public boolean shouldAlwaysRefreshCache() {
        return shouldAlwaysRefreshCache;
    }

    /**
     * PUBLIC:
     * Return if the descriptor reference class is defined as read-only
     *
     */
    public boolean shouldBeReadOnly() {
        return shouldBeReadOnly;
    }

    /**
     * PUBLIC:
     * Return if for cache hits on primary key read object queries to be disabled.
     *
     * @see #disableCacheHits()
     */
    public boolean shouldDisableCacheHits() {
        return shouldDisableCacheHits;
    }

    /**
     * PUBLIC:
     * This method returns <CODE>true</CODE> if the <CODE>Descriptor</CODE> is configured to only refresh the cache
     * if the data received from the database by a query is newer than the data in the cache (as determined by the
     * optimistic locking field). Otherwise, it returns <CODE>false</CODE>.
     *
     * @see #setShouldOnlyRefreshCacheIfNewerVersion
     */
    public boolean shouldOnlyRefreshCacheIfNewerVersion() {
        return shouldOnlyRefreshCacheIfNewerVersion;
    }

    /**
     * INTERNAL:
     * Return if mappings should be ordered or not.  By default this is set to true
     * to prevent attributes from being merged in the wrong order
     *
     */
    public boolean shouldOrderMappings() {
        return shouldOrderMappings;
    }

    /**
     * INTERNAL:
     * PERF: Return if the primary key is simple (direct-mapped) to allow fast extraction.
     */
    public boolean hasSimplePrimaryKey() {
        return hasSimplePrimaryKey;
    }

    /**
     * INTERNAL:
     * PERF: Set if the primary key is simple (direct-mapped) to allow fast extraction.
     */
    public void setHasSimplePrimaryKey(boolean hasSimplePrimaryKey) {
        this.hasSimplePrimaryKey = hasSimplePrimaryKey;
    }

    /**
     * INTERNAL:
     * PERF: Return if deferred locks should be used.
     * Used to optimize read locking.
     * This is determined based on if any relationships do not use indirection.
     */
    public boolean shouldAcquireCascadedLocks() {
        return shouldAcquireCascadedLocks;
    }

    /**
     * INTERNAL:
     * PERF: Set if deferred locks should be used.
     * This is determined based on if any relationships do not use indirection,
     * but this provides a backdoor hook to force on if require because of events usage etc.
     */
    public void setShouldAcquireCascadedLocks(boolean shouldAcquireCascadedLocks) {
        this.shouldAcquireCascadedLocks = shouldAcquireCascadedLocks;
    }

    /**
     * PUBLIC:
     * Return true if this descriptor is using CacheIdentityMap
     */
    public boolean shouldUseCacheIdentityMap() {
        return (getIdentityMapClass() == ClassConstants.CacheIdentityMap_Class);
    }

    /**
     * PUBLIC:
     * Return true if this descriptor is using FullIdentityMap
     */
    public boolean shouldUseFullIdentityMap() {
        return (getIdentityMapClass() == ClassConstants.FullIdentityMap_Class);
    }

    /**
     * PUBLIC:
     * Return true if this descriptor is using HardCacheWeakIdentityMap.
     */
    public boolean shouldUseHardCacheWeakIdentityMap() {
        return (getIdentityMapClass() == ClassConstants.HardCacheWeakIdentityMap_Class);
    }

    /**
     * PUBLIC:
     * Return true if this descriptor is using NoIdentityMap
     */
    public boolean shouldUseNoIdentityMap() {
        return (getIdentityMapClass() == ClassConstants.NoIdentityMap_Class);
    }

    /**
     * INTERNAL:
     * Allows one to do conforming in a UnitOfWork without registering.
     * Queries executed on a UnitOfWork will only return working copies for objects
     * that have already been registered.
     * <p>Extreme care should be taken in using this feature, for a user will
     * get back a mix of registered and original (unregistered) objects.
     * <p>Best used with a WrapperPolicy where invoking on an object will trigger
     * its registration (CMP).  Without a WrapperPolicy {@link oracle.toplink.essentials.sessions.UnitOfWork#registerExistingObject registerExistingObject}
     * should be called on any object that you intend to change.
     * @return true by default.
     * @see #setShouldRegisterResultsInUnitOfWork
     * @see oracle.toplink.essentials.queryframework.ObjectLevelReadQuery#shouldRegisterResultsInUnitOfWork
     * @bug 2612601
     */
    public boolean shouldRegisterResultsInUnitOfWork() {
        return shouldRegisterResultsInUnitOfWork;
    }

    /**
     * PUBLIC:
     * Return true if this descriptor is using SoftCacheWeakIdentityMap.
     */
    public boolean shouldUseSoftCacheWeakIdentityMap() {
        return (getIdentityMapClass() == ClassConstants.SoftCacheWeakIdentityMap_Class);
    }

    /**
     * PUBLIC:
     * Return true if this descriptor is using WeakIdentityMap
     */
    public boolean shouldUseWeakIdentityMap() {
        return (getIdentityMapClass() == ClassConstants.WeakIdentityMap_Class);
    }

    /**
     * PUBLIC:
     * Returns a brief string representation of the receiver.
     */
    public String toString() {
        return Helper.getShortClassName(getClass()) + "(" + getJavaClassName() + " --> " + getTables() + ")";
    }

    /**
     * PUBLIC:
     * Set the class of identity map to be the cache identity map.
     * This map caches the LRU instances read from the database.
     * The default in JDK1.1 is "FullIdentityMap", in JDK1.2 it is the "SoftCacheWeakIdentityMap".
     */
    public void useCacheIdentityMap() {
        setIdentityMapClass(ClassConstants.CacheIdentityMap_Class);
    }

    /**
     * PUBLIC:
     * Specifies that the creation of clones within a unit of work is done by
     * sending the #clone() method to the original object. The #clone() method
     * must return a logical shallow copy of the original object.
     * This can be used if the default mechanism of creating a new instance
     * does not handle the object's non-persistent attributes correctly.
     *
     * @see #useCloneCopyPolicy(String)
     */
    public void useCloneCopyPolicy() {
        useCloneCopyPolicy("clone");
    }

    /**
     * PUBLIC:
     * Specifies that the creation of clones within a unit of work is done by
     * sending the cloneMethodName method to the original object. This method
     * must return a logical shallow copy of the original object.
     * This can be used if the default mechanism of creating a new instance
     * does not handle the object's non-persistent attributes correctly.
     *
     * @see #useCloneCopyPolicy()
     */
    public void useCloneCopyPolicy(String cloneMethodName) {
        CloneCopyPolicy policy = new CloneCopyPolicy();
        policy.setMethodName(cloneMethodName);
        setCopyPolicy(policy);
    }

    /**
     * PUBLIC:
     * Specifies that the creation of clones within a unit of work is done by building
     * a new instance using the
     * technique indicated by the descriptor's instantiation policy
     * (which by default is to use the
     * the default constructor). This new instance is then populated by using the
     * descriptor's mappings to copy attributes from the original to the clone.
     * This is the default.
     * If another mechanism is desired the copy policy allows for a clone method to be called.
     *
     * @see #useCloneCopyPolicy()
     * @see #useCloneCopyPolicy(String)
     * @see #useDefaultConstructorInstantiationPolicy()
     * @see #useMethodInstantiationPolicy(String)
     * @see #useFactoryInstantiationPolicy(Class, String)
     * @see #useFactoryInstantiationPolicy(Class, String, String)
     * @see #useFactoryInstantiationPolicy(Object, String)
     */
    public void useInstantiationCopyPolicy() {
        setCopyPolicy(new InstantiationCopyPolicy());
    }

    /**
     * PUBLIC:
     * Use the default constructor to create new instances of objects built from the database.
     * This is the default.
     * The descriptor's class must either define a default constructor or define
     * no constructors at all.
     *
     * @see #useMethodInstantiationPolicy(String)
     * @see #useFactoryInstantiationPolicy(Class, String)
     * @see #useFactoryInstantiationPolicy(Class, String, String)
     * @see #useFactoryInstantiationPolicy(Object, String)
     */
    public void useDefaultConstructorInstantiationPolicy() {
        getInstantiationPolicy().useDefaultConstructorInstantiationPolicy();
    }

    /**
     * PUBLIC:
     * Use an object factory to create new instances of objects built from the database.
     * The methodName is the name of the
     * method that will be invoked on the factory. When invoked, it must return a new instance
     * of the descriptor's class.
     * The factory will be created by invoking the factoryClass's default constructor.
     *
     * @see #useDefaultConstructorInstantiationPolicy()
     * @see #useMethodInstantiationPolicy(String)
     * @see #useFactoryInstantiationPolicy(Class, String, String)
     * @see #useFactoryInstantiationPolicy(Object, String)
     */
    public void useFactoryInstantiationPolicy(Class factoryClass, String methodName) {
        getInstantiationPolicy().useFactoryInstantiationPolicy(factoryClass, methodName);
    }

    /**
     * INTERNAL:
     * Set the factory class name, used by the MW.
     */
    public void useFactoryInstantiationPolicy(String factoryClassName, String methodName) {
        getInstantiationPolicy().useFactoryInstantiationPolicy(factoryClassName, methodName);
    }

    /**
     * PUBLIC:
     * Use an object factory to create new instances of objects built from the database.
     * The factoryMethodName is a static method declared by the factoryClass.
     * When invoked, it must return an instance of the factory. The methodName is the name of the
     * method that will be invoked on the factory. When invoked, it must return a new instance
     * of the descriptor's class.
     *
     * @see #useDefaultConstructorInstantiationPolicy()
     * @see #useFactoryInstantiationPolicy(Class, String)
     * @see #useFactoryInstantiationPolicy(Object, String)
     * @see #useMethodInstantiationPolicy(String)
     */
    public void useFactoryInstantiationPolicy(Class factoryClass, String methodName, String factoryMethodName) {
        getInstantiationPolicy().useFactoryInstantiationPolicy(factoryClass, methodName, factoryMethodName);
    }

    /**
     * INTERNAL:
     * Set the factory class name, used by the MW.
     */
    public void useFactoryInstantiationPolicy(String factoryClassName, String methodName, String factoryMethodName) {
        getInstantiationPolicy().useFactoryInstantiationPolicy(factoryClassName, methodName, factoryMethodName);
    }

    /**
     * PUBLIC:
     * Use an object factory to create new instances of objects built from the database.
     * The methodName is the name of the
     * method that will be invoked on the factory. When invoked, it must return a new instance
     * of the descriptor's class.
     *
     * @see #useDefaultConstructorInstantiationPolicy()
     * @see #useMethodInstantiationPolicy(String)
     * @see #useFactoryInstantiationPolicy(Class, String)
     * @see #useFactoryInstantiationPolicy(Class, String, String)
     */
    public void useFactoryInstantiationPolicy(Object factory, String methodName) {
        getInstantiationPolicy().useFactoryInstantiationPolicy(factory, methodName);
    }

    /**
     * PUBLIC:
     * Set the class of identity map to be the full identity map.
     * This map caches all instances read and grows to accomodate them.
     * The default is the "SoftCacheWeakIdentityMap".
     */
    public void useFullIdentityMap() {
        setIdentityMapClass(ClassConstants.FullIdentityMap_Class);
    }

    /**
     * PUBLIC:
     * Set the class of identity map to be the hard cache weak identity map.
     * This map uses weak references to only cache object in-memory.
     * It also includes a secondary fixed sized soft cache to improve caching performance.
     * This is provided because some Java VM's do not implement soft references correctly.
     * The default is the "SoftCacheWeakIdentityMap".
     */
    public void useHardCacheWeakIdentityMap() {
        setIdentityMapClass(ClassConstants.HardCacheWeakIdentityMap_Class);
    }

    /**
     * PUBLIC:
     * Use the specified static method to create new instances of objects built from the database.
     * This method must be statically declared by the descriptor's class, and it must
     * return a new instance of the descriptor's class.
     *
     * @see #useDefaultConstructorInstantiationPolicy()
     * @see #useFactoryInstantiationPolicy(Class, String)
     * @see #useFactoryInstantiationPolicy(Class, String, String)
     * @see #useFactoryInstantiationPolicy(Object, String)
     */
    public void useMethodInstantiationPolicy(String staticMethodName) {
        getInstantiationPolicy().useMethodInstantiationPolicy(staticMethodName);
    }

    /**
     * PUBLIC:
     * Set the class of identity map to be the no identity map.
     * This map does no caching.
     * The default is the "SoftCacheWeakIdentityMap".
     */
    public void useNoIdentityMap() {
        setIdentityMapClass(ClassConstants.NoIdentityMap_Class);
    }

    /**
     * PUBLIC:
     * Set the class of identity map to be the soft cache weak identity map.
     * The SoftCacheIdentityMap holds a fixed number of objects is memory
     * (using SoftReferences) to improve caching.
     * The default is the "SoftCacheWeakIdentityMap".
     */
    public void useSoftCacheWeakIdentityMap() {
        setIdentityMapClass(ClassConstants.SoftCacheWeakIdentityMap_Class);
    }

    /**
     * PUBLIC:
     * Return true if the receiver uses write (optimistic) locking.
     */
    public boolean usesOptimisticLocking() {
        return (optimisticLockingPolicy != null);
    }
    
    /**
     * PUBLIC:
     * Return true if the receiver uses version optimistic locking.
     */
    public boolean usesVersionLocking() {
        return (usesOptimisticLocking() && (getOptimisticLockingPolicy() instanceof VersionLockingPolicy));
    }

    /**
     * PUBLIC:
     * Return true if the receiver uses sequence numbers.
     */
    public boolean usesSequenceNumbers() {
        return ((getSequenceNumberField() != null) && (getSequenceNumberName() != null));
    }

    /**
     * PUBLIC:
     * Use the Timestamps locking policy and storing the value in the cache key
     * #see useVersionLocking(String)
     */
    public void useTimestampLocking(String writeLockFieldName) {
        useTimestampLocking(writeLockFieldName, true);
    }

    /**
     * PUBLIC:
     * Set the locking policy to use timestamp version locking.
     * This updates the timestamp field on all updates, first comparing that the field has not changed to detect locking conflicts.
     * Note: many database have limited precision of timestamps which can be an issue is highly concurrent systems.
     *
     * The parameter 'shouldStoreInCache' configures the version lock value to be stored in the cache or in the object.
     * Note: if using a stateless model where the object can be passed to a client and then later updated in a different transaction context,
     * then the version lock value should not be stored in the cache, but in the object to ensure it is the correct value for that object.
     * @see VersionLockingPolicy
     */
    public void useTimestampLocking(String writeLockFieldName, boolean shouldStoreInCache) {
        TimestampLockingPolicy policy = new TimestampLockingPolicy(writeLockFieldName);
        if (shouldStoreInCache) {
            policy.storeInCache();
        } else {
            policy.storeInObject();
        }
        setOptimisticLockingPolicy(policy);
    }

    /**
     * PUBLIC:
     * Default to use the version locking policy and storing the value in the cache key
     * #see useVersionLocking(String)
     */
    public void useVersionLocking(String writeLockFieldName) {
        useVersionLocking(writeLockFieldName, true);
    }

    /**
     * PUBLIC:
     * Set the locking policy to use numeric version locking.
     * This updates the version field on all updates, first comparing that the field has not changed to detect locking conflicts.
     *
     * The parameter 'shouldStoreInCache' configures the version lock value to be stored in the cache or in the object.
     * Note: if using a stateless model where the object can be passed to a client and then later updated in a different transaction context,
     * then the version lock value should not be stored in the cache, but in the object to ensure it is the correct value for that object.
     * @see TimestampLockingPolicy
     */
    public void useVersionLocking(String writeLockFieldName, boolean shouldStoreInCache) {
        VersionLockingPolicy policy = new VersionLockingPolicy(writeLockFieldName);
        if (shouldStoreInCache) {
            policy.storeInCache();
        } else {
            policy.storeInObject();
        }
        setOptimisticLockingPolicy(policy);
    }

    /**
     * PUBLIC:
     * Set the class of identity map to be the weak identity map.
     * The default is the "SoftCacheWeakIdentityMap".
     */
    public void useWeakIdentityMap() {
        setIdentityMapClass(ClassConstants.WeakIdentityMap_Class);
    }

    /**
     * INTERNAL:
     * Validate the entire post-initialization descriptor.
     */
    protected void validateAfterInitialization(AbstractSession session) {
        selfValidationAfterInitialization(session);
        for (Enumeration mappings = getMappings().elements(); mappings.hasMoreElements();) {
            ((DatabaseMapping)mappings.nextElement()).validateAfterInitialization(session);
        }
    }

    /**
     * INTERNAL:
     * Validate the entire pre-initialization descriptor.
     */
    protected void validateBeforeInitialization(AbstractSession session) {
        selfValidationBeforeInitialization(session);
        for (Enumeration mappings = getMappings().elements(); mappings.hasMoreElements();) {
            ((DatabaseMapping)mappings.nextElement()).validateBeforeInitialization(session);
        }
    }

    /**
     * INTERNAL:
     * Check that the qualifier on the table names are properly set.
     */
    protected void verifyTableQualifiers(Platform platform) {
        DatabaseTable table;
        Enumeration tableEnumeration;
        String tableQualifier = platform.getTableQualifier();

        if (tableQualifier.length() == 0) {
            return;
        }

        tableEnumeration = getTables().elements();
        while (tableEnumeration.hasMoreElements()) {
            table = (DatabaseTable)tableEnumeration.nextElement();
            if (table.getTableQualifier().length() == 0) {
                table.setTableQualifier(tableQualifier);
            }
        }
    }

    /**
     * ADVANCED:
     * Return the cmp descriptor that holds EJB CMP specific information.
     * This will be null unless explicitly set, or after CMP deoloyment.
     * This can only be specified when using CMP for bean class descriptors.
     * This must be set explicitly if any setting need to be configured and
     * before calling getCMPPolicy().
     */
    public CMPPolicy getCMPPolicy() {
        return cmpPolicy;
    }

    /**
     * ADVANCED:
     * Set the cmp descriptor that holds EJB CMP specific information.
     * This can only be specified when using CMP for bean class descriptors.
     * This must be set explicitly if any setting need to be configured and
     * before calling getCMPPolicy().
     */
    public void setCMPPolicy(CMPPolicy newCMPPolicy) {
        cmpPolicy = newCMPPolicy;
        if (cmpPolicy != null){
            cmpPolicy.setDescriptor(this);
        }
    }

    /**
     * PUBLIC:
     * Get the fetch group manager for the descriptor.  The fetch group manager is responsible
     * for managing the fetch group behaviors and operations.
     * To use the fetch group, the domain object must implement FetchGroupTracker interface. Otherwise,
     * a descriptor validation exception would throw during initialization.
     * NOTE: This is currently only supported in CMP2.
     * @see oracle.toplink.essentials.queryframework.FetchGroupTracker
     */
    public FetchGroupManager getFetchGroupManager() {
        return fetchGroupManager;
    }

    /**
     * PUBLIC:
     * Set the fetch group manager for the descriptor.  The fetch group manager is responsible
     * for managing the fetch group behaviors and operations.
     */
    public void setFetchGroupManager(FetchGroupManager fetchGroupManager) {
        this.fetchGroupManager = fetchGroupManager;
        if (fetchGroupManager != null) {
            //set the back reference
            fetchGroupManager.setDescriptor(this);
        }
    }

    /**
     * INTERNAL:
     * Return true if the descriptor is a CMP entity descriptor
     */
    public boolean isDescriptorForCMP() {
        return (this.getCMPPolicy() != null);
    }

    /**
     * INTERNAL:
     * Return if the descriptor has a fecth group manager asociated with.
     */
    public boolean hasFetchGroupManager() {
        return (fetchGroupManager != null);
    }
    
    /**
     * INTERNAL:
     */
     public boolean hasCascadeLockingPolicies() {
        return !cascadeLockingPolicies.isEmpty();
     }
     
    /**
     * INTERNAL:
     * Return if the descriptor has a CMP policy.
     */
    public boolean hasCMPPolicy() {
        return (cmpPolicy != null);
    }

    /**
     * INTERNAL:
     *
     * Return the default fetch group on the descriptor.
     * All read object and read all queries will use the default fetch group if
     * no fetch group is explicitly defined for the query.
     */
    public FetchGroup getDefaultFetchGroup() {
        if (!hasFetchGroupManager()) {
            //fetch group manager is not set, therefore no default fetch group.
            return null;
        }
        return getFetchGroupManager().getDefaultFetchGroup();
    }
}
