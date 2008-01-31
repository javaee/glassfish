package com.sun.enterprise.deploy.shared;


import java.io.File;
import java.net.URI;
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
    
    /**
     * Returns the default application name usable for identifying the archive.
     * <p>
     * The default application name is the name portion (without the file type) of 
     * the archive's URI.  A concrete subclass should override this method if it
     * needs to provide an alternative way of deriving the default 
     * application name.
     * 
     * @param archive the archive for which the default name is needed
     * @return the default application name for the specified archive
     */
    public String getDefaultApplicationName(ReadableArchive archive) {
        String name = null;
        URI uri = archive.getURI();
        String path = uri.getPath();
        if (path != null) {
            /*
             * Strip the path up to and including the last slash, if there is one.
             * Then the name is the part of that stripped path up to the last dot.
             */
            name = path.substring(path.lastIndexOf('/') + 1);
            if (name.lastIndexOf(".")!=-1) {
                name = name.substring(0, name.lastIndexOf("."));
            }
        }
        return name;
    }
}
