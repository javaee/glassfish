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
 
package com.sun.org.apache.jdo.tck.pc.company;

import java.math.BigDecimal;

import com.sun.org.apache.jdo.tck.util.DeepEquality;
import com.sun.org.apache.jdo.tck.util.EqualityHelper;

/**
 * This class represents a dental insurance carrier selection for a
 * particular <code>Employee</code>.
 */
public class DentalInsurance extends Insurance {

    private BigDecimal lifetimeOrthoBenefit;

    /** This is the JDO-required no-args constructor */
    protected DentalInsurance() {}

    /**
     * Initialize a <code>DentalInsurance</code> instance.
     * @param insid The insurance instance identifier.
     * @param carrier The insurance carrier.
     * @param lifetimeOrthoBenefit The lifetimeOrthoBenefit.
     */
    public DentalInsurance(long insid, String carrier, 
                           BigDecimal lifetimeOrthoBenefit) {
        super(insid, carrier);
        this.lifetimeOrthoBenefit = lifetimeOrthoBenefit;
    }

    /**
     * Initialize a <code>DentalInsurance</code> instance.
     * @param insid The insurance instance identifier.
     * @param carrier The insurance carrier.
     * @param employee The employee associated with this insurance.
     * @param lifetimeOrthoBenefit The lifetimeOrthoBenefit.
     */
    public DentalInsurance(long insid, String carrier, Employee employee,
                           BigDecimal lifetimeOrthoBenefit) {
        super(insid, carrier, employee);
        this.lifetimeOrthoBenefit = lifetimeOrthoBenefit;
    }

    /**
     * Get the insurance lifetimeOrthoBenefit.
     * @return The insurance lifetimeOrthoBenefit.
     */
    public BigDecimal getLifetimeOrthoBenefit() {
        return lifetimeOrthoBenefit;
    }

    /**
     * Set the insurance lifetimeOrthoBenefit.
     * @param lifetimeOrthoBenefit The insurance lifetimeOrthoBenefit.
     */
    public void setLifetimeOrthoBenefit(BigDecimal lifetimeOrthoBenefit) {
        this.lifetimeOrthoBenefit = lifetimeOrthoBenefit;
    }

    /** 
     * Returns <code>true</code> if all the fields of this instance are
     * deep equal to the coresponding fields of the specified Person.
     * @param other the object with which to compare.
     * @param helper EqualityHelper to keep track of instances that have
     * already been processed. 
     * @return <code>true</code> if all the fields are deep equal;
     * <code>false</code> otherwise.  
     * @throws ClassCastException if the specified instances' type prevents
     * it from being compared to this instance. 
     */
    public boolean deepCompareFields(DeepEquality other, 
                                     EqualityHelper helper) {
        DentalInsurance otherIns = (DentalInsurance)other;
        return super.deepCompareFields(otherIns, helper) &&
            helper.equals(lifetimeOrthoBenefit, 
                          otherIns.lifetimeOrthoBenefit);
    }
}

