package org.jvnet.hk2.config;

import javax.xml.stream.Location;

/**
 * Immutable {@link Location} implementation.
 * @author Kohsuke Kawaguchi
 */
class LocationImpl implements Location {
    private final int lineNumber, columnNumber;
    private final String systemId;

    public LocationImpl(Location loc) {
        this.lineNumber = loc.getLineNumber();
        this.columnNumber = loc.getColumnNumber();
        this.systemId = loc.getSystemId();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public int getCharacterOffset() {
        return -1;
    }

    public String getPublicId() {
        return null;
    }

    public String getSystemId() {
        return systemId;
    }
}
