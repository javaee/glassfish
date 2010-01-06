/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

//
// Created       : 2005 Jul 29 (Fri) 08:23:33 by Harold Carr.
// Last Modified : 2005 Aug 31 (Wed) 19:57:12 by Harold Carr.
//

package org.glassfish.enterprise.iiop.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.CSIv2SSLTaggedComponentHandler;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.spi.orbutil.ORBConstants;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;

// END imports for getSocketInfo code

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.logging.LogDomains;
//
import org.glassfish.enterprise.iiop.api.IIOPSSLUtil;
import org.glassfish.internal.api.Globals;

/**
 * @author Harold Carr
 */
public class CSIv2SSLTaggedComponentHandlerImpl
    extends org.omg.CORBA.LocalObject
    implements CSIv2SSLTaggedComponentHandler,
	       ORBConfigurator
{
    private static Logger _logger = null;
    static {
        _logger = LogDomains.getLogger(CSIv2SSLTaggedComponentHandlerImpl.class, LogDomains.CORBA_LOGGER);
    }

    private final String baseMsg = 
	CSIv2SSLTaggedComponentHandlerImpl.class.getName();

    private ORB orb;

    ////////////////////////////////////////////////////
    //
    // CSIv2SSLTaggedComponentHandler
    //

    public TaggedComponent insert(IORInfo iorInfo, 
 				  List<ClusterInstanceInfo> clusterInstanceInfo)
    {
	TaggedComponent result = null;
	try {
	    if (_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, baseMsg + ".insert->:");
	    }

            List<com.sun.corba.ee.spi.folb.SocketInfo> socketInfos = new ArrayList<com.sun.corba.ee.spi.folb.SocketInfo>();
            for(ClusterInstanceInfo clInstInfo : clusterInstanceInfo){
                for(int endPIndex=0; endPIndex < clInstInfo.endpoints.length; endPIndex++){
                    com.sun.corba.ee.spi.folb.SocketInfo socketInfo = clInstInfo.endpoints[endPIndex];
                    if(socketInfo.type.equals("SSL") || socketInfo.type.equals("SSL_MUTUALAUTH")){
                        socketInfos.add(socketInfo);                  
                    }
                }                
            }
            IIOPSSLUtil sslUtil = null;
            if (Globals.getDefaultHabitat() != null) {
                sslUtil = Globals.getDefaultHabitat().getComponent(IIOPSSLUtil.class);
                return sslUtil.createSSLTaggedComponent(iorInfo, socketInfos);
            } else {
                return null;
            }
           
	} finally {
	    if (_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, baseMsg + ".insert<-: " + result);
	    }
	}
    }

    public List<SocketInfo> extract(IOR ior)
    {
	List<SocketInfo> socketInfo = null;
        try {
	    if (_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, baseMsg + ".extract->:");
	    }
            IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate)ior.
                                 getProfile().getTaggedProfileTemplate();
            IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress() ;
            String host = primary.getHost().toLowerCase();

            IIOPSSLUtil sslUtil = null;
            if (Globals.getDefaultHabitat() != null) {
                sslUtil = Globals.getDefaultHabitat().getComponent(IIOPSSLUtil.class);
                socketInfo = (List<SocketInfo>)sslUtil.getSSLPortsAsSocketInfo(ior);
            }
            if (socketInfo == null) {
                if (_logger.isLoggable(Level.FINE)) {
		    _logger.log(Level.FINE, baseMsg
				+ ".extract: did not find SSL SocketInfo");
		}
            } else {
                if (_logger.isLoggable(Level.FINE)) {
		    _logger.log(Level.FINE, baseMsg
				+ ".extract: found SSL socketInfo");
		}
            }        
	    if (_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, baseMsg 
			    + ".extract: Connection Context");		
	    }
        } catch ( Exception ex ) {
	    _logger.log(Level.WARNING, "Exception getting SocketInfo", ex);
        } finally {
	    if (_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, baseMsg + ".extract<-: " + socketInfo);
	    }
	}
	return socketInfo;
    }

    ////////////////////////////////////////////////////
    //
    // ORBConfigurator
    //

    public void configure(DataCollector collector, ORB orb) 
    {
	if (_logger.isLoggable(Level.FINE)) {
	    _logger.log(Level.FINE, ".configure->:");
	}

	this.orb = orb;
	try {
	    orb.register_initial_reference(
	        ORBConstants.CSI_V2_SSL_TAGGED_COMPONENT_HANDLER,
	        this);
	} catch (InvalidName e) {
	    _logger.log(Level.WARNING, ".configure: ", e);
	}

	if (_logger.isLoggable(Level.FINE)) {
	    _logger.log(Level.FINE, ".configure<-:");
	}
    }
}

// End of file.


