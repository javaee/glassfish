/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author bhavanishankar@dev.java.net
 */
public abstract class AbstractServerMojo extends AbstractMojo {

    // Only PluginUtil has access to org.glassfish.simpleglassfishapi.Constants
    // Hence declare the param names here.
    public final static String PLATFORM_KEY = "GlassFish_Platform";
    public final static String INSTANCE_ROOT_PROP_NAME = "com.sun.aas.instanceRoot";
    public static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot";
    public static final String CONFIG_FILE_URI_PROP_NAME = "org.glassfish.embeddable.configFileURI";
    private static final String NETWORK_LISTENER_KEY = "embedded-glassfish-config." +
            "server.network-config.network-listeners.network-listener.%s";

    public static String thisArtifactId = "org.glassfish.embedded:maven-embedded-glassfish-plugin";

    private static String SHELL_JAR = "lib/embedded/glassfish-embedded-static-shell.jar";
    private static String FELIX_JAR = "osgi/felix/bin/felix.jar";

    private static final String EMBEDDED_GROUP_ID = "org.glassfish.main.extras";
    private static final String EMBEDDED_ALL = "glassfish-embedded-all";
    private static final String EMBEDDED_ARTIFACT_PREFIX = "glassfish-embedded-";

    private static final String GF_API_GROUP_ID = "org.glassfish.main.common";
    private static final String GF_API_ARTIFACT_ID = "simple-glassfish-api";
    private static final String DEFAULT_GF_VERSION = "4.0";
    private static String gfVersion;

    /**
     * The remote repositories where artifacts are located.
     * This is automatically injected by the Maven framework.
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    protected List remoteRepositories;

    /**
     * Identifier of the Embedded GlassFish server.
     *
     * @parameter expression="${serverID}" default-value="maven"
     */
    protected String serverID;

    /**
     * <b><i>Note : Using &lt;ports&gt; configuration is preferred over this configuration.</b></i>
     * <p/>
     * Specify the HTTP port number.
     * <p/>
     * For example:
     * &lt;port&gt;8080&lt;/port&gt;
     * <p/>
     * This setting is ignored when configFile option is used.
     * <p/>
     *
     * @parameter expression="${port}" default-value="-1"
     */
    protected int port;


    /**
     * Location of valid GlassFish installation.
     *
     * @parameter expression="${installRoot}"
     */
    protected String installRoot;

    /**
     * Location of valid GlassFish domain.
     *
     * @parameter expression="${instanceRoot}"
     */
    protected String instanceRoot;

    /**
     * Location of custom configuration file (i.e., location of custom domain.xml).
     *
     * @parameter expression="${configFile}"
     */
    protected String configFile;

    /**
     * Specify whether the custom configuration file or config/domain.xml at
     * the specified instance root is operated read only or not.
     *
     * @parameter expression="${configFileReadOnly}" default-value="true"
     */
    protected Boolean configFileReadOnly;

    /**
     * Specify the port numbers for the network listeners.
     * <p/>
     * Built-in domain.xml has HTTP and HTTPS network listeners by names
     * http-listener and https-listener respectively.
     * That allows you to configure the ports like this:
     * <p/>
     * <pre>
     * &lt;ports&gt;
     *      &lt;http-listener&gt;8080&lt;/http-listener&gt;
     *      &lt;https-listener&gt;8181&lt;/http-listener&gt;
     * &lt;/ports&gt;
     * </pre>
     * <p/>
     * If you are using custom domain.xml, you can either configure the ports
     * directy in your domain.xml or configure using this configuration parameter by
     * correctly specifying port numbers for the the names of the network-listener element
     * of your domain.xml.
     *
     * @parameter
     */
    protected Map<String, String> ports;

    /**
     * Specify the set of properties required to bootstrap GlassFishRuntime.
     * For example:
     * <pre>
     * &lt;bootstrapProperties&gt;
     *      &lt;property>GlassFish_Platform=felix&lt;/property&gt;
     * &lt;/bootstrapProperties&gt;
     * </pre>
     *
     * @parameter
     */
    protected List<String> bootstrapProperties;

    /**
     * Specify the location of the properties file which has the properties required to bootstrap GlassFishRuntime.
     * For example:
     * <p/>
     * &lt;bootstrapPropertiesFile&gt;bootstrap.properties&lt;/bootstrapPropertiesFile&gt;
     * <p/>
     * where bootstrap.properties is a file containing the bootstrap properties.
     *
     * @parameter
     */
    protected File bootstrapPropertiesFile;

    /**
     * Specify the set of properties required to create a new Embedded GlassFish.
     * <p/>
     * For example:
     * <pre>
     * &lt;glassfishProperties&gt;
     *      &lt;property>embedded-glassfish-config.server.jms-service.jms-host.default_JMS_host.port=17676&lt;/property&gt;
     * &lt;/glassfishProperties&gt;
     * </pre>
     *
     * @parameter
     */
    protected List<String> glassfishProperties;

    /**
     * Specify the location of the properties file which has the properties required to create a new GlassFish.
     * For example:
     * <p/>
     * &lt;glassfishPropertiesFile&gt;glassfish.properties&lt;/glassfishPropertiesFile&gt;
     * <p/>
     * where glassfish.properties is a file containing the GlassFish properties.
     *
     * @parameter
     */
    protected File glassfishPropertiesFile;

    /**
     * Specify the system properties.
     * For example:
     * <pre>
     * &lt;systemProperties&gt;
     *      &lt;property&gt;com.sun.aas.imqLib=${env.S1AS_HOME}/../mq/lib&lt;/property&gt;
     *      &lt;property&gt;com.sun.aas.imqBin=${env.S1AS_HOME}/../mq/bin&lt;/property&gt;
     * &lt;/systemProperties>
     * </pre>
     *
     * @parameter
     */
    protected List<String> systemProperties;

    /**
     * Specify the location of the properties file which has the system properties.
     * <p/>
     * For example:
     * &lt;systemPropertiesFile&gt;/tmp/system.properties&lt;/systemPropertiesFile&gt;
     *
     * @parameter
     */
    protected File systemPropertiesFile;

    /**
     * Specify whether the temporary file system created by Embedded GlassFish
     * should be deleted when Maven exits.
     * <p/>
     * Embedded GlassFish creates the temporary
     * file system under java.io.tmpdir unless a different directory is specified with
     * glassfish.embedded.tmpdir system property.
     *
     * @parameter expression="${autoDelete}" default-value="true"
     */
    protected Boolean autoDelete;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * This is automatically injected by the Maven framework.
     *
     * @parameter default-value="${plugin.artifacts}"
     */
    private java.util.List<Artifact> artifacts; // pluginDependencies

    /**
     * @component
     */
    protected MavenProjectBuilder projectBuilder;

    /**
     * This is automatically injected by the Maven framework.
     *
     * @parameter expression="${localRepository}"
     * @required
     */
    protected ArtifactRepository localRepository;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    /**
     * Used to construct artifacts for deletion/resolution...
     *
     * @component
     */
    protected ArtifactFactory factory;

    /**
     * @parameter expression="${containerType}" default-value="all"
     * @deprecated This is a deprecated and unused configuration. Likely to be removed in the next version of the plugin.
     */
    protected String containerType;

//    protected GlassFish gf;

    // HashMap with Key=serverId, Value=Bootstrap ClassLoader
    protected static HashMap<String, ClassLoader> classLoaders = new HashMap();
    private static ClassLoader classLoader;

    /**
     * @component
     */
    private ArtifactMetadataSource artifactMetadataSource;

    public abstract void execute() throws MojoExecutionException, MojoFailureException;

    protected ClassLoader getClassLoader() throws MojoExecutionException {
/*
        URLClassLoader classLoader = classLoaders.get(serverID);
        if (classLoader != null) {
            printClassPaths("Using Existing Bootstrap ClassLoader. ServerId = " + serverID +
                    ", ClassPaths = ", classLoader);
            return classLoader;
        }
        try {
            classLoader = hasGlassFishInstallation() ? getInstalledGFClassLoader() : getUberGFClassLoader();
            classLoaders.put(serverID, classLoader);
            printClassPaths("Created New Bootstrap ClassLoader. ServerId = " + serverID
                    + ", ClassPaths = ", classLoader);
            return classLoader;
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
*/
        try {
            if (classLoader != null) {
                return classLoader;
            } else {
                classLoader = hasGlassFishInstallation() ? getInstalledGFClassLoader() : getUberGFClassLoader();
                printClassPaths("Created New Bootstrap ClassLoader. ServerId = " + serverID
                        + ", ClassPaths = ", classLoader);
            }
            return classLoader;
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }

    protected void cleanupClassLoader(String serverId) {
        ClassLoader cl = classLoaders.remove(serverID);
        if (cl != null) {
            System.out.println("Cleaned up ClassLoader for ServerID " + serverID);
        }
    }

    private void printClassPaths(String msg, ClassLoader classLoader) {
        System.out.println(msg);
        ClassLoader cl = classLoader;
        while (cl != null && cl instanceof URLClassLoader) {
            for (URL u : ((URLClassLoader) cl).getURLs()) {
                System.out.println("ClassPath Element : " + u);
            }
            cl = cl.getParent();
        }
    }

    // checks if the glassfish installation is present in the specified installRoot

    private boolean hasGlassFishInstallation() {
        return installRoot != null ? new File(installRoot, SHELL_JAR).exists()
                && new File(installRoot, FELIX_JAR).exists() : false;
    }

    private ClassLoader getInstalledGFClassLoader() throws Exception {
        File gfJar = new File(installRoot, SHELL_JAR);
        File felixJar = new File(installRoot, FELIX_JAR);
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{gfJar.toURI().toURL(), felixJar.toURI().toURL()}, getClass().getClassLoader());
        return classLoader;
    }

    private Artifact getUberFromSpecifiedDependency() {
        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                if (EMBEDDED_GROUP_ID.equals(artifact.getGroupId())) {
                    if (artifact.getArtifactId().startsWith(EMBEDDED_ARTIFACT_PREFIX)) {
                        return artifact;
                    }
                }
            }
        }
        return null;
    }

    // GlassFish should be of same version as simple-glassfish-api as defined in plugin's pom.
    private String getGlassfishVersion(Artifact gfMvnPlugin) throws Exception {
        if (gfVersion != null) {
            return gfVersion;
        }
        ResolutionGroup resGroup = artifactMetadataSource.retrieve(
                gfMvnPlugin, localRepository, remoteRepositories);
        MavenProject pomProject = projectBuilder.buildFromRepository(resGroup.getPomArtifact(),
                remoteRepositories, localRepository);
        List<Dependency> dependencies = pomProject.getOriginalModel().getDependencies();
        for (Dependency dependency : dependencies) {
            if (GF_API_GROUP_ID.equals(dependency.getGroupId()) &&
                    GF_API_ARTIFACT_ID.equals(dependency.getArtifactId())) {
                gfVersion = dependency.getVersion();
            }
        }
        gfVersion = gfVersion != null ? gfVersion : DEFAULT_GF_VERSION;
        return gfVersion;
    }

    private ClassLoader getUberGFClassLoader() throws Exception {
        // Use the version user has configured in the plugin.
        Artifact gfUber = getUberFromSpecifiedDependency();
        ClassLoader cl = getClass().getClassLoader();
        if (gfUber == null) { // not specified as dependency, hence not there in the classloader cl.
            Artifact gfMvnPlugin = (Artifact) project.getPluginArtifactMap().get(thisArtifactId);
            String gfVersion = getGlassfishVersion(gfMvnPlugin); // get the same version of uber jar as that of simple-glassfish-api used while building this plugin.
            gfUber = factory.createArtifact(EMBEDDED_GROUP_ID, EMBEDDED_ALL,
                    gfVersion, "compile", "jar");
            resolver.resolve(gfUber, remoteRepositories, localRepository);
            cl = new URLClassLoader(
                    new URL[]{gfUber.getFile().toURI().toURL()}, getClass().getClassLoader());
        }
        return cl;
    }

    protected Properties getGlassFishProperties() {
        Properties props = new Properties();

        if (instanceRoot != null) {
            props.setProperty(INSTANCE_ROOT_PROP_NAME,
                    new File(instanceRoot).getAbsolutePath());
        }

        if (configFile != null) {
            try {
                URI configFileURI = URI.create(configFile);
                String scheme = configFileURI.getScheme();
                if (scheme == null || "file".equalsIgnoreCase(scheme)) {
                    props.setProperty(CONFIG_FILE_URI_PROP_NAME, new File(configFileURI).toURI().toString());
                } else {
                    // if it is a java.net.URI pointing to file: or jar: or http: then use it as is.
                    props.setProperty(CONFIG_FILE_URI_PROP_NAME, configFileURI.toString());
                }
            } catch (Exception ex) {
                // should never come here, but just in case...
                props.setProperty(CONFIG_FILE_URI_PROP_NAME, new File(configFile).toURI().toString());
            }
        }

        if (!configFileReadOnly) {
            props.setProperty("org.glassfish.embeddable.configFileReadOnly", "false");
        }

        if (port != -1 && configFile == null) {
            String httpListener = String.format(NETWORK_LISTENER_KEY, "http-listener");
            props.setProperty(httpListener + ".port", String.valueOf(port));
            props.setProperty(httpListener + ".enabled", "true");
        }

        if (ports != null) {
            for (String listenerName : ports.keySet()) {
                String portNumber = ports.get(listenerName);
                if (portNumber != null && portNumber.trim().length() > 0) {
                    String networkListener = String.format(NETWORK_LISTENER_KEY, listenerName);
                    props.setProperty(networkListener + ".port", portNumber);
                    props.setProperty(networkListener + ".enabled", "true");
                }
            }
        }

        if (!autoDelete) {
            props.setProperty("org.glassfish.embeddable.autoDelete", "false");
        }

        load(glassfishPropertiesFile, props);
        load(glassfishProperties, props);

        return props;
    }

    protected Properties getBootStrapProperties() {
        setSystemProperties();
        Properties props = new Properties();
        props.setProperty(PLATFORM_KEY, "Static");
        if (installRoot != null) {
            props.setProperty(INSTALL_ROOT_PROP_NAME,
                    new File(installRoot).getAbsolutePath());
        }
        load(bootstrapPropertiesFile, props);
        load(bootstrapProperties, props);
        return props;
    }

    private void load(List<String> stringList, Properties p) {
        if (p == null || stringList == null) {
            return;
        }
        for (String prop : stringList) {
            try {
                p.load(new StringReader(prop));
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
    }

    private void load(File propertiesFile, Properties p) {
        if (propertiesFile == null || p == null) {
            return;
        }
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(propertiesFile);
            p.load(stream);
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }
        }
    }

    private void setSystemProperties() {
        Properties sysProps = new Properties();
        load(systemPropertiesFile, sysProps);
        load(systemProperties, sysProps);
        for (Object obj : sysProps.keySet()) {
            String key = (String) obj;
            String currentVal = System.getProperty(key);
            if (currentVal == null) {
                String value = sysProps.getProperty(key);
                if (value != null && value.trim().length() > 0) {
                    System.setProperty(key, value);
                    System.out.println("Set system property [" + key + " = " + value + "]");
                }
            }
        }
    }

//    private String getDefaultInstallRoot() {
//        Artifact gfMvnPlugin = (Artifact) project.getPluginArtifactMap().get(thisArtifactId);
//        String userDir = System.getProperty("user.home");
//        String fs = File.separator;
//        return new File(userDir, "." + gfMvnPlugin.getArtifactId() + fs +
//                gfMvnPlugin.getVersion()).getAbsolutePath();
//    }
//
//    private String getDefaultInstanceRoot(String installRoot) {
//        String fs = File.separator;
//        return new File(installRoot, "domains" + fs + "domain1").getAbsolutePath();
//    }

    public void startGlassFish(String serverId, ClassLoader cl, Properties bootstrapProperties,
                               Properties glassfishProperties) throws Exception {
        Class clazz = cl.loadClass(PluginUtil.class.getName());
        Method m = clazz.getMethod("startGlassFish", new Class[]{String.class,
                ClassLoader.class, Properties.class, Properties.class});
        m.invoke(null, new Object[]{serverId, cl, bootstrapProperties, glassfishProperties});
    }

    public void stopGlassFish(String serverId, ClassLoader cl) throws Exception {
        Class clazz = cl.loadClass(PluginUtil.class.getName());
        Method m = clazz.getMethod("stopGlassFish", new Class[]{String.class});
        m.invoke(null, new Object[]{serverId});
    }

}
