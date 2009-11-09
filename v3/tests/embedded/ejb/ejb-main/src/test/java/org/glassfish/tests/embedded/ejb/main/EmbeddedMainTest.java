package org.glassfish.tests.embedded.ejb.main;

import org.glassfish.tests.embedded.ejb.test.*;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Nov 2, 2009
 * Time: 10:20:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmbeddedMainTest extends EmbeddedTest {

    public static void main(String[] args) {
        EmbeddedMainTest test = new EmbeddedMainTest();
        System.setProperty("basedir", System.getProperty("user.dir"));
        test.test();
    }

    @Override
    public File getDeployableArtifact() {
        File f = new File(System.getProperty("basedir"));
        f = f.getParentFile();
        f = new File(f, "ejb-api");
        f = new File(f, "target");
        f = new File(f, "classes");
        System.out.println("Using file " + f.getAbsolutePath());
        return f;
    }
}
