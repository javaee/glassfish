package org.glassfish.hk2.classmodel.reflect.test;

import org.glassfish.hk2.classmodel.reflect.*;

import org.glassfish.hk2.classmodel.reflect.test.ordering.MethodDeclarationOrderTest;
import org.junit.Test;
import org.junit.Assert;
import java.io.IOException;


/**
 * Model related tests
 */
public class ModelTest {

    @Test
    public void orderTest() throws IOException, InterruptedException {

        Types types = ClassModelTestsUtils.getTypes();
        Type order = types.getBy(MethodDeclarationOrderTest.class.getName());
        Assert.assertNotNull(order);
        Assert.assertTrue(order.getMethods().size()==4);
        int i=1;
        for (MethodModel mm : order.getMethods()) {
            if (mm.getName().equals("<init>"))
                continue;

            Assert.assertEquals("method"+i, mm.getName());
            i++;
        }
    }
}
