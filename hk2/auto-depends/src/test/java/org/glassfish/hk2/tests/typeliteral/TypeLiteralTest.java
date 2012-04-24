package org.glassfish.hk2.tests.typeliteral;

import org.glassfish.hk2.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;

/**
 * JUnit test
 *
 * @author Jerome Dochez
 */
public class TypeLiteralTest {

    Habitat services;

    @Before
    public void setUp() {
        services = (Habitat) HK2.get().create(null, TypeLiteralModule.class);
    }

    @Test
    public void injectionTest() {
        PathPatternInjectionTarget target = services.byType(PathPatternInjectionTarget.class).get();
        Assert.assertNotNull(target);
        System.out.println(target.routeBuilder.routeBuilderTest());
        System.out.println(target.mapBuilder.routeBuilderTest());
        System.out.println(target.namedBuilder.routeBuilderTest());
    }

    @Test
    public void apiTest() {
        RouteBuilder<PathPattern> pathPattern = services.forContract(new TypeLiteral<RouteBuilder<PathPattern>>() {}).get();
        System.out.println(pathPattern.routeBuilderTest());
    }
}
