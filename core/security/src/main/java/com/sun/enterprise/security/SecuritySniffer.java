package com.sun.enterprise.security;

import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.io.IOException;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.v3.deployment.GenericSniffer;

/**
 * SecuritySniffer for security related activities
 */
@Service
public class SecuritySniffer extends GenericSniffer {

    final String[] containers = { "com.sun.enterprise.security.SecurityContainer" };

    @Inject
    Habitat habitat;
    
    Inhabitant<SecurityLifecycle> lifecycle;

    public SecuritySniffer() {
        super("security", "WEB-INF/web.xml", null);
    }

    /**
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     * <p/>
     * This method returns a {@link com.sun.enterprise.module.ModuleDefinition} for the module containing
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
        lifecycle = habitat.getInhabitantByType(SecurityLifecycle.class);
        lifecycle.get();
        return null;
    }

    /**
     * Tears down a container, remove all imported libraries from the module
     * subsystem.
     */
    @Override
     public void tearDown() {
        if (lifecycle!=null) {
            lifecycle.release();
        }
    }

    /**
     * Returns the list of Containers that this Sniffer enables.
     * <p/>
     * The runtime will look up each container implementing
     * using the names provided in the habitat.
     *
     * @return list of container names known to the habitat for this sniffer
     */
    public String[] getContainersNames() {
        return containers;
    }
}
