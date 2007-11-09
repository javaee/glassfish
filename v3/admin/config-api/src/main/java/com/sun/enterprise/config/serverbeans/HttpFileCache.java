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



package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.glassfish.api.admin.ConfigBean;

import java.beans.PropertyVetoException;
import java.io.Serializable;


/**
 *
 */

/* @XmlType(name = "") */
@Configured
public class HttpFileCache
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute

    protected String globallyEnabled;
    @Attribute

    protected String fileCachingEnabled;
    @Attribute

    protected String maxAgeInSeconds;
    @Attribute

    protected String mediumFileSizeLimitInBytes;
    @Attribute

    protected String mediumFileSpaceInBytes;
    @Attribute

    protected String smallFileSizeLimitInBytes;
    @Attribute

    protected String smallFileSpaceInBytes;
    @Attribute

    protected String fileTransmissionEnabled;
    @Attribute

    protected String maxFilesCount;
    @Attribute

    protected String hashInitSize;



    /**
     * Gets the value of the globallyEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getGloballyEnabled() {
        if (globallyEnabled == null) {
            return "true";
        } else {
            return globallyEnabled;
        }
    }

    /**
     * Sets the value of the globallyEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setGloballyEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("globallyEnabled", this.globallyEnabled, value);

        this.globallyEnabled = value;
    }

    /**
     * Gets the value of the fileCachingEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getFileCachingEnabled() {
        if (fileCachingEnabled == null) {
            return "on";
        } else {
            return fileCachingEnabled;
        }
    }

    /**
     * Sets the value of the fileCachingEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFileCachingEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("fileCachingEnabled", this.fileCachingEnabled, value);

        this.fileCachingEnabled = value;
    }

    /**
     * Gets the value of the maxAgeInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMaxAgeInSeconds() {
        if (maxAgeInSeconds == null) {
            return "30";
        } else {
            return maxAgeInSeconds;
        }
    }

    /**
     * Sets the value of the maxAgeInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxAgeInSeconds(String value) throws PropertyVetoException {
        support.fireVetoableChange("maxAgeInSeconds", this.maxAgeInSeconds, value);

        this.maxAgeInSeconds = value;
    }

    /**
     * Gets the value of the mediumFileSizeLimitInBytes property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMediumFileSizeLimitInBytes() {
        if (mediumFileSizeLimitInBytes == null) {
            return "537600";
        } else {
            return mediumFileSizeLimitInBytes;
        }
    }

    /**
     * Sets the value of the mediumFileSizeLimitInBytes property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMediumFileSizeLimitInBytes(String value) throws PropertyVetoException {
        support.fireVetoableChange("mediumFileSizeLimitInBytes", this.mediumFileSizeLimitInBytes, value);

        this.mediumFileSizeLimitInBytes = value;
    }

    /**
     * Gets the value of the mediumFileSpaceInBytes property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMediumFileSpaceInBytes() {
        if (mediumFileSpaceInBytes == null) {
            return "10485760";
        } else {
            return mediumFileSpaceInBytes;
        }
    }

    /**
     * Sets the value of the mediumFileSpaceInBytes property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMediumFileSpaceInBytes(String value) throws PropertyVetoException {
        support.fireVetoableChange("mediumFileSpaceInBytes", this.mediumFileSpaceInBytes, value);

        this.mediumFileSpaceInBytes = value;
    }

    /**
     * Gets the value of the smallFileSizeLimitInBytes property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSmallFileSizeLimitInBytes() {
        if (smallFileSizeLimitInBytes == null) {
            return "2048";
        } else {
            return smallFileSizeLimitInBytes;
        }
    }

    /**
     * Sets the value of the smallFileSizeLimitInBytes property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSmallFileSizeLimitInBytes(String value) throws PropertyVetoException {
        support.fireVetoableChange("smallFileSizeLimitInBytes", this.smallFileSizeLimitInBytes, value);

        this.smallFileSizeLimitInBytes = value;
    }

    /**
     * Gets the value of the smallFileSpaceInBytes property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSmallFileSpaceInBytes() {
        if (smallFileSpaceInBytes == null) {
            return "1048576";
        } else {
            return smallFileSpaceInBytes;
        }
    }

    /**
     * Sets the value of the smallFileSpaceInBytes property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSmallFileSpaceInBytes(String value) throws PropertyVetoException {
        support.fireVetoableChange("smallFileSpaceInBytes", this.smallFileSpaceInBytes, value);

        this.smallFileSpaceInBytes = value;
    }

    /**
     * Gets the value of the fileTransmissionEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getFileTransmissionEnabled() {
        if (fileTransmissionEnabled == null) {
            return "false";
        } else {
            return fileTransmissionEnabled;
        }
    }

    /**
     * Sets the value of the fileTransmissionEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFileTransmissionEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("fileTransmissionEnabled", this.fileTransmissionEnabled, value);

        this.fileTransmissionEnabled = value;
    }

    /**
     * Gets the value of the maxFilesCount property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getMaxFilesCount() {
        if (maxFilesCount == null) {
            return "1024";
        } else {
            return maxFilesCount;
        }
    }

    /**
     * Sets the value of the maxFilesCount property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxFilesCount(String value) throws PropertyVetoException {
        support.fireVetoableChange("maxFilesCount", this.maxFilesCount, value);

        this.maxFilesCount = value;
    }

    /**
     * Gets the value of the hashInitSize property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getHashInitSize() {
        if (hashInitSize == null) {
            return "0";
        } else {
            return hashInitSize;
        }
    }

    /**
     * Sets the value of the hashInitSize property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHashInitSize(String value) throws PropertyVetoException {
        support.fireVetoableChange("hashInitSize", this.hashInitSize, value);

        this.hashInitSize = value;
    }



}
