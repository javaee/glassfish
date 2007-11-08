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

package oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk;

import java.util.Vector;
import java.util.Collection;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name="CMP3_DEPARTMENT")
@IdClass(oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk.DepartmentPK.class)
public class Department {
    private String name;
    private String role;
    private String location;
    private Collection<Scientist> scientists;

    public Department() {
        scientists = new Vector<Scientist>();
    }

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Id
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Id
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @OneToMany(fetch=EAGER, mappedBy="department")
    @OrderBy // will default to Scientists composite pk.
    public Collection<Scientist> getScientists() {
        return scientists;
    }

    public void setScientists(Collection<Scientist> scientists) {
        this.scientists = scientists;
    }

    public Scientist addScientist(Scientist scientist) {
        scientists.add(scientist);
        scientist.setDepartment(this);
        return scientist;
    }

    public Scientist removeScientist(Scientist scientist) {
        scientists.remove(scientist);
        scientist.setDepartment(null);
        return scientist;
    }
    
    public DepartmentPK getPK() {
        return new DepartmentPK(name, role, location);
    }
}
