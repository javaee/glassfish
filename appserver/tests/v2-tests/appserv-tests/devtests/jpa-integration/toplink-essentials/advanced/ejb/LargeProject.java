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


// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
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
