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

package com.sun.persistence.runtime.sqlstore.sql.select.impl;

import com.sun.persistence.runtime.model.mapping.RuntimeMappingField;

/**
 * This class implements Fetcher to fetch StateFieldResultElement
 * for a state field.
 * @author jielin
 */
class CMPVariableData implements Fetcher {
    
    private String navigationId;
    private RuntimeMappingField mappingField;
    
    /** Creates a new instance of CMPVariableData */  
    CMPVariableData(RuntimeMappingField mappingField, String navigationId) {
        this.mappingField = mappingField;
        this.navigationId = navigationId;
    }

    /**
     * fetch result element for a state field
     * @param plan the object which construct query and result set
     * @return StateFieldResultElement a result element for state field
     */
    public StateFieldResultElement fetch(SelectPlan plan) {
        return plan.fetchStateField(mappingField, navigationId);
    }
}


