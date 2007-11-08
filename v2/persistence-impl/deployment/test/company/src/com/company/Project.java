/*
 *
 * Created on June 28, 2005, 3:15 PM
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
@Entity
public class Project {
    private long projid;
    private String name;
    private Set employees = new HashSet(); // element type is Employee

    @Id public long getProjid() {
        return projid;
    }
    public void setProjid(long projid) {
        this.projid = projid;
    }

    @Basic public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @ManyToMany(targetEntity="com.company.Employee") 
    @AssociationTable(table=@Table(name="PROJECTPERSON"), 
                      joinColumns=@JoinColumn(name="PROJID", referencedColumnName="PROJID"),
                      inverseJoinColumns= @JoinColumn(name="PERSONID", referencedColumnName="PERSONID")) 
    public Set getEmployees() {
        return employees;
    }
    public void setEmployees(Set employees) {
        this.employees = employees;
    }
}
