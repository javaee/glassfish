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

import com.sun.persistence.runtime.model.mapping.RuntimeMappingClass;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingField;
import com.sun.persistence.runtime.sqlstore.sql.select.impl.SelectPlan.ParameterData;
import com.sun.persistence.runtime.sqlstore.sql.impl.InputParameter;
import com.sun.persistence.runtime.sqlstore.sql.impl.EntityUserInputParameter;
import com.sun.forte4j.modules.dbmodel.ColumnElement;

/**
 * @author Mitesh Meswani
 */
class IdentificationVariableData implements Fetcher {
    private RuntimeMappingClass mappingClass;
    private String navigationId;

    IdentificationVariableData(RuntimeMappingClass mappingClass, String navigationId) {
        this.mappingClass = mappingClass;
        this.navigationId = navigationId;
    }

    public RuntimeMappingClass getMappingClass() {
        return mappingClass;
    }

    public String getNavigationId() {
        return navigationId;
    }
    
    public EntityResultElement fetch(SelectPlan plan) {
        return plan.fetchEntity(mappingClass, navigationId);
    }

    /**
     * This method generates sql for expression of type entity = ?1. The
     * generated sql is of the form entity.pk1 = ? and entity.pk2 = ?....
     * The method also adds paramaters to parameter table for each of the '?'
     * above
     * @param selectPlan The selectPlan from which column text is obtained
     * @param paramData The parameter data for the parameter
     * @param sqlText The sql text for the comparision is returned here
     * @return Array of InputParameter for this comparision. Please note that
     * param sqlText also contains a return value
     */
    public InputParameter[] generateToParamComparison(SelectPlan selectPlan,
            ParameterData paramData, StringBuffer sqlText) {
        assert sqlText.length() == 0 : "This method initializes the sqlText" // NOI18N
                + "It must be empty"; // NOI18N
        RuntimeMappingClass mappingClass = getMappingClass();
        RuntimeMappingField[] pkFields = mappingClass.getPrimaryKeyMappingFields();
        InputParameter[] inputParams = new InputParameter[pkFields.length];
        for (int i = 0 ; i < pkFields.length; i++) {
            //--Prepare the Text
            if (i != 0) {
                //More than one pk field append and
                sqlText.append(" and "); //NOI18N
            }
            //Assume pk fields are always mapped to single column
            RuntimeMappingField pkField = pkFields[i];
            ColumnElement column = pkField.getColumns()[0];
            StringBuffer columnText = selectPlan.getColumnText(
                    getNavigationId(), pkField.getFirstMappingTable(), column);
            sqlText.append(columnText).append(" = ").append("?"); // NOI18N

            //--Add to the parameter table
            InputParameter inputParam = new EntityUserInputParameter(
                    paramData.positionMarker, pkField);
            inputParams[i] = inputParam;
        }
        return inputParams;
    }
}
