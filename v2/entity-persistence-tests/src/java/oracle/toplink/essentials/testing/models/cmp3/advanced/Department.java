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
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  


// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import java.util.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

/**
 * <p><b>Purpose</b>: Represents the department of an Employee
 * <p><b>Description</b>: Held in a private 1:1 relationship from Employee
 * @see Employee
 */
@Entity(name="ADV_DEPT")
@Table(name="CMP3_DEPT")
@NamedNativeQuery(
    name="findAllSQLDepartments", 
    query="select * from CMP3_DEPT",
    resultClass=oracle.toplink.essentials.testing.models.cmp3.advanced.Department.class
)
public class Department implements Serializable {
	private Integer id;
    private String name;
	private Collection<Employee> employees;
    private Collection<Employee> managers;

    public Department() {
        this("");
    }

    public Department(String name) {
        this.name = name;
        managers = new Vector();
    }

	@Id
    @GeneratedValue(strategy=TABLE, generator="DEPARTMENT_TABLE_GENERATOR")
	@TableGenerator(
        name="DEPARTMENT_TABLE_GENERATOR", 
        table="CMP3_DEPARTMENT_SEQ", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="DEPT_SEQ"
    )
	public Integer getId() { 
        return id; 
    }
    
	public void setId(Integer id) { 
        this.id = id; 
    }

	public String getName() { 
        return name; 
    }
    
	public void setName(String name) { 
        this.name = name; 
    }
    
	@OneToMany(fetch=EAGER, mappedBy="department")
	public Collection<Employee> getEmployees() { 
        return employees; 
    }
    
    public void setEmployees(Collection<Employee> employees) {
		this.employees = employees;
	}
    
    //To test default 1-M mapping
    @OneToMany(fetch=EAGER, cascade=PERSIST)
    public Collection<Employee> getManagers() {
        return managers;
    }

    public void setManagers(Collection<Employee> managers) {
        this.managers = managers;
    }
    
    public void addManager(Employee employee) {
        if (employee != null && managers != null && !managers.contains(employee)) { 
            this.managers.add(employee); 
        }
    }
}