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

import org.glassfish.hk2.*;
import org.glassfish.hk2.tests.contracts.Latin;
import org.glassfish.hk2.tests.contracts.PathPattern;
import org.glassfish.hk2.tests.contracts.RouteBuilder;
import org.glassfish.hk2.tests.contracts.SomeContract;
import org.glassfish.hk2.tests.services.*;
import org.junit.Ignore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 5/12/11
 * Time: 5:38 AM
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class TestModule implements Module {

    @Override
    public void configure(BinderFactory binderFactory) {

        binderFactory.bind(SomeContract.class).to(SomeService.class);
        binderFactory.bind(SomeContract.class).named("foo").to(SomeService.class);
        binderFactory.bind(SomeContract.class).named("european").annotatedWith(Latin.class).to(LatinService.class);


        binderFactory.bind(new TypeLiteral<RouteBuilder<PathPattern>>() {}).to(PathPatternRouteBuilder.class);

        binderFactory.bind(SomeContract.class).toProvider(new Factory<SomeContract>() {
            @Override
            public SomeContract get() throws ComponentException {
                return new SomeService();
            }
        });


        binderFactory.bind("org.glassfish.hk2.tests.contracts.SomeContract").to(SomeService.class);
        binderFactory.bind(new TypeLiteral<RouteBuilder<PathPattern>>() {}).toProvider(PPRBFactory.class);
        binderFactory.bind(new TypeLiteral<Map<String, SomeContract>>() {}).to(
                new TypeLiteral<HashMap<String, SomeContract>>() {});


        binderFactory.bind().toProvider(PPRBFactory.class); // get type info from class
        binderFactory.bind().toProvider(new TypeLiteral<PPRBFactory>() {}); // with type info
        binderFactory.bind(SomeContract.class).toProvider(new MySomeServiceFactory());



    }
}
