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
import java.util.ArrayList;
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
    private Map<DifferenceKey, Difference> differences = new LinkedHashMap<DifferenceKey, Difference>();
    
    public void addDifference(Difference difference) {
        DifferenceKey addedKey = new DifferenceKey(difference.getSource());
        
        Difference existingDifference = differences.get(addedKey);
        if (existingDifference == null) {
            differences.put(addedKey, difference);
        }
        else {
            existingDifference.merge(difference);
        }
    }
    
    public List<Difference> getDifferences() {
        return new ArrayList<Difference>(differences.values());
    }
    
    public int getDifferenceCost() {
        int retVal = 0;
        
        for (Difference d : differences.values()) {
            retVal += d.getSize();
        }
        
        return retVal;
    }
    
    public void merge(Differences diffs) {
        for (Difference diff : diffs.getDifferences()) {
          addDifference(diff);
        }
    }
    
    public static class Difference {
        private final BaseHK2JAXBBean source;
        private final List<PropertyChangeEvent> nonChildChanges = new LinkedList<PropertyChangeEvent>();
        
        /** From xmlName to the bean to add */
        private final Map<String, List<AddData>> adds = new LinkedHashMap<String, List<AddData>>();
        
        private final Map<String, List<RemoveData>> removes = new LinkedHashMap<String, List<RemoveData>>();
        
        private final Map<String, List<MoveData>> moves = new LinkedHashMap<String, List<MoveData>>();
        
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
        
        public void addAdd(String propName, AddData toAdd) {
            List<AddData> field = adds.get(propName);
            if (field == null) {
                field = new ArrayList<AddData>();
                
                adds.put(propName, field);
            }
            
            field.add(toAdd);
        }
        
        public void addAdd(String propName, BaseHK2JAXBBean toAdd) {
            addAdd(propName, new AddData(toAdd));
        }
        
        public void addMove(String propName, MoveData md) {
            List<MoveData> field = moves.get(propName);
            if (field == null) {
                field = new ArrayList<MoveData>();
                
                moves.put(propName, field);
            }
            
            field.add(md);
        }
        
        public Map<String, List<AddData>> getAdds() {
            return adds;
        }
        
        public void addRemove(String propName, RemoveData removeData) {
            List<RemoveData> field = removes.get(propName);
            if (field == null) {
                field = new ArrayList<RemoveData>();
                
                removes.put(propName, field);
            }
            
            field.add(removeData);
        }
        
        private void merge(Difference mergeMe) {
            for (PropertyChangeEvent pce : mergeMe.getNonChildChanges()) {
                addNonChildChange(pce);
            }
            
            Map<String, List<AddData>> adds = mergeMe.getAdds();
            for (Map.Entry<String, List<AddData>> entry : adds.entrySet()) {
                String propName = entry.getKey();
                List<AddData> addMes = entry.getValue();
                for (AddData addMe : addMes) {
                    addAdd(propName, addMe);
                }
            }
            
            Map<String, List<RemoveData>> removes = mergeMe.getRemoves();
            for (Map.Entry<String, List<RemoveData>> entry : removes.entrySet()) {
                String propName = entry.getKey();
                List<RemoveData> removeMes = entry.getValue();
                for (RemoveData removeMe : removeMes) {
                    addRemove(propName, removeMe);
                }
            }
            
            Map<String, List<MoveData>> moves = mergeMe.getMoves();
            for (Map.Entry<String, List<MoveData>> entry : moves.entrySet()) {
                String propName = entry.getKey();
                List<MoveData> moveMes = entry.getValue();
                for (MoveData moveMe : moveMes) {
                    addMove(propName, moveMe);
                }
            }
        }
        
        public Map<String, List<RemoveData>> getRemoves() {
            return removes;
        }
        
        public Map<String, List<MoveData>> getMoves() {
            return moves;
        }
        
        public boolean isDirty() {
            return !nonChildChanges.isEmpty() || !adds.isEmpty() || !removes.isEmpty() || !moves.isEmpty();
        }
        
        public int getSize() {
            int retVal = nonChildChanges.size();
            
            for (List<AddData> addMe : adds.values()) {
                for (AddData add : addMe) {
                    int addCost = Utilities.calculateAddCost(add.getToAdd());
                
                    retVal += addCost;
                }
            }
            
            for (List<RemoveData> rds : removes.values()) {
                for (RemoveData rd : rds) {
                    BaseHK2JAXBBean removeMe = rd.getChild();
                
                    int addCost = Utilities.calculateAddCost(removeMe);
                
                    retVal += addCost;
                }
            }
            
            for (List<MoveData> mds : moves.values()) {
                retVal += mds.size();
            }
            
            return retVal;
        }
        
        @Override
        public String toString() {
            return "Difference(" + source + "," + System.identityHashCode(this) + ")";
        }
    }
    
    public static class AddData {
        private final BaseHK2JAXBBean toAdd;
        private final int index;
        
        public AddData(BaseHK2JAXBBean toAdd, int index) {
            this.toAdd = toAdd;
            this.index = index;
        }
        
        private AddData(BaseHK2JAXBBean toAdd) {
            this(toAdd, -1);
        }
        
        public BaseHK2JAXBBean getToAdd() { return toAdd; }
        public int getIndex() { return index; }
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
    
    public static class MoveData {
        private final int oldIndex;
        private final int newIndex;
        
        public MoveData(int oldIndex, int newIndex) {
            this.oldIndex = oldIndex;
            this.newIndex = newIndex;
        }
        
        public int getOldIndex() { return oldIndex; }
        public int getNewIndex() { return newIndex; }
    }

    /**
     * Prints very pretty version of modifications
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Differences(num=" + differences.size() + ",cost=" + getDifferenceCost() + "\n");
        
        int lcv = 1;
        for (Difference d : differences.values()) {
            BaseHK2JAXBBean source = d.getSource();
            
            String xmlPath = source._getXmlPath();
            String instanceName = source._getInstanceName();
            
            List<PropertyChangeEvent> events = d.getNonChildChanges();
            sb.append(lcv + ". Modified Bean sourcePath=" + xmlPath + " sourceInstance=" + instanceName + "\n");
            
            for (PropertyChangeEvent event : events) {
                sb.append("  CHANGED: " + event.getPropertyName() + " from " + event.getOldValue() + " to " + event.getNewValue() + "\n");
            }
            
            Map<String, List<AddData>> addss = d.getAdds();
            for (Map.Entry<String, List<AddData>> adds : addss.entrySet()) {
                String propertyName = adds.getKey();
                
                for (AddData ad : adds.getValue()) {
                    BaseHK2JAXBBean added = ad.getToAdd();
                    int index = ad.getIndex();
                    
                    String addedXmlPath = added._getXmlPath();
                    String addedInstanceName = added._getInstanceName();
                    
                    sb.append("  ADDED: addedPath=" + addedXmlPath + " addedInstanceName=" + addedInstanceName + " addedIndex=" + index +
                        " property=" + propertyName + "\n");
                }
            }
            
            Map<String, List<RemoveData>> removeds = d.getRemoves();
            for (Map.Entry<String, List<RemoveData>> remove : removeds.entrySet()) {
                String propertyName = remove.getKey();
            
                for (RemoveData rd : remove.getValue()) {
                    String removedXmlPath = rd.getChild()._getXmlPath();
                    String removedInstanceName = rd.getChild()._getInstanceName();
                    
                    sb.append("  REMOVED: removedPath=" + removedXmlPath + " removedInstanceName=" + removedInstanceName + 
                        " property=" + propertyName + "\n");
                }
            }
            
            Map<String, List<MoveData>> moveds = d.getMoves();
            for (Map.Entry<String, List<MoveData>> entry : moveds.entrySet()) {
                String propertyName = entry.getKey();
                for (MoveData md : entry.getValue()) {
                    sb.append("  MOVED: oldIndex=" + md.getOldIndex() + " newIndex=" + md.getNewIndex() +
                        " property=" + propertyName + "\n");
                }
            }
            
            lcv++;
        }
        
        return sb.toString() + "\n," + System.identityHashCode(this) + ")";
    }
    
    private static class DifferenceKey {
        private final String xmlPath;
        private final String instanceName;
        private final int hash;
        
        private DifferenceKey(BaseHK2JAXBBean bean) {
            xmlPath = bean._getXmlPath();
            instanceName = bean._getInstanceName();
            
            hash = xmlPath.hashCode() ^ instanceName.hashCode();
        }
        
        @Override
        public int hashCode() {
            return hash;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof DifferenceKey)) return false;
            
            DifferenceKey other = (DifferenceKey) o;
            
            return xmlPath.equals(other.xmlPath) && instanceName.equals(other.instanceName) ;
        }
        
    }
}
