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
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/*
 * SCO.java
 *
 * created May 9, 2000
 *
 * @author Marina Vatkina
 * @version 1.0.1
 */

package com.sun.org.apache.jdo.sco;

public interface SCO {
    /**
     * Returns the field name
     *
     * @return field name as java.lang.String
     */
    String getFieldName();

    /**
     * Returns the owner object of the SCO instance
     *
     * @return owner object
     */
    Object getOwner();

    /** 
     * Sets the owner and field number.  Called by StateManager upon
     * assignment to a managed instance.
     * @param owner the owner object. 
     * @param fieldNumber the number of the field associated with this instance.
     */
    void setOwner (Object owner, int fieldNumber);

    /**
     * Nullifies references to the owner Object iff the passed in owner and 
     * fieldNumber match.
     * @param owner the existing owner object.
     * @param fieldNumber the existing number of the field.
     */
    void unsetOwner(Object owner, int fieldNumber);

    /**
     * Make a copy of this object.
     * @since 1.0.1
     */
    Object clone();
}
