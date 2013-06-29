/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.guice.bridge.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.jvnet.hk2.guice.bridge.api.GuiceScope;

import com.google.inject.Binding;

/**
 *
 * @param <T> The cache type
 * @author jwells
 */
public class GuiceServiceHk2Bean<T> extends AbstractActiveDescriptor<T> {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 4339256124914729858L;
    
    private Class<?> implClass = null;
    private Binding<T> binding = null;
    
    /**
     * For serialization
     */
    public GuiceServiceHk2Bean() {
    }
    
    /* package */ GuiceServiceHk2Bean(
            Set<Type> contracts,
            Set<Annotation> qualifiers,
            Class<?> implClass,
            Binding<T> binding) {
        super(contracts,
                GuiceScope.class,
                ReflectionHelper.getNameFromAllQualifiers(qualifiers, implClass),
                qualifiers,
                DescriptorType.CLASS,
                DescriptorVisibility.NORMAL,
                0,
                new Boolean(false),
                null,
                (String) null,
                new HashMap<String, List<String>>()
               );
        
        this.implClass = implClass;
        super.setImplementation(implClass.getName());
        
        this.binding = binding;
    }

    @Override
    public Class<?> getImplementationClass() {
        return implClass;
    }

    @Override
    public T create(ServiceHandle<?> root) {
        T retVal = binding.getProvider().get();
        return retVal;
    }
    
    @Override
    public String toString() {
        return "GuiceServiceHk2Bean( " + super.toString() + ")";
    }

}
