/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.module.*;
import org.apache.felix.bundlerepository.*;
import org.apache.felix.bundlerepository.Repository;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import static org.jvnet.hk2.osgiadapter.Logger.logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class ObrHandler extends ServiceTracker {

    private boolean deployFragments = false;
    public boolean deployOptionalRequirements = false;

    public ObrHandler(BundleContext bctx) {
        super(bctx, RepositoryAdmin.class.getName(), null);
        deployFragments = Boolean.valueOf(bctx.getProperty(Constants.OBR_DEPLOYS_FRGAMENTS));
        deployOptionalRequirements = Boolean.valueOf(bctx.getProperty(Constants.OBR_DEPLOYS_OPTIONAL_REQUIREMENTS));
        open();
    }

    @Override
    public Object addingService(ServiceReference reference) {
        if (this.getTrackingCount() == 1) {
            logger.logp(Level.INFO, "ObrHandler", "addingService",
                    "We already have a repository admin service, so ignoring {0}", new Object[]{reference});
            return null; // we are not tracking this
        }
        return super.addingService(reference);
    }

    @Override
    public void remove(ServiceReference reference) {
        super.remove(reference);
    }

    public RepositoryAdmin getRepositoryAdmin() {
        assert (getTrackingCount() < 2);
        try {
            return (RepositoryAdmin) waitForService(Constants.OBR_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public synchronized void addRepository(URI obrUri) throws Exception {
        if (isDirectory(obrUri)) {
            setupRepository(new File(obrUri), isSynchronous());
        } else {
            getRepositoryAdmin().addRepository(obrUri.toURL());
        }
    }

    private boolean isDirectory(URI obrUri) {
        try {
            new File(obrUri);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public synchronized void addRepository(OSGiDirectoryBasedRepository hk2Repo) throws Exception {
        File dir = new File(hk2Repo.getLocation());
        setupRepository(dir, isSynchronous());
    }

    private void setupRepository(final File repoDir, boolean synchronous) throws Exception {
        if (synchronous) {
            _setupRepository(repoDir);
        } else {
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        _setupRepository(repoDir);
                    } catch (Exception e) {
                        throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                    }
                }
            });
        }
    }

    private boolean isSynchronous() {
        String property = context.getProperty(Constants.INITIALIZE_OBR_SYNCHRONOUSLY);
        // default is synchronous as we are not sure if we have covered every race condition in asynchronous path
        return property == null || Boolean.TRUE.toString().equalsIgnoreCase(property);
    }

    private synchronized void _setupRepository(File repoDir) throws Exception {
        File repoXml = getRepositoryXmlFile(repoDir);
        final long tid = Thread.currentThread().getId();
        if (repoXml.exists()) {
            long t = System.currentTimeMillis();
            updateRepository(repoXml, repoDir);
            long t2 = System.currentTimeMillis();
            logger.logp(Level.INFO, "ObrHandler", "_setupRepository", "Thread #{0}: updateRepository took {1} ms",
                    new Object[]{tid, t2 - t});
        } else {
            long t = System.currentTimeMillis();
            createRepository(repoXml, repoDir);
            long t2 = System.currentTimeMillis();
            logger.logp(Level.INFO, "ObrHandler", "_setupRepository", "Thread #{0}: createRepository took {1} ms",
                    new Object[]{tid, t2 - t});
        }
        final URL repoUrl = repoXml.toURI().toURL();
        logger.logp(Level.INFO, "ObrHandler", "_setupRepository", "Thread #{0}: Adding repository = {1}",
                new Object[]{tid, repoUrl});
        long t = System.currentTimeMillis();
        getRepositoryAdmin().addRepository(repoUrl);
        long t2 = System.currentTimeMillis();
        logger.logp(Level.INFO, "ObrHandler", "_setupRepository", "Thread #{0}: Adding repo took {1} ms",
                new Object[]{tid, t2 - t});
    }

    private File getRepositoryXmlFile(File repoDir) {
        return new File(context.getDataFile(""), repoDir.getName() + Constants.REPOSITORY_XML_FILE_NAME);
    }

    /**
     * Create a new Repository from a directory by recurssively traversing all the jar files found there.
     *
     * @param repoXml
     * @param repoDir
     * @return
     * @throws IOException
     */
    private void createRepository(File repoXml, File repoDir) throws IOException {
        DataModelHelper dmh = getRepositoryAdmin().getHelper();
        List<Resource> resources = new ArrayList<Resource>();
        for (File jar : findAllJars(repoDir)) {
            Resource r = dmh.createResource(jar.toURI().toURL());
            resources.add(r);
        }
        Repository repository = dmh.repository(resources.toArray(new Resource[resources.size()]));
        final FileWriter writer = new FileWriter(repoXml);
        dmh.writeRepository(repository, writer);
        writer.flush();
        logger.logp(Level.INFO, "ObrHandler", "createRepository", "Created {0} containing {1} resources.", new Object[]{repoXml, resources.size()});
    }

    private void updateRepository(File repoXml, File repoDir) throws IOException {
        boolean obsoleteRepo = false;
        long lastModifiedTime = repoXml.lastModified();
        obsoleteRepo = repoDir.lastModified() > lastModifiedTime; // let's be optimistic and see if the repoDir has been touched.
        if (!obsoleteRepo) {
            // now compare timestamp of each jar
            for (File jar : findAllJars(repoDir)) {
                if (jar.lastModified() > lastModifiedTime) {
                    logger.logp(Level.INFO, "ObrHandler", "updateRepository", "{0} is newer than repository.xml", new Object[]{jar});
                    obsoleteRepo = true;
                    break;
                }
            }
        }
        if (obsoleteRepo) {
            if (!repoXml.delete()) {
                throw new IOException("Failed to delete " + repoXml);
            } else {
                logger.logp(Level.INFO, "ObrHandler", "updateRepository", "Recreating {0}", new Object[]{repoXml});
            }
            createRepository(repoXml, repoDir);
        }
    }

    private List<File> findAllJars(File repo) {
        final List<File> files = new ArrayList<File>();
        repo.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    pathname.listFiles(this);
                } else if (pathname.getName().endsWith("jar")) {
                    files.add(pathname);
                }
                return true;
            }
        });
        return files;
    }

    /* package */ synchronized Bundle deploy(Resource resource) {
        final Resolver resolver = getRepositoryAdmin().resolver();
        boolean resolved = resolve(resolver, resource);
        if (resolved) {
            final int flags = !deployOptionalRequirements ? Resolver.NO_OPTIONAL_RESOURCES : 0;
            resolver.deploy(flags);
            return getBundle(resource);
        } else {
            Reason[] reqs = resolver.getUnsatisfiedRequirements();
            logger.logp(Level.WARNING, "ObrHandler", "deploy",
                    "Unable to satisfy the requirements: {0}", new Object[]{Arrays.toString(reqs)});
            return null;
        }
    }

    /* package */ boolean resolve(final Resolver resolver, Resource resource) {
        resolver.add(resource);
        boolean resolved = resolver.resolve();
        logger.logp(Level.INFO, "ObrHandler", "resolve", "At the end of first pass, resolver outcome is \n: {0}",
                new Object[]{getResolverOutput(resolver)});
        // The following code is not enough to deploy fragments, so it is commented out.
//        if (resolved && deployFragments) {
//            // Take a copy of required and optional resources before adding them to resolver, otherwise
//            // resolver.getRequiredResources() or resolver.getOptionalResources() throws IllegalStateException
//            // as its m_resolved flag changes to false when we add a resource to it.
//            final Resource[] requiredResources = resolver.getRequiredResources();
//            final Resource[] optionalResources = resolver.getOptionalResources();
//
//            for (Resource r: requiredResources) {
//                resolver.add(r);
//            }
//            for (Resource r: optionalResources) {
//                resolver.add(r);
//            }
//            resolved = resolver.resolve();
//            logger.logp(Level.INFO, "ObrHandler", "resolve", "At the end of second pass, resolver outcome is \n: {0}",
//                    new Object[]{getResolverOutput(resolver)});
//        }
        return resolved;
    }

    /* package */ synchronized Bundle deploy(String name, String version) {
        Resource resource = findResource(name, version);
        if (resource == null) {
            logger.logp(Level.INFO, "ObrHandler", "deploy",
                    "No resource matching name = {0} and version = {1} ", new Object[]{name, version});
            return null;
        }
        if (resource.isLocal()) {
            return getBundle(resource);
        }
        return deploy(resource);
    }

    /* package */ synchronized Bundle deploy(ModuleDefinition md) {
        return deploy(md.getName(), md.getVersion());
    }

    private Bundle getBundle(Resource resource) {
        for (Bundle b : context.getBundles()) {
            final String bsn = b.getSymbolicName();
            final Version bv = b.getVersion();
            final String rsn = resource.getSymbolicName();
            final Version rv = resource.getVersion();
            boolean versionMatching = (rv == bv) || (rv != null && rv.equals(bv));
            boolean nameMatching = (bsn == rsn) || (bsn != null && bsn.equals(rsn));
            if (nameMatching && versionMatching) return b;
        }
        return null;
    }

    private Resource findResource(String name, String version) {
        final RepositoryAdmin repositoryAdmin = getRepositoryAdmin();
        if (repositoryAdmin == null) {
            logger.logp(Level.WARNING, "ObrHandler", "findResource",
                    "OBR is not yet available, so can't find resource with name = {0} and version = {1} from repository",
                    new Object[]{name, version});
            return null;
        }
        String s1 = "(symbolicname=" + name + ")";
        String s2 = "(version=" + version + ")";
        String query = (version != null) ? "(&" + s1 + s2 + ")" : s1;
        try {
            Resource[] resources = getRepositoryAdmin().discoverResources(query);
            logger.logp(Level.INFO, "ObrHandler", "findResource",
                    "Using the first one from the list of {0} discovered bundles shown below: {1}",
                    new Object[]{resources.length, Arrays.toString(resources)});
            return resources.length > 0 ? resources[0] : null;
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
        }
    }

    private void printResolverOutput(Resolver resolver) {
        StringBuffer sb = getResolverOutput(resolver);
        logger.logp(Level.INFO, "ObrHandler", "printResolverOutput", "OBR resolver state: {0}", new Object[]{sb});
    }

    private StringBuffer getResolverOutput(Resolver resolver) {Resource[] addedResources = resolver.getAddedResources();
        Resource[] requiredResources = resolver.getRequiredResources();
        Resource[] optionalResources = resolver.getOptionalResources();
        Reason[] unsatisfiedRequirements = resolver.getUnsatisfiedRequirements();
        StringBuffer sb = new StringBuffer("Added resources: [");
        for (Resource r : addedResources) {
            sb.append("\n").append(r.getSymbolicName()).append(", ").append(r.getVersion()).append(", ").append(r.getURI());
        }
        sb.append("]\nRequired Resources: [");
        for (Resource r : requiredResources) {
            sb.append("\n").append(r.getURI());
        }
        String optionalRequirementsDeployed = deployOptionalRequirements ? "deployed" : "not deployed";
        sb.append("]\nOptional resources (" +  optionalRequirementsDeployed + "): [");
        for (Resource r : optionalResources) {
            sb.append("\n").append(r.getURI());
        }
        sb.append("]\nUnsatisfied requirements: [");
        for (Reason r : unsatisfiedRequirements) {
            sb.append("\n").append(r.getRequirement());
        }
        sb.append("]");
        return sb;
    }

}
