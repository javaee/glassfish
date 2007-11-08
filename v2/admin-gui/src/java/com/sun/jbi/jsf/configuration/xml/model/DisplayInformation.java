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
 * DisplayInformation.java
 */

package com.sun.jbi.jsf.configuration.xml.model;

import com.sun.jbi.jsf.util.JBILogger;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * @author Sun Microsystems
 * 
 */
public class DisplayInformation implements Serializable {

    String attributeName;

    String displayName;

    String displayDescription;

    boolean isPasswordField;

    String defaultValue;
    
    /**
     * Controls printing of diagnostic messages to the log
     */
    private static Logger sLog = JBILogger.getInstance();


    /**
     * 
     */
    public DisplayInformation() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param attributeName
     * @param displayName
     * @param displayDescription
     * @param isPasswordField
     * @param defaultValue
     */
    public DisplayInformation(String attributeName, String displayName,
            String displayDescription, boolean isPasswordField) {
        super();
        this.attributeName = attributeName;
        this.displayName = displayName;
        this.displayDescription = displayDescription;
        this.isPasswordField = isPasswordField;
    }

    /**
     * @return the attributeName
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @param attributeName
     *            the attributeName to set
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue
     *            the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the displayDescription
     */
    public String getDisplayDescription() {
        return displayDescription;
    }

    /**
     * @param displayDescription
     *            the displayDescription to set
     */
    public void setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the isPasswordField
     */
    public boolean isPasswordField() {
        return isPasswordField;
    }

    /**
     * @param isPasswordField
     *            the isPasswordField to set
     */
    public void setPasswordField(boolean isPasswordField) {
        this.isPasswordField = isPasswordField;
    }
    
    public void dump() {
        sLog.fine("////////////////////////////");
        sLog.fine("//   attributeName: "+attributeName);
        sLog.fine("//   displayName: "+displayName);
        sLog.fine("//   displayDescription: "+displayDescription);
        sLog.fine("//   isPasswordField: "+isPasswordField);
        sLog.fine("//   defaultValue: "+defaultValue);
        sLog.fine("////////////////////////////");
        sLog.fine("");
    }


}
