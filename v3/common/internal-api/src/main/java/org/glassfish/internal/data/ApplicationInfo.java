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

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.container.Container;

import java.util.List;
import java.util.ArrayList;

/**
 * Information about a running application. Applications are composed of modules.
 * Modules run in an individual container.
 *
 * @author Jerome Dochez
 */
public class ApplicationInfo {
    
    final private ModuleInfo[] modules;
    final private String name;
    final private ReadableArchive source;


    /**
     * Creates a new instance of an ApplicationInfo
     *
     * @param source the archive for this application
     * @param name name of the application
     * @param modules the modules that are forming the application
     */
    public ApplicationInfo(ReadableArchive source,
                           String name, ModuleInfo... modules) {
        this.name = name;
        this.source = source;
        this.modules = modules;
    }
    

    
    /**
     * Returns the registration name for this application
     * @return the application registration name
     */
    public String getName() {
        return name;
    }  
    


    /**
     * Returns the directory where the application bits are located
     * @return the application bits directory
     * */
    public ReadableArchive getSource() {
        return source;
    }


    /**
     * Returns the modules of this application
     * @return the modules of this application
     */
    public ModuleInfo[] getModuleInfos() {
        return modules;
    }

    /**
     * Returns the list of sniffers that participated in loaded this
     * application
     *
     * @return array of sniffer that loaded the application's module
     */
    public Iterable<Sniffer> getSniffers() {
        List<Sniffer> sniffers = new ArrayList<Sniffer>();
        for (ModuleInfo module : modules) {
            sniffers.add(module.getContainerInfo().getSniffer());
        }
        return sniffers;
    }

    /*
     * Returns the ModuleInfo for a particular container type
     * @param type the container type
     * @return the module info is this application as a module implemented with
     * the passed container type
     */
    public <T extends Container> ModuleInfo getModuleInfo(Class<T> type) {
        for (ModuleInfo info : modules) {
            T container = null;
            try {
                container = type.cast(info.getContainerInfo().getContainer());
            } catch (Exception e) {
                // ignore, wrong container
            }
            if (container!=null) {
                return info;
            }
        }
        return null;
    }

    
}
