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


package com.sun.persistence.runtime.sqlstore.sql.impl;

import com.sun.persistence.runtime.model.mapping.RuntimeMappingField;
import com.sun.persistence.runtime.query.QueryInternal;
import com.sun.persistence.support.spi.PersistenceCapable;
import com.sun.persistence.support.JDOUserException;
import com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaType;

/**
 * User parameter that correpods to an entity. Please note that there will be
 * one instance of this paramater for each '?' generated in sql. Thus for
 * entities mapped to composite pk, there will be 'n' instance of this class
 * one for each field of the pk
 * @author Mitesh Meswani
 */
public class EntityUserInputParameter extends UserInputParameter {

    RuntimeMappingField field;

    /**
     * Construct an instance of EntityUserParam that corresponds to the given
     * field and given positionMarker in the user query.
     * @param positionMarker The positionMarker for this parameter in user query.
     * @param field The pkField that this parameter corresponds to.
     */
    public EntityUserInputParameter(String positionMarker, RuntimeMappingField field) {
        super(positionMarker);
        this.field = field;
    }

    /**
     * @inheritDoc
     */
    public Object getValue(QueryInternal userParams) {
        Object userValue = super.getValue(userParams);
        checkParameterType(userValue);
        PersistenceCapable pc = (PersistenceCapable) userValue;
        // The userValue is of type entity. We need to extract pk value that
        // corresponds to fieldNumber of field for this instance.
        // The approach is to get a copy of oid from the pc using jdoNewObjectIdInstance
        // and then initate double dispatch using jdoCopyKeyFieldsFromObjectId
        // supplied fieldConsumer will contain value of the field
        Object oid = pc.jdoNewObjectIdInstance();
        int fieldNumber = field.getJDOField().getFieldNumber();
        ParameterFieldConsumer fieldConsumer =
                new ParameterFieldConsumer(fieldNumber);
        pc.jdoCopyKeyFieldsFromObjectId(fieldConsumer, oid);

        return fieldConsumer.getValue();
    }

    /**
     * Checks if the parameter passed by the user is of same type as declaring
     * class of field
     * @param userValue
     */
    private void checkParameterType(Object userValue) {
        //The pc that user gives should be of same type as we are expecting
        Class expectedClass = ((RuntimeJavaType) field.getJDOField().getDeclaringClass().getJavaType() )
                .getJavaClass();
        Class userClass = userValue.getClass();
        if (!expectedClass.equals(userClass)) {
            //Meesage need to come from bundle
            throw new JDOUserException("The expected class of parameter " +
                    positionMarker + " is " + expectedClass + " Got: " + userClass);
        }
    }

    /**
     * Helper for double dispatch
     */
    private static class ParameterFieldConsumer implements
            PersistenceCapable.ObjectIdFieldConsumer {

        /**
         * Value for given <code>fieldNumber</code>
         */
        Object value = null;

        /**
         * The field number whose value is rememberd by this instance
         */
        int fieldNumber;

        ParameterFieldConsumer(int fieldNumber) {
            this.fieldNumber = fieldNumber;
        }

        public Object getValue() { return value; }

        public void storeBooleanField(int fieldNumber, boolean value) {
            if (fieldNumber == this.fieldNumber) {
                this.value = Boolean.valueOf(value);
            }
        }

        public void storeCharField(int fieldNumber, char value) {
            if (fieldNumber == this.fieldNumber) {
                this.value = Character.valueOf(value);
            }
        }

        public void storeByteField(int fieldNumber, byte value) {
            if (fieldNumber == this.fieldNumber) {
                this.value = Byte.valueOf(value);
            }
        }

        public void storeShortField(int fieldNumber, short value) {
            if (fieldNumber == this.fieldNumber) {
                this.value = Short.valueOf(value);
            }
        }

        public void storeIntField(int fieldNumber, int value) {
            if (fieldNumber == this.fieldNumber) {
                this.value = Integer.valueOf(value);
            }
        }

        public void storeLongField(int fieldNumber, long value) {
            if (fieldNumber == this.fieldNumber) {
                this.value = Long.valueOf(value);
            }
        }

        public void storeFloatField(int fieldNumber, float value) {
            if (fieldNumber == this.fieldNumber) {
                this.value = Float.valueOf(value);
            }
        }

        public void storeDoubleField(int fieldNumber, double value) {
            if (fieldNumber == this.fieldNumber) {
                this.value = Double.valueOf(value);
            }
        }

        public void storeStringField(int fieldNumber, String value) {
            if (fieldNumber == this.fieldNumber) {
                this.value = value;
            }
        }

        public void storeObjectField(int fieldNumber, Object value) {
            if (fieldNumber == this.fieldNumber) {
                this.value = value;
            }
        }
    }
}
