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

package org.glassfish.internal.data;

import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.component.Habitat;

import java.util.*;

/**
 * The container Registry holds references to the currently running containers.
 *
 * @author Jerome Dochez
 */
@Service
@Scoped(Singleton.class)
public class ContainerRegistry {

    @Inject
    Habitat habitat;
    
    Map<String, EngineInfo> containers = new HashMap<String, EngineInfo>();


    public synchronized void addContainer(String name, EngineInfo info) {
        containers.put(name, info);
        info.setRegistry(this);
    }

    public List<Sniffer> getStartedContainersSniffers() {

        ArrayList<Sniffer> sniffers = new ArrayList<Sniffer>();

        for (EngineInfo info : getContainers() ) {
            sniffers.add(info.getSniffer());
        }
        return sniffers;
    }

    public synchronized EngineInfo getContainer(String containerType) {
        return containers.get(containerType);
    }

    public synchronized EngineInfo removeContainer(EngineInfo container) {
        for (Map.Entry<String, EngineInfo> entry : containers.entrySet()) {
            if (entry.getValue().equals(container)) {
                return containers.remove(entry.getKey());
            }
        }
        return null;
    }

    public Iterable<EngineInfo> getContainers() {
        ArrayList<EngineInfo> copy = new ArrayList<EngineInfo>(containers.size());
        copy.addAll(containers.values());
        return copy;
    }
        
}
