/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.internal;

import java.beans.PropertyChangeEvent;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;

/**
 * @author jwells
 *
 */
public class Differences {
    private List<Difference> differences = new LinkedList<Difference>();
    
    public void addDifference(Difference difference) {
        differences.add(difference);
    }
    
    public List<Difference> getDifferences() {
        return differences;
    }
    
    public static class Difference {
        private final BaseHK2JAXBBean source;
        private final List<PropertyChangeEvent> nonChildChanges = new LinkedList<PropertyChangeEvent>();
        
        /** From xmlName to the bean to add */
        private final Map<String, BaseHK2JAXBBean> adds = new LinkedHashMap<String, BaseHK2JAXBBean>();
        
        private final Map<String, RemoveData> removes = new LinkedHashMap<String, RemoveData>();
        
        public Difference(BaseHK2JAXBBean source) {
            this.source = source;
        }
        
        public BaseHK2JAXBBean getSource() {
            return source;
        }
        
        public void addNonChildChange(PropertyChangeEvent evt) {
            nonChildChanges.add(evt);
        }
        
        public List<PropertyChangeEvent> getNonChildChanges() {
            return nonChildChanges;
        }
        
        public void addAdd(String propName, BaseHK2JAXBBean toAdd) {
            adds.put(propName, toAdd);
        }
        
        public Map<String, BaseHK2JAXBBean> getAdds() {
            return adds;
        }
        
        public void addRemove(String propName, RemoveData removeData) {
            removes.put(propName, removeData);
        }
        
        public Map<String, RemoveData> getRemoves() {
            return removes;
        }
        
        public boolean isDirty() {
            return !nonChildChanges.isEmpty() || !adds.isEmpty() || !removes.isEmpty();
        }
        
        @Override
        public String toString() {
            return "Difference(" + source + "," + System.identityHashCode(this) + ")";
        }
    }
    
    public static class RemoveData {
        private final String childProperty;
        private final String childKey;
        private final int index;
        private final BaseHK2JAXBBean child;
        
        public RemoveData(String childProperty, BaseHK2JAXBBean child) {
            this(childProperty, null, -1, child);
        }
        
        public RemoveData(String childProperty, String childKey, BaseHK2JAXBBean child) {
            this(childProperty, childKey, -1, child);
        }
        
        public RemoveData(String childProperty, int index, BaseHK2JAXBBean child) {
            this(childProperty, null, index, child);
        }
        
        private RemoveData (String childProperty,
                String childKey,
                int index,
                BaseHK2JAXBBean child) {
            this.childProperty = childProperty;
            this.childKey = childKey;
            this.index = index;
            this.child = child;
        }
        
        /**
         * @return the childProperty
         */
        public String getChildProperty() {
            return childProperty;
        }
        /**
         * @return the childKey
         */
        public String getChildKey() {
            return childKey;
        }
        /**
         * @return the index
         */
        public int getIndex() {
            return index;
        }
        /**
         * @return the child
         */
        public BaseHK2JAXBBean getChild() {
            return child;
        }
        
    }

    /**
     * Prints very pretty version of modifications
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Differences(num=" + differences.size() + "\n");
        
        int lcv = 1;
        for (Difference d : differences) {
            BaseHK2JAXBBean source = d.getSource();
            
            String xmlPath = source._getXmlPath();
            String instanceName = source._getInstanceName();
            
            List<PropertyChangeEvent> events = d.getNonChildChanges();
            sb.append(lcv + ". Modified Bean sourcePath=" + xmlPath + " sourceInstance=" + instanceName + "\n");
            
            for (PropertyChangeEvent event : events) {
                sb.append("  CHANGED: " + event.getPropertyName() + " from " + event.getOldValue() + " to " + event.getNewValue() + "\n");
            }
            
            Map<String, BaseHK2JAXBBean> adds = d.getAdds();
            for (Map.Entry<String, BaseHK2JAXBBean> add : adds.entrySet()) {
                BaseHK2JAXBBean added = add.getValue();
                    
                String addedXmlPath = added._getXmlPath();
                String addedInstanceName = added._getInstanceName();
                    
                sb.append("  ADDED: addedPath=" + addedXmlPath + " addedInstanceName=" + addedInstanceName);
            }
            
            Map<String, RemoveData> removed = d.getRemoves();
            for (Map.Entry<String, RemoveData> remove : removed.entrySet()) {
                RemoveData removeMe = remove.getValue();
                    
                String removedXmlPath = removeMe.getChild()._getXmlPath();
                String removedInstanceName = removeMe.getChild()._getInstanceName();
                    
                sb.append("  REMOVED: removedPath=" + removedXmlPath + " removedInstanceName=" + removedInstanceName);
            }
            
            lcv++;
        }
        
        return sb.toString();
    }
}
