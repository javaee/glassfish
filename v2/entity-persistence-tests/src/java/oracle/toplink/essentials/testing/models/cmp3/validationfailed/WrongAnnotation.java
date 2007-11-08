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
package oracle.toplink.essentials.testing.models.cmp3.validationfailed;

import java.util.*;
import java.io.Serializable;
import javax.persistence.*;
import static javax.persistence.GenerationType.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;


@Entity
public class WrongAnnotation{
	private Integer id;
	private String firstName;
	private String lastName;
    
	public WrongAnnotation () {
    }
    
	@Id
	public Integer getId() { 
        return id; 
    }
    
	public void setId(Integer id) { 
        this.id = id; 
    }

    @Column(name="F_NAME")
	public String getFirstName() { 
        return firstName; 
    }

	public void setFirstName(String firstName){
		this.firstName = firstName;
	};
    

    // Not defined in the XML, this should get processed.
    //this is the wrong annotaion which causes the ValidationException
    @Column(name="F_NAME")
	public String getLastName() { 
        return lastName; 
    }

	public void setLastName(String lastName){
		this.lastName = lastName;
	}

}
