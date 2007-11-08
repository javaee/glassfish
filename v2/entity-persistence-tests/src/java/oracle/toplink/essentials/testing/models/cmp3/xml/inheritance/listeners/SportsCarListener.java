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


package oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners;

/**
 * A listener for the SportsCar entity.
 * 
 * It implements the following annotations:
 * - PreRemove
 * - PostRemove
 * - PreUpdate
 * - PostUpdate
 * 
 * It overrides the following annotations:
 * - None
 * 
 * It inherits the following annotations:
 * - PostLoad from Vehicle.
 * - PrePersist from ListenerSuperclass
 * - PostPersist from FueledVehicleListener
 */
public class SportsCarListener extends ListenerSuperclass {
    public static int PRE_REMOVE_COUNT = 0;
    public static int POST_REMOVE_COUNT = 0;
    public static int PRE_UPDATE_COUNT = 0;
    public static int POST_UPDATE_COUNT = 0;
    
	public void preRemove(Object sportsCar) {
        PRE_REMOVE_COUNT++;
	}

	public void postRemove(Object sportsCar) {
        POST_REMOVE_COUNT++;
	}

	public void preUpdate(Object sportsCar) {
        PRE_UPDATE_COUNT++;
	}

	public void postUpdate(Object sportsCar) {
        POST_UPDATE_COUNT++;
	}
}
