package com.sun.enterprise.module;

import com.sun.hk2.component.InhabitantsScanner;
import org.jvnet.hk2.component.MultiMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Holds information about /META-INF/services and /META-INF/inhabitants for a {@link Module}.
 *
 * <p>
 * A Service implementation is identified by the service
 * interface it implements, the implementation class of that service interface
 * and the module in which that implementation resides.
 *
 * <p>
 * Note that since a single {@link ModuleDefinition} is allowed to be used
 * in multiple {@link Module}s, this class may not reference anything {@link Module}
 * specific.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ModuleMetadata {

    /**
     * META-INF/inhabitants/* files.
     */
    private final MultiMap<String, InhabitantsDescriptor> inhabitants = new MultiMap<String, InhabitantsDescriptor>();

    public static final class InhabitantsDescriptor {
        public final String systemId;
        private final byte[] data;

        public InhabitantsDescriptor(String systemId, byte[] data) {
            this.systemId = systemId;
            this.data = data;
        }

        public InhabitantsScanner createScanner() throws IOException {
            return new InhabitantsScanner(new ByteArrayInputStream(data),systemId);
        }
    }

    public static final class Entry {
        public final List<String> providerNames = new ArrayList<String>();
        public final List<URL> resources = new ArrayList<URL>();

        /**
         * Loads a single service file.
         */
        private void load(URL source, InputStream is) throws IOException {
            this.resources.add(source);
            try {
                Scanner scanner = new Scanner(is);
                while (scanner.hasNext()) {
                    providerNames.add(scanner.next());
                }
            } finally {
                is.close();
            }
        }

        public boolean hasProvider() {
            return !providerNames.isEmpty();
        }
    }

    /**
     * {@link Entry}s keyed by the service name.
     */
    private final Map<String,Entry> entries = new HashMap<String, Entry>();

    /*package*/
    public Entry getEntry(String serviceName) {
        Entry e = entries.get(serviceName);
        if(e==null) e = NULL_ENTRY;
        return e;
    }

    /*package*/
    public Iterable<Entry> getEntries() {
        return entries.values();
    }

    public List<URL> getDescriptors(String serviceName) {
        return getEntry(serviceName).resources;
    }

    public void load(URL source, String serviceName) throws IOException {
        load(source,serviceName,source.openStream());
    }

    public void load(URL source, String serviceName, InputStream is) throws IOException {
        Entry e = entries.get(serviceName);
        if(e==null) {
            e = new Entry();
            entries.put(serviceName,e);
            e.load(source,is);
        }
    }
    
    public void addHabitat(String name, InhabitantsDescriptor descriptor) {
        inhabitants.add(name,descriptor);
    }

    public List<InhabitantsDescriptor> getHabitats(String name) {
        return inhabitants.get(name);
    }



    /**
     * Empty Entry used to indicate that there's no service.
     * This is mutable, so its working correctly depends on the good will of the callers.
     */
    private static final Entry NULL_ENTRY = new Entry();
}
