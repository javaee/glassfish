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


package com.sun.persistence.runtime.query.impl;

import javax.persistence.Query.TemporalType;

import com.sun.persistence.runtime.query.TemporalParameter;

/**
 * Represents the result of setting a Date or Calendar parameter on a Query,
 * with a given TemporalType.
 * @author Dave Bristor
 */
// Tested by EJBQLQueryImplTest
public class TemporalParameterImpl implements TemporalParameter {

    /**
     * Will be either a java.util.Date or a java.util.Calendar, depending on
     * value given to Query.setParameter.
     */
    private final Object value;

    /**
     * Value given via Query.setParameter.
     */
    private final TemporalType temporalType;

    public TemporalParameterImpl(Object value, TemporalType temporalType) {
        this.value = value;
        this.temporalType = temporalType;
    }

    /** {@inheritDoc} */
    public Object getValue() {
        return value;
    }

    /** {@inheritDoc} */
    public TemporalType getTemporalType() {
        return temporalType;
    }   
}
