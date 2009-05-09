package com.sun.enterprise.v3.server;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.CompositeHandler;
import com.sun.enterprise.deploy.shared.AbstractReadableArchive;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.jar.Manifest;

/**
 * A composite archive is a readable archive that hides the sub archives.
 *
 * @author Jerome Dochez
 */
public class CompositeArchive extends AbstractReadableArchive {

    final ReadableArchive delegate;
    final CompositeHandler filter;

    public CompositeArchive(ReadableArchive delegate, CompositeHandler filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    public InputStream getEntry(String name) throws IOException {
        if (filter.accept(delegate, name)) {
            return delegate.getEntry(name);
        }
        return null;
    }

    public boolean exists(String name) throws IOException {
        if (filter.accept(delegate, name)) {
            return delegate.exists(name);                                    
        }
        return false;
    }

    public long getEntrySize(String name) {
        if (filter.accept(delegate, name)) {
            return delegate.getEntrySize(name);
        }
        return 0;
    }

    public void open(URI uri) throws IOException {
        delegate.open(uri);
    }

    public ReadableArchive getSubArchive(String name) throws IOException {
        if (filter.accept(delegate, name)) {
            return delegate.getSubArchive(name);
        }
        return null;
    }

    public boolean exists() {
        return delegate.exists();
    }

    public boolean delete() {
        return delegate.delete();
    }

    public boolean renameTo(String name) {
        return delegate.renameTo(name);
    }

    public void close() throws IOException {
        delegate.close();
    }

    public Enumeration<String> entries() {

        Enumeration<String> original = delegate.entries();
        Vector<String> results = new Vector<String>();
        while (original.hasMoreElements()) {
            String entryName = original.nextElement();
            if (filter.accept(delegate, entryName)) {
                results.add(entryName);
            }
        }
        return results.elements();
    }

    public Enumeration<String> entries(String prefix) {

        Enumeration<String> original = delegate.entries(prefix);
        Vector<String> results = new Vector<String>();
        while (original.hasMoreElements()) {
            String entryName = original.nextElement();
            if (filter.accept(delegate, entryName)) {
                results.add(entryName);
            }
        }
        return results.elements();
    }

    public boolean isDirectory(String name) {
        if (filter.accept(delegate, name)) {
            return delegate.isDirectory(name);
        }
        return false;
    }

    public Manifest getManifest() throws IOException {
        return delegate.getManifest();
    }

    public URI getURI() {
        return delegate.getURI();
    }

    public long getArchiveSize() throws SecurityException {
        return delegate.getArchiveSize();
    }

    public String getName() {
        return delegate.getName();
    }

    // we don't hide the top level directories as we need to use them
    // to figure out whether the EarSniffer can handle it in the 
    // case of optional application.xml
    public Collection<String> getDirectories() throws IOException {
        return delegate.getDirectories();
    }
}
