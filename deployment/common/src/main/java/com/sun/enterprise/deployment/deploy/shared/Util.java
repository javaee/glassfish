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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.deployment.deploy.shared;

import java.net.URI;

/**
 * Utility logic.
 * 
 */
public class Util {

   /**
    * Returns the name portion of the specified URI.  This is defined as the 
    * part of the URI's path after the final slash (if any).  If the URI ends
    * with a slash that final slash is ignored in finding the name.
    * 
    * @param uri the URI from which to extract the name
    * @return the name portion of the URI
    */
    public static String getURIName(URI uri) {
        String name = null;
        String path = uri.getSchemeSpecificPart();
        if (path != null) {
            /*
             * Strip the path up to and including the last slash, if there is one.
             * A directory URI may end in a slash, so be sure to remove it if it
             * is there.
             */
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            int startOfName = path.lastIndexOf('/') + 1; // correct whether a / appears or not
            name = path.substring(startOfName);
        }
        return name;
    }
}
