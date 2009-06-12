package org.glassfish.kernel.embedded;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedFileSystem;

import java.net.URL;
import java.io.IOException;

import com.sun.enterprise.v3.server.GFDomainXml;
import com.sun.enterprise.v3.server.DomainXmlPersistence;

/**
 * Embedded domain.xml, can use externally pointed domain.xml
 */
public class EmbeddedDomainXml extends GFDomainXml {

    @Inject
    Server server;

    @Inject(optional=true)
    EmbeddedFileSystem fileSystem=null;

    @Override
    protected URL getDomainXml(ServerEnvironmentImpl env) throws IOException {
        if (server.fileSystem!=null) {
            return server.fileSystem.configFile.toURI().toURL();
        }
        return getClass().getClassLoader().getResource("org/glassfish/embed/domain.xml");
    }

    @Override
    protected void upgrade() {
        // for now, we don't upgrade in embedded mode...
    }

}
