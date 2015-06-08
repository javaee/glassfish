/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.hk2.classmodel.reflect.AnnotatedElement;
import org.glassfish.hk2.classmodel.reflect.AnnotationModel;
import org.glassfish.hk2.classmodel.reflect.AnnotationType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Model a annotation instance
 */
public class AnnotationModelImpl implements AnnotationModel {
    
    final AnnotationType type;
    final AnnotatedElement element;
    private final Map<String, Object> values = new HashMap<String, Object>();

    public AnnotationModelImpl(AnnotatedElement element, AnnotationType type) {
        this.type = type;
        this.element = element;
    }
    
    @Override
    public String toString() {
      return "AnnotationModel:" + type + "-" + element;
    }

    @SuppressWarnings("unchecked")
    public void addValue(String name, Object value) {
        if (null == name) {
          // check for arrayed value(s)
          name = "value";
          Object prevVal = values.get(name);
          if (null != prevVal) {
            if (Collection.class.isInstance(prevVal)) {
              ((Collection<Object>)prevVal).add(unwrap(value));
              return;
            } else {
              Collection<Object> coll = new ArrayList<Object>();
              coll.add(prevVal);
              coll.add(unwrap(value));
              value = coll;
            }
          }
        }
        values.put(name, unwrap(value));
    }
    
    private Object unwrap(Object value) {
      if (org.glassfish.hk2.external.org.objectweb.asm.Type.class.isInstance(value)) {
        return org.glassfish.hk2.external.org.objectweb.asm.Type.class.cast(value).getClassName();
      }
      return value;
    }

    @Override
    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public AnnotationType getType() {
        return type;
    }

    @Override
    public AnnotatedElement getElement() {
        return element;
    }

}
