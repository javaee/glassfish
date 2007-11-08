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

import javax.persistence.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.GenerationType.*;

@Entity
@Table(name="CMP3_CERTIFICATION")
public class Certification  {
    private Integer id;
    private String description;
    private BeerConsumer beerConsumer;
    
    public Certification() {}
    
    @ManyToOne
	@JoinColumn(name="CONSUMER_ID")  
    public BeerConsumer getBeerConsumer() {
        return beerConsumer;
    }
    
    @Basic
    public String getDescription() {
        return description;    
    }
    
    @Id
    @GeneratedValue(strategy=TABLE, generator="CERTIFICATION_TABLE_GENERATOR")
	@TableGenerator(
        name="CERTIFICATION_TABLE_GENERATOR", 
        table="CMP3_BEER_SEQ", 
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="CERTIFICATION_SEQ")
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public void setBeerConsumer(BeerConsumer beerConsumer) {
        this.beerConsumer = beerConsumer;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
