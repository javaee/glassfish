/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.bootstrap;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.jvnet.hk2.annotations.Contract;

/**
 * 
 * @author jwells, mason.taube@oracle.com
 *
 */
@Contract
public interface PopulatorPostProcessor {

    /**
     * This method can be used to alter the descriptor read in.  It can also
     * add descriptors, or remove the descriptor (by returning an empty list).
     * If this method returns null, then the passed in descriptor will be used.
     * Any alterations made to this descriptor in that case will remain in effect.
     * If this method returns a list the descriptors from the list will be added
     * to the service locator, and not the incoming descriptorImpl.  However, the
     * incoming descriptorImpl may be a member of the list.
     * 
     * @param descriptorImpl The descriptorImpl read from some external source.  This
     * processor can modify this descriptor fully.
     * 
     * @return A descriptors to be added to the system.  If this returns non-null
     * the descriptor will be added to the system 
     * If this returns null then no descriptor will be added to the system.
     */
     DescriptorImpl process(DescriptorImpl descriptorImpl);

     public void setServiceLocator(ServiceLocator serviceLocator);
}
