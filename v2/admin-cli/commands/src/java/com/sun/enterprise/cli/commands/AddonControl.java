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

package com.sun.enterprise.cli.commands;

import java.io.File;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.lang.reflect.InvocationTargetException;
import com.sun.enterprise.util.SystemPropertyConstants;

/**
 *  This is a control class that invoke AddonFacade using
 *  reflection. 
 *
 *  @author binod@dev.java.net
 */
final class AddonControl {

    private final String PACKAGE = "com.sun.enterprise.cli.framework";
    private final String ADDONFACADE = "com.sun.enterprise.addons.AddonFacade";
    private File installRoot = null;
    private Logger logger = null;
    private Class addonFacade = null;

    AddonControl() throws Exception {
        String installDir =
        System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
        this.installRoot = new File(installDir);
        this.logger = Logger.getLogger(PACKAGE, null);
        this.addonFacade = Class.forName(ADDONFACADE);
    } 

    /**
     * Invoke the install method.
     */
    void install(File jarFile) throws Throwable {
        try {
            Class[] args = new Class[] {File.class, File.class, Logger.class};
            Method m = addonFacade.getMethod("install", args);
            m.invoke(null, new Object[] {installRoot, jarFile, logger});
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
    }

    /**
     * Invoke the uninstall method.
     */
    void uninstall(String addon) throws Throwable {
        try {
            Class[] args = new Class[] {File.class, String.class, Logger.class};
            Method m = addonFacade.getMethod("uninstall", args);
            m.invoke(null, new Object[] {installRoot, addon, logger});
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
    }

    /**
     * Invoke configureDAS method. 
     */
    void configureDAS(File domainRoot) throws Throwable {
        try {
            Class[] args = new Class[] {File.class, File.class, Logger.class};
            Method m = addonFacade.getMethod("configureDAS", args);
            m.invoke(null, new Object[] {installRoot, domainRoot, logger});
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
    }

    /**
     * Invoke unconfigureDAS method. 
     */
    void unconfigureDAS(File domainRoot) throws Throwable {
        try {
            Class[] args = new Class[] {File.class, File.class, Logger.class};
            Method m = addonFacade.getMethod("unconfigureDAS", args);
            m.invoke(null, new Object[] {installRoot, domainRoot, logger});
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
    }

}
    
