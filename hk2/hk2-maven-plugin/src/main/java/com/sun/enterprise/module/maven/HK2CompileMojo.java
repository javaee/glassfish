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
package com.sun.enterprise.module.maven;

import com.sun.enterprise.tools.apt.MetainfServiceGenerator;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractCompilerMojo;
import org.apache.maven.plugin.CompilationFailureException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.CompilerError;
import org.codehaus.plexus.compiler.CompilerException;
import org.jvnet.hk2.config.generator.AnnotationProcessorFactoryImpl;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Replacement of the default javac mojo that uses APT, so that we can not only
 * compile source code but also generate HK2 related metadata at the same time.
 *
 * @goal hk2-compile
 * @phase compile
 * @author Kohsuke Kawaguchi
 */
public class HK2CompileMojo extends CompilerMojo {
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Additional jars that contain annotation processors.
     *
     * @parameter
     */
    public Processor[] processors;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository localRepository;

    /**
     * @component
     */
    private ArtifactMetadataSource metadataSource;

    public void execute() throws MojoExecutionException, CompilationFailureException {
        // overwrite the compilerId value. This seems to be the only way to
        //do so without touching the copied files.
        setField("compilerId", "hk2-apt");

        // invoke APT with the known set of annotation processors that we care about.
        AptInvoker old = AptInvoker.replace(new AptInvoker() {
            List<CompilerError> compileInProcess(String[] args) throws CompilerException {
                com.sun.tools.apt.Main aptTool = new com.sun.tools.apt.Main();
                int r = aptTool.process(
                    new CompositeAnnotationProcessorFactory(
                        new MetainfServiceGenerator(),
                        new AnnotationProcessorFactoryImpl(),
                        loadAdditionalAnnotationProcessors()
                    ), new PrintWriter(System.out,true),args);
                if(r!=0)
                    throw new CompilerException("APT failed: "+r);

                // TODO: should I try to parse the output?
                return Collections.emptyList();
            }
        });
        try {
            super.execute();
        } finally {
            AptInvoker.replace(old);
        }

        // TODO: ideally we should do this in the AptCompiler class, but I don't know
        // how to get MavenProject injected there
        project.getCompileSourceRoots().add(new File(project.getBasedir(),"target/apt-generated-sources").getAbsolutePath());
    }

    /**
     * Loads additional annotation processors specified in &lt;processor> configuration element.
     *
     * <p>
     * We need to resolve their transitive dependencies as well, because otherwise they won't load.
     */
    private AnnotationProcessorFactory loadAdditionalAnnotationProcessors() throws CompilerException {
        if(processors==null)
            return new CompositeAnnotationProcessorFactory();   // none

        Set<Artifact> processorArtifacts = new HashSet<Artifact>();
        for (Processor p : processors) {
            processorArtifacts.add(p.createArtifact(artifactFactory));
        }

        // TODO: perhaps we should create one URLClassLoader per each annotation processor to
        // ensure sufficient isolation?
        Set<URL> classpaths = new LinkedHashSet<URL>();
        try {
            ArtifactResolutionResult result = artifactResolver.resolveTransitively(
                    processorArtifacts, project.getArtifact(),
                    localRepository,
                    this.project.getRemoteArtifactRepositories(),
                    metadataSource, new ScopeArtifactFilter("runtime"));
            for( Artifact a : (Set<Artifact>)result.getArtifacts()) {
                classpaths.add(a.getFile().toURL());
            }
        } catch (AbstractArtifactResolutionException e) {
            throw new CompilerException("Failed to resolve annotation processors",e);
        } catch (MalformedURLException e) {
            throw new CompilerException("Failed to resolve annotation processors",e);
        }

        // load them into the classloader
        ClassLoader cl = new URLClassLoader(classpaths.toArray(new URL[classpaths.size()]),getClass().getClassLoader());

        List<AnnotationProcessorFactory> factories = new ArrayList<AnnotationProcessorFactory>();
        for(AnnotationProcessorFactory apf : ServiceFinder.find(AnnotationProcessorFactory.class,cl)) {
            factories.add(apf);
            getLog().info("Picked up annotation processor "+apf.getClass().getName());
        }

        return new CompositeAnnotationProcessorFactory(factories);
    }

    private void setField(String name, String value) {
        try {
            Field field = AbstractCompilerMojo.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e); // impossible
        } catch (IllegalAccessException e) {
            throw new AssertionError(e); // impossible
        }
    }
}
