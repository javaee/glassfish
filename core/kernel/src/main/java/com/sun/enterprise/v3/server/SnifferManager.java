/*
 * Copyright (c) 2008, Kohsuke Kawaguchi. All Rights Reserved.
 */

package com.sun.enterprise.v3.server;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.container.Sniffer;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

import com.sun.enterprise.module.impl.ClassLoaderProxy;

/**
 * Provide convenience methods to deal with {@link Sniffer}s in the system.
 *
 * @author Kohsuke Kawaguchi
 */
@Service
public class SnifferManager {
    // I am not injecting the sniffers because this can rapidly become an expensive
    // operation especially when no application has been deployed.
    volatile Collection<Sniffer> sniffers;

    @Inject
    protected Habitat habitat;

    /**
     * Returns all the presently registered sniffers
     *
     * @return Collection (possibly empty but never null) of Sniffer
     */
    public Collection<Sniffer> getSniffers() {
        if (sniffers==null) {
            sniffers = habitat.getAllByContract(Sniffer.class);
        }
        return sniffers;
    }

    /**
     * Check if there's any {@link Sniffer} installed at all.
     */
    public final boolean hasNoSniffers() {
        return getSniffers().isEmpty();
    }

    public Sniffer getSniffer(String appType) {
        assert appType!=null;
        for (Sniffer sniffer :  getSniffers()) {
            if (appType.equalsIgnoreCase(sniffer.getModuleType())) {
                return sniffer;
            }
        }
        return null;
    }

    /**
     * Returns a collection of sniffers that recognized some parts of the
     * passed archive as components their container handle.
     *
     * If no sniffer recognize the passed archive, an empty collection is
     * returned.
     *
     * @param archive source archive abstraction
     * @param cloader is a class loader capable of loading classes and resources
     * from the passed archive.
     * @return possibly empty collection of sniffers that handle the passed
     * archive.
     */
    public Collection<Sniffer> getSniffers(ReadableArchive archive, ClassLoader cloader) {

        List<Sniffer> appSniffers = new ArrayList<Sniffer>();
        for (Sniffer sniffer : getSniffers()) {
            if (sniffer.handles(archive, cloader )) {
                appSniffers.add(sniffer);
            }
        }
        return appSniffers;
    }

    /**
     * Sets up a parent classloader that will be used to create a temporary application
     * class loader to load classes from the archive before the Deployers are available.
     * Sniffer.handles() method takes a class loader as a parameter and this class loader
     * needs to be able to load any class the sniffer load themselves.
     *
     * @param parent parent class loader for this class loader
     * @param sniffers sniffer instances
     * @return a class loader with visibility on all classes loadable by classloaders.
     */
    public ClassLoader createSnifferParentCL(ClassLoader parent, Collection<Sniffer> sniffers) {
        // Use the sniffers class loaders as the delegates to the parent class loader.
        // This will allow any class loadable by the sniffer (therefore visible to the sniffer
        // class loader) to be also loadable by the archive's class loader.
        ClassLoaderProxy cl = new ClassLoaderProxy(new URL[0], parent);
        for (Sniffer sniffer : sniffers) {
            cl.addDelegate(sniffer.getClass().getClassLoader());
        }
        return cl;

    }

    /**
     * Short for {@code createSnifferParentCL(parent,this.getSniffers())}
     */
    public final ClassLoader createSnifferParentCL(ClassLoader parent) {
        return createSnifferParentCL(parent,getSniffers());
    }

}
