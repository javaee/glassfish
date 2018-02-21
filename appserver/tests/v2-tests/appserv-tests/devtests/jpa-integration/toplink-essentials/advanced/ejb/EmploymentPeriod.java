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

// Copyright (c) 1998, 2005, Oracle. All rights reserved.


// Copyright (c) 1998, 2005, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.sql.Date;
import java.io.*;
import javax.persistence.*;

/**
 * <p><b>Purpose</b>: Defines the period an Employee worked for the organization
 *    <p><b>Description</b>: The period holds the start date and optionally the 
 *    end date if the employee has left (null otherwise). Maintained in an 
 *    aggregate relationship of Employee
 *    @see Employee
 */
@Embeddable
@Table(name="CMP3_EMPLOYEE")
public class EmploymentPeriod implements Serializable {
    private Date startDate;
    private Date endDate;

    public EmploymentPeriod() {}

    /**
     * Return a new employment period instance.
     * The constructor's purpose is to allow only valid instances of a class to 
     * be created. Valid means that the get/set and clone/toString methods 
     * should work on the instance. Arguments to constructors should be avoided 
     * unless those arguments are required to put the instance into a valid 
     * state, or represent the entire instance definition.
     */
    public EmploymentPeriod(Date theStartDate, Date theEndDate) {
        startDate = theStartDate;
        endDate = theEndDate;
    }

	@Column(name="S_DATE")
	public Date getStartDate() { 
        return startDate; 
    }
    
	public void setStartDate(Date date) { 
        this.startDate = date; 
    }

	@Column(name="E_DATE")
	public Date getEndDate() { 
        return endDate; 
    }
    
	public void setEndDate(Date date) { 
        this.endDate = date; 
    }

    /**
     * Print the start & end date
     */
    public String toString() {
        java.io.StringWriter writer = new java.io.StringWriter();
        writer.write("EmploymentPeriod: ");
        
        if (this.getStartDate() != null) {
            writer.write(this.getStartDate().toString());
        }
        
        writer.write("-");
        
        if (this.getEndDate() != null) {
            writer.write(this.getEndDate().toString());
        }
        
        return writer.toString();
    }
}
