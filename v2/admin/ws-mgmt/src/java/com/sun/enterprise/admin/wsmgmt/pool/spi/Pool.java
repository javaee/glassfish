/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.admin.wsmgmt.pool.spi;

import java.util.Collection;

/**
 * Pool used to keep track of SOAP messages.
 */
public interface Pool {

    /**
     * Returns the name of this pool. 
     *
     * @return  name of this pool
     */
    public String getName();

    /**
     * Returns the object for the given key. 
     *
     * @param  key  key used to put objects in the pool
     *
     * @return  object for the given key or null
     */
    public Object get(Object key);

    /**
     * Adds the object to the pool. 
     *
     * @param  key  key used to put this object
     * @param  val  actual object to be added to the pool
     *
     * @return  previous value for this key or null
     */
    public Object put(Object key, Object val);

    /**
     * Removes this keyed entry from the pool.
     *
     * @param  key  key of the entry that is targeted to be removed
     *
     * @return  removed entry or null if key not found
     */
    public Object remove(Object key);

    /**
     * Removes all mappings from this pool.
     */
    public void clear();

    /**
     * Returns the number of mappings in this pool. 
     *
     * @return  current number of mappings in this pool
     */
    public int size();

    /**
     * Returns the maximum number of mappings allowed in this pool.
     *
     * @return  max number of mappings allowed
     */
    public int getMaxSize();

    /**
     * Returns true if this pool contains mapping for the specified key.
     *
     * @param  key  the presence of the key to be tested
     *
     * @return  true if this pool contains mapping for the specified key
     */
    public boolean containsKey(Object key);

    /**
     * Returns true if this pool contains mapping for the specified value. 
     * 
     * @param  val  the presence of the value to be tested
     *
     * @return  true if this pool contains mapping for the specified value
     */
    public boolean containsValue(Object val);

    /**
     * Returns a collection view of the values contained in this pool. 
     *
     * @return  a collection view of the values contained in this pool
     */
    public Collection values();

    /**
     * Resets the max size of the pool. If new size is less than current 
     * size, all extra entries in the pool will be removed.
     *
     * @param  size  new max size of the pool
     */
    public void resize(int size);
}
