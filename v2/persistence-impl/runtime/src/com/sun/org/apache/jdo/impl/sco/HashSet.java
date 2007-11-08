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

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/* 
 * sco.HashSet.java
 */
 
package com.sun.org.apache.jdo.impl.sco;

import java.util.Collection;
import java.util.Iterator;

import com.sun.org.apache.jdo.sco.SCO;
import com.sun.org.apache.jdo.sco.SCOCollection;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.util.I18NHelper;



/**
 * A mutable 2nd class object that represents HashSet.
 * @author Marina Vatkina
 * @version 1.0.1
 * @see java.util.HashSet
 */
public class HashSet extends java.util.HashSet implements SCOCollection {

    private transient StateManagerInternal owner;

    private transient int fieldNumber = -1;

    private transient   Class elementType;

    private transient boolean allowNulls;

    private transient java.util.HashSet added = new java.util.HashSet();

    private transient java.util.HashSet removed = new java.util.HashSet();

    private transient Object[] frozenElements = null;
    
    /** 
     * I18N message handler 
     */ 
    private final static I18NHelper msg = I18NHelper.getInstance( 
        "com.sun.org.apache.jdo.impl.sco.Bundle");  // NOI18N

    private final static String _HashSet = "HashSet"; // NOI18N

    /**   
     * Creates a new empty <code>HashSet</code> object.
     *    
     * @param elementType the element types allowed
     * @param allowNulls true if nulls are allowed
     * @see java.util.HashSet
     */  
    public HashSet(Class elementType, boolean allowNulls) {
        super(); 
        this.elementType = elementType;
        this.allowNulls = allowNulls;
    } 

    /**   
     * Creates a new empty <code>HashSet</code> object that has 
     * the specified initial capacity.
     *    
     * @param elementType the element types allowed
     * @param allowNulls true if nulls are allowed
     * @param      initialCapacity   the initial capacity of the hash map. 
     * @throws     IllegalArgumentException if the initial capacity is less
     *             than zero.
     * @see java.util.HashSet
     */  
    public HashSet(Class elementType, boolean allowNulls, int initialCapacity) {
        super(initialCapacity); 
        this.elementType = elementType;
        this.allowNulls = allowNulls;
    } 

    /**   
     * Creates a new empty <code>HashSet</code> object that has 
     * the specified initial capacity..
     *    
     * @param elementType the element types allowed
     * @param allowNulls true if nulls are allowed
     * @param      initialCapacity   the initial capacity of the hash map. 
     * @param      loadFactor        the load factor of the hash map.
     * @throws     IllegalArgumentException if the initial capacity is less
     *             than zero.
     * @see java.util.HashSet
     */  
    public HashSet(Class elementType, boolean allowNulls, int initialCapacity, 
        float loadFactor) {
        super(initialCapacity, loadFactor); 
        this.elementType = elementType;
        this.allowNulls = allowNulls;
    } 

    // -------------------------Public Methods------------------

    /**
     * Adds the specified element to this set if it is not already
     * present.
     *   
     * @param o element to be added to this set.
     * @return <tt>true</tt> if the set did not already contain the specified
     * element.
     * @see java.util.HashSet
     */
    public boolean add(Object o) {
        SCOHelper.debug(_HashSet, "add"); // NOI18N

        SCOHelper.assertNullsAllowed(o, allowNulls);
        SCOHelper.assertElementType(o, elementType);

        // Mark the field as dirty
        this.makeDirty();

        thaw();
        
        boolean modified = super.add(o); 
        if (modified) {
            if (removed.remove(o) == false) {
                added.add(o);
            }
        }

        // Apply updates
        this.trackUpdates(modified);

        return modified;
    }  

    /**
     * Adds all of the elements in the specified collection to this collection
     *   
     * @param c collection whose elements are to be added to this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     * call.
     * @throws UnsupportedOperationException if the <tt>addAll</tt> method is
     *            not supported by this collection.
     *   
     * @see java.util.AbstractCollection
     * @see java.util.HashSet
     */  
    public boolean addAll(Collection c) {
        SCOHelper.debug(_HashSet, "addAll"); // NOI18N

        // iterate the collection and make a list of wrong elements.
        Throwable[] err = new Throwable[c.size()];
        int l = 0;

        Iterator i = c.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            try {
                SCOHelper.assertNullsAllowed(o, allowNulls);
                SCOHelper.assertElementType(o, elementType);
            } catch (Throwable e) {
                err[l++] = e;
            }
        }
        SCOHelper.validateResult(l, err);

        boolean modified = false;
        // Mark the field as dirty
        this.makeDirty();

        thaw();
        
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (!super.contains(o)) {
                if (removed.remove(o) == false) {
                    added.add(o);
                }
                super.add(o);
                modified = true;
            }
        }

        // Apply updates
        this.trackUpdates(modified);

        return modified;
    } 

    /**
     * Removes the given element from this set if it is present.
     *   
     * @param o object to be removed from this set, if present.
     * @return <tt>true</tt> if the set contained the specified element.
     * @see java.util.HashSet
     */  
    public boolean remove(Object o) {
        SCOHelper.debug(_HashSet, "remove"); // NOI18N

        // Mark the field as dirty
        this.makeDirty();

        thaw();
        
        boolean modified = super.remove(o);
        if (modified) { 
            if (added.remove(o) == false) { 
                removed.add(o);
            } 
        } 

        // Apply updates
        this.trackUpdates(modified);

        return modified;
    }    


    /**
     * Removes from this collection all of its elements that are contained in
     * the specified collection (optional operation). <p>
     * Processes each element remove internally not to have call backs
     * into #remove(Object). 
     *   
     * @param c elements to be removed from this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     * call.
     *   
     * @throws    UnsupportedOperationException removeAll is not supported
     *            by this collection.
     *   
     * @see java.util.HashSet
     * @see java.util.AbstractCollection
     */  
    public boolean removeAll(Collection c) {
        SCOHelper.debug(_HashSet, "removeAll"); // NOI18N

        boolean modified = false;
        // Mark the field as dirty
        this.makeDirty();

        thaw();
        
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (super.contains(o)) {
                removeInternal(o);
                modified = true;
                if (added.remove(o) == false) {
                    removed.add(o);
                }
            }
        }

        // Apply updates
        this.trackUpdates(modified);

        return modified;
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation). 
     *
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call.
     *
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> method
     *            is not supported by this collection.
     *
     * @see java.util.HashSet
     * @see java.util.AbstractCollection
     */  
    public boolean retainAll(Collection c) {
        SCOHelper.debug(_HashSet, "retainAll"); // NOI18N

        // Mark the field as dirty
        this.makeDirty();
        
        thaw();

        for (Iterator iter = super.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (!c.contains(o)) { 
                if (added.remove(o) == false) { 
                    removed.add(o); 
                } 
            } 
        }

        boolean modified = super.retainAll(c);

        // Apply updates
        this.trackUpdates(modified);

        return modified;
    }

    /**
     * Removes all of the elements from this set.
     * @see java.util.HashSet
     */  
    public void clear() {
        SCOHelper.debug(_HashSet, "clear"); // NOI18N

        // Mark the field as dirty
        this.makeDirty();

        thaw();
        removed.clear();
        added.clear();

        for (Iterator iter = super.iterator(); iter.hasNext();) {
            removed.add(iter.next());
        }

        super.clear();

        // Apply updates
        this.trackUpdates(true);

    }     
 
    /**
     * Creates and returns a copy of this object.
     *   
     * <P>Mutable Second Class Objects are required to provide a public
     * clone method in order to allow for copying PersistenceCapable
     * objects. In contrast to Object.clone(), this method must not throw a
     * CloneNotSupportedException.
     */  
    public Object clone() {
        SCOHelper.debug(_HashSet, "clone"); // NOI18N

        Object obj = super.clone();
        if (obj instanceof SCO) {
            ((SCO)obj).unsetOwner(owner, fieldNumber);
        }
        return obj;
    }

    /**
     * @see SCOCollection#reset()
     */  
    public void reset() {
        added.clear();
        removed.clear();
        frozenElements = null;
    }

    /**
     * @see SCOCollection#addInternal(Object o)
     */  
    public void addInternal(Object o) {
        super.add(o);
    }


    /**
     * @see SCOCollection#addAllInternal(Collection c)
     */  
    public void addAllInternal(Collection c) {
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            super.add(iter.next());
        }
    }    

    /** 
     * @see SCOCollection#getAdded()
     */  
    public Collection getAdded() { 
        return (Collection)added; 
    }     
 
    /**  
     * @see SCOCollection#getRemoved()
     */  
    public Collection getRemoved()  {  
        return (Collection)removed;  
    }      


    /** 
     * @see SCOCollection#clearInternal()
     */   
    public void clearInternal() {    
        super.clear(); 
        this.reset(); 
    }  

    /**
     * @see SCOCollection#removeInternal(Object o)
     */  
    public void removeInternal(Object o) {
        super.remove(o);
    }

    /**
     * @see SCO#unsetOwner(Object owner, int fieldNumber)
     */
    public void unsetOwner(Object owner, int fieldNumber) { 
        // Unset only if owner and fieldNumber match.
        if (this.owner == owner && this.fieldNumber == fieldNumber) {
            this.owner = null; 
            this.fieldNumber = -1;
        }
    }

    /**
     * @see SCO#setOwner (Object owner, int fieldNumber)
     */
    public void setOwner (Object owner, int fieldNumber) {
        // Set only if it was not set before.
        if (this.owner == null && owner instanceof StateManagerInternal) {
            this.owner = (StateManagerInternal)owner;    
            this.fieldNumber = fieldNumber;
        }
    }

    /** 
     * @see SCO#getOwner()
     */   
    public Object getOwner() {   
        return SCOHelper.getOwner(owner);
    } 
 
    /**  
     * @see SCO#getFieldName()
     */   
    public String getFieldName() {
        return SCOHelper.getFieldName(owner, fieldNumber);   
    }

    /**
     * Notify StateManager to mark field as dirty.
     */
    private void makeDirty() {
        if (owner != null) {
            owner.makeDirty(fieldNumber); //
        }
     }   
    /**
     * Notify StateManager to process the changes.
     */  
    private void trackUpdates(boolean modified) {
        if (modified && owner != null) {
               owner.trackUpdates(fieldNumber, this);
        }
    }

    /**
     * @see SCOCollection#getElementType() {
     */  
    public Class getElementType() {
        return elementType;
    }

    /**
     * @see SCOCollection#allowNulls() {
     */  
    public boolean allowNulls() {
        return allowNulls;
    }

    public boolean contains(Object o) {
        thaw();
        return super.contains(o);
    }
    
    public boolean containsAll(Collection c) {
        thaw();
        return super.containsAll(c);
    }
    
    public boolean isEmpty() {
        thaw();
        return super.isEmpty();
    }
    
    public Iterator iterator() {
        thaw();
        return super.iterator();
    }
    
    public int size() {
        thaw();
        return super.size();
    }
    
    public boolean equals(Object o) {
        thaw();
        return super.equals(o);
    }
    
    public int hashCode() {
        thaw();
        return super.hashCode();
    }
    
    public String toString() {
        thaw();
        return super.toString();
    }
    
    public Object[] toArray() {
        thaw();
        return super.toArray();
    }
    
    public Object[] toArray(Object[] a) {
        thaw();
        return super.toArray(a);
    }
    
    /** Returns the frozen state of this set.
     * @since 1.0.1
     */
    private boolean isFrozen() {
        return frozenElements != null;
    }
    
    /** Returns the frozen contents of this Collection, if this Collection
     * is implicitly user-orderable.  If the collection is not frozen already,
     * freeze it first.
     * @since 1.0.1
     * @return the frozen elements of this collection.
     */
    private Object[] getFrozen() {
        if (!isFrozen()) {
            frozenElements = Freezer.freeze(this, super.size());
        }
        return frozenElements;
    }
    
    /** Set the contents of this Collection from the frozen elements, if this Collection
     * is implicitly user-orderable.
     * @since 1.0.1
     * @param elements the frozen elements of this set.
     */
    public void setFrozen(Object[] elements) {
        frozenElements = elements;
    }
    
    /**
     * Thaw the frozen elements of this collection. If the elements are frozen,
     * retrieve them from the datastore and internally add them. Then reset
     * the frozen state since they're not frozen any more.
     * @since 1.0.1
     */
    private void thaw() {
        if (isFrozen()) {
            frozenElements = Freezer.thaw(this, owner, frozenElements);
        }
    }
    
    /** Create a new iterator over the frozen elements without thawing.
     * @since 1.0.1
     * @return the frozen iterator.
     */    
    public Iterator frozenIterator() {
        return Freezer.createFrozenIterator(getFrozen());
    }
    
    /** Create an iterator regardless whether the collection is frozen. 
     * If frozen, don't thaw the collection, but get a frozen iterator. 
     * If thawed, get a regular iterator.
     * @since 1.0.1
     * @return the iterator.
     */
    public Iterator eitherIterator() {
        if (isFrozen()) {
            return frozenIterator();
        } else {
            return super.iterator();
        }
    }
    
}
