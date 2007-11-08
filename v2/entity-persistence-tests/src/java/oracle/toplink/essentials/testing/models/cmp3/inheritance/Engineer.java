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

import static javax.persistence.CascadeType.*;
import javax.persistence.*;
import static javax.persistence.InheritanceType.*;
import java.util.List;

@Entity
@Table(name="CMP3_ENGINEER")
@DiscriminatorValue("E")
public class Engineer extends Person {
    private String title;
    private Company company;
    private List<Laptop> laptops;
    private List<Desktop> desktops;

    @ManyToOne
    public Company getCompany() {
        return company;
    }
    
    @Column(name="TITLE")
    public String getTitle() {
        return title;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    @ManyToMany(cascade={PERSIST, MERGE})
    @JoinTable(
        name="CMP3_ENGINEER_LAPTOP",
        joinColumns=@JoinColumn(name="ENGINEER_ID", referencedColumnName="ID"),
        inverseJoinColumns={
            @JoinColumn(name="LAPTOP_MFR", referencedColumnName="MFR"),
            @JoinColumn(name="LAPTOP_SNO", referencedColumnName="SNO")
        }
    )
    public List<Laptop> getLaptops() {
        return laptops;
    }

    public void setLaptops(List<Laptop> laptops) {
        this.laptops = laptops;
    }
    
    @ManyToMany(cascade={PERSIST, MERGE})
    @JoinTable(
        name="CMP3_ENGINEER_DESKTOP",
        joinColumns=@JoinColumn(name="ENGINEER_ID", referencedColumnName="ID"),
        inverseJoinColumns={
            @JoinColumn(name="DESKTOP_MFR", referencedColumnName="MFR"),
            @JoinColumn(name="DESKTOP_SNO", referencedColumnName="DT_SNO")
        }
    )
    public List<Desktop> getDesktops() {
        return desktops;
    }

    public void setDesktops(List<Desktop> desktops) {
        this.desktops = desktops;
    }
}
