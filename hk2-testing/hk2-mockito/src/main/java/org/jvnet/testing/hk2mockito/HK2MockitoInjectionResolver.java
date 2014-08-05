/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.testing.hk2mockito;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import javax.inject.Inject;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceHandle;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.testing.hk2mockito.internal.MockitoService;
import org.jvnet.testing.hk2mockito.internal.cache.ParentCache;

/**
 * This class is a custom resolver that creates or finds services and wraps in a
 * spy.
 *
 * @author Sharmarke Aden
 */
@Rank(Integer.MAX_VALUE)
@Service
public class HK2MockitoInjectionResolver implements InjectionResolver<Inject> {

    private final MockitoService mockitoService;
    private final ParentCache parentCache;

    @Inject
    HK2MockitoInjectionResolver(MockitoService mockitoService, ParentCache parentCache) {
        this.mockitoService = mockitoService;
        this.parentCache = parentCache;
    }

    @Override
    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        AnnotatedElement parent = injectee.getParent();
        Member member = (Member) parent;
        Type requiredType = injectee.getRequiredType();
        Type parentType = member.getDeclaringClass();

        SUT sut = parent.getAnnotation(SUT.class);
        SC sc = parent.getAnnotation(SC.class);
        MC mc = parent.getAnnotation(MC.class);

        Object service;

        parentCache.put(requiredType, parentType);

        if (sut != null) {
            service = mockitoService.findOrCreateSUT(sut, injectee, root);
        } else if (sc != null) {
            service = mockitoService.findOrCreateCollaborator(sc.value(), sc.field(), injectee, root);
        } else if (mc != null) {
            service = mockitoService.findOrCreateCollaborator(mc.value(), mc.field(), injectee, root);
        } else {
            service = mockitoService.createOrFindService(injectee, root);
        }

        return service;
    }

    @Override
    public boolean isConstructorParameterIndicator() {
        return false;
    }

    @Override
    public boolean isMethodParameterIndicator() {
        return false;
    }

}
