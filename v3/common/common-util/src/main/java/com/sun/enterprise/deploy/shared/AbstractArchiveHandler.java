package com.sun.enterprise.deploy.shared;


import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import com.sun.enterprise.util.io.FileUtils;

/**
 * Common methods for ArchiveHandler implementations
 *
 * @author Jerome Dochez
 */
public class AbstractArchiveHandler {

    /**
     * Prepares the jar file to a format the ApplicationContainer is
     * expecting. This could be just a pure unzipping of the jar or
     * nothing at all.
     *
     * @param source of the expanding
     * @param target of the expanding
     */
    public void expand(ReadableArchive source, WritableArchive target) throws IOException {

        Enumeration<String> e = source.entries();
        while (e.hasMoreElements()) {
            String entryName = e.nextElement();
            InputStream is = new BufferedInputStream(source.getEntry(entryName));
            OutputStream os = null;
            try {
                os = target.putNextEntry(entryName);
                FileUtils.copy(is, os, source.getEntrySize(entryName));
            } finally {
                if (os!=null) {
                    target.closeEntry();
                }
                if (is!=null) {
                    is.close();
                }
            }
        }
    }
}
