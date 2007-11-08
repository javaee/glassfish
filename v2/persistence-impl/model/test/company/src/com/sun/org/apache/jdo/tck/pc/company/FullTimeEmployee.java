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
 * This class represents a full-time employee.
 */
public class FullTimeEmployee extends Employee {

    private double  salary;

    /** This is the JDO-required no-args constructor */
    protected FullTimeEmployee() {}

    /**
     * Initialize a full-time employee.
     * @param personid The person identifier.
     * @param first The person's first name.
     * @param last The person's last name.
     * @param middle The person's middle name.
     * @param born The person's birthdate.
     * @param addr The person's address.
     * @param hired The date that the person was hired.
     * @param sal The salary of the full-time employee.
     */
    public FullTimeEmployee(long personid, String first, String last,
                            String middle, Date born, Address addr, 
                            Date hired, double sal) {
        super(personid, first, last, middle, born, addr, hired);
        salary = sal;
    }

    /**
     * Get the salary of the full time employee.
     * @return The salary of the full time employee.
     */
    public double getSalary() {
        return salary;
    }
    
    /**
     * Set the salary for the full-time employee.
     * @param salary The salary to set for the full-time employee.
     */
    public void setSalary(double salary) {
        this.salary = salary;
    }
    
    /** */
    public String toString() {
        StringBuffer rc = new StringBuffer("FullTimeEmployee: "); // NOI18N
        rc.append(super.toString());
        rc.append(" $" + salary); // NOI18N
        return rc.toString();
    }

    /** 
     * Returns <code>true</code> if all the fields of this instance are
     * deep equal to the coresponding fields of the specified
     * FullTimeEmployee. 
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
        FullTimeEmployee otherEmp = (FullTimeEmployee)other;
        return super.deepCompareFields(otherEmp, helper) &&
            helper.closeEnough(salary, otherEmp.salary);
    }
    
}
