 package oracle.toplink.essentials.testing.models.cmp3.advanced;

import javax.persistence.*;

import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

@Entity
public class Man {
    private Integer id;
    private String firstName;
    private String lastName;
    private PartnerLink partnerLink;

	public Man() {}
    
    @Id
    @GeneratedValue(strategy=IDENTITY)
	public Integer getId() { 
        return id; 
    }

    @Column(name="F_NAME")
    public String getFirstName() { 
        return firstName; 
    }
    
    public void setFirstName(String name) { 
        this.firstName = name; 
    }

    @Column(name="L_NAME")
    public String getLastName() { 
        return lastName; 
    }
    
    public void setLastName(String name) { 
        this.lastName = name; 
    }
    
    @OneToOne(mappedBy="man")
	public PartnerLink getPartnerLink() { 
        return partnerLink; 
    }
    
	public void setId(Integer id) { 
        this.id = id; 
    }
    
    public void setPartnerLink(PartnerLink partnerLink) { 
        this.partnerLink = partnerLink; 
    }
}
