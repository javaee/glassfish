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

import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.sun.org.apache.jdo.tck.util.DeepEquality;
import com.sun.org.apache.jdo.tck.util.EqualityHelper;

/**
 * This class represents an employee.
 */
public abstract class Employee extends Person {

    private Date             hiredate;
    private double           weeklyhours;
    private DentalInsurance  dentalInsurance;
    private MedicalInsurance medicalInsurance;
    private Department       department;
    private Department       fundingDept;
    private Employee         manager;
    private Employee         mentor;
    private Employee         protege;
    private Employee         hradvisor;
    private transient Set reviewedProjects = new HashSet(); // element-type is Project
    private transient Set projects = new HashSet();         // element-type is Project
    private transient Set team = new HashSet();             // element-type is Employee
    private transient Set hradvisees = new HashSet();       // element-type is Employee

    /** This is the JDO-required no-args constructor */
    protected Employee() {}

    /**
     * Initialize an <code>Employee</code> instance.
     * @param personid The identifier for the person.
     * @param firstname The first name of the employee.
     * @param lastname The last name of the employee.
     * @param middlename The middle name of the employee.
     * @param birthdate The birth date of the employee.
     * @param address The address of the employee.
     * @param hiredate The date that the employee was hired.
     */
    public Employee(long personid, String firstname, String lastname, 
                    String middlename, Date birthdate, Address address,
                    Date hiredate) {
        super(personid, firstname, lastname, middlename, birthdate, address);
        this.hiredate = hiredate;
    }

    /**
     * Get the date that the employee was hired.
     * @return The date the employee was hired.
     */
    public Date getHiredate() {
        return hiredate;
    }

    /**
     * Set the date that the employee was hired.
     * @param hiredate The date the employee was hired.
     */
    public void setHiredate(Date hiredate) {
        this.hiredate = hiredate;
    }

    /**
     * Get the weekly hours of the employee.
     * @return The number of hours per week that the employee works.
     */
    public double getWeeklyhours() {
        return weeklyhours;
    }

    /**
     * Set the number of hours per week that the employee works.
     * @param weeklyhours The number of hours per week that the employee
     * works. 
     */
    public void setWeeklyhours(double weeklyhours) {
        this.weeklyhours = weeklyhours;
    }

    /**
     * Get the reviewed projects.
     * @return The reviewed projects as an unmodifiable set.
     */
    public Set getReviewedProjects() {
        return Collections.unmodifiableSet(reviewedProjects);
    }

    /**
     * Add a reviewed project.
     * @param project A reviewed project.
     */
    public void addReviewedProjects(Project project) {
        reviewedProjects.add(project);
    }

    /**
     * Remove a reviewed project.
     * @param project A reviewed project.
     */
    public void removeReviewedProject(Project project) {
        reviewedProjects.remove(project);
    }

    /**
     * Set the reviewed projects for the employee.
     * @param reviewedProjects The set of reviewed projects.
     */
    public void setReviewedProjects(Set reviewedProjects) {
        // workaround: create a new HashSet, because fostore does not
        // support LinkedHashSet
        this.reviewedProjects = 
            (reviewedProjects != null) ? new HashSet(reviewedProjects) : null;
    }

    /**
     * Get the employee's projects.
     * @return The employee's projects are returned as an unmodifiable
     * set. 
     */
    public Set getProjects() {
        return Collections.unmodifiableSet(projects);
    }

    /**
     * Add a project for the employee.
     * @param project The project.
     */
    public void addProject(Project project) {
        projects.add(project);
    }

    /**
     * Remove a project from an employee's set of projects.
     * @param project The project.
     */
    public void removeProject(Project project) {
        projects.remove(project);
    }

    /**
     * Set the projects for the employee.
     * @param projects The set of projects of the employee.
     */
    public void setProjects(Set projects) {
        // workaround: create a new HashSet, because fostore does not
        // support LinkedHashSet
        this.projects = (projects != null) ? new HashSet(projects) : null;
    }
    
    /**
     * Get the dental insurance of the employee.
     * @return The employee's dental insurance.
     */
    public DentalInsurance getDentalInsurance() {
        return dentalInsurance;
    }

    /**
     * Set the dental insurance object for the employee.
     * @param dentalInsurance The dental insurance object to associate with
     * the employee. 
     */
    public void setDentalInsurance(DentalInsurance dentalInsurance) {
        this.dentalInsurance = dentalInsurance;
    }
    /**
     * Get the medical insurance of the employee.
     * @return The employee's medical insurance.
     */
    public MedicalInsurance getMedicalInsurance() {
        return medicalInsurance;
    }

    /**
     * Set the medical insurance object for the employee.
     * @param medicalInsurance The medical insurance object to associate
     * with the employee. 
     */
    public void setMedicalInsurance(MedicalInsurance medicalInsurance) {
        this.medicalInsurance = medicalInsurance;
    }

    /**
     * Get the employee's department.
     * @return The department associated with the employee.
     */
    public Department getDepartment() {
        return department;
    }

    /**
     * Set the employee's department.
     * @param department The department.
     */
    public void setDepartment(Department department) {
        this.department = department;
    }

    /**
     * Get the employee's funding department.
     * @return The funding department associated with the employee.
     */
    public Department getFundingDept() {
        return fundingDept;
    }

    /**
     * Set the employee's funding department.
     * @param department The funding department.
     */
    public void setFundingDept(Department department) {
        this.fundingDept = department;
    }

    /**
     * Get the employee's manager.
     * @return The employee's manager.
     */
    public Employee getManager() {
        return manager;
    }

    /**
     * Set the employee's manager.
     * @param manager The employee's manager.
     */
    public void setManager(Employee manager) {
        this.manager = manager;
    }

    /**
     * Get the employee's team.
     * @return The set of <code>Employee</code>s on this employee's team,
     * returned as an unmodifiable set. 
     */
    public Set getTeam() {
        return Collections.unmodifiableSet(team);
    }

    /**
     * Add an <code>Employee</code> to this employee's team.
     * This method sets both sides of the relationship, modifying
     * this employees team to include parameter emp and modifying
     * emp to set its manager attribute to this object.
     * @param emp The <code>Employee</code> to add to the team.
     */
    public void addToTeam(Employee emp) {
        team.add(emp);
        emp.manager = this;
    }

    /**
     * Remove an <code>Employee</code> from this employee's team.
     * This method will also set the <code>emp</code> manager to null.
     * @param emp The <code>Employee</code> to remove from the team.
     */
    public void removeFromTeam(Employee emp) {
        team.remove(emp);
        emp.manager = null;
    }

    /**
     * Set the employee's team.
     * @param team The set of <code>Employee</code>s.
     */
    public void setTeam(Set team) {
        // workaround: create a new HashSet, because fostore does not
        // support LinkedHashSet
        this.team = (team != null) ? new HashSet(team) : null;
    }

    /**
     * Set the mentor for this employee and also set the inverse protege
     * relationship. 
     * @param mentor The mentor for this employee.
     */
    public void setMentor(Employee mentor) {
        this.mentor = mentor;
        mentor.protege = this;
    }

    /**
     * Get the mentor for this employee.
     * @return The mentor.
     */
    public Employee getMentor() {
        return mentor;
    }

    /**
     * Get the protege of this employee.
     * @return The protege of this employee.
     */
    public Employee getProtege() {
        return protege;
    }

    /**
     * Get the HR advisor for the employee.
     * @return The HR advisor.
     */
    public Employee getHradvisor() {
        return hradvisor;
    }

    /**
     * Get the HR advisees of this HR advisor.
     * @return An unmodifiable <code>Set</code> containing the
     * <code>Employee</code>s that are HR advisees of this employee.
     */
    public Set getHradvisees() {
        return Collections.unmodifiableSet(hradvisees);
    }

    /**
     * Add an <code>Employee</code> as an advisee of this HR advisor. 
     * This method also sets the <code>emp</code> hradvisor to reference
     * this object. In other words, both sides of the relationship are
     * set. 
     * @param emp The employee to add as an advisee.
     */
    public void addAdvisee(Employee emp) {
        hradvisees.add(emp);
        emp.hradvisor = this;
    }

    /**
     * Remove an <code>Employee</code> as an advisee of this HR advisor.
     * This method also sets the <code>emp</code> hradvisor to null.
     * In other words, both sides of the relationship are set.
     * @param emp The employee to add as an HR advisee.
     */
    public void removeAdvisee(Employee emp) {
        hradvisees.remove(emp);
        emp.hradvisor = null;
    }

    /**
     * Set the HR advisees of this HR advisor.
     * @param hradvisees The <code>Employee</code>s that are HR advisees of
     * this employee. 
     */
    public void setHradvisees(Set hradvisees) {
        // workaround: create a new HashSet, because fostore does not
        // support LinkedHashSet
        this.hradvisees = (hradvisees != null) ? new HashSet(hradvisees) : null;
    }

    /** Serialization support: initialize transient fields. */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        reviewedProjects = new HashSet();
        projects = new HashSet();
        team = new HashSet();
        hradvisees = new HashSet();
    }

    /** 
     * Returns <code>true</code> if all the fields of this instance are
     * deep equal to the corresponding fields of the specified Employee.
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
        Employee otherEmp = (Employee)other;
        return super.deepCompareFields(otherEmp, helper) &&
            helper.equals(hiredate, otherEmp.hiredate) &&
            helper.closeEnough(weeklyhours, otherEmp.weeklyhours) &&
            helper.deepEquals(dentalInsurance, otherEmp.dentalInsurance) &&
            helper.deepEquals(medicalInsurance, otherEmp.medicalInsurance) &&
            helper.deepEquals(department, otherEmp.department) &&
            helper.deepEquals(fundingDept, otherEmp.fundingDept) &&
            helper.deepEquals(manager, otherEmp.manager) &&
            helper.deepEquals(mentor, otherEmp.mentor) &&
            helper.deepEquals(protege, otherEmp.protege) &&
            helper.deepEquals(hradvisor, otherEmp.hradvisor) &&
            helper.deepEquals(reviewedProjects, otherEmp.reviewedProjects) &&
            helper.deepEquals(projects, otherEmp.projects) &&
            helper.deepEquals(team, otherEmp.team) &&
            helper.deepEquals(hradvisees, otherEmp.hradvisees);
    }

}

