/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.xml.test.dynamic.overlay;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.glassfish.hk2.configuration.hub.api.Change;
import org.glassfish.hk2.configuration.hub.api.Change.ChangeCategory;
import org.glassfish.hk2.utilities.general.GeneralUtilities;

/**
 * This is diff'd against the change that was received to make
 * it easier to build up test cases
 * @author jwells
 *
 */
public final class ChangeDescriptor {
    private final ChangeCategory category;
    private final String typeName;
    private final List<String> instanceKey;
    private final String props[];
    private final String instance;
    private final String arName; // add-remove name also the old name
    
    public ChangeDescriptor(ChangeCategory category, String type, String instance, String arName, String... props) {
        this.category = category;
        this.typeName = type;
        this.props = props;
        this.instanceKey = tokenizeInstanceKey(instance);
        this.instance = instance;
        this.arName = arName;
    }
    
    private static List<String> tokenizeInstanceKey(String instance) {
        LinkedList<String> retVal = new LinkedList<String>();
        
        if (instance == null) return retVal;
        
        StringTokenizer st = new StringTokenizer(instance, ".");
        while (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            if (nextToken.startsWith("XMLServiceUID")) continue;
            
            retVal.add(nextToken);
        }
        
        return retVal;
    }
    
    private String checkInstanceKey(String recievedKey) {
        List<String> receivedToken = tokenizeInstanceKey(recievedKey);
        
        if (instanceKey.size() != receivedToken.size()) {
            return "Instance cardinality for " + recievedKey + " does not match " + instance;
        }
        
        for (int lcv = 0; lcv < receivedToken.size(); lcv++) {
            String expected = instanceKey.get(lcv);
            String received = receivedToken.get(lcv);
            
            if ("*".equals(expected)) continue;
            if (!GeneralUtilities.safeEquals(expected, received)) {
              return "Failed in " + this + " at index " + lcv;
            }
        }
        
        return null;
    }
    
    String check(Change change) {
        if (!GeneralUtilities.safeEquals(category, change.getChangeCategory())) {
            return "Category is not the same expected=" + this + " got=" + change;
        }
        
        if (!GeneralUtilities.safeEquals(typeName, change.getChangeType().getName())) {
            return "Type is not the same expected=" + this + " got=" + change;
        }
        
        String errorInstanceKey = checkInstanceKey(change.getInstanceKey());
        if (errorInstanceKey != null) return errorInstanceKey;
        
        List<PropertyChangeEvent> modifiedProperties = change.getModifiedProperties();
        if (modifiedProperties == null) {
            modifiedProperties = Collections.emptyList();
        }
        
        if (props.length != modifiedProperties.size()) {
            return "Expectect property length of " + props.length + " but got size " + modifiedProperties.size();
        }
        for (int lcv = 0; lcv < props.length; lcv++) {
            String prop = props[lcv];
            
            // Props is unordered, must go through list
            boolean found = false;
            for (int inner = 0; inner < modifiedProperties.size(); inner++) {
                if (GeneralUtilities.safeEquals(prop, modifiedProperties.get(inner).getPropertyName())) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
              return "Did not find prop " + prop + " in " + this;
            }
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return category + " type=" + typeName + " name=" + arName + " instanceKey=" + instanceKey;
    }
    
}