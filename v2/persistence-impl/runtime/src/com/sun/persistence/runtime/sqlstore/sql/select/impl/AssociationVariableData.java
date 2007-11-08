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

import com.sun.org.apache.jdo.model.jdo.JDORelationship;
import com.sun.persistence.api.model.mapping.MappingReferenceKey;
import com.sun.persistence.api.model.mapping.MappingRelationship;
import com.sun.persistence.api.model.mapping.MappingTable;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingClass;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingField;
import com.sun.persistence.runtime.sqlstore.sql.impl.EntityUserInputParameter;
import com.sun.persistence.runtime.sqlstore.sql.impl.InputParameter;
import com.sun.persistence.runtime.sqlstore.sql.select.impl.SelectPlan.ParameterData;
import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.forte4j.modules.dbmodel.ColumnPairElement;

/**
 * This class represents a association variable in a query
 * @author Mitesh Meswani
 */
public class AssociationVariableData extends IdentificationVariableData {
    /**
     * The parent field for this association variable
     * For example, for expression "e.dpartment", the parent field is
     * field "department" of class Emlployee
     */
    private RuntimeMappingField parentField;

    /**
     * Parent path for this variable. It can be an instance of AssociationVariableData
     * For example, for expresion "e.department", parentPathData is
     * IdentificationVariableData corresponding to e.
     * For expresion e.department.company", parentPathData is AssociationVariableData
     * cooresponding to "e.department"
     */
    private IdentificationVariableData parentPathData;

    /**
     * The constrcuctor
     * @param mappingClass mapping class corresponding to this association variable
     * for expression "e.department", this is the mapping class corresponding to
     * Department
     * @param navigationId navigation id for this association variable for expression
     * "e.department", this is "e" + "department"  (Navigation id of e +
     * navigation id of department.
     */
    public AssociationVariableData(RuntimeMappingClass mappingClass,
            String navigationId, RuntimeMappingField parentField,
            IdentificationVariableData parentPathData) {
        super(mappingClass, navigationId);
        this.parentPathData  = parentPathData;
        this.parentField     = parentField;
    }

    /**
     * This method generates sql for expression of type association = ?1. The
     * generated sql is of the form parent.fk1 = ? and parent.fk2 = ?....
     * The method also adds parameters to parameter table for each of the '?'
     * above
     * @param selectPlan The selectPlan from which column text is obtained
     * @param paramData The parameter data for the parameter
     * @param sqlText The sql text for the comparison is returned here
     * @return Array of InputParameter for this comparison. Please note that
     * param sqlText also contains a return value
     */
    public InputParameter[] generateToParamComparison(SelectPlan selectPlan,
            ParameterData paramData, StringBuffer sqlText) {
        assert sqlText.length() == 0 : "This method initializes the sqlText" // NOI18N
                + "It must be empty"; // NOI18N

        RuntimeMappingClass parentMappingClass = parentPathData.getMappingClass();
        MappingRelationship relation = parentField.getMappingRelationship();
        MappingReferenceKey refKey = 
            relation.getMappingReferenceKey(MappingRelationship.USAGE_REFERENCE);
        ColumnPairElement[] c = refKey.getColumnPairs();

        assert c.length != 0 : "There is no column to join the relationship"
                + "Model validations should have caught this";  // NOI18N
        // Assume all local columns of a relationships are from the same table
        String parentTableName =
                c[0].getLocalColumn().getDeclaringTable().getName().getName();
        MappingTable parentMappingTable =
                parentMappingClass.getMappingTable(parentTableName);
        InputParameter[] inputParams = new InputParameter[c.length];
        // The pk fields are target field of the relationships
        // TODO: Relationship can be mapped to non pk fields also.
        // Meed to get the inversefield from model
        RuntimeMappingField[] pkFields = getMappingClass().getPrimaryKeyMappingFields();
        assert pkFields.length == c.length : "Model mismatch"; // NOI18N
        String positionMarker = paramData.positionMarker;
        for(int i = 0; i < c.length ; i++) {
            //--Prepare the Text
            if (i != 0) {
                //More than one pk field append and
                sqlText.append(" and "); //NOI18N
            }
            ColumnElement column = c[i].getLocalColumn();
            RuntimeMappingField pkField = pkFields[i];
            inputParams[i] = addIdFieldToParamComparison(selectPlan, pkField,
                    positionMarker, column, parentMappingTable, sqlText);
        }
        return inputParams;
    }

    private InputParameter addIdFieldToParamComparison(SelectPlan selectPlan,
            RuntimeMappingField pkField, String positionMarker,
            ColumnElement column, MappingTable parentMappingTable,
            StringBuffer sqlText) {
        StringBuffer columnText = selectPlan.getColumnText(
                parentPathData.getNavigationId(), parentMappingTable, column);
        sqlText.append(columnText).append(" = ").append("?"); // NOI18N

        //--Add to the parameter table
        // TODO: It is assumed that column elements and pk fields are in
        // the same order. This might not be correct. Check with the model
        // team on how best this can be implemented.
        InputParameter inputParam = new EntityUserInputParameter(positionMarker,
                pkField);
        return inputParam;
    }

}
