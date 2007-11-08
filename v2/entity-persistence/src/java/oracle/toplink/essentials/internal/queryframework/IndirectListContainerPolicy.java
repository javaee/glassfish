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
import oracle.toplink.essentials.indirection.*;
import oracle.toplink.essentials.internal.helper.ClassConstants;

/**
 * A ContainerPolicy for jdk1.1 only (IndirectList implements Collection
 * in jdk1.2; so the CollectionContainerPolicy can be used.)
 *
 * @see ContainerPolicy
 * @author Big Country
 *    @since TOPLink/Java 2.5
 */
public class IndirectListContainerPolicy extends InterfaceContainerPolicy {

    /**
     * INTERNAL:
     * Construct a new policy.
     */
    public IndirectListContainerPolicy() {
        super();
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class.
     * @param containerClass java.lang.Class
     */
    public IndirectListContainerPolicy(Class containerClass) {
        super(containerClass);
        DescriptorException.invalidContainerPolicy(this, containerClass);
    }

    /**
     * INTERNAL:
     * Add element into the container.
     *
     * @param element java.lang.Object
     * @param container java.lang.Object
     * @return boolean indicating whether the container changed
     */
    protected boolean addInto(Object key, Object element, Object container) {
        try {
            ((IndirectList)container).addElement(element);
            return true;
        } catch (ClassCastException ex) {
            throw QueryException.cannotAddElement(element, container, ex);
        }
    }

    /**
     * INTERNAL:
     * Add element into a container which implements the Collection interface.
     *
     * @param element java.lang.Object
     * @param container java.lang.Object
     * @return boolean indicating whether the container changed
     */
    public void addIntoWithOrder(Vector indexes, Hashtable elements, Object container) {
        Object object = null;
        try {
            Enumeration indexEnum = indexes.elements();
            while (indexEnum.hasMoreElements()) {
                Integer index = (Integer)indexEnum.nextElement();
                object = elements.get(index);
                if (index.intValue() >= (sizeFor(container) - 1)) {
                    ((IndirectList)container).addElement(object);
                } else {
                    ((IndirectList)container).setElementAt(object, index.intValue());
                }
            }
        } catch (ClassCastException ex1) {
            throw QueryException.cannotAddElement(object, container, ex1);
        }
    }

    /**
     * INTERNAL:
     * Remove all the elements from container.
     *
     * @param container java.lang.Object
     */
    public void clear(Object container) {
        ((IndirectList)container).clear();
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
        return ((IndirectList)container).contains(element);
    }

    public Class getInterfaceType() {
        return ClassConstants.IndirectList_Class;
    }

    /**
     * INTERNAL:
     * Return whether the iterator has more objects,
     *
     * @param iterator java.lang.Object
     * @return boolean true if iterator has more objects
     */
    public boolean hasNext(Object iterator) {
        return ((Enumeration)iterator).hasMoreElements();
    }

    /**
     * INTERNAL:
     * Returns true if the collection has order
     *
     * @see ContainerPolicy#iteratorFor(java.lang.Object)
     */
    public boolean hasOrder() {
        return true;
    }

    /**
     * INTERNAL:
     * Return an Iterator for the given container.
     *
     * @param container java.lang.Object
     * @return java.lang.Object the iterator
     */
    public Object iteratorFor(Object container) {
        return ((IndirectList)container).elements();
    }

    /**
     * INTERNAL:
     * Return the next object on the queue.
     * Valid for some subclasses only.
     *
     * @param iterator java.lang.Object
     * @return java.lang.Object the next object in the queue
     */
    protected Object next(Object iterator) {
        return ((Enumeration)iterator).nextElement();
    }

    /**
     * INTERNAL:
     * Remove element from container which implements the Collection interface.
     *
     * @param element java.lang.Object
     * @param container java.lang.Object
     */
    protected boolean removeFrom(Object key, Object element, Object container) {
        return ((IndirectList)container).removeElement(element);
    }

    /**
     * INTERNAL:
     * Remove elements from this container starting with this index
     *
     * @param beginIndex int the point to start deleting values from the collection
     * @param container java.lang.Object
     * @return boolean indicating whether the container changed
     */
    public void removeFromWithOrder(int beginIndex, Object container) {
        int size = sizeFor(container) - 1;
        try {
            for (; size >= beginIndex; --size) {
                ((IndirectList)container).removeElementAt(size);
            }
        } catch (ClassCastException ex1) {
            throw QueryException.cannotRemoveFromContainer(new Integer(size), container, this);
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
        return ((IndirectList)container).size();
    }
}
