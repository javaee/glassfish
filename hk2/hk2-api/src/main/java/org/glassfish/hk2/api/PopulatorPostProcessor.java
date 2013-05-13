/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.api;

import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * This interface allows the customization of services read in from
 * an external source.  For example, specific classloaders can be added,
 * or duplicate descriptors can be removed.
 * 
 * @author jwells
 *
 */
public interface PopulatorPostProcessor {
    /**
     * This method can be used to alter the descriptor read in.  It can also
     * add descriptors, or remove the descriptor (by returning null).
     * Any alterations made to the descriptor passed in will remain in effect.
     *
     * @param serviceLocator the ServiceLocator being populated.  Will not be null
     * @param descriptorImpl The descriptorImpl read from some external source.  This
     * processor can modify this descriptor fully
     * 
     * @return The descriptors to be added to the system.  If this returns null
     * then the descriptorImpl passed in will NOT be added to the system.  Implementations
     * may return the descriptor passed in, but do not have to.  The descriptor added to
     * the system will be the one returned from this method
     */
     public DescriptorImpl process(ServiceLocator serviceLocator, DescriptorImpl descriptorImpl);

}
