/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Set;

/**
 * This is added to the {@link ServiceLocatorFactory} in order to listen on service locators
 * coming and going.  Implementations of this interface must be able to be stored in a HashMap
 * 
 * @author jwells
 *
 */
public interface ServiceLocatorListener {
    /**
     * This method returns the complete list of named service
     * locators at the time that this listener is registered.  The list
     * may be empty.  This method will NOT pass any unnamed
     * ServiceLocators, as they are not tracked by the system
     * <p>
     * Any exceptions thrown from this method will be logged
     * and ignored.  If an exception is thrown from
     * this method then this listener will NOT be added
     * to the set of listeners notified by the system
     * 
     * @param initialLocators The set of named locators available when
     * the listener is registered
     */
    public void initialize(Set<ServiceLocator> initialLocators);
    
    /**
     * This method is called whenever a ServiceLocator has been
     * added to the set of ServiceLocators.  This method
     * WILL be passed unnamed ServiceLocators when they are added
     * <p>
     * Any exceptions thrown from this method will be logged
     * and ignored
     * 
     * @param added The non-null ServiceLocator that is to be added
     */
    public void locatorAdded(ServiceLocator added);
    
    /**
     * This method is called whenever a ServiceLocator will be
     * removed from the set of ServiceLocators.  This method WILL
     * be passed unnamed ServiceLocators when they are destroyed
     * <p>
     * Any exceptions thrown from this method will be logged
     * and ignored
     * 
     * @param destroyed The non-null ServiceLocator that is to be destroyed
     */
    public void locatorDestroyed(ServiceLocator destroyed);

}
