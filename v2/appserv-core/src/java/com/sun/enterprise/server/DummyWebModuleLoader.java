/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.server;

import javax.management.MBeanException;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.deployment.Descriptor;
import java.net.URL;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.event.ApplicationEvent;

/**
 * This is just a dummy module loader for web application. The only responsibility
 * of this class is to generate a notifictation event so that ondemand init code
 * can load the web applications as the appserver starts up.
 * 
 * @author Binod PG
 */
public class DummyWebModuleLoader extends AbstractLoader {

    static Logger _logger=LogDomains.getLogger(LogDomains.LOADER_LOGGER);

    /**
     * Constructs a dummy loader. It generates a descriptor representing 
     * the webapp.
     */
    public DummyWebModuleLoader(String id, ClassLoader loader, WebModulesManager manager) {
        super(id, loader, manager);
        try {
            this.application = manager.getDescriptor(id, manager.getLocation(id));
        } catch (ConfigException confEx) {
            _logger.log(Level.SEVERE,"loader.configexception", confEx);
        }
    }

    //Dummy load. Notification is fired here.
    public boolean doLoad(boolean jsr77) {
        notifyAppEvent(ApplicationEvent.BEFORE_APPLICATION_LOAD);
        loadWebserviceEndpoints(jsr77);
        notifyAppEvent(ApplicationEvent.AFTER_APPLICATION_LOAD);
        return true;
    }

    public boolean unload(boolean jsr77) {
        unloadWebserviceEndpoints(jsr77);
        notifyAppEvent(ApplicationEvent.AFTER_APPLICATION_UNLOAD);
        return true;
    }

    public void createRootMBean () throws MBeanException {
    }

    public void deleteRootMBean () throws MBeanException{
    }

    public void createLeafMBeans () throws MBeanException{
    }

    public void deleteLeafMBeans () throws MBeanException {
    }

    public void createLeafMBean (Descriptor descriptor) throws MBeanException {
    }

    public void deleteLeafMBean (Descriptor descriptor) throws MBeanException {
    }

    public void deleteLeafAndRootMBeans () throws MBeanException {
    }

    public void setState(int state) throws MBeanException {
    }
}
