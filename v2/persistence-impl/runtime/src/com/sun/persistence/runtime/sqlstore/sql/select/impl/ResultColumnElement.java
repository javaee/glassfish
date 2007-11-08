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

import com.sun.persistence.support.JDODataStoreException;

import java.sql.ResultSet;

/**
 * Column element of a ResultSet
 * @author Mitesh Meswani
 */
public interface ResultColumnElement {

    /**
     * Gets the result for this column as a boolean from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this column as a boolean from the give ResultSet
     * @throws JDODataStoreException if a jdbc error is encountered
     */
    public boolean getBooleanResult(ResultSet rs) throws JDODataStoreException;

    /**
     * Gets the result for this column as a char from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this column as a char from the given ResultSet
     * @throws JDODataStoreException if a jdbc error is encountered
     */
    public char getCharResult(ResultSet rs) throws JDODataStoreException;

    /**
     * Gets the result for this column as a byte from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this column as a byte from the given ResultSet
     * @throws JDODataStoreException if a jdbc error is encountered
     */
    public byte getByteResult(ResultSet rs) throws JDODataStoreException;

    /**
     * Gets the result for this column as a short from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this column as a short from the given ResultSet
     * @throws JDODataStoreException if a jdbc error is encountered
     */
    public short getShortResult(ResultSet rs) throws JDODataStoreException;

    /**
     * Gets the result for this column as a int from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this column as a int from the given ResultSet
     * @throws JDODataStoreException if a jdbc error is encountered
     */
    public int getIntResult(ResultSet rs) throws JDODataStoreException;

    /**
     * Gets the result for this column as a long from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this column as a long from the given ResultSet
     * @throws JDODataStoreException if a jdbc error is encountered
     */
    public long getLongResult(ResultSet rs) throws JDODataStoreException;

    /**
     * Gets the result for this column as a float from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this column as a float from the given ResultSet
     * @throws JDODataStoreException if a jdbc error is encountered
     */
    public float getFloatResult(ResultSet rs) throws JDODataStoreException;

    /**
     * Gets the result for this column as a double from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this column as a double from the given ResultSet
     * @throws JDODataStoreException if a jdbc error is encountered
     */
    public double getDoubleResult(ResultSet rs) throws JDODataStoreException;

    /**
     * Gets the result for this column as a String from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this column as a String from the given ResultSet
     * @throws JDODataStoreException if a jdbc error is encountered
     */
    public String getStringResult(ResultSet rs) throws JDODataStoreException;

    /**
     * Gets the result for this column as a Object from the given ResultSet
     * @param rs The ResultSet from which the result should be fetched
     * @return Result for this column as a Object from the given ResultSet
     * @throws JDODataStoreException if a jdbc error is encountered
     */
    public Object getObjectResult(ResultSet rs) throws JDODataStoreException;

}

