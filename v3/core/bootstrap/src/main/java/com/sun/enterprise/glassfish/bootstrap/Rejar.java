package com.sun.enterprise.glassfish.bootstrap;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Nov 10, 2008
 * Time: 8:53:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class Rejar {

    public Rejar() {
    }
    
    public void rejar(File out, File modules) throws IOException {

        Map<String, ByteArrayOutputStream> metadata = new HashMap<String, ByteArrayOutputStream>();
        FileOutputStream fos = new FileOutputStream(out);
        Set<String> names = new HashSet<String>();
        names.add(Attributes.Name.MAIN_CLASS.toString());
        JarOutputStream jos = new JarOutputStream(fos, getManifest());
        processDirectory(jos, modules, names, metadata);
        for (File directory : modules.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        })) {
            processDirectory(jos, directory, names, metadata);
        }

        // copy the inhabitants files.
        for (Map.Entry<String, ByteArrayOutputStream> e : metadata.entrySet()) {
            copy(e.getValue().toByteArray(), e.getKey(), jos);
        }
        jos.flush();
        jos.close();            
    }

    protected Manifest getManifest() throws IOException {
        Manifest m = new Manifest();
        m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); 
        m.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "com.sun.enterprise.glassfish.bootstrap.ASMain");
        return m;
    }

    protected void processDirectory(JarOutputStream jos, File directory, Set<String> names, Map<String, ByteArrayOutputStream> metadata ) throws IOException {

            for (File module : directory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith("jar")) {
                        return true;
                    }
                    return false;
                }
            })) {
                // add module
                JarFile in = new JarFile(module);
                Enumeration<JarEntry> entries = in.entries();
                while (entries.hasMoreElements()) {
                    JarEntry je = entries.nextElement();
                    if (je.getName().endsWith("MANIFEST.MF") || names.contains(je.getName())) {
                        continue;
                    }
                    if (je.isDirectory())
                        continue;

                    if (je.getName().startsWith("META-INF/inhabitants/")
                            || je.getName().startsWith("META-INF/services/")) {
                        ByteArrayOutputStream stream = metadata.get(je.getName());
                        if (stream==null) {
                            metadata.put(je.getName(), stream = new ByteArrayOutputStream());
                        }
                        stream.write(("# from "+ module.getName() + "\n").getBytes());
                        copy(in, je, stream);
                    } else {
                        names.add(je.getName());
                        copy(in, je, jos);
                    }
                }

            };
    }

    protected  void copy(JarFile in, JarEntry je, JarOutputStream jos) throws IOException {
        try {
            jos.putNextEntry(new JarEntry(je.getName()));
            copy(in, je, (OutputStream) jos);
        } finally {
            jos.flush();
            jos.closeEntry();
        }
    }

    protected void copy(JarFile in, JarEntry je, OutputStream os) throws IOException {
        copy(in, je, Channels.newChannel(os));
    }

    protected void copy(JarFile in, JarEntry je, WritableByteChannel out) throws IOException {
        InputStream is = in.getInputStream(je);
        try {
            ReadableByteChannel inChannel = Channels.newChannel(is);
            ByteBuffer byteBuffer = ByteBuffer.allocate(Long.valueOf(je.getSize()).intValue());
            inChannel.read(byteBuffer);
            byteBuffer.rewind();
            out.write(byteBuffer);
        } finally {
            is.close();
        }
    }

    protected void copy(byte[] bytes, String name, JarOutputStream jos) throws IOException {
        try {
            jos.putNextEntry(new JarEntry(name));
            jos.write(bytes);
        } finally {
            jos.closeEntry();
        }
    }
}
