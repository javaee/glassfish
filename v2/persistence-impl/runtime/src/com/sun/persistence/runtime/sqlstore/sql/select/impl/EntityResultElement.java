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


package com.sun.persistence.runtime.sqlstore.sql.select.impl;

import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingClass;
import com.sun.persistence.support.spi.JDOImplHelper;
import com.sun.persistence.support.spi.PersistenceCapable;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.ResultSet;

/**
 * ResultElement for fetching an entity
 * @author Mitesh Meswani
 */
public class EntityResultElement extends ObjectResultElement {
    /**
     * Field nos of idFields
     */
    private int[] idFieldNos;

    /**
     * ResultElment to parse idFields
     */
    private ResultElement[] idFields;

    //TODO: Talk with Michael Rochelle about RuntimeMappingRelationship
    //private RuntimeMappingRelationship parentField;
    //private ResultElement[] eagerFetchedEmbeddedFieldsResultElments;
    //private ResultElement[] eagerFetchedRelationshipFieldsResultElments;

    private static final JDOImplHelper jdoImplHelper = 
        (JDOImplHelper) AccessController.doPrivileged (
            // Need to have privileges to perform JDOImplHelper.getInstance().
            new PrivilegedAction () {
                public Object run () {
                    try {
                        return JDOImplHelper.getInstance();
                    }    
                    catch (SecurityException e) {
                        throw new JDOFatalInternalException (
                            // TBD: I18N for message, please have a look at
                            // EXC_CannotGetJDOImplHelper in Bundle
                            // com.sun.org.apache.jdo.impl.state
                            "Cannot get JDOImplHelper", e); //NOI18N
                    }
                }
            }    
            );

    /**
     * Constructs a ResultElement that parses result for an entity
     * @param mappingClass The <code>RuntimeMappingClass</code> of the entity
     * @param idResultElements result elements corresponding to id fields
     * @param eagerFetchedStateFieldsResultElements result elements for eagerly
     * fetched fields
     * @param sqlText sqlText for select clause
     */
    public EntityResultElement(RuntimeMappingClass mappingClass,
            StateFieldResultElement[] idResultElements,
            StateFieldResultElement[] eagerFetchedStateFieldsResultElements,
            String sqlText) {
        super(mappingClass, eagerFetchedStateFieldsResultElements, sqlText);

        // Calculate the derived information and cache it.
        idFieldNos = getFieldNumbers(idResultElements);
        this.idFields = getResultFieldArrayForDoubleDispatch(idResultElements);
    }

    /**
     * Fetch instance of entity from the given <code>rs</code>. The fetched
     * entity is bound to the given <code>pm</code>
     * @param pm The given PersistenceManager
     * @param rs The given Resultset
     * @return Instance of entity from the current row of the resultset
     */
    public PersistenceCapable getResult(PersistenceManagerInternal pm,
            ResultSet rs) {
        Class objectClass = getEntityClass(rs);
        Object oid = createOid(objectClass, rs);
        StateManagerInternal sm = findOrCreateSM(pm, oid, objectClass);

        //Fetch the state fields
        StateFieldSupplier stateFieldSupplier = new StateFieldSupplier(
                rs, stateFieldNos, stateFields);
        stateFieldSupplier.setFieldValues(sm);

        //Fetch the embedded state fields

        //Fetch prefetched relationship fields

        return sm.getObject();
    }

    /**
     * Create instance of Oid for given objectClass from given resultSet.
     */
    private Object createOid(Class objectClass, ResultSet rs) {
        StateFieldSupplier ofs = new StateFieldSupplier(
                rs, idFieldNos, idFields);
        Object oid = jdoImplHelper.newObjectIdInstance(objectClass, ofs);
        return oid;
    }

    /**
     * Find or create sm for the given object type and idFieldValues
     */
    private StateManagerInternal findOrCreateSM(PersistenceManagerInternal pm,
            Object oid, Class objectClass) {
        return pm.getStateManager(oid, objectClass);
    }

    /**
     * Get java class for the entity. ResultElement corresponding to polymorphic
     * queries should override this method and read appropriate values from the
     * resultset to determine the entity class
     * @param rs The given ResultSet
     * @return Entity Class
     */
    public Class getEntityClass(ResultSet rs) {
        return entityType.getJavaClass();
    }

}

