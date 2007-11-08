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
 * SCOCollection.java
 *
 * created April 3, 2000
 *
 * @author Marina Vatkina
 * @version 1.0.1
 */

package com.sun.org.apache.jdo.sco;
import java.util.Collection;
import java.util.Iterator;

public interface SCOCollection extends java.util.Collection, SCO {
    /**
     * Resets removed and added lists after flush
     */
    void reset();

    /**
     * Adds object to the Collection without recording
     * the event. Used internaly to initially populate the Collection
     */ 
    void addInternal(Object o);

    /**
     * Adds objects of the given Collection to this Collection without recording
     * the event. Used internaly to initially populate the Collection
     */ 
    void addAllInternal(Collection c);


    /**
     * Clears Collection without recording
     * the event. Used internaly to clear the Collection
     */
    void clearInternal();


    /**
     * Removes element from the Collection without recording
     * the event. Used internaly to update the Collection
     */
    void removeInternal(Object o);

    /**
     * Returns the Collection of added elements
     *
     * @return Collection of the added elements as java.util.Collection
     */
    Collection getAdded();

    /**
     * Returns the Collection of removed elements
     *
     * @return Collection of the removed elements as java.util.Collection
     */
    Collection getRemoved();

    /**
     * Returns the element type assignment compatible with all
     * added elements of this collection.
     * 
     * @return the element type assignment compatible with all
     * added elements.
     */
    Class getElementType();

    /**
     * Returns whether nulls are permitted as elements.
     *   
     * @return true if nulls are permitted as elements. 
     */ 
    boolean allowNulls();

    /**
     * Set the contents of this Collection from the frozen elements.
     * @since 1.0.1
     * @param elements the frozen elements.
     */
    void setFrozen(Object[] elements);
    
    /**
     * Get an iterator over the frozen elements of this collection. This allows
     * iterator of the elements without thawing them, as is needed for
     * transcription.
     * @since 1.0.1
     * @return an iterator over the frozen elements.
     */
    Iterator frozenIterator();
    
    /** Get an iterator regardless of whether the map is frozen.
     * If frozen, get a frozen iterator.
     * If thawed, get a regular iterator.
     * @since 1.0.1
     * @return the iterator over the elements.
     */
    Iterator eitherIterator();
    
}
