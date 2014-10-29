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
package org.glassfish.hk2.utilities;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.FactoryDescriptors;

/**
 * This is an implementation of FactoryDescriptors that can be
 * used by hk2 uses when creating descriptors that describe
 * a {@link Factory}
 * 
 * @author jwells
 *
 */
public class FactoryDescriptorsImpl implements FactoryDescriptors {
    private final Descriptor asService;
    private final Descriptor asProvideMethod;
    
    /**
     * This creates a descriptor pair describing a {@link Factory}
     * and the associated {@link Factory#provide()} method
     * 
     * @param asService A description of the factory itself as an hk2 service.
     * May not be null.  Must have DescriptorType of {@link DescriptorType#CLASS}.  One
     * of the contracts must be Factory
     * @param asProvideMethod A description of the provide method of the factory.  Must have
     * DescriptorType of {@link DescriptorType#PROVIDE_METHOD}.
     * May not be null
     * @throws IllegalArgumentException if the descriptors are not of the proper type
     */
    public FactoryDescriptorsImpl(Descriptor asService, Descriptor asProvideMethod) {
        if (asService == null || asProvideMethod == null) throw new IllegalArgumentException();
        if (!DescriptorType.CLASS.equals(asService.getDescriptorType())) {
            throw new IllegalArgumentException("Creation of FactoryDescriptors must have first argument of type CLASS");
        }
        if (!asService.getAdvertisedContracts().contains(Factory.class.getName())) {
            throw new IllegalArgumentException("Creation of FactoryDescriptors must have Factory as a contract of the first argument");
        }
        if (!DescriptorType.PROVIDE_METHOD.equals(asProvideMethod.getDescriptorType())) {
            throw new IllegalArgumentException("Creation of FactoryDescriptors must have second argument of type PROVIDE_METHOD");
            
        }
        this.asService = asService;
        this.asProvideMethod = asProvideMethod;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.FactoryDescriptors#getFactoryAsService()
     */
    @Override
    public Descriptor getFactoryAsAService() {
        return asService;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.FactoryDescriptors#getFactoryAsAFactory()
     */
    @Override
    public Descriptor getFactoryAsAFactory() {
        return asProvideMethod;
    }
    
    @Override
    public int hashCode() {
        return asService.hashCode() ^ asProvideMethod.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof FactoryDescriptors)) return false;
        
        FactoryDescriptors other = (FactoryDescriptors) o;
        Descriptor otherService = other.getFactoryAsAService();
        Descriptor otherFactory = other.getFactoryAsAFactory();
        
        if (otherService == null || otherFactory == null) return false;
        
        return (asService.equals(otherService) && asProvideMethod.equals(otherFactory));
    }
    
    @Override
    public String toString() {
        return "FactoryDescriptorsImpl(\n" +
          asService + ",\n" + asProvideMethod + ",\n\t" + System.identityHashCode(this) + ")";
    }

}
