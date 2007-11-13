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

package com.sun.enterprise.rails;

import com.sun.enterprise.module.ImportPolicy;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.impl.CookedModuleDefinition;
import com.sun.enterprise.v3.admin.Utils;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;


/**
 * Sets up the jruby modules so we can load classes successfully
 *
 * @author Jerome Dochez
 */
public class RailsImportPolicy implements ImportPolicy {

    public void prepare(Module module) {

        String railsHome = System.getProperty("jruby.home");
        if (railsHome==null) {
            return;
        }
        File rootLocation = new File(railsHome);
        if (!rootLocation.exists()) {
            throw new RuntimeException("JRuby installation not found at " + rootLocation.getPath());
        }

        rootLocation = new File(rootLocation, "lib");
        CookedModuleDefinition jvyaml = null;

        CookedModuleDefinition jruby = null;
        try {
            Attributes jrubyAttr = new Attributes();
            StringBuffer classpath= new StringBuffer();
            for (File lib : rootLocation.listFiles()) {
                if (lib.isFile()) {
                    if (lib.getName().equals("jruby.jar")) {
                        continue;
                    }

                    classpath.append(lib.getName());
                    classpath.append(" ");
                }
            }
            jrubyAttr.putValue(Attributes.Name.CLASS_PATH.toString(), classpath.toString());

            jruby = new CookedModuleDefinition(
                    new File(rootLocation, "jruby.jar"), jrubyAttr);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        module.getRegistry().add(jruby);           //To change body of imp

        Module jrubyModule = module.getRegistry().makeModuleFor(jruby.getName(), jruby.getVersion());
        // add jruby to this module's import so it is not garbage collected until this one is
        module.addImport(jrubyModule);

        Module grizzlyRails = module.getRegistry().makeModuleFor("org.glassfish.external:grizzly-jruby", null);
        grizzlyRails.addImport(new ModuleDependency("org.glassfish.external:grizzly-http", null));
        grizzlyRails.addImport(jrubyModule);
    }
}