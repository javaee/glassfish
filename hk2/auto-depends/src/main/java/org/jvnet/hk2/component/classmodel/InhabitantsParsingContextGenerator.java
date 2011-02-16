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
package org.jvnet.hk2.component.classmodel;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.classmodel.reflect.AnnotatedElement;
import org.glassfish.hk2.classmodel.reflect.AnnotationType;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.hk2.classmodel.reflect.util.ParsingConfig;
import org.glassfish.hk2.classmodel.reflect.util.ResourceLocator;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.InhabitantMetadata;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import com.sun.hk2.component.InhabitantsScanner;

/**
 * Responsible for generating the collection of inhabitants, decoupling from
 * implementation detail for the caller.
 * <p/>
 * The caller is expected to continually build up the InhabitantsGenerator
 * context by calling add*(), followed by calling getModelInhabitants() to
 * obtain the progenitors of the inhabitants.
 * <p/>
 * There are two ways to close this instance, either through {@link #getContext()}
 * or through calling {@link #close()} directly.
 *
 * @author Jerome Dochez
 * @author Jeff Trent
 * @since 3.1
 */
public abstract class InhabitantsParsingContextGenerator implements Closeable {

    private static final Logger logger = 
      Logger.getLogger(InhabitantsParsingContextGenerator.class.getName());

    private Parser parser;
    private final ParsingContext context;

    private final LinkedHashMap<String, InhabitantsScanner> metaInfScanners = 
            new LinkedHashMap<String, InhabitantsScanner>();

    private FileFilter inhabitantsClassPathFilter; // might also be a full ClassPathAdvisor!
    
    private final LinkedHashSet<URI> parsed = new LinkedHashSet<URI>();
    

    /**
     * Factory for the {@link InhabitantsParsingContextGenerator}
     *
     * @param h habitat not currently used; reserved for future use
     * @param es the executor to use for any async processing (e.g., parsing)
     * @param inhabitantsClassPath the fully qualified classpath in order to resolve the class-model
     * @param inhabitantsClassPathFilter the filter used for pruning the classpath (may also be a {@link ClassPathAdvisor})
     * 
     * @return an empty context InhabitantsGenerator
     */
    public static InhabitantsParsingContextGenerator create(Habitat h, 
            ExecutorService es,
            ClassPath inhabitantsClassPath,
            FileFilter inhabitantsClassPathFilter) {
        return new InhabitantsParsingContextGenerator(es, inhabitantsClassPath, inhabitantsClassPathFilter) {};
    }
    
    protected InhabitantsParsingContextGenerator(final ExecutorService es,
            final ClassPath inhabitantsClassPath,
            final FileFilter inhabitantsClassPathFilter) {
        // setup the parser
        ParsingContext.Builder builder = new ParsingContext.Builder();
        final Set<String> annotations = new HashSet<String>();
        annotations.add(Contract.class.getName());
        annotations.add(Service.class.getName());
        annotations.add(InhabitantMetadata.class.getName());
        annotations.add(RunLevel.class.getName());
        annotations.add("org.jvnet.hk2.config.Configured");

        builder.config(new ParsingConfig() {
            final Set<String> empty = Collections.emptySet();

            public Set<String> getAnnotationsOfInterest() {
                return empty;
            }

            public Set<String> getTypesOfInterest() {
                return annotations;
            }
        });

        // optionally provide an executor
        builder.executorService(es);
        
        // Important Note:
        // we are careful not to create a locator if we don't have to because it will
        // mean additional burden on the parser since it will need to locate and resolve
        // all of the dangling types.  If we are being called in unit-test mode then
        // the chances for this is very unlikely because we have (or should have) a
        // "full" testing classpath.
        
        Locator locator = null;
        if (null == inhabitantsClassPathFilter) {
          this.inhabitantsClassPathFilter = null;
          locator = (null == inhabitantsClassPath) ? null : new Locator(inhabitantsClassPath);
        } else {
          this.inhabitantsClassPathFilter = inhabitantsClassPathFilter;
          if (ClassPathAdvisor.class.isInstance(this.inhabitantsClassPathFilter)) {
            ClassPathAdvisor advisor = ClassPathAdvisor.class.cast(this.inhabitantsClassPathFilter);
            advisor.starting(inhabitantsClassPath);
          }

          if (null != inhabitantsClassPath) {
            LinkedHashSet<File> accepted = new LinkedHashSet<File>();
            for (File f : inhabitantsClassPath.getFileEntries()) {
              if (inhabitantsClassPathFilter.accept(f)) {
                accepted.add(f);
              }
            }
            
            locator = new Locator(ClassPath.create(null, accepted));
          }
        }
        
        builder.locator(locator);
        
        this.context = builder.build();
        this.parser = new Parser(context);
    }

    /**
     * Add the collection of files to the current InhabitantsGenerator context.
     *
     * @param files the files to parse.
     * @throws IOException
     */
    public void parse(Collection<File> files) throws IOException {
        for (File file : files) {
            parse(file);
        }
    }

    /**
     * Retrieves the parsing context that can be used for model generation elsewhere.
     * <b>Note that this can be called at most once and then this instance is implicitly
     * closed.</b>
     *
     * @return the parsing context given the code sources provided
     */
    public ParsingContext getContext() {
        if (null != parser) {
          try {
            parser.awaitTermination();
          } catch (InterruptedException e) {
            close();
            throw new RuntimeException(e);
          }
        }
        
        if (ClassPathAdvisor.class.isInstance(this.inhabitantsClassPathFilter)) {
          ClassPathAdvisor advisor = ClassPathAdvisor.class.cast(this.inhabitantsClassPathFilter);
          advisor.finishing(getSignificantURIReferences(), getInsignificantURIReferences());
          
          // we don't want to be notified again
          this.inhabitantsClassPathFilter = null;
        }
        
        return context;
    }
    
    /**
     * Given the set of annotations that comprise Hk2, as well as the provided classpath / files to
     * introspect, return the set of URIs that actually reference something of "significant value"
     * pertaining to habitat creation.
     */
    private Set<URI> getSignificantURIReferences() {
      LinkedHashSet<URI> result = new LinkedHashSet<URI>();

      Types types = context.getTypes();
      for (String annotation : context.getConfig().getTypesOfInterest()) {
        AnnotationType atype = (AnnotationType) types.getBy(annotation);
        if (null != atype) {
          Collection<AnnotatedElement> coll = atype.allAnnotatedTypes();
          for (AnnotatedElement ae : coll) {
            Type type = types.getBy(ae.getName());
            if (null != type) {
              for (URI uri : type.getDefiningURIs()) {
                try {
                  result.add(new File(uri).getCanonicalFile().toURI());
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              }
            }
          }
        }
      }
      
      return Collections.unmodifiableSet(result);
    }

    /**
     * Given the set of annotations that comprise Hk2, as well as the provided classpath / files to
     * introspect, return the set of URIs that did not provide any "significant value"
     * pertaining to habitat creation.
     */
    private Set<URI> getInsignificantURIReferences() {
      LinkedHashSet<URI> result = new LinkedHashSet<URI>();

      result.addAll(parsed);
      result.removeAll(getSignificantURIReferences());
      
      return Collections.unmodifiableSet(result);
    }
    
    /**
     * @return the collection of {@link InhabitantsScanner}s being maintained
     */
    public Collection<InhabitantsScanner> getInhabitantsScanners() {
        return Collections.unmodifiableCollection(metaInfScanners.values());
    }

    protected void addInhabitantsScanner(String name, InhabitantsScanner is) {
        synchronized (metaInfScanners) {
            if (!metaInfScanners.containsKey(name)) {
                metaInfScanners.put(name, is);
            }
        }
    }

    /**
     * Eventually we can perform optimizations here instead of a pass-thru to parseAlways.
     */
    public void parse(File f) throws IOException {
      if (null == parser) {
        throw new IllegalStateException("parser closed");
      }
      
      f = f.getCanonicalFile();
      if (null == inhabitantsClassPathFilter || inhabitantsClassPathFilter.accept(f)) {
        parseAlways(parser, f);
      }
    }

    protected void parseAlways(Parser parser, final File f) throws IOException {
      logger.log(Level.FINE, "introspecting {0}", f);
      parsed.add(f.toURI());
      try {
        parser.parse(f, new Runnable() {
            public void run() {
                logger.log(Level.FINER, "Finished introspecting {0}", f.getName());
            }
        });
      } catch (IOException e) {
        logger.log(Level.WARNING, "problem during parsing - closing prematurely", e);
        close();
      }
    }

    @Override
    public void close() {
      if (null != parser) {
        parser.close();
        parser = null;
      }
    }

    
    private static class Locator implements ResourceLocator {
      private final ClassLoader resourceLoader;
      
      public Locator(ClassPath inhabitantsClassPath) {
        try {
          resourceLoader = new URLClassLoader(inhabitantsClassPath.getRawURLs(), null);
          
          if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "resourceLoader is {0}", Arrays.asList(inhabitantsClassPath.getRawURLs()).toString());
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public InputStream openResourceStream(String name) {
        if (name.startsWith("java/")) {
          logger.log(Level.FINE, "skipping {0}", name);
          return null;  // wasteful to parse these
        }
        if (name.indexOf(".")==-1) {
          return null; // intrinsic types.
        }

        logger.log(Level.FINE, "loading resource {0}", name);
        
        try {
          InputStream resource = resourceLoader.getResourceAsStream(name);
          logger.log(Level.FINE, "resource {0} resolved to {1}", new Object[] {name, resource});
          return resource;
        } catch (Exception e) {
          throw new RuntimeException("failed while getting resource: " + name);
        }
      }

      @Override
      public URL getResource(String name) {
        if (name.startsWith("java/")) {
          logger.log(Level.FINE, "skipping {0}", name);
          return null;  // wasteful to parse these
        }
        if (name.indexOf(".")==-1) {
          return null; // intrinsic types.
        }
        
        return resourceLoader.getResource(name);
      }
    }

}
