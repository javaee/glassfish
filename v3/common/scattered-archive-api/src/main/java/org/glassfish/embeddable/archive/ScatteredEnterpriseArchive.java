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
 * Abstraction for a Scattered Java EE Application.
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
 * <a name="l61">        </span><span class="s0">// Create a scattered web module</span><span class="s1">
 * <a name="l62">        ScatteredArchive webmodule = </span><span class="s2">new </span><span class="s1">ScatteredArchive(</span><span class="s4">&quot;testweb&quot;</span><span class="s1">, ScatteredArchive.Type.WAR);
 * <a name="l63">        </span><span class="s0">// target/classes directory contains my complied servlets</span><span class="s1">
 * <a name="l64">        webmodule.addClassPath(</span><span class="s4">&quot;target/classes&quot;</span><span class="s1">);
 * <a name="l65">        </span><span class="s0">// src/main/resources directory contains my static files such as .jsp, .img, .htm files.</span><span class="s1">
 * <a name="l66">        webmodule.setResourcePath(</span><span class="s4">&quot;src/main/resources&quot;</span><span class="s1">);
 * <a name="l67">        </span><span class="s0">// /tmp/sun-web.xml is my META-INF/sun-web.xml</span><span class="s1">
 * <a name="l68">        webmodule.addMetadata(</span><span class="s4">&quot;META-INF/sun-web.xml&quot;</span><span class="s1">, </span><span class="s4">&quot;/tmp/sun-web.xml&quot;</span><span class="s1">);
 * <a name="l69">
 * <a name="l70">        </span><span class="s0">// Create a scattered enterprise archive.</span><span class="s1">
 * <a name="l71">        ScatteredEnterpriseArchive archive = </span><span class="s2">new </span><span class="s1">ScatteredEnterpriseArchive(</span><span class="s4">&quot;testapp&quot;</span><span class="s1">);
 * <a name="l72">        </span><span class="s0">// Add scattered web module to the scattered enterprise archive.</span><span class="s1">
 * <a name="l73">        archive.addArchive(webmodule.toURI());
 * <a name="l74">        </span><span class="s0">// /tmp/mylibrary.jar is a library JAR file.</span><span class="s1">
 * <a name="l75">        archive.addArchive(</span><span class="s4">&quot;/tmp/mylibrary.jar&quot;</span><span class="s1">);
 * <a name="l76">        </span><span class="s0">// target/myejb.jar is a EJB module.</span><span class="s1">
 * <a name="l77">        archive.addArchive(</span><span class="s4">&quot;target/myejb.jar&quot;</span><span class="s1">);
 * <a name="l78">        </span><span class="s0">// src/application.xml is my META-INF/application.xml</span><span class="s1">
 * <a name="l79">        archive.addMetadata(</span><span class="s4">&quot;META-INF/application.xml&quot;</span><span class="s1">, </span><span class="s4">&quot;src/application.xml&quot;</span><span class="s1">);
 * <a name="l80">
 * <a name="l81">        Deployer deployer = glassfish.getDeployer();
 * <a name="l82">        </span><span class="s0">// Deploy my scattered enterprise application</span><span class="s1">
 * <a name="l83">        deployer.deploy(archive.toURI());
 * </pre>
 *
 * @author bhavanishankar@dev.net
 */
public class ScatteredEnterpriseArchive {

    String name;
    static final String type = "ear";
    List<File> archives = new ArrayList<File>();
    Map<String, File> metadatas = new HashMap<String, File>();
    
    /**
     * Construct a new scattered enterprise archive.
     *
     * @param name Name of the enterprise archive.
     * @throws NullPointerException if name is null.
     */
    public ScatteredEnterpriseArchive(String name) {
        if (name == null) {
            throw new NullPointerException("name must not be null.");
        }
        this.name = name;
    }

    /**
     * Add a module or a library to this scattered enterprise archive.
     * <p/>
     * The specified archive location should be one of the following:
     * <pre>
     *      ScatteredArchive URI obtained via {@link ScatteredArchive#toURI()}.
     *      Location of a library JAR file.
     *      Location of a Java EE module.
     * </pre>
     * Refer to the example above.
     *
     * @param archiveURI Module or library archive URI.
     * @throws NullPointerException          if archiveURI is null
     * @throws IllegalArgumentException if the archiveURI location is not found.
     */
    public void addArchive(URI archiveURI) {
        addArchive(archiveURI != null ? new File(archiveURI) : null);
    }

    /**
     * Add a module or a library to this scattered enterprise archive.
     * <p/>
     * The specified archive location should be one of the following:
     * <pre>
     *      Location of a library JAR file.
     *      Location of a Java EE module.
     * </pre>
     * Refer to the example above.
     *
     * @param archive Location of module or library archive. Must be a File location.
     * @throws NullPointerException          if archive is null
     * @throws IllegalArgumentException if the archive file is not found or archive file is a directory.
     */
    public void addArchive(String archive) {
        addArchive(archive != null ? new File(archive) : null);
    }

    /**
     * Add a module or a library to this scattered enterprise archive.
     * <p/>
     * Follows the same semantics as {@link #addArchive(String)} method.
     */
    public void addArchive(File archive) {
        if(archive == null) {
            throw new NullPointerException("archive must not be null.");
        }
        if(!archive.exists()) {
            throw new IllegalArgumentException(archive + " does not exist.");
        }
        if(archive.isDirectory()) {
            throw new IllegalArgumentException(archive + " is a directory.");
        }
        this.archives.add(archive);
    }

    /**
     * Add a new metadata to this enterprise archive.
     * <p/>
     * A metadata is identified by its name (e.g., META-INF/application.xml)
     * <p/>
     * If the scattered enterprise archive already contains the metadata with
     * the same name, the old value is replaced.
     *
     * @param metadataName name of the metadata (e.g., META-INF/application.xml)
     * @param metadata     Location of metdata. Must be a File location.
     * @throws NullPointerException          if metadataName or metadata is null
     * @throws IllegalArgumentException if the metadata is not found or metadata is a directory. 
     */
    public void addMetadata(String metadataName, String metadata) {
        addMetadata(metadataName, metadata != null ? new File(metadata) : null);
    }

    /**
     * Add a new metadata to this enterprise archive.
     * <p/>
     * Follows the same semantics as {@link #addMetadata(String, String)} method.
     */
    public void addMetadata(String metadataName, File metadata) {
        if(metadataName == null) {
            throw new NullPointerException("metadataName must not be null.");
        }
        if(metadata == null) {
            throw new NullPointerException("metadata must not be null.");
        }
        if(!metadata.exists()) {
            throw new IllegalArgumentException(metadata + " does not exist.");
        }
        if(metadata.isDirectory()) {
            throw new IllegalArgumentException(metadata + " is a directory.");
        }
        this.metadatas.put(metadataName, metadata);
    }

    /**
     * Get the deployable URI for this scattered enterprise archive.
     *
     * @return Deployable scattered enterprise Archive URI.
     */
    public URI toURI() {
        return new Assembler().assemble(this);
    }
}
