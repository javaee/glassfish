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

package org.glassfish.hk2.tests;

import org.glassfish.hk2.Descriptor;
import org.glassfish.hk2.DynamicBinderFactory;
import org.glassfish.hk2.Services;
import org.glassfish.hk2.inject.Creator;
import org.glassfish.hk2.tests.contracts.SomeContract;
import org.glassfish.hk2.tests.services.SomeService;
import org.junit.Ignore;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 5/3/11
 * Time: 10:13 AM
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class ServicesTest {

    public ServicesTest(Services services) {
        services.locate(services.forContract(String.class).named("foo"));

        services.forContract(String.class).named("foo").get();

//        services.bind(services.newBinder().named("foo").in(ThreadScope.class);


        Descriptor t=null;
        MyType myType = MyType.class.cast(services.locate(t).best().get());

        DynamicBinderFactory binder = services.bindDynamically();
        binder.bind(String.class).named("foo").to("Foo");
        binder.bind(SomeContract.class).toInstance(new SomeService());
        binder.bind(SomeContract.class).to(SomeService.class).in(new ThreadScope());

        binder.commit();

        services.forContract(Creator.class).get().newValueObject(String.class);

    }
}
