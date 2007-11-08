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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Vector;

/**
 *Records mappings of paths to content that stem from app clients nested
 *within a Java EE application or jars in the Java EE app itself.

 * @author tjquinn
 */
public class ApplicationContentOrigin extends UserContentOrigin {
    
    /** this application origin's child app client origins */
    private Vector<AppclientContentOrigin> appclientOrigins;
    
    /**
     *Returns a new instance, representing a single application that is
     *an origin of served content.
     *@param the Application descriptor object for the app of interest
     */
    public ApplicationContentOrigin(Application application) {
        super(application);
        appclientOrigins = new Vector<AppclientContentOrigin>();
    }
    
    /**
     *Adds an origin representing an embedded app client to the application's
     *origin.
     *@param the app client content origin of the embedded app client
     */
    public void addNestedOrigin(AppclientContentOrigin origin) {
        appclientOrigins.add(origin);
    }
    
    /**
     *Removes the specified child origin and reports if it was present
     *@param the app client content origin to be removed
     *@return boolean indicating if the sub-origin was present
     */
    public boolean removeNestedOrigin(AppclientContentOrigin origin) {
        return appclientOrigins.remove(origin);
    }
    
    /**
     *Returns the embedded app client origins.
     *@return the sub-origins
     */
    public Vector<AppclientContentOrigin> getAppclientOrigins() {
        return appclientOrigins;
    }

    /**
     *Reports the prefix for the content key for all content related to this origin.
     *<p>
     *The content key is used as the key when content is stored in any of the maps
     *@return the content key prefix for any content from this origin
     */
    public String getContentKeyPrefix() {
        return NamingConventions.TopLevelApplication.contentKeyPrefix(this);
    }
    /**
     *Returns the path, within the virtual namespace provided by the JWS system
     *servlet, where the app client jar file for this application resides.
     *@return the path to the client jar file
     */
    public String getAppclientJarPath() {
        return NamingConventions.TopLevelApplication.appclientJarPath(this);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        for (AppclientContentOrigin child : appclientOrigins) {
            sb.append(lineSep).append("    ").append(child.toString());
        }
        return sb.toString();
    }
    
    /**
     *Returns the context root for this origin, as specified by the developer
     *of the app client or (if missing) as defaulted by us during loading.
     *@return the context root under which the origin's documents are addressable
     */
    public String getContextRoot() {
        return NamingConventions.TopLevelApplication.contextRoot(application);
    }

    /**
     *Returns whether this content origin's appclient is enabled for Java
     *Web Start access.
     *@return boolean indicating whether the application is enabled for JWS access
     */
    public boolean isEnabled() {
        try {
            return AppclientJWSSupportInfo.getInstance().isEnabled(this);
        } catch (IOException ioe) {
            /*
             *An IOException can occur if some infrastructure objects cannot be
             *located in obtaining the instance.  Very unlikely and also logged elsewhere.
             */
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }   
}
