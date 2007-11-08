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

import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import oracle.toplink.essentials.indirection.ValueHolderInterface;

@Entity
@Table(name="CMP3_BLUE")
public class Blue extends Beer  {
    public ValueHolderInterface ignoredObject;
    
    public Blue() {}
    
    // This class is intentionally left with no annotations to test that
    // it picks us the access type from the mapped superclass.
    
    public boolean equals(Object anotherBlue) {
        if (anotherBlue.getClass() != Blue.class) {
            return false;
        }
        
        return (getId().equals(((Blue)anotherBlue).getId()));
    }
    
    // Mimicking an accessor that was weaved to have value holders ... the 
    // metadata processing should ignore this mapping.
    
    @OneToOne
    public ValueHolderInterface getIgnoredObject() {
        return ignoredObject;
    }
    
    public void setIgnoredObject(ValueHolderInterface ignoredObject) {
        this.ignoredObject = ignoredObject;
    }
}
