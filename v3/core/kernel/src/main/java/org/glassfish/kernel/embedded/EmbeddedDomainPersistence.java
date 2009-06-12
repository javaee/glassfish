package org.glassfish.kernel.embedded;

import com.sun.enterprise.v3.server.DomainXmlPersistence;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * @author Jerome Dochez
 */
public class EmbeddedDomainPersistence extends DomainXmlPersistence {
    
    // so far we never persist...
    @Override
    protected File getDestination() throws IOException {
        return null;
    }

    @Override
    protected OutputStream getOutputStream(File destination) throws IOException {
        Logger.getAnonymousLogger().info("Domain xml saved at " + destination.getAbsolutePath());
        return super.getOutputStream(destination);
    }
}
