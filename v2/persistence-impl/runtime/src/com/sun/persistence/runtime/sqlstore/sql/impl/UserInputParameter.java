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


package com.sun.persistence.runtime.sqlstore.sql.impl;

import com.sun.persistence.runtime.query.QueryInternal;

/**
 * Input parameter supplied by the user.
 * @author Mitesh Meswani
 */
public class UserInputParameter implements InputParameter {
    /**
     * The posistion marker of this parameter in user query.
     */
    String positionMarker;

    /**
     * Construct an instance of UserParameter
     * @param positionMarker The posistion marker of this parameter in user query.
     */
    public UserInputParameter(String positionMarker) {
        this.positionMarker = positionMarker;
    }

    /**
     * @inheritDoc
     */
    public Object getValue(QueryInternal userParams) {
        return userParams.getParameter(positionMarker);
    }

}
