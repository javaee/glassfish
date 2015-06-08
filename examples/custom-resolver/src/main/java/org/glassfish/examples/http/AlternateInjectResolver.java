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

package org.glassfish.examples.http;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;

/**
 * @author jwells
 *
 */
@Singleton
public class AlternateInjectResolver implements InjectionResolver<AlternateInject> {
    @Inject @Named(InjectionResolver.SYSTEM_RESOLVER_NAME)
    private InjectionResolver<Inject> systemResolver;
    
    @Inject
    private HttpRequest request;

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.InjectionResolver#resolve(org.glassfish.hk2.api.Injectee, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        AnnotatedElement parent = injectee.getParent();
        if (!(parent instanceof Method)) {
            throw new AssertionError("The AlternateInjectionResolver only works on methods (for now)");
        }
        
        Method method = (Method) parent;
        
        Annotation annotations[] = method.getParameterAnnotations()[injectee.getPosition()];
        HttpParameter httpParam = getHttpParameter(annotations);
        if (httpParam == null) {
            return systemResolver.resolve(injectee, root);
        }
        
        int index = httpParam.value();
        String fromRequest = request.getPathElement(index);
        if (fromRequest == null) {
            throw new AssertionError("There should have been a value at index " + index);
        }
        
        Class<?> injecteeType = method.getParameterTypes()[injectee.getPosition()];
        if (int.class.equals(injecteeType)) {
            return Integer.parseInt(fromRequest);
        }
        if (long.class.equals(injecteeType)) {
            return Long.parseLong(fromRequest);
        }
        if (String.class.equals(injecteeType)) {
            return fromRequest;
        }
        
        throw new AssertionError("Unknown type conversion: " + injecteeType);
    }
    
    private static HttpParameter getHttpParameter(Annotation annotations[]) {
        for (Annotation anno : annotations) {
            if (HttpParameter.class.equals(anno.annotationType())) {
                return (HttpParameter) anno;
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.InjectionResolver#isConstructorParameterIndicator()
     */
    @Override
    public boolean isConstructorParameterIndicator() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.InjectionResolver#isMethodParameterIndicator()
     */
    @Override
    public boolean isMethodParameterIndicator() {
        return false;
    }

}
