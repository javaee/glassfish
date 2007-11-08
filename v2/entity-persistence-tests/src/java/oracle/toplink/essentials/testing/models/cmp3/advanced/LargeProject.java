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

import javax.persistence.*;

/**
 * Local interface for the large project bean.
 * This is the bean's public/local interface for the clients usage.
 * All locals must extend the javax.ejb.EJBLocalObject.
 * The bean itself does not have to implement the local interface, but must implement all of the methods.
 */
@Entity
@Table(name="CMP3_LPROJECT")
@DiscriminatorValue("L")
@NamedQueries({
@NamedQuery(
	name="findWithBudgetLargerThan",
	query="SELECT OBJECT(project) FROM LargeProject project WHERE project.budget >= :amount"
),
@NamedQuery(
	name="constructLProject",
	query="SELECT new oracle.toplink.essentials.testing.models.cmp3.advanced.LargeProject(project.name) FROM LargeProject project")
}
)
public class LargeProject extends Project {
	private double budget;
    public LargeProject () {
        super();
    }
    public LargeProject (String name) {
        this();
        this.setName(name);
    }

	public double getBudget() { 
        return budget; 
    }
    
	public void setBudget(double budget) { 
		this.budget = budget; 
	}
}
