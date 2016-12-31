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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.glassfish.hk2.utilities.cache.CacheUtilities;
import org.glassfish.hk2.utilities.cache.Computable;
import org.glassfish.hk2.utilities.cache.ComputationErrorException;
import org.glassfish.hk2.utilities.cache.WeakCARCache;
import org.glassfish.hk2.xml.internal.Differences.Difference;
import org.glassfish.hk2.xml.internal.Differences.RemoveData;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;

/**
 * @author jwells
 *
 */
public class UnkeyedDiff {
    private final static Differences EMPTY_DIFF = new Differences();
    
    private final List<BaseHK2JAXBBean> legacy;
    private final List<BaseHK2JAXBBean> proposed;
    private final ParentedModel parentModel;
    
    private final Difference difference;
    private boolean computed = false;
    
    public UnkeyedDiff(Difference difference, List<BaseHK2JAXBBean> legacy, List<BaseHK2JAXBBean> proposed, ParentedModel parentModel) {
        this.difference = difference;
        
        if (legacy == null) legacy = Collections.emptyList();
        if (proposed == null) proposed = Collections.emptyList();
        
        this.legacy = new ArrayList<BaseHK2JAXBBean>(legacy);
        this.proposed = new ArrayList<BaseHK2JAXBBean>(proposed);
        
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
    
    public UnkeyedDiff(Difference difference, Object legacy[], Object proposed[], ParentedModel parentModel) {
        this(difference, asList(legacy), asList(proposed), parentModel);
    }
    
    public synchronized void compute() {
        if (computed) return;
        
        try {
            internalCompute();
        }
        finally {
            computed = true;
        }
    }
    
    private void internalCompute() {
        DifferenceTable table = new DifferenceTable();
        
        // First step, calculate the diagonal
        boolean diagonalChange = !table.calculateDiagonal();
        if (!diagonalChange) {
            if (legacy.size() == proposed.size()) {
                // Exactly the same lists
                return;
            }
            
            // We are either adding to the end or removing from the end
            if (legacy.size() < proposed.size()) {
                // Adds to the end
                for (int lcv = legacy.size(); lcv < proposed.size(); lcv++) {
                    difference.addAdd(parentModel.getChildXmlTag(), proposed.get(lcv));
                }
                
                return;
            }
            
            // Removes from the end, proposed.size < legacy.size
            for (int lcv = proposed.size(); lcv < legacy.size(); lcv++) {
                String xmlTag = parentModel.getChildXmlTag();
                
                difference.addRemove(xmlTag, new RemoveData(xmlTag, lcv, legacy.get(lcv)));
            }
            
            return;
        }
        
        throw new AssertionError("not yet implemented");
    }
    
    private class DifferenceTable {
        private final int min;
        
        private final WeakCARCache<TableKey, Differences> table;
        
        private DifferenceTable() {
            min = Math.min(legacy.size(), proposed.size());
            
            int tableSize = legacy.size() * proposed.size();
            
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
    }
    
    private class DifferenceMaker implements Computable<TableKey, Differences> {

        @Override
        public Differences compute(TableKey key)
                throws ComputationErrorException {
            BaseHK2JAXBBean legacyBean = legacy.get(key.legacyIndex);
            BaseHK2JAXBBean proposedBean = proposed.get(key.proposedIndex);
            
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

}
