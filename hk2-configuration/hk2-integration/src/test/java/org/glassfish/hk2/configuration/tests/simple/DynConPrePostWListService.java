/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.configuration.tests.simple;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.glassfish.hk2.configuration.api.Configured;
import org.glassfish.hk2.configuration.api.ConfiguredBy;
import org.glassfish.hk2.configuration.api.PostDynamicChange;
import org.glassfish.hk2.configuration.api.PreDynamicChange;
import org.jvnet.hk2.annotations.Service;

/**
 * @author jwells
 *
 */
@Service @ConfiguredBy(type=BasicConfigurationTest.TEST_TYPE_THREE)
public class DynConPrePostWListService {
    @Configured(dynamicity=Configured.Dynamicity.FULLY_DYNAMIC)
    private String fieldOutput1;
    
    private String preChangeCalled = null;
    private String postChangeCalled = null;
    
    private List<PropertyChangeEvent> preChangeList = null;
    private List<PropertyChangeEvent> postChangeList = null;
    
    @PreDynamicChange
    private void preChange(List<PropertyChangeEvent> changes) {
        preChangeCalled = fieldOutput1;
        preChangeList = changes;
    }
    
    @PostDynamicChange
    private void postChange(List<PropertyChangeEvent> changes) {
        postChangeCalled = fieldOutput1;
        postChangeList = changes;
    }

    public String isPostChangeCalled() {
        return postChangeCalled;
    }
    
    public String isPreChangeCalled() {
        return preChangeCalled;
    }
    
    public List<PropertyChangeEvent> getPostChangeList() {
        return postChangeList;
    }
    
    public List<PropertyChangeEvent> getPreChangeList() {
        return preChangeList;
    }
    
    public String getFieldOutput1() {
        return fieldOutput1;
    }

}
