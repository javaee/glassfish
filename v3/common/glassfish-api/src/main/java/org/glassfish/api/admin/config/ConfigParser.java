package org.glassfish.api.admin.config;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.component.Habitat;

import java.net.URL;
import java.io.IOException;

/**
 * @author Jerome Dochez
 */
@Contract
public interface ConfigParser {

    /**
     * Parse a Container configuration and add it the main configuration.
     *
     * @param habitat habitat were to
     * @param configuration
     */
    public Container parseContainerConfig(Habitat habitat, URL configuration) throws IOException;

}
