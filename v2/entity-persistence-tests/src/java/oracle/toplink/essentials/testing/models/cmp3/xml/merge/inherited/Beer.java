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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.JoinColumn;
import static javax.persistence.FetchType.LAZY;

import java.sql.Timestamp;

/**
 * All annotations should be ignored in this class as the XML definition is
 * declared as metadata-complete=true
 */
public class Beer extends Beverage {
    private Timestamp version;
    private double alcoholContent;
    private BeerConsumer beerConsumer;
    private EmbeddedSerialNumber embeddedSerialNumber;
    
    public static int BEER_PRE_PERSIST_COUNT = 0;
    
    public Beer() {}
    
    @PrePersist
    public void celebrate() {
        BEER_PRE_PERSIST_COUNT++;
    }
    
    @Basic
    @Column(name="ALCOHOL_CONTENT")
    public double getAlcoholContent() {
        return alcoholContent;
    }
    
    @ManyToOne(fetch=LAZY)
    @JoinColumn(name="TOTALLY_WRONG_ID")
    public BeerConsumer getBeerConsumer() {
        return beerConsumer;
    }
    
    // The version is defined in XML
    public Timestamp getVersion() {
        return version;
    }
    
    public void setAlcoholContent(double alcoholContent) {
        this.alcoholContent = alcoholContent;
    }
    
    public void setBeerConsumer(BeerConsumer beerConsumer) {
        this.beerConsumer = beerConsumer;
    }
    
    public void setVersion(Timestamp version) {
        this.version = version;
    }

    public EmbeddedSerialNumber getEmbeddedSerialNumber() {
        return this.embeddedSerialNumber;
    }
    
    public void setEmbeddedSerialNumber(EmbeddedSerialNumber embeddedSerialNumber) {
        this.embeddedSerialNumber = embeddedSerialNumber;
    }
}
