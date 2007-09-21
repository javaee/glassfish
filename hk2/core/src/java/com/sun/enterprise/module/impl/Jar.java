package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.ModuleMetadata;
import com.sun.enterprise.module.ModuleMetadata.InhabitantsDescriptor;
import com.sun.hk2.component.InhabitantsFile;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.zip.ZipException;
import java.net.URL;

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
     * Loads all <tt>META-INF/habitats</tt> entries and store them to the list.
     */
    public abstract void loadMetadata(ModuleMetadata result);

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

        private File[] fixNull(File[] f) {
            if(f==null) return new File[0];
            else        return f;
        }

        public void loadMetadata(ModuleMetadata result) {
            for( File svc : fixNull(new File(dir, InhabitantsFile.PATH).listFiles())) {
                if(svc.isDirectory())
                    continue;

                try {
                    result.addHabitat(svc.getName(),
                        new InhabitantsDescriptor(svc.getPath(), readFully(svc))
                    );
                } catch(IOException e) {
                    Utils.getDefaultLogger().log(Level.SEVERE, "Error reading habitats file from " + svc, e);
                }
            }

            for( File svc : fixNull(new File(dir, SERVICE_LOCATION).listFiles()) ) {
                if(svc.isDirectory())
                    continue;

                try {
                    result.load( svc.toURL(), svc.getName() );
                } catch(IOException e) {
                    Utils.getDefaultLogger().log(Level.SEVERE, "Error reading service provider from " + svc, e);
                }
            }
        }

        public String getBaseName() {
            return dir.getName();
        }

        private byte[] readFully(File f) throws IOException {
            byte[] buf = new byte[(int)f.length()];
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            try {
                in.readFully(buf);
                return buf;
            } finally {
                in.close();
            }
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

        public void loadMetadata(ModuleMetadata result) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(InhabitantsFile.PATH)) {
                    String habitatName = entry.getName().substring(InhabitantsFile.PATH.length()+1);

                    try {
                        result.addHabitat(habitatName,new InhabitantsDescriptor(
                            "jar:"+file.toURL()+"!/"+entry.getName(),
                            loadFully(entry)
                        ));
                    } catch(IOException e) {
                        Utils.getDefaultLogger().log(Level.SEVERE, "Error reading inhabitants list in " + jar.getName(), e);
                    }
                } else
                if (entry.getName().startsWith(SERVICE_LOCATION)) {
                    String serviceName = entry.getName().substring(SERVICE_LOCATION.length()+1);

                    try {
                        result.load( new URL("jar:"+file.toURL()+"!/"+entry.getName()), serviceName, jar.getInputStream(entry));
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

        private byte[] loadFully(JarEntry e) throws IOException {
            byte[] buf = new byte[(int)e.getSize()];
            DataInputStream in = new DataInputStream(jar.getInputStream(e));
            try {
                in.readFully(buf);
                return buf;
            } finally {
                in.close();
            }
        }
    }

    final static private String SERVICE_LOCATION = "META-INF/services";
}
