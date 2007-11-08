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

import java.util.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.mappings.converters.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: A MapContainerPolicy is ContainerPolicy whose container class
 * implements the Map interface.
 * <p>
 * <p><b>Responsibilities</b>:
 * Provide the functionality to operate on an instance of a Map.
 *
 * @see ContainerPolicy
 * @see CollectionContainerPolicy
 */
public class DirectMapContainerPolicy extends InterfaceContainerPolicy {
    protected DatabaseField keyField;
    protected DatabaseField valueField;
    protected Converter keyConverter;
    protected Converter valueConverter;

    /**
     * INTERNAL:
     * Construct a new policy.
     */
    public DirectMapContainerPolicy() {
        super();
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class.
     */
    public DirectMapContainerPolicy(Class containerClass) {
        super(containerClass);
    }

    /**
     * INTERNAL:
     * Add key, value pair into container which implements the Map interface.
     */
    public boolean addInto(Object key, Object value, Object container, AbstractSession session) {
        try {
            ((Map)container).put(key, value);
        } catch (ClassCastException ex1) {
            throw QueryException.cannotAddElement(key, container, ex1);
        }
        return true;
    }

    /**
     * INTERNAL:
     * Add element into container which implements the Map interface. Not used since key is not obtained from the object
     */
    public boolean addInto(Object element, Object container, AbstractSession session) {
        throw ValidationException.operationNotSupported("addInto(Object element, Object container, Session session)");
    }

    /**
     * INTERNAL:
     * Return a container populated with the contents of the specified Vector.
     */
    public Object buildContainerFromVector(Vector vector, AbstractSession session) {
        Map container = (Map)containerInstance(vector.size());
        AbstractRecord row;

        for (Enumeration e = vector.elements(); e.hasMoreElements();) {
            row = (AbstractRecord)e.nextElement();
            Object key = row.get(keyField);
            Object value = row.get(valueField);
            if (getKeyConverter() != null) {
                key = getKeyConverter().convertDataValueToObjectValue(key, session);
            }
            if (getValueConverter() != null) {
                value = getValueConverter().convertDataValueToObjectValue(value, session);
            }
            if (key != null) {
                container.put(key, value);
            }
        }
        return container;
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
     * Return true if keys are the same.  False otherwise
     */
    public boolean compareContainers(Object firstObjectMap, Object secondObjectMap) {
        if (sizeFor(firstObjectMap) != sizeFor(secondObjectMap)) {
            return false;
        }

        for (Object firstIterator = iteratorFor(firstObjectMap); hasNext(firstIterator);) {
            Object key = next(firstIterator);
            if (!((Map)firstObjectMap).get(key).equals(((Map)secondObjectMap).get(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * INTERNAL:
     * Return true if keys are the same in the source as the backup.  False otherwise
     * in the case of readonly compare against the original
     */
    public boolean compareKeys(Object sourceValue, AbstractSession session) {
        Object backUpVersion = null;

        //CR 4172
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

    public Class getInterfaceType() {
        return ClassConstants.Map_Class;
    }

    public boolean isDirectMapPolicy() {
        return true;
    }

    /**
     * INTERNAL:
     * Return an Iterator for the given container.
     */
    public Object iteratorFor(Object container) {
        if (((Map)container).keySet() == null) {
            return null;
        }
        return ((Map)container).keySet().iterator();
    }

    /**
     * INTERNAL:
     * Return an Iterator for the given container.
     */
    public Object iteratorForValue(Object container) {
        if (((Map)container).values() == null) {
            return null;
        }
        return ((Map)container).values().iterator();
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

    public void setKeyField(DatabaseField field) {
        keyField = field;
    }

    public void setValueField(DatabaseField field) {
        valueField = field;
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

    /**
     * INTERNAL:
     * Validate the container type.
     */
    public boolean isValidContainer(Object container) {
        // PERF: Use instanceof which is inlined, not isAssignable which is very inefficent.
        return container instanceof Map;
    }

    /**
     * INTERNAL:
     * Return an value of the key from container
     */
    public Object valueFromKey(Object key, Object container) {
        return ((Map)container).get(key);
    }

    public Converter getKeyConverter() {
        return keyConverter;
    }

    public void setKeyConverter(Converter keyConverter) {
        this.keyConverter = keyConverter;
    }

    public void setValueConverter(Converter valueConverter) {
        this.valueConverter = valueConverter;
    }

    public Converter getValueConverter() {
        return valueConverter;
    }
}
