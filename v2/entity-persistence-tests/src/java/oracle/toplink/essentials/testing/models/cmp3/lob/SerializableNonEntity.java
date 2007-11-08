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
package oracle.toplink.essentials.testing.models.cmp3.lob;

import java.io.Serializable;

/*
 * This class is used to test serialization to persistent fields in other entities,
 * such as mapping attributes of this type to String or blob field types.
 */
public class SerializableNonEntity implements Serializable{
    Long someValue;

    public SerializableNonEntity() {
    }
  
    public SerializableNonEntity(Long value) {
        this.someValue=value;
    }
  
    public void setSomeValue(Long someValue){
        this.someValue=someValue;
    }
    public Long getSomeValue(){
        return someValue;
    }
}
