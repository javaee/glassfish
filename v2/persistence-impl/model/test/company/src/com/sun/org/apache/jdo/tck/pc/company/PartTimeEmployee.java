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

import java.util.Date;

import com.sun.org.apache.jdo.tck.util.DeepEquality;
import com.sun.org.apache.jdo.tck.util.EqualityHelper;

/**
 * This class represents a part-time employee.
 */
public class PartTimeEmployee extends Employee {
    private double wage;

    /** This is the JDO-required no-args constructor. */
    protected PartTimeEmployee() {}

    /**
     * Initialize a part-time employee.
     * @param personid The identifier for the person.
     * @param first The person's first name.
     * @param last The person's last name.
     * @param middle The person's middle name.
     * @param born The person's birthdate.
     * @param addr The person's address.
     * @param hired The date the person was hired.
     * @param wage The person's wage.
     */
    public PartTimeEmployee(long personid, String first, String last,
                            String middle, Date born, Address addr, 
                            Date hired, double wage ) {
        super(personid, first, last, middle, born, addr, hired);
        this.wage = wage;
    }

    /**
     * Get the wage of the part-time employee.
     * @return The wage of the part-time employee.
     */
    public double getWage() {
        return wage;
    }

    /**
     * Set the wage of the part-time employee.
     * @param wage The wage of the part-time employee.
     */
    public void setWage(double wage) {
        this.wage = wage;
    }

    /** */
    public String toString() {
        StringBuffer rc = new StringBuffer("PartTimeEmployee: "); // NOI18N
        rc.append(super.toString());
        rc.append(" $" + wage); // NOI18N
        return rc.toString();
    }

    /** 
     * Returns <code>true</code> if all the fields of this instance are
     * deep equal to the coresponding fields of the specified
     * PartTimeEmployee. 
     * @param other the object with which to compare.
     * @param helper EqualityHelper to keep track of instances that have
     * already been processed. 
     * @return <code>true</code> if all the fields are deep equal;
     * <code>false</code> otherwise.  
     * @throws ClassCastException if the specified instances' type prevents
     * it from being compared to this instance. 
     */
    public boolean deepCompareFields(PartTimeEmployee other, 
                                        EqualityHelper helper) {
        PartTimeEmployee otherEmp = (PartTimeEmployee)other;
        return super.deepCompareFields(otherEmp, helper) &&
            helper.closeEnough(wage, otherEmp.wage);
    }
}
