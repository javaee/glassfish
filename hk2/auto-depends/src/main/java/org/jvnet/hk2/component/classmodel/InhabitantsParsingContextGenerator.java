/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.jvnet.hk2.component.classmodel;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.util.ParsingConfig;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import com.sun.hk2.component.InhabitantsScanner;

/**
 * Responsible for generating the collection of inhabitants, decoupling from
 * implementation detail for the caller.
 * <p/>
 * <p/>
 * The caller is expected to continually build up the InhabitantsGenerator
 * context by calling add*(), followed by calling getModelInhabitants() to
 * obtain the progenitors of the inhabitants.
 *
 * @author Jerome Dochez
 * @author Jeff Trent
 * @since 3.1
 */
public abstract class InhabitantsParsingContextGenerator {

    private final Logger logger = Logger
            .getLogger(InhabitantsParsingContextGenerator.class.getName());

    private final Parser parser;
    private final ParsingContext context;

    private final LinkedHashMap<String, InhabitantsScanner> metaInfScanners = new LinkedHashMap<String, InhabitantsScanner>();

    /**
     * Factory for the {@link InhabitantsParsingContextGenerator}
     *
     * @param h habitat not currently used; reserved for future use
     * @return an empty context InhabitantsGenerator
     */
    public static InhabitantsParsingContextGenerator create(Habitat h) {
        return new InhabitantsParsingContextGenerator() {};
    }

    protected InhabitantsParsingContextGenerator() {
        // setup the parser
        ParsingContext.Builder builder = new ParsingContext.Builder();
        final Set<String> annotations = new HashSet<String>();
        annotations.add(Contract.class.getCanonicalName());
        annotations.add(Service.class.getCanonicalName());
        // TODO: relocate "@Configured" declaration into core
        annotations.add("org.jvnet.hk2.config.Configured");

        builder.config(new ParsingConfig() {
            final Set<String> empty = Collections.emptySet();

            public Set<String> getInjectionTargetAnnotations() {
                return empty;
            }

            public Set<String> getInjectionTargetInterfaces() {
                return annotations;
            }

            public Set<String> getInjectionPointsAnnotations() {
                return empty;
            }
        });

        context = builder.build();
        parser = new Parser(context);
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
     *
     * @return the parsing context given the code sources provided
     */
    public ParsingContext getContext() {
        try {
            parser.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return context;
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
    public void parse(final File f) throws IOException {
        parseAlways(parser, f);
    }

    protected void parseAlways(Parser parser, final File f) throws IOException {
        parser.parse(f, new Runnable() {
            public void run() {
                logger.log(Level.FINER, "Finished introspecting {0}", f.getName());
            }
        });
    }

}
