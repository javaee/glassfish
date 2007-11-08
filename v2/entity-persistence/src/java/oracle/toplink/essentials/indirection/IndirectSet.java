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

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.io.*;
import java.lang.reflect.Method;

import oracle.toplink.essentials.exceptions.DescriptorException;
import oracle.toplink.essentials.exceptions.QueryException;
import oracle.toplink.essentials.internal.localization.ToStringLocalization;
import oracle.toplink.essentials.internal.indirection.*;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedGetMethod;
import oracle.toplink.essentials.internal.security.PrivilegedMethodInvoker;

/**
 * IndirectSet is an example implementation of the Set protocol that
 * allows a domain class to take advantage of TopLink Indirection
 * without having to declare its instance variable as a ValueHolderInterface.
 * <p> To use an IndirectSet:<ul>
 * <li> Declare the appropriate instance variable with type Set (or Collection).
 * <li> Send the message #useTransparentCollection() to the appropriate
 * CollectionMapping.
 * <li> Send the message #useCollectionClass(IndirectSet.class) to the same
 * CollectionMapping. (The order of these two message sends is significant.)
 * </ul>
 * TopLink will place an IndirectSet in the instance variable when the
 * containing domain object is read from the datatabase. With the first
 * message sent to the IndirectSet, the contents
 * are fetched from the database and normal Set behavior is resumed.
 *
 * <p>
 * Implementation notes:<ul>
 * <li> The Set interface is implemented by delegating nearly every message
 * to the Set held on to by the 'delegate' instance variable. (The 'delegate'
 * will be either a HashSet or yet another IndirectSet.)
 * <li> The IndirectContainer interface is implemented in a straightforward
 * fashion: <ul>
 *     <li> #get- and #setValueHolder() are implemented as simple accessors for the
 * 'valueHolder' instance variable. (Note that #setValueHolder() clears out the
 * 'delegate' instance variable, since its contents are invalidated by the arrival
 * of a new value holder.)
 *     <li> #isInstantiated() is simply delegated to the value holder.
 *     </ul>
 * <li> TopLink requires that the Cloneable interface be implemented. The #clone()
 * method must clone the 'delegate'. (The implementation here uses reflection to invoke
 * the #clone() method because it is not included in the common interface shared
 * by IndirectSet and its base delegate class, HashSet; namely, Set.)
 * <li> TopLink requires that the Serializable interface be implemented.
 * <li> The database read is ultimately triggered when one of the "delegated"
 * methods makes the first call to #getDelegate(), which in turn calls
 * #buildDelegate(), which
 * sends the message #getValue() to the value holder.
 * The value holder performs the database read.
 * <li> For debugging purposes, #toString() will <em>not</em> trigger a database
 * read. This is not required behavior.
 * </ul>
 *
 * @see oracle.toplink.essentials.mappings.CollectionMapping
 * @author Big Country
 * @since TOPLink/Java 3.0+
 */
public class IndirectSet implements Set, IndirectContainer, Cloneable, Serializable {

    /** Reduce type casting */
    private Set delegate;

    /** Delegate indirection behavior to a value holder */
    private ValueHolderInterface valueHolder;
    
    /** The mapping attribute name, used to raise change events. */
    private String attributeName;

    /** Store initial size for lazy init. */
    protected int initialCapacity = 10;

    /** Store load factor for lazy init. */
    protected float loadFactor = 0.75f;

    /**
     * Construct an empty IndirectSet.
     */
    public IndirectSet() {
        this.delegate = null;
        this.valueHolder = null;
    }

    /**
     * Construct an empty IndirectSet with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the set
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public IndirectSet(int initialCapacity) {
        this.delegate = null;
        this.initialCapacity = initialCapacity;
        this.valueHolder = null;
    }

    /**
     * Construct an empty IndirectSet with the specified initial capacity and
     * load factor.
     *
     * @param   initialCapacity     the initial capacity of the set
     * @param   loadFactor   the load factor of the set
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public IndirectSet(int initialCapacity, float loadFactor) {
        this.delegate = null;
        this.initialCapacity = initialCapacity;
        this.loadFactor = loadFactor;
        this.valueHolder = null;
    }

    /**
     * Construct an IndirectSet containing the elements of the specified collection.
     *
     * @param   c   the initial elements of the set
     */
    public IndirectSet(Collection c) {
        this.delegate = null;
        this.valueHolder = new ValueHolder(new HashSet(c));
    }

    /**
     * @see java.util.Set#add(java.lang.Object)
     */
    public synchronized boolean add(Object o) {
        this.getDelegate().add(o);
        this.raiseAddChangeEvent(o);
        return true;
    }

    /**
     * @see java.util.Set#addAll(java.util.Collection)
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
     * Return the freshly-built delegate.
     */
    protected Set buildDelegate() {
        return (Set)getValueHolder().getValue();
    }

    /**
     * @see java.util.Set#clear()
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
     * @see java.lang.Object#clone()
     * This will result in a database query if necessary.
     */

    /*
        There are 3 situations when #clone() is called:
        1.    The developer actually wants to clone the collection (typically to modify one
            of the 2 resulting collections). In which case the contents must be read from
            the database.
        2.    A UnitOfWork needs a clone (or backup clone) of the collection. But the
            UnitOfWork checks "instantiation" before cloning collections (i.e. "un-instantiated"
            collections are not cloned).
        3.    A MergeManager needs an extra copy of the collection (because the "backup"
            and "target" are the same object?). But the MergeManager also checks "instantiation"
            before merging collections (again, "un-instantiated" collections are not merged).
    */
    public Object clone() {
        try {
            IndirectSet result = (IndirectSet)super.clone();
            result.delegate = this.cloneDelegate();
            result.attributeName = null;
            return result;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("clone not supported");
        }
    }

    /**
     * Clone the delegate.
     */
    protected Set cloneDelegate() {
        java.lang.reflect.Method cloneMethod;
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    cloneMethod = (Method)AccessController.doPrivileged(new PrivilegedGetMethod(this.getDelegate().getClass(), "clone", (Class[])null, false));
                } catch (PrivilegedActionException exception) {
                    throw QueryException.cloneMethodRequired();
                }
            } else {
                cloneMethod = PrivilegedAccessHelper.getMethod(this.getDelegate().getClass(), "clone", (Class[])null, false);
            }
        } catch (NoSuchMethodException ex) {
            throw QueryException.cloneMethodRequired();
        }

        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return (Set)AccessController.doPrivileged(new PrivilegedMethodInvoker(cloneMethod, this.getDelegate(), (Object[])null));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof IllegalAccessException) {
                        throw QueryException.cloneMethodInaccessible();
                    } else {
                        throw QueryException.cloneMethodThrowException(((java.lang.reflect.InvocationTargetException)throwableException).getTargetException());
                    }
                }
            } else {
                return (Set)PrivilegedAccessHelper.invokeMethod(cloneMethod, this.getDelegate(), (Object[])null);
            }
        } catch (IllegalAccessException ex1) {
            throw QueryException.cloneMethodInaccessible();
        } catch (java.lang.reflect.InvocationTargetException ex2) {
            throw QueryException.cloneMethodThrowException(ex2.getTargetException());
        }
    }

    /**
     * @see java.util.Set#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return this.getDelegate().contains(o);
    }

    /**
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c) {
        return this.getDelegate().containsAll(c);
    }

    /**
     * @see java.util.Set#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        return this.getDelegate().equals(o);
    }

    /**
     * Check whether the contents have been read from the database.
     * If they have not, read them and set the delegate.
     */
    protected Set getDelegate() {
        if (delegate == null) {
            delegate = this.buildDelegate();
        }
        return delegate;
    }

    /**
     * Return the valueHolder.
     */
    public ValueHolderInterface getValueHolder() {
        // PERF: lazy initialize value holder and vector as are normally set after creation.
        if (valueHolder == null) {
            valueHolder = new ValueHolder(new HashSet(initialCapacity, loadFactor));
        }
        return valueHolder;
    }

    /**
     * INTERNAL:
     * Return whether this IndirectSet has been registered in a UnitOfWork
     */
    public boolean hasBeenRegistered() {
        return getValueHolder() instanceof oracle.toplink.essentials.internal.indirection.UnitOfWorkQueryValueHolder;
    }

    /**
     * @see java.util.Set#hashCode()
     */
    public int hashCode() {
        return this.getDelegate().hashCode();
    }

    /**
     * @see java.util.Set#isEmpty()
     */
    public boolean isEmpty() {
        return this.getDelegate().isEmpty();
    }

    /**
     * Return whether the contents have been read from the database.
     */
    public boolean isInstantiated() {
        return this.getValueHolder().isInstantiated();
    }

    /**
     * @see java.util.Set#iterator()
     */
    public Iterator iterator() {
        // Must wrap the interator to raise the remove event.
        return new Iterator() {
            Iterator delegateIterator = IndirectSet.this.getDelegate().iterator();
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
                IndirectSet.this.raiseRemoveChangeEvent(this.currentObject);
            }
        };
    }

    /**
     * @see java.util.Set#remove(java.lang.Object)
     */
    public synchronized boolean remove(Object o) {
        if (this.getDelegate().remove(o)) {     
            this.raiseRemoveChangeEvent(o);
            return true;
        }
        return false;
    }

    /**
     * @see java.util.Set#removeAll(java.util.Collection)
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
     * @see java.util.Set#retainAll(java.util.Collection)
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
     * Set the value holder.
     * Note that the delegate must be cleared out.
     */
    public void setValueHolder(ValueHolderInterface valueHolder) {
        this.delegate = null;
        this.valueHolder = valueHolder;
    }

    /**
     * @see java.util.Set#size()
     */
    public int size() {
        return this.getDelegate().size();
    }

    /**
     * @see java.util.Set#toArray()
     */
    public Object[] toArray() {
        return this.getDelegate().toArray();
    }

    /**
     * @see java.util.Set#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] a) {
        return this.getDelegate().toArray(a);
    }

    /**
     * Use the delegate's #toString(); but wrap it with braces to indicate
     * there is a bit of indirection.
     * Don't allow this method to trigger a database read.
     * @see java.util.HashSet#toString()
     */
    public String toString() {
        if (ValueHolderInterface.shouldToStringInstantiate) {
            return this.getDelegate().toString();
        }
        if (this.isInstantiated()) {
            return "{" + this.getDelegate().toString() + "}";
        } else {
            return "{" + oracle.toplink.essentials.internal.helper.Helper.getShortClassName(this.getClass()) + ": " + ToStringLocalization.buildMessage("not_instantiated", (Object[])null) + "}";

        }
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
