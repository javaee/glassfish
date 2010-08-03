package com.sun.enterprise.v3.server;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.classmodel.reflect.ArchiveAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jul 28, 2010
 * Time: 6:03:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReadableArchiveScannerAdapter implements ArchiveAdapter {
    final ReadableArchive archive;

    public ReadableArchiveScannerAdapter(ReadableArchive archive) {
        this.archive = archive;
    }

    @Override
    public URI getURI() {
        return archive.getURI();
    }

    @Override
    public Manifest getManifest() throws IOException {
        return archive.getManifest();
    }

    @Override
    public void onEachEntry(EntryTask entryTask) throws IOException {

        Enumeration<String> entries = archive.entries();
        while (entries.hasMoreElements()) {
            String name = entries.nextElement();
            if (name.endsWith(".class")) {
                InputStream is = null;
                try {
                    is = archive.getEntry(name);
                    Entry entry = new Entry(name, archive.getEntrySize(name), false);
                    entryTask.on(entry, is);
                } finally {
                    if (is!=null)
                        is.close();
                }
            }
            // ok for now, I am assuming that all wars/rars have been exploded...
            if (name.endsWith(".jar")) {

                // we need to check that there is no exploded directory by this name.
                String explodedName = name.replaceAll("[/ ]", "__").replace(".jar", "_jar");
                if (!archive.exists(explodedName)) {

                    ReadableArchive subArchive = null;
                    try {
                        subArchive = archive.getSubArchive(name);
                        if (subArchive!=null) {
                            ReadableArchiveScannerAdapter adapter = new ReadableArchiveScannerAdapter(subArchive);
                            adapter.onEachEntry(entryTask);
                        }
                    } finally {
                        if (subArchive!=null) {
                            subArchive.close();
                        }
                    }
                }
            }  
        }
    }

    @Override
    public void close() throws IOException {
    }
}
