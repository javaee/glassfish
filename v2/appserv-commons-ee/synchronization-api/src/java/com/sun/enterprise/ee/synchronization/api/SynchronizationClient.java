/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.ee.synchronization.api;

import java.io.IOException;
import com.sun.enterprise.ee.synchronization.SynchronizationException;

/**
 * Utility to synchronize a file between server instance(s) and DAS. This 
 * allows synchornization of a file or jar. This API design is similar to 
 * FTP service client. It provides upload and download functionality.
 * This client has bi-directional (push and pull) capabilities to shuttle
 * bits and is intended for use in any end points in the system. In order
 * to push bits from DAS, this API should be used.
 *
 * <xmp>
 * Example: 
 * The following code connects to server instance named server1 and downloads
 * domain.xml from its ${com.sun.aas.instanceRoot}/config/domain.xml to 
 * ${com.sun.ass.instanceRoot}/config directory.
 *
 * Server name of any instance can be obtained from domain.xml 
 * (xpath is domain/servers/server@name).
 *
 *  try {
 *      SynchronizationClient sc = 
 *          SynchronizationFactory.createSynchronizationClient("server1");
 *      sc.connect();
 *      sc.get("config/domain.xml", new File("config"));
 *      sc.disconnect();
 *  } catch(SynchronizationException) {
 *      // synchronization failed - handle error
 *  }
 * </xmp>
 *
 * Please refer to more inline documentation for default values and more 
 * description of API methods.
 *
 * <p>Notes: To connect to DAS, please use server name of DAS.
 *
 * @author Nazrul Islam
 * @author Satish Viswanatham
 * @since  JDK1.4
 */
public interface SynchronizationClient
{

    /**
     * Gets the file from the target server into the current server's
     * file system. 
     *
     * @param  remoteFile   name of remote file to be downloaded. This
     *    file name is relative to instance root dir. It may also contain 
     *    ${com.sun.aas.instanceRoot} token as specified in domain.xml. 
     *    Only files under install root are allowed to be synchronized.
     *
     * @param  localFile    local file name, where downloaded file must
     *    be saved. If the file does not exist, it is created. This file
     *    path can be absolute or relative to instance root dir. It may
     *    also contain ${com.sun.aas.instanceRoot} token as specified 
     *    in domain.xml.  
     *
     * @throws SynchronizatioException   if synchronization fails
     */
    public void get(String remoteFile, String localFile)
            throws SynchronizationException;

    /**
     * Uploads the file to the remote server's file system. 
     *
     * @param  localFile  name of localFile to be uploaded. The file 
     *    path can be absolute or relative to the instance root directory.
     *
     * @param  remoteDir  either temp dir or any sub directory of
     *    instance root. If the argument is null, the file is uploaded
     *    to tmp directory. Only relative path (relative to instanceRoot) 
     *    is allowed.
     *
     * @return  upload location of the file relative to the remote instance root
     *
     * @throws SynchronizatioException   if synchronization fails 
     */
    public String put(String localFile, String remoteDir) 
             throws SynchronizationException;

    /**
     * Connects to the remote side. Uses the authentication information
     * passed in the constructor. If the client is running in DAS, the 
     * MBean server connection info (host, port, protocol) is obtained
     * from the domain configuration. If the client is running in a 
     * remote server instance, then DAS properties kept under node agent
     * configuration is used. If it is a server to server communication,
     * domain configuration available in that server is used.
     * 
     * @throws  IOException  if unable to connect
     */
    public void connect() throws java.io.IOException;

    /**
     * Closes the connection to the remote side.
     *
     * @throws  IOException  if an error while closing connection
     */
    public void disconnect() throws java.io.IOException;
}
