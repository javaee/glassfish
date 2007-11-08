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
 * BundleReader.java        May 6, 2003, 3:33 PM
 *
 */

package com.sun.enterprise.tools.common.validation.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.sun.enterprise.tools.common.validation.Constants;


/**
 * BundleReader  is a Class  to read properties from the bundle.
 * <code>getValue()</code> method can be used to read the properties
 * from the bundle file(Bundle.properties). Default bundle file used
 * is <code>{ @link Constants }.BUNDLE_FILE</code>. Bundle file to use
 * can be set by using <code>setBundle()</code> method.
 *  
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 <b>NOT THREAD SAFE: mutable 'resourceBundle'</b>
 */
public class BundleReader {

    /**
     * A resource bundle of this reader.
     */
    private static ResourceBundle resourceBundle;

    
    /** Creates a new instance of BundleReader */
    public BundleReader() {
    }

    private static void IGNORE_EXCEPTION(final Exception e ) {
        // ignore
    }
    /**
     * Gets the value of the the given <code>key</code> from the bundle
     * 
     * @param key the key of which, the value needs to be fetched from
     * the bundle.
     */
    public static String getValue(String key) {
        if(resourceBundle == null)
            return key;
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException missingResourceException) {
            IGNORE_EXCEPTION(missingResourceException);
            return key;
        }
    }


    /**
     * sets the given bundle file as the file to use by this object.
     */
    public static void setBundle(String bundleFile){
        try {
            resourceBundle = ResourceBundle.getBundle(bundleFile);
        } catch (Exception ex) {
            IGNORE_EXCEPTION(ex);
        }
    }
    

    static {
        try {
            resourceBundle = ResourceBundle.getBundle(Constants.BUNDLE_FILE);
        } catch (Exception ex) {
            IGNORE_EXCEPTION(ex);
        }
    }
}
