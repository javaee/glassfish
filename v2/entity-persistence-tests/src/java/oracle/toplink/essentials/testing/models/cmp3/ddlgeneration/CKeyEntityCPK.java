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
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.TABLE;

/**
 * @author Wonseok Kim
 */
@Embeddable
public class CKeyEntityCPK {
    @GeneratedValue(strategy = TABLE, generator = "CKEYENTITY_TABLE_GENERATOR")
    @Column(name = "SEQ")
    public int seq;

    @Column(name = "ROLE")
    public String role;


    public CKeyEntityCPK() {
    }

    public CKeyEntityCPK(String role) {
        this.role = role;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CKeyEntityCPK that = (CKeyEntityCPK) o;

        return seq == that.seq && role.equals(that.role);
    }

    public int hashCode() {
        int result;
        result = seq;
        result = 31 * result + role.hashCode();
        return result;
    }
}
