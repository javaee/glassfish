
package org.glassfish.embed;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.glassfish.api.deployment.archive.ReadableArchive;
import com.sun.enterprise.deploy.shared.AbstractReadableArchive;


/**
 * A <strong>lot</strong> of methods need to be written in order to implement
 * ReadableArchive.  The no-op methods are implemented here to make ScatteredWar
 * easier to understand.
 * @author bnevins
 */
abstract class ReadableArchiveAdapter extends AbstractReadableArchive{

    public long getEntrySize(String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void open(URI arg0) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ReadableArchive getSubArchive(String arg0) throws IOException {
        return null;
    }

    public boolean delete() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean renameTo(String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getArchiveSize() throws SecurityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean exists() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
