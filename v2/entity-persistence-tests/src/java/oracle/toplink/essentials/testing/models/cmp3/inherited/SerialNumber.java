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

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
@Table(name="CMP3_SERIAL_NUMBER")
public class SerialNumber {
    private Alpine alpine;
    private Integer number;
    
    public SerialNumber() {}
    
    @Id
    @Column(name="S_NUMBER")
    public Integer getNumber() {
        return number;
    }
    
    @OneToOne(mappedBy="serialNumber")
    public Alpine getAlpine() {
        return alpine;
    }
    
    public void setAlpine(Alpine alpine) {
        this.alpine = alpine;
        setNumber(alpine.getId());
    }
    
    public void setNumber(Integer number) {
        this.number = number;
    }
}