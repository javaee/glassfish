/*
 * Copyright (c) 2008, Your Corporation. All Rights Reserved.
 */

package org.jvnet.hk2;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Kohsuke Kawaguchi
 */
public class Main {
    public static void main(String[] args) throws IOException {
        for (String arg : args) {
            if(args.length>1) {
                System.out.println("=== "+arg);
            }
            dump(new File(arg));
        }
    }

    public static void dump(File f) throws IOException {
        JarFile jar = new JarFile(f);
        Manifest m = jar.getManifest();
        dump(m);
        jar.close();
    }

    public static void dump(Manifest m) throws IOException {
        OSGiManifest manifest = new OSGiManifest(m);
        new PlainTextPrinter(System.out).print(manifest);
    }
}
