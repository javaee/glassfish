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
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: A CollectionContainerPolicy is ContainerPolicy whose container class
 * implements the Collection interface.
 * <p>
 * <p><b>Responsibilities</b>:
 * Provide the functionality to operate on an instance of a Collection.
 *
 * @see ContainerPolicy
 * @see MapContainerPolicy
 */
public class CollectionContainerPolicy extends InterfaceContainerPolicy {

    /**
     * INTERNAL:
     * Construct a new policy.
     */
    public CollectionContainerPolicy() {
        super();
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class.
     */
    public CollectionContainerPolicy(Class containerClass) {
        super(containerClass);
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class name.
     */
    public CollectionContainerPolicy(String containerClassName) {
        super(containerClassName);
    }

    /**
     * INTERNAL:
     * Add element into a container which implements the Collection interface.
     *
     * @param element java.lang.Object
     * @param container java.lang.Object
     * @return boolean indicating whether the container changed
     */
    public boolean addInto(Object key, Object element, Object container) {
        try {
            return ((Collection)container).add(element);
        } catch (ClassCastException ex1) {
            throw QueryException.cannotAddElement(element, container, ex1);
        } catch (IllegalArgumentException ex2) {
            throw QueryException.cannotAddElement(element, container, ex2);
        } catch (UnsupportedOperationException ex3) {
            throw QueryException.cannotAddElement(element, container, ex3);
        }
    }

    /**
     * INTERNAL:
     * Return a container populated with the contents of the specified Vector.
     */
    public Object buildContainerFromVector(Vector vector, AbstractSession session) {
        if ((getContainerClass() == vector.getClass()) && (!hasElementDescriptor())) {
            return vector;
        } else {
            return super.buildContainerFromVector(vector, session);
        }
    }

    /**
     * INTERNAL:
     * Remove all the elements from container.
     *
     * @param container java.lang.Object
     */
    public void clear(Object container) {
        try {
            ((Collection)container).clear();
        } catch (UnsupportedOperationException ex) {
            throw QueryException.methodNotValid(container, "clear()");
        }
    }

    /**
     * INTERNAL:
     * Return the true if element exists in container.
     *
     * @param element java.lang.Object
     * @param container java.lang.Object
     * @return boolean true if container 'contains' element
     */
    protected boolean contains(Object element, Object container) {
        return ((Collection)container).contains(element);
    }

    public Class getInterfaceType() {
        return ClassConstants.Collection_Class;
    }

    /**
     * INTERNAL:
     * Return whether the collection has order.
     * SortedSets cannot be indexed, but they are order-sensitive.
     */
    public boolean hasOrder() {
        return Helper.classImplementsInterface(this.getContainerClass(), ClassConstants.SortedSet_Class);
    }

    /**
     * INTERNAL:
     * Validate the container type.
     */
    public boolean isValidContainer(Object container) {
        // PERF: Use instanceof which is inlined, not isAssignable which is very inefficent.
        return container instanceof Collection;
    }

    public boolean isCollectionPolicy() {
        return true;
    }

    /**
     * INTERNAL:
     * Return an iterator for the given container.
     *
     * @param container java.lang.Object
     * @return java.util.Enumeration/java.util.Iterator
     */
    public Object iteratorFor(Object container) {
        return ((Collection)container).iterator();
    }

    /**
     * INTERNAL:
     * Remove element from container which implements the Collection interface.
     *
     * @param key java.lang.Object This param represents the key that would be used by this object in a map, may be null
     * @param element java.lang.Object
     * @param container java.lang.Object
     */
    protected boolean removeFrom(Object key, Object element, Object container) {
        try {
            return ((Collection)container).remove(element);
        } catch (UnsupportedOperationException ex) {
            throw QueryException.methodNotValid(element, "remove(Object element)");
        }
    }

    /**
     * INTERNAL:
     * Return the size of container.
     *
     * @param anObject java.lang.Object
     * @return int The size of the container.
     */
    public int sizeFor(Object container) {
        return ((Collection)container).size();
    }
}
