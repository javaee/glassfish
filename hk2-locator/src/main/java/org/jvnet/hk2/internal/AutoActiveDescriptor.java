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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;

/**
 * @author jwells
 * @param <T> The type from the cache
 *
 */
public class AutoActiveDescriptor<T> extends AbstractActiveDescriptor<T> {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -7921574114250721537L;
    private Class<?> implClass;
    private Creator<T> creator;
    private SystemDescriptor<?> hk2Parent;
    private Type implType;
    
    /**
     * For serialization
     */
    public AutoActiveDescriptor() {
        super();
    }
    
    /**
     * Constructor with all relevant fields
     * 
     * @param clazz The class of the implementation
     * @param creator The creator to use (factory or clazz)
     * @param advertisedContracts The set of advertised contracts
     * @param scope The scope of the service
     * @param name The name of the service (may be null)
     * @param qualifiers The set of qualifier annotations
     * @param descriptorVisibility The visibility of this descriptor
     * @param ranking The initial rank
     * @param proxy Whether or not this can be proxied (null for default)
     * @param proxyForSameScope Whether or not to proxy within the same scope (null for default)
     * @param classAnalysisName The name of the class analyzer (null for default)
     * @param metadata The set of metadata associated with this descriptor
     * @param descriptorType The type of the descriptor
     */
    public AutoActiveDescriptor(
            Class<?> clazz,
            Creator<T> creator,
            Set<Type> advertisedContracts,
            Class<? extends Annotation> scope, String name,
            Set<Annotation> qualifiers,
            DescriptorVisibility descriptorVisibility,
            int ranking,
            Boolean proxy,
            Boolean proxyForSameScope,
            String classAnalysisName,
            Map<String, List<String>> metadata,
            DescriptorType descriptorType,
            Type clazzType) {
        super(advertisedContracts,
                scope,
                name,
                qualifiers,
                DescriptorType.CLASS,
                descriptorVisibility,
                ranking,
                proxy,
                proxyForSameScope,
                classAnalysisName,
                metadata);
        
        implClass = clazz;
        this.creator = creator;
        
        setImplementation(implClass.getName());
        setDescriptorType(descriptorType);
        
        if (clazzType == null) {
            implType = clazz;
        }
        else {
            implType = clazzType;
        }
    }
    
    /* package */ void resetSelfDescriptor(ActiveDescriptor<?> toMe) {
        if (!(creator instanceof ClazzCreator)) return;
        ClazzCreator<?> cc = (ClazzCreator<?>) creator;
        
        cc.resetSelfDescriptor(toMe);
    }
    
    /* package */ void setHK2Parent(SystemDescriptor<?> hk2Parent) {
        this.hk2Parent = hk2Parent;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationClass()
     */
    @Override
    public Class<?> getImplementationClass() {
        return implClass;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationType()
     */
    @Override
    public Type getImplementationType() {
        return implType;
    }
    
    @Override
    public void setImplementationType(Type t) {
        this.implType = t;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#create(org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public T create(ServiceHandle<?> root) {
        return creator.create(root, hk2Parent);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#dispose(java.lang.Object, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public void dispose(T instance) {
        creator.dispose(instance);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getInjectees()
     */
    @Override
    public List<Injectee> getInjectees() {
        return creator.getInjectees();
    }

    
}
