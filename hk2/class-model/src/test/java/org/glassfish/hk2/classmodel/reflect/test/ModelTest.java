package org.glassfish.hk2.classmodel.reflect.test;

import org.glassfish.hk2.classmodel.reflect.*;
import org.glassfish.hk2.classmodel.reflect.impl.TypesImpl;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 12, 2010
 * Time: 3:20:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelTest {

    final static String packageName = "org.glassfish.hk2.classmodel.reflect.test.model";

    @Test
    public void modelTest() throws IOException, InterruptedException {
        File userDir = new File(System.getProperty("user.dir"));
        File modelDir = new File(userDir, "target" + File.separator + "test-classes");

        if (modelDir.exists()) {
            ParsingContext pc = (new ParsingContext.Builder()).build();            
            Parser parser = new Parser(pc);

            parser.parse(modelDir, new Runnable() {
                @Override
                public void run() {
                    System.out.println("done parsing !");
                }
            });
            Exception[] exceptions = parser.awaitTermination(100, TimeUnit.SECONDS);
            if (exceptions!=null) {
                for (Exception e : exceptions) {
                    System.out.println("Found Exception ! : " +e);
                }
                Assert.assertTrue("Exceptions returned", exceptions.length==0);
            }
            Types types = pc.getTypes();
            for (Type t : types.getAllTypes()) {
                System.out.println(t.toString());
            }
        }
    }
}
