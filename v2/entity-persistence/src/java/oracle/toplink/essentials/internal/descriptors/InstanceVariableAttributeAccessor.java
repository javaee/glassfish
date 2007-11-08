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
package oracle.toplink.essentials.internal.descriptors;

import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.mappings.AttributeAccessor;
import oracle.toplink.essentials.internal.security.*;

/**
 * <p><b>Purpose</b>: A wrapper class for handling cases when the domain object has instance varible
 * to map to the database field.
 *
 * @author Sati
 * @since TOPLink/Java 1.0
 */
public class InstanceVariableAttributeAccessor extends AttributeAccessor {

    /** The attribute name of an object is converted to Field type to access it reflectively */
    protected transient Field attributeField;

    /**
     * Returns the class type of the attribute.
     */
    public Class getAttributeClass() {
        if (getAttributeField() == null) {
            return null;
        }

        return getAttributeType();
    }

    /**
     * Returns the value of attributeField.
     */
    protected Field getAttributeField() {
        return attributeField;
    }

    /**
     * Returns the declared type of attributeField.
     */
    public Class getAttributeType() {
        return attributeField.getType();
    }

    /**
     * Returns the value of the attribute on the specified object.
     */
    public Object getAttributeValueFromObject(Object anObject) throws DescriptorException {
        try {
            // PERF: Direct variable access.
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return AccessController.doPrivileged(new PrivilegedGetValueFromField(this.attributeField, anObject));
                } catch (PrivilegedActionException exception) {
                    throw DescriptorException.illegalAccesstWhileGettingValueThruInstanceVaraibleAccessor(getAttributeName(), anObject.getClass().getName(), exception.getException());
                }
            } else {
                return oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getValueFromField(this.attributeField, anObject);
            }
        } catch (IllegalArgumentException exception) {
            throw DescriptorException.illegalArgumentWhileGettingValueThruInstanceVariableAccessor(getAttributeName(), getAttributeType().getName(), anObject.getClass().getName(), exception);
        } catch (IllegalAccessException exception) {
            throw DescriptorException.illegalAccesstWhileGettingValueThruInstanceVaraibleAccessor(getAttributeName(), anObject.getClass().getName(), exception);
        } catch (NullPointerException exception) {
            String className = null;
            if (anObject != null) {
                // Some JVM's throw this exception for some very odd reason
                className = anObject.getClass().getName();
            }
            throw DescriptorException.nullPointerWhileGettingValueThruInstanceVariableAccessor(getAttributeName(), className, exception);
        }
    }

    /**
     * instanceVariableName is converted to Field type.
     */
    public void initializeAttributes(Class theJavaClass) throws DescriptorException {
        if (getAttributeName() == null) {
            throw DescriptorException.attributeNameNotSpecified();
        }
        try {
            setAttributeField(Helper.getField(theJavaClass, getAttributeName()));
        } catch (NoSuchFieldException exception) {
            throw DescriptorException.noSuchFieldWhileInitializingAttributesInInstanceVariableAccessor(getAttributeName(), theJavaClass.getName(), exception);
        } catch (SecurityException exception) {
            throw DescriptorException.securityWhileInitializingAttributesInInstanceVariableAccessor(getAttributeName(), theJavaClass.getName(), exception);
        }
    }

    /**
     * Sets the value of the attributeField.
     */
    protected void setAttributeField(Field field) {
        attributeField = field;
    }

    /**
     * Sets the value of the instance variable in the object to the value.
     */
    public void setAttributeValueInObject(Object anObject, Object value) throws DescriptorException {
        DescriptorException descriptorException;

        try {
            // PERF: Direct variable access.
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    AccessController.doPrivileged(new PrivilegedSetValueInField(this.attributeField, anObject, value));
                } catch (PrivilegedActionException exception) {
                    throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), value, exception.getException());
                }
            } else {
                oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.setValueInField(this.attributeField, anObject, value);
            }
        } catch (IllegalArgumentException exception) {
            // This is done to overcome VA Java bug because VA Java does not allow null to be set reflectively.
            try {
                // This is done to overcome VA Java bug because VA Java does not allow null to be set reflectively.
                // Bug2910086 In JDK1.4, IllegalArgumentException is thrown if value is null.
                // TODO: This code should be removed, it should not be required and may cause unwanted sideeffects.
                if (value == null) {
                    // cr 3737  If a null pointer was thrown because toplink attempted to set a null referece into a
                    // primitive create a primitive of value 0 to set in the object.
                    Class fieldClass = getAttributeClass();
                    if (oracle.toplink.essentials.internal.helper.Helper.isPrimitiveWrapper(fieldClass)) {
                        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                            try {
                                AccessController.doPrivileged(new PrivilegedSetValueInField(this.attributeField, anObject, ConversionManager.getDefaultManager().convertObject(new Integer(0), fieldClass)));
                            } catch (PrivilegedActionException exc) {
                                throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), value, exc.getException());
                                                        }
                        } else {
                            oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.setValueInField(this.attributeField, anObject, ConversionManager.getDefaultManager().convertObject(new Integer(0), fieldClass));
                        }
                    }
                    return;
                }
            } catch (IllegalAccessException accessException) {
                throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), value, exception);
            }

            // TODO: This code should be removed, it should not be required and may cause unwanted sideeffects.
            // Allow XML change set to merge correctly since new value in XML change set is always String
            try {
                if (value instanceof String) {
                    Object newValue = ConversionManager.getDefaultManager().convertObject(value, getAttributeClass());
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try {
                            AccessController.doPrivileged(new PrivilegedSetValueInField(this.attributeField, anObject, newValue));
                        } catch (PrivilegedActionException exc) {
                        }
                    } else {
                        oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.setValueInField(this.attributeField, anObject, newValue);
                    }
                    return;
                }
            } catch (Exception e) {
                // Do nothing and move on to throw the original exception
            }
            throw DescriptorException.illegalArgumentWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), getAttributeType().getName(), value, exception);
        } catch (IllegalAccessException exception) {
            if (value == null) {
                return;
            }
            throw DescriptorException.illegalAccessWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), anObject.getClass().getName(), value, exception);
        } catch (NullPointerException exception) {
            try {
                // TODO: This code should be removed, it should not be required and may cause unwanted sideeffects.
                //Bug2910086 In JDK1.3, NullPointerException is thrown if value is null.  Add a null pointer check so that the TopLink exception is thrown if anObject is null.
                if (anObject != null) {
                    // cr 3737  If a null pointer was thrown because toplink attempted to set a null referece into a
                    // primitive create a primitive of value 0 to set in the object.
                    Class fieldClass = getAttributeClass();
                    if (oracle.toplink.essentials.internal.helper.Helper.isPrimitiveWrapper(fieldClass) && (value == null)) {
                        if (oracle.toplink.essentials.internal.helper.Helper.isPrimitiveWrapper(fieldClass)) {
                            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                                try {
                                    AccessController.doPrivileged(new PrivilegedSetValueInField(this.attributeField, anObject, ConversionManager.getDefaultManager().convertObject(new Integer(0), fieldClass)));
                                } catch (PrivilegedActionException exc) {
                                    throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), value, exc.getException());
                                }
                            } else {
                                oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.setValueInField(this.attributeField, anObject, ConversionManager.getDefaultManager().convertObject(new Integer(0), fieldClass));
                            }
                        }
                    } else {
                        throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), value, exception);
                    }
                } else {
                    // Some JVM's throw this exception for some very odd reason
                    throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), value, exception);
                }
            } catch (IllegalAccessException accessException) {
                throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), value, exception);
            }
        }
    }
}
