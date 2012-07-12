/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.deprecated.internal;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.Context;
import org.glassfish.hk2.Provider;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Creator;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

import com.sun.hk2.component.AbstractInhabitantImpl;

/**
 * @author jwells
 *
 */
public class CreatorImpl<T> extends AbstractInhabitantImpl<T> implements Creator<T> {
    private final Class<?> c;
    private final ServiceLocator locator;
    private final Map<String, List<String>> metadata;
    
    /**
     * 
     * @param c
     * @param locator
     * @param metadata
     * @param d
     */
    public CreatorImpl(Class<?> c, ServiceLocator locator, Map<String, List<String>> metadata, Descriptor d) {
        super(d);
        this.c = c;
        this.locator = locator;
        this.metadata = metadata;
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.Inhabitant#typeName()
     */
    @Override
    public String typeName() {
        return getImplementation();
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.Inhabitant#type()
     */
    @Override
    public Class<? extends T> type() {
        return (Class<? extends T>) c;
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.Inhabitant#get(org.jvnet.hk2.component.Inhabitant)
     */
    @SuppressWarnings("unchecked")
    @Override
    public T get(Inhabitant onBehalfOf) {
        Object retVal = locator.getService(c);
        return (T) retVal;
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.Inhabitant#metadata()
     */
    @Override
    public Map<String, List<String>> metadata() {
        return metadata;
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.Inhabitant#release()
     */
    @Override
    public void release() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.Provider#isActive()
     */
    @Override
    public boolean isActive() {
        ServiceHandle<?> handle = locator.getServiceHandle(c);
        if (handle == null) return false;
            
        return handle.isActive();
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.Creator#create(org.jvnet.hk2.component.Inhabitant)
     */
    @SuppressWarnings("unchecked")
    @Override
    public T create(Inhabitant onBehalfOf) throws ComponentException {
        return (T) locator.createAndInitialize(c);
    }

    /* (non-Javadoc)
     * @see org.jvnet.hk2.component.Creator#initialize(java.lang.Object, org.jvnet.hk2.component.Inhabitant)
     */
    @Override
    public void initialize(T t, Inhabitant onBehalfOf)
            throws ComponentException {
        // Do nothing
        
    }

    
}
