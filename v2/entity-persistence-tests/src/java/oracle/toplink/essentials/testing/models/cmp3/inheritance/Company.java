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


package oracle.toplink.essentials.testing.models.cmp3.inheritance;

import java.util.*;
import java.io.*;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;
import static javax.persistence.InheritanceType.*;

@Entity
@Table(name="CMP3_COMPANY")
public class Company implements Serializable {
    private Number id;
    private String name;
    private Collection<Vehicle> vehicles;
    private Set<Engineer> engineers;

    public Company() {
        vehicles = new Vector<Vehicle>();
    }

	@Id
    @GeneratedValue(strategy=TABLE, generator="COMPANY_TABLE_GENERATOR")
	@TableGenerator(
        name="COMPANY_TABLE_GENERATOR", 
        table="CMP3_INHERITANCE_SEQ", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="COMPANY_SEQ"
    )
    @Column(name="ID")
    public Number getId() {
        return id;
    }

	public void setId(Number id) { 
        this.id = id; 
    }

    @OneToMany(mappedBy="company")
    public Set<Engineer> getEngineers() {
        return engineers;
    }
    
    public void setEngineers(Set<Engineer> engineers) {
        this.engineers = engineers;
    }
    
	@OneToMany(cascade=ALL, mappedBy="owner")
    public Collection<Vehicle> getVehicles() {
        return vehicles;
    }

	public void setVehicles(Collection<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	@Column(name="NAME")
    public String getName() {
        return name;
    }

    public void setName(String aName) {
        name = aName;
    }
}