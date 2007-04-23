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

package com.sun.enterprise.module;

/**
 * Constants used in the module manifest. 
 * @author dochez
 */
public class ManifestConstants {
    // No instanciation allowed.
    private ManifestConstants() {
    }
    
    public static final String PKG_EXPORT_NAME = "Export-Package";
    public static final String BUNDLE_IMPORT_NAME = "Import-Bundles";
    public static final String BUNDLE_NAME = "Bundle-Name";
    public static final String CLASS_PATH = "Class-Path";
    public static final String CLASS_PATH_ID = "Class-Path-Id";
    public static final String IMPORT_POLICY = "Module-Import-Policy";
    public static final String LIFECYLE_POLICY = "Module-Lifecycle-Policy";
}