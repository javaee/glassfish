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
 * InstanceCallbacks.java
 *
 * Created on February 25, 2000
 */
 
package com.sun.persistence.support;

/** A <code>PersistenceCapable</code> class that provides callback methods for life
 * cycle events implements this interface.
 *
 * <P>Classes which include non-persistent fields whose values depend
 * on the values of persistent fields require callbacks on specific
 * JDO instance life cycle events in order to correctly populate the
 * values in these fields.
 *
 * <P>This interface defines the methods executed
 * by the <code>PersistenceManager</code> for these life cycle events.  If the class
 * implements <code>InstanceCallbacks</code>, it must explicitly declare it in the
 * class definition.  
 *
 * <P>The callbacks might also be used if the persistent instances
 * need to be put into the runtime infrastructure of the application.
 * For example, a persistent instance might notify other instances
 * on changes to state.  The persistent instance is in a list of
 * managed instances, and when the persistent instance is made hollow,
 * it can no longer generate change events, and the persistent
 * instance should be removed from the list of managed instances.
 *
 * <P>To implement this, the application programmer would implement
 * the <code>jdoPostLoad</code> callback to put itself into the list of managed
 * instances; and implement the <code>jdoPreClear</code> to remove itself from
 * the list.
 *
 * <P>Note that JDO does not manage the state of non-persistent
 * fields, and when a JDO instance transitions to hollow, JDO clears
 * the persistent fields.  It is the programmer's responsibility to
 * clear non-persistent fields so that garbage collection of
 * referred instances can occur.
 *
 * @author Craig Russell
 * @version 1.0
 */
public interface InstanceCallbacks 
{
    /**
     * Called after the values are loaded from the data store into
     * this instance.
     *
     * <P>This method is not modified by the Reference Enhancer.
     * <P>Derived fields should be initialized in this method.
     * The context in which this call is made does not allow access to 
     * other persistent JDO instances.
     */
    void jdoPostLoad();

    /**
     * Called before the values are stored from this instance to the
     * data store.
     *
     * <P>Data store fields that might have been affected by modified
     * non-persistent fields should be updated in this method.
     *
     * <P>This method is modified by the enhancer so that changes to 
     * persistent fields will be reflected in the data store. 
     * The context in which this call is made allows access to the 
     * <code>PersistenceManager</code> and other persistent JDO instances.
     */
    void jdoPreStore();

    /**
     * Called before the values in the instance are cleared.
     *
     * <P>Transient fields should be cleared in this method.  
     * Associations between this
     * instance and others in the runtime environment should be cleared.
     *
     * <P>This method is not modified by the enhancer.
     */
    void jdoPreClear();

    /**
     * Called before the instance is deleted.
     * This method is called before the state transition to persistent-deleted 
     * or persistent-new-deleted. Access to field values within this call 
     * are valid. Access to field values after this call are disallowed. 
     * <P>This method is modified by the enhancer so that fields referenced 
     * can be used in the business logic of the method.
     */
    void jdoPreDelete();
}
