/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.jaxb.internal;

import java.util.Map;

import javax.xml.namespace.QName;

import org.glassfish.hk2.xml.internal.ModelImpl;

/**
 * @author jwells
 *
 */
public interface NamespaceBeanLikeMap {
    /**
     * Gets the value for key based on the namespace.
     * Locking must be done by the caller
     * 
     * @param namespace if null or the empty string the
     * default namespace will be used
     * @param key the non-null key
     * @return The value if found (may be null) or
     * null if not set (use {@link #isSet(String, String)}
     * to determine if a value has been set
     */
    public Object getValue(String namespace, String key);
    
    /**
     * Sets the value for key based on the namespace.
     * Locking must be done by the caller
     * 
     * @param namespace if null or the empty string the
     * default namespace will be used
     * @param key the non-null key
     * @param value The value to set this key to (may be null)
     */
    public void setValue(String namespace, String key, Object value);
    
    /**
     * Determines if the value for key based on namespace
     * has been explicitly set.  Locking must be done
     * by the caller
     * 
     * @param nanmespace if null or the empty string the
     * default namespace will be used
     * @param key the non-null key
     * @return true if the value is explicitly set, false
     * otherwise
     */
    public boolean isSet(String nanmespace, String key);
    
    /**
     * Create a backup of all namespaces at this point.
     * Locking must be done by the caller
     */
    public void backup();
    
    /**
     * Either drop the backup data or restore the
     * current data to the backup. Locking must
     * be done by the caller
     * 
     * @param drop if true the backup should be
     * dropped, if false the current data should
     * be made the same as the backup
     */
    public void restoreBackup(boolean drop);
    
    /**
     * Gets the bean-like map given the set
     * of prefixes to use for the various
     * namespaces.  Note that if a namespace is
     * not found in the map the values from that
     * namespace will not be included in the 
     * returned map.  Locking must be handled
     * by the caller
     * 
     * @param namespaceToPrefixMap A map from namespace
     * to the prefix that should be put on the keys for
     * the namespace
     * @return A map with the fully qualified bean-like names
     */
    public Map<String, Object> getBeanLikeMap(Map<String, String> namespaceToPrefixMap);
    
    /**
     * Gets a map from QName to value.  Default namespace
     * is not taken into account
     * 
     * @return A non-null map from QName to value
     */
    public Map<QName, Object> getQNameMap();
    
    /**
     * Does a shallow copy from another namespace bean-like map to this one
     * 
     * @param copyFrom The other namesapce bean-like map to copy
     * @param model The model to use to determine what fields to copy
     * @param copyReferences true if references should also be copied
     */
    public void shallowCopy(NamespaceBeanLikeMap copyFrom, ModelImpl model, boolean copyReferences);
    
    /**
     * Gets a raw copy of the bean-like map.  The outer map has
     * namespace keys (including one for default) and the inner
     * map is the bean-like map for that namespace.  The map returned
     * is not a copy, so any changes to it will affect the underlying
     * object (so don't change it)
     * 
     * @return
     */
    public Map<String, Map<String, Object>> getNamespaceBeanLikeMap();
}
