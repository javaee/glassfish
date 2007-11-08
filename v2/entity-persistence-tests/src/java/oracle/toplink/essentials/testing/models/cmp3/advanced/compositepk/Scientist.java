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

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.OneToOne;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import static javax.persistence.GenerationType.*;
import javax.persistence.GeneratedValue;
import javax.persistence.SequenceGenerator;

@Entity
@Table(name="CMP3_SCIENTIST")
@IdClass(oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk.ScientistPK.class)
public class Scientist {
    private int idNumber;
    private String firstName;
    private String lastName;
    private Cubicle cubicle;
    private Department department;

    public Scientist() {}

    @Id
    @GeneratedValue(strategy=SEQUENCE, generator="SCIENTIST_SEQUENCE_GENERATOR")
	@SequenceGenerator(name="SCIENTIST_SEQUENCE_GENERATOR", sequenceName="SCIENTIST_SEQ", allocationSize=1)
    @Column(name="ID_NUMBER")
    public int getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(int idNumber) {
        this.idNumber = idNumber;
    }

    @Id
    @Column(name="F_NAME")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    @Id
    @Column(name="L_NAME")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="DEPT_NAME", referencedColumnName="NAME"),
        @JoinColumn(name="DEPT_ROLE", referencedColumnName="ROLE"),
        @JoinColumn(name="DEPT_LOCATION", referencedColumnName="LOCATION")
    })
    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
    
    @OneToOne
    @JoinColumns({
        @JoinColumn(name="CUBE_ID", referencedColumnName="ID"),
        @JoinColumn(name="CUBE_CODE", referencedColumnName="CODE")
    })
    public Cubicle getCubicle() {
        return cubicle;
    }

    public void setCubicle(Cubicle cubicle) {
        this.cubicle = cubicle;
    }
    
    public ScientistPK getPK() {
        return new ScientistPK(idNumber, firstName, lastName);
    }
}
