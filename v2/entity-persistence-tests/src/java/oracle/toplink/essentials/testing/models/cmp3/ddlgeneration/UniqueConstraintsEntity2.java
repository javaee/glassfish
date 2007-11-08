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
package oracle.toplink.essentials.testing.models.cmp3.ddlgeneration;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Id;
import javax.persistence.Column;
import java.io.Serializable;

/**
 * Unique constraints test model. Unique constraints are given by XML descriptor. 
 */
@Entity
// Followings are given by XML descriptor.
//@Table(name="DDL_UCENTITY2", uniqueConstraints = {
//    @UniqueConstraint(columnNames={"column2"}),
//    @UniqueConstraint(columnNames={"column31", "column32"})
//})
public class UniqueConstraintsEntity2 implements Serializable {

    @Id
    private Integer id;

    private Integer column1;
    private Integer column2;
    private Integer column31;
    private Integer column32;

    public UniqueConstraintsEntity2() {
    }

    public UniqueConstraintsEntity2(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public Integer getColumn1() {
        return column1;
    }

    public void setColumn1(Integer column1) {
        this.column1 = column1;
    }

    public Integer getColumn2() {
        return column2;
    }

    public void setColumn2(Integer column2) {
        this.column2 = column2;
    }

    public Integer getColumn31() {
        return column31;
    }

    public void setColumn31(Integer column31) {
        this.column31 = column31;
    }

    public Integer getColumn32() {
        return column32;
    }

    public void setColumn32(Integer column32) {
        this.column32 = column32;
    }

    public void setColumns(Integer col1, Integer col2, Integer col31, Integer col32) {
        setColumn1(col1);
        setColumn2(col2);
        setColumn31(col31);
        setColumn32(col32);
    }
}
