package org.glassfish.api;

/**
 * Authorized manifest entries to hook up to the module management subsystem.
 * These are extensions to the OSGi specifications and therefore not portable.
 *
 * @Author Jerome Dochez
 */
public class ManifestConstants {

    /**
     * Hooks up a module class loader to all implementation of the comma separated
     * list of contracts.
     */
    public final static String GLASSFISH_REQUIRE_SERVICES = "GlassFish-require-services";
}
