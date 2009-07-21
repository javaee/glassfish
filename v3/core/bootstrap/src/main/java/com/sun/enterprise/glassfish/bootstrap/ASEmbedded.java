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
package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.PlatformMain;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.hk2.component.InhabitantsParserDecorator;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ServiceLoader;
import java.io.File;
import java.io.IOException;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitants;
import org.kohsuke.MetaInfServices;
import com.sun.enterprise.module.single.SingleModulesRegistry;

/**
 * Main for embedded
 */
@MetaInfServices(PlatformMain.class)
public class ASEmbedded extends ASMainNonOSGi {

    Habitat habitat = null;
    
    Logger getLogger() {
        return logger;
    }

    protected String getPreferedCacheDir() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() {
        return "Embedded"; 
    }

    boolean createCache(File cacheDir) throws IOException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> T getStartedService(Class<T> serviceType) {
        if (habitat !=null)
            return habitat.getComponent(serviceType);
        else
            return null;
    }

    @Override
    public void run(final Logger logger, String... args) throws Exception {
        
        final StartupContext startupContext = getContext(StartupContext.class);

        final ModulesRegistry registry = new SingleModulesRegistry(ASEmbedded.class.getClassLoader());
        registry.setParentClassLoader(ASEmbedded.class.getClassLoader());

        Main main = new EmbeddedMain();
        try {
            main.launch(registry, startupContext);
        } catch (BootException e) {
            if (logger!=null) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            } else {
                Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
            }
        }              
    }

    final class EmbeddedMain extends Main {

        @Override
        protected Habitat createHabitat(ModulesRegistry registry, StartupContext context) throws BootException {
            Habitat habitat = registry.newHabitat();
            for (Object c : getContexts()) {
                habitat.add(Inhabitants.create(c));
            }
            // the root registry must be added as other components sometimes inject it
            habitat.add(new ExistingSingletonInhabitant<ModulesRegistry>(ModulesRegistry.class, registry));
            habitat.add(new ExistingSingletonInhabitant<Logger>(Logger.class, ASEmbedded.this.logger));
            registry.createHabitat("default", createInhabitantsParser(habitat));

            // post massaging, will need to be cleaned
            ASEmbedded.this.habitat = habitat;
            return habitat;
        }

        @Override
        protected InhabitantsParser createInhabitantsParser(Habitat habitat) {
            InhabitantsParser parser = super.createInhabitantsParser(habitat);


            ServiceLoader<InhabitantsParserDecorator> decorators =  ServiceLoader.load(InhabitantsParserDecorator.class, ASMain.class.getClassLoader());
            for (InhabitantsParserDecorator decorator : decorators) {
                if (decorator.getName().equalsIgnoreCase(getName()))
                    decorator.decorate(parser);
            }

            return parser;
        }
        
    }
    
}
