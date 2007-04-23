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

import java.util.Collection;

/**
 * A resource manager is responsible for adding and retrieving resource from a backing store.
 *
 * <p>
 * ResourceManager must be thread-safe. Multiple threads may concurrently
 * execute various methods of a single ResourceManager instance.
 */
public interface ResourceManager {

    /**
     * Adds a new resource to this manager, using the Extract instance information
     * to qualify the resource, null if the resource is not qualified.
     *
     * @param resourceInfo is the locator information for that resource
     * @param value new value to add to this resource manager
     */
    public abstract <T> void add(ResourceLocator<T> resourceInfo, T value);

    /**
     * Looks up a previously stored instance of the provided type, further
     * identifying the requested resource using the Inject meta data about it.
     * If no qualification of the resource is provided and there is more than
     * one instance of the provided type stored in this manager, the last entry
     * will be returned.
     *
     * @param resourceInfo is the locator information for that resource
     * @return the instance if found, null otherwise.
     */
    public abstract <T> T lookup(ResourceLocator<T> resourceInfo);

    /**
     * Returns all previously stored instances of a particular type, null if none
     * were registered.
     *
     * @param type of the resources looked up
     * @return
     *      Can be empty but never null.
     */
    public abstract <T> Collection<? extends T> lookupAll(Class<T> type);

    /**
     * Removes a resource from this manager.
     * @param value of the resource to be removed
     */
    // TODO: the other methods on this interface allows the same object to be listed
    // under multiple names, so this definition is strange.
    // remove when I work on component de-initialization.
    public abstract void remove(Object value);
    
}
