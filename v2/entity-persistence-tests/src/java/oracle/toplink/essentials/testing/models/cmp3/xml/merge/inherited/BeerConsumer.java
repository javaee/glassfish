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


package oracle.toplink.essentials.testing.models.cmp3.xml.merge.inherited;

import java.util.Map;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Enumeration;
import javax.persistence.*;
import static javax.persistence.FetchType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.GenerationType.*;

public class BeerConsumer {
    public int post_load_count = 0;
    public int post_persist_count = 0;
    public int post_remove_count = 0;
    public int post_update_count = 0;
    public int pre_persist_count = 0;
    public int pre_remove_count = 0;
    public int pre_update_count = 0;
    
    private Integer id;
    private String name;
    private Collection<Alpine> alpineBeersToConsume;
    private Map<Integer, Canadian> canadianBeersToConsume;
    private Map<Integer, Certification> certifications;
    private Map<TelephoneNumberPK, TelephoneNumber> telephoneNumbers;
    
    public BeerConsumer() {
        super();
        alpineBeersToConsume = new Vector<Alpine>();
        canadianBeersToConsume = new Hashtable<Integer, Canadian>();
        certifications = new Hashtable<Integer, Certification>();
        telephoneNumbers = new Hashtable<TelephoneNumberPK, TelephoneNumber>();
    }
    
    public void addAlpineBeerToConsume(Alpine alpine) {
        alpine.setBeerConsumer(this);
        ((Vector) alpineBeersToConsume).add(alpine);
    }
    
    public void addAlpineBeerToConsume(Alpine alpine, int index) {
        alpine.setBeerConsumer(this);
        ((Vector) alpineBeersToConsume).insertElementAt(alpine, index);
    }
    
    /**
     * This model requires that a BeerConsumer be persisted prior to assigning
     * him/her a telephone number. This is because the BeerConsumer id is part
     * of the composite primary key, and that key is needed for the map that
     * holds the telephone numbers.
     */
    public void addTelephoneNumber(TelephoneNumber telephoneNumber) {
        telephoneNumber.setBeerConsumer(this);
        telephoneNumbers.put(telephoneNumber.buildPK(), telephoneNumber);
    }
    
    public Collection<Alpine> getAlpineBeersToConsume() {
        return alpineBeersToConsume;
    }
    
    public Alpine getAlpineBeerToConsume(int index) {
        return (Alpine) ((Vector) alpineBeersToConsume).elementAt(index);
    }
    
    public Map<Integer, Canadian> getCanadianBeersToConsume() {
        return canadianBeersToConsume;
    }
    
    public Map<Integer, Certification> getCertifications() {
        return certifications;
    }
   
    public Integer getId() {
        return id;
    }
    
    // This annotation is used
    @Basic
    public String getName() {
        return name;
    }
    
    // Define the relationship incorrectly, to ensure XML overrides annotation
    @OneToMany(mappedBy="thisIsVeryWrong", cascade=ALL, fetch=EAGER)
    @MapKey // key defaults to an instance of the composite pk class 
	public Map<TelephoneNumberPK, TelephoneNumber> getTelephoneNumbers() { 
        return telephoneNumbers; 
    }
    
    public boolean hasTelephoneNumber(TelephoneNumber telephoneNumber) {
        Enumeration keys = ((Hashtable) telephoneNumbers).keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            
            if (telephoneNumbers.get(key).equals(telephoneNumber)) {
                return true;
            }
        }
        
        return false;
    }
    
    public Alpine moveAlpineBeerToConsume(int fromIndex, int toIndex) {
        Alpine alpine = ((Vector<Alpine>) alpineBeersToConsume).elementAt(fromIndex);
        ((Vector) alpineBeersToConsume).removeElementAt(fromIndex);
        ((Vector) alpineBeersToConsume).add(toIndex, alpine);
        return alpine;
    }
    
    @PostLoad
	public void postLoad() {
        ++post_load_count;
	}
    
    @PostPersist
	public void postPersist() {
        ++post_persist_count;
	}
   
   	@PostRemove
	public void postRemove() {
        ++post_remove_count;
	}
    
    @PostUpdate
	public void postUpdate() {
        ++post_update_count;
	}
    
    @PrePersist
	public void prePersist() {
        ++pre_persist_count;
	}
    
    @PreRemove
	public void preRemove() {
        ++pre_remove_count;
	}

	@PreUpdate
	public void preUpdate() {
        ++pre_update_count;
	}
    
    public Alpine removeAlpineBeerToConsume(int index) {
        Alpine alpine = ((Vector<Alpine>) alpineBeersToConsume).elementAt(index);
        alpine.setBeerConsumer(null);
        ((Vector) alpineBeersToConsume).removeElementAt(index);
        return alpine;
    }
    
    public void removePhoneNumber(TelephoneNumber telephoneNumber) {
        Enumeration keys = ((Hashtable) telephoneNumbers).keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            TelephoneNumber potentialTelephoneNumber = (TelephoneNumber) telephoneNumbers.get(key);
            
            if (potentialTelephoneNumber.equals(telephoneNumber)) {
                telephoneNumbers.remove(key);
                potentialTelephoneNumber.setBeerConsumer(null);
                return;
            }
        }
    }
    
    public void setAlpineBeersToConsume(Collection<Alpine> alpineBeersToConsume) {
        this.alpineBeersToConsume = alpineBeersToConsume;
    }
    
    public void setCanadianBeersToConsume(Map<Integer, Canadian> canadianBeersToConsume) {
        this.canadianBeersToConsume = canadianBeersToConsume;
    }
    
    public void setCertifications(Map<Integer, Certification> certifications) {
        this.certifications = certifications;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setTelephoneNumbers(Map<TelephoneNumberPK, TelephoneNumber> telephoneNumbers) {
        this.telephoneNumbers = telephoneNumbers;
	}
}
