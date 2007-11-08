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
import java.util.Set;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.mappings.AttributeAccessor;
import oracle.toplink.essentials.internal.security.*;

/**
 * <p><b>Purpose</b>: A wrapper class for handling cases when the domain object attributes are
 * to be accessed thru the accessor methods. This could happen if the variables are not defined
 * public in the domain object.
 *
 * @author Sati
 * @since TOPLink/Java 1.0
 */
public class MethodAttributeAccessor extends AttributeAccessor {
    protected String setMethodName;
    protected String getMethodName;
    protected transient Method setMethod;
    protected transient Method getMethod;

    /**
     * Return the return type of the method accessor.
     */
    public Class getAttributeClass() {
        if (getGetMethod() == null) {
            return null;
        }

        return getGetMethodReturnType();
    }

    /**
     * Gets the value of an instance variable in the object.
     */
    public Object getAttributeValueFromObject(Object anObject) throws DescriptorException {
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return AccessController.doPrivileged(new PrivilegedMethodInvoker(getGetMethod(), anObject, (Object[])null));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof IllegalAccessException) {
                        throw DescriptorException.illegalAccessWhileGettingValueThruMethodAccessor(getGetMethodName(), anObject.getClass().getName(), throwableException);
                    } else {
                        throw DescriptorException.targetInvocationWhileGettingValueThruMethodAccessor(getGetMethodName(), anObject.getClass().getName(), throwableException);
                     }
                }
            } else {
                return PrivilegedAccessHelper.invokeMethod(getGetMethod(), anObject, (Object[])null);
            }
        } catch (IllegalArgumentException exception) {
            throw DescriptorException.illegalArgumentWhileGettingValueThruMethodAccessor(getGetMethodName(), anObject.getClass().getName(), exception);
        } catch (IllegalAccessException exception) {
            throw DescriptorException.illegalAccessWhileGettingValueThruMethodAccessor(getGetMethodName(), anObject.getClass().getName(), exception);
        } catch (InvocationTargetException exception) {
            throw DescriptorException.targetInvocationWhileGettingValueThruMethodAccessor(getGetMethodName(), anObject.getClass().getName(), exception);
        } catch (NullPointerException exception) {
            // Some JVM's throw this exception for some very odd reason
            throw DescriptorException.nullPointerWhileGettingValueThruMethodAccessor(getGetMethodName(), anObject.getClass().getName(), exception);
        }
    }

    /**
     * Return the accessor method for the attribute accessor.
     */
    protected Method getGetMethod() {
        return getMethod;
    }

    /**
     * Return the name of theh accessor method for the attribute accessor.
     */
    public String getGetMethodName() {
        return getMethodName;
    }

    public Class getGetMethodReturnType() {
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            try {
                return (Class)AccessController.doPrivileged(new PrivilegedGetMethodReturnType(getGetMethod()));
            } catch (PrivilegedActionException exception) {
                // we should not get here since this call does not throw any checked exceptions
                return null;
            }
        } else {
            return PrivilegedAccessHelper.getMethodReturnType(getGetMethod());
        }
    }

    /**
     * Return the set method for the attribute accessor.
     */
    protected Method getSetMethod() {
        return setMethod;
    }

    /**
     * Return the name of the set method for the attribute accessor.
     */
    public String getSetMethodName() {
        return setMethodName;
    }

    public Class getSetMethodParameterType() {
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
            try {
                return ((Class[])AccessController.doPrivileged(new PrivilegedGetMethodParameterTypes(getSetMethod())))[0];
            } catch (PrivilegedActionException exception) {
                // we should not get here since this call does not throw any checked exceptions
                return null;
            }
        } else {
            return PrivilegedAccessHelper.getMethodParameterTypes(getSetMethod())[0];
        }
    }

    /**
     * Set get and set method after creating these methods by using
     * get and set method names
     */
    public void initializeAttributes(Class theJavaClass) throws DescriptorException {
        if (getAttributeName() == null) {
            throw DescriptorException.attributeNameNotSpecified();
        }
        try {
            setGetMethod(Helper.getDeclaredMethod(theJavaClass, getGetMethodName(), (Class[])null));

            // The parameter type for the set method must always be the return type of the get method.
            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = getGetMethodReturnType();
            setSetMethod(Helper.getDeclaredMethod(theJavaClass, getSetMethodName(), parameterTypes));
        } catch (NoSuchMethodException ex) {
            DescriptorException descriptorException = DescriptorException.noSuchMethodWhileInitializingAttributesInMethodAccessor(getSetMethodName(), getGetMethodName(), theJavaClass.getName());
            descriptorException.setInternalException(ex);
            throw descriptorException;
        } catch (SecurityException exception) {
            DescriptorException descriptorException = DescriptorException.securityWhileInitializingAttributesInMethodAccessor(getSetMethodName(), getGetMethodName(), theJavaClass.getName());
            descriptorException.setInternalException(exception);
            throw descriptorException;
        }
    }

    public boolean isMethodAttributeAccessor() {
        return true;
    }

    /**
     * Sets the value of the instance variable in the object to the value.
     */
    public void setAttributeValueInObject(Object domainObject, Object attributeValue) throws DescriptorException {
        Object[] parameters = new Object[1];
        parameters[0] = attributeValue;
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    AccessController.doPrivileged(new PrivilegedMethodInvoker(getSetMethod(), domainObject, parameters));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof IllegalAccessException) {
                        throw DescriptorException.illegalAccessWhileSettingValueThruMethodAccessor(getSetMethodName(), attributeValue, throwableException);
                    } else {
                        throw DescriptorException.targetInvocationWhileSettingValueThruMethodAccessor(getSetMethodName(), attributeValue, throwableException);
                     }
                }
            } else {
                PrivilegedAccessHelper.invokeMethod(getSetMethod(), domainObject, parameters);
            }
        } catch (IllegalAccessException exception) {
            throw DescriptorException.illegalAccessWhileSettingValueThruMethodAccessor(getSetMethodName(), attributeValue, exception);
        } catch (IllegalArgumentException exception) {
            // TODO: This code should be removed, it should not be required and may cause unwanted sideeffects.
            // Allow XML change set to merge correctly since new value in XML change set is always String
            try {
                if (attributeValue instanceof String) {
                    Object newValue = ConversionManager.getDefaultManager().convertObject(attributeValue, getAttributeClass());
                    Object[] newParameters = new Object[1];
                    newParameters[0] = newValue;
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try {
                            AccessController.doPrivileged(new PrivilegedMethodInvoker(getSetMethod(), domainObject, newParameters));
                        } catch (PrivilegedActionException exc) {
                            // Do nothing and move on to throw the original exception
                        }
                    } else {
                        PrivilegedAccessHelper.invokeMethod(getSetMethod(), domainObject, newParameters);
                    }
                    return;
                }
            } catch (Exception e) {
                // Do nothing and move on to throw the original exception
            }
            throw DescriptorException.illegalArgumentWhileSettingValueThruMethodAccessor(getSetMethodName(), attributeValue, exception);
        } catch (InvocationTargetException exception) {
            throw DescriptorException.targetInvocationWhileSettingValueThruMethodAccessor(getSetMethodName(), attributeValue, exception);
        } catch (NullPointerException exception) {
            try {
                // TODO: This code should be removed, it should not be required and may cause unwanted sideeffects.
                // cr 3737  If a null pointer was thrown because toplink attempted to set a null referece into a
                // primitive create a primitive of value 0 to set in the object.
                // Is this really the best place for this? is this not why we have null-value and conversion-manager?
                Class fieldClass = getSetMethodParameterType();

                //Found when fixing Bug2910086
                if (fieldClass.isPrimitive() && (attributeValue == null)) {
                    parameters[0] = ConversionManager.getDefaultManager().convertObject(new Integer(0), fieldClass);
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try {
                            AccessController.doPrivileged(new PrivilegedMethodInvoker(getSetMethod(), domainObject, parameters));
                        } catch (PrivilegedActionException exc) {
                            Exception throwableException = exc.getException();
                            if (throwableException instanceof IllegalAccessException) {
                                throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), attributeValue, throwableException);
                            } else {
                                throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), attributeValue, throwableException);
                             }
                        }
                    } else {
                        PrivilegedAccessHelper.invokeMethod(getSetMethod(), domainObject, parameters);
                    }
                } else {
                    // Some JVM's throw this exception for some very odd reason
                    throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), attributeValue, exception);
                }
            } catch (IllegalAccessException accessException) {
                throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), attributeValue, exception);
            } catch (InvocationTargetException invocationException) {
                throw DescriptorException.nullPointerWhileSettingValueThruInstanceVariableAccessor(getAttributeName(), attributeValue, exception);
            }
        }
    }

    /**
     * Set the accessor method for the attribute accessor.
     */
    protected void setGetMethod(Method getMethod) {
        this.getMethod = getMethod;
    }

    /**
     * Set the name of the accessor method for the attribute accessor.
     */
    public void setGetMethodName(String getMethodName) {
        this.getMethodName = getMethodName;
    }

    /**
     * Set the set method for the attribute accessor.
     */
    protected void setSetMethod(Method setMethod) {
        this.setMethod = setMethod;
    }

    /**
     * Set the name of the set method for the attribute accessor.
     */
    public void setSetMethodName(String setMethodName) {
        this.setMethodName = setMethodName;
    }
}
