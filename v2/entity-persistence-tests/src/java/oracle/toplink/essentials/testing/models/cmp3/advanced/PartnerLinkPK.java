package oracle.toplink.essentials.testing.models.cmp3.advanced;

import javax.persistence.*;

public class PartnerLinkPK {
    private Integer manId;
    private Integer womanId;

	public PartnerLinkPK() {}
    
    @Id
	public Integer getManId() { 
        return manId; 
    }
    
    @Id
	public Integer getWomanId() { 
        return womanId; 
    }
    
	public void setManId(Integer manId) { 
        this.manId = manId; 
    }
    
    public void setWomanId(Integer womanId) { 
        this.womanId = womanId; 
    }

    public boolean equals(Object anotherPartnerLinkPK) {
        if (anotherPartnerLinkPK.getClass() != PartnerLinkPK.class) {
            return false;
        }
        
        return getManId().equals(((PartnerLinkPK) anotherPartnerLinkPK).getManId()) && 
               getWomanId().equals(((PartnerLinkPK) anotherPartnerLinkPK).getWomanId());
    }
}