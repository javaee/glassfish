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
package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.io.*;
import javax.persistence.*;
import static javax.persistence.FetchType.*;

/**
 * <p><b>Purpose</b>: Describes an Employee's phone number.
 *    <p><b>Description</b>: Used in a 1:M relationship from an employee.
 */
@IdClass(oracle.toplink.essentials.testing.models.cmp3.advanced.PhoneNumberPK.class)
@Entity
@Table(name="CMP3_PHONENUMBER")
public class PhoneNumber implements Serializable {
	private String number;
	private String type;
	private Employee owner;
    private Integer id;
    private String areaCode;
	
    public PhoneNumber() {
        this("", "###", "#######");
    }

    public PhoneNumber(String type, String theAreaCode, String theNumber) {
        this.type = type;
        this.areaCode = theAreaCode;
        this.number = theNumber;
        this.owner = null;
    }

    @Id
	@Column(name="OWNER_ID", insertable=false, updatable=false)
	public Integer getId() { 
        return id; 
    }
    
	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name="NUMB")
	public String getNumber() { 
        return number; 
    }
    
	public void setNumber(String number) { 
        this.number = number; 
    }

    @Id
    @Column(name="TYPE")
	public String getType() { 
        return type; 
    }
    
	public void setType(String type) {
		this.type = type;
	}

	@Column(name="AREA_CODE")
	public String getAreaCode() { 
        return areaCode; 
    }
    
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	@ManyToOne
	@JoinColumn(name="OWNER_ID", referencedColumnName="EMP_ID")
	public Employee getOwner() { 
        return owner; 
    }
    
	public void setOwner(Employee owner) {
		this.owner = owner;
	}

    /**
     * Example: Phone[Work]: (613) 225-8812
     */
    public String toString() {
        StringWriter writer = new StringWriter();

        writer.write("PhoneNumber[");
        writer.write(getType());
        writer.write("]: (");
        writer.write(getAreaCode());
        writer.write(") ");

        int numberLength = this.getNumber().length();
        writer.write(getNumber().substring(0, Math.min(3, numberLength)));
        if (numberLength > 3) {
            writer.write("-");
            writer.write(getNumber().substring(3, Math.min(7, numberLength)));
        }

        return writer.toString();
    }
    
    /**
     * Builds the PhoneNumberPK for this class
     */
    public PhoneNumberPK buildPK(){
        PhoneNumberPK pk = new PhoneNumberPK();
        pk.setId(this.getOwner().getId());
        pk.setType(this.getType());
        return pk;
    }
}
