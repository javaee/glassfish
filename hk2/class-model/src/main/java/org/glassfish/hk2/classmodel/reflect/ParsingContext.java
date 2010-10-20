/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *  Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License. You can obtain
 *  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *  Sun designates this particular file as subject to the "Classpath" exception
 *  as provided by Sun in the GPL Version 2 section of the License file that
 *  accompanied this code.  If applicable, add the following below the License
 *  Header, with the fields enclosed by brackets [] replaced by your own
 *  identifying information: "Portions Copyrighted [year]
 *  [name of copyright owner]"
 *
 *  Contributor(s):
 *
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */
package org.glassfish.hk2.classmodel.reflect;

import org.glassfish.hk2.classmodel.reflect.impl.ModelClassVisitor;
import org.glassfish.hk2.classmodel.reflect.impl.TypeBuilder;
import org.glassfish.hk2.classmodel.reflect.impl.TypesCtr;
import org.glassfish.hk2.classmodel.reflect.impl.TypesImpl;
import org.glassfish.hk2.classmodel.reflect.util.ParsingConfig;
import org.glassfish.hk2.classmodel.reflect.util.ResourceLocator;
import org.objectweb.asm.ClassVisitor;

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
        Logger logger=Logger.getLogger("org.glassfish.classmodel");
        ExecutorService executorService=null;
        ArchiveSelector archiveSelector=null;
        ParsingConfig config=null;
        ResourceLocator locator=null;

        public Logger logger() {
            return logger;
        }
        
        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public Builder executorService(ExecutorService service) {
            this.executorService = service;
            return this;
        }

        public Builder archiveSelector(ArchiveSelector selector) {
            this.archiveSelector =  selector;
            return this;
        }

        public Builder locator(ResourceLocator locator) {
            this.locator = locator;
            return null;
        }

        public Builder config(ParsingConfig config) {
            this.config = config;
            return this;
        }
        
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
        Runtime runtime = Runtime.getRuntime();
        int nrOfProcessors = runtime.availableProcessors();        
        this.executorService = builder.executorService;
        this.archiveSelector = builder.archiveSelector;
        this.logger = builder.logger;
        this.locator = builder.locator;
        this.config = builder.config!=null?builder.config:new ParsingConfig() {
            final Set<String> emptyList = Collections.emptySet();
            @Override
            public Set<String> getInjectionTargetAnnotations() {
                return emptyList;
            }

            @Override
            public Set<String> getInjectionTargetInterfaces() {
                return emptyList;
            }

            @Override
            public Set<String> getInjectionPointsAnnotations() {
                return emptyList;
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

    public Types getTypes() {
        return types;
    }

    public ResourceLocator getLocator() {
        return locator;
    }

    public ClassVisitor getClassVisitor(URI uri, String entryName) {
        return new ModelClassVisitor(this, uri, entryName);
    }

    public ParsingConfig getConfig() {
        return config;
    }
}
