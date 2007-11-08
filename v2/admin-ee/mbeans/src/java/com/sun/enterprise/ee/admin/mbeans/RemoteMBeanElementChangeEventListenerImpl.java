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

/*
 * RemoteMBeanElementChangeEventListenerImpl.java
 *
 * Created on July 29, 2005, 12:46 AM
 */

package com.sun.enterprise.ee.admin.mbeans;


import com.sun.enterprise.admin.common.JMXFileTransfer;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.MBeanElementChangeEvent;
import com.sun.enterprise.admin.event.MBeanElementChangeEventListener;
import com.sun.enterprise.admin.mbeans.custom.InProcessMBeanElementChangeEventListenerImpl;
import com.sun.enterprise.admin.mbeans.custom.CMBStrings;
import com.sun.enterprise.config.ConfigAdd;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.ee.admin.clientreg.InstanceRegistry;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 */
public class RemoteMBeanElementChangeEventListenerImpl extends InProcessMBeanElementChangeEventListenerImpl
        implements MBeanElementChangeEventListener {
    
    final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
    /** Creates a new instance of RemoteMBeanElementChangeEventListenerImpl */
    public RemoteMBeanElementChangeEventListenerImpl() {
    }

    public void handleUpdate(final MBeanElementChangeEvent event) throws AdminEventListenerException {
        logger.info(CMBStrings.get("cmb.ee.handleUpdate", event.getElementId(), event.EVENT_TYPE));
        super.handleUpdate(event);
    }

    public void handleDelete(final MBeanElementChangeEvent event) throws AdminEventListenerException {
        logger.info(CMBStrings.get("cmb.ee.handleDelete", event.getElementId(), event.EVENT_TYPE));
        super.handleDelete(event);
    }

    public void handleCreate(final MBeanElementChangeEvent event) throws AdminEventListenerException {
        logger.info(CMBStrings.get("cmb.ee.handleCreate", event.getElementId(), event.EVENT_TYPE));
        //synchronize the bits of the classes and ask the super implementation to load the mbean
        try {
            synchronizeMBeanClasses(event);
        } catch (final Exception e) {
            throw new AdminEventListenerException(e);
        }
        super.handleCreate(event);
    }
    
    private void synchronizeMBeanClasses(final MBeanElementChangeEvent event) throws Exception {
        final ArrayList<ConfigAdd> list     = event.getConfigChangeList(); // I know that they are ConfigAdd's
        final ConfigContext rcc             = event.getConfigContext();
        for (final ConfigAdd added : list)  {
            final String xp = added.getXPath();
            if (xp != null) {
                final Object co = rcc.exactLookup(xp);
                if (co instanceof Mbean) {
                    final Mbean     am = (Mbean)co;
                    final String   icn = am.getImplClassName();
                    synchronizeClass(icn, rcc);
                }
            }
        }
    }
    
    private void synchronizeClass(final String c, final ConfigContext rcc) throws Exception {
        final MBeanServerConnection mbsc    = InstanceRegistry.getDASConnection(rcc);
        final String remLoc                 = getAbsoluteDASMBeanClassLocation(mbsc, c);
        final String locLoc                 = getAbsoluteLocalClassFolder(c);
        final JMXFileTransfer ftp           = new JMXFileTransfer(mbsc);
        
        final File destPath = new File(locLoc);
        if (!destPath.exists()) {
            final boolean touched = destPath.mkdirs();
            String msg;
           if(touched)
               msg = CMBStrings.get("cmb.ee.mkdirGood", destPath);
           else
               msg = CMBStrings.get("cmb.ee.mkdirBad", destPath);
            
            logger.info(msg);
        }
        ftp.downloadFile(remLoc, locLoc);
        logger.info(CMBStrings.get("cmb.ee.downloadClass", locLoc));
        //now synchronize possible standard mbean interface
        final String sbiRemLoc = remLoc.substring(0, remLoc.lastIndexOf(".class")) + "MBean.class";
        try {
            ftp.downloadFile(sbiRemLoc, locLoc);
            logger.info(CMBStrings.get("cmb.ee.downloadInterfaceGood", sbiRemLoc));
        } catch(final Exception e) {
            logger.info(CMBStrings.get("cmb.ee.downloadInterfaceBad"));
        }
    }
    
    private String getAbsoluteDASMBeanClassLocation(final MBeanServerConnection mbsc, final String cn) throws Exception {
        final String dasName                    = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;
        final String instanceRootPropertyName   = SystemPropertyConstants.INSTANCE_ROOT_PROPERTY;
        final ObjectName remSSMBeanON           = ObjectNames.getPerInstanceSystemServicesObjectName(dasName);
        final String m                          = "getHostServerSystemPropertyValue";
        final Object[] p                        = new String[]{instanceRootPropertyName};
        final String[] s                        = new String[]{"java.lang.String"};
        
        final String remoteInstanceRoot = (String) mbsc.invoke(remSSMBeanON, m, p, s);
        logger.info(CMBStrings.get("cmb.ee.instanceRoot", remoteInstanceRoot));
        final String remoteMBeansLocation = remoteInstanceRoot + "/applications/mbeans/";  // need to do better here TODO
        logger.info(CMBStrings.get("cmb.ee.remoteLocation", remoteMBeansLocation));
        final String remoteMBeanPath = remoteMBeansLocation + cn.replace('.', '/') + ".class";
        logger.info(CMBStrings.get("cmb.ee.remotePath", remoteMBeanPath));
        
        return ( remoteMBeanPath );
    }
    private String getAbsoluteLocalClassFolder(final String c) throws Exception {
        final String localMBeansLocation    = System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) + "/applications/mbeans/";
        final String localDestinationPath   = localMBeansLocation + cn2dn(c);
        logger.info(CMBStrings.get("cmb.ee.copyLocation",  localDestinationPath));
        
        return ( localDestinationPath );
    }
    private String cn2dn(final String fc) throws Exception {
        String p = ""; //empty string, for classes in "default" anonymous package
        String c = fc;
        if (c.indexOf(".class") != -1) // strip out the ".class" if present
            c = c.substring(0, c.lastIndexOf(".class"));
        if (c.indexOf('.') != -1) {
            p = c.substring(0, c.lastIndexOf('.'));
            p = p.replace('.', '/');
        }
        return ( p );
    }
}