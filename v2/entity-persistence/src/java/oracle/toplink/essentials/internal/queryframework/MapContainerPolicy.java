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
package oracle.toplink.essentials.internal.queryframework;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.lang.reflect.*;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.ejb.cmp3.base.CMP3Policy;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.queryframework.DatabaseQuery;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;
import oracle.toplink.essentials.internal.security.PrivilegedMethodInvoker;
import oracle.toplink.essentials.internal.security.PrivilegedGetValueFromField;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: A MapContainerPolicy is ContainerPolicy whose container 
 * class implements the Map interface.
 * <p>
 * <p><b>Responsibilities</b>:
 * Provide the functionality to operate on an instance of a Map.
 *
 * @see ContainerPolicy
 * @see CollectionContainerPolicy
 */
public class MapContainerPolicy extends InterfaceContainerPolicy {
    protected String keyName;
    protected String elementClassName;
    protected Class elementClass;
    protected transient Field keyField;
    protected transient Method keyMethod;

    /**
     * INTERNAL:
     * Construct a new policy.
     */
    public MapContainerPolicy() {
        super();
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class.
     */
    public MapContainerPolicy(Class containerClass) {
        super(containerClass);
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class name.
     */
    public MapContainerPolicy(String containerClassName) {
        super(containerClassName);
    }

    /**
     * Prepare and validate.
     * Set the element class.
     */
    public void prepare(DatabaseQuery query, AbstractSession session) throws QueryException {
        if ((getElementClass() == null) && (query.getDescriptor() != null)) {
            setElementClass(query.getDescriptor().getJavaClass());
        }
        
        super.prepare(query, session);
    }

    /**
     * INTERNAL:
     * Add element into container which implements the Map interface.
     */
    public boolean addInto(Object key, Object element, Object container, AbstractSession session) {
        Object wrapped = element;
        
        if (hasElementDescriptor()) {
            wrapped = getElementDescriptor().getObjectBuilder().wrapObject(element, session);
        }
        
        try {
            if (key != null) {
                return ((Map) container).put(key, wrapped) != null;
            } else {
                return ((Map) container).put(keyFrom(element, session), wrapped) != null;
            }
        } catch (ClassCastException ex1) {
            throw QueryException.mapKeyNotComparable(element, container);
        }
    }

    /**
     * INTERNAL:
     * Remove all the elements from container.
     */
    public void clear(Object container) {
        try {
            ((Map)container).clear();
        } catch (UnsupportedOperationException ex) {
            throw QueryException.methodNotValid(container, "clear()");
        }
    }

    /**
     * INTERNAL:
     * Return true if keys are the same in the source as the backup.  False otherwise
     * in the case of readonly compare against the original
     */
    public boolean compareKeys(Object sourceValue, AbstractSession session) {
        Object backUpVersion = null;

        if (((UnitOfWorkImpl)session).isClassReadOnly(sourceValue.getClass())) {
            backUpVersion = ((UnitOfWorkImpl)session).getOriginalVersionOfObject(sourceValue);
        } else {
            backUpVersion = ((UnitOfWorkImpl)session).getBackupClone(sourceValue);
        }
        
        return (keyFrom(backUpVersion, session).equals(keyFrom(sourceValue, session)));
    }

    /**
     * INTERNAL:
     * Return the true if element exists in container.
     * @return boolean true if container 'contains' element
     */
    protected boolean contains(Object element, Object container) {
        return ((Map)container).containsValue(element);
    }
    
    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this ContainerPolicy to 
     * actual class-based settings. This method is used when converting a 
     * project that has been built with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        super.convertClassNamesToClasses(classLoader);
        
        if (elementClassName == null){
            return;
        }
        
        try {
            Class elementClass = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    elementClass = (Class)AccessController.doPrivileged(new PrivilegedClassForName(elementClassName, true, classLoader));
                } catch (PrivilegedActionException exception) {
                    throw ValidationException.classNotFoundWhileConvertingClassNames(containerClassName, exception.getException());
                }
            } else {
                elementClass = oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(elementClassName, true, classLoader);
            }
            setElementClass(elementClass);
        } catch (ClassNotFoundException exc){
            throw ValidationException.classNotFoundWhileConvertingClassNames(containerClassName, exc);
        }
    }

    /**
     * INTERNAL:
     * Returns the element class which defines the map key.
     */
    public Class getElementClass() {
        return elementClass;
    }
    
    /**
     * INTERNAL:
     * Returns the element class name which defines the map key.
     */
    public String getElementClassName() {
        return elementClassName;
    }

    /**
     * INTERNAL:
     */
    public Class getInterfaceType() {
        return ClassConstants.Map_Class;
    }

    /**
     * INTERNAL:
     * Returns the key name which will return the value of the key to be used 
     * in the container.
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * INTERNAL
     * Yes this is a MapPolicy
     */
    public boolean isMapPolicy() {
        return true;
    }

    /**
     * INTERNAL:
     * Return an Iterator for the given container.
     */
    public Object iteratorFor(Object container) {
        return ((Map)container).values().iterator();
    }

    /**
     * INTERNAL:
     * Return the key for the specified element.
     */
    public Object keyFrom(Object element, AbstractSession session) {
        // Should only run through this once ...
        if (keyName != null && keyMethod == null && keyField == null) {
            try {
                keyMethod = Helper.getDeclaredMethod(elementClass, keyName, (Class[]) null);
            } catch (NoSuchMethodException ex) {
                try {
                    keyField = Helper.getField(elementClass, keyName);
                } catch (NoSuchFieldException e) {
                    throw ValidationException.mapKeyNotDeclaredInItemClass(keyName, elementClass);    
                }
            }
        }
        
        Object keyElement = element;
        
        if (hasElementDescriptor()) {
            keyElement = getElementDescriptor().getObjectBuilder().unwrapObject(element, session);    
        }
        
        if (keyMethod != null) {
            try {              
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        return AccessController.doPrivileged(new PrivilegedMethodInvoker(keyMethod, keyElement, (Object[])null));
                    } catch (PrivilegedActionException exception) {
                        Exception throwableException = exception.getException();
                        if (throwableException instanceof IllegalAccessException) {
                            throw QueryException.cannotAccessMethodOnObject(keyMethod, keyElement);
                        } else {
                            throw QueryException.calledMethodThrewException(keyMethod, keyElement, throwableException);
                        }
                    }
                } else {
                    return PrivilegedAccessHelper.invokeMethod(keyMethod, keyElement, (Object[])null);
                }
            } catch (IllegalAccessException e) {
                throw QueryException.cannotAccessMethodOnObject(keyMethod, keyElement);
            } catch (InvocationTargetException exception) {
                throw QueryException.calledMethodThrewException(keyMethod, keyElement, exception);
            }
        } else if (keyField != null) {
            try {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        return AccessController.doPrivileged(new PrivilegedGetValueFromField(keyField, keyElement));
                    } catch (PrivilegedActionException exception) {
                        throw QueryException.cannotAccessFieldOnObject(keyField, keyElement);
                    }
                } else {
                    return oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getValueFromField(keyField, keyElement);
                }
            } catch (IllegalAccessException e) {
                throw QueryException.cannotAccessFieldOnObject(keyField, keyElement);
            }
        } else {
            // If we get this far I think it is safe to assume we have
            // an element descriptor.
            return ((CMP3Policy) getElementDescriptor().getCMPPolicy()).createPrimaryKeyInstance(keyElement, session);
        }
    }

    /**
     * INTERNAL:
     * Remove element from container which implements the Map interface.
     */
    public boolean removeFrom(Object key, Object element, Object container, AbstractSession session) {
        try {
            Object returnValue = null;
            if (key != null) {
                returnValue = ((Map)container).remove(key);
            } else {
                returnValue = ((Map)container).remove(keyFrom(element, session));
            }
            if (returnValue == null) {
                return false;
            } else {
                return true;
            }
        } catch (UnsupportedOperationException ex) {
            throw QueryException.methodNotValid(container, "remove(Object element)");
        }
    }

    /**
     * INTERNAL:
     * Remove element from container which implements the Map interface.
     */
    public boolean removeFromWithIdentity(Object element, Object container, AbstractSession session) {
        boolean found = false;
        Vector knownKeys = new Vector(1);
        try {
            Iterator iterator = ((Map)container).keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                if (((Map)container).get(key) == element) {
                    knownKeys.addElement(key);
                    found = true;
                }
            }
            if (found) {
                for (int index = 0; index < knownKeys.size(); ++index) {
                    ((Map)container).remove(knownKeys.elementAt(index));
                }
            }
            return found;
        } catch (UnsupportedOperationException ex) {
            throw QueryException.methodNotValid(container, "remove(Object element)");
        }
    }

    /**
     * INTERNAL:
     * Sets the element class which defines the method.
     */
    public void setElementClass(Class elementClass) {
        if (elementClass != null) {
            elementClassName = elementClass.getName();
        }
        
        this.elementClass = elementClass;
    }

    /**
     * INTERNAL:
     * Validate the container type.
     */
    public boolean isValidContainer(Object container) {
        // PERF: Use instanceof which is inlined, not isAssignable which 
        // is very inefficent.
        return container instanceof Map;
    }
    
    /**
     * INTERNAL:
     * Sets the key name to be used to generate the key in a Map type container 
     * class. The key name, may be the name of a field or method.
     */
    public void setKeyName(String keyName, String elementClassName) {
        // The key name and class name must be held as the policy is used 
        // directly from the mapping.
        this.keyName = keyName;
        this.elementClassName = elementClassName;
    }
    
    /**
     * INTERNAL:
     * Sets the key name to be used to generate the key in a Map type container 
     * class. The key name, maybe the name of a field or method.
     */
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
    
    /**
     * INTERNAL:
     * Return the size of container.
     */
    public int sizeFor(Object container) {
        return ((Map)container).size();
    }

    /**
     * INTERNAL:
     * If the key has changed, remove the element and add it back into the target.
     */
    public void validateElementAndRehashIfRequired(Object sourceValue, Object targetMap, AbstractSession session, Object targetVersionOfSource) {
        if (session.isUnitOfWork()) {
            //this must be a unit of work at this point
            Object backupValue = ((UnitOfWorkImpl)session).getBackupClone(sourceValue);
            if (!keyFrom(backupValue, session).equals(keyFrom(sourceValue, session))) {
                //the key has been changed.  Remove the old value and put back the new one
                removeFrom(backupValue, targetMap, session);
                addInto(targetVersionOfSource, targetMap, session);
            }
        }
    }
}
