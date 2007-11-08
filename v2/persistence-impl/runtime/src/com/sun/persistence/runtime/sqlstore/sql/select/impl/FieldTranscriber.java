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

import java.sql.ResultSet;

/**
 * Transcriber that knows how to fetch an object field from database
 * @author Mitesh Meswani
 */
public interface FieldTranscriber {
    /**
     * Get object result from the given resultset for give resultColumn
     * @param rs The given result set
     * @param resultColumn The given result column
     * @return Object result from the given resultset
     */
    Object getResult(ResultSet rs, ResultColumnElement resultColumn);
}
