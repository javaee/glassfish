/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Extract;

/**
 * Used to located resource in our resource manager.
 * Resources are indexed with their types and can be further
 * indexed using a name.
 *
 * @author Jerome Dochez
 */
public final class ResourceLocator<T> {

    final static String DEFAULT_NAME="";

    final private Class<T> type;
    final private String name;

    /**
     * Creates a new ResourceLocator instance using the resource meta-data
     * from the @{link Inject} annotation instance
     * @param instance the annoation instance
     * @param contract if the type of the resource
     */
    public ResourceLocator(Inject instance, Class<T> contract) {
        this(instance!=null?instance.name():DEFAULT_NAME,contract);
    }

    /**
     * Creates a new ResourceLocator instance using the resource meta-data
     * from the @{link Service} annotation instance
     * @param instance the annoation instance
     * @param contract if the type of the resource
     */
    public ResourceLocator(Service instance,  Class<T> contract) {
        this(instance!=null?instance.name():DEFAULT_NAME,contract);
    }

    /**
     * Creates a new ResourceLocator instance using the resource meta-data
     * from the @{link Extract} annotation instance
     * @param instance the annoation instance
     * @param contract if the type of the resource
     */
    public ResourceLocator(Extract instance,  Class<T> contract) {
        this(instance!=null?instance.name():DEFAULT_NAME,contract);
    }

    public ResourceLocator(String name,  Class<T> contract) {
        assert contract !=null;
        if(name==null)  name=DEFAULT_NAME;
        this.name = name;
        this.type = contract;
    }

    public ResourceLocator(Class<T> contract) {
        this(DEFAULT_NAME,contract);
    }

    /**
     * Returns the name of the resource if any
     * @return the name of the resource if any
     */
    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * Returns true if the resource meta-data is the same
     * @param o the other resource locator to compare with
     * @return true if the resource meta-data is the same
     */
    @Override
    public boolean equals(Object o) {
        if(this.getClass()!=o.getClass())
            return false;

        ResourceLocator that = (ResourceLocator) o;
        if (name!=null) {
            return name.equals(that.name) && type==that.type;
        } else {
            return that.name==null && type==that.type;
        }
    }

    public int hashCode() {
        return type.hashCode()*31 ^ (name==null?0:name.hashCode());
    }
}
