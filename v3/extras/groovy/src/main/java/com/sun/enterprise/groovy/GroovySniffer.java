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
