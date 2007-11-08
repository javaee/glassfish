// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.inheritance;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;

import static javax.persistence.InheritanceType.JOINED;

@Entity
@Table(name="CMP3_OFFROAD_TIRE")
@Inheritance(strategy=JOINED)
@DiscriminatorValue("Offroad")
public class OffRoadTireInfo extends TireInfo {
    protected String code;
    protected String name;

    public OffRoadTireInfo() {}

    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
