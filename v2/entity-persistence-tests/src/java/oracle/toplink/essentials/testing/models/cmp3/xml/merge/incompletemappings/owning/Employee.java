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
package oracle.toplink.essentials.testing.models.cmp3.xml.merge.incompletemappings.owning;

import java.util.*;
import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

/**
 *
 */
@Entity(name="XMLIncompleteMergeEmplyee")
@Table(name="CMP3_XML_MERGE_EMPLOYEE")
@PrimaryKeyJoinColumn(name="EMP_ID", referencedColumnName="EMP_ID")
public class Employee implements Serializable {
	private Integer id;
	private int version;
	private String firstName;
	private String lastName;
	private Address address;
	private Collection<PhoneNumber> phoneNumbers;
	private Collection<Project> projects;
    private Collection<Employee> managedEmployees;
    private Employee manager;
	private SecurityBadge securityBadge;
    
	public Employee () {
        this.phoneNumbers = new Vector<PhoneNumber>();
        this.projects = new Vector<Project>();
        this.managedEmployees = new Vector<Employee>();
	}
    
    public Employee(String firstName, String lastName){
        this();
        this.firstName = firstName;
        this.lastName = lastName;
    }

	@Id
    @GeneratedValue(strategy=TABLE, generator="XML_MERGE_EMPLOYEE_TABLE_GENERATOR")
	@TableGenerator(
        name="XML_MERGE_EMPLOYEE_TABLE_GENERATOR", 
        table="CMP3_XML_MERGE_EMPLOYEE_SEQ", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="XML_MERGE_EMPLOYEE_SEQ"
    )
    @Column(name="EMP_ID")
	public Integer getId() { 
        return id; 
    }
    
	public void setId(Integer id) { 
        this.id = id; 
    }

	@Version
	@Column(name="VERSION")
	public int getVersion() { 
        return version; 
    }
    
	protected void setVersion(int version) {
		this.version = version;
	}

    @Column(name="F_NAME")
	public String getFirstName() { 
        return firstName; 
    }
    
	public void setFirstName(String name) { 
        this.firstName = name; 
    }

    @Column(name="L_NAME")
	public String getLastName() { 
        return lastName; 
    }
    
	public void setLastName(String name) { 
        this.lastName = name; 
    }

	@ManyToOne(cascade=PERSIST, fetch=LAZY)
	@JoinColumn(name="ADDR_ID")
	public Address getAddress() { 
        return address; 
    }
    
	public void setAddress(Address address) {
		this.address = address;
	}

	@OneToMany(cascade=ALL, mappedBy="owner")
	public Collection<PhoneNumber> getPhoneNumbers() { 
        return phoneNumbers; 
    }
    
	public void setPhoneNumbers(Collection<PhoneNumber> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	// the @JoinColumn will be ignored, as it is partially defined in XML
	@OneToOne
	@JoinColumn(name="SECURITYBADGE_BADGE_ID", referencedColumnName="BADGE_ID")
	public SecurityBadge getSecurityBadge() {
        return securityBadge; 
    }
    
	public void setSecurityBadge(SecurityBadge securityBadge) { 
        this.securityBadge = securityBadge; 
    }
	
	@OneToMany(cascade=ALL, mappedBy="manager")
	public Collection<Employee> getManagedEmployees() { 
        return managedEmployees; 
    }
    
	public void setManagedEmployees(Collection<Employee> managedEmployees) {
		this.managedEmployees = managedEmployees;
	}

    // Not defined in the XML, this should get processed.
	@ManyToOne(cascade=PERSIST, fetch=LAZY)
	public Employee getManager() { 
        return manager; 
    }
    
	public void setManager(Employee manager) {
		this.manager = manager;
	}

	@ManyToMany(cascade=PERSIST)
    @JoinTable(
		name="CMP3_XML_MERGE_PROJ_EMP",
        // Default for the project side and specify for the employee side
        // Will test both defaulting and set values.
        joinColumns=@JoinColumn(name="EMPLOYEES_EMP_ID", referencedColumnName="EMP_ID")
		//inverseJoinColumns=@JoinColumn(name="PROJECTS_PROJ_ID", referencedColumnName="PROJ_ID")
	)
	public Collection<Project> getProjects() { 
        return projects; 
    }
    
	public void setProjects(Collection<Project> projects) {
		this.projects = projects;
	}

    public void addManagedEmployee(Employee emp) {
        getManagedEmployees().add(emp);
        emp.setManager(this);
    }

    public void addPhoneNumber(PhoneNumber phone) {
        phone.setOwner(this);
        getPhoneNumbers().add(phone);
    }

    public void addProject(Project theProject) {
        getProjects().add(theProject);
    }

    public String toString() {
        return "Employee: " + getId();
    }
}
