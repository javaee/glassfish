package com.sun.enterprise.module.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Finds out where a class file is loaded from.
 *
 * @author dochez
 */
public class Which {
    public static File jarFile(Class clazz) throws IOException {
        String resourceName = clazz.getName().replace(".","/")+".class";
        URL resource = clazz.getClassLoader().getResource(resourceName);
        if (resource==null) {
            throw new IllegalArgumentException("Cannot get bootstrap path from "+ clazz + " class location, aborting");
        }

        if (resource.getProtocol().equals("jar")) {
            try {
                JarURLConnection c = (JarURLConnection) resource.openConnection();
                URL jarFile = c.getJarFileURL();
                try {
                    return new File(jarFile.toURI());
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Incorrect bootstrap class URI: "+jarFile, e);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot open jar file "+resource, e);
            }
        } else
            throw new IllegalArgumentException("Don't support packaging "+resource+" , please contribute !");
    }
}
