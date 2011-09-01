/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
package org.glassfish.hk2.tests.basic;

import org.glassfish.hk2.ComponentException;
import org.glassfish.hk2.Factory;
import org.glassfish.hk2.Provider;
import org.glassfish.hk2.tests.basic.arbitrary.QualifierInjected;
import org.glassfish.hk2.tests.basic.services.ServiceB1;

import static org.junit.Assert.*;

/**
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class AssertionUtils {

    private AssertionUtils() {
    }

    public static void assertQualifierInjectedContent(QualifierInjected instance) throws ComponentException {
        assertNotNull("Instance not provisioned", instance);
        assertNotNull("Qualified injection point null", instance.getQb());
        // the .getSimpleName() bellow is meant to avoid the long, package-poluted JUnit assertion reports
        assertEquals("Qualified injection point of unexepceted type", ServiceB1.class.getSimpleName(), instance.getQb().getClass().getSimpleName());
        assertNotNull("Qualified injection provider not provisioned", instance.getQbf());
        assertNotNull("Qualified injection provider returned null", instance.getQbf().get());
        assertEquals("Qualified injection provider returned instance of unexpected type", ServiceB1.class.getSimpleName(), instance.getQbf().get().getClass().getSimpleName());
    }

    public static <T> void assertInjectedInstance(Class<? extends T> expectedType, T instance) {
        assertNotNull("Provisioned instance is null.", instance);
        assertEquals("Provisioned instance unexpected type.", expectedType.getSimpleName(), instance.getClass().getSimpleName());
    }

    public static <T> void assertInjectedProvider(Class<? extends T> expectedType, Provider<T> provider) {
        assertNotNull("Provisioned instance provider is null.", provider);
        assertNotNull("Provider returned null instance.", provider.get());
        assertEquals("Provider returned instance of unexpected type.", expectedType.getSimpleName(), provider.get().getClass().getSimpleName());
    }

    public static <T> void assertInjectedFactory(Class<? extends T> expectedType, Factory<T> provider) {
        assertNotNull("Injected instance factory is null.", provider);
        assertNotNull("Factory returned null instance.", provider.get());
        assertEquals("Factory returned instance of unexpected type.", expectedType.getSimpleName(), provider.get().getClass().getSimpleName());
    }
    
}
