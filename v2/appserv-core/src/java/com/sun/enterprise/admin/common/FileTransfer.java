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

package com.sun.enterprise.admin.common;

import java.io.File;
import java.io.IOException;

/*
 * FileTransfer.java
 *
 */

/** Interface to upload & download a given file to the server.
 */
public interface FileTransfer {
     
    /** uploads the given file to the server running at the host:port with
     * authentication. Assumes jmx-remote with s1ashttp protocol.
     * @param filePath the absolute path to the file to be uploaded
     * @throws IOException for IO related exceptions
     * @return The full path of the uploaded file
     */    
    public String uploadFile(String filePath) throws IOException ;
           
   /**
     *  Exports the Client stub jar to the given location.
     *  @param appName The name of the application or module.
     *  @param destDir The directory into which the stub jar file 
     *  should be exported.
     *  @return Returns the absolute location to the exported jar file.
     * @throws IOException for IO related exceptions
     */
    public String downloadClientStubs(String  appName, String  destDir) throws IOException;

    /**
     * downloads file to the given location.
     * @param filePath the absolute path to the file to be downloaded
     *  @param destDir The directory into which the stub jar file 
     *  should be exported.
     *  @return Returns the absolute location to the exported jar file.
     * @throws IOException for IO related exceptions
     */
    public String downloadFile(String filePath, String destinationDirPath) throws IOException;

    /**
     * Downloads file to the given location. This method supports multiple
     * distributed clients.
     *
     * @param filePath the absolute path to the file to be downloaded
     * @param destPath local file path where file should be downloaded
     *
     * @return the absolute location to the downloaded jar file
     *
     * @throws IOException for IO related exceptions
     */
    public String mcDownloadFile(String filePath, File destPath) 
        throws IOException;

}
