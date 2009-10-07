package org.glassfish.internal.deployment;

import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.api.deployment.DeploymentContext;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.annotations.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.util.jar.JarFile;

import com.sun.enterprise.util.io.FileUtils;

/**
 * Pretty generic implementation of some ArchiveHandler methods
 *
 * @author Jerome Dochez
 */
public abstract class GenericHandler implements ArchiveHandler {

    @Inject
    protected Habitat habitat;

    /**
     * Prepares the jar file to a format the ApplicationContainer is
     * expecting. This could be just a pure unzipping of the jar or
     * nothing at all.
     *
     * @param source of the expanding
     * @param target of the expanding
     * @param context deployment context
     * @throws IOException when the archive is corrupted
     */
    public void expand(ReadableArchive source, WritableArchive target,
        DeploymentContext context) throws IOException {

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
                is.close();
            }
        }

        // last is manifest is existing.
        Manifest m = source.getManifest();
        if (m!=null) {
            OutputStream os  = target.putNextEntry(JarFile.MANIFEST_NAME);
            m.write(os);
            target.closeEntry();
        }
    }

    /**
     * Returns the default application name usable for identifying the archive.
     * <p>
     * This default implementation returns the name portion of
     * the archive's URI.  The archive's name depends on the type of archive
     * (FileArchive vs. JarArchive vs. MemoryMappedArchive, for example).
     * <p>
     * A concrete subclass can override this method to provide an alternative
     * way of deriving the default application name.
     *
     * @param archive the archive for which the default name is needed
     * @param context deployment context
     * @return the default application name for the specified archive
     */
    public String getDefaultApplicationName(ReadableArchive archive, 
        DeploymentContext context) {
        // first try to get the name from ApplicationNameProvider if 
        // we can find an implementation of this service
        ApplicationNameProvider nameProvider = habitat.getComponent(ApplicationNameProvider.class);

        String appName = null;
        if (nameProvider != null) {
            appName = nameProvider.getNameFor(archive, context);
            if (appName != null) {
                return appName;
            }
        }

        // now try to get the default
        appName = archive.getName();
        int lastDot = appName.lastIndexOf('.');
        if (lastDot != -1) {
            if (appName.substring(lastDot).equalsIgnoreCase("." + getArchiveType())) {
                appName = appName.substring(0, lastDot);
            }
        }
        return appName;
    }

    public String getDefaultApplicationName(ReadableArchive archive) {
        return getDefaultApplicationName(archive, null);
    }

    /**
     * Returns the manifest file for this archive, this file is usually located at
     * the META-INF/MANIFEST location, however, certain archive type can change this
     * default location or use another mean of expressing manifest information.
     *
     * @param archive file
     * @return manifest instance or null if this archive has no manifest
     */
    public Manifest getManifest(ReadableArchive archive) throws IOException {
        return archive.getManifest();
    }
    
}
