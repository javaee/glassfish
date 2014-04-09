/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.classmodel.reflect;

import org.glassfish.hk2.classmodel.reflect.impl.ModelClassVisitor;
import org.glassfish.hk2.classmodel.reflect.impl.TypeBuilder;
import org.glassfish.hk2.classmodel.reflect.impl.TypesCtr;
import org.glassfish.hk2.classmodel.reflect.impl.TypesImpl;
import org.glassfish.hk2.classmodel.reflect.util.CommonModelRegistry;
import org.glassfish.hk2.classmodel.reflect.util.ParsingConfig;
import org.glassfish.hk2.classmodel.reflect.util.ResourceLocator;
import org.glassfish.hk2.external.org.objectweb.asm.ClassVisitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Parsing context for parsing jars and directories and getting a classmodel back
 *
 * @author Jerome Dochez
 */
public class ParsingContext {

    /**
     * Context builder
     */
    public static class Builder {
        Logger logger=Logger.getLogger("org.glassfish.hk2.classmodel");
        ExecutorService executorService=null;
        ArchiveSelector archiveSelector=null;
        ParsingConfig config=null;
        ResourceLocator locator=null;

        /**
         * Returns the configured or default logger for the class-model library.
         *
         * @return the current logger associated with this builder, either
         * set using {@link #logger(java.util.logging.Logger)} method, either
         * using the default logger for this library.
         */
        public Logger logger() {
            return logger;
        }

        /**
         * Sets the logger to be used during the parsing activity.
         *
         * @param logger a logger instance
         * @return itself
         */
        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Sets the executor service to be used to spawn threads during the
         * parsing activity. The parsing activity is an asynchronous process
         * that can choose to spawn threads to handle sub part of the handling
         * process.
         *
         * @param service the executor service to be used during the parsing
         * activity
         * @return itself
         */
        public Builder executorService(ExecutorService service) {
            this.executorService = service;
            return this;
        }

        /**
         * Sets the archive selector that can selects which jar should be parsed
         * during the parsing activity. This is particularly useful when the
         * parser is configured to parse an entire directory of jars but only
         * needs to actually parse those jars depending on some environmental
         * condition or if the jar shows a particular stigma (like a jar entry
         * existence).
         *
         * @param selector the archive selector.
         * @return itself
         */
        public Builder archiveSelector(ArchiveSelector selector) {
            this.archiveSelector =  selector;
            return this;
        }

        /**
         * Sets the resource locator that can be used to load and parse extra
         * types that were referenced during the parsing but could not be parsed
         * due to their absence from the input archive set. The parser will call
         * the {@link ResourceLocator} to give a chance to the caller to selectively
         * add such unvisited types to the parsing activity
         *
         * @param locator a resource locator instance
         * @return itself
         */
        public Builder locator(ResourceLocator locator) {
            this.locator = locator;
            return null;
        }

        /**
         * Sets the parsing config that can be used to select which types should
         * be exhaustively visited (fields + methods visits) or not.
         *
         * @param config the config instance
         * @return itself
         */
        public Builder config(ParsingConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Build the final ParsingContext with the provided configuration.
         *
         * @return the @{link ParsingContext} instance
         */
        public ParsingContext build() {
            return new ParsingContext(this);
        }


    }

    final TypesCtr types = new TypesCtr();
    final ExecutorService executorService;
    final ArchiveSelector archiveSelector;
    final Logger logger;
    final ParsingConfig config;
    final ResourceLocator locator;

    private ParsingContext(Builder builder) {
//        Runtime runtime = Runtime.getRuntime();
        this.executorService = builder.executorService;
        this.archiveSelector = builder.archiveSelector;
        this.logger = builder.logger;
        this.locator = builder.locator;
        this.config = builder.config!=null?builder.config:new ParsingConfig() {
            final Set<String> emptyList = Collections.emptySet();
            @Override
            public Set<String> getAnnotationsOfInterest() {
                return emptyList;
            }

            @Override
            public Set<String> getTypesOfInterest() {
                return emptyList;
            }

            @Override
            public boolean modelUnAnnotatedMembers() {
                return false;
            }
        };
    }


    Map<URI, TypeBuilder> builders = new HashMap<URI, TypeBuilder>();

    public synchronized TypeBuilder getTypeBuilder(URI definingURI) {
        TypeBuilder builder = builders.get(definingURI);
        if (builder==null) {
            builder = new TypesImpl(types, definingURI);
            builders.put(definingURI, builder);
        }
        return builder;
    }

    /**
     * Return the holder instance of all the visited types. This should only
     * be called once the {@link org.glassfish.hk2.classmodel.reflect.Parser#awaitTermination()}
     * has returned.
     *
     * @return the visited types.
     */
    public Types getTypes() {
        return types;
    }

    public ResourceLocator getLocator() {
        return locator != null ? locator :
                CommonModelRegistry.getInstance().canLoadResources() ? CommonModelRegistry.getInstance() : null;
    }

    public ClassVisitor getClassVisitor(URI uri, String entryName) {
        return new ModelClassVisitor(this, uri, entryName, false);
    }

    public ClassVisitor getClassVisitor(URI uri, String entryName, boolean isApplicationClass) {
        return new ModelClassVisitor(this, uri, entryName, isApplicationClass);
    }

    public ParsingConfig getConfig() {
        return config;
    }
}
