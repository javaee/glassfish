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

package com.sun.appserv.addons;

/**
 * AddonVersion object represents the version 
 * in terms of major, minor and patch level.
 * <p>
 * For ex.
 * am_components_installer_01_02_03.jar or am_components_configurator_01_02_03.jar
 * <p>
 * If no version is present in the name, it'll be assumed that 
 * the major is 00, minor is 00 and patch level is 00.
 * <p>
 * This class provides methods to get the components of version 
 * major, minor and patch level as int and the full version as string
 * ex. 01_02_03
 *
 * @author Sreenivas Munnangi
 * @since 9.1
 */
public interface AddonVersion {

    /**
     * Get the full version as string, for example.
     * If the addon is named as am_components_installer_01_02_03.jar
     * then the version output will be "01_02_03" in String format.
     * @return String version
     */
    public String getVersion() throws AddonException;

    /**
     * Get the majr version, for example.
     * If the addon is named as am_components_installer_01_02_03.jar
     * then the value of 1 will be returned.
     * @return int major
     */
    public int getMajor() throws AddonException;

    /**
     * Get the minor version, for example.
     * If the addon is named as am_components_installer_01_02_03.jar
     * then the value of 2 will be returned.
     * @return int minor
     */
    public int getMinor() throws AddonException;

    /**
     * Get the patch version, for example.
     * If the addon is named as am_components_installer_01_02_03.jar
     * then the value of 3 will be returned.
     * @return int patch
     */
    public int getPatch() throws AddonException;

}
