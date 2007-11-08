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

package com.sun.enterprise.addons;

import com.sun.appserv.addons.AddonVersion;
import com.sun.appserv.addons.AddonException;

/**
 * Parse the given addon name into majr, minor and patch levels
 * and return those component versions to the callers.
 *
 * @since 9.1
 * @authod sreenivas.munnangi@sun.com
 */
public class AddonVersionImpl implements AddonVersion {

    private String addonName = null;
    private String namePart = null;
    private String version = null;
    private int major = 0;
    private int minor = 0;
    private int patch = 0;

    private final String DEFAULT_VERSION = "00_00_00";
    private final String VERSION_PATTERN = ".*_[0-9][0-9]_[0-9][0-9]_[0-9][0-9]";
    private final String VERSION_SEPARATOR = "_";

    /**
     * Constructor
     */
    AddonVersionImpl(String addonName) throws AddonException {
        this.addonName = addonName;
        if (addonName.matches(VERSION_PATTERN)) {
            version = addonName.substring((addonName.length() - DEFAULT_VERSION.length()));
            namePart = addonName.substring(0, (addonName.length() - (DEFAULT_VERSION.length() + 1)));
        } else {
            version = DEFAULT_VERSION;
            namePart = addonName;
        }
        parseVersion(version);
    }

    private void parseVersion(String versionPart) throws AddonException {
        String [] strArr = versionPart.split(VERSION_SEPARATOR);
        setMajor(strArr[0]);
        setMinor(strArr[1]);
        setPatch(strArr[2]);
    }

    /**
     * Get the full version as string, for example.
     * If the addon is named as am_components_installer_01_02_03.jar
     * then the version output will be "01_02_03" in String format.
     * @return String version
     */
    public String getVersion() throws AddonException {
        return version;
    }

    /**
     * Get the majr version, for example.
     * If the addon is named as am_components_installer_01_02_03.jar
     * then the value of 1 will be returned.
     * @return int major
     */
    public int getMajor() throws AddonException {
        return this.major;
    }

    private void setMajor(String major) throws AddonException {
        try {
            this.major = (Integer.valueOf(major)).intValue();
        } catch (Exception e) {
            throw new AddonException(e);
        }
    }

    /**
     * Get the minor version, for example.
     * If the addon is named as am_components_installer_01_02_03.jar
     * then the value of 2 will be returned.
     * @return int minor
     */
    public int getMinor() throws AddonException {
        return this.minor;
    }

    private void setMinor(String minor) throws AddonException {
        try {
            this.minor = (Integer.valueOf(minor)).intValue();
        } catch (Exception e) {
            throw new AddonException(e);
        }
    }

    /**
     * Get the patch version, for example.
     * If the addon is named as am_components_installer_01_02_03.jar
     * then the value of 3 will be returned.
     * @return int patch
     */
    public int getPatch() throws AddonException {
        return this.patch;
    }

    private void setPatch(String patch) throws AddonException {
        try {
            this.patch = (Integer.valueOf(patch)).intValue();
        } catch (Exception e) {
            throw new AddonException(e);
        }
    }

    protected String getName() {
        return addonName;
    }

    protected String getNamePart() {
        return namePart;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("");
        try {
        sb.append("Version details for the AddOn " + addonName + ":"); 
        sb.append("version = " + getVersion() + ":");
        sb.append("major = " + getMajor() + ":");
        sb.append("minor = " + getMinor() + ":");
        sb.append("patch = " + getPatch());
        } catch (Exception e) {
        }
        return sb.toString();
    }

    protected boolean isHigher(AddonVersionImpl newVersion) 
        throws AddonException {

        if (newVersion.getMajor() > getMajor()) {
            return true;
        } else if (newVersion.getMajor() < getMajor()) {
            return false;
        }

        if (newVersion.getMinor() > getMinor()) {
            return true;
        } else if (newVersion.getMinor() < getMinor()) {
            return false;
        }

        if (newVersion.getPatch() > getPatch()) {
            return true;
        }

        return false;
    }

    protected boolean isLower(AddonVersionImpl newVersion) 
        throws AddonException {

        if (newVersion.getMajor() < getMajor()) {
            return true;
        } else if (newVersion.getMajor() > getMajor()) {
            return false;
        }

        if (newVersion.getMinor() < getMinor()) {
            return true;
        } else if (newVersion.getMinor() > getMinor()) {
            return false;
        }

        if (newVersion.getPatch() < getPatch()) {
            return true;
        }

        return false;
    }

}
