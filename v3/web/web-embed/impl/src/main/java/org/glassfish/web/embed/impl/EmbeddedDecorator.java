package org.glassfish.web.embed.impl;

import com.sun.hk2.component.InhabitantsParserDecorator;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.enterprise.deployment.archivist.WebArchivist;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.api.embedded.Server;
import org.kohsuke.MetaInfServices;

/**
 * @author Jerome Dochez
 */
@MetaInfServices
public class EmbeddedDecorator implements InhabitantsParserDecorator {

    @Inject
    Server server;
    
    public String getName() {
        return "Embedded";
    }

    public void decorate(InhabitantsParser inhabitantsParser) {
        inhabitantsParser.replace(WebArchivist.class, EmbeddedWebArchivist.class);

    }
}
