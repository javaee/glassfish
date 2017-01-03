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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.utilities.cache.CacheUtilities;
import org.glassfish.hk2.utilities.cache.Computable;
import org.glassfish.hk2.utilities.cache.ComputationErrorException;
import org.glassfish.hk2.utilities.cache.WeakCARCache;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.xml.internal.Differences.AddData;
import org.glassfish.hk2.xml.internal.Differences.Difference;
import org.glassfish.hk2.xml.internal.Differences.MoveData;
import org.glassfish.hk2.xml.internal.Differences.RemoveData;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;

/**
 * @author jwells
 *
 */
public class UnkeyedDiff {
    private final static String UNKEYED_DEBUG_PROPERTY = "org.jvnet.hk2.properties.xml.unkeyed.debug";
    private final static boolean UNKEYED_DEBUG = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
        @Override
        public Boolean run() {
            return Boolean.getBoolean(UNKEYED_DEBUG_PROPERTY);
        }   
    });
    
    private final List<BaseHK2JAXBBean> legacyList;
    private final List<BaseHK2JAXBBean> proposedList;
    private final ParentedModel parentModel;
    private final BaseHK2JAXBBean parent;
    
    private boolean computed = false;
    private Differences finalSolution = null;
    
    private HashMap<Integer, Differences> solution;
    private HashSet<Integer> usedLegacy;
    private HashSet<Integer> unusedLegacy;
    private HashMap<Integer, Differences> proposedAdds;
    private HashMap<Integer, SchrodingerSolution> quantumSolutions;
    
    public UnkeyedDiff(List<BaseHK2JAXBBean> legacy, List<BaseHK2JAXBBean> proposed, BaseHK2JAXBBean parent, ParentedModel parentModel) {
        if (legacy == null) legacy = Collections.emptyList();
        if (proposed == null) proposed = Collections.emptyList();
        
        this.legacyList = new ArrayList<BaseHK2JAXBBean>(legacy);
        this.proposedList = new ArrayList<BaseHK2JAXBBean>(proposed);
        
        this.parent = parent;
        this.parentModel = parentModel;
    }
    
    private static List<BaseHK2JAXBBean> asList(Object a[]) {
        if (a == null) return Collections.<BaseHK2JAXBBean>emptyList();
        
        ArrayList<BaseHK2JAXBBean> retVal = new ArrayList<BaseHK2JAXBBean>(a.length);
        
        for (Object o : a) {
            retVal.add((BaseHK2JAXBBean) o);
        }
        
        return retVal;
    }
    
    public UnkeyedDiff(Object legacy[], Object proposed[], BaseHK2JAXBBean parent, ParentedModel parentModel) {
        this(asList(legacy), asList(proposed), parent, parentModel);
    }
    
    public synchronized Differences compute() {
        if (computed) return finalSolution;
        
        Differences retVal = null;
        try {
            retVal = internalCompute();
        }
        finally {
            if (retVal != null) {
                finalSolution = retVal;
            
                computed = true;
            }
        }
        
        return retVal;
    }
    
    private Differences internalCompute() {
        Differences retVal = new Differences();
        
        boolean needsChangeOfList = false;
        
        DifferenceTable table = new DifferenceTable();
        
        // First step, calculate the diagonal
        boolean diagonalChange = !table.calculateDiagonal();
        if (!diagonalChange) {
            if (legacyList.size() == proposedList.size()) {
                // Exactly the same lists
                return retVal;
            }
            
            // We are either adding to the end or removing from the end
            if (legacyList.size() < proposedList.size()) {
                // Adds to the end
                for (int lcv = legacyList.size(); lcv < proposedList.size(); lcv++) {
                    Difference d = new Difference(parent);
                    
                    d.addAdd(parentModel.getChildXmlTag(), new AddData(proposedList.get(lcv), lcv));
                    
                    retVal.addDifference(d);
                }
                
                return retVal;
            }
            
            // Removes from the end, proposed.size < legacy.size
            for (int lcv = proposedList.size(); lcv < legacyList.size(); lcv++) {
                String xmlTag = parentModel.getChildXmlTag();
                
                Difference d = new Difference(parent);
                
                d.addRemove(xmlTag, new RemoveData(xmlTag, lcv, legacyList.get(lcv)));
                
                retVal.addDifference(d);
            }
            
            return retVal;
        }
        
        initializeSolution();
        
        // Step one, find all diagonals that are exact, always prefer them over anything else
        for (int lcv = 0; lcv < table.getDiagonalSize(); lcv++) {
            Differences differences = table.getDiff(lcv, lcv);
            
            if (differences.getDifferences().isEmpty()) {
                // This is the best solution for this slot
                addSolution(lcv, lcv, differences);
            }
        }
        
        // Step two, match up any other exact matches
        for (int proposedIndex = 0; proposedIndex < proposedList.size(); proposedIndex++) {
            if (solution.containsKey(proposedIndex)) {
                if (UNKEYED_DEBUG) {
                    Logger.getLogger().debug("Skipping proposedIndex " + proposedIndex + " since it has already has a solution");
                }
                
                continue;
            }
            
            int currentBestDiffIndex = -1;
            Differences currentBestDiffs = null;
            for (int legacyIndex = 0; legacyIndex < legacyList.size(); legacyIndex++) {
                if (usedLegacy.contains(legacyIndex)) {
                    if (UNKEYED_DEBUG) {
                        Logger.getLogger().debug("Skipping legacyIndex " + legacyIndex + " for proposedIndex " +
                                proposedIndex + " since it has already has already been used");
                    }
                    
                    continue;
                }
                
                Differences currentDiffs = table.getDiff(legacyIndex, proposedIndex);
                
                if (currentBestDiffs == null || (currentBestDiffs.getDifferenceCost() > currentDiffs.getDifferenceCost())) {
                    currentBestDiffs = currentDiffs;
                    currentBestDiffIndex = legacyIndex;
                    
                    if (currentDiffs.getDifferences().isEmpty()) {
                        needsChangeOfList = true;
                        break;
                    }
                }
            }
            
            if (currentBestDiffs == null) {
                // We need to add this proposed bean
                Differences addMeDifference = new Differences();
                Difference difference = new Difference(parent);
                difference.addAdd(parentModel.getChildXmlTag(), new AddData(proposedList.get(proposedIndex), proposedIndex));
                
                addMeDifference.addDifference(difference);
                
                needsChangeOfList = true;
                addSolution(-1, proposedIndex, addMeDifference);
            }
            else {
                if (currentBestDiffs.getDifferences().isEmpty()) {
                    // Will never get better than a move
                    needsChangeOfList = true;
                    
                    Differences moveMeDifferences = new Differences();
                    Difference difference = new Difference(parent);
                    difference.addMove(parentModel.getChildXmlTag(), new MoveData(currentBestDiffIndex, proposedIndex));
                    
                    moveMeDifferences.addDifference(difference);
                    
                    addSolution(currentBestDiffIndex, proposedIndex, moveMeDifferences);
                }
                else {
                    BaseHK2JAXBBean currentProposed = proposedList.get(proposedIndex);
                    
                    Differences proposedAdd = proposedAdds.get(proposedIndex);
                    if (proposedAdd == null) {
                        proposedAdd = new Differences();
                        Difference d = new Difference(parent);
                        d.addAdd(parentModel.getChildXmlTag(), new AddData(currentProposed, proposedIndex));
                        
                        proposedAdd.addDifference(d);
                        
                        proposedAdds.put(proposedIndex, proposedAdd);
                    }
                    
                    SchrodingerSolution sd = new SchrodingerSolution(currentBestDiffIndex, currentBestDiffs, proposedAdd);
                    quantumSolutions.put(proposedIndex, sd);
                }
            }
            
        }
        
        for (Map.Entry<Integer, SchrodingerSolution> entry : quantumSolutions.entrySet()) {
            int proposedIndex = entry.getKey();
            SchrodingerSolution sd = entry.getValue();
            
            int legacyIndex = sd.legacyIndexOfDiff;
            if (usedLegacy.contains(legacyIndex)) {
                // No choice, the old best solution is no longer available
                needsChangeOfList = true;
                addSolution(-1, proposedIndex, sd.proposedAddDifference);
            }
            else if (unusedLegacy.contains(legacyIndex)) {
                // The cost of the add solution now must also contain the cost of the remove of the unused
                BaseHK2JAXBBean legacy = legacyList.get(legacyIndex);
                
                int removeCost = Utilities.calculateAddCost(legacy);
                
                int totalAddCost = removeCost + sd.proposedAddDifference.getDifferenceCost();
                
                if (totalAddCost >= sd.legacyDifference.getDifferenceCost()) {
                    // Change is better
                    addSolution(legacyIndex, proposedIndex, sd.legacyDifference);
                }
                else {
                    needsChangeOfList = true;
                    addSolution(-1, proposedIndex, sd.proposedAddDifference);
                }
            }
            else {
                // Can I get here?
                throw new AssertionError("Should not be able to get here");
            }
        }
        
        // Add all the changes
        for (Differences diffs : solution.values()) {
            retVal.merge(diffs);
        }
        
        // Now add all the removes
        for (Integer legacyRemoveIndex : unusedLegacy) {
            Difference d = new Difference(parent);
            
            d.addRemove(parentModel.getChildXmlTag(),
                    new RemoveData(parentModel.getChildXmlTag(), legacyRemoveIndex, legacyList.get(legacyRemoveIndex)));
            
            retVal.addDifference(d);
        }
        
        return retVal;
    }
    
    private void initializeSolution() {
        solution = new HashMap<Integer, Differences>();
        usedLegacy = new HashSet<Integer>();
        unusedLegacy = new HashSet<Integer>();
        for (int lcv = 0; lcv < legacyList.size(); lcv++) {
            unusedLegacy.add(lcv);
        }
        
        proposedAdds = new HashMap<Integer, Differences>();
        quantumSolutions = new HashMap<Integer, SchrodingerSolution>();
    }
    
    private void addSolution(int legacyIndex, int proposedIndex, Differences minimum) {
        solution.put(proposedIndex, minimum);
        if (legacyIndex >= 0) {
            usedLegacy.add(legacyIndex);
            unusedLegacy.remove(legacyIndex);
        }
    }
    
    private class DifferenceTable {
        private final int min;
        
        private final WeakCARCache<TableKey, Differences> table;
        
        private DifferenceTable() {
            min = Math.min(legacyList.size(), proposedList.size());
            
            int tableSize = legacyList.size() * proposedList.size();
            
            table = CacheUtilities.createWeakCARCache(new DifferenceMaker(),
                    tableSize, false);
        }
        
        private boolean calculateDiagonal() {
            boolean theSame = true;
            
            for (int lcv = 0; lcv < min; lcv++) {
                TableKey diagonalKey = new TableKey(lcv, lcv);
                
                Differences differences = table.compute(diagonalKey);
                if (!differences.getDifferences().isEmpty()) {
                    theSame = false;
                }
            }
            
            return theSame;
        }
        
        private Differences getDiff(int legacyIndex, int proposedIndex) {
            return table.compute(new TableKey(legacyIndex, proposedIndex));
        }
        
        private int getDiagonalSize() {
            return min;
        }
        
        @Override
        public String toString() {
            return "DifferenceTable(min=" + min + " legacySize=" + legacyList.size() + " proposedSize=" + proposedList.size() + "," + System.identityHashCode(this) + ")";
        }
    }
    
    private class DifferenceMaker implements Computable<TableKey, Differences> {

        @Override
        public Differences compute(TableKey key)
                throws ComputationErrorException {
            BaseHK2JAXBBean legacyBean = legacyList.get(key.legacyIndex);
            BaseHK2JAXBBean proposedBean = proposedList.get(key.proposedIndex);
            
            Differences retVal = Utilities.getDiff(legacyBean, proposedBean);
            return retVal;
        }
        
        @Override
        public String toString() {
            return "DifferenceMaker(" + System.identityHashCode(this) + ")";
        }
    }
    
    private static class TableKey {
        private final int legacyIndex;
        private final int proposedIndex;
        private final int hash;
        
        private TableKey(int legacyIndex, int proposedIndex) {
            this.legacyIndex = legacyIndex;
            this.proposedIndex = proposedIndex;
            hash = legacyIndex ^ proposedIndex;
        }
        
        @Override
        public int hashCode() {
            return hash;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof TableKey)) return false;
            
            TableKey other = (TableKey) o;
            
            return (other.legacyIndex == legacyIndex) && (other.proposedIndex == proposedIndex);
        }
        
        @Override
        public String toString() {
            return "TableKey(" + legacyIndex + "," + proposedIndex + "," + System.identityHashCode(this) + ")";
        }
    }
    
    /**
     * Schrodinger solution because it isn't one or the other until later
     * when we know more about the full solution
     * 
     * @author jwells
     *
     */
    private static class SchrodingerSolution {
        /*
         * Solution 0:  The legacy modification
         */
        private final int legacyIndexOfDiff;
        private final Differences legacyDifference;
        
        /*
         * Solution 1:  The proposed add modification
         */
        private final Differences proposedAddDifference;
        
        private SchrodingerSolution(int legacyIndexOfDiff, Differences legacyDifference, Differences proposedAddDifference) {
            this.legacyIndexOfDiff = legacyIndexOfDiff;
            this.legacyDifference = legacyDifference;
            
            this.proposedAddDifference = proposedAddDifference;
        }
        
        @Override
        public String toString() {
            return "SchrodingerSolution(" + legacyIndexOfDiff + "," + legacyDifference + "," + proposedAddDifference + "," + System.identityHashCode(this) + ")";
        }
    }

}
