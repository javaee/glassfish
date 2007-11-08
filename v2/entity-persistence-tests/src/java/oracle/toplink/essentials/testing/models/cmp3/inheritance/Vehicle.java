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

import java.io.*;
import oracle.toplink.essentials.tools.schemaframework.*;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;
import static javax.persistence.InheritanceType.*;

@Entity
@EntityListeners(oracle.toplink.essentials.testing.models.cmp3.inheritance.listeners.VehicleListener.class)
@Table(name="CMP3_VEHICLE")
@Inheritance(strategy=JOINED)
@DiscriminatorColumn(name="VEH_TYPE")
@DiscriminatorValue("V")
public abstract class Vehicle implements Serializable {
    private Number id;
    private Company owner;
    private Integer passengerCapacity;

    public Vehicle() {}

    public void change() {
        return;
    }

    public abstract String getColor();

	@Id
    @GeneratedValue(strategy=TABLE, generator="VEHICLE_TABLE_GENERATOR")
	@TableGenerator(
        name="VEHICLE_TABLE_GENERATOR", 
        table="CMP3_INHERITANCE_SEQ", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="VEHICLE_SEQ")
    @Column(name="ID")
    public Number getId() {
        return id;
    }

	@ManyToOne(cascade=PERSIST, fetch=LAZY)
	@JoinColumn(name="OWNER_ID", referencedColumnName="ID")
    public Company getOwner() {
        return owner;
    }

	@Column(name="CAPACITY")
    public Integer getPassengerCapacity() {
        return passengerCapacity;
    }

    /**
     * Return the view for Sybase.
     */
    public static ViewDefinition oracleView() {
        ViewDefinition definition = new ViewDefinition();

        definition.setName("AllVehicles");
        definition.setSelectClause("Select V.*, F.FUEL_CAP, F.FUEL_TYP, B.DESCRIP, B.DRIVER_ID, C.CDESCRIP" + " from VEHICLE V, FUEL_VEH F, BUS B, CAR C" + " where V.ID = F.ID (+) AND V.ID = B.ID (+) AND V.ID = C.ID (+)");

        return definition;
    }
    
    public abstract void setColor(String color);

    public void setId(Number id) { 
        this.id = id; 
    }
    
    public void setOwner(Company ownerCompany) {
        owner = ownerCompany;
    }
    
    public void setPassengerCapacity(Integer capacity) {
        passengerCapacity = capacity;
    }

    /**
     * Return the view for Sybase.
     */
    public static ViewDefinition sybaseView() {
        ViewDefinition definition = new ViewDefinition();

        definition.setName("AllVehicles");
        definition.setSelectClause("Select V.*, F.FUEL_CAP, F.FUEL_TYP, B.DESCRIP, B.DRIVER_ID, C.CDESCRIP" + " from VEHICLE V, FUEL_VEH F, BUS B, CAR C" + " where V.ID *= F.ID AND V.ID *= B.ID AND V.ID *= C.ID");

        return definition;
    }
    
    public String toString() {
        return oracle.toplink.essentials.internal.helper.Helper.getShortClassName(getClass()) + "(" + id + ")";
    }
}