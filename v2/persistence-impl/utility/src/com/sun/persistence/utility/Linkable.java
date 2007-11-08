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

/*
 * ConnectionImpl.java
 *
 * Create on March 3, 2000
 */


package com.sun.persistence.utility;

/**
 * This file defines a linkable interface.  Any object that needs to be a member
 * of com.forte.util.DoubleLinkedList should implement this interface.
 */
public interface Linkable {
    public Linkable getNext();

    public Linkable getPrevious();

    public void setNext(Linkable node);

    public void setPrevious(Linkable node);
}

