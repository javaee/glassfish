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
 * sco.Vector.java
 */

package com.sun.org.apache.jdo.impl.sco;

import java.util.Collection;
import java.util.Iterator;


import com.sun.org.apache.jdo.sco.SCO;
import com.sun.org.apache.jdo.sco.SCOCollection;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.JDOUserException;


/**
 * A mutable 2nd class object that represents Vector.
 * @author Marina Vatkina
 * @version 1.0.1
 * @see java.util.Vector
 */
public class Vector extends java.util.Vector
    implements SCOCollection {

    private transient StateManagerInternal owner;

    private transient int fieldNumber = -1;

    private transient         Class elementType;
 
    private transient boolean allowNulls;

    private transient java.util.Vector added = new java.util.Vector();

    private transient java.util.Vector removed = new java.util.Vector();

    /**
     * I18N message handler
     */  
    private final static I18NHelper msg = I18NHelper.getInstance(
        "com.sun.org.apache.jdo.impl.sco.Bundle");  // NOI18N

    private final static String _Vector = "Vector"; // NOI18N

    /**
     * Constructs an empty vector so that its internal data array
     * has size <tt>10</tt> and its standard capacity increment is
     * zero.
     * @param elementType the element types allowed
     * @param allowNulls true if nulls are allowed
     */
    public Vector(Class elementType, boolean allowNulls) {
        super();
        this.elementType = elementType;
        this.allowNulls = allowNulls;
    }

    /**
     * Constructs an empty vector with the specified initial capacity and
     * with its capacity increment equal to zero.
     *
     * @param elementType         the element types allowed 
     * @param allowNulls         true if nulls are allowed 
     * @param initialCapacity   the initial capacity of the vector.
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public Vector(Class elementType, boolean allowNulls, int initialCapacity) {
        super(initialCapacity);
        this.elementType = elementType;
        this.allowNulls = allowNulls;
    }

    /** ------------------Public Methods----------------*/

    /**
     * Sets the component at the specified <code>index</code> of this
     * vector to be the specified object. The previous component at that
     * position is discarded.<p> 
     *
     * @param      obj     what the component is to be set to.
     * @param      index   the specified index.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see java.util.Vector
     */  
    public synchronized void setElementAt(Object obj, int index) {
        SCOHelper.debug(_Vector, "setElementAt"); // NOI18N

        if (obj == null) {
            SCOHelper.assertNullsAllowed(obj, allowNulls);
            // It is actualy remove
            this.removeElementAt(index);
        }

        SCOHelper.assertElementType(obj, elementType);
        // Mark the field as dirty
        this.makeDirty();

        Object o = super.elementAt(index);
        super.setElementAt(obj, index);

        if (added.remove(o) == false) {
            removed.add(o);
        }
        if (removed.remove(obj) == false) {
            added.add(obj);
        }
        // Apply updates
        this.trackUpdates(true);
    }

    /**
     * Deletes the component at the specified index. 
     *
     * @param      index   the index of the object to remove.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see java.util.Vector
     */  
    public synchronized void removeElementAt(int index) {
        SCOHelper.debug(_Vector, "removeElementAt"); // NOI18N

        // Mark the field as dirty
        this.makeDirty();

        Object obj = super.elementAt(index);
        super.removeElementAt(index);
        if (added.remove(obj) == false)
                removed.add(obj);

        // Apply updates
        this.trackUpdates(true);

    }

    /**
     * Inserts the specified object as a component in this vector at the
     * specified <code>index</code>.
     * 
     * @param      obj     the component to insert.
     * @param      index   where to insert the new component.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see java.util.Vector
     */  
    public synchronized void insertElementAt(Object obj, int index) {
        SCOHelper.debug(_Vector, "insertElementAt"); // NOI18N

        SCOHelper.assertNullsAllowed(obj, allowNulls);
        SCOHelper.assertElementType(obj, elementType);
        // Mark the field as dirty
        this.makeDirty();

        super.insertElementAt(obj, index);
        if (removed.remove(obj) == false) {
            added.add(obj);
        }

        // Apply updates
        this.trackUpdates(true);
    } 
 
    /**
     * Adds the specified component to the end of this vector,
     * increasing its size by one. 
     *   
     * @param   obj   the component to be added.
     * @see java.util.Vector 
     */  
    public synchronized void addElement(Object obj) {
        SCOHelper.debug(_Vector, "addElement"); // NOI18N

        SCOHelper.assertNullsAllowed(obj, allowNulls);
        SCOHelper.assertElementType(obj, elementType);
        // Mark the field as dirty
        this.makeDirty();

        super.addElement(obj);
        if (removed.remove(obj) == false) {
            added.add(obj); 
        }

        // Apply updates
        this.trackUpdates(true);
    }

    /**
     * Removes the first (lowest-indexed) occurrence of the argument
     * from this vector. 
     *   
     * @param   obj   the component to be removed.
     * @return  <code>true</code> if the argument was a component of this
     *          vector; <code>false</code> otherwise.
     * @see java.util.Vector
     */   
    public synchronized boolean removeElement(Object obj) {
        SCOHelper.debug(_Vector, "removeElement"); // NOI18N

        // Because java.util.Vector.removeElement(Object) calls internally 
        // removeElementAt(int) which is not supported, we cannot rely on jdk. 
        // We need to process remove here.

        // Mark the field as dirty
        this.makeDirty();

        int i = super.indexOf(obj);
        if (i > -1) {
            super.removeElementAt(i);

            if (added.remove(obj) == false) {
                removed.add(obj);
            }
            // Apply updates
            this.trackUpdates(true);
            return true;
        }
        return false;

    }

    /**
     * Removes all components from this vector and sets its size to zero.<p>
     *
     * @see java.util.Vector
     */ 
    public synchronized void removeAllElements() {
        SCOHelper.debug(_Vector, "removeElements"); // NOI18N

        // Mark the field as dirty
        this.makeDirty();

        for (Iterator iter = super.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (added.remove(o) == false) {
                removed.add(o);
            }
        }
        added.clear();

        super.removeAllElements();

        // Apply updates
        this.trackUpdates(true);
    }



    /**
     * Replaces the element at the specified position in this Vector with the
     * specified element.
     *   
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @exception ArrayIndexOutOfBoundsException index out of range
     *            (index &lt; 0 || index &gt;= size()).
     * @exception IllegalArgumentException fromIndex &gt; toIndex.
     * @see java.util.Vector 
     */  
    public synchronized Object set(int index, Object element) {
        SCOHelper.debug(_Vector, "set"); // NOI18N

        if (element == null) {
            SCOHelper.assertNullsAllowed(element, allowNulls);
            // It is actualy remove
            return this.remove(index); 
        }

        SCOHelper.assertElementType(element, elementType);
        // Mark the field as dirty
        this.makeDirty();

        Object o = super.set(index, element);
        if (added.remove(o) == false) {
                removed.add(o);
        }
        if (removed.remove(element) == false) {
                added.add(element);
        }

        // Apply updates
        this.trackUpdates(true);

        return o;
    } 


    /**
     * Appends the specified element to the end of this Vector.
     *   
     * @param o element to be appended to this Vector.
     * @return true (as per the general contract of Collection.add).
     * @see java.util.Vector
     */  
    public synchronized boolean add(Object o) {
        SCOHelper.debug(_Vector, "add"); // NOI18N

        SCOHelper.assertNullsAllowed(o, allowNulls);
        SCOHelper.assertElementType(o, elementType);
        // Mark the field as dirty
        this.makeDirty();

        if (removed.remove(o) == false) {
            added.add(o);
        }

        boolean modified = super.add(o);

        // Apply updates
        this.trackUpdates(modified);

        return modified;
    }    

    /**
     * Removes the first occurrence of the specified element in this Vector
     * If the Vector does not contain the element, it is unchanged.  
     *
     * @param o element to be removed from this Vector, if present.
     * @return true if the Vector contained the specified element.
     * @see java.util.Vector 
     */   
    public boolean remove(Object o) {
        SCOHelper.debug(_Vector, "remove"); // NOI18N

        return this.removeElement(o);
    }

    /**
     * Inserts the specified element at the specified position in this Vector.
     *   
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @exception ArrayIndexOutOfBoundsException index is out of range
     *            (index &lt; 0 || index &gt; size()).
     * @see java.util.Vector
     */  
    public void add(int index, Object element) {
        SCOHelper.debug(_Vector, "add by index"); // NOI18N

        this.insertElementAt(element, index);
    }

    /**
     * Removes the element at the specified position in this Vector.
     * shifts any subsequent elements to the left (subtracts one from their
     * indices).  Returns the element that was removed from the Vector.
     *   
     * @param index the index of the element to removed.
     * @exception ArrayIndexOutOfBoundsException index out of range (index
     *            &lt; 0 || index &gt;= size()).
     * @see java.util.Vector 
     */   
    public synchronized Object remove(int index) {
        SCOHelper.debug(_Vector, "remove by index"); // NOI18N


        // Mark the field as dirty
        this.makeDirty();

        Object obj = super.remove(index);
        if (added.remove(obj) == false) {
            removed.add(obj);
        }

        // Apply updates
        this.trackUpdates(true);

        return obj;
    } 

    /**
     * Removes all of the elements from this Vector.  The Vector will
     * be empty after this call returns (unless it throws an exception).
     *   
     * @see java.util.Vector
     */ 
    public void clear() {
        SCOHelper.debug(_Vector, "clear"); // NOI18N

        this.removeAllElements();
    }

    /**
     * Appends all of the elements in the specified Collection to the end of
     * this Vector, in the order that they are returned by the specified
     * Collection's Iterator.  
     *   
     * @param c elements to be inserted into this Vector.
     * @see java.util.Vector
     */  
    public synchronized boolean addAll(Collection c) {
        SCOHelper.debug(_Vector, "addAll"); // NOI18N

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

        // Mark the field as dirty
        this.makeDirty();

        removed.removeAll(c);
        added.addAll(c);

        boolean modified = super.addAll(c);

        // Apply updates
        this.trackUpdates(modified);

        return modified;
    }

    /**
     * Removes from this Vector all of its elements that are contained in the
     * specified Collection.
     *
     * @return true if this Vector changed as a result of the call.
     * @see java.util.Vector 
     */   
    public synchronized boolean removeAll(Collection c) {
        SCOHelper.debug(_Vector, "removeAll"); // NOI18N

        boolean modified = false;
        // Mark the field as dirty
        this.makeDirty();

        Iterator e = c.iterator();
        while (e.hasNext()) {
            Object o = e.next();
            if(super.contains(o)) {
                removeInternal(o);
                if (added.remove(o) == false)
                    removed.add(o);
                    modified = true;
            }
        }

        // Apply updates
        this.trackUpdates(modified);

        return modified;
    } 

    /**
     * Inserts all of the elements in in the specified Collection into this
     * Vector at the specified position.  Shifts the element currently at
     * that position (if any) and any subsequent elements to the right
     * (increases their indices).  The new elements will appear in the Vector
     * in the order that they are returned by the specified Collection's
     * iterator.
     *   
     * @param index index at which to insert first element
     *              from the specified collection.
     * @param c elements to be inserted into this Vector.
     * @exception ArrayIndexOutOfBoundsException index out of range (index
     *            &lt; 0 || index &gt; size()).
     * @see java.util.Vector  
     */   
    public synchronized boolean addAll(int index, Collection c) {
        SCOHelper.debug(_Vector, "addAll from index"); // NOI18N

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

        // Mark the field as dirty
        this.makeDirty();

        removed.removeAll(c); 
        added.addAll(c); 

        boolean modified = super.addAll(index, c);

        // Apply updates
        this.trackUpdates(modified);

        return modified;
    }    

    /**
     * Retains only the elements in this Vector that are contained in the
     * specified Collection.  
     *   
     * @return true if this Vector changed as a result of the call.
     * @see java.util.Vector   
     */    
    public synchronized boolean retainAll(Collection c)  {
        SCOHelper.debug(_Vector, "retainAll"); // NOI18N

        boolean modified = false;
        java.util.ArrayList v = new java.util.ArrayList();

        // Mark the field as dirty
        this.makeDirty();
        for (Iterator iter = super.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (!c.contains(o)) {
                v.add(o);
                if (added.remove(o) == false) {
                    removed.add(o);
                }
                modified = true;
            }
        }

        // Now remove the rest (stored in "v")
        for (Iterator iter = v.iterator(); iter.hasNext();) {
            removeInternal(iter.next());
        }

        // Apply updates
        this.trackUpdates(modified);


        return modified;
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
        SCOHelper.debug(_Vector, "clone"); // NOI18N

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
    }

    /**
     * @see SCOCollection#addInternal(Object o)
     */
    public void addInternal(Object o) {
        super.addElement(o);
    }

 
    /** 
     * @see SCOCollection#addAllInternal(Collection c)
     */ 
    public void addAllInternal(Collection c) { 
        super.addAll(c); 
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
    public Collection getRemoved() { 
        return (Collection)removed; 
    }     

    /**
     * @see SCOCollection#clearInternal()
     */  
    public void clearInternal() {
        //Cannot call super.clear() as it internally calls removeAllElements()
        // which causes marking field as dirty.

        int s = super.size() - 1;

        // Need to loop backwards to avoid resetting size of the collection
        for (int i = s; i > -1; i--) {
                super.removeElementAt(i);
        }
                
        this.reset();
    }


    /**
     * @see SCOCollection#removeInternal(Object o)
     */  
    public void removeInternal(Object o) {
        int i = super.indexOf(o);
        super.remove(i);
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
     * Marks object dirty
     */
    private void makeDirty() {
        if (owner != null) {
            owner.makeDirty(fieldNumber); //
        }
     }   

    /**
     * Apply changes 
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

    /** Get an iterator over the frozen elements of this collection. 
     * This class does not require freezing, so this method returns 
     * a standard iterator.
     * @since 1.0.1
     * @return an iterator over the elements.
     */
    public Iterator frozenIterator() {
        return iterator();
    }
    
    /** Set the contents of this Collection from the frozen elements.
     * This class does not support explicit frozen operations, and this method
     * always throws an exception.
     * @since 1.0.1
     * @param elements not used.
     */
    public void setFrozen(Object[] elements) {
        throw new JDOFatalInternalException(msg.msg("EXC_UnsupportedFreezerOperation")); //NOI18N
    }
    
    /** Get an iterator regardless of whether the collection is frozen. 
     * This class does not support frozen operations and always returns 
     * a regular iterator.
     * @since 1.0.1
     * @return an iterator over the elements.
     */
    public Iterator eitherIterator() {
        return iterator();
    }
    
}
