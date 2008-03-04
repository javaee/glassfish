package com.sun.enterprise.rails;

import com.sun.enterprise.v3.deployment.GenericSniffer;
import com.sun.enterprise.module.impl.CookedModuleDefinition;
import com.sun.enterprise.module.impl.ModuleImpl;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.container.Container;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Habitat;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.jar.Attributes;
import java.util.Collection;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * JRuby sniffer
 */
@Service(name = "rails")
@Scoped(Singleton.class)
public class RailsSniffer extends GenericSniffer implements Sniffer {

    @Inject
    Habitat habitat;

    @Inject
    ModulesRegistry registry;

    public RailsSniffer() {
        super("jruby", "app/controllers/application.rb", null);
    }


    final String[] containers = {"com.sun.enterprise.rails.RailsContainer"};

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
    public Module[] setup(String containerHome, Logger logger) throws IOException {
        super.setup(containerHome, logger);



        File rootLocation = new File(containerHome);
        if (!rootLocation.exists()) {
            throw new RuntimeException("JRuby installation not found at " + rootLocation.getPath());
        }

        rootLocation = new File(rootLocation, "lib");
        CookedModuleDefinition jruby = null;
        try {
            Attributes jrubyAttr = new Attributes();
            StringBuffer classpath = new StringBuffer();
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

        registry.add(jruby);
        Module jrubyModule = registry.makeModuleFor(jruby.getName(), jruby.getVersion());

        Module grizzlyRails = registry.makeModuleFor("org.glassfish.external:grizzly-jruby-module", null);
        if (grizzlyRails != null) {
            grizzlyRails.addImport(jrubyModule);
        }
        Module[] modules = { grizzlyRails, jrubyModule };
        return modules;
    }

    public String[] getContainersNames() {
        return containers;
    }
}
