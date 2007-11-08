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

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

// PENDING copied from ejbmodule data object

package com.sun.enterprise.tools.common.util;

import org.netbeans.modules.jarpackager.api.ArchiveEntry;
import com.sun.forte4j.j2ee.lib.dd.ejb2.gen.EjbJar;
import com.sun.forte4j.j2ee.lib.dataobject.J2eeDataObject;

public class ExtractUtils {
    
    /**
     * convert an icon urls into an icon references.
     *
     * @param url    a string representing the url of an icon.
     * @return The name of the ArchiveEntry in a form appropriate for
     *         putting in a deployment descriptor.
     */
    static String urlToReference(String url) {
        if ((url == null)        ||
            (url.equals("null")) ||             // NOI18N
            (url.length() == 0)    ) {
            return null;
        }
        // Make an ArchiveEntry.
        // This is a little bit inefficient as we throw this away
        // and it will be made again later.
        ArchiveEntry archiveEntry = J2eeDataObject.urlToArchiveEntry(url);
        return archiveEntry.getName();
    }

    /**
     * Copy the UserInfo .
     * Also convert icon urls into icon references.
     *
     * @param from   copy from here
     * @param to     copy to here
     */
    static public void copyUserInfo(EjbJar from, EjbJar to) {
        String url;
        String reference;

        url = from.getLargeIcon();
        reference = urlToReference(url);
        to.setLargeIcon(reference);

        url = from.getSmallIcon();
        reference = urlToReference(url);
        to.setSmallIcon(reference);
        
//        System.out.println("Copying " + from.getDescription()); //NOI18N

        to.setDescription(from.getDescription());
        to.setDisplayName(from.getDisplayName());
    }
}
