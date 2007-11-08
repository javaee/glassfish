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
package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.io.Serializable;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="CMP3_VEGETABLE")
public class Vegetable implements Serializable {
    private VegetablePK id;       
    private double cost;
    private String[] tags;
    
    public Vegetable() {}

    public boolean equals(Object otherVegetable) {
        if (otherVegetable instanceof Vegetable) {
            return getId().equals(((Vegetable) otherVegetable).getId());
        }
        
        return false;
    }

    public double getCost() {
        return cost;
    }
    
    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(name="name", column=@Column(name="vegetable_name")),
        @AttributeOverride(name="color", column=@Column(name="vegetable_color"))
    })
    public VegetablePK getId() {
        return id;
    }
    
    public int hashCode() {
        int hash = 0;
        hash += (this.getId() != null ? this.getId().hashCode() : 0);
        return hash;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
    
    public void setId(VegetablePK id) {
        this.id = id;
    }
    
    public String[] getTags() {
        return this.tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
    
    public String toString() {
        return "Vegetable[id=" + getId() + "]";
    }
}
