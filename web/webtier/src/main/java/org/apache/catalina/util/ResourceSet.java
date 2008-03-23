

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.util;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Extended implementation of <strong>HashSet</strong> that includes a
 * <code>locked</code> property.  This class can be used to safely expose
 * resource path sets to user classes without having to clone them in order
 * to avoid modifications.  When first created, a <code>ResourceMap</code>
 * is not locked.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:28:20 $
 */

public final class ResourceSet extends HashSet {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new, empty set with the default initial capacity and
     * load factor.
     */
    public ResourceSet() {

        super();

    }


    /**
     * Construct a new, empty set with the specified initial capacity and
     * default load factor.
     *
     * @param initialCapacity The initial capacity of this set
     */
    public ResourceSet(int initialCapacity) {

        super(initialCapacity);

    }


    /**
     * Construct a new, empty set with the specified initial capacity and
     * load factor.
     *
     * @param initialCapacity The initial capacity of this set
     * @param loadFactor The load factor of this set
     */
    public ResourceSet(int initialCapacity, float loadFactor) {

        super(initialCapacity, loadFactor);

    }


    /**
     * Construct a new set with the same contents as the existing collection.
     *
     * @param coll The collection whose contents we should copy
     */
    public ResourceSet(Collection coll) {

        super(coll);

    }


    // ------------------------------------------------------------- Properties


    /**
     * The current lock state of this parameter map.
     */
    private boolean locked = false;


    /**
     * Return the locked state of this parameter map.
     */
    public boolean isLocked() {

        return (this.locked);

    }


    /**
     * Set the locked state of this parameter map.
     *
     * @param locked The new locked state
     */
    public void setLocked(boolean locked) {

        this.locked = locked;

    }


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager("org.apache.catalina.util");


    // --------------------------------------------------------- Public Methods


    /**
     * Add the specified element to this set if it is not already present.
     * Return <code>true</code> if the element was added.
     *
     * @param o The object to be added
     *
     * @exception IllegalStateException if this ResourceSet is locked
     */
    public boolean add(Object o) {

        if (locked)
            throw new IllegalStateException
              (sm.getString("resourceSet.locked"));
        return (super.add(o));

    }


    /**
     * Remove all of the elements from this set.
     *
     * @exception IllegalStateException if this ResourceSet is locked
     */
    public void clear() {

        if (locked)
            throw new IllegalStateException
              (sm.getString("resourceSet.locked"));
        super.clear();

    }


    /**
     * Remove the given element from this set if it is present.
     * Return <code>true</code> if the element was removed.
     *
     * @param o The object to be removed
     *
     * @exception IllegalStateException if this ResourceSet is locked
     */
    public boolean remove(Object o) {

        if (locked)
            throw new IllegalStateException
              (sm.getString("resourceSet.locked"));
        return (super.remove(o));

    }


}
