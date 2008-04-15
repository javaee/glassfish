/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.groovy;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.impl.CookedModuleDefinition;
import com.sun.enterprise.v3.deployment.GenericSniffer;
import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Groovy sniffer
 */
@Service(name = "grails")
@Scoped(Singleton.class)
public class GroovySniffer extends GenericSniffer implements Sniffer {

    @Inject
    Habitat habitat;

    @Inject
    ModulesRegistry registry;

    public GroovySniffer() {
        super("grails", "grails-app", null);
    }

    final String[] containers = {"com.sun.enterprise.groovy.GroovyContainer"};

    /**
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     * <p/>
     * This method returns a {@link ModuleDefinition} for the module containing
     * the core implementation of the container. That means that this module
     * will be locked as long as there is at least one module loaded in the
     * associated container.
     *
     * @param containerHome is where the container implementation resides
     * @param logger        the logger to use 
     * @return the module definition of the core container implementation.
     * @throws java.io.IOException exception if something goes sour
     */
    @Override
    public Module[] setup(String containerHome, Logger logger) throws IOException {
        super.setup(containerHome, logger);

        System.out.println("ContainerHome: " + containerHome);
        File grailsRootLoc = new File(containerHome);
        if (!grailsRootLoc.exists()) {
            throw new RuntimeException("Grails installation not found at " + grailsRootLoc.getAbsolutePath());
        }

        grailsRootLoc = new File(grailsRootLoc, "lib");
        CookedModuleDefinition groovy = null;
        try {
            Attributes groovyAttr = new Attributes();
            StringBuffer classpath = new StringBuffer();
            String groovyJarName = null;
            for (File lib : grailsRootLoc.listFiles()) {
                if (lib.isFile()) {
                    if (lib.getName().startsWith("groovy-all")) {
                        System.out.println("Found groovy lib: " + lib.getAbsolutePath());
                        groovyJarName = lib.getName();
                        continue;
                    }
                    classpath.append(lib.getName());
                    classpath.append(" ");
                }
            }
            groovyAttr.putValue(Attributes.Name.CLASS_PATH.toString(), classpath.toString());
            groovy = new CookedModuleDefinition(
                    new File(grailsRootLoc, groovyJarName), groovyAttr);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot setup groovy classpath", e);
            throw e;
        }

        registry.add(groovy);
        Module groovyModule = registry.makeModuleFor(groovy.getName(), groovy.getVersion());
        
//        Module grizzlyGroovy = registry.makeModuleFor("org.glassfish.external:grizzly-groovy-module", null);
//        if (grizzlyGroovy != null) {
//            grizzlyGroovy.addImport(groovyModule);
//        }
        
        Module[] modules = { /*grizzlyGroovy, */groovyModule};
        return modules;
    }

    public String[] getContainersNames() {
        return containers;
    }
}
