/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.locator.named;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.inject.Named;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.UnsatisfiedDependencyException;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class Named2Test {
    /**
     * Tests that a named qualifier on an injection point
     * must be satisfied
     */
    @Test
    public void testNamedQualifierMustBeSatisfied() {
        ServiceLocator locator = LocatorHelper.getServiceLocator(RosalindService.class,
                Rosalind.class,
                Romeo.class,
                RosalindBasisService.class);
        
        // This makes sure that Rosalind is a proper service and that someone can get it
        Assert.assertNotNull(locator.getService(RosalindBasisService.class));
        
        try {
            locator.getService(RosalindService.class);
            Assert.fail("Should not have worked, Rosalind service needs a name, but there is no service with that name");
        }
        catch (MultiException me) {
            // Expected
            Injectee foundUnsatisfied = null;
            for (Throwable th : me.getErrors()) {
                if (th instanceof UnsatisfiedDependencyException) {
                    foundUnsatisfied = ((UnsatisfiedDependencyException) th).getInjectee();
                }
            }
            
            Assert.assertNotNull(foundUnsatisfied);
            
            Assert.assertEquals(CitizenOfVerona.class, foundUnsatisfied.getRequiredType());
            Set<Annotation> annotations = foundUnsatisfied.getRequiredQualifiers();
            Assert.assertEquals(1, annotations.size());
            
            for (Annotation annotation : annotations) {
                Assert.assertEquals(Named.class, annotation.annotationType());
                
                Named named = (Named) annotation;
                Assert.assertEquals(NamedTest.ROSALIND, named.value());
            }
        }
    }

}
