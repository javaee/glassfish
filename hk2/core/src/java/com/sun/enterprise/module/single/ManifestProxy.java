package com.sun.enterprise.module.single;

import java.lang.reflect.InvocationTargetException;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.*;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a manifest proxying
 *
 * @author Jerome Dochez
 */
public class ManifestProxy extends Manifest {

    public final Map<String, Attributes> attributes = new HashMap<String, Attributes>();
    public final Attributes mainAttributes = new Attributes();
    public final Map<String, String> mappings = new HashMap<String, String>();

    public ManifestProxy(ClassLoader cl, List<SeparatorMappings> mappings) throws IOException {
        try {
            if (mappings != null) {
                for (SeparatorMappings mapping : mappings) {
                    this.mappings.put(mapping.key, mapping.separator);
                }
            }
            Method met  = null;
            Class<?> t = cl.getClass();
            while (t!=null && met==null) {
                try {
                    met = t.getDeclaredMethod("findResources", String.class);
                } catch(NoSuchMethodException e) {
                    // ignore
                }
                t=t.getSuperclass();
            }
            if (met==null) {
                Logger.getLogger(ManifestProxy.class.getName()).log(Level.SEVERE, "Cannot get findResources method handle");
                return;
            }
            Enumeration<URL> urls=null;
            try {
                met.setAccessible(true);
                urls = (Enumeration<URL>) met.invoke(cl, JarFile.MANIFEST_NAME);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(ManifestProxy.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(ManifestProxy.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(ManifestProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (urls==null) {
                return;
            }
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                InputStream is = null;
                try {
                    is = url.openStream();
                    Manifest m = new Manifest(is);
                    for (Map.Entry<String, Attributes> attr : m.getEntries().entrySet()) {
                        if (attributes.containsKey(attr.getKey())) {
                            merge(attributes.get(attr.getKey()), attr.getValue());
                        } else {
                            attributes.put(attr.getKey(), new Attributes(attr.getValue()));
                        }
                    }
                    merge(mainAttributes, m.getMainAttributes());
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }
        } catch (SecurityException ex) {
            Logger.getLogger(ManifestProxy.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void merge(Attributes target, Attributes source) {
        for (Object o : source.keySet()) {
            if (target.containsKey(o)) {
                String sep = mappings.containsKey(o.toString())?mappings.get(o.toString()):",";
                String newValue = target.get(o) + sep
                        + source.get(o);
                target.put(o, newValue);
            } else {
                target.put(o, source.get(o));
            }
        }
    }

    @Override
    public Attributes getMainAttributes() {        
        return mainAttributes;
    }

    @Override
    public Map<String, Attributes> getEntries() {
        return attributes;
    }

    @Override
    public Attributes getAttributes(String name) {
        return attributes.get(name);
    }

    @Override
    public void clear() {
        mainAttributes.clear();
        attributes.clear();
    }

    @Override
    public void write(OutputStream out) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void read(InputStream is) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public static final class SeparatorMappings {
        final String key;
        final String separator;

        public SeparatorMappings(String key, String separator) {
            this.key = key;
            this.separator = separator;
        }
    }
}
