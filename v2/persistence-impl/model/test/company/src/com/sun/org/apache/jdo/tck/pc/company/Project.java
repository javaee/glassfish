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
import java.util.Set;
import java.util.HashSet;
import java.math.BigDecimal;

import com.sun.org.apache.jdo.tck.util.DeepEquality;
import com.sun.org.apache.jdo.tck.util.EqualityHelper;

/**
 * This class represents a project, a budgeted task with one or more
 * employees working on it.
 */
public class Project 
    implements Serializable, Comparable, DeepEquality  {

    private long       projid;
    private String     name;
    private BigDecimal budget;
    private transient Set reviewers = new HashSet(); // element type is Employee
    private transient Set members = new HashSet();   // element type is Employee

    /** This is the JDO-required no-args constructor. */
    protected Project() {}

    /**
     * Initialize a project.
     * @param projid The project identifier.
     * @param name The name of the project.
     * @param budget The budget for the project.
     */
    public Project(long projid, String name, BigDecimal budget) {
        this.projid = projid;
        this.name = name;
        this.budget = budget;
    }

    /**
     * Get the project ID.
     * @return The project ID.
     */
    public long getProjid() {
        return projid;
    }

    /**
     * Get the name of the project.
     * @return The name of the project.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the project.
     * @param name The name of the project.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the project's budget.
     * @return The project's budget.
     */
    public BigDecimal getBudget() {
        return budget;
    }

    /**
     * Set the project's budget.
     * @param budget The project's budget.
     */
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    /**
     * Get the reviewers associated with this project.
     */
    public Set getReviewers() {
        return Collections.unmodifiableSet(reviewers);
    }

    /**
     * Add a reviewer to the project.
     * @param emp The employee to add as a reviewer.
     */
    public void addReviewer(Employee emp) {
        reviewers.add(emp);
    }

    /**
     * Remove a reviewer from the project.
     * @param emp The employee to remove as a reviewer of this project.
     */
    public void removeReviewer(Employee emp) {
        reviewers.remove(emp);
    }

    /**
     * Set the reviewers associated with this project.
     * @param reviewers The set of reviewers to associate with this project.
     */
    public void setReviewers(Set reviewers) {
        // workaround: create a new HashSet, because fostore does not
        // support LinkedHashSet
        this.reviewers = (reviewers != null) ? new HashSet(reviewers) : null;
    }

    /**
     * Get the project members.
     * @return The members of the project is returned as an unmodifiable
     * set of <code>Employee</code>s. 
     */
    public Set getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Add a new member to the project.
     * @param emp The employee to add to the project.
     */
    public void addMember(Employee emp) {
        members.add(emp);
    }

    /**
     * Remove a member from the project.
     * @param emp The employee to remove from the project.
     */
    public void removeMember(Employee emp) {
        members.remove(emp);
    }

    /**
     * Set the members of the project.
     * @param employees The set of employees to be the members of this
     * project. 
     */
    public void setMembers(Set employees) {
        // workaround: create a new HashSet, because fostore does not
        // support LinkedHashSet
        this.members = (members != null) ? new HashSet(employees) : null;
    }

    /** Serialization support: initialize transient fields. */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        reviewers = new HashSet();
        members = new HashSet();
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
        Project otherProject = (Project)other;
        return (projid == otherProject.projid) &&
            helper.equals(name, otherProject.name) &&
            helper.equals(budget, otherProject.budget) &&
            helper.deepEquals(reviewers, otherProject.reviewers) &&
            helper.deepEquals(members, otherProject.members);
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
        return compareTo((Project)o);
    }

    /** 
     * Compares this object with the specified Project object for
     * order. Returns a negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the specified
     * object.  
     * @param other The Project object to be compared. 
     * @return a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified Project
     * object. 
     */
    public int compareTo(Project other) {
        long otherId = other.projid;
        return (projid < otherId ? -1 : (projid == otherId ? 0 : 1));
    }
    
    
    /** 
     * Indicates whether some other object is "equal to" this one.
     * @param obj the object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     * argument; <code>false</code> otherwise. 
     */
    public boolean equals(Object obj) {
        if (obj instanceof Project) {
            return compareTo((Project)obj) == 0;
        }
        return false;
    }
        
    /**
     * Returns a hash code value for the object. 
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return (int)projid;
    }

    /**
     * This class is used to represent the application identity
     * for the <code>Project</code> class.
     */
    public static class Oid implements Serializable, Comparable {

        /**
         * This field represents the identifier for the
         * <code>Project</code> class. It must match a field in the
         * <code>Project</code> class in both name and type. 
         */
        public long projid;
        
        /** The name of the class of the target object.
        */
        public static final String targetClassName = "org.apache.jdo.tck.pc.company.Projectt"; // NOI18N

        /**
         * The required public no-arg constructor.
         */
        public Oid() { }

        /**
         * Initialize the application identifier with a project ID.
         * @param projid The id of the project.
         */
        public Oid(long projid) {
            this.projid = projid;
        }
        
        public Oid(String s) { projid = Long.parseLong(justTheId(s)); }

        public String toString() { return getTargetClassName() + ": "  + projid;} // NOI18N

        /** */
        public boolean equals(java.lang.Object obj) {
            if( obj==null || !this.getClass().equals(obj.getClass()) )
                return( false );
            Oid o = (Oid) obj;
            if( this.projid != o.projid ) return( false );
            return( true );
        }

        /** */
        public int hashCode() {
            return( (int) projid );
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
            if( projid < other.projid ) return -1;
            if( projid > other.projid ) return 1;
            return 0;
        }

    }

}

