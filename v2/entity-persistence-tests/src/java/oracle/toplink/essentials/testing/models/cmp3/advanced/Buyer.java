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


// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.util.*;
import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.InheritanceType.*;
import static javax.persistence.FetchType.*;

/**
 * Bean class: BuyerBean
 * Remote interface: Buyer
 * Primary key class: BuyerPK
 * Home interface: BuyerHome
 *
 */
@Entity
@Table(
        name="CMP3_BUYER",
        uniqueConstraints=
            { @UniqueConstraint(columnNames={"BUYER_ID", "BUYER_NAME"}),
              @UniqueConstraint(columnNames={"BUYER_ID", "DESCRIP"})
            }
        )
@Inheritance(strategy=JOINED)
@NamedQuery(
	name="findBuyerByName",
	query="SELECT OBJECT(buyer) FROM Buyer buyer WHERE buyer.name = :name"
)
public class Buyer implements Serializable {
    public int pre_update_count = 0;
    public int post_update_count = 0;
    public int pre_remove_count = 0;
    public int post_remove_count = 0;
    public int pre_persist_count = 0;
    public int post_persist_count = 0;
    public int post_load_count = 0;
    
	private Integer id;
	private int version;
	private String name;
	private String description;

	public Buyer () {
	}

	@Id
    @GeneratedValue(strategy=SEQUENCE, generator="BUYER_SEQUENCE_GENERATOR")
	@SequenceGenerator(name="BUYER_SEQUENCE_GENERATOR", sequenceName="BUYER_SEQ", allocationSize=10)
	@Column(name="BUYER_ID")
	public Integer getId() { 
        return id; 
    }
    
	public void setId(Integer id) { 
        this.id = id; 
    }

	@Version
	@Column(name="VERSION")
	public int getVersion() { 
        return version; 
    }
    
	protected void setVersion(int version) { 
        this.version = version; 
    }

	@Column(name="BUYER_NAME", nullable=false)
	public String getName() { 
        return name; 
    }
    
	public void setName(String name) { 
        this.name = name; 
    }

	@Column(name="DESCRIP", nullable=false)
	public String getDescription() { 
        return description; 
    }
    
	public void setDescription(String description) { 
        this.description = description; 
    }

    public String displayString() {
        StringBuffer sbuff = new StringBuffer();
        sbuff.append("Buyer ").append(getId()).append(": ").append(getName()).append(", ").append(getDescription());

        return sbuff.toString();
    }
    
    @PrePersist
	public void prePersist() {
        ++pre_persist_count;
	}

	@PostPersist
	public void postPersist() {
        ++post_persist_count;
	}

	@PreRemove
	public void preRemove() {
        ++pre_remove_count;
	}

	@PostRemove
	public void postRemove() {
        ++post_remove_count;
	}

	@PreUpdate
	public void preUpdate() {
        ++pre_update_count;
	}

	@PostUpdate
	public void postUpdate() {
        ++post_update_count;
	}

	@PostLoad
	public void postLoad() {
        ++post_load_count;
	}
}
