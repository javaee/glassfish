/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
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
