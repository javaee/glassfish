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
package oracle.toplink.essentials.testing.models.cmp3.xml.advanced;

import java.util.*;
import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

/**
 * Bean class: EmployeeBean
 * Remote interface: Employee
 * Primary key class: EmployeePK
 * Home interface: EmployeeHome
 *
 * Employees have a one-to-many relationship with Employees through the 
 * managedEmployees attribute.
 * Addresses exist in one-to-one relationships with Employees through the
 * address attribute.
 * Employees have a many-to-many relationship with Projects through the
 * projects attribute.
 *  
 * Employee now has invalid annotation fields and data. This is done so that
 * we may test the XML/Annotation merging. Employee has been defined in the
 * XML, therefore, most annotations should not be processed. If they are, then
 * they will force an error, which means something is wrong with our merging.
 *  
 * The invalid annotations that should not be processed have _INVALID
 * appended to some annotation field member. Others will not have this,
 * which means they should be processed (their mappings are not defined in the
 * XML)
 */
public class Employee implements Serializable {
	private Integer id;
	private int version;
	private String firstName;
	private String lastName;
	private Address address;
	private Collection<PhoneNumber> phoneNumbers;
	private Collection<Project> projects;
	private int salary;
	private EmploymentPeriod period;
    private Collection<Employee> managedEmployees;
    private Employee manager;
    
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

	public Integer getId() { 
        return id; 
    }
    
	public void setId(Integer id) { 
        this.id = id; 
    }

	public int getVersion() { 
        return version; 
    }
    
	protected void setVersion(int version) {
		this.version = version;
	}

	public String getFirstName() { 
        return firstName; 
    }
    
	public void setFirstName(String name) { 
        this.firstName = name; 
    }

	public String getLastName() { 
        return lastName; 
    }
    
	public void setLastName(String name) { 
        this.lastName = name; 
    }

	public Address getAddress() { 
        return address; 
    }
    
	public void setAddress(Address address) {
		this.address = address;
	}

	public Collection<PhoneNumber> getPhoneNumbers() { 
        return phoneNumbers; 
    }
    
	public void setPhoneNumbers(Collection<PhoneNumber> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public Collection<Employee> getManagedEmployees() { 
        return managedEmployees; 
    }
    
	public void setManagedEmployees(Collection<Employee> managedEmployees) {
		this.managedEmployees = managedEmployees;
	}

	public Employee getManager() { 
        return manager; 
    }
    
	public void setManager(Employee manager) {
		this.manager = manager;
	}

	public Collection<Project> getProjects() { 
        return projects; 
    }
    
	public void setProjects(Collection<Project> projects) {
		this.projects = projects;
	}

	public int getSalary() { 
        return salary; 
    }
    
	public void setSalary(int salary) { 
        this.salary = salary; 
    }

	public EmploymentPeriod getPeriod() {
		return period;
	}
    
	public void setPeriod(EmploymentPeriod period) {
		this.period = period;
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

    public void removeManagedEmployee(Employee emp) {
        getManagedEmployees().remove(emp);
    }

    public void removePhoneNumber(PhoneNumber phone) {
        // Note that getPhoneNumbers() will not have a phone number identical to 
        // "phone", (because it's serialized) and this will take advantage of 
        // equals() in PhoneNumber to remove properly
        getPhoneNumbers().remove(phone);
    }

    public void removeProject(Project theProject) {
        getProjects().remove(theProject);
    }

    public String toString() {
        return "Employee: " + getId();
    }

    public String displayString() {
        StringBuffer sbuff = new StringBuffer();
        sbuff.append("Employee ").append(getId()).append(": ").append(getLastName()).append(", ").append(getFirstName()).append(getSalary());

        return sbuff.toString();
    }
    
    // These methods were added for testing purpose only - BUG 4349991
    
    // Static method should be ignored
    static public void getAbsolutelyNothing() {}
    
    // Get methods with parameters should be ignored
    public String getYourStringBack(String str) {
        return str;
    }
    
    // Get methods with no corresponding set method, should be ignored.
    // logs a warning though.
    public String getAnEmptyString() {
        return "";
    }
}
