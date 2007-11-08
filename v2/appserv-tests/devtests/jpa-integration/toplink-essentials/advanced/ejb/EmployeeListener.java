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
// Copyright (c) 1998, 2005, Oracle. All rights reserved.


package oracle.toplink.essentials.testing.models.cmp3.advanced;

import javax.persistence.*;
import java.util.EventListener;

public class EmployeeListener implements EventListener {
    public static int PRE_PERSIST_COUNT = 0;
    public static int POST_PERSIST_COUNT = 0;
    public static int PRE_REMOVE_COUNT = 0;
    public static int POST_REMOVE_COUNT = 0;
    public static int PRE_UPDATE_COUNT = 0;
    public static int POST_UPDATE_COUNT = 0;
    public static int POST_LOAD_COUNT = 0;

    // preUpdate will remove this prefix from firstName and lastName
    public static String PRE_UPDATE_NAME_PREFIX = "PRE_UPDATE_NAME_PREFIX";
    
	@PrePersist
	public void prePersist(Object emp) {
        PRE_PERSIST_COUNT++;
	}

	@PostPersist
	public void postPersist(Object emp) {
        POST_PERSIST_COUNT++;
	}

	@PreRemove
	public void preRemove(Object emp) {
        PRE_REMOVE_COUNT++;
	}

	@PostRemove
	public void postRemove(Object emp) {
        POST_REMOVE_COUNT++;
	}

	@PreUpdate
	public void preUpdate(Object emp) {
        PRE_UPDATE_COUNT++;
        Employee employee = (Employee)emp;
        if(employee.getFirstName() != null && employee.getFirstName().startsWith(PRE_UPDATE_NAME_PREFIX)) {
            employee.setFirstName(employee.getFirstName().substring(PRE_UPDATE_NAME_PREFIX.length()));
        }
        if(employee.getLastName() != null && employee.getLastName().startsWith(PRE_UPDATE_NAME_PREFIX)) {
            employee.setLastName(employee.getLastName().substring(PRE_UPDATE_NAME_PREFIX.length()));
        }
	}

	@PostUpdate
	public void postUpdate(Object emp) {
        POST_UPDATE_COUNT++;
	}

	@PostLoad
	public void postLoad(Employee emp) {
        POST_LOAD_COUNT++;
	}
}
