/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.classmodel.reflect.impl;

import org.glassfish.hk2.classmodel.reflect.*;
import org.glassfish.hk2.classmodel.reflect.util.ParsingConfig;

import java.util.*;

/**
 * Implementation of an annotation model
 */
public class AnnotationTypeImpl extends InterfaceModelImpl implements AnnotationType {

    private final Set<AnnotatedElement> references = Collections.synchronizedSet(new HashSet<AnnotatedElement>());
    private final Map<String, Object> defValues = new HashMap<String, Object>();

    public AnnotationTypeImpl(String name, TypeProxy<Type> sink) {
        super(name, sink, null);
    }

    public Collection<AnnotatedElement> allAnnotatedTypes() {
        return Collections.unmodifiableSet(references);
    }

    public void addDefaultValue(String name, Object value) {
      if (org.glassfish.hk2.external.org.objectweb.asm.Type.class.isInstance(value)) {
        defValues.put(name, org.glassfish.hk2.external.org.objectweb.asm.Type.class.cast(value).getClassName());
      } else {
        defValues.put(name, value);
      }
    }
    
    @Override
    public Map<String, Object> getDefaultValues() {
      return Collections.unmodifiableMap(defValues);
    }
    
    Set<AnnotatedElement> getAnnotatedElements() {
        return references;
    }

    @Override
    protected void print(StringBuffer sb) {
        super.print(sb);
        sb.append(", referenced from [");
        for (AnnotatedElement a : allAnnotatedTypes()) {
            sb.append(" ").append(a.shortDesc());
        }
        sb.append("]");
    }
}
