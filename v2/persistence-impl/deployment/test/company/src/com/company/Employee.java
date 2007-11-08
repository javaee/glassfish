/*
 *
 * Created on June 28, 2005, 12:45 PM
 */

package com.company;

import javax.persistence.*;
import static javax.persistence.GeneratorType.*;
import static javax.persistence.AccessType.*;
import java.util.*;

/**
 *
 * @author Markus Fuchs
 */
@Entity(access=FIELD)
@Table(name="PERSON")
public class Employee { 
    @Id @Column(name="PERSONID", primaryKey=true)
    private long empid;
    @Basic private String firstname;
    @Basic private String lastname;
    @Basic private Date birthdate;
    @Basic private double weeklyhours;
    @Basic private char discriminator;

    @OneToOne(mappedBy="employee")
    private Insurance insurance;

    @ManyToOne()
    @JoinColumn(name="DEPTID", referencedColumnName="DEPTID", nullable=true)
    private Department department;

    @ManyToMany(targetEntity="com.company.Project", mappedBy="employees")
    private Set projects;

    public long getEmpid() {
        return empid;
    }
    public void setEmpid(long empid) {
        this.empid = empid;
    }

    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Date getBirthdate() {
        return birthdate;
    }
    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public double getWeeklyhours() {
        return weeklyhours;
    }
    public void setWeeklyhours(double weeklyhours) {
        this.weeklyhours = weeklyhours;
    }

    public char getDiscriminator() {
        return discriminator;
    }
    public void setDiscriminator(char discriminator) {
        this.discriminator = discriminator;
    }

    public Insurance getInsurance() {
        return insurance;
    }
    public void setInsurance(Insurance insurance) {
        this.insurance = insurance;
    }

    public Department getDepartment() {
        return department;
    }
    public void setDepartment(Department department) {
        this.department = department;
    }

    public Set getProjects() {
        return projects;
    }
    public void setProjects(Set projects) {
        this.projects = projects;
    }
}
