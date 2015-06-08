/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.testing.hk2testng;

import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.testing.hk2testng.service.PrimaryService;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/**
 *
 * @author saden
 */
@HK2(PrimaryInjectionTest.SAME_LOCATOR_NAME)
public class PrimaryInjectionTest {
    public final static String SAME_LOCATOR_NAME = "Same";

    @Inject
    PrimaryService primaryService;
    
    @Inject
    ServiceLocator serviceLocator;

    @Test
    public void assertPrimaryServiceInjecton() {
        assertThat(primaryService).isNotNull();
    }

    @Test
    public void assertSecondaryService() {
        assertThat(primaryService.getSecondaryService()).isNotNull();
    }
    
    /**
     * The exact same test is found in ConfigurationMethodInjectionTest.  The
     * intent of the test is to ensure that there are not multiple
     * descriptors for PrimaryService due to the fact that multiple
     * test classes are using the same service locator.  If the
     * service locator was being populated by both test classes then
     * one of these two identical tests would see multiple instances
     * of the primaryService in the IterableProvider
     */
    @Test
    public void assertNoMultiplePrimaries() {
        List<ActiveDescriptor<?>> allPrimaryServices = serviceLocator.getDescriptors(
                BuilderHelper.createContractFilter(PrimaryService.class.getName()));
        
        assertThat(allPrimaryServices.size()).isEqualTo(1);
    }
}
