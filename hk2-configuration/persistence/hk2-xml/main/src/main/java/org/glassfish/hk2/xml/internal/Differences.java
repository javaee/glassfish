/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016-2017 Oracle and/or its affiliates. All rights reserved.
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
        
        private final Map<String, AddRemoveMoveDifference> childChanges = new LinkedHashMap<String, AddRemoveMoveDifference>();
        
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
        
        public Map<String, AddRemoveMoveDifference> getChildChanges() {
            return childChanges;
        }
        
        private AddRemoveMoveDifference getARMDiff(String propName) {
            AddRemoveMoveDifference field = childChanges.get(propName);
            if (field == null) {
                field = new AddRemoveMoveDifference();
                
                childChanges.put(propName, field);
            }
            
            return field;
        }
        
        public void addAdd(String propName, AddData toAdd) {
            AddRemoveMoveDifference arm = getARMDiff(propName);
            arm.addAdd(toAdd);
        }
        
        public void addAdd(String propName, BaseHK2JAXBBean toAdd, int index) {
            addAdd(propName, new AddData(toAdd, index));
        }
        
        public void addMove(String propName, MoveData md) {
            AddRemoveMoveDifference arm = getARMDiff(propName);
            arm.addMove(md);
        }
        
        public void addRemove(String propName, RemoveData removeData) {
            AddRemoveMoveDifference arm = getARMDiff(propName);
            arm.addRemove(removeData);
        }
        
        public void addDirectReplace(String propName, BaseHK2JAXBBean toAdd, RemoveData removeData) {
            AddRemoveMoveDifference arm = getARMDiff(propName);
            arm.addDirectReplace(new AddRemoveData(new AddData(toAdd, -1), removeData));
        }
        
        private void merge(Difference mergeMe) {
            for (PropertyChangeEvent pce : mergeMe.getNonChildChanges()) {
                addNonChildChange(pce);
            }
            
            Map<String, AddRemoveMoveDifference> childChanges = mergeMe.getChildChanges();
            for (Map.Entry<String, AddRemoveMoveDifference> childEntry : childChanges.entrySet()) {
                String propertyName = childEntry.getKey();
                AddRemoveMoveDifference arm = childEntry.getValue();
                
                for (AddData add : arm.getAdds()) {
                    addAdd(propertyName, new AddData(add));
                }
                
                for (RemoveData remove : arm.getRemoves()) {
                    addRemove(propertyName, new RemoveData(remove));
                }
                
                for (MoveData move : arm.getMoves()) {
                    addMove(propertyName, new MoveData(move));
                }
                
            }
        }
        
        public boolean isDirty() {
            return !nonChildChanges.isEmpty() || !childChanges.isEmpty();
        }
        
        public boolean hasChildChanges() {
            return !childChanges.isEmpty();
        }
        
        public int getSize() {
            int retVal = nonChildChanges.size();
            
            retVal += childChanges.size();
            
            return retVal;
        }
        
        @Override
        public String toString() {
            return "Difference(" + source + "," + System.identityHashCode(this) + ")";
        }
    }
    
    public static class AddRemoveData {
        private final AddData add;
        private final RemoveData remove;
        
        private AddRemoveData(AddData add, RemoveData remove) {
            this.add = add;
            this.remove = remove;
        }
        
        public AddData getAdd() {
            return add;
        }
        
        public RemoveData getRemove() {
            return remove;
        }
        
        @Override
        public String toString() {
            return "AddRemoveData(" + add + "," + remove + "," + System.identityHashCode(this) + ")";
        }
    }
    
    public static class AddRemoveMoveDifference {
        private final List<AddData> adds = new ArrayList<AddData>();
        private final List<RemoveData> removes = new ArrayList<RemoveData>();
        private final List<MoveData> moves = new ArrayList<MoveData>();
        private final List<AddRemoveData> directReplace = new ArrayList<AddRemoveData>();
        
        private void addAdd(AddData add) {
            adds.add(add);
        }
        
        private void addRemove(RemoveData remove) {
            removes.add(remove);
        }
        
        private void addMove(MoveData move) {
            moves.add(move);
        }
        
        private void addDirectReplace(AddRemoveData dr) {
            directReplace.add(dr);
        }
        
        public List<AddData> getAdds() {
            return adds;
        }
        
        public List<RemoveData> getRemoves() {
            return removes;
        }
        
        public List<MoveData> getMoves() {
            return moves;
        }
        
        public List<AddRemoveData> getDirectReplaces() {
            return directReplace;
        }
        
        public boolean requiresListChange() {
            return !adds.isEmpty() || !removes.isEmpty() || !moves.isEmpty() || !directReplace.isEmpty();
        }
        
        public int getSize() {
            return adds.size() + removes.size() + moves.size() + directReplace.size();
        }
        
        public int getNewSize(int oldSize) {
            // Direct replace is +1 and -1, so 0
            return oldSize - removes.size() + adds.size();
        }
        
        @Override
        public String toString() {
            return "AddRemoveMoveDifference(" + adds + "," + removes + "," + moves + "," + directReplace + "," + System.identityHashCode(this) + ")";
        }
    }
    
    public static class AddData {
        private final BaseHK2JAXBBean toAdd;
        private final int index;
        
        public AddData(BaseHK2JAXBBean toAdd, int index) {
            this.toAdd = toAdd;
            this.index = index;
        }
        
        public AddData(AddData copyMe) {
            this.toAdd = copyMe.toAdd;
            this.index = copyMe.index;
        }
        
        public BaseHK2JAXBBean getToAdd() { return toAdd; }
        public int getIndex() { return index; }
        
        @Override
        public String toString() {
            return "AddData(" + toAdd + "," + index + "," + System.identityHashCode(this) + ")";
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
        
        public RemoveData(RemoveData copyMe) {
            this(copyMe.childProperty, copyMe.childKey, copyMe.index, copyMe.child);
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
        
        @Override
        public String toString() {
            return "RemoveData(" + childProperty + "," + childKey + "," + index + "," + child + "," + System.identityHashCode(this) + ")";
        }
        
    }
    
    public static class MoveData {
        private final int oldIndex;
        private final int newIndex;
        
        public MoveData(int oldIndex, int newIndex) {
            this.oldIndex = oldIndex;
            this.newIndex = newIndex;
        }
        
        public MoveData(MoveData copyMe) {
            this(copyMe.oldIndex, copyMe.newIndex);
        }
        
        public int getOldIndex() { return oldIndex; }
        public int getNewIndex() { return newIndex; }
        
        @Override
        public String toString() {
            return "MoveData(" + oldIndex + "," + newIndex + "," + System.identityHashCode(this) + ")";
        }
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
                sb.append("\tCHANGED: " + event.getPropertyName() + " from " + event.getOldValue() + " to " + event.getNewValue() + "\n");
            }
            
            Map<String, AddRemoveMoveDifference> childChanges = d.getChildChanges();
            
            for (Map.Entry<String, AddRemoveMoveDifference> childEntry : childChanges.entrySet()) {
                String propertyName = childEntry.getKey();
                
                sb.append("  CHANGED CHILD: " + propertyName + "\n");
                AddRemoveMoveDifference arm = childEntry.getValue();
                
                for (AddData ad : arm.getAdds()) {
                    BaseHK2JAXBBean added = ad.getToAdd();
                    int index = ad.getIndex();
                    
                    String addedXmlPath = added._getXmlPath();
                    String addedInstanceName = added._getInstanceName();
                    
                    sb.append("    ADDED: addedPath=" + addedXmlPath + " addedInstanceName=" + addedInstanceName + " addedIndex=" + index + "\n");
                }
                
                for (RemoveData rd : arm.getRemoves()) {
                    String removedXmlPath = rd.getChild()._getXmlPath();
                    String removedInstanceName = rd.getChild()._getInstanceName();
                    
                    sb.append("    REMOVED: removedPath=" + removedXmlPath + " removedInstanceName=" + removedInstanceName + "\n");
                }
                
                for (MoveData md : arm.getMoves()) {
                    sb.append("    MOVED: oldIndex=" + md.getOldIndex() + " newIndex=" + md.getNewIndex() + "\n");
                }
                
                for (AddRemoveData ard : arm.getDirectReplaces()) {
                    AddData ad = ard.getAdd();
                    RemoveData rd = ard.getRemove();
                    
                    BaseHK2JAXBBean added = ad.getToAdd();
                    
                    String addedXmlPath = added._getXmlPath();
                    String addedInstanceName = added._getInstanceName();
                    
                    String removedInstanceName = rd.getChild()._getInstanceName();
                    
                    sb.append("    DIRECT_REPLACEMENT: modifiedPath=" + addedXmlPath + " addedInstanceName=" + addedInstanceName +
                            " removedInstanceName=" + removedInstanceName + "\n");
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
        
        @Override
        public String toString() {
            return "DifferenceKey(" + xmlPath + "," + instanceName + "," + hash + "," + System.identityHashCode(this) + ")";
        }
        
    }
}
