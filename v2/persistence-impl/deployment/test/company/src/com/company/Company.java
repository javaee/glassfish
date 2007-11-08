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
@Entity(access=FIELD)
public class Company { 
    @Id private long companyid;
    @Basic private String name;
    @Basic private Date founded;

    @OneToMany(targetEntity="com.company.Department", mappedBy="company")
    private Set departments = new HashSet(); // element type is Department

    public long getCompanyid() {
        return companyid;
    }
    public void setCompanyid(long companyid) {
        this.companyid = companyid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Date getFounded() {
        return founded;
    }
    public void setFounded(Date founded) {
        this.founded = founded;
    }

    public Set getDepartments() {
        return departments;
    }
    public void setDepartments(Set departments) {
        this.departments = departments;
    }
}
