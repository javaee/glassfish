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

/**
 * Basic implementation of a result element
 * @author Mitesh Meswani
 */
public abstract class ResultElementImpl implements ResultElement {
    /**
     * sqlText in select clause that corresponds to this result element Stored
     * here to facilitate construction of sql text while walking the query tree
     */
    protected String sqlText;

    ResultElementImpl(String sqlText) {
        this.sqlText = sqlText;
    }

    /**
     * @inheritDoc
     */
    public String getSQLText() {
        return sqlText;
    }
}
