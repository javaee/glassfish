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


package oracle.toplink.essentials.testing.models.cmp3.inheritance.listeners;

import java.util.EventListener;
import javax.persistence.PrePersist;

/**
 * A superclass listener for SportsCar and Bus entities.
 * 
 * It implements the following annotations:
 * - PrePersist
 * 
 * It overrides the following annotations:
 * - None
 * 
 * It inherits the following annotations:
 * - None
 */
public class ListenerSuperclass implements EventListener {
    public static int COMMON_PRE_PERSIST_COUNT = 0;
    
    @PrePersist
	public void prePersist(Object obj) {
        COMMON_PRE_PERSIST_COUNT++;
	}
}
