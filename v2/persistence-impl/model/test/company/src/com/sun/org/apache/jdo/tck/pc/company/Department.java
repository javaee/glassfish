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
import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sun.org.apache.jdo.tck.util.DeepEquality;
import com.sun.org.apache.jdo.tck.util.EqualityHelper;

/**
 * This class represents a department within a company.
 */
public class Department
    implements Serializable, Comparable, DeepEquality {

    private long    deptid;
    private String  name;
    private Company company;
    private Employee employeeOfTheMonth;
    private transient Set employees = new HashSet(); // element type is Employee
    private transient Set fundedEmps = new HashSet(); // element type is Employee

    /** This is the JDO-required no-args constructor */
    protected Department() {}

    /**
     * Initialize a <code>Department</code> instance.
     * @param deptid The department id.
     * @param name The name of the department.
     */
    public Department(long deptid, String name) {
        this.deptid = deptid;
        this.name = name;
    }

    /**
     * Initialize a <code>Department</code> instance.
     * @param deptid The department id.
     * @param name The name of the department.
     * @param company The company that the department is associated with. 
     */
    public Department(long deptid, String name, Company company) {
        this.deptid = deptid;
        this.name = name;
        this.company = company;
    }

    /**
     * Initialize a <code>Department</code> instance.
     * @param deptid The department id.
     * @param name The name of the department.
     * @param company The company that the department is associated with.
     * @param employeeOfTheMonth The employee of the month the
     * department is associated with.
     */
    public Department(long deptid, String name, Company company, 
                      Employee employeeOfTheMonth) {
        this.deptid = deptid;
        this.name = name;
        this.company = company;
        this.employeeOfTheMonth = employeeOfTheMonth;
    }

    /**
     * Get the department id.
     * @return The department id.
     */
    public long getDeptid() {
        return deptid;
    }

    /**
     * Get the name of the department.
     * @return The name of the department.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the department.
     * @param name The name to set for the department.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the company associated with the department.
     * @return The company.
     */
    public Company getCompany() {
        return company;
    }

    /**
     * Set the company for the department.
     * @param company The company to associate with the department.
     */
    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     * Get the employee of the month associated with the department.
     * @return The employee of the month.
     */
    public Employee getEmployeeOfTheMonth() {
        return employeeOfTheMonth;
    }

    /**
     * Set the employee of the month for the department.
     * @param employeeOfTheMonth The employee of the month to
     * associate with the department. 
     */
    public void setEmployeeOfTheMonth(Employee employeeOfTheMonth) {
        this.employeeOfTheMonth = employeeOfTheMonth;
    }

    /**
     * Get the employees in the department as an unmodifiable set.
     * @return The set of employees in the department, as an unmodifiable
     * set. 
     */
    public Set getEmployees() {
        return Collections.unmodifiableSet(employees);
    }

    /**
     * Add an employee to the department.
     * @param emp The employee to add to the department.
     */
    public void addEmployee(Employee emp) {
        employees.add(emp);
    }

    /**
     * Remove an employee from the department.
     * @param emp The employee to remove from the department.
     */
    public void removeEmployee(Employee emp) {
        employees.remove(emp);
    }

    /**
     * Set the employees to be in this department.
     * @param employees The set of employees for this department.
     */
    public void setEmployees(Set employees) {
        // workaround: create a new HashSet, because fostore does not
        // support LinkedHashSet
        this.employees = (employees != null) ? new HashSet(employees) : null;
    }

    /**
     * Get the funded employees in the department as an unmodifiable set.
     * @return The set of funded employees in the department, as an
     * unmodifiable set. 
     */
    public Set getFundedEmps() {
        return Collections.unmodifiableSet(fundedEmps);
    }

    /**
     * Add an employee to the collection of funded employees of this
     * department. 
     * @param emp The employee to add to the department.
     */
    public void addFundedEmp(Employee emp) {
        fundedEmps.add(emp);
    }

    /**
     * Remove an employee from collection of funded employees of this
     * department. 
     * @param emp The employee to remove from the department.
     */
    public void removeFundedEmp(Employee emp) {
        fundedEmps.remove(emp);
    }

    /**
     * Set the funded employees to be in this department.
     * @param employees The set of funded employees for this department. 
     */
    public void setFundedEmps(Set employees) {
        // workaround: create a new HashSet, because fostore does not
        // support LinkedHashSet
        this.fundedEmps = (fundedEmps != null) ? new HashSet(employees) : null;
    }

    /** Serialization support: initialize transient fields. */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        employees = new HashSet();
        fundedEmps = new HashSet();
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
        Department otherDept = (Department)other;
        return (deptid == otherDept.deptid) && 
            helper.equals(name, otherDept.name) &&
            helper.deepEquals(company, otherDept.company) &&
            helper.deepEquals(employeeOfTheMonth, otherDept.employeeOfTheMonth) &&
            helper.deepEquals(employees, otherDept.employees) &&
            helper.deepEquals(fundedEmps, otherDept.fundedEmps);
    }
    
    /** 
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. 
     * @param o The Object to be compared. 
     * @return a negative integer, zero, or a positive integer as this 
     * object is less than, equal to, or greater than the specified object. 
     * @throws ClassCastException - if the specified object's type prevents
     * it from being compared to this Object. 
     */
    public int compareTo(Object o) {
        return compareTo((Department)o);
    }

    /** 
     * Compares this object with the specified Department object for
     * order. Returns a negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the specified
     * object.  
     * @param other The Department object to be compared. 
     * @return a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified
     * Department object. 
     */
    public int compareTo(Department other) {
        long otherId = other.deptid;
        return (deptid < otherId ? -1 : (deptid == otherId ? 0 : 1));
    }
    
    /** 
     * Indicates whether some other object is "equal to" this one.
     * @param obj the object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     * argument; <code>false</code> otherwise. 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Department) {
            return compareTo((Department)obj) == 0;
        }
        return false;
    }
        
    /**
     * Returns a hash code value for the object. 
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return (int)deptid;
    }

    /**
     * The application identity class associated with the
     * <code>Department</code> class. 
     */
    public static class Oid implements Serializable, Comparable {

        /**
         * This field represents the application identifier field 
         * for the <code>Department</code> class. 
         * It must match in name and type with the field in the
         * <code>Department</code> class. 
         */
        public long deptid;

        /**
         * The required public, no-arg constructor.
         */
        public Oid() { }
        
        /** The name of the class of the target object.
        */
        public static final String targetClassName = "org.apache.jdo.tck.pc.company.Department"; // NOI18N

        /**
         * A constructor to initialize the identifier field.
         * @param deptid the deptid of the Department.
         */
        public Oid(long deptid) {
            this.deptid = deptid;
        }
        
        public Oid(String s) { deptid = Long.parseLong(justTheId(s)); }

        public String toString() { return getTargetClassName() + ": "  + deptid;} // NOI18N

        /** */
        public boolean equals(java.lang.Object obj) {
            if( obj==null || !this.getClass().equals(obj.getClass()) )
                return( false );
            Oid o = (Oid) obj;
            if( this.deptid != o.deptid ) return( false );
            return( true );
        }

        /** */
        public int hashCode() {
            return( (int) deptid );
        }
        
        protected static String justTheId(String str) {
            return str.substring(str.indexOf(':') + 1);
        }
        
        /** Return the target class name.
         * @return the target class name.
         */
        public String getTargetClassName() {
            return targetClassName;
        } 

        /** */
        public int compareTo(Object obj) {
            // may throw ClassCastException which the user must handle
            Oid other = (Oid) obj;
            if( deptid < other.deptid ) return -1;
            if( deptid > other.deptid ) return 1;
            return 0;
        }

    }

}

