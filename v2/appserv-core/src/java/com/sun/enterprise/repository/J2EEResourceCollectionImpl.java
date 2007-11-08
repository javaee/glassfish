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
package com.sun.enterprise.repository;

import java.util.*;

/**
 * Concrete implementation class for J2EEResourceCollection.
 *
 * @author Kenneth Saks
 */ 
public class J2EEResourceCollectionImpl implements J2EEResourceCollection {

    private Map resourceSets_;

    public J2EEResourceCollectionImpl() {
        resourceSets_ = new HashMap();
    }

    public Set getResourcesByType(int type) {
        Map resourceSet = getResourcesInternal(type);
        Collection collection = resourceSet.values();
        Set shallowCopy = new HashSet();
        for(Iterator iter = collection.iterator(); iter.hasNext();) {
            J2EEResource next = (J2EEResource) iter.next();
            shallowCopy.add(next);
        }
        return shallowCopy;
    }
    
    public Set getAllResources() {
        Set allResources = new HashSet();
        Collection resourcesByType = resourceSets_.values();
        for(Iterator iter = resourcesByType.iterator(); iter.hasNext(); ) {
            HashMap next = (HashMap) iter.next();
            allResources.addAll(next.values());
        }
        return allResources;
    }

    public void addResource(J2EEResource resource) {
        Map resourceSet = getResourcesInternal(resource.getType());
        resourceSet.put(resource.getName(), resource);
    }

    public boolean removeResource(J2EEResource resource) {
        Map resourceSet = getResourcesInternal(resource.getType());
        boolean removed = 
            (resourceSet.remove(resource.getName()) != null);
        return removed;
    }

    public void removeAllResourcesByType(int type) {
        resourceSets_.remove(new Integer(type));
    }

    public J2EEResource getResourceByName(int type, String resourceName) {
        Map resourceSet = getResourcesInternal(type);
        return (J2EEResource) resourceSet.get(resourceName);
            
    }

    private Map getResourcesInternal(int type) {
        Map resourceSet = (Map) resourceSets_.get(new Integer(type));
        if( resourceSet == null ) {
            resourceSet = new HashMap();
            resourceSets_.put(new Integer(type), resourceSet);
        }
        return resourceSet;
    }

}
