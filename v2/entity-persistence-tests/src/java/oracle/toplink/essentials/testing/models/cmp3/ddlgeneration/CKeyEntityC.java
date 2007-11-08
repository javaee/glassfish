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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Collection;

/**
 * Composite Key Entity.
 * 
 * @author Wonseok Kim
 */
@Entity
@Table(name = "DDL_CKENTC")
public class CKeyEntityC {

    @EmbeddedId
    private CKeyEntityCPK key;
    
    // Test for GF#1392
    // If there is a same name column for the entity and many-to-many table, wrong pk constraint generated.
    @Column(name="C_ROLE")
    private String tempRole;

    @OneToOne
    @JoinColumns({
        @JoinColumn(name="A_SEQ", referencedColumnName = "SEQ"),
        @JoinColumn(name="A_L_NAME", referencedColumnName = "L_NAME"),
        @JoinColumn(name="A_F_NAME", referencedColumnName = "F_NAME")
    })
    private CKeyEntityA a;
    
    @ManyToMany
        @JoinTable(name="DDL_CKENT_C_B",
        joinColumns={
            @JoinColumn(name="C_SEQ", referencedColumnName="SEQ"),
            @JoinColumn(name="C_ROLE", referencedColumnName="ROLE")
        },
        inverseJoinColumns={
            @JoinColumn(name="B_SEQ", referencedColumnName = "SEQ"),
            @JoinColumn(name="B_CODE", referencedColumnName = "CODE")
        }
    )
    private Collection<CKeyEntityB> bs;


    public CKeyEntityC() {
    }

    public CKeyEntityC(CKeyEntityCPK key) {
        this.key = key;
    }

    public CKeyEntityCPK getKey() {
        return key;
    }

    public String getTempRole() {
        return tempRole;
    }

    public void setTempRole(String tempRole) {
        this.tempRole = tempRole;
    }

    public CKeyEntityA getA() {
        return a;
    }

    public void setA(CKeyEntityA a) {
        this.a = a;
    }

    public Collection<CKeyEntityB> getBs() {
        return bs;
    }

    public void setBs(Collection<CKeyEntityB> bs) {
        this.bs = bs;
    }
}
