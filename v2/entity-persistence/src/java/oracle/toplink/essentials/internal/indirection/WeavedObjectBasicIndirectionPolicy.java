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

package oracle.toplink.essentials.internal.indirection;

import oracle.toplink.essentials.exceptions.DescriptorException;
import oracle.toplink.essentials.indirection.ValueHolder;
import oracle.toplink.essentials.indirection.ValueHolderInterface;
import oracle.toplink.essentials.indirection.WeavedAttributeValueHolderInterface;
import oracle.toplink.essentials.internal.helper.ClassConstants;
import oracle.toplink.essentials.internal.helper.ConversionManager;
import oracle.toplink.essentials.internal.helper.Helper;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.mappings.ForeignReferenceMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;

/**
 * INTERNAL:
 * A WeavedObjectBasicIndirectionPolicy is used by OneToOne mappings that are LAZY through weaving
 * and which use Property(method) access.
 * 
 * It extends BasicIndirection by providing the capability of calling the set method that was initially
 * mapped in addition to the set method for the weaved valueholder in order to coordinate the value of the
 * underlying property with the value stored in the valueholder
 * 
 * @author Tom Ware
 *
 */
public class WeavedObjectBasicIndirectionPolicy extends BasicIndirectionPolicy {  
    
    protected String setMethodName = null; // name of the initial set method.
    protected Method setMethod = null; // lazily initialized set method based on the set method name
    
    public WeavedObjectBasicIndirectionPolicy(String setMethodName) {
        super();
        this.setMethodName = setMethodName;
    }    
    
    /**
     * INTERNAL:
     * Return the "real" attribute value, as opposed to any wrapper.
     * This will trigger the wrapper to instantiate the value. In a weaved policy, this will
     * also call the initial setter method to coordinate the values of the valueholder with
     * the underlying data
     * 
     */
    public Object getRealAttributeValueFromObject(Object object, Object attribute) {
        Object value = super.getRealAttributeValueFromObject(object, attribute);
        // Provide the indirection policy with a callback that allows it to do any updates it needs as the result of getting the value
        updateValueInObject(object, value, attribute);
        return value;
    }
    
    /**
     * This method will lazily initialize the set method
     * Lazy initialization occurs to that we are not required to have a handle on
     * the actual class that we are using until runtime.  This helps to satisfy the 
     * weaving requirement that demands that we avoid loading domain classes into
     * the main class loader until after weaving occurs.
     * @return
     */
    protected Method getSetMethod(){
        if (setMethod == null){
            ForeignReferenceMapping sourceMapping = (ForeignReferenceMapping)mapping;
            // The parameter type for the set method must always be the return type of the get method.
            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = sourceMapping.getReferenceClass();
            try {
                setMethod = Helper.getDeclaredMethod(sourceMapping.getDescriptor().getJavaClass(), setMethodName, parameterTypes);
            } catch (NoSuchMethodException e){
                throw DescriptorException.errorAccessingSetMethodOfEntity(sourceMapping.getDescriptor().getJavaClass(), setMethodName ,sourceMapping.getDescriptor(), e);
            }
        }
        return setMethod;
    }

    
    /**
     * Coordinate the valueholder for this mapping with the underlying property by calling the
     * initial setter method
     */
    public void updateValueInObject(Object object, Object value, Object attributeValue){
        setRealAttributeValueInObject(object, value);
        ((WeavedAttributeValueHolderInterface)attributeValue).setIsCoordinatedWithProperty(true);
    }
    
    /**
     * INTERNAL:
     * Set the value of the appropriate attribute of target to attributeValue.
     * In this case, place the value inside the target's ValueHolder.
     */
    public void setRealAttributeValueInObject(Object target, Object attributeValue) {
        Object[] parameters = new Object[1];
        parameters[0] = attributeValue;
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    AccessController.doPrivileged(new PrivilegedMethodInvoker(getSetMethod(), target, parameters));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof IllegalAccessException) {
                        throw DescriptorException.illegalAccessWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, throwableException);
                    } else {
                        throw DescriptorException.targetInvocationWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, throwableException);
                     }
                }
            } else {
                PrivilegedAccessHelper.invokeMethod(getSetMethod(), target, parameters);
            }
        } catch (IllegalAccessException exception) {
            throw DescriptorException.illegalAccessWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, exception);
        } catch (IllegalArgumentException exception) {
              throw DescriptorException.illegalArgumentWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, exception);
        } catch (InvocationTargetException exception) {
            throw DescriptorException.targetInvocationWhileSettingValueThruMethodAccessor(setMethod.getName(), attributeValue, exception);
        }
    }  
}
