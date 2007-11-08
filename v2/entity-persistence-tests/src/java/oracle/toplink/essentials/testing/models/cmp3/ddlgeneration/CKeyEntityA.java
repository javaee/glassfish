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
package oracle.toplink.essentials.testing.models.cmp3.ddlgeneration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.util.Collection;

/**
 * Composite key Entity.
 * 
 * @author Wonseok Kim
 */
@Entity
@IdClass(CKeyEntityAPK.class)
@Table(name = "DDL_CKENTA")
@TableGenerator(
    name = "CKEYENTITY_TABLE_GENERATOR",
    table = "DDL_CKENT_SEQ",
    pkColumnName = "SEQ_NAME",
    valueColumnName = "SEQ_COUNT",
    pkColumnValue = "CKENT_SEQ"
)
public class CKeyEntityA {
    @Id
    @GeneratedValue(strategy = TABLE, generator = "CKEYENTITY_TABLE_GENERATOR")
    @Column(name = "SEQ")
    private int seq;

    @Id
    @Column(name = "F_NAME")
    private String firstName;

    @Id
    @Column(name = "L_NAME")
    private String lastName;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="B_SEQ", referencedColumnName = "SEQ"),
        @JoinColumn(name="B_CODE", referencedColumnName = "CODE")
    })
    private CKeyEntityB bs;
    
    @OneToOne(mappedBy="a")
    private CKeyEntityC c;
    
    // Relationship using candidate(unique) keys
    // For testing whether a generated FK constraint has reordered unique keys according to target table.
    // CKeyEntityB has unique constraint ("UNQ2", "UNQ1").
    @OneToOne
    @JoinColumns({
        @JoinColumn(name="B_UNQ1", referencedColumnName = "UNQ1"),
        @JoinColumn(name="B_UNQ2", referencedColumnName = "UNQ2")
    })
    private CKeyEntityB uniqueB;
    

    public CKeyEntityA() {
    }

    public CKeyEntityA(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getSeq() {
        return seq;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public CKeyEntityAPK getKey() {
        return new CKeyEntityAPK(seq, firstName, lastName);
    }
    
    public CKeyEntityB getBs() {
        return bs;
    }

    public void setBs(CKeyEntityB bs) {
        this.bs = bs;
    }

    public CKeyEntityC getC() {
        return c;
    }

    public void setC(CKeyEntityC c) {
        this.c = c;
    }

    public CKeyEntityB getUniqueB() {
        return uniqueB;
    }

    public void setUniqueB(CKeyEntityB uniqueB) {
        this.uniqueB = uniqueB;
    }
}
