/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.ModuleMetadata;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * {@link ModuleDefinition} implementation that picks up most of the module
 * properties from the manifest file of the jar, as baked in by
 * the hk2-maven-plugin.
 *
 * @author Jerome Dochez
 */
public class DefaultModuleDefinition implements ModuleDefinition {
    
    private final String name;
    private final String version;
    private final String[] publicPkgs;
    protected final List<ModuleDependency> dependencies = new ArrayList<ModuleDependency>();
    protected final List<URI> classPath = new ArrayList<URI>();
    private final String importPolicy;
    private final String lifecyclePolicy;
    private final Manifest manifest;
    /**
     * Main attributes section of the manifest.
     * Always non-null.
     */
    protected final Attributes mainAttributes;
    /**
     * Metadata that works like index.
     */
    private final ModuleMetadata metadata = new ModuleMetadata();

    /** TO DO need to support a URI constructor */
    public DefaultModuleDefinition(File location) throws IOException {
        this(location, null);
    }
    
    protected DefaultModuleDefinition(File location, Attributes attr) throws IOException {

        classPath.add(location.toURI());
        
        Jar jarFile = Jar.create(location);
        manifest = jarFile.getManifest();
        if (attr==null && manifest!=null) {
            attr = manifest.getMainAttributes();
        }

        // no attributes whatsoever, I just use an empty collection to avoid 
        // testing for null all the time.
        if (attr==null) {
            attr = new Attributes();
        }
        this.mainAttributes = attr;
                
        // name
        if (attr.getValue(ManifestConstants.BUNDLE_NAME)!=null) {
            name = attr.getValue(ManifestConstants.BUNDLE_NAME);
        } else {
            name = location.getName();
        }

        // classpath
        parseClassPath(attr, location.toURI());
        
        // class exported...
        String exported = attr.getValue(ManifestConstants.PKG_EXPORT_NAME);
        ArrayList<String> tmpList = new ArrayList<String>();
        for( String token : new Tokenizer(exported,",")) {
            tmpList.add(token);
        }
        publicPkgs = tmpList.toArray(new String[tmpList.size()]);
        
        // class imported
        String imported = attr.getValue(ManifestConstants.BUNDLE_IMPORT_NAME);
        for( String token : new Tokenizer(imported,",")) {
            // no versioning so far...
            dependencies.add(new ModuleDependency(token,""));
        }

        importPolicy = attr.getValue(ManifestConstants.IMPORT_POLICY);
        lifecyclePolicy = attr.getValue(ManifestConstants.LIFECYLE_POLICY);
        
        version = "1.0"; // for now ;-)

        parseAttributes(attr);

        jarFile.loadMetadata(metadata);
    }

    /**
     * Parses <tt>{@value ManifestConstants#CLASS_PATH}</tt> from manifest attributes
     * and updates URI list.
     */
    protected void parseClassPath(Attributes attr, URI baseURI) {
        String classpath = attr.getValue(ManifestConstants.CLASS_PATH);
        for( String classpathElement : new Tokenizer(classpath," ")) {
            classpathElement = decorateClassPath(classpathElement);
            URI result;
            File ref = new File(classpathElement);
            if (!ref.isAbsolute()) {
                try {
                    result = baseURI.resolve(classpathElement);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unable to parse Class-Path entry '"+classpath+"' in "+baseURI);
                }
            } else
                result = ref.toURI();
            classPath.add(result);
        }
    }

    /**
     * Extension point to manipulate the classpath element before it's parsed.
     * @see #parseClassPath(Attributes,URI) 
     */
    protected String decorateClassPath(String classpathElement) {
        return classpathElement;
    }

    /**
     * Extensibility point to parse more information from Manifest attributes.
     *
     * @param attr
     *      Main attributes of the manifest. Always non-null.
     */
    protected void parseAttributes(Attributes attr) {
        // noop
    }

    /**
     * Returns the name of the module
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the list of classes and packages that have been designated as
     * public interfaces of this module.
     * @return the list of public packages or classes
     */
    public String[] getPublicInterfaces() {
        return publicPkgs.clone();
    }
    
    /**
     * Returns the list of module dependencies
     * @return the ModuleDependency
     */
    public ModuleDependency[] getDependencies() {
        return dependencies.toArray(new ModuleDependency[dependencies.size()]);
    }
    
    /**
     * Returns the list of URI locations forming the classpath for this module.
     *
     * @return the list of URI locations for this module
     */
    public URI[] getLocations() {
        return classPath.toArray(new URI[classPath.size()]);
    }
    
    /**
     * Returns the module's version
     * @return the module's version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Returns the class name implementing the
     * {@link com.sun.enterprise.module.ImportPolicy ImportPolicy} interface for this module or
     * null if there is no such implementation
     * @return the  {@link com.sun.enterprise.module.ImportPolicy ImportPolicy} implementation class name
     */
    public String getImportPolicyClassName() {
        return importPolicy;
    }
    
    /**
     * Returns the class name implementing the
     * {@link com.sun.enterprise.module.LifecyclePolicy LifecyclePolicy} interface for this module or
     * null if there is no such implementation
     * @return the  {@link com.sun.enterprise.module.LifecyclePolicy LifecyclePolicy} implementation class name
     */
    public String getLifecyclePolicyClassName() {
        return lifecyclePolicy;
    }
    
    /**
     * Returns the manifest file from the module's implementation jar file
     * @return the manifest file
     */
    public Manifest getManifest() {
        return manifest;
    }

    public ModuleMetadata getMetadata() {
        return metadata;
    }
}
