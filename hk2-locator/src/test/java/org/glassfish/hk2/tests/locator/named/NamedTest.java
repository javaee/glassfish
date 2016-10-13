/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2016 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.tests.locator.named;

import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class NamedTest {
    private final static String TEST_NAME = "NamedTest";
    private final static ServiceLocator locator = LocatorHelper.create(TEST_NAME, new NamedModule());
    
    /** Did my heart love till now? */
    public final static String ROMEO = "Romeo";
    /** O! she doth teach the torches to burn bright */
    public final static String JULIET = "Juliet";
    /** Queen Mab */
    public final static String MERCUTIO = "Mercutio";
    /** Romeos' cousin */
    public final static String BENVOLIO = "Benvolio";
    /** A rose by any other name */
    public final static String ROSE = "Rose";
    /** Romeo's first girlfriend */
    public final static String ROSALIND = "Rosalind";
    
    /**
     * Tests that I can differentiate between citizens
     */
    @Test
    public void getAutoNamedService() {
        CitizenOfVerona romeo = locator.getService(CitizenOfVerona.class, ROMEO);
        Assert.assertEquals(ROMEO, romeo.getName());
        
        CitizenOfVerona juliet = locator.getService(CitizenOfVerona.class, JULIET);
        Assert.assertEquals(JULIET, juliet.getName());
    }
    
    /**
     * Tests that I can inject via name on fields, methods and constructors
     */
    @Test
    public void getInjectedViaName() {
        Verona v = locator.getService(Verona.class);
        
        Assert.assertEquals(ROMEO, v.getRomeo().getName());
        Assert.assertEquals(JULIET, v.getJuliet().getName());
        Assert.assertEquals(MERCUTIO, v.getMercutio().getName());
        Assert.assertEquals(BENVOLIO, v.getBenvolio().getName());
    }
    
    /**
     * Tests that the same name of different types will both return
     */
    @Test
    public void getMultiNamed() {
        List<ActiveDescriptor<?>> roses = locator.getDescriptors(BuilderHelper.createNameFilter(ROSE));
        Assert.assertEquals(2, roses.size());
        
        int lcv = 0;
        for (ActiveDescriptor<?> rose : roses) {
            switch (lcv) {
            case 0:
                Assert.assertTrue(rose.getImplementation().equals(Centifolia.class.getName()));
                break;
            case 1:
                Assert.assertTrue(rose.getImplementation().equals(Damask.class.getName()));
                break;
            }
            
            lcv++;
        }
    }
    
    /**
     * Tests that the same name of different types will both return
     */
    @Test
    public void getMultiNamedQualifiedWithType() {
        List<ActiveDescriptor<?>> roses = locator.getDescriptors(
                BuilderHelper.createNameAndContractFilter(Centifolia.class.getName(), ROSE));
        Assert.assertEquals(1, roses.size());
        
        int lcv = 0;
        for (ActiveDescriptor<?> rose : roses) {
            switch (lcv) {
            case 0:
                Assert.assertTrue(rose.getImplementation().equals(Centifolia.class.getName()));
                break;
            }
            
            lcv++;
        }
        
        roses = locator.getDescriptors(
                BuilderHelper.createNameAndContractFilter(Damask.class.getName(), ROSE));
        Assert.assertEquals(1, roses.size());
        
        lcv = 0;
        for (ActiveDescriptor<?> rose : roses) {
            switch (lcv) {
            case 0:
                Assert.assertTrue(rose.getImplementation().equals(Damask.class.getName()));
                break;
            }
            
            lcv++;
        }
    }
    
    /**
     * Tests that you can use an Index filter with both values returning null
     */
    @Test
    public void getIndexedFilterWithBothIndexesNull() {
        List<ActiveDescriptor<?>> capulets = locator.getDescriptors(new DoubleNullIndexFilter(true));
        Assert.assertEquals(1, capulets.size());
        
        int lcv = 0;
        for (ActiveDescriptor<?> capulet : capulets) {
            switch (lcv) {
            case 0:
                Assert.assertTrue(capulet.getImplementation().equals(Juliet.class.getName()));
                break;
            }
            
            lcv++;
        }
        
        List<ActiveDescriptor<?>> montagues = locator.getDescriptors(new DoubleNullIndexFilter(false));
        Assert.assertEquals(3, montagues.size());
        
        lcv = 0;
        for (ActiveDescriptor<?> montague : montagues) {
            switch (lcv) {
            case 0:
                Assert.assertTrue(montague.getImplementation().equals(Romeo.class.getName()));
                break;
            case 1:
                Assert.assertTrue(montague.getImplementation().equals(Mercutio.class.getName()));
                break;
            case 2:
                Assert.assertTrue(montague.getImplementation().equals(Benvolio.class.getName()));
                break;
            }
            
            lcv++;
        }
    }
    
    private static class DoubleNullIndexFilter implements IndexedFilter {
        private final boolean capulet;
        
        private DoubleNullIndexFilter(boolean capulet) {
            this.capulet = capulet;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.Filter#matches(org.glassfish.hk2.api.Descriptor)
         */
        @Override
        public boolean matches(Descriptor d) {
            if (capulet) {
                if (d.getQualifiers().contains(Capulet.class.getName())) {
                    return true;
                }
            }
            else {
                if (d.getQualifiers().contains(Montague.class.getName())) {
                    return true;
                }
            }
            
            return false;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.IndexedFilter#getAdvertisedContract()
         */
        @Override
        public String getAdvertisedContract() {
            // Both indexes return null
            return null;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.IndexedFilter#getName()
         */
        @Override
        public String getName() {
            // Both indexes return null
            return null;
        }
        
    }

}
