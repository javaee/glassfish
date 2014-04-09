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
package org.glassfish.hk2.tests.literal;

import java.lang.reflect.Field;

import junit.framework.Assert;

import org.glassfish.hk2.api.AnnotationLiteral;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class AnnotationLiteralTest {
    public final static String VALUE1 = "value1";
    public final static String VALUE2 = "value2";
    
    /**
     * Tests that the equals and hashCode works between AnnotationLiterals
     */
    @Test
    public void testEqualsAndHashCodeOfLiteral() {
        AnnoWithString aws1_1 = new AnnoWithStringLiteral(VALUE1);
        AnnoWithString aws1_2 = new AnnoWithStringLiteral(VALUE1);
        AnnoWithString aws2_1 = new AnnoWithStringLiteral(VALUE2);
        
        Assert.assertEquals(aws1_1.hashCode(), aws1_2.hashCode());
        Assert.assertTrue(aws1_1.equals(aws1_2));
        Assert.assertTrue(aws1_2.equals(aws1_1));
        
        Assert.assertFalse(aws1_1.equals(aws2_1));
        Assert.assertFalse(aws1_1.hashCode() == aws2_1.hashCode());
        
    }
    
    /**
     * Tests that the Jdk version of equals works the same as the literal version
     */
    @Test
    public void testEqualsAndHashCodeWithJdk() {
        AnnoWithString aws1 = new AnnoWithStringLiteral(VALUE1);
        AnnoWithString aws2 = new AnnoWithStringLiteral(VALUE2);
        
        AnnoWithString awsJdk = ClassWithAnnoWithString.class.getAnnotation(AnnoWithString.class);
        
        Assert.assertEquals(aws1.hashCode(), awsJdk.hashCode());
        Assert.assertFalse(aws2.hashCode() == awsJdk.hashCode());
        
        Assert.assertTrue(aws1.equals(awsJdk));
        Assert.assertFalse(aws2.equals(awsJdk));
        
        Assert.assertTrue(awsJdk.equals(aws1));
        Assert.assertFalse(awsJdk.equals(aws2));
    }
    
    private Q getQField() {
        Class<?> c = ClassWithQField.class;
        
        Field field;
        try {
            field = c.getField("qField");
        }
        catch (NoSuchFieldException nsfe) {
            return null;
        }
        
        return field.getAnnotation(Q.class);
    }
    
    /**
     * Tests JDK version of equals works with an empty qualifier
     */
    @Test
    public void testEqualsOfEmptyAnnotation() {
        Q qJdk = ClassWithQ.class.getAnnotation(Q.class);
        Q qJdkField = getQField();
        
        Assert.assertNotNull(qJdk);
        Assert.assertNotNull(qJdkField);
        
        Assert.assertEquals(qJdk, qJdkField);
        
        Assert.assertEquals(new QImpl().hashCode(), qJdk.hashCode());
        Assert.assertEquals(qJdkField.hashCode(), qJdk.hashCode());
        
        Assert.assertTrue(new QImpl().equals(qJdk));
        
        Assert.assertTrue(qJdk.equals(new QImpl()));
    }
    
    /**
     * An AnnotationLiteral MUST implement an AnnotationType
     */
    @Test(expected=IllegalStateException.class)
    public void testInvalidAnnotationLiteral() {
        new AnnotationLiteral<Q>() {
            /**
             * 
             */
            private static final long serialVersionUID = 8047528061664493726L;};
    }
    
    public class QImpl extends AnnotationLiteral<Q> implements Q {

        /**
         * 
         */
        private static final long serialVersionUID = 4372411188097605709L;
        
    }

}
