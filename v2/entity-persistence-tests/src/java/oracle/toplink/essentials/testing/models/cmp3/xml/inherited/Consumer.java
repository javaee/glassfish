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


package oracle.toplink.essentials.testing.models.cmp3.xml.inherited;

public class Consumer  {
    public int post_load_count = 0;
    public int post_persist_count = 0;
    public int post_remove_count = 0;
    public int post_update_count = 0;
    public int pre_persist_count = 0;
    public int pre_remove_count = 0;
    public int pre_update_count = 0;
    
    public Consumer() {}
    
	public void postLoad() {
        ++post_load_count;
	}
    
	public void postPersist() {
        ++post_persist_count;
	}
   
	public void postRemove() {
        ++post_remove_count;
	}
    
	public void postUpdate() {
        ++post_update_count;
	}
    
	public void prePersist() {
        ++pre_persist_count;
	}
    
	public void preRemove() {
        ++pre_remove_count;
	}

	public void preUpdate() {
        ++pre_update_count;
	}
}
