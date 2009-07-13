package org.glassfish.web.embed.impl;

import org.jvnet.hk2.annotations.Inject;
import org.glassfish.api.embedded.Server;
import org.glassfish.web.embed.WebBuilder;

import java.net.URL;
import java.io.IOException;

import com.sun.enterprise.deployment.archivist.WebArchivist;

/**
 * @author Jerome Dochez
 */
public class EmbeddedWebArchivist extends WebArchivist {

    @Inject
    WebBuilder builder;

    @Override
    protected URL getDefaultWebXML() throws IOException {
        if (builder.getDefaultWebXml()!=null) {
            return builder.getDefaultWebXml();
        }
        return super.getDefaultWebXML();
    }
}
