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
package com.sun.enterprise.tools.apt;

import com.sun.enterprise.tools.InhabitantsDescriptor;
import com.sun.enterprise.tools.Messager;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;

import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.io.File;
import java.io.IOException;

/**
 * List of {@link InhabitantsDescriptor}s.
 * This data structure needs to survive multiple rounds.
 * 
 * @author Kohsuke Kawaguchi
 */
final class DescriptorList {
    /**
     * Habitat name to its descriptor.
     */
    final Map<String,InhabitantsDescriptor> descriptors = new HashMap<String, InhabitantsDescriptor>();

    /**
     * Really ugly, but because there's no easy way to make sure we call {@link #loadExisting(AnnotationProcessorEnvironment)}
     * once and only once, we use the flag to remember if we already loaded existing stuff.
     */
    private boolean loaded;

    protected void loadExisting(AnnotationProcessorEnvironment env) {
        if(loaded)  return;
        loaded=true;

        String outDirectory = env.getOptions().get("-d");
        if (outDirectory==null) {
            outDirectory = System.getProperty("user.dir");
        }
        File outDir = new File(new File(outDirectory),"META-INF/inhabitants").getAbsoluteFile();

        if (!outDir.exists()) {
            return;
        }
        for (File file : outDir.listFiles()) {
            if(file.isDirectory())  continue;

            try {
                descriptors.put(file.getName(),new InhabitantsDescriptor(file));
            } catch (IOException e) {
                env.getMessager().printError(e.getMessage());
            }
        }
    }

    public void write(final AnnotationProcessorEnvironment env) {
        String outDirectory = env.getOptions().get("-d");
        if (outDirectory==null) outDirectory = env.getOptions().get("-s");
        if(outDirectory==null)  outDirectory = System.getProperty("user.home");

        Messager messager = new Messager() {
          @Override
          public void printError(String msg, Exception e) {
            e.printStackTrace();
            env.getMessager().printError(msg);
          }
        };
        
        for (Entry<String, InhabitantsDescriptor> e : descriptors.entrySet()) {
            e.getValue().write(new File(outDirectory), messager, e.getKey());
        }
    }

    public InhabitantsDescriptor get(String name) {
        InhabitantsDescriptor descriptor = descriptors.get(name);
        if(descriptor==null) {
            descriptor = new InhabitantsDescriptor();
            descriptors.put(name,descriptor);
        }
        return descriptor;
    }
}
