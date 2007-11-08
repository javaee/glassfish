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
package oracle.toplink.essentials.testing.models.cmp3.xml.inherited;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.AttributeOverride;

// This one should get picked up since there is not attribute override
// specified in the XML for this class.
@AttributeOverride(name="id", column=@Column(name="ALPINE_ID", nullable=false))
public class Alpine extends Beer  {
    public enum Classification { STRONG, BITTER, SWEET }
    
    private Date bestBeforeDate;
    private Classification classification;
    
    public Alpine() {}
    
    public Date getBestBeforeDate() {
        return bestBeforeDate;
    }
    
    public Classification getClassification() {
        return classification;    
    }
    
    public void setBestBeforeDate(Date bestBeforeDate) {
        this.bestBeforeDate = bestBeforeDate;
    }
    
    public void setClassification(Classification classification) {
        this.classification = classification;
    }
    
    public boolean equals(Object anotherAlpine) {
        if (anotherAlpine.getClass() != Alpine.class) {
            return false;
        }
        
        return (getId().equals(((Alpine)anotherAlpine).getId()));
    }
    
    // This is here for testing purposes. It is bogus, the access type has
    // been set to FIELD for this class in XML therefore, this method should
    // not get processed. This processed will cause and error since the 
    // Embedded is an int.
    @EmbeddedId
    public int getBogusEmbeddedId() {
        return 0;
    }
    
    public void setBogusEmbeddedId(int id) {
        
    }
}
