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
 * Created on April 20, 2005, 10:09 AM
 */

package com.sun.persistence.runtime.sqlstore.impl;

import com.sun.persistence.api.model.mapping.MappingReferenceKey;
import com.sun.persistence.api.model.mapping.MappingRelationship;
import com.sun.persistence.api.model.mapping.MappingTable;
import com.sun.persistence.runtime.LogHelperSQLStore;
import com.sun.persistence.runtime.connection.SQLConnector;
import com.sun.persistence.runtime.connection.impl.SQLConnectorImpl;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingClass;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingField;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.query.impl.QueryFactory;
import com.sun.persistence.runtime.sqlstore.database.DBVendorType;
import com.sun.persistence.runtime.sqlstore.sql.update.BaseDMLStatement;
import com.sun.persistence.runtime.sqlstore.sql.update.DeleteSQLBuffer;
import com.sun.persistence.runtime.sqlstore.sql.update.InsertSQLBuffer;
import com.sun.persistence.runtime.sqlstore.sql.update.NullableFieldParameterBinder;
import com.sun.persistence.runtime.sqlstore.sql.update.SQLBuffer;
import com.sun.persistence.runtime.sqlstore.sql.update.UpdateSQLBuffer;
import com.sun.persistence.support.Extent;
import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.identity.ByteIdentity;
import com.sun.persistence.support.identity.CharIdentity;
import com.sun.persistence.support.identity.IntIdentity;
import com.sun.persistence.support.identity.LongIdentity;
import com.sun.persistence.support.identity.ShortIdentity;
import com.sun.persistence.support.identity.SingleFieldIdentity;
import com.sun.persistence.support.identity.StringIdentity;
import com.sun.persistence.support.spi.PersistenceCapable;
import com.sun.persistence.utility.logging.Logger;

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.forte4j.modules.dbmodel.ColumnPairElement;

import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOCollection;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDORelationship;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.store.StoreManager;
import com.sun.org.apache.jdo.util.I18NHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

/**
 * This class represents the relational database datastore. It provides the
 * means to write and read instances, to get the extent of classes, and to get
 * the object id for a persistence capable object. It also implements a database
 * connection to perform any SQL statements. You should have
 * <code>ConnectionFactory</code> object or <code>DataSource</code> object
 * ready in order to construct this object.
 * @author jie leng
 */
public class SQLStoreManager implements StoreManager {

    /**
     * A Connector instance which is initialized at construction of
     * SQLStoreManager object
     */
    SQLConnector _connector = null;

    /**
     * The associated <code>PersistenceManager</code>.
     */
    PersistenceManagerInternal pm;

    /**
     * The associated <code>QueryFactory</code>.
     */
    QueryFactory qf = QueryFactory.getInstance();

    /** Runtime logger instance. */
    private static Logger logger = LogHelperSQLStore.getLogger();

    /** Used in logging messages. */
    private static final String className = SQLStoreManager.class.getName();

    /** I18N support. */
    private static final I18NHelper msg =
        I18NHelper.getInstance("com.sun.persistence.runtime.Bundle"); // NOI18N

    /**
     * Creates a new instance of SQLStoreManager
     * @param pm Associated <code>PersistenceManager</code>
     * @param connectionFactory A <code>ConnectionFactory</code> object
     * or a <code>DataSource</code> object.
     * @param username A string for the user of the database connection.
     * @param password A string for the password of the database connection.
     */
    public SQLStoreManager(PersistenceManagerInternal pm,
            Object connectionFactory, String username, String password) {
        this.pm = pm;
        _connector =
                new SQLConnectorImpl(connectionFactory, username, password);
    }

    /**
     * Returns a Connector suitable for committing or rolling back operations on
     * this store.
     */
    public SQLConnector getConnector() {
        return _connector;
    }

    /**
     * Returns a Connector suitable for committing or rolling back operations on
     * this store for a specific userid.
     * @param userid the userid for the connection
     * @param password the password for the connection
     */
    public SQLConnector getConnector(String userid, String password) {
        return _connector;
    }

    /**
     * Returns a DBVendorType for this SQLStoreManager
     */
    public DBVendorType getDBVendorType() {
        Connection con = null;
        DBVendorType dbVendor = null;

        try {
            con = _connector.getConnection();
            dbVendor = new DBVendorType(con.getMetaData(), null);
        } catch (SQLException ex) {
            throw new JDOFatalInternalException(msg.msg(
                    "core.configuration.getvendortypefailed"), ex); // NOI18N
        } finally {
            try {
                _connector.releaseConnection();
            } catch (Exception e) {
                // Ignore
            }
        }

        return dbVendor;
    }

    //
    // Methods which represent individual requests on the store
    //

    /**
     * Causes the object managed by the given state manager's object to be
     * inserted into the database.
     * @param loadedFields BitSet of fields to be inserted in the database (may
     * be ignored).
     * @param dirtyFields BitSet of all fields as all fields are marked as dirty
     * for <code>insert</code>. After return, bits will remain as set for fields
     * that were <em>not</em> inserted. If any bits are set, the return will be
     * <code>FLUSHED_PARTIAL</code>.
     * @param sm The state manager whose object is to be inserted.
     * @return one of <code>StateManagerInternal.FLUSHED_{COMPLETE, PARTIAL,
     * NONE}</code>, depending on the success of the operation in
     * inserting specified fields into the database.
     */
    public int insert(BitSet loadedFields, BitSet dirtyFields,
            StateManagerInternal sm) {
        int result = StateManagerInternal.FLUSHED_NONE;
        final boolean debug = logger.isLoggable(Logger.FINEST);

        if (debug) {
            logger.entering(className, "insert");  //NOI18N
        }

        RuntimeMappingClass runtimeMappingClass = getRuntimeMappingClass(sm);
        MappingTable mappingTables[] = runtimeMappingClass.getMappingTables();
        int numOfFields = runtimeMappingClass.getJDOClass().getManagedFieldCount();

        for (int i = 0; i < mappingTables.length; i++) {
            String tableName = mappingTables[i].getName();
            SQLBuffer insertSQLBuffer = new InsertSQLBuffer(tableName);
            BaseDMLStatement insertStmt = new BaseDMLStatement(numOfFields);

            processDirtyFields(sm, dirtyFields, runtimeMappingClass,
                    insertStmt, insertSQLBuffer);

            insertStmt.setSQLString(insertSQLBuffer.toString());
            insertStmt.execute(sm, _connector.getConnection());
        }
        result = StateManagerInternal.FLUSHED_COMPLETE;

        if (debug) {
            logger.exiting(className, "insert"); // NOI18N
        }

        return result;
    }

    /**
     * Causes the object managed by the given state manager to be updated in the
     * database.
     * @param loadedFields BitSet of fields loaded from the database.
     * @param dirtyFields BitSet of changed fields that are to be flushed to the
     * database. It is the StoreManager policy which fields are to be verified
     * against those in the database, if this <code>update</code> is within the
     * context of an optimistic transaction.  After return, bits will remain set
     * for fields that were not flushed, and in such case the return will be
     * <code>FLUSHED_PARTIAL</code>.
     * @param sm The state manager whose object is to be updated.
     * @return one of <code>StateManagerInternal.FLUSHED_{COMPLETE, PARTIAL,
     * NONE}</code>, depending on the success of the operation in
     * updating specified fields into the database.
     */
    public int update(BitSet loadedFields, BitSet dirtyFields,
            StateManagerInternal sm) {
        int result = StateManagerInternal.FLUSHED_NONE;
        final boolean debug = logger.isLoggable(Logger.FINEST);

        if (debug) {
            logger.entering(className, "update"); //NOI18N
        }

        RuntimeMappingClass runtimeMappingClass = getRuntimeMappingClass(sm);
        MappingTable mappingTables[] = runtimeMappingClass.getMappingTables();
        int numOfFields = runtimeMappingClass.getJDOClass().getManagedFieldCount();

        for (int i = 0; i < mappingTables.length; i++) {
            String tableName = mappingTables[i].getName();
            SQLBuffer updateSQLBuffer = new UpdateSQLBuffer(tableName);
            BaseDMLStatement updateStmt = new BaseDMLStatement(numOfFields);

            processDirtyFields(sm, dirtyFields, runtimeMappingClass,
                    updateStmt, updateSQLBuffer);
            processLoadedFields(sm, loadedFields, runtimeMappingClass,
                    updateStmt, updateSQLBuffer);

            updateStmt.setSQLString(updateSQLBuffer.toString());
            updateStmt.execute(sm, _connector.getConnection());
        }
        result = StateManagerInternal.FLUSHED_COMPLETE;

        if (debug) {
            logger.exiting(className, "update"); // NOI18N
        }

        return result;
    }

    /**
     * Causes the object managed by the given state manager to be verified in
     * the database.
     * @param loadedFields BitSet of fields to be verified against those in the
     * database.
     * @param dirtyFields Unused as there are no changed fields in this
     * transaction.
     * @param sm The state manager whose object is to be verified.
     * @return StateManagerInternal.FLUSHED_COMPLETE.
     * @throws JDODataStoreException if data in memory does not match that in
     * the database.
     */
    public int verifyFields(BitSet loadedFields, BitSet dirtyFields,
            StateManagerInternal sm) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Causes the object managed by the given state manager to be deleted in the
     * database.
     * @param loadedFields BitSet of fields loaded from the database.
     * @param dirtyFields BitSet of changed fields. It is the StoreManager
     * policy which fields are to be verified against those in the database, if
     * this <code>delete</code> is within the context of an optimistic
     * transaction. After return, bits will remain set for the fields that were
     * not flushed, if the <code>update</code> was performed to resolve
     * dependencies. In such case the return will be <code>FLUSHED_PARTIAL</code>.
     * @param sm The state manager whose object is to be deleted.
     * @return one of <code>StateManagerInternal.FLUSHED_{COMPLETE,
     * NONE}</code>, depending on the success of the delete operation.
     */
    public int delete(BitSet loadedFields, BitSet dirtyFields,
            StateManagerInternal sm) {
        int result = StateManagerInternal.FLUSHED_NONE;
        final boolean debug = logger.isLoggable(Logger.FINEST);

        if (debug) {
            logger.entering(className, "delete"); //NOI18N
        }

        RuntimeMappingClass runtimeMappingClass = getRuntimeMappingClass(sm);
        MappingTable mappingTables[] = runtimeMappingClass.getMappingTables();
        int numOfFields = runtimeMappingClass.getJDOClass().getManagedFieldCount();

        for (int i = 0; i < mappingTables.length; i++) {
            String tableName = mappingTables[i].getName();
            SQLBuffer deleteSQLBuffer = new DeleteSQLBuffer(tableName);
            BaseDMLStatement deleteStmt = new BaseDMLStatement(numOfFields);

            processLoadedFields(sm, loadedFields, runtimeMappingClass,
                    deleteStmt, deleteSQLBuffer);

            deleteStmt.setSQLString(deleteSQLBuffer.toString());
            deleteStmt.execute(sm, _connector.getConnection());
        }
        result = StateManagerInternal.FLUSHED_COMPLETE;

        if (debug) {
            logger.exiting(className, "delete"); // NOI18N
        }

        return result;
    }

    /**
     * Flushes all elements in the given iterator.
     * @param it Iterator of StateManagerInternal instances to be flushed.
     * @param pm PersistenceManagerInternal on whose behalf instances are being
     * flushed.
     * @throws com.sun.persistence.support.JDOFatalDataStoreException if instances
     *  could not all be flushed as determined by <code>sm.isFlushed()</code>.
     */
    public void flush(Iterator it, PersistenceManagerInternal pm) {
        final boolean optimistic = pm.currentTransaction().getOptimistic();
        final boolean debug = logger.isLoggable(Logger.FINEST);

        if (debug) {
            logger.entering(
                    className, "flush", Boolean.valueOf(optimistic)); // NOI18N
        }

        verifyPM(pm);

        boolean err = false;

        while (it.hasNext()) {
            StateManagerInternal sm = (StateManagerInternal)it.next();
            sm.preStore();

            sm.replaceSCOFields();
            sm.flush(this);
            if (! sm.isFlushed()) {
                err = true;
                break;
            }
        }

        if (debug) {
            logger.exiting(className, "flush", Boolean.valueOf(!err)); // NOI18N
        }

        if (err) {
            // XXX Collect all errors
            // XXX message can be fixed to say "possibly due to circular dependencies"
            throw new JDOFatalInternalException(
                    msg.msg("jdo.persistencemanagerimpl.notprocessed")); // NOI18N
        }
    }

    //
    // The following methods allow fields of an object to be read from the
    // store to the client.  There are not any corresponding methods for
    // update, nor delete,as those are handled via prepare and commit.  I.e.,
    // when the prepare method of an implementation of this interface is
    // invoked, it should examine the given sm to see if it is dirty or
    // deleted, and update or remove it in the store accordingly.
    //

    /**
     * Causes values for fields required by the state manager's object to be
     * retrieved from the store and supplied to the state manager.
     * @param sm The state manager whose fields are to be read.
     * @param fieldNums The fields which are to be read.
     */
    public void fetch(StateManagerInternal sm, int fieldNums[]) {
        final boolean debug = logger.isLoggable(Logger.FINEST);

        if (debug) {
            logger.entering(className, "fetch"); //NOI18N
        }

        // XXX: TODO: fetch group fields to be added
        // At this point if it's not 1 field, treat it as DFG
        if (fieldNums != null && fieldNums.length > 1) {
            fieldNums = null;
        }

        // XXX: TODO: we should not be looking up JDOClass or RuntimeMappingClass
        RuntimeMappingClass runtimeMappingClass = getRuntimeMappingClass(sm);
        JDOClass jdoClass = runtimeMappingClass.getJDOClass();

        Query q = qf.getQuery(jdoClass, fieldNums, pm);
        if (fieldNums != null && fieldNums.length > 0) {
            // Lazy loading or navigation

            q.setParameter(1, sm.getObject());
            List rc = q.getResultList();
            // XXX: TODO: create a FieldManager that can handle more than 1 field
            // and extract primitive values if fetch group fields had been added.
            com.sun.org.apache.jdo.impl.state.SimpleFieldManager fm =
                    new com.sun.org.apache.jdo.impl.state.SimpleFieldManager();

            JDOField f = jdoClass.getField(fieldNums[0]);
            JDORelationship rl = f.getRelationship();
            boolean onManySide = rl.isJDOCollection();
            if (onManySide) {
                Class el = (Class) ((JDOCollection)rl).getElementType().
                        getUnderlyingObject();
                Class type = (Class) rl.getDeclaringField().getType().
                        getUnderlyingObject();
    
                /* If it is a sorted set check for comparator:
                Comparator cr = null;
                if (java.util.SortedSet.class.isInstance(type)) {
                    cr = ((SortedSet)c).comparator();
                }
                */
    
                // RESOLVE: allowNulls...
                boolean allowNulls = true;
                fm.storeObjectField(fieldNums[0], 
                        pm.newCollectionInstanceInternal(
                            type, ((el == null)? Object.class : el), 
                            allowNulls, new Integer(rc.size()), null, 
                            rc, null));
    
            } else {
                fm.storeObjectField(fieldNums[0], rc.get(0));
            }
            sm.replaceFields(fieldNums, fm);
        } else {
            // It's a reload
            // XXX: TODO: Extract multiple PK fields.
            q.setParameter(1, sm.getExternalObjectId());
            q.getSingleResult(); // Ignore the result - it's a reload
        }

        if (debug) {
            logger.exiting(className, "fetch"); // NOI18N
        }
    }

    /**
     * Provides the means to get all instances of a particular class, or of that
     * class and its subclasses.  If there are no instances of the given class
     * (nor its subclass) in the store, returns null.
     * @param pcClass Indicates the class of the instances that are in the
     * returned Extent.
     * @param subclasses If true, then instances subclasses of pcClass are
     * included in the resulting Extent.  If false, then only instances of
     * pcClass are  included.
     * @param pm PersistenceManagerInternal making the request.
     * @return An Extent from which instances of pcClass (and subclasses if
     * appropriate) can be obtained.  Does not return null if there are
     * no instances; in that case it returns an empty Extent.
     */
    public Extent getExtent(Class pcClass, boolean subclasses,
            PersistenceManagerInternal pm) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //
    // The following methods provide mappings between object ids, state
    // managers, and persistence capables.
    //

    /**
     * Creates a new Object Id for the given StateManagerInternal.  The
     * resulting Object Id should not be given to user/client code.
     * @param sm StateManagerInternal for which an Object Id is needed.
     * @param pm PersistenceManagerInternal in which the sm's object is
     * created.
     * @return Object Id corresponding to the given StateManagerInternal
     */
    public Object createObjectId(StateManagerInternal sm,
            PersistenceManagerInternal pm) {
        return  sm.getObject().jdoNewObjectIdInstance();
    }

    /**
     * Returns an Object Id that can be given to user/client code and which is a
     * <em>copy or external representation</em> of the given objectId.
     * @param objectId Object Id for which an external Object Id is needed.
     * @param pc PersistenceCapable instance associated with this Object Id.
     * @return An Object Id that can be given to user/client code.
     */
    public Object getExternalObjectId(Object objectId, PersistenceCapable pc) {
        if (objectId == null || !(objectId instanceof SingleFieldIdentity))
            return objectId;

        // TODO: Replace the block below with 
        // ((SingleFieldIdentity)objectId).getKeyAsObject();
        if (objectId instanceof ByteIdentity) {
            return new Byte(((ByteIdentity)objectId).getKey());
        } else if (objectId instanceof CharIdentity) {
            return new Character(((CharIdentity)objectId).getKey());
        } else if (objectId instanceof IntIdentity) {
            return new Integer(((IntIdentity)objectId).getKey());
        } else if (objectId instanceof LongIdentity) {
            return new Long(((LongIdentity)objectId).getKey());
        } else if (objectId instanceof ShortIdentity) {
            return new Short(((ShortIdentity)objectId).getKey());
        } else if (objectId instanceof StringIdentity) {
            return new String(((StringIdentity)objectId).getKey());
        } else {
            //TODO: As per disucssion with craig, when ObjectIdentity is
            // implemented, change following to
            // return new ObjectIdentity(pcClass, objecId);
            throw new UnsupportedOperationException(
                    "ObjectSingleFieldIdentity not supported");
        }
    }

    /**
     * Returns an Object Id that can be used by the runtime code and which is a
     * <em>an internal representation</em> of the given objectId.
     * @param objectId Object Id for which an internal Object Id is needed.
     * @param pm PersistenceManagerInternal which requested the Object Id.
     * @param pcClass PersistenceCapable Class that corresponds to this Object Id.
     * @return An Object Id that can be given to user/client code.
     */
    public Object getInternalObjectId(Object objectId,
            PersistenceManagerInternal pm, Class pcClass) {
        // Do not try to wrap SingleFieldIdentity
        if (objectId == null || objectId instanceof SingleFieldIdentity)
            return objectId;

        if (objectId instanceof Byte) {
            return new ByteIdentity(pcClass, (Byte)objectId);
        } else if (objectId instanceof Character) {
            return new CharIdentity(pcClass, (Character)objectId);
        } else if (objectId instanceof Integer) {
            return new IntIdentity(pcClass, (Integer)objectId);
        } else if (objectId instanceof Long) {
            return new LongIdentity(pcClass, (Long)objectId);
        } else if (objectId instanceof Short) {
            return new ShortIdentity(pcClass, (Short)objectId);
        } else if (objectId instanceof String) {
            return new StringIdentity(pcClass, (String)objectId);
        } else {
            //TODO: As per disucssion with craig, when ObjectIdentity is
            // implemented, change following to
            // return new ObjectIdentity(pcClass, objecId);
            throw new UnsupportedOperationException(
                    "ObjectSingleFieldIdentity not supported");
        }
    }

    /**
     * Returns the Class of the PersistenceCapable instance identified by the
     * given oid.
     * @param oid object id whose java.lang.Class is wanted.
     * @param pm PersistenceManagerInternal to use in loading the oid's Class.
     * @return java.lang.Class of the PersistenceCapable instance identified
     * with this oid.
     */
    public Class getPCClassForOid(Object oid, PersistenceManagerInternal pm) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @inheritDoc
     */
    public boolean isMediationRequiredToCopyOid() {
        return false;
    }

    /**
     * Returns true if actual Class for a PersistenceCapable instance can be
     * resolved only in the database.
     * @param objectId Object Id whose java.lang.Class needs to be resolved.
     * @return true if the request needs to be resolved in the back end.
     */
    public boolean hasActualPCClass(Object objectId) {
        return false;
    }

    /**
     * This method returns an object id instance corresponding to the Class and
     * String arguments. The String argument might have been the result of
     * executing toString on an object id instance.
     * @param pcClass the Class of the persistence-capable instance
     * @param str the String form of the object id
     * @return an instance of the object identity class
     */
    public Object newObjectIdInstance(Class pcClass, String str) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * This method copies PK field values from internal Object Id into the
     * Hollow instance.
     * @param sm StateManagerInternal for which an operation is needed.
     * @param pcClass the Class of the persistence-capable instance
     */
    public void copyKeyFieldsFromObjectId(StateManagerInternal sm,
            Class pcClass) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //
    // Assorted other methods.
    //


    /**
     * Checks, if the passed persistence manager is the same as the associated.
     * @param pm persistence manager
     */
    private void verifyPM(PersistenceManagerInternal pm) {
        if (pm != this.pm) {
            throw new JDOFatalInternalException(msg.msg(
                    "EXC_WrongPm", pm, this.pm));  // NOI18N
        }
    }

    /**
     * Get the runtime mapping model that would be queried to get mapping
     * information related to the table and fields of the table.
     * @return runtime mapping model
     */
    private RuntimeMappingModel getRuntimeMappingModel() {
        return ((SQLPersistenceManagerFactory)
                pm.getPersistenceManagerFactory()).getRuntimeMappingModel();
    }

    /**
     * For a given StateManager get the runtime mapping class.
     * @param sm the StateManager instance for which we need to get the mapping
     * class.
     * @return the runtime mapping class associated with this StateManager
     * instance.
     */
    private RuntimeMappingClass getRuntimeMappingClass(StateManagerInternal sm) {
        String pcClassName = sm.getPCClass().getName();
        return getRuntimeMappingModel().getMappingClass(pcClassName);
    }

    /**
     * Iterate over the bitset of dirty fields and ensure that the fields are
     * added to the BaseDMLStatement. This would ensure that the correct SQL
     * statement gets generated. The fields in the dirtyFields bitset
     * correspond to the entries in the SET clause in case of the Update sql
     * statement or all the fields in the Insert sql statement.
     * @param sm the StateManager which be used later as part of double dispatch
     * to get the values to be bound to the prepared statement.
     * @param dirtyFields BitSet of changed fields that are to be flushed to the
     * database. This field then has to be added to the prepared statement.
     * @param runtimeMappingClass the runtime class that is associated to the
     * StateManager instance.
     * @param baseDMLStmt contains the sql statement which would be finally
     * executed against the database.
     * @param sqlBuffer the string buffer that is used to create the SQL
     * statement. As each field is processed the corresponding sql statement
     * component for that field is appended to this string buffer.
     */
    private void processDirtyFields(StateManagerInternal sm,
            BitSet dirtyFields, RuntimeMappingClass runtimeMappingClass,
            BaseDMLStatement baseDMLStmt, SQLBuffer sqlBuffer) {

        for (int j = dirtyFields.nextSetBit(0);  j >= 0;
            j = dirtyFields.nextSetBit(j + 1)) {

            // Now get RuntimeMappingField associated to this fieldNumber
            RuntimeMappingField mappingField =
                    runtimeMappingClass.getMappingField(j);

            addToStatement(sm, mappingField, baseDMLStmt, sqlBuffer, false);
        }
    }

    /**
     * Iterate over the bitset of loadedFields and ensure that the fields are
     * added to the BaseDMLStatement. This would ensure that the correct SQL
     * statement gets generated. The fields in the loadedFields bitset
     * correspond to the entries in the WHERE clause in case of Update or
     * Delete sql statements.
     * @param sm the StateManager which be used later as part of double dispatch
     * to get the values to be bound to the prepared statement.
     * @param loadedFields BitSet of fields to be verified against those in the
     * database.
     * @param runtimeMappingClass the runtime class that is associated to the
     * StateManager instance.
     * @param baseDMLStmt the statement that corresponds to the sql statement
     * which would be finally executed against the database.
     * @param sqlBuffer the string buffer that is used to create the SQL
     * statement. As each field is processed the corresponding sql statement
     * component for that field is appended to this string buffer.
     */
    private void processLoadedFields(StateManagerInternal sm,
            BitSet loadedFields, RuntimeMappingClass runtimeMappingClass,
            BaseDMLStatement baseDMLStmt, SQLBuffer sqlBuffer) {

        for (int j = loadedFields.nextSetBit(0);  j >= 0;
            j = loadedFields.nextSetBit(j + 1)) {

            // Now get RuntimeMappingField associated to this fieldNumber
            RuntimeMappingField mappingField =
                    runtimeMappingClass.getMappingField(j);

            if (mappingField.getJDOField().isPrimaryKey()) {
                addToStatement(sm, mappingField, baseDMLStmt, sqlBuffer, true);
            }
        }
    }

    /**
     * Add the runtime mapping field to the statement's field array. The
     * statement could be an Insert/Update/Delete statement. Also append the
     * appropriate column information to the sql statement buffer.
     * @param sm the StateManager instance which would be used to get the value
     * associated with this field when binding the values.
     * @param field the field that is to be added to the sql statement.
     * @param stmt contains the sql statement that would be eventually executed
     * against the database.
     * @param sql the string buffer that is appended with information related
     * to this field
     * @param whereClause if true the field has to be added to the WHERE clause
     * of the Update or Delete sql statement. If false the field would be part
     * of the Insert or SET clause of the Update sql statement.
     */
    private static void addToStatement(StateManagerInternal sm,
            RuntimeMappingField field, BaseDMLStatement stmt, SQLBuffer sql,
            boolean whereClause) {

        if (field.getMappingRelationship() == null) {
            addStateField(field, stmt, sql, whereClause);
        } else {
            addRelationship(field, stmt, sql, whereClause);
        }
    }

    /**
     * Adds a state field to the statement. State fields are all fields
     * except relationship and embedded fields.
     * @param field the field that is to be added to the sql statement.
     * @param stmt contains the sql statement that would be eventually executed
     * against the database.
     * @param sql the string buffer that is appended with information related
     * to this field.
     * @param whereClause if true the field has to be added to the WHERE clause
     * of the Update or Delete sql statement. If false the field would be part
     * of the Insert or SET clause of the Update sql statement.
     */
    private static void addStateField(RuntimeMappingField field,
            BaseDMLStatement stmt, SQLBuffer sql, boolean whereClause) {
        // XXX Skip version columns
        if (field.getJDOField().getJavaField() == null)
            return;

        int fieldNum = field.getJDOField().getFieldNumber();
        JavaType type = field.getJDOField().getType();
        ColumnElement columns[] = field.getColumns();

        // TODO: We assume that a state field cannot be mapped to more
        // than one column, i.e. columns.length == 1.
        for (int i = 0; i < columns.length; i++) {
            if (type.isPrimitive()) {
                stmt.addField(fieldNum, whereClause);
            } else {
                int sqlType = columns[i].getType();
                stmt.addField(fieldNum, sqlType, whereClause);
            }
            sql.appendColumn(columns[i], whereClause);
        }
    }

    /**
     * Adds a relationship or embedded field to the statement. Relationships
     * are only stored on the owning side.
     * @param field the field that is to be added to the sql statement.
     * @param stmt contains the sql statement that would be eventually executed
     * against the database.
     * @param sql the string buffer that is appended with information related
     * to this field.
     * @param whereClause if true the field has to be added to the WHERE clause
     * of the Update or Delete sql statement. If false the field would be part
     * of the Insert or SET clause of the Update sql statement.
     */
    private static void addRelationship(RuntimeMappingField field,
            BaseDMLStatement stmt, SQLBuffer sql, boolean whereClause) {
        int fieldNum = field.getJDOField().getFieldNumber();
        MappingRelationship relation = field.getMappingRelationship();
        JDORelationship jdoRelationship = relation.getJDORelationship();

        if (jdoRelationship.isOwner()) {
            if (jdoRelationship.isJDOReference()) {
                if (!relation.usesJoinTable()) {
                    // Foreign key update
                    processForeignKeyReference(fieldNum, relation,
                            jdoRelationship, stmt, sql, whereClause);
                } else {
                    // TODO: Join table update
                    throw new UnsupportedOperationException("Not yet implemented");
                }
            } else if (jdoRelationship.isJDOCollection()) {
                // Join table
                processCollection(fieldNum, relation, jdoRelationship,
                        whereClause);
            }
        }
    }

    /**
     * Stores a relationship mapped to a foreign key or an embedded field. The
     * relationship is only stored on the owning side.
     * @param fieldNum JOD absolute field number.
     * @param relation relationship mapping information.
     * @param jdoRelationship JDO relationship information.
     * @param stmt contains the sql statement that would be eventually executed
     * against the database.
     * @param sql the string buffer that is appended with information related
     * to this field.
     * @param whereClause if true the field has to be added to the WHERE clause
     * of the Update or Delete sql statement. If false the field would be part
     * of the Insert or SET clause of the Update sql statement.
     */
    private static void processForeignKeyReference(int fieldNum,
            MappingRelationship relation, JDORelationship jdoRelationship,
            BaseDMLStatement stmt, SQLBuffer sql, boolean whereClause) {

        JDOClass jdoClass = jdoRelationship.getRelatedJDOClass();
        // TODO: This information should come from MappingRelationship.
        int[] foreignFieldNums = jdoClass.getPrimaryKeyFieldNumbers();
        MappingReferenceKey refKey =  
            relation.getMappingReferenceKey(MappingRelationship.USAGE_REFERENCE);
        ColumnPairElement[] columnPairs = refKey.getColumnPairs();
        assert foreignFieldNums.length == columnPairs.length :
                "There must be exactly one field for each column"; //NOI18N

        // Create a FieldParameterBinder for foreign key fields
        NullableFieldParameterBinder fpb = new NullableFieldParameterBinder(
                whereClause, foreignFieldNums.length);

        for (int i = 0; i < foreignFieldNums.length; i++) {
            ColumnElement ce = columnPairs[i].getLocalColumn();
            // Bind one field to exactly one parameter.
            fpb.addField(foreignFieldNums[i], ce.getType());
            sql.appendColumn(ce, whereClause);
        }
        stmt.addFieldParameterBinder(fieldNum, fpb, whereClause);
    }

    /**
     * Stores the values added to/removed from a collection relationship field.
     * Collections are typically stored to a join table. Each value will create
     * a separate insert/delete statement. The field must be the owning side
     * of the relationship.
     * @param fieldNum JOD absolute field number.
     * @param relation relationship mapping information.
     * @param jdoRelationship JDO relationship information.
     * @param whereClause if true the field has to be added to the WHERE clause
     * of the Update or Delete sql statement. If false the field would be part
     * of the Insert or SET clause of the Update sql statement.
     */
    private static void processCollection(int fieldNum,
            MappingRelationship relation, JDORelationship jdoRelationship,
            boolean whereClause) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
