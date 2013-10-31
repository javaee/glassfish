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
package org.glassfish.hk2.classmodel.reflect.test.method;

import org.glassfish.hk2.classmodel.reflect.*;
import org.glassfish.hk2.classmodel.reflect.test.ClassModelTestsUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * method related tests
 */
public class MethodTest {

    @Test
    public void simpleTest() throws IOException, InterruptedException{
        Types types = ClassModelTestsUtils.getTypes();
        Type type = types.getBy(SomeAnnotation.class.getName());
        Assert.assertTrue(type instanceof AnnotationType);
        AnnotationType annotation = (AnnotationType) type;
        Collection<AnnotatedElement> aes = annotation.allAnnotatedTypes();
        // we must find our SimpleAnnotatedMethod.setFoo method
        Assert.assertNotNull(aes);
        Assert.assertTrue(aes.size()>0);
        for (AnnotatedElement ae : aes) {
            if (ae instanceof MethodModel) {
                MethodModel mm = (MethodModel) ae;
                if ("setFoo".equals(mm.getName())) {
                    if (mm.getDeclaringType().getName().equals(SimpleAnnotatedMethod.class.getName())) {
                        // success
                      
                        Assert.assertEquals(mm.getAnnotations().toString(), 1, mm.getAnnotations().size());
                        AnnotationModel ann = (AnnotationModel) mm.getAnnotations().iterator().next();
                        Assert.assertEquals("values", 3, ann.getValues().size());
                        Assert.assertEquals("aLong value", 10, ann.getValues().get("aLong"));
                        Assert.assertEquals("aClass value", "java.lang.Void", ann.getValues().get("aClass"));
//                        Assert.assertEquals("aClassArr value", "java.lang.Void", ann.getValues().get("aClassArr"));
                        Assert.assertEquals("default values", 3, ann.getType().getDefaultValues().size());
                        Assert.assertEquals("java.lang.Void", ann.getType().getDefaultValues().get("environment"));
                        return;
                    }
                }
            }
        }
        Assert.fail("Did not find a SimpleAnnotatedMethod.setFoo annotated method with SomeAnnotation");
    }
}
