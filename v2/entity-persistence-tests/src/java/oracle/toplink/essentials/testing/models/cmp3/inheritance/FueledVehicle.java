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

import javax.persistence.*;
import static javax.persistence.InheritanceType.*;

@Entity
@EntityListeners(oracle.toplink.essentials.testing.models.cmp3.inheritance.listeners.FueledVehicleListener.class)
@Table(name="CMP3_FUEL_VEH")
@DiscriminatorValue("F")
public class FueledVehicle extends Vehicle {
    private Integer fuelCapacity;
    private String description;
    private String fuelType;
    private String colour;

    public void change() {
        this.setPassengerCapacity(new Integer(100));
        this.setFuelType("HOT AIR");
    }
    
    @Column(name="COLOUR")
    public String getColor() {
        return colour;
    }
    
	@Column(name="DESCRIP")
    public String getDescription() {
        return description;
    }
    
    @Column(name="FUEL_CAP")
    public Integer getFuelCapacity() {
        return fuelCapacity;
    }

    @Column(name="FUEL_TYP")
    public String getFuelType() {
        return fuelType;
    }
    
    public void setColor(String colour) {
        this.colour = colour;
    }
    
    public void setDescription(String aDescription) {
        description = aDescription;
    }

    public void setFuelCapacity(Integer capacity) {
        fuelCapacity = capacity;
    }

    public void setFuelType(String type) {
        fuelType = type;
    }
}
