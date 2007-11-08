/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.model.jdo.caching;

import com.sun.org.apache.jdo.model.jdo.JDORelationship;
import com.sun.org.apache.jdo.impl.model.jdo.JDOReferenceImplDynamic;

/**
 * An instance of this class represents the JDO relationship metadata 
 * of a reference relationship field. This caching implementation
 * caches any calulated value to avoid re-calculating it if it is
 * requested again. 
 *
 * @author Michael Bouschen
 * @since 2.0
 * @version 2.0
 */
public class JDOReferenceImplCaching extends JDOReferenceImplDynamic {
    
    /** 
     * Get the mappedBy relationship. If there is no mappedBy relationship
     * set, the method checks the mappedBy name as specified in the declaring
     * field and resolves the relationship. The method returns
     * <code>null</code> if there is no mappedBy relationship set and there
     * is no mappedBy name specified on the declaring field.
     * @return the mappedBy relationship if available; <code>null</code>
     * otherwise.
     */
    public JDORelationship getMappedBy() {
        if (mappedBy == null) {
            mappedBy = super.getMappedBy();
        }
        return mappedBy;
    }

    /**
     * Get the inverse JDORelationship in the case of a two-way relationship.
     * @return the inverse relationship
     */
    public JDORelationship getInverseRelationship() {
        if (inverse == null) {
            inverse = super.getInverseRelationship();
        }
        return inverse;
    }
}
