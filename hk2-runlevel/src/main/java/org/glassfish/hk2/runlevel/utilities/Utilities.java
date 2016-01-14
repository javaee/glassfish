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

package org.glassfish.hk2.runlevel.utilities;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Run level service related utilities.
 *
 * @author tbeerbower
 */
public class Utilities {
    /**
     * Get the run level value from the metadata of the given descriptor.
     *
     * @param descriptor  the descriptor to get the run level for
     *
     * @return the run level
     */
    public static int getRunLevelValue(ServiceLocator locator, Descriptor descriptor) {
        boolean isReified = false;
        ActiveDescriptor<?> active = null;
        if (descriptor instanceof ActiveDescriptor) {
            active = (ActiveDescriptor<?>) descriptor;
            
            isReified = active.isReified();
            if (isReified) {
                Annotation anno = active.getScopeAsAnnotation();
                if (anno instanceof RunLevel) {
                    RunLevel runLevel = (RunLevel) anno;
                
                    return runLevel.value();
                }
            }
        }
        
        List<String> list = descriptor.getMetadata().
                get(RunLevel.RUNLEVEL_VAL_META_TAG);
        
        if (list == null || list.isEmpty()) {
            if (active != null && !isReified) {
                active = locator.reifyDescriptor(active);
                
                Annotation anno = active.getScopeAsAnnotation();
                if (anno instanceof RunLevel) {
                    RunLevel runLevel = (RunLevel) anno;
                
                    return runLevel.value();
                }
            }
            
            return RunLevel.RUNLEVEL_VAL_IMMEDIATE;
        }
        
        return Integer.parseInt(list.get(0));
    }

    /**
     * Get the run level mode from the metadata of the given descriptor.
     *
     * @param descriptor  the descriptor
     *
     * @return the mode
     */
    public static int getRunLevelMode(ServiceLocator locator, Descriptor descriptor, Integer modeOverride) {
        if (modeOverride != null) return modeOverride;
        
        boolean isReified = false;
        ActiveDescriptor<?> active = null;
        if (descriptor instanceof ActiveDescriptor) {
            active = (ActiveDescriptor<?>) descriptor;
            
            isReified = active.isReified();
            if (isReified) {
                Annotation anno = active.getScopeAsAnnotation();
                if (anno instanceof RunLevel) {
                    RunLevel runLevel = (RunLevel) anno;
                
                    return runLevel.mode();
                }
            }
        }
        
        List<String> list = descriptor.getMetadata().
                get(RunLevel.RUNLEVEL_MODE_META_TAG);
        
        if (list == null || list.isEmpty()) {
            if (active != null && !isReified) {
                active = locator.reifyDescriptor(active);
                
                Annotation anno = active.getScopeAsAnnotation();
                if (anno instanceof RunLevel) {
                    RunLevel runLevel = (RunLevel) anno;
                
                    return runLevel.mode();
                }
            }
            
            return RunLevel.RUNLEVEL_MODE_VALIDATING;
        }
        
        return Integer.parseInt(list.get(0));
    }
}
