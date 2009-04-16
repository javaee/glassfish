/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.admin.payload;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.Payload.Part;

/**
 * Manages transferred files delivered via the request or response {@link Payload}.
 * <p>
 * Callers can process the entire payload
 * at once, treating each Part as a file, using the {@link #extractFiles}
 * method.  Or, the caller can invoke the {@link #extractFile}
 * method to work with a single Part as a file.
 * <p>
 * If the caller wants to extract the payload's content as temporary files it should
 * instantiate {@link Temp} which exposes a {@link PayLoadFilesManager.Temp#cleanup}
 * method.  The caller should invoke this method once it has finished with
 * the transferred files, although the finalizer will invoke cleanup just in case.
 * <p>
 * On the other hand, if the caller wants to keep the transferred files it
 * should instantiate {@link Perm}.
 * <p>
 * <code>Temp</code> uses a unique temporary directory, then creates one
 * temp file for each part it is asked to deal with, either from an entire payload
 * (extractFiles) or a single part (extractFile).  Recall that each part in the
 * payload has a name which is a relative or absolute URI.
 * 
 * @author tjquinn
 */
public abstract class PayloadFilesManager {

    private static final String XFER_DIR_PREFIX = "xfer-";
    public final static LocalStringManagerImpl strings = new LocalStringManagerImpl(PayloadFilesManager.class);

    private final File targetDir;
    protected final Logger logger;
    private final ActionReport report;

    private PayloadFilesManager(
            final File targetDir,
            final ActionReport report,
            final Logger logger) {
        this.targetDir = targetDir;
        this.report = report;
        this.logger = logger;
    }

    protected File getTargetDir() {
        return targetDir;
    }

    /**
     * Extracts files from a Payload and leaves them on disk.
     * <p>
     * The Perm manager constructs output file paths this way.  The URI from the
     * manager's targetDir (which the caller passes to the constructor) is the default
     * parent URI for the output file.
     * <p>
     * Next, the Part's properties are checked for a file-xfer-root property.
     * If found, it is used as a URI (either absolute or, if relative, resolved
     * against the targetDir).
     * <p>
     * Finally, the "output name" is either the
     * name from the Payload.Part for the {@link #extractFile(org.glassfish.api.admin.Payload.Part) }
     * method or the caller-provided argument in the {@link #extractFile(org.glassfish.api.admin.Payload.Part, java.lang.String) }
     * method.
     * <p>
     * In either case, the output name is used as a URI
     * string and is resolved against the targetDir combined with (if present) the
     * file-xfer-root property.
     * <p>
     * The net effect of this
     * is that if the output name is an absolute URI then it will override the
     * targetDir and the file-xfer-root setting.  If the output name is
     * relative then it will be resolved
     * against the targetDir plus file-xfer-root URI to derive the URI for the output file.
     */
    public static class Perm extends PayloadFilesManager {

        /**
         * Creates a new PayloadFilesManager for dealing with permanent files that
         * will be anchored at the specified target directory.
         * @param targetDir directory under which the payload's files should be stored
         * @param report result report to which extraction results will be appened
         * @param logger logger to receive messages
         */
        public Perm(final File targetDir, final ActionReport report, final Logger logger) {
            super(targetDir, report, logger);
        }

        /**
         * Creates a new PayloadFilesManager for permanent files anchored at
         * the caller's current directory.
         * @param report result report to which extraction results will be appended
         * @param logger logger to receive messages
         */
        public Perm(final ActionReport report, final Logger logger) {
            super(new File(System.getProperty("user.dir")), report, logger);
        }

        /**
         * Creates a new PayloadFilesManager for permanent files anchored at
         * the caller's current directory.
         * @param logger logger to receive messages
         */
        public Perm(final Logger logger) {
            this(null, logger);
        }

        /**
         * Creates a new PayloadFilesManager for permanent files anchored at
         * the caller's current directory.
         */
        public Perm() {
            this(null, Logger.getLogger(Perm.class.getName()));
        }

        @Override
        protected void postExtract(File extractedFile) {
            // no-op for permanent files
        }

        private URI getParentURI(Part part) {
            final Properties partProps = part.getProperties();
            String parentPathFromPart = partProps.getProperty("file-xfer-root");
            URI parentURI = getTargetDir().toURI();
            if (parentPathFromPart != null) {
                /*
                 * Add a trailing slash
                 */
                if ( ! parentPathFromPart.endsWith("/")) {
                    parentPathFromPart = parentPathFromPart + "/";
                }
                File parentFile = new File(parentPathFromPart);
                parentFile.mkdirs();
                parentURI = parentURI.resolve(parentFile.toURI());
            }
            return parentURI;
        }

        @Override
        protected URI getOutputFileURI(Part part, String name) throws IOException {
            /*
             * The part name might have path elements using / as the
             * separator, so figure out the full path for the resulting
             * file.
             */
            URI targetURI = getParentURI(part).resolve(name);
            return targetURI;
        }
    }

    /**
     * Extracts files from a payload, treating them as temporary files.
     * The caller should invoke {@link #cleanup} once it is finished with the
     * extracted files, although the finalizer will invoke cleanup if the
     * caller has not.
     */
    public static class Temp extends PayloadFilesManager {

        private boolean isCleanedUp = false;

        /** maps payload part name paths (excluding name and type) to temp file subdirs */
        private Map<String,File> pathToTempSubdir = new HashMap<String,File>();

        /**
         * Creates a new PayloadFilesManager for temporary files.
         * @param report results report to which extraction results will be appended
         * @param logger logger to receive messages
         * @throws java.io.IOException
         */
        public Temp(final ActionReport report, final Logger logger) throws IOException {
            super(createTempFolder(
                      new File(System.getProperty("java.io.tmpdir")),
                      logger),
                  report,
                  logger);
        }

        /**
         * Creates a new PayloadFilesManager for temporary files.
         * @param logger logger to receive messages
         * @throws java.io.IOException
         */
        public Temp(final Logger logger) throws IOException {
            this(null, logger);
        }

        /**
         * Deletes the temporary files created by this temp PayloadFilesManager.
         */
        public void cleanup() {
            if ( ! isCleanedUp) {
                FileUtils.whack(super.targetDir);
                isCleanedUp = true;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            cleanup();
        }

        @Override
        protected void postExtract(File extractedFile) {
            extractedFile.deleteOnExit();
        }

        @Override
        protected URI getOutputFileURI(Part part, String name) throws IOException {

            /*
             * See if our map already has an entry for this part's parent path.
             */
            String parentPath = getParentPath(name);
            File tempFile;
            if (parentPath != null) {
                URI parentURI = getTempSubDirForPath(parentPath);
                tempFile = new File(new File(parentURI), getNameAndType(name));
            } else {
                tempFile = new File(getTargetDir(), getNameAndType(name));
            }
            return tempFile.toURI();
        }

        private String getParentPath(final String partName) {
            int lastSlash = partName.lastIndexOf('/');
            if (lastSlash != -1) {
                return partName.substring(0, lastSlash);
            }
            return null;
        }

        URI getTempSubDirForPath(final String parentPath) throws IOException {
            File tempSubDir = pathToTempSubdir.get(parentPath);
            if (tempSubDir == null) {
                /*
                 * The extra dashes make sure the prefix meets createTempFile's reqts.
                 * Replace slashes (forward or backward) that are directory
                 * separators and replace colons (from Windows devices) with single
                 * dashes.  This technique generates unique but flat directory
                 * names so same-named files in different directories will
                 * go to different directories.
                 */
                String tempDirPrefix = parentPath.replaceAll("[/:\\\\]", "-") + "---";
                tempSubDir = createTempFolder(getTargetDir(), tempDirPrefix, super.logger);
                pathToTempSubdir.put(parentPath, tempSubDir);
            }
            return tempSubDir.toURI();
        }

        private String getNameAndType(final String path) {
            final int lastSlash = path.lastIndexOf('/');
            return path.substring(lastSlash + 1);
        }

    }

    protected abstract void postExtract(final File extractedFile);

    protected abstract URI getOutputFileURI(final Payload.Part part, final String name) throws IOException;

    /**
     * Extract a file from the Part, using the name stored in the part and
     * the file-xfer-root part property (if present) to derive
     * the relative or absolute URI to use for creating the extracted file.
     * @param part the Part containing the data to extract
     * @return the extracted File
     * @throws java.io.IOException
     */
    public File extractFile(final Payload.Part part) throws IOException {
        return extractFile(part, part.getName());
    }

    /**
     * Extracts the contents of the specified Part as a file, specifying
     * the relative or absolute URI to use for creating the extracted file.
     * If outputName is relative it is resolved against the manager's target
     * directory (which the caller passed to the constructor) and the
     * file-xfer-root Part property, if present.
     * @param part the Part containing the file's contents
     * @param outputName absolute or relative URI string to use for the extracted file
     * @return File for the extracted file
     * @throws java.io.IOException
     */
    public File extractFile(final Payload.Part part, final String outputName) throws IOException {
        OutputStream os = null;
        InputStream is = null;
        /*
         * Look in the Part's properties first for the URI of the target
         * directory for the file.  If there is none there then use the
         * target directory for this manager.
         */


        try {
            File extractedFile = new File(getOutputFileURI(part, outputName));

            /*
             * Create the required directory tree under the temp directory.
             * This makes sure that if the command uploaded files with the
             * same names but in different original directories that they
             * do not collide in the temp directory.
             */
            File immediateParent = extractedFile.getParentFile();
            immediateParent.mkdirs();
            if (extractedFile.exists()) {
                if (!extractedFile.delete()) {
                      logger.warning(strings.getLocalString(
                            "payload.overwrite",
                            "Overwriting previously-uploaded file because the attempt to delete it failed: {0}",
                            extractedFile.getAbsolutePath()));
                }
            }

            os = new BufferedOutputStream(new FileOutputStream(extractedFile));
            is = part.getInputStream();
            int bytesRead;
            byte[] buffer = new byte[1024 * 64];
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            final String lastModifiedString = part.getProperties().getProperty("last-modified");
            final long lastModified = (lastModifiedString != null ?
                Long.parseLong(lastModifiedString) :
                System.currentTimeMillis());

            extractedFile.setLastModified(lastModified);
            postExtract(extractedFile);
            logger.fine("Extracted transferred entry " + part.getName() + " to " +
                    extractedFile.getAbsolutePath());
            reportExtractionSuccess();
            return extractedFile;
        }
        catch (Exception e) {
            reportExtractionFailure(part.getName(), e);
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        } finally {
            if (os != null) {
                os.close();
                os = null;
            }
        }
    }

    /**
     * Returns all Files extracted from the Payload, treating each Part as a
     * separate file.
     * @param inboundPayload Payload containing file data to be extracted
     * @return the Files corresponding to the content of each extracted file
     * @throws java.io.IOException
     */
    public List<File> extractFiles(final Payload.Inbound inboundPayload) throws IOException {

        if (inboundPayload == null) {
            return Collections.EMPTY_LIST;
        }

        ArrayList<File> result = new ArrayList<File>();
        OutputStream os = null;
        InputStream is = null;

        try {
            StringBuilder uploadedEntryNames = new StringBuilder();
            for (Iterator<Payload.Part> partIt = inboundPayload.parts(); partIt.hasNext();) {
                Payload.Part part = partIt.next();
                result.add(extractFile(part));
                uploadedEntryNames.append(part.getName()).append(" ");
            }
            return result;
        } catch (Exception e) {
            final IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
        finally {
            if (is != null) {
                is.close();
                is = null;
            }
        }
    }

    private void reportExtractionSuccess() {
        if (report != null) {
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        }
    }

    private void reportExtractionFailure(final String partName, final Exception e) {
        if (report != null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(strings.getLocalString(
                    "payload.errExtracting",
                    "Error extracting tranferred file {0}",
                    partName));
            report.setFailureCause(e);
        }
    }

    /**
     * Creates a unique temporary directory within the specified parent.
     * @param parent directory within which to create the temp dir; will be created if absent
     * @return the temporary folder
     * @throws java.io.IOException
     */
    private static File createTempFolder(final File parent, final String prefix, final Logger logger) throws IOException {
        File result = File.createTempFile(prefix, "", parent);
        try {
            if ( ! result.delete()) {
                throw new IOException(
                        strings.getLocalString(
                            "payload.command.errorDeletingTempFile",
                            "Unknown error deleting temporary file {0}",
                            result.getAbsolutePath()));
            }
            if ( ! result.mkdir()) {
                throw new IOException(
                        strings.getLocalString(
                            "payload.command.errorCreatingDir",
                            "Unknown error creating directory {0}",
                            result.getAbsolutePath()));
            }
            logger.fine("Created temporary upload folder " + result.getAbsolutePath());
            return result;
        } catch (Exception e) {
            IOException ioe = new IOException(strings.getLocalString(
                    "payload.command.errorCreatingXferFolder",
                    "Error creating temporary file transfer folder"));
            ioe.initCause(e);
            throw ioe;
        }
    }

    private static File createTempFolder(final File parent, final Logger logger) throws IOException {
        return createTempFolder(parent, XFER_DIR_PREFIX, logger);
    }
}

