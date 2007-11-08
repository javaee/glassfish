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
import javax.persistence.Embeddable;

@Embeddable
public class VegetablePK implements Serializable {
    private String name;
    private String color;
    
    public VegetablePK() {}

    public VegetablePK(String name, String color) {
        setName(name);
        setColor(color);
    }

    public boolean equals(Object otherVegetablePK) {
        if (otherVegetablePK instanceof VegetablePK) {
            if (! getName().equals(((VegetablePK) otherVegetablePK).getName())) {
                return false;
            }
            
            return ( getColor().equals(((VegetablePK) otherVegetablePK).getColor()));
        }
        
        return false;
    }
    
    public String getColor() {
        return color;
    }
    
    public String getName() {
        return name;
    }
    
    public int hashCode() {
        int hash = 0;
        hash += (this.getName() != null ? this.getName().hashCode() : 0);
        hash += (this.getColor() != null ? this.getColor().hashCode() : 0);
        return hash;
    }

    public void setColor(String color) {
        this.color = color;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "VegetablePK [id=" + getName() + " - " + getColor() + "]";
    }
}
