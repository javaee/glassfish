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
 * Configuration.java
 */

 package com.sun.jbi.jsf.configuration.xml.model;

 import com.sun.jbi.jsf.util.JBILogger;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @author Sun Microsystems
 * 
 */
public class Configuration implements Serializable {
 
    private static Logger sLog = JBILogger.getInstance();
   
    String name;

    Map<String, DisplayInformation> displayDetailsMap = new HashMap<String, DisplayInformation>();

    Map<String, DisplayInformation> labelDisplayDetailsMap = new HashMap<String, DisplayInformation>();

    /**
     * 
     */
    public Configuration() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String componentName) {
        this.name = componentName;
    }

    /**
     * @return the displayDetailsMap
     */
    public Map<String, DisplayInformation> getDisplayDetailsMap() {
        return displayDetailsMap;
    }

    public Map<String, DisplayInformation> getLabelDisplayDetailsMap() {
        return labelDisplayDetailsMap;
    }

    /**
     * @param displayDetailsMap
     *            the displayDetailsMap to set
     */
    public void setDisplayDetailsMap(
            Map<String, DisplayInformation> displayDetailsMap) {
        this.displayDetailsMap = displayDetailsMap;
        Set<Entry<String, DisplayInformation>> set = displayDetailsMap
                .entrySet();
        Entry<String, DisplayInformation> entry = null;
        for (Iterator<Entry<String, DisplayInformation>> iterator = set
                .iterator(); iterator.hasNext() == true;) {
            entry = iterator.next();
            if (entry != null) {
                String ket = entry.getKey();
                DisplayInformation info = entry.getValue();
                if (info != null) {
                    this.labelDisplayDetailsMap
                            .put(info.getDisplayName(), info);
                }
            }
        }
    }

    /**
     * @param displayDetailsMap
     *            the displayDetailsMap to set
     */
    public void setLabelDisplayDetailsMap(
            Map<String, DisplayInformation> labelDisplayDetailsMap) {
        this.labelDisplayDetailsMap = labelDisplayDetailsMap;
        Set<Entry<String, DisplayInformation>> set = labelDisplayDetailsMap
                .entrySet();
        Entry<String, DisplayInformation> entry = null;
        for (Iterator<Entry<String, DisplayInformation>> iterator = set
                .iterator(); iterator.hasNext() == true;) {
            entry = iterator.next();
            if (entry != null) {
                String ket = entry.getKey();
                DisplayInformation info = entry.getValue();
                if (info != null) {
                    this.displayDetailsMap.put(info.getAttributeName(), info);
                }
            }
        }
    }

    /**
     * 
     * @param attributeName
     * @param value
     * @return
     */
    public DisplayInformation addDisplayDetail(String attributeName,
            DisplayInformation value) {
        DisplayInformation returnValue = null;
        if ((attributeName != null) && (value != null)) {
            returnValue = this.displayDetailsMap.put(attributeName, value);
            this.labelDisplayDetailsMap.put(value.getDisplayName(), value);
        }
        return returnValue;
    }

    public void dump() {
        sLog.fine("name: " + name);
        Entry<String, DisplayInformation> entry = null;
        String key = null;
        DisplayInformation value = null;
        Set<Entry<String, DisplayInformation>> set = this.displayDetailsMap
                .entrySet();
        Iterator<Entry<String, DisplayInformation>> iterator = set.iterator();
        while (iterator.hasNext() == true) {
            entry = iterator.next();
            if (entry != null) {
                key = entry.getKey();
                value = entry.getValue();
                value.dump();
            }
        }
    }



}
