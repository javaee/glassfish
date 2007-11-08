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

import java.io.Serializable;

import com.sun.org.apache.jdo.tck.util.DeepEquality;
import com.sun.org.apache.jdo.tck.util.EqualityHelper;

/**
 * This class represents a dental insurance carrier selection for a
 * particular <code>Employee</code>.
 */
public class MedicalInsurance extends Insurance {

    private String planType; // possible values: "PPO", "EPO", "NPO" // NOI18N

    /** This is the JDO-required no-args constructor */
    protected MedicalInsurance() {}

    /**
     * Initialize a <code>MedicalInsurance</code> instance.
     * @param insid The insurance instance identifier.
     * @param carrier The insurance carrier.
     * @param planType The planType.
     */
    public MedicalInsurance(long insid, String carrier, 
                            String planType)
    {
        super(insid, carrier);
        this.planType = planType;
    }

    /**
     * Initialize a <code>MedicalInsurance</code> instance.
     * @param insid The insurance instance identifier.
     * @param carrier The insurance carrier.
     * @param employee The employee associated with this insurance.
     * @param planType The planType.
     */
    public MedicalInsurance(long insid, String carrier, 
                            Employee employee, String planType)
    {
        super(insid, carrier, employee);
        this.planType = planType;
    }

    /**
     * Get the insurance planType.
     * @return The insurance planType.
     */
    public String getPlanType() {
        return planType;
    }

    /**
     * Set the insurance planType.
     * @param planType The insurance planType.
     */
    public void setPlanType(String planType) {
        this.planType = planType;
    }

    /**
     * Indicates whether some other object is "deep equal to" this one.
     * @param other the object with which to compare.
     * @param helper EqualityHelper to keep track of instances that have
     * already been processed. 
     * @return <code>true</code> if this object is deep equal to the
     * specified object; <code>false</code> otherwise. 
     */
    public boolean deepEquals(DeepEquality other, EqualityHelper helper) {
        if (this == other)
            return true;
        if (!(other instanceof MedicalInsurance))
            return false;
        if (helper.isProcessed(this))
            return true;
        helper.markProcessed(this);
        return deepCompareFields((MedicalInsurance)other, helper);
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
        MedicalInsurance otherIns = (MedicalInsurance)other;
        return super.deepCompareFields(otherIns, helper) &&
            helper.equals(planType, otherIns.planType);
    }
}

