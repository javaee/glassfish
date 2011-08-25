/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.hk2.component;

import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A very primitive cache of at most one entry, that uses both SoftReference(s) to
 * the key as well as the value it caches.  Useful to provide minimal caching
 * without imposing on heap.
 * 
 * @author Jeff Trent
 */
public class SoftCache<K, V> {

  private Logger logger = Logger.getLogger(SoftCache.class.getName());
  
  protected SoftReference<K> key;
  protected SoftReference<V> value;
  
  /**
   * Returns either the cached value if hasn't been reclaimed by GC.  Otherwise invokes
   * the callable to refresh the cached value, and caching it again in the process.
   * 
   * @param key the key; must be non-null
   * @param callable the callable in case the cache is empty; must be non-null
   * @return the value (either from cache or populated by the callable as a last resort)
   */
  public synchronized V get(K k, Callable<V> callable) {
    assert(null != callable);
    K thisK = (null == key) ? null : key.get();
    if (k.equals(thisK)) {
      if (null == value) {
        return usingCacheValue(null);
      } else {
        V thisV = value.get();
        if (null != thisV) {
          return usingCacheValue(thisV);
        }
      }
    }

    key = new SoftReference<K>(k);
    try {
      V thisV = callable.call();
      return cacheValue(value, thisV);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected  V cacheValue(SoftReference<V> value2, V thisV) {
    if (null == thisV) {
      value = null;
    } else {
      value = new SoftReference<V>(thisV);
    }
    
    logger.log(Level.FINEST, "caching value");
    return thisV;
  }

  protected V usingCacheValue(V thisV) {
    logger.log(Level.FINEST, "using cached value");
    return thisV;
  }

  /**
   * Clears the cache
   */
  public void clear() {
    logger.log(Level.FINEST, "clearing cached value");
    key = null;
    value = null;
  }

}
