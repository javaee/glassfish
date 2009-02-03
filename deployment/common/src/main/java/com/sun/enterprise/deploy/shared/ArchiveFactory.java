/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.deploy.shared;

import com.sun.logging.LogDomains;
import org.glassfish.api.ContractProvider;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Singleton;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This implementation of the ArchiveFactory interface
 * is capable of creating the right abstraction of the Archive 
 * interface depending on the protocol used in the URL.
 *
 * @author Jerome Dochez
 */
@Service
@Scoped(Singleton.class)
public class ArchiveFactory implements ContractProvider {

    @Inject
    Habitat habitat;

    final static Logger logger = LogDomains.getLogger(DeploymentUtils.class, LogDomains.DPL_LOGGER);

    public WritableArchive createArchive(File path) throws java.io.IOException {
        try {
            /*
             *Use the expanded constructor so illegal characters (such as embedded blanks) in the path 
             *will be encoded.
             */
            return createArchive(prepareArchiveURI(path));
        } catch(java.net.URISyntaxException e) {
            return null;
        }
    }
    
    public ReadableArchive openArchive(File path) throws java.io.IOException {
        try {
            return openArchive(prepareArchiveURI(path));
        } catch(java.net.URISyntaxException e) {
            return null;
        }
    }
    
    /**
     * Creates a new archivist using the URL as the path. The URL 
     * protocol will define the type of desired archive (jar, file, etc)
     * @param path to the archive
     * @return the apropriate archive
     */
    public WritableArchive createArchive(URI path) throws IOException {
        
        String protocol = path.getScheme();
        try {
            WritableArchive archive = habitat.getComponent(WritableArchive.class, protocol);
            if (archive==null) {
                logger.log(Level.SEVERE, "Cannot find an archive implementation for " + protocol);
                throw new MalformedURLException("Protocol not supported : " + protocol);
            }

            archive.create(path);
            return archive;
        } catch (ComponentException e) {
            logger.log(Level.SEVERE, "Cannot find an archive implementation for " + protocol, e);
            throw new MalformedURLException("Protocol not supported : " + protocol);
        }
    }
    
    /**
     * Opens an existing archivist using the URL as the path. 
     * The URL protocol will defines the type of desired archive 
     * (jar, file, memory, etc...) 
     * @param path url to the existing archive
     * @return the appropriate archive 
     */
    public ReadableArchive openArchive(URI path) throws IOException {

        String protocol = path.getScheme();
        try {
            ReadableArchive archive = habitat.getComponent(ReadableArchive.class, protocol);
            if (archive==null) {
                logger.log(Level.SEVERE, "Cannot find an archive implementation for " + protocol);
                throw new MalformedURLException("Protocol not supported : " + protocol);                
            }
            archive.open(path);
            return archive;
        } catch (ComponentException e) {
            logger.log(Level.SEVERE, "Cannot find an archive implementation for " + protocol, e);
            throw new MalformedURLException("Protocol not supported : " + protocol);
        } 
    }
    
    /**
     *Create a URI for the jar specified by the path string.
     *<p>
     *The steps used here correctly encode "illegal" characters - such as embedded blanks - in 
     *the path string that otherwise would render the URI unusable.  The URI constructor that
     *accepts just the path string does not perform this encoding.
     *@param path string for the archive
     *@return URI with any necessary encoding of special characters
     */
    static java.net.URI prepareArchiveURI(File path) throws java.net.URISyntaxException, java.io.UnsupportedEncodingException, java.io.IOException {
       
        URI archiveURI = path.toURI();
        String scheme = (path.isDirectory() ? "file" : "jar");
        URI answer = new URI(scheme, null /* authority */, archiveURI.getPath(), null /* query */, null /* fragment */);
        return answer;
    }
}
