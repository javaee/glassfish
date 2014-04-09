/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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


package org.jvnet.hk2.osgiadapter;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.RepositoryChangeListener;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.Version;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiObrBasedRepository implements Repository {

    private org.apache.felix.bundlerepository.Repository obr;

    public OSGiObrBasedRepository(org.apache.felix.bundlerepository.Repository obr) {
        this.obr = obr;
    }

    @Override
    public String getName() {
        return obr.getName();
    }

    @Override
    public URI getLocation() {
        return URI.create(obr.getURI());
    }

    @Override
    public ModuleDefinition find(String name, String version) {
        List<ModuleDefinition> mds = findAll(name, version);
        return mds.isEmpty() ? null : mds.get(0);
    }

    @Override
    public List<ModuleDefinition> findAll() {
        return findAll(null, null);
    }

    private ModuleDefinition makeModuleDef(File jar) throws IOException {
        return new OSGiModuleDefinition(jar);
    }

    @Override
    public List<ModuleDefinition> findAll(String name) {
        return findAll(name, null);
    }

    private List<ModuleDefinition> findAll(String name, String version) {
        List<ModuleDefinition> mds = new ArrayList<ModuleDefinition>();
        for (Resource resource : obr.getResources()) {
            if (name != null) {
                final String rsn = resource.getSymbolicName();
                final Version rv = resource.getVersion();
                boolean versionMatching = (version == null) || version.equals(rv.toString());
                boolean nameMatching = name.equals(rsn);
                if (nameMatching && versionMatching) {
                    try {
                        final URI uri = URI.create(resource.getURI());
                        mds.add(makeModuleDef(new File(uri)));
                    } catch (IOException e) {
                        throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                    }
                }
            }
        }
        return mds;
    }

    @Override
    public void initialize() throws IOException {
        // obr.xml is already available
    }

    @Override
    public void shutdown() throws IOException {
        // no-op, since we don't do anything in initialize()
    }

    @Override
    public List<URI> getJarLocations() {
        List<URI> uris = new ArrayList<URI>();
        for (Resource resource : obr.getResources()) {
            final URI e = URI.create(resource.getURI());
            uris.add(e);
        }
        return uris;
    }

    @Override
    public boolean addListener(RepositoryChangeListener listener) {
        return false;  // not supported
    }

    @Override
    public boolean removeListener(RepositoryChangeListener listener) {
        return false;  // not supported
    }

}
