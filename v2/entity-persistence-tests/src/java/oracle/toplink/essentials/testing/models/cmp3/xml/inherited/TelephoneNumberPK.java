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


// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.xml.inherited;

public class TelephoneNumberPK  {
	public String type;
    protected String number;
    private String areaCode;

    public TelephoneNumberPK() {}
    
	public String getAreaCode() { 
        return areaCode; 
    }
    
	public String getNumber() { 
        return number; 
    }

	public String getType() { 
        return type; 
    }
    
    public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
    
	public void setNumber(String number) { 
        this.number = number; 
    }

	public void setType(String type) {
		this.type = type;
	}
    
    public boolean equals(Object anotherTelephoneNumberPK) {
        if (anotherTelephoneNumberPK.getClass() != TelephoneNumberPK.class) {
            return false;
        }
        
        TelephoneNumberPK telephoneNumberPK = (TelephoneNumberPK) anotherTelephoneNumberPK;
        
        return (
            telephoneNumberPK.getAreaCode().equals(getAreaCode()) && 
            telephoneNumberPK.getNumber().equals(getNumber()) &&
            telephoneNumberPK.getType().equals(getType())
        );

    }
}
