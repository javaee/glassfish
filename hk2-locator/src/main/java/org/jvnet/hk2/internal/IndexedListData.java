/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;

/**
 * This object contains a list of values.  The list is not always sorted, but will
 * always be returned sorted.
 * 
 * All of the methods on here must be called with lock held.
 * 
 * @author jwells
 *
 */
public class IndexedListData {
    private final ArrayList<SystemDescriptor<?>> unsortedList = new ArrayList<SystemDescriptor<?>>();
    private volatile boolean sorted = true;
    
    public Collection<SystemDescriptor<?>> getSortedList() {
        if (sorted) return unsortedList;
        
        synchronized (this) {
            if (sorted) return unsortedList;
        
            if (unsortedList.size() <= 1) {
                sorted = true;
                return unsortedList;
            }
            
            Collections.sort(unsortedList, ServiceLocatorImpl.DESCRIPTOR_COMPARATOR);
        
            sorted = true;
            return unsortedList;
        }
    }
    
    public synchronized void addDescriptor(SystemDescriptor<?> descriptor) {
        unsortedList.add(descriptor);
        
        if (unsortedList.size() > 1) {
            sorted = false;
        }
        else {
            sorted = true;
        }
        
        descriptor.addList(this);
    }
    
    public synchronized void removeDescriptor(SystemDescriptor<?> descriptor) {
        ListIterator<SystemDescriptor<?>> iterator = unsortedList.listIterator();
        while (iterator.hasNext()) {
            SystemDescriptor<?> candidate = iterator.next();
            if (ServiceLocatorImpl.DESCRIPTOR_COMPARATOR.compare(descriptor, candidate) == 0) {
                iterator.remove();
                break;
            }
        }
        
        if (unsortedList.size() > 1) {
            sorted = false;
        }
        else {
            sorted = true;
        }
        
        descriptor.removeList(this);
    }
    
    public synchronized boolean isEmpty() {
        return unsortedList.isEmpty();
    }
    
    /**
     * Called by a SystemDescriptor when its ranking has changed
     */
    public synchronized void unSort() {
        if (unsortedList.size() > 1) {
            sorted = false;
        }
    }
    
    public synchronized void clear() {
        for (SystemDescriptor<?> descriptor : unsortedList) {
            descriptor.removeList(this);
        }
        
        unsortedList.clear();
    }
    
    public synchronized int size() {
        return unsortedList.size();
    }
}
