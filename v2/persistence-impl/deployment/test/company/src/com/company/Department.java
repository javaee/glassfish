/*
 *
 * Created on June 13, 2005, 4:31 PM
 */

package com.company;

import javax.persistence.*;
import static javax.persistence.GeneratorType.*;
import static javax.persistence.AccessType.*;
import java.util.*;

/**
 *
 * @author Jie Leng
 * @author Markus Fuchs
 */
@Entity
public class Department {
    private long deptid;
    private String name;
    private Company company;
    private Set employees = new HashSet(); // element type is Employee

    @Id public long getDeptid() {
        return deptid;
    }
    public void setDeptid(long deptid) {
        this.deptid = deptid;
    }

    @Basic public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne()
    @JoinColumn(name="COMPANYID", referencedColumnName="COMPANYID", nullable=true)
    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }

    @OneToMany(targetEntity="com.company.Employee", mappedBy="department")
    public Set getEmployees() {
        return employees;
    }
    public void setEmployees(Set employees) {
        this.employees = employees;
    }
}
