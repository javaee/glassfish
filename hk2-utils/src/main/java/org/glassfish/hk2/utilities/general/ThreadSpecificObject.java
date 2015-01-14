/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities.general;

/**
 * This ties the given object with the thread upon
 * which this object is created
 * 
 * This class can be used as the key in a hashSet if the
 * incoming object can be used as the key in a hashSet
 * @author jwells
 *
 */
public class ThreadSpecificObject<T> {
    private final T incoming;
    private final long tid;
    private final int hash;
    
    public ThreadSpecificObject(T incoming) {
        this.incoming = incoming;
        this.tid = Thread.currentThread().getId();
        
        int hash = (incoming == null) ? 0 : incoming.hashCode();
        hash ^= Long.valueOf(tid).hashCode();
        
        this.hash = hash;
    }
    
    /**
     * Gets the thread on which this object was created
     * @return The thread on which this object was created
     */
    public long getThreadIdentifier() {
        return tid;
    }
    
    /**
     * Gets the incoming object bound to the thread id
     * @return The incoming object bound to the thread id
     */
    public T getIncomingObject() {
        return incoming;
    }
    
    @Override
    public int hashCode() {
        return hash;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof ThreadSpecificObject)) return false;
        ThreadSpecificObject other = (ThreadSpecificObject) o;
        
        if (tid != other.tid) return false;
        return GeneralUtilities.safeEquals(incoming, other.incoming);
    }
}
