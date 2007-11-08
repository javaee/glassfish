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

package com.sun.enterprise.appclient.jws;

import java.io.File;
import java.net.URI;

/**
 *Represents content to be served back to Java Web Start on users' systems.
 *
 * @author tjquinn
 */
  
public class AppserverStaticContent extends StaticContent {

    /** records whether this particular static content instance represents a signed jar or not */
    private boolean isSigned = false;
    
    /**
     *Returns a new instance of the AppserverStaticContent class.
     *@param origin the owning ContentOrigin
     *@param contentKey content key for storing and retrieving the content
     *@param path path within the content's subcategory of this content
     *@param file File object for the physical file corresponding to this static content
     *@param installRootURI the app server's installation root URI
     *@param isMainJarFile whether this appserver static content should be flagged as the main jar in a JNLP document
     *@return new StaticContent object
     */
    public AppserverStaticContent(ContentOrigin origin, String contentKey, String path, File file, URI installRootURI, boolean isMainJarFile) {
        super(origin, contentKey, path, file, installRootURI, isMainJarFile);
    }

    /**
     *Returns a new instance of the AppserverStaticContent class that is not a main jar.
     *@param origin the owning ContentOrigin for the new content
     *@param contentKey content key for storing and retrieving the content
     *@param path path within the content's subcategory of this content
     *@param file File object for the physical file corresponding to this static content
     *@param installRootURI installation URI for the app server
     *@return new StaticContent object
     */
    public AppserverStaticContent(ContentOrigin origin, String contentKey, String path, File file, URI installRootURI) {
        this(origin, contentKey, path, file, installRootURI, false);
    }

    /**
     *Returns a new instance of the AppserverStaticContent class that is not a main jar.
     *@param origin owning ContentOrigin of the new content
     *@param content key prefix for storing and retrieving the content
     *@param path within the content's subcategory of this content
     *@param file object for the physical file corresponding to this static content
     *@param the app server's installation root URI
     *@param whether the content is a jar that contains the main class we want Java Web Start to run
     *@param whether the content is a signed jar or not
     *@return new StaticContent object
     */
    public AppserverStaticContent(ContentOrigin origin, String contentKey, String path, File file, URI installRootURI, boolean isMainJarFile, boolean isSigned) {
        this(origin, contentKey, path, file, installRootURI, isMainJarFile);
        this.isSigned = isSigned;
    }
    
    /**
     *Returns the path for this content.
     *<p>
     *For appserver static content, the content key and the path are the same.
     */
    public String getPath() {
        return getContentKey();
    }
}
