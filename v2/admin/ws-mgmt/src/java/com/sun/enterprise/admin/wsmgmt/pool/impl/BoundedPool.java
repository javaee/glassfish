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
package com.sun.enterprise.admin.wsmgmt.pool.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import com.sun.enterprise.admin.wsmgmt.pool.spi.Pool;

/**
 * Pool used to keep track of SOAP messages. This implementation of 
 * the pool keeps a fixed amount of entries in the pool. If pool 
 * size exceeds the max allowed entry, the oldest entry is removed from
 * the pool. 
 */
public class BoundedPool implements Pool{

    /**
     * Constructor.
     *
     * @param  name  name of the pool
     */
    public BoundedPool(String name) {
        _name    = name;
        _keyList = Collections.synchronizedList(new ArrayList());
        _map     = new HashMap();
    }

    /**
     * Constructor.
     *
     * @param  name  name of the pool
     * @param  size  max size of the pool
     */
    public BoundedPool(String name, int size) {

        this(name);
        _maxSizeAllowed  = size;
    }

    /**
     * Returns the object for the given key. 
     *
     * @param  key  key used to put objects in the pool
     *
     * @return  object for the given key or null
     */
    public Object get(Object key) {
        return _map.get(key);
    }

    /**
     * Adds the object to the pool. If pool size is greater 
     * than the max allowed, the oldest entry is removed 
     * from the pool. 
     *
     * @param  key  key used to put this object
     * @param  val  actual object to be added to the pool
     *
     * @return  previous value for this key or null
     */
    public Object put(Object key, Object val) {

        if ((key == null) && (val == null)) {
            throw new IllegalArgumentException();
        }

        Object oldVal = null;

        synchronized (this) {
            oldVal = _map.put(key, val);
            _keyList.add(key);

            // remove the extra entry from the pool
            if (_keyList.size() > _maxSizeAllowed) {
                Object rkey = _keyList.remove(0);
                assert (rkey != null);

                Object rval = _map.remove(rkey);
                assert (rval != null);
            }
        }

        return oldVal;
    }

    /**
     * Removes this keyed entry from the pool.
     *
     * @param  key  key of the entry that is targeted to be removed
     *
     * @return  removed entry or null if key not found
     */
    public Object remove(Object key) {
        Object val = null;

        synchronized (this) {
            _keyList.remove(key);
            val = _map.remove(key);
        }

        return val;
    }

    /**
     * Removes all mappings from this pool.
     */
    public void clear() {
        synchronized (this) {
            _map.clear();
            _keyList.clear();
        }
    }

    /**
     * Returns the number of mappings in this pool. 
     *
     * @return  current number of mappings in this pool
     */
    public int size() {
        return _map.size();
    }

    /**
     * Returns the maximum number of mappings allowed in this pool.
     *
     * @return  max number of mappings allowed
     */
    public int getMaxSize() {
        return _maxSizeAllowed;
    }

    /**
     * Returns true if this pool contains mapping for the specified key.
     *
     * @param  key  the presence of the key to be tested
     *
     * @return  true if this pool contains mapping for the specified key
     */
    public boolean containsKey(Object key) {
        return _map.containsKey(key);
    }

    /**
     * Returns true if this pool contains mapping for the specified value. 
     * 
     * @param  val  the presence of the value to be tested
     *
     * @return  true if this pool contains mapping for the specified value
     */
    public boolean containsValue(Object val) {
        return _map.containsValue(val);
    }

    /**
     * Returns a collection view of the values contained in this pool. 
     *
     * @return  a collection view of the values contained in this pool
     */
    public Collection values() {
        return _map.values();
    }

    /**
     * Resets the max size of the pool. If new size is less than current 
     * size, all extra entries in the pool are removed.
     *
     * @param  size  new max size of the pool
     */
    public void resize(int size) {

        if (size < 1) {
            return;
        }

        if (size == _maxSizeAllowed) {
            // do nothing
        } else if (size > _maxSizeAllowed) {
            synchronized (this) {
                _maxSizeAllowed = size;
            }
        } else {
            // remove the extra entries from the pool
            synchronized (this) {
                int currentSize = _map.size();
                int diff = currentSize - size;

                // remove extra entries if current size is bigger than diff
                for (int i=0; i<diff; i++) {
                    Object key = _keyList.get(i);
                    assert (key != null);

                    if (key != null) {
                        Object val = _map.remove(key);
                        assert (val != null);
                    }
                }
                for(int i=0; i <diff; i++) {
                    _keyList.remove(0);
                }
                _maxSizeAllowed = size;
            }
        }
    }

    /**
     * Returns the name of the pool.
     *
     * @return  name of the pool
     */
    public String getName() {
        return _name;
    }

    // ---- INSTANCE VARIABLES - PRIVATE -------------------------
    private List _keyList        = null;
    private Map _map             = null;
    private String _name         = null;
    private int _maxSizeAllowed  = 25;
}
