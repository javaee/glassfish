/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import org.glassfish.hk2.api.Descriptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

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
public final class ModuleMetadata implements Serializable {

    /**
     * META-INF/hk2-locator/* cache
     */
    private Map<String, List<Descriptor>> descriptors = new HashMap<String, List<Descriptor>>();

    public Map<String, List<Descriptor>> getDescriptors() {
        return descriptors;
    }

    public synchronized void addDescriptors(String serviceLocatorName, Collection<Descriptor> descriptorsToAdd) {
        List<Descriptor> descriptorList = descriptors.get(serviceLocatorName);

        if (descriptorList == null) {
            descriptorList = new ArrayList<Descriptor>();

            descriptors.put(serviceLocatorName, descriptorList);
        }

        descriptorList.addAll(descriptorsToAdd);
    }

    public static final class Entry implements Serializable {
        public final List<String> providerNames = new ArrayList<String>();
        public final List<URL> resources = new ArrayList<URL>();

        /**
         * Loads a single service file.
         */
        private void load(URL source, InputStream is) throws IOException {
            this.resources.add(source);
            try {
                /*
                 * The format of service file is specified at
                 * http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider
                 * According to the above spec,
                 * The file contains a list of fully-qualified binary names of
                 * concrete provider classes, one per line.
                 * Space and tab characters surrounding each name,
                 * as well as blank lines, are ignored.
                 * The comment character is '#' ('\u0023', NUMBER SIGN);
                 * on each line all characters following the first comment
                 * character are ignored. The file must be encoded in UTF-8.
                 */
                Scanner scanner = new Scanner(is);
                final String commentPattern = "#"; // NOI18N
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (!line.startsWith(commentPattern)) {
                        StringTokenizer st = new StringTokenizer(line);
                        while (st.hasMoreTokens()) {
                            providerNames.add(st.nextToken());
                            break; // Only one entry per line
                        }
                    }
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
        } else {
          is.close();
        }
    }

    /**
     * Empty Entry used to indicate that there's no service.
     * This is mutable, so its working correctly depends on the good will of the callers.
     */
    private static final Entry NULL_ENTRY = new Entry();
}
