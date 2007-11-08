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
 * Extent.java
 *
 * Created on December 8, 2000, 3:06 PM
 */

package com.sun.persistence.support;

import java.util.Iterator;

/** Instances of the <code>Extent</code> class represent the entire collection
 * of instances in the data store of the candidate class
 * possibly including its subclasses.
 * <P>The <code>Extent</code> instance has two possible uses:
 * <ol>
 * <li>to iterate all instances of a particular class 
 * <li>to execute a <code>Query</code> in the data store over all instances
 * of a particular class
 * </ol>
 * @author Craig Russell
 * @version 1.0
 */
public interface Extent {
        
    /** Returns an iterator over all the instances in the <code>Extent</code>.
     * The behavior of the returned iterator might depend on the setting of the
     * <code>ignoreCache</code> flag in the owning <code>PersistenceManager</code>.
     * @return an iterator over all instances in the <code>Extent</code>
     */
    Iterator iterator();

    /** Returns whether this <code>Extent</code> was defined to contain subclasses.
     * @return true if this <code>Extent</code> was defined to contain instances
     * that are of a subclass type.
     */    
    boolean hasSubclasses();

    /** An <code>Extent</code> contains all instances of a particular class in the data
     * store; this method returns the <code>Class</code> of the instances.
      * @return the <code>Class</code> of instances of this <code>Extent</code>.
      */
    Class getCandidateClass();

    /** An <code>Extent</code> is managed by a <code>PersistenceManager</code>;
     * this method gives access to the owning <code>PersistenceManager</code>.
     * @return the owning <code>PersistenceManager</code>
     */
    PersistenceManager getPersistenceManager();
    
    /** Close all <code>Iterator</code>s associated with this <code>Extent</code> instance.
     * <code>Iterator</code>s closed by this method will return <code>false</code>
     * to <code>hasNext()</code> and will throw
     * <code>NoSuchElementException</code> on <code>next()</code>.
     * The <code>Extent</code> instance can still be used
     * as a parameter of <code>Query.setExtent</code>, and to get an <code>Iterator</code>.
     */    
    void closeAll ();
    
    /** Close an <code>Iterator</code> associated with this <code>Extent</code> instance.
     * <code>Iterator</code>s closed by this method will return <code>false</code>
     * to <code>hasNext()</code> and will throw <code>NoSuchElementException</code>
     * on <code>next()</code>. The <code>Extent</code> instance can still be used
     * as a parameter of <code>Query.setExtent</code>, and to get an <code>Iterator</code>.
     * @param it an <code>Iterator</code> obtained by the method
     * <code>iterator()</code> on this <code>Extent</code> instance.
     */    
    void close (Iterator it);
}

