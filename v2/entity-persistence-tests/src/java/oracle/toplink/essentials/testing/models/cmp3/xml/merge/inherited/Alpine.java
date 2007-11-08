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

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import static javax.persistence.TemporalType.DATE;

public class Alpine extends Beer  {
    public enum Classification { STRONG, BITTER, SWEET }
    
    private Date bestBeforeDate;
    private Classification classification;
    @Transient private String localTransientString;
    
    public static int ALPINE_PRE_PERSIST_COUNT = 0;
    
    public Alpine() {}
    
    @PrePersist
    public void celebrate() {
        ALPINE_PRE_PERSIST_COUNT++;
    }
    
    // Overidden in XML
    @Column(name="BB_DATE")
    @Temporal(DATE)
    public Date getBestBeforeDate() {
        return bestBeforeDate;
    }
    
    // This annotation should be valid and the property should not be persisted
    public String getLocalTransientString() {
        return localTransientString;
    }

    public Classification getClassification() {
        return classification;    
    }

    public void setBestBeforeDate(Date bestBeforeDate) {
        this.bestBeforeDate = bestBeforeDate;
    }
    
    public void setLocalTransientString(String localTransientString) {
        this.localTransientString=localTransientString;
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
}
