package oracle.toplink.essentials.testing.models.cmp3.advanced;

import javax.persistence.*;

import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

@Entity
@Table(name="MW")
@IdClass(oracle.toplink.essentials.testing.models.cmp3.advanced.PartnerLinkPK.class)
public class PartnerLink {
    private Man man;
    private Woman woman;

	public PartnerLink() {}
    
    @OneToOne(cascade=PERSIST)
	@JoinColumn(name="M")
	public Man getMan() { 
        return man; 
    }
    
    @Id
    @Column(name="M", insertable=false, updatable=false)
	public Integer getManId() {
        return (getMan() == null) ? null : getMan().getId();
    }
    
    @OneToOne(cascade=PERSIST)
	@JoinColumn(name="W")
	public Woman getWoman() { 
        return woman; 
    }
    
    @Id
    @Column(name="W", insertable=false, updatable=false)
	public Integer getWomanId() {
        return (getWoman() == null) ? null : getWoman().getId();
    }
    
	public void setMan(Man man) { 
        this.man = man; 
    }
    
    public void setManId(Integer manId) {  
    }
    
    public void setWoman(Woman woman) { 
        this.woman = woman; 
    }
    
    public void setWomanId(Integer womanId) { 
    }
}
