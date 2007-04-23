package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.ServiceProviderInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.zip.ZipException;

/**
 * Abstraction of {@link JarFile} so that we can handle
 * both a jar file and a directory image transparently.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Jar {
    private Jar() {}

    /**
     * See {@link JarFile#getManifest()} for the contract.
     */
    public abstract Manifest getManifest() throws IOException;

    /**
     * Loads all <tt>META-INF/services</tt> entries and store them to the list.
     */
    public abstract void getServiceProviders(List<ServiceProviderInfo> result);

    /**
     * Gets the base name of the jar.
     *
     * <p>
     * For example, "bar" for "bar.jar".
     */
    public abstract String getBaseName();

    public static Jar create(File file) throws IOException {
        if(file.isDirectory())
            return new Directory(file);
        else
            return new Archive(file);
    }

    /**
     * Loads a single service file.
     */
    protected final void load(InputStream is, String serviceName, List<ServiceProviderInfo> result) throws IOException {
        try {
            Scanner scanner = new Scanner(is);
            while (scanner.hasNext()) {
                ServiceProviderInfo info = new ServiceProviderInfo(serviceName, scanner.next());
                result.add(info);
            }
        } finally {
            is.close();
        }
    }


    private static final class Directory extends Jar {
        private final File dir;

        public Directory(File dir) {
            this.dir = dir;
        }

        public Manifest getManifest() throws IOException {
            File mf = new File(dir,JarFile.MANIFEST_NAME);
            if(mf.exists()) {
                FileInputStream in = new FileInputStream(mf);
                try {
                    return new Manifest(in);
                } finally {
                    in.close();
                }
            } else {
                return null;
            }
        }

        public void getServiceProviders(List<ServiceProviderInfo> result) {
            File[] services = new File(dir,SERVICE_LOCATION).listFiles();
            if(services==null)  return;

            for( File svc : services ) {
                if(svc.isDirectory())
                    continue;

                try {
                    load(new FileInputStream(svc), svc.getName(), result);
                } catch(IOException e) {
                    Utils.getDefaultLogger().log(Level.SEVERE, "Error reading service provider in " + svc, e);
                }
            }
        }

        public String getBaseName() {
            return dir.getName();
        }
    }

    private static final class Archive extends Jar {
        private final JarFile jar;
        private final File file;

        public Archive(File jar) throws IOException {
            try {
                this.jar = new JarFile(jar);
                this.file = jar;
            } catch (ZipException e) {
                // ZipException doesn't include this crucial information, so rewrap
                IOException x = new IOException("Failed to open " + jar);
                x.initCause(e);
                throw x;
            }
        }

        public Manifest getManifest() throws IOException {
            return jar.getManifest();
        }

        public void getServiceProviders(List<ServiceProviderInfo> result) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(SERVICE_LOCATION)) {
                    String serviceName = entry.getName().substring(SERVICE_LOCATION.length()+1);

                    try {
                        load(jar.getInputStream(entry), serviceName, result);
                    } catch(IOException e) {
                        Utils.getDefaultLogger().log(Level.SEVERE, "Error reading service provider in " + jar.getName(), e);
                    }
                }
            }
        }

        public String getBaseName() {
            String name = file.getName();
            int idx = name.lastIndexOf('.');
            if(idx>=0)
                name = name.substring(0,idx);
            return name;
        }
    }

    final static private String SERVICE_LOCATION = "META-INF/services";
}
