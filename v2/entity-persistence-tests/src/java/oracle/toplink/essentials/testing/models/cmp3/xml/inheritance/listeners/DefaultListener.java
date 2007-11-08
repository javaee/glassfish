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

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;

public class DefaultListener {
    // Not going to test all the callbacks from the default listener
    // since the listener gets added to 90+ classes and when testing that
    // is a lot of notifications which slows things down. Just
    // going to test a couple of the callbacks which should be
    // good enough.
    
    public static int PRE_PERSIST_COUNT = 0;
    public static int POST_PERSIST_COUNT = 0;
    public static int POST_LOAD_COUNT = 0;

    public DefaultListener() {
        super();
    }
    
    @PostLoad
    // Defined in XML, annotations processor should ignore this one.
    // If @PostLoad was defined on another method than it should throw
    // and exception cause we have multiple methods for the same call
    // back defined. Exception throwing tested manually.
    public void postLoad(Object obj) {
        POST_LOAD_COUNT++;
    }
    
	// @PrePersist
    // Defined in XML, test that we pick it up.
	public void prePersist(Object obj) {
        PRE_PERSIST_COUNT++;
	}
    
	@PostPersist
	public void postPersist(Object obj) {
        POST_PERSIST_COUNT++;
	}
}
