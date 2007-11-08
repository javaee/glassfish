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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.Vector;

/**
 * List of configuration changes. A user interface feature allows users to
 * apply changes after one or more edits to configuration attributes. This
 * class keeps track of configuration changes.
 */
public class ConfigChangeList implements SortedMap, Serializable {

    private Vector names;
    private Vector values;
    private HashMap nameMap;

    /**
     * Create an empty ConfigChangeList.
     */
    public ConfigChangeList() {
        names = new Vector();
        values = new Vector();
        nameMap = new HashMap();
    }

    private ConfigChangeList(Vector names, Vector values, HashMap nameMap) {
        this.names = names;
        this.values = values;
        this.nameMap = nameMap;
    }

    /**
     * Add a config change to list. If a config change for the same property
     * already exists, then the specified change value is added to the end of
     * the list of changes. The name is the XPath of the attribute for which
     * change is being addeed.
     */
    public void addConfigChange(String name, Object value) {
    }

    /**
     * Remove all config changes for the specified name from the list. The name
     * is the XPath of the attribute that needs to be removed.
     */
    public void removeConfigChange(String name) {
    }
 
    /**
     * Remove specified config change from the list.
     */
    public void removeConfigChange(String name, Object value) {
    }

    public java.util.SortedMap tailMap(java.lang.Object obj) {
        return null;
    }
    
    public java.lang.Object firstKey() {
        return null;
    }
    
    public java.lang.Object put(java.lang.Object obj, java.lang.Object obj1) {
        return null;
    }
    
    public java.lang.Object remove(java.lang.Object obj) {
        return null;
    }
    
    public java.util.Set keySet() {
        return null;
    }
    
    public void clear() {
    }
    
    public java.util.Collection values() {
        return null;
    }
    
    public java.util.SortedMap subMap(java.lang.Object obj, java.lang.Object obj1) {
        return null;
    }
    
    public boolean containsKey(java.lang.Object obj) {
        return false;
    }
    
    public int size() {
        return 0;
    }
    
    public java.util.Set entrySet() {
        return null;
    }
    
    public java.util.SortedMap headMap(java.lang.Object obj) {
        return null;
    }
    
    public boolean containsValue(java.lang.Object obj) {
        return false;
    }
    
    public void putAll(java.util.Map map) {
    }
    
    public boolean isEmpty() {
        return true;
    }
    
    public java.lang.Object get(java.lang.Object obj) {
        return null;
    }
    
    public java.util.Comparator comparator() {
        return null;
    }
    
    public java.lang.Object lastKey() {
        return null;
    }
    
}
