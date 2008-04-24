/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
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

        public InhabitantsDescriptor(URL systemId, byte[] data) {
            this(systemId.toExternalForm(),data);
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
