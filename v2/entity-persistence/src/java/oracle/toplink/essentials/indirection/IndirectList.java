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
package oracle.toplink.essentials.indirection;

import java.util.*;
import oracle.toplink.essentials.internal.indirection.*;

/**
 * IndirectList allows a domain class to take advantage of TopLink indirection
 * without having to declare its instance variable as a ValueHolderInterface.
 * <p>To use an IndirectList:<ul>
 * <li> Declare the appropriate instance variable with type IndirectList (jdk1.1)
 * or Collection/List/Vector (jdk1.2).
 * <li> Send the message #useTransparentCollection() to the appropriate
 * CollectionMapping.
 * </ul>
 * TopLink will place an
 * IndirectList in the instance variable when the containing domain object is read from
 * the datatabase. With the first message sent to the IndirectList, the contents
 * are fetched from the database and normal Collection/List/Vector behavior is resumed.
 *
 * @see oracle.toplink.essentials.mappings.CollectionMapping
 * @see oracle.toplink.essentials.indirection.IndirectMap
 * @author Big Country
 * @since TOPLink/Java 2.5
 */
public class IndirectList extends Vector implements IndirectContainer {

    /** Reduce type casting. */
    protected Vector delegate;

    /** Delegate indirection behavior to a value holder. */
    protected ValueHolderInterface valueHolder;
    
    /** The mapping attribute name, used to raise change events. */
    private transient String attributeName;

    /** Store initial size for lazy init. */
    protected int initialCapacity = 10;

    /**
     * PUBLIC:
     * Construct an empty IndirectList so that its internal data array
     * has size <tt>10</tt> and its standard capacity increment is zero.
     */
    public IndirectList() {
        this(10);
    }

    /**
     * PUBLIC:
     * Construct an empty IndirectList with the specified initial capacity and
     * with its capacity increment equal to zero.
     *
     * @param   initialCapacity   the initial capacity of the vector
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public IndirectList(int initialCapacity) {
        this(initialCapacity, 0);
    }

    /**
     * PUBLIC:
     * Construct an empty IndirectList with the specified initial capacity and
     * capacity increment.
     *
     * @param   initialCapacity     the initial capacity of the vector
     * @param   capacityIncrement   the amount by which the capacity is
     *                              increased when the vector overflows
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public IndirectList(int initialCapacity, int capacityIncrement) {
        super(0);
        this.initialize(initialCapacity, capacityIncrement);
    }

    /**
     * PUBLIC:
     * Construct an IndirectList containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     * @param c a collection containing the elements to construct this IndirectList with.
     */
    public IndirectList(Collection c) {
        super(0);
        this.initialize(c);
    }

    /**
     * @see java.util.Vector#add(int, java.lang.Object)
     */
    public void add(int index, Object element) {
        this.getDelegate().add(index, element);
        raiseAddChangeEvent(element);
    }
    
    /**
     * Raise the add change event and relationship maintainence.
     */
    protected void raiseAddChangeEvent(Object element) {
        if (hasBeenRegistered()) {
            ((UnitOfWorkQueryValueHolder)getValueHolder()).updateForeignReferenceSet(element, null);
        }
    }
    
    /**
     * Raise the remove change event.
     */
    protected void raiseRemoveChangeEvent(Object element) {
        if (hasBeenRegistered()) {
            ((UnitOfWorkQueryValueHolder)getValueHolder()).updateForeignReferenceRemove(element);
        }
    }

    /**
     * @see java.util.Vector#add(java.lang.Object)
     */
    public synchronized boolean add(Object o) {
        this.getDelegate().add(o);
        raiseAddChangeEvent(o);
        return true;
    }

    /**
     * @see java.util.Vector#addAll(int, java.util.Collection)
     */
    public synchronized boolean addAll(int index, Collection c) {
        Iterator objects = c.iterator();
        // Must trigger add events if tracked or uow.
        if (hasBeenRegistered()) {
            while (objects.hasNext()) {
                this.add(index, objects.next());
                index++;
            }
            return true;
        }

        return this.getDelegate().addAll(index, c);

    }

    /**
     * @see java.util.Vector#addAll(java.util.Collection)
     */
    public synchronized boolean addAll(Collection c) {
        // Must trigger add events if tracked or uow.
        if (hasBeenRegistered()) {
            Iterator objects = c.iterator();
            while (objects.hasNext()) {
                this.add(objects.next());
            }
            return true;
        }

        return getDelegate().addAll(c);
    }

    /**
     * @see java.util.Vector#addElement(java.lang.Object)
     */
    public synchronized void addElement(Object obj) {
        this.add(obj);
    }

    /**
     * PUBLIC:
     * Return the freshly-built delegate.
     * <br>In jdk1.1, the delegate may not be a Vector (in which case, it better be another IndirectList).
     * <br>In jdk1.2, IndirectList is a subclass of Vector, so there is no need to check.
     */
    protected Vector buildDelegate() {
        return (Vector)getValueHolder().getValue();
    }

    /**
     * @see java.util.Vector#capacity()
     */
    public int capacity() {
        return this.getDelegate().capacity();
    }

    /**
     * @see java.util.Vector#clear()
     */
    public void clear() {
        if (hasBeenRegistered()) {
            Iterator objects = this.iterator();
            while (objects.hasNext()) {
                Object o = objects.next();
                objects.remove();
                this.raiseRemoveChangeEvent(o);
            }
        } else {
            this.getDelegate().clear();
        }
    }

    /**
     * PUBLIC:
     * @see java.util.Vector#clone()
     * This will result in a database query if necessary.
     */

    /*
        There are 3 situations when clone() is called:
        1.    The developer actually wants to clone the collection (typically to modify one
            of the 2 resulting collections). In which case the contents must be read from
            the database.
        2.    A UnitOfWork needs a clone (or backup clone) of the collection. But the
            UnitOfWork checks "instantiation" before cloning collections ("un-instantiated"
            collections are not cloned).
        3.    A MergeManager needs an extra copy of the collection (because the "backup"
            and "target" are the same object?). But the MergeManager checks "instantiation"
            before merging collections (again, "un-instantiated" collections are not merged).
    */
    public synchronized Object clone() {
        IndirectList result = (IndirectList)super.clone();
        result.delegate = (Vector)this.getDelegate().clone();
        result.attributeName = null;
        return result;
    }

    /**
     * PUBLIC:
     * @see java.util.Vector#contains(java.lang.Object)
     */
    public boolean contains(Object elem) {
        return this.getDelegate().contains(elem);
    }

    /**
     * @see java.util.Vector#containsAll(java.util.Collection)
     */
    public synchronized boolean containsAll(Collection c) {
        return this.getDelegate().containsAll(c);
    }

    /**
     * @see java.util.Vector#copyInto(java.lang.Object[])
     */
    public synchronized void copyInto(Object[] anArray) {
        this.getDelegate().copyInto(anArray);
    }

    /**
     * @see java.util.Vector#elementAt(int)
     */
    public synchronized Object elementAt(int index) {
        return this.getDelegate().elementAt(index);
    }

    /**
     * @see java.util.Vector#elements()
     */
    public Enumeration elements() {
        return this.getDelegate().elements();
    }

    /**
     * @see java.util.Vector#ensureCapacity(int)
     */
    public synchronized void ensureCapacity(int minCapacity) {
        this.getDelegate().ensureCapacity(minCapacity);
    }

    /**
     * @see java.util.Vector#equals(java.lang.Object)
     */
    public synchronized boolean equals(Object o) {
        return this.getDelegate().equals(o);
    }

    /**
     * @see java.util.Vector#firstElement()
     */
    public synchronized Object firstElement() {
        return this.getDelegate().firstElement();
    }

    /**
     * @see java.util.Vector#get(int)
     */
    public synchronized Object get(int index) {
        return this.getDelegate().get(index);
    }

    /**
     * PUBLIC:
     * Check whether the contents have been read from the database.
     * If they have not, read them and set the delegate.
     */
    protected synchronized Vector getDelegate() {
        if (delegate == null) {
            delegate = this.buildDelegate();
        }
        return delegate;
    }

    /**
     * PUBLIC:
     * Return the valueHolder.
     */
    public synchronized ValueHolderInterface getValueHolder() {
        // PERF: lazy initialize value holder and vector as are normally set after creation.
        if (valueHolder == null) {
            valueHolder = new ValueHolder(new Vector(this.initialCapacity, this.capacityIncrement));
        }
        return valueHolder;
    }

    /**
     * INTERNAL:
     * return whether this IndirectList has been registered with the UnitOfWork
     */
    public boolean hasBeenRegistered() {
        return getValueHolder() instanceof oracle.toplink.essentials.internal.indirection.UnitOfWorkQueryValueHolder;
    }

    /**
     * INTERNAL:
     * @see java.util.Vector#hashCode()
     */
    public synchronized int hashCode() {
        return this.getDelegate().hashCode();
    }

    /**
     * @see java.util.Vector#indexOf(java.lang.Object)
     */
    public int indexOf(Object elem) {
        return this.getDelegate().indexOf(elem);
    }

    /**
     * @see java.util.Vector#indexOf(java.lang.Object, int)
     */
    public synchronized int indexOf(Object elem, int index) {
        return this.getDelegate().indexOf(elem, index);
    }

    /**
     * Initialize the instance.
     */
    protected void initialize(int initialCapacity, int capacityIncrement) {
        this.initialCapacity = initialCapacity;
        this.capacityIncrement = capacityIncrement;
        this.delegate = null;
        this.valueHolder = null;
    }

    /**
     * Initialize the instance.
     */
    protected void initialize(Collection c) {
        this.delegate = null;
        Vector temp = new Vector(c);
        this.valueHolder = new ValueHolder(temp);
    }

    /**
     * @see java.util.Vector#insertElementAt(java.lang.Object, int)
     */
    public synchronized void insertElementAt(Object obj, int index) {
        this.getDelegate().insertElementAt(obj, index);
        this.raiseAddChangeEvent(obj);
    }

    /**
     * @see java.util.Vector#isEmpty()
     */
    public boolean isEmpty() {
        return this.getDelegate().isEmpty();
    }

    /**
     * PUBLIC:
     * Return whether the contents have been read from the database.
     */
    public boolean isInstantiated() {
        return this.getValueHolder().isInstantiated();
    }

    /**
     * @see java.util.AbstractList#iterator()
     */
    public Iterator iterator() {
        // Must wrap the interator to raise the remove event.
        return new Iterator() {
            Iterator delegateIterator = IndirectList.this.getDelegate().iterator();
            Object currentObject;
            
            public boolean hasNext() {
                return this.delegateIterator.hasNext();
            }
            
            public Object next() {
                this.currentObject = this.delegateIterator.next();
                return this.currentObject;
            }
            
            public void remove() {
                this.delegateIterator.remove();
                IndirectList.this.raiseRemoveChangeEvent(this.currentObject);
            }
        };
    }

    /**
     * @see java.util.Vector#lastElement()
     */
    public synchronized Object lastElement() {
        return this.getDelegate().lastElement();
    }

    /**
     * @see java.util.Vector#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object elem) {
        return this.getDelegate().lastIndexOf(elem);
    }

    /**
     * @see java.util.Vector#lastIndexOf(java.lang.Object, int)
     */
    public synchronized int lastIndexOf(Object elem, int index) {
        return this.getDelegate().lastIndexOf(elem, index);
    }

    /**
     * @see java.util.AbstractList#listIterator()
     */
    public ListIterator listIterator() {
        return this.listIterator(0);
    }

    /**
     * @see java.util.AbstractList#listIterator(int)
     */
    public ListIterator listIterator(final int index) {
        // Must wrap the interator to raise the remove event.
        return new ListIterator() {
            ListIterator delegateIterator = IndirectList.this.getDelegate().listIterator(index);
            Object currentObject;
            
            public boolean hasNext() {
                return this.delegateIterator.hasNext();
            }
            
            public boolean hasPrevious() {
                return this.delegateIterator.hasPrevious();
            }
            
            public int previousIndex() {
                return this.delegateIterator.previousIndex();
            }
            
            public int nextIndex() {
                return this.delegateIterator.nextIndex();
            }
            
            public Object next() {
                this.currentObject = this.delegateIterator.next();
                return this.currentObject;
            }
            
            public Object previous() {
                this.currentObject = this.delegateIterator.previous();
                return this.currentObject;
            }
            
            public void remove() {
                this.delegateIterator.remove();
                IndirectList.this.raiseRemoveChangeEvent(this.currentObject);
            }
            
            public void set(Object object) {
                this.delegateIterator.set(object);
                IndirectList.this.raiseRemoveChangeEvent(this.currentObject);
                IndirectList.this.raiseAddChangeEvent(object);
            }
            
            public void add(Object object) {
                this.delegateIterator.add(object);
                IndirectList.this.raiseAddChangeEvent(object);
            }
        };
    }

    /**
     * @see java.util.Vector#remove(int)
     */
    public synchronized Object remove(int index) {
        Object value = this.getDelegate().remove(index);        
        this.raiseRemoveChangeEvent(value);
        return value;
    }

    /**
     * @see java.util.Vector#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        if (this.getDelegate().remove(o)) {     
            this.raiseRemoveChangeEvent(o);
            return true;
        }
        return false;
    }

    /**
     * @see java.util.Vector#removeAll(java.util.Collection)
     */
    public synchronized boolean removeAll(Collection c) {
        // Must trigger remove events if tracked or uow.
        if (hasBeenRegistered()) {
            Iterator objects = c.iterator();
            while (objects.hasNext()) {
                this.remove(objects.next());
            }
            return true;
        }
        return this.getDelegate().removeAll(c);
    }

    /**
     * @see java.util.Vector#removeAllElements()
     */
    public synchronized void removeAllElements() {
        // Must trigger remove events if tracked or uow.
        if (hasBeenRegistered()) {
            Iterator objects = this.iterator();
            while (objects.hasNext()) {
                Object object = objects.next();
                objects.remove();
                this.raiseRemoveChangeEvent(object);
            }
            return;
        }
        this.getDelegate().removeAllElements();
    }

    /**
     * @see java.util.Vector#removeElement(java.lang.Object)
     */
    public synchronized boolean removeElement(Object obj) {
        return this.remove(obj);
    }

    /**
     * @see java.util.Vector#removeElementAt(int)
     */
    public synchronized void removeElementAt(int index) {
        this.remove(index);
    }

    /**
     * @see java.util.Vector#retainAll(java.util.Collection)
     */
    public synchronized boolean retainAll(Collection c) {
        // Must trigger remove events if tracked or uow.
        if (hasBeenRegistered()) {
            Iterator objects = getDelegate().iterator();
            while (objects.hasNext()) {
                Object object = objects.next();
                if (!c.contains(object)) {
                    objects.remove();
                    this.raiseRemoveChangeEvent(object);
                }
            }
            return true;
        }
        return this.getDelegate().retainAll(c);
    }

    /**
     * @see java.util.Vector#set(int, java.lang.Object)
     */
    public synchronized Object set(int index, Object element) {
        Object oldValue = this.getDelegate().set(index, element);
        this.raiseRemoveChangeEvent(oldValue);
        this.raiseAddChangeEvent(element);
        return oldValue;
    }

    /**
     * @see java.util.Vector#setElementAt(java.lang.Object, int)
     */
    public synchronized void setElementAt(Object obj, int index) {
        this.set(index, obj);
    }

    /**
     * @see java.util.Vector#setSize(int)
     */
    public synchronized void setSize(int newSize) {
        // Must trigger remove events if tracked or uow.
        if (hasBeenRegistered()) {
            if (newSize > this.size()) {
                for (int index = size(); index > newSize; index--) {
                    this.remove(index - 1);
                }
            }
        }    
        this.getDelegate().setSize(newSize);
    }

    /**
     * PUBLIC:
     * Set the value holder.
     */
    public void setValueHolder(ValueHolderInterface valueHolder) {
        this.delegate = null;
        this.valueHolder = valueHolder;
    }

    /**
     * @see java.util.Vector#size()
     */
    public int size() {
        return this.getDelegate().size();
    }

    /**
     * @see java.util.Vector#subList(int, int)
     */
    public List subList(int fromIndex, int toIndex) {
        return this.getDelegate().subList(fromIndex, toIndex);
    }

    /**
     * @see java.util.Vector#toArray()
     */
    public synchronized Object[] toArray() {
        return this.getDelegate().toArray();
    }

    /**
     * @see java.util.Vector#toArray(java.lang.Object[])
     */
    public synchronized Object[] toArray(Object[] a) {
        return this.getDelegate().toArray(a);
    }

    /**
     * PUBLIC:
     * Use the java.util.Vector#toString(); but wrap it with braces to indicate
     * there is a bit of indirection.
     * Don't allow this method to trigger a database read.
     * @see java.util.Vector#toString()
     */
    public String toString() {
        if (ValueHolderInterface.shouldToStringInstantiate) {
            return this.getDelegate().toString();
        }
        if (this.isInstantiated()) {
            return "{" + this.getDelegate().toString() + "}";
        } else {
            return "{" + oracle.toplink.essentials.internal.helper.Helper.getShortClassName(this.getClass()) + ": not instantiated}";
        }
    }

    /**
     * @see java.util.Vector#trimToSize()
     */
    public synchronized void trimToSize() {
        this.getDelegate().trimToSize();
    }
     
    /**
     * Return the mapping attribute name, used to raise change events.
     */
     public String getTopLinkAttributeName() {
         return attributeName;
     }
     
    /**
     * Set the mapping attribute name, used to raise change events.
     * This is required if the change listener is set.
     */
     public void setTopLinkAttributeName(String attributeName) {
         this.attributeName = attributeName;
     }
}
