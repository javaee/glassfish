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

import static javax.persistence.GenerationType.TABLE;
import javax.persistence.*;
import java.util.Collection;

/**
 * Composite Key Entity.
 * 
 * @author Wonseok Kim
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "DDL_CKENTB", 
    uniqueConstraints = { 
        @UniqueConstraint(columnNames={"UNQ2, UNQ1"}) //The order of columns shoud not be changed. See CKeyEntityA. 
})
public class CKeyEntityB {
    @EmbeddedId
    private CKeyEntityBPK key;

    @Column(name = "UNQ1", nullable = false)
    private String unq1;

    @Column(name = "UNQ2", nullable = false)
    private String unq2;
    

    @OneToMany(mappedBy="bs")
    private Collection<CKeyEntityA> as;
    
    @ManyToMany(mappedBy="bs")
    private Collection<CKeyEntityC> cs;

    @OneToOne(mappedBy="uniqueB")
    private CKeyEntityA uniqueA;

    public CKeyEntityB() {
    }

    public CKeyEntityB(CKeyEntityBPK key) {
        this.key = key;
    }

    public CKeyEntityBPK getKey() {
        return key;
    }

    public void setKey(CKeyEntityBPK key) {
        this.key = key;
    }

    public String getUnq1() {
        return unq1;
    }

    public void setUnq1(String unq1) {
        this.unq1 = unq1;
    }

    public String getUnq2() {
        return unq2;
    }

    public void setUnq2(String unq2) {
        this.unq2 = unq2;
    }

    public Collection<CKeyEntityA> getAs() {
        return as;
    }

    public void setAs(Collection<CKeyEntityA> as) {
        this.as = as;
    }

    public Collection<CKeyEntityC> getCs() {
        return cs;
    }

    public void setCs(Collection<CKeyEntityC> cs) {
        this.cs = cs;
    }

    public CKeyEntityA getUniqueA() {
        return uniqueA;
    }

    public void setUniqueA(CKeyEntityA uniqueA) {
        this.uniqueA = uniqueA;
    }
}
