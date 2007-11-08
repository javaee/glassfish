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
package oracle.toplink.essentials.testing.models.cmp3.advanced;

import java.sql.Date;
import java.io.*;
import javax.persistence.*;

/**
 * <p><b>Purpose</b>: Defines the former employment of an Employee.
 *    <p><b>Description</b>: The former employment holds the name of the
 *    former company and an EmploymentPeriod. Maintained in an 
 *    aggregate relationship of Employee
 *    @see Employee
 */
@Embeddable
@Table(name="CMP3_EMPLOYEE")
public class FormerEmployment implements Serializable {
    private String company;
    private EmploymentPeriod period;

    public FormerEmployment() {}

    public FormerEmployment(String company, EmploymentPeriod period) {
        this.company = company;
        this.period = period;
    }

    @Basic
    public String getFormerCompany() { 
        return company; 
    }
    
    public void setFormerCompany(String company) { 
        this.company = company; 
    }

    @Embedded
    public EmploymentPeriod getPeriod() { 
        return period; 
    }
    
    public void setPeriod(EmploymentPeriod period) { 
        this.period = period; 
    }
    

    /**
     * Print the company and the period
     */
    public String toString() {
        java.io.StringWriter writer = new java.io.StringWriter();
        writer.write("FormerEmployment: ");
        
        if (this.getFormerCompany() != null) {
            writer.write(this.getFormerCompany().toString());
        }
        
        writer.write(",");
        
        if (this.getPeriod() != null) {
            writer.write(this.getPeriod().toString());
        }
        
        return writer.toString();
    }
}
