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

import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.state.FieldManager;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingClass;
import com.sun.persistence.support.spi.PersistenceCapable.ObjectIdFieldSupplier;

import java.sql.ResultSet;

/**
 * ResultElement for fetching a Managed Object like Entity or Embedded Object
 * @author Mitesh Meswani
 */
public abstract class ObjectResultElement extends ResultElementImpl {
    protected RuntimeMappingClass entityType;

    /* ---- Cached derived information from data provided to constructor ---- */

    /**
     * Field nos of stateFields for this object
     */
    protected int[] stateFieldNos;

    /**
     * ResultElement to parse stateFields for this object
     */
    protected StateFieldResultElement[] stateFields;

    /**
     * Construct ResultElement that parses result for an Object
     * @param entityType The <code>RuntimeMappingClass</code> of the entity
     * @param resultFields result elements corresponding to state fields
     * @param sqlText sqlText for select clause
     */
    protected ObjectResultElement(RuntimeMappingClass entityType,
            StateFieldResultElement[] resultFields, String sqlText) {
        super(sqlText);
        this.entityType = entityType;

        // Calculate the derived information and cache it.
        stateFieldNos = getFieldNumbers(resultFields);
        this.stateFields = getResultFieldArrayForDoubleDispatch(resultFields);

    }

    /**
     * Extract field numbers from given array of <code>StateFieldResultElement</code>
     * @param fields The given array of <code>StateFieldResultElements</code>
     * @return field numbers that corresponds to given array of
     *         <code>StateFieldResultElement</code>
     */
    protected static int[] getFieldNumbers(StateFieldResultElement[] fields) {
        int[] fieldNos = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            StateFieldResultElement field = fields[i];
            fieldNos[i] = field.getJDOFieldNumber();
        }
        return fieldNos;
    }

    /**
     * For double dispatch to work, we need to have an array of <code>
     * StateFieldResultElement</code> that has a StateFieldResultElement at
     * index that corresponds to its field number. ( see StateFieldSupplier
     * fetch<xxx>Field(int) ) for details on why this is required. This method
     * takes the given array  <code>inputResultFields</code> and constructs a
     * sparse array that satisfies the above condition.
     * @param inputResultFields The given input array
     * @return A Sparse array of StateFieldResultElement that has a given
     *         element at index that corresponds to its field number
     */
    protected StateFieldResultElement[] getResultFieldArrayForDoubleDispatch(
            StateFieldResultElement[] inputResultFields) {
        // Allocate an array of size max field number from the supplied
        // fieldNos.
        // For now use max field no of entity (getManagedFieldCount() )
        JDOClass jdoClass = entityType.getJDOClass();
        StateFieldResultElement[] resultFields =
                new StateFieldResultElement[jdoClass.getManagedFieldCount()];
        for (StateFieldResultElement resultElement : inputResultFields) {
            resultFields[resultElement.getJDOFieldNumber()] = resultElement;
        }
        return resultFields;
    }

    /**
     * Field Supplier to facilitate the double dispatch for state fields and id
     * fields. An instance of this class will be created per state manager that
     * needs to be populated
     */
    protected static class StateFieldSupplier implements FieldManager,
            ObjectIdFieldSupplier {

        private ResultSet rs;
        private int[] stateFieldNos;
        // Can be a sparse array
        private StateFieldResultElement[] stateFieldResultElements;

        StateFieldSupplier(ResultSet rs, int[] stateFieldNos,
                ResultElement[] resultFields) {
            this.rs = rs;
            this.stateFieldNos = stateFieldNos;
            this.stateFieldResultElements =
                    (StateFieldResultElement[]) resultFields;
        }

        /**
         * @inheritDoc
         */
        public void storeBooleanField(int fieldNum, boolean value) {
            throw new UnsupportedOperationException();
        }

        /**
         * @inheritDoc
         */
        public boolean fetchBooleanField(int fieldNum) {
            return stateFieldResultElements[fieldNum].getBooleanResult(rs);
        }

        /**
         * @inheritDoc
         */
        public void storeCharField(int fieldNum, char value) {
            throw new UnsupportedOperationException();
        }

        /**
         * @inheritDoc
         */
        public char fetchCharField(int fieldNum) {
            return stateFieldResultElements[fieldNum].getCharResult(rs);
        }

        /**
         * @inheritDoc
         */
        public void storeByteField(int fieldNum, byte value) {
            throw new UnsupportedOperationException();
        }

        /**
         * @inheritDoc
         */
        public byte fetchByteField(int fieldNum) {
            return stateFieldResultElements[fieldNum].getByteResult(rs);
        }

        /**
         * @inheritDoc
         */
        public void storeShortField(int fieldNum, short value) {
            throw new UnsupportedOperationException();
        }

        /**
         * @inheritDoc
         */
        public short fetchShortField(int fieldNum) {
            return stateFieldResultElements[fieldNum].getShortResult(rs);
        }

        /**
         * @inheritDoc
         */
        public void storeIntField(int fieldNum, int value) {
            throw new UnsupportedOperationException();
        }

        /**
         * @inheritDoc
         */
        public int fetchIntField(int fieldNum) {
            return stateFieldResultElements[fieldNum].getIntResult(rs);
        }

        /**
         * @inheritDoc
         */
        public void storeLongField(int fieldNum, long value) {
            throw new UnsupportedOperationException();
        }

        /**
         * @inheritDoc
         */
        public long fetchLongField(int fieldNum) {
            return stateFieldResultElements[fieldNum].getLongResult(rs);
        }

        /**
         * @inheritDoc
         */
        public void storeFloatField(int fieldNum, float value) {
            throw new UnsupportedOperationException();
        }

        /**
         * @inheritDoc
         */
        public float fetchFloatField(int fieldNum) {
            return stateFieldResultElements[fieldNum].getFloatResult(rs);
        }

        /**
         * @inheritDoc
         */
        public void storeDoubleField(int fieldNum, double value) {
            throw new UnsupportedOperationException();
        }

        /**
         * @inheritDoc
         */
        public double fetchDoubleField(int fieldNum) {
            return stateFieldResultElements[fieldNum].getDoubleResult(rs);
        }

        /**
         * @inheritDoc
         */
        public void storeStringField(int fieldNum, String value) {
            throw new UnsupportedOperationException();
        }

        /**
         * @inheritDoc
         */
        public String fetchStringField(int fieldNum) {
            return stateFieldResultElements[fieldNum].getStringResult(rs);
        }

        /**
         * @inheritDoc
         */
        public void storeObjectField(int fieldNum, Object value) {
            throw new UnsupportedOperationException();
        }

        /**
         * @inheritDoc
         */
        public Object fetchObjectField(int fieldNum) {
            return stateFieldResultElements[fieldNum].getObjectResult(rs);
        }

        /**
         * Set values from resultset into given sm
         */
        public void setFieldValues(StateManagerInternal sm) {
            sm.replaceFields(stateFieldNos, this);
        }
    }

}
