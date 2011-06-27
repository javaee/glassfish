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
package org.glassfish.hk2.tests.scoping;

import org.glassfish.hk2.HK2;
import org.glassfish.hk2.Services;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.junit.Hk2Runner;

/**
 * Scoping junit test
 * @author Jerome Dochez
 */
@RunWith(Hk2Runner.class)
public class ScopingTest {

    static Services services;

    @BeforeClass
    public static void setup() {
        HK2 hk2 = HK2.get();
        services = hk2.create(null, ScopingModule.class);
    }

    @Test
    public void perLookup() {
        ToBeScopedContract perLookup = services.forContract(ToBeScopedContract.class).named("perlookup").get();
        Assert.assertNotNull(perLookup);
        Assert.assertTrue(perLookup.instanceCount()==1);
        ToBeScopedContract perLookup2 = services.forContract(ToBeScopedContract.class).named("perlookup").get();
        Assert.assertNotNull(perLookup2);
        Assert.assertTrue(perLookup2.instanceCount() == 2);
        Assert.assertNotSame(perLookup, perLookup2);
    }


    @Test
    public void singleton() {
        ToBeScopedContract singleton = services.forContract(ToBeScopedContract.class).named("singleton").get();
        Assert.assertNotNull(singleton);
        Assert.assertTrue(singleton.instanceCount()==1);
        ToBeScopedContract singleton2 = services.forContract(ToBeScopedContract.class).named("singleton").get();
        Assert.assertNotNull(singleton2);
        Assert.assertTrue(singleton2.instanceCount()==1);
        Assert.assertSame(singleton, singleton2);
    }
}
