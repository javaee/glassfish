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
package com.sun.gjc.spi.base.datastructure;

import com.sun.gjc.spi.ManagedConnectionFactory;
import com.sun.gjc.spi.base.*;
import com.sun.logging.LogDomains;

import java.util.*;
import java.util.logging.Logger;
import java.sql.SQLException;

/**
 *
 * @author Shalini M
 */
public class LRUCacheImpl implements Cache {

    /**
     * Stores the objects for statement caching
     */
    private Map<CacheObjectKey, CacheEntry> list;
    /**
     * Size of the cache
     */
    private int maxSize ;
    protected final static Logger _logger;

    static {
        _logger = LogDomains.getLogger(ManagedConnectionFactory.class, LogDomains.RSR_LOGGER);
    }

    public LRUCacheImpl(int maxSize){
        this.maxSize = maxSize;
        list = new LinkedHashMap<CacheObjectKey, CacheEntry>();
    }

    /**
     * Check if an entry is found for this key object. If found, the entry is
     * put in the result object and back into the list.
     * 
     * @param key key whose mapping entry is to be checked.
     * @return result object that contains the key with the entry if not 
     * null when
     * (1) object not found in cache
     */
    public Object checkAndUpdateCache(CacheObjectKey key) {
        Object result = null;
        CacheEntry entry = list.get(key);        
        if(entry != null) {
                result = entry.entryObj;
        }
        return result;
    }
    
    /**
     * Add the key and entry value into the cache.
     * @param key key that contains the sql string and its type (PS/CS)
     * @param o entry that is the wrapper of PreparedStatement or 
     * CallableStatement
     * @param force If the already existing key is to be overwritten
     */
    public void addToCache(CacheObjectKey key, Object o, boolean force) {
        if(force || !list.containsKey(key)){
            //overwrite or if not already found in cache

            if(list.size() >= maxSize){
                purge();
            }
            CacheEntry entry = new CacheEntry(key, o);
            list.put(key, entry);
        }
    }

    /**
     * Clears the statement cache
     */
    public void clearCache(){
       _logger.fine("clearing objects in cache");
       list.clear();
    }

    public void flushCache() {
        while(list.size()!=0){
            purge();
        }
    }

    public void purge() {
        Iterator keyIterator = list.keySet().iterator();
        while(keyIterator.hasNext()){
            CacheObjectKey key = (CacheObjectKey)keyIterator.next();
            CacheEntry entry = list.get(key);
            try{
                PreparedStatementWrapper ps = (PreparedStatementWrapper)entry.entryObj;
                ps.setCached(false);
                ps.close();
            }catch(SQLException e){
                //ignore
            }
            keyIterator.remove();
            break;
        }
    }

    /**
     * Returns the number of entries in the statement cache
     * @return has integer value
     */
    public int getSize() {
       return list.size();
    }

    /**
     * Cache object that has a key and an entry. This is used to put inside the 
     * statement cache.
     */
    public class CacheEntry{
        private CacheObjectKey key;
        private Object entryObj;

        public CacheEntry(CacheObjectKey key, Object o){
            this.key = key;
            this.entryObj = o;
        }
    }

    public Set getObjects(){
        //TODO-SC-DEFER can the set be "type-safe"
        Set set = new HashSet();
        for(CacheEntry entry : list.values()){
            set.add(entry.entryObj);
        }
        return set;
    }

    public boolean isSynchronized() {
        return false;
    }
}
