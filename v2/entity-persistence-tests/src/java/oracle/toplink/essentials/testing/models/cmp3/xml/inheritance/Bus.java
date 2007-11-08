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
package oracle.toplink.essentials.testing.models.cmp3.xml.inheritance;

import java.util.ArrayList;
import javax.persistence.EntityListeners;

// These listeners are overriden in XML. BusListener2 callbacks should be
// called before BusListener callbacks. A BusListener3 has also been added
// and should be called after BusListener2 and before BusListener.
@EntityListeners({
    oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.BusListener.class, 
    oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.BusListener2.class
})
public class Bus extends FueledVehicle {
    public static int PRE_PERSIST_COUNT = 0;
    public static int POST_PERSIST_COUNT = 0;
    public static int PRE_REMOVE_COUNT = 0;
    public static int POST_REMOVE_COUNT = 0;
    public static int PRE_UPDATE_COUNT = 0;
    public static int POST_UPDATE_COUNT = 0;
    public static int POST_LOAD_COUNT = 0;
    
    private Person busDriver;
    public ArrayList prePersistCalledListeners = new ArrayList();
    public ArrayList postPersistCalledListeners = new ArrayList();
    
    public void addPostPersistCalledListener(Class listener) {
        postPersistCalledListeners.add(listener);
    }
    
    public void addPrePersistCalledListener(Class listener) {
        prePersistCalledListeners.add(listener);
    }
    
    public Person getBusDriver() {
        return busDriver;
    }

    public int postPersistCalledListenerCount() {
        return postPersistCalledListeners.size();
    }
    
    public int prePersistCalledListenerCount() {
        return prePersistCalledListeners.size();
    }
    
    public Class getPostPersistCalledListenerAt(int index) {
        return (Class) postPersistCalledListeners.get(index);
    }
    
    public Class getPrePersistCalledListenerAt(int index) {
        return (Class) prePersistCalledListeners.get(index);
    }
    
    public void setBusDriver(Person busDriver) {
        this.busDriver = busDriver;
    }

    // CALLBACK METHODS //
    public void prePersist() {
        PRE_PERSIST_COUNT++;
    }
    
    protected void postPersist() {
        POST_PERSIST_COUNT++;
    }
    
    private void preRemove() {
        PRE_REMOVE_COUNT++;
    }
    
    void postRemove() {
        POST_REMOVE_COUNT++;
    }
    
    public void preUpdate() {
        PRE_UPDATE_COUNT++;
    }
    
    public void postUpdate() {
        POST_UPDATE_COUNT++;
    }
    
    public void postLoad() {
        POST_LOAD_COUNT++;
    }
}
