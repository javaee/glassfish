
package org.glassfish.embed.impl;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.glassfish.api.deployment.archive.ReadableArchive;

/**
 * A <strong>lot</strong> of methods need to be written in order to implement
 * ReadableArchive.  The no-op methods are implemented here to make ScatteredWar
 * easier to understand.
 * @author Byron Nevins
 */
abstract public class ReadableArchiveAdapter implements ReadableArchive{

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

    public Enumeration<String> entries() {
        return null;
    }

    public Enumeration<String> entries(String prefix) {
        return null;
    }

    public Collection<String> getDirectories() throws IOException  {
        return null;
    }

    public boolean isDirectory (java.lang.String name) {
        return false;
    }

    public void setParentArchive(ReadableArchive parentArchive) {
    }

    public ReadableArchive getParentArchive() {
        return null;
    }
}
