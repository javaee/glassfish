package com.sun.enterprise.v3.rails;

import com.sun.enterprise.v3.deployment.GenericSniffer;
import com.sun.enterprise.module.impl.CookedModuleDefinition;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDependency;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.container.ContainerProvider;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Habitat;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.jar.Attributes;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * JRuby sniffer
 */
@Service(name="rails")
@Scoped(Singleton.class)
public class RailsSniffer extends GenericSniffer implements Sniffer {

    @Inject
    Habitat habitat;

    public RailsSniffer() {
        super("jruby", "app/controllers/application.rb", null);
    }
    

    final String[] deployers = { "com.sun.enterprise.rails.RailsDeployer" };

    /**
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     *
     * @param containerHome is where the container implementation resides
     * @param logger        the logger to use
     * @throws java.io.IOException exception if something goes sour
     */
    public void setup(String containerHome, Logger logger) throws IOException {
        super.setup(containerHome, logger);

        Inhabitant<? extends ContainerProvider> railsContainer = habitat.getInhabitant(ContainerProvider.class, getModuleType());
        Module glueModule = Module.find(railsContainer.type());
        if (glueModule!=null) {

            File rootLocation = new File(containerHome);
            if (!rootLocation.exists()) {
                throw new RuntimeException("JRuby installation not found at " + rootLocation.getPath());
            }

            rootLocation = new File(rootLocation, "lib");
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
                logger.log(Level.SEVERE, "Cannot setup jruby classpath", e);
                throw e;
            }

            glueModule.getRegistry().add(jruby);           //To change body of imp

            Module jrubyModule = glueModule.getRegistry().makeModuleFor(jruby.getName(), jruby.getVersion());
            // add jruby to this module's import so it is not garbage collected until this one is
            glueModule.addImport(jrubyModule);

            Module grizzlyRails = glueModule.getRegistry().makeModuleFor("org.glassfish.external:grizzly-jruby-module", null);
            if (grizzlyRails!=null) {
                grizzlyRails.addImport(new ModuleDependency("org.glassfish.external:grizzly-module", null));
                grizzlyRails.addImport(jrubyModule);
            }
        } else {
            logger.severe("Cannot find connector module for container " + getModuleType());
            throw new FileNotFoundException("Cannot find connector module for container " + getModuleType());
        }
    }

    public String[] getDeployersNames() {
        return deployers;
    }    
}
