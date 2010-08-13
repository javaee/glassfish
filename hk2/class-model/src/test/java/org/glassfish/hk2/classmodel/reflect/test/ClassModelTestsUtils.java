package org.glassfish.hk2.classmodel.reflect.test;

import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Aug 12, 2010
 * Time: 7:01:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassModelTestsUtils {

    static Types types = null;

    private final static ClassModelTestsUtils instance = new ClassModelTestsUtils();


    public static Types getTypes() throws IOException, InterruptedException {

        synchronized(instance) {

            if (types == null) {
                File userDir = new File(System.getProperty("user.dir"));
                File modelDir = new File(userDir, "target" + File.separator + "test-classes");

                if (modelDir.exists()) {
                    ParsingContext pc = (new ParsingContext.Builder()).build();
                    Parser parser = new Parser(pc);

                    parser.parse(modelDir, null);
                    Exception[] exceptions = parser.awaitTermination(100, TimeUnit.SECONDS);
                    if (exceptions!=null) {
                        for (Exception e : exceptions) {
                            System.out.println("Found Exception ! : " +e);
                        }
                        Assert.assertTrue("Exceptions returned", exceptions.length==0);
                    }
                    types = pc.getTypes();
                }
            }
        }
        return types;
    }

}
