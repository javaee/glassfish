/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.embeddable.archive;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstraction for a Scattered Java EE module (parts disseminated in various directories).
 * <p/>
 * <p/>
 * Usage example :
 * <p/>
 * <style type="text/css">
 * .ln { color: rgb(0,0,0); font-weight: normal; font-style: normal; }
 * .s0 { color: rgb(128,128,128); }
 * .s1 { }
 * .s2 { color: rgb(0,0,255); }
 * .s3 { color: rgb(128,128,128); font-weight: bold; }
 * .s4 { color: rgb(255,0,255); }
 * </style>
 * <pre>
 * <a name="l58">        GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish();
 * <a name="l59">        glassfish.start();
 * <a name="l60">
 * <a name="l61">        </span><span class="s0">// Create a scattered web application.</span><span class="s1">
 * <a name="l62">        ScatteredArchive archive = </span><span class="s2">new </span><span class="s1">ScatteredArchive(</span><span class="s4">&quot;testapp&quot;</span><span class="s1">, ScatteredArchive.Type.WAR);
 * <a name="l63">        </span><span class="s0">// target/classes directory contains my complied servlets</span><span class="s1">
 * <a name="l64">        archive.addClassPath(</span><span class="s4">&quot;target/classes&quot;</span><span class="s1">);
 * <a name="l67">        </span><span class="s0">// /tmp/sun-web.xml is my META-INF/sun-web.xml</span><span class="s1">
 * <a name="l68">        archive.addMetadata(</span><span class="s4">&quot;META-INF/sun-web.xml&quot;</span><span class="s1">, </span><span class="s4">&quot;/tmp/sun-web.xml&quot;</span><span class="s1">);
 * <a name="l69">
 * <a name="l70">        Deployer deployer = glassfish.getDeployer();
 * <a name="l71">        </span><span class="s0">// Deploy my scattered web application</span><span class="s1">
 * <a name="l72">        deployer.deploy(archive.toURI());
 * </pre>
 *
 * @author Jerome Dochez
 * @author bhavanishankar@dev.net
 */
public class ScatteredArchive {

    String name;
    String type;
    File rootDirectory;
    List<File> classpaths = new ArrayList<File>();
//    File resourcespath;
    Map<String, File> metadatas = new HashMap<String, File>();

    /**
     * Construct a new empty scattered archive.
     *
     * @param name Name of the archive.
     * @param type Type of the archive
     * @throws NullPointerException if name or type is null
     */
    public ScatteredArchive(String name, Type type) {
        if (name == null) {
            throw new NullPointerException("name must not be null.");
        }
        if (type == null) {
            throw new NullPointerException("type must not be null.");
        }
        this.name = name;
        this.type = (type == Type.WAR) ? "war" : "jar";
    }

    /**
     * Construct a new scattered archive with all the contents from the rootDirectory.
     * <p/>
     * By default, a scattered archive is not different from any other
     * archive where all the files are located under a top level
     * directory (rootDirectory).
     * <p/>
     * For example, In case of a WAR type archive, the rootDirectory should look like this:
     * <pre>
     *      rootDirectory/WEB-INF/classes/org/myorg/FooServlet.class
     *      rootDirectory/WEB-INF/classes/org/myorg/Bar.class
     *      rootDirectory/WEB-INF/web.xml
     *      rootDirectory/WEB-INF/lib/myjar.jar
     *      rootDirectory/index.jsp
     *      rootDirectory/theme.css
     *      rootDirectory/helper.js
     * </pre>
     * Some files can then be scattered in different locations and be specified
     * through the appropriate add methods of this class.
     * <p/>
     *
     * @param name          Name of the archive.
     * @param type          Type of the archive
     * @param rootDirectory Top level directory.
     * @throws NullPointerException     if name, type or rootDirectory is null.
     * @throws IllegalArgumentException if rootDirectory does not exist or is not a directory.
     */
    public ScatteredArchive(String name, Type type, String rootDirectory) {
        this(name, type, rootDirectory != null ? new File(rootDirectory) : null);
    }

    /**
     * Construct a new scattered archive with all the contents from the rootDirectory.
     * <p/>
     * Follows the same semantics as {@link ScatteredArchive(String, ScatteredArchive.Type, String)} constructor.
     */
    public ScatteredArchive(String name, Type type, File rootDirectory) {
        if (name == null) {
            throw new NullPointerException("name must not be null.");
        }
        if (type == null) {
            throw new NullPointerException("type must not be null.");
        }
        if (rootDirectory == null) {
            throw new NullPointerException("rootDirectory must not be null.");
        }
        if (!rootDirectory.exists()) {
            throw new IllegalArgumentException(rootDirectory + " does not exist.");
        }
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException(rootDirectory + " is not a directory.");
        }
        this.name = name;
        this.type = (type == Type.WAR) ? "war" : "jar";
        this.rootDirectory = rootDirectory;
    }

    /**
     * Construct a new scattered archive with a set of classpaths.
     * <p/>
     * classpaths can contain Directory or JAR file locations.
     * <p/>
     * Using this constructor has the same effect of doing:
     * <pre>
     *      ScatteredArchive archive = new ScatteredArchive(name, type);
     *      for(String classpath : classpaths)
     *          archive.addClassPath(classpath);
     *      }</pre>
     *
     * @param name       Name of the archive.
     * @param type       Type of the archive "war" or "jar" or "rar".
     * @param classpaths Directory or JAR file locations.
     * @throws NullPointerException          if name, type or classpaths is null
     * @throws IllegalArgumentException if any of the classpaths is not found.
     */
//    public ScatteredArchive(String name, Type type, String[] classpaths) {
//
//    }

    /**
     * Construct a new scattered archive with a set of classpaths.
     *
     * The permitted values and their meanings are as specified for the
     * {@link ScatteredArchive(String, ScatteredArchive.Type, String, String[])}  constructor.
     *
     * @param name Name of the archive
     * @param type Type of the archive
     * @param classpaths Directory or JAR file locations.
     */
//    public ScatteredArchive(String name, Type type, File[] classpaths) {
//
//    }

    /**
     * Add a directory or a JAR file to this scattered archive.
     * <p/>
     * The classpath that is added is considered as a plain Java CLASSPATH.
     * <p/>
     * <b>Case 1 : classpath is a directory:</b>
     * <p/>
     * Let us say there is TEMP/abc directory, which has following contents:
     * <pre>
     *      TEMP/abc/org/myorg/a/A.class
     *      TEMP/abc/org/myorg/b/B.class
     *      TEMP/abc/com/xyz/c/C.class
     *      TEMP/abc/LocalStrings.properties
     *      TEMP/abc/image/1.png
     * </pre>
     * then addClassPath(new File("TEMP", "abc") will make:
     * <p/>
     * (a) The following classes available in the deployed scattered archive application:
     * <pre>
     *          org.myorg.a.A
     *          org.myorg.b.B
     *          com.xyz.c.C
     * </pre>
     * (b) LocalStrings.properties available in the deployed scattered archive application.
     * So, the deployed application can do ResourceBundle.getBundle("LocalStrings");
     * <p/>
     * (c) image/1.png available in the deployed scattered archive application.
     * So, the deployed application can load the image file via getClass().getClassLoader().getResource("image/1.png");
     * <p/>
     * If there is any other type of file under TEMP/abc then it will also be available
     * in the deployed scattered archive application's classloader.
     * <p/>
     * <b>Case 2: classpath is a JAR file</b>
     * <p/>
     * Let us say there is TEMP/xyz.jar, then addClassPath(new File("TEMP", "xyz.jar"))
     * will make all the classes and any random files inside TEMP/xyz.jar
     * available in the deployed scattered archive application.
     *
     * @param classpath A directory or a JAR file.
     * @throws NullPointerException     if classpath is null
     * @throws IllegalArgumentException if the classpath is not found.
     */
    public void addClassPath(String classpath) {
        addClassPath(classpath != null ? new File(classpath) : null);
    }

    /**
     * Add a directory or a JAR file to this scattered archive.
     * <p/>
     * Follows the same semantics as {@link #addClassPath(String)} method.
     */
    public void addClassPath(File classpath) {
        if (classpath == null) {
            throw new NullPointerException("classpath must not be null.");
        }
        if (!classpath.exists()) {
            throw new IllegalArgumentException(classpath + " does not exist.");
        }
        this.classpaths.add(classpath);
    }

    /**
     * Add a new metadata to this scattered archive.
     * <p/>
     * A metadata is identified by its name (e.g., META-INF/ejb.xml)
     * <p/>
     * If the scattered archive already contains the metadata with the same name,
     * then the old value is replaced.
     *
     * @param metadataName name of the metadata (e.g.,
     *                     META-INF/ejb.xml or META-INF/sun-ejb-jar.xml)
     * @param metadata     Metadata location. Must be a File location.
     * @throws NullPointerException     if metadataName or metadata is null
     * @throws IllegalArgumentException if the metadata does not exist or metadata is a directory.
     */
    public void addMetadata(String metadataName, String metadata) {
        addMetadata(metadataName, metadata != null ? new File(metadata) : null);
    }

    /**
     * Add a new metadata to this scattered archive.
     * <p/>
     * Follows the same semantics as {@link #addMetadata(String, String)} method.
     */
    public void addMetadata(String metadataName, File metadata) {
        if (metadataName == null) {
            throw new NullPointerException("metadataName must not be null.");
        }
        if (metadata == null) {
            throw new NullPointerException("metadata must not be null.");
        }
        if (!metadata.exists()) {
            throw new IllegalArgumentException(metadata + " does not exist.");
        }
        if(metadata.isDirectory()) {
            throw new IllegalArgumentException(metadata + " is a directory.");
        }
        this.metadatas.put(metadataName, metadata);
    }

    /**
     * Set the location of resources files to this scattered archive.
     * <p/>
     * For a WAR type scattered archive, the specified resource location can be
     * thought of as a document root of the web application. The document root
     * is where JSP pages, and static web resources such as images are stored.
     * <p/>
     * For the other type of archive, all the contents under the specified
     * resource location will be available in the deployed scattered
     * application's classloader.
     *
     * @param resourcespath Resources directory.
     * @throws NullPointerException     if resourcepath is null.
     * @throws IllegalArgumentException if resourcespath is not found or is not a directory.
     */
//    public void setResourcePath(String resourcespath) {
//        setResourcePath(resourcespath != null ? new File(resourcespath) : null);
//    }

    /**
     * Set the location of resources files to this scattered archive.
     * <p/>
     * Follows the same semantics as {@link #setResourcePath(String)} method.
     */
//    public void setResourcePath(File resourcespath) {
//        if (resourcespath == null) {
//            throw new NullPointerException("resourcespath must not be null.");
//        }
//        if (!resourcespath.exists()) {
//            throw new IllegalArgumentException(resourcespath + " does not exist.");
//        }
//        if (!resourcespath.isDirectory()) {
//            throw new IllegalArgumentException(resourcespath + " is not a directory");
//        }
//        this.resourcespath = resourcespath;
//    }

    /**
     * Get the deployable URI for this scattered archive.
     *
     * @return Deployable scattered archive URI.
     */
    public URI toURI() {
        return new Assembler().assemble(this);
    }

    /**
     * Enumeration values for the scattered Java EE module types.
     *
     * @author bhavanishankar@java.net
     */
    public enum Type {
        /**
         * The module is an Enterprise Java Bean archive.
         */
        EJB,
        /**
         * The module is an Web Application archive.
         */
        WAR,
        /**
         * The module is an Connector archive.
         */
        RAR,
        /**
         * The module is an Client Application archive.
         */
        CAR
    }
}
