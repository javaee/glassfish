/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.locator.negative.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.HK2Loader;

/**
 * @author jwells
 *
 */
public class NullDescriptorImpl implements Descriptor {
    private String implementation;
    private Set<String> contracts;
    private DescriptorType type;
    private Map<String, List<String>> metadata;
    private Set<String> qualifiers;
    private String name;
    
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getImplementation()
     */
    @Override
    public String getImplementation() {
        return implementation;
    }
    
    public void unNullContracts() {
        contracts = new HashSet<String>();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getAdvertisedContracts()
     */
    @Override
    public Set<String> getAdvertisedContracts() {
        return contracts;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getScope()
     */
    @Override
    public String getScope() {
        return null;
    }
    
    /* package */ void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getName()
     */
    @Override
    public String getName() {
        return name;
    }
    
    public void unNullQualifiers() {
        qualifiers = new HashSet<String>();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getQualifiers()
     */
    @Override
    public Set<String> getQualifiers() {
        return qualifiers;
    }
    
    public void unNullType(boolean asFactory) {
        if (asFactory) {
            type = DescriptorType.PROVIDE_METHOD;
        }
        else {
            type = DescriptorType.CLASS;
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getDescriptorType()
     */
    @Override
    public DescriptorType getDescriptorType() {
        return type;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getDescriptorVisibility()
     */
    @Override
    public DescriptorVisibility getDescriptorVisibility() {
        return DescriptorVisibility.NORMAL;
    }
    
    public void unNullMetadata() {
        metadata = new HashMap<String, List<String>>();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getMetadata()
     */
    @Override
    public Map<String, List<String>> getMetadata() {
        return metadata;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getLoader()
     */
    @Override
    public HK2Loader getLoader() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getRanking()
     */
    @Override
    public int getRanking() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#setRanking(int)
     */
    @Override
    public int setRanking(int ranking) {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getServiceId()
     */
    @Override
    public Long getServiceId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getLocatorId()
     */
    @Override
    public Long getLocatorId() {
        return null;
    }

    @Override
    public Boolean isProxiable() {
        return null;
    }

    @Override
    public String getClassAnalysisName() {
        return null;
    }

    @Override
    public Boolean isProxyForSameScope() {
        return null;
    }

}
