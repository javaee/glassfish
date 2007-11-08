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

package com.sun.enterprise.admin.mbeans.custom;

import com.sun.enterprise.admin.mbeans.custom.loading.MBeanClassLoader;
import java.util.Map;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/** Implements an algorithm to select the ObjectName given the various parameters. See Design Document for details.
 */
public class ObjectNameSelectionAlgorithm {
    
    private ObjectNameSelectionAlgorithm() {
    }
    public static ObjectName select(final Map<String, String> params) throws RuntimeException {
        ObjectName on = null;
        try {
            if (params.containsKey(CustomMBeanConstants.OBJECT_NAME_KEY))
                on = new ObjectName(params.get(CustomMBeanConstants.OBJECT_NAME_KEY));
            else { 
                /* WBN 
                 * note that we always form an ON.  For a CMB that implements 
                 * MBeanRegistration -- it may use that name or it may create its
                 * own name.  If it creates its own name, we'll see it soon and then use
                 * that name in preference to this one...
                 */
                on = MBeanValidator.formDefaultObjectName(params);
            }
            return ( on );
        } catch(final MalformedObjectNameException me) {
            throw new RuntimeException(me);
        }
    }
    
    public static boolean implementsMBeanRegistrationInterface(String className) throws RuntimeException {
        boolean imri = false;
        try {
            //Note that the bits of the class need to be loaded dynamically and hence we need MBeanClassLoader
            ClassLoader mbcl        = new MBeanClassLoader();
            Class mbc               = Class.forName(className, false, mbcl);
            final Class[] iifs      = mbc.getInterfaces();
            for (Class c : iifs) {
                if (javax.management.MBeanRegistration.class.equals(c)) {
                    imri = true;
                    mbc = null; mbcl = null; // make them garbage-collectible  asap :), purpose served
                    break;
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException (e);
        }
        return ( imri );
    }
    
    private static boolean implementsMBeanRegistrationInterface(final Map<String, String> params) throws RuntimeException {
        return implementsMBeanRegistrationInterface(params.get(CustomMBeanConstants.IMPL_CLASS_NAME_KEY));
    }
}
