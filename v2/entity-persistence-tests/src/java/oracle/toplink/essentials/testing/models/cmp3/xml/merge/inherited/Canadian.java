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
import javax.persistence.*;
import static javax.persistence.TemporalType.DATE;

// The table annotation is overidden in XML, remaining annotation is used
@Table(name="BIGBAD_CANADIAN")
public class Canadian extends Beer {
    public enum Flavor { LAGER, LIGHT, ICE, DRY }

    private Flavor flavor;
    private Date bornOnDate;
    
    public Canadian() {}
    
    public Date getBornOnDate() {
        return bornOnDate;
    }
    
    @Basic
    public Flavor getFlavor() {
        return flavor;
    }
    
    public void setBornOnDate(Date bornOnDate) {
        this.bornOnDate = bornOnDate;
    }
    
    public void setFlavor(Flavor flavor) {
        this.flavor = flavor;
    }
}
