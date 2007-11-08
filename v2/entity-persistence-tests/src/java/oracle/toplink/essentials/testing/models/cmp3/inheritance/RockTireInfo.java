// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.inheritance;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="CMP3_ROCK_TIRE")
@DiscriminatorValue("Rock")
public class RockTireInfo extends OffRoadTireInfo {
    public enum Grip { REGULAR, SUPER, MEGA }
    
    protected Grip grip;

    public RockTireInfo() {}

    public Grip getGrip() {
        return grip;
    }

    public void setGrip(Grip grip) {
        this.grip = grip;
    }
}
