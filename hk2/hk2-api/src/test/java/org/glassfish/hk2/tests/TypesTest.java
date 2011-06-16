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

import org.glassfish.hk2.Services;
import org.glassfish.hk2.TypeLiteral;
import org.junit.Test;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.tiger_types.Types;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 5/11/11
 * Time: 7:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class TypesTest {

    public static class RoutesBuilder<T, U> {
        T p1;
        U p2;
    }

    public static class RouteBuilder<T> {
        T pattern;
    }

    public static class Pattern {

    }

    public static class PathPattern extends Pattern {

    }

    RouteBuilder<PathPattern> pathPatternRouteBuilder;
    List<RouteBuilder<PathPattern>> builders;

    @Test
    public void testTypes() throws Exception {
        Field f = TypesTest.class.getDeclaredField("pathPatternRouteBuilder");
        Type type1 = Types.getTypeArgument(f.getGenericType(),0);
        System.out.println("Type 1 is " + f.getType() + " type 2 is " + Types.erasure(type1));

    }

    @Inject
    Services services=null;

    @Test
    public void test3Levels() throws Exception {

        Field f = TypesTest.class.getDeclaredField("builders");
        Type type1 = Types.getTypeArgument(f.getGenericType(), 0);
        Type type2 = Types.getTypeArgument(type1, 0);
        System.out.println("Type 1 is " + f.getType() + " type 2 is " + Types.erasure(type1) + " type 3 is " + Types.erasure(type2));

        TypeLiteral<List<RouteBuilder<PathPattern>>> pathPatternRouteBuilderType = new TypeLiteral<List<RouteBuilder<PathPattern>>>() {};
        Type type = pathPatternRouteBuilderType.getClass().getGenericSuperclass();
        System.out.println("Raw type is " + pathPatternRouteBuilderType.getRawType());
        explore(type);
        System.out.println();

        TypeLiteral<Map<RoutesBuilder<Pattern, PathPattern>, String>> pathPatternRouteBuilderType2 = new TypeLiteral<Map<RoutesBuilder<Pattern, PathPattern>, String>>() {};
        explore(pathPatternRouteBuilderType2.getClass().getGenericSuperclass());

        if (services!=null) {
            services.bindDynamically().bind(Pattern.class).to(PathPattern.class);
            services.bindDynamically().bind(pathPatternRouteBuilderType).toInstance(new ArrayList<RouteBuilder<PathPattern>>());
            services.bindDynamically().bind().to(pathPatternRouteBuilderType);
        }

    }

    private void explore(Type clazz) {
        exploreType(clazz);
        System.out.println();
    }

    private void exploreType(Type type) {
        if (type instanceof ParameterizedType) {
            System.out.print(TypeLiteral.getRawType(type).getName());
            Collection<Type> types = Arrays.asList(((ParameterizedType) type).getActualTypeArguments());
            Iterator<Type> typesEnum = types.iterator();
            System.out.print("<");
            while(typesEnum.hasNext()) {
                exploreType(typesEnum.next());
                if (typesEnum.hasNext()) System.out.print(",");
            }
            System.out.print(">");
        } else {
            System.out.print(TypeLiteral.getRawType(type).getName());
        }
    }
}
