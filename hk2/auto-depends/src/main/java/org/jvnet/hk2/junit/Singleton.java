package org.jvnet.hk2.junit;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Apr 27, 2010
 * Time: 9:13:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class Singleton {

    public Singleton() {
        System.out.println("Singleton created");
        String classPath = System.getProperty("surefire.test.class.path");
        if (classPath==null) {
            classPath = System.getProperty("java.class.path");
        }
        StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        while(st.hasMoreElements()) {
            System.out.println(st.nextToken());
        }

        
    }
}
