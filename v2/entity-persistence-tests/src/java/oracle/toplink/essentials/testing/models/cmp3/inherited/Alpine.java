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
package oracle.toplink.essentials.testing.models.cmp3.inherited;

import java.util.Date;
import java.util.ArrayList;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PrimaryKeyJoinColumn;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.TemporalType.DATE;

@Entity
@Table(name="CMP3_ALPINE")
public class Alpine extends Beer  {
    public enum Classification { STRONG, BITTER, SWEET }
    
    private Date bestBeforeDate;
    private Classification classification;
    private SerialNumber serialNumber;
    private ArrayList inspectionDates;
    
    public static int ALPINE_PRE_PERSIST_COUNT = 0;
    
    public Alpine() {
        inspectionDates = new ArrayList();
    }
    
    public void addInspectionDate(Date date) {
        getInspectionDates().add(date);
    }
    
    @PrePersist
    public void celebrate() {
        ALPINE_PRE_PERSIST_COUNT++;
    }
    
    @Column(name="BB_DATE")
    @Temporal(DATE)
    public Date getBestBeforeDate() {
        return bestBeforeDate;
    }
    
    public Classification getClassification() {
        return classification;    
    }

    @Column(name="I_DATES")
    public ArrayList getInspectionDates() {
        return inspectionDates;    
    }
    
    @OneToOne(cascade=ALL)
    @PrimaryKeyJoinColumn
    public SerialNumber getSerialNumber() {
        return serialNumber;
    }
    
    public void setBestBeforeDate(Date bestBeforeDate) {
        this.bestBeforeDate = bestBeforeDate;
    }
    
    public void setClassification(Classification classification) {
        this.classification = classification;
    }
    
    public void setInspectionDates(ArrayList inspectionDates) {
        this.inspectionDates = inspectionDates;
    }
    
    public void setSerialNumber(SerialNumber serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public boolean equals(Object anotherAlpine) {
        if (anotherAlpine.getClass() != Alpine.class) {
            return false;
        }
        
        return (getId().equals(((Alpine)anotherAlpine).getId()));
    }
}
