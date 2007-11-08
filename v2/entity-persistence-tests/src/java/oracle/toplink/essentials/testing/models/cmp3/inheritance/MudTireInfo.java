// Copyright (c) 1998, 2006, Oracle. All rights reserved.
package oracle.toplink.essentials.testing.models.cmp3.inheritance;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Embedded;
import javax.persistence.Table;

@Entity
@Table(name="CMP3_MUD_TIRE")
@DiscriminatorValue("Mud")
public class MudTireInfo extends OffRoadTireInfo {
    protected TireRating tireRating;
    protected int treadDepth;

    public MudTireInfo() {}

	@Column(name="TREAD_DEPTH")
    public int getTreadDepth() {
        return treadDepth;
    }

    public void setTreadDepth(int treadDepth) {
        this.treadDepth = treadDepth;
    }
    
    @Embedded
    public TireRating getTireRating() {
        return tireRating;
    }
    
    public void setTireRating(TireRating rating) {
        this.tireRating = rating;
    }
}
