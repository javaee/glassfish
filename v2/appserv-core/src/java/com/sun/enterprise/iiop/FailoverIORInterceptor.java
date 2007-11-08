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

package com.sun.enterprise.iiop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.logging.LogDomains;

import org.omg.CORBA.Any;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.Encoding;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

import com.sun.enterprise.iiop.IIOPEndpointsInfo;
import com.sun.enterprise.iiop.AlternateIIOPAddressComponent;
import com.sun.enterprise.iiop.AlternateIIOPAddressComponentHelper;

import com.sun.corba.ee.spi.legacy.interceptor.ORBInitInfoExt ;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.SocketInfo;

import com.sun.enterprise.config.serverbeans.IiopListener;
import com.sun.enterprise.config.serverbeans.ServerRef;


/**
 * @author Harold Carr
 */
public class FailoverIORInterceptor
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer, IORInterceptor
{
    private static Logger _logger = null;
    static {
       _logger = LogDomains.getLogger(LogDomains.CORBA_LOGGER);
    }

    private ORB orb ;

    private static final String baseMsg
	= FailoverIORInterceptor.class.getName();
    private static final String ORB_LISTENER = "orb-listener";

    private List currentAddressList;
    private List previousAddressList;
    private List randomizedAddressList;
    private List randomizedAndMarshaledAddressList;

    //
    // Interceptor
    //

    public String name()    { return baseMsg; }
    public void   destroy() { }

    //
    // ORBInitializer
    //

    public FailoverIORInterceptor()
    {
    }

    public void pre_init(ORBInitInfo info) { }

    public void post_init(ORBInitInfo info)
    {
	ORB orb = ((ORBInitInfoExt)info).getORB() ;
	try {
	    info.add_ior_interceptor(new FailoverIORInterceptor(orb));
	    _logger.log(Level.FINE, baseMsg + " registered.");
	} catch (DuplicateName ex) {
            _logger.log(Level.WARNING, "DuplicateName from " + baseMsg , ex);
	    /*
	    RuntimeException rte = 
		new RuntimeException("DuplicateName from " + baseMsg , ex);
	    rte.initCause(ex);
	    throw rte;
	    */
	}
    }

    //
    // IORInterceptor
    //

    public FailoverIORInterceptor( ORB orb ) 
    {
	this.orb = orb ;
    }

    public void establish_components(IORInfo iorInfo)
    {
	try {
	    _logger.log(Level.FINE,
			baseMsg + ".establish_components->:");	 
 
	    ServerRef[] servers = IIOPEndpointsInfo.getServersInCluster();
	    IiopListener[][] listeners = IIOPEndpointsInfo.getListenersInCluster();
              
	    currentAddressList = new ArrayList();
	    if (servers != null) {
	        for (int i = 0; i < servers.length; i++) {
		    String serverName = servers[i].getRef();
		    String hostName =
		      IIOPEndpointsInfo.getHostNameForServerInstance(serverName);
		    if (hostName==null) {
		        hostName = listeners[i][0].getAddress();
		    }		 
		    for (int j = 0; j < listeners[i].length; j++) { 
		        String id = listeners[i][j].getId();
			String port = 
			  IIOPEndpointsInfo.getResolvedPort(listeners[i][j], serverName);
			// REVISIT: Make question an official API.
			if (! id.startsWith(ORB_LISTENER)) {
			    if (_logger.isLoggable(Level.INFO)) {
			        _logger.log(Level.INFO, 
					    baseMsg + ".establish_components:"
					    + " ignoring SSL ports: " +
					    id + " " + hostName + " " + port);
			    }
			    continue;
			}
			
			if (_logger.isLoggable(Level.FINE)) {
			    _logger.log(Level.FINE, 
					baseMsg + ".establish_components:"
					+ " adding AlternateIIOPAddressComponent"
					+ " for listener id: " + id
					+ " address/port: " + hostName
					+ "/" + port);
			}
			
			AlternateIIOPAddressComponent address =
			  new AlternateIIOPAddressComponent(
							    hostName,
							    Integer.decode(port).shortValue());
			
			currentAddressList.add(address);
		    }
		}
	    }

	    if (previousAddressList == null) {

		//
		// Save the previous list for subsequent comparisons.
		//

		previousAddressList = new ArrayList();
		// List does not define .clone and Object.clone is protected.
		Iterator i = currentAddressList.iterator();
		while (i.hasNext()) { previousAddressList.add(i.next()); }

		//
		// Randomize the list for this ORB.
		// Save it in unmarshaled form for logging.
		//

		java.util.Collections.shuffle(currentAddressList);
		randomizedAddressList = currentAddressList;

		if (_logger.isLoggable(Level.FINE)) {
		    _logger.log(Level.FINE, baseMsg 
				+ " first call - saving randomized alternate address list: "
				+ randomizedAddressList);
		}

		//
		// Save a marshaled version for subsequent calls.
		//

		randomizedAndMarshaledAddressList = new ArrayList();
		i = randomizedAddressList.iterator();
		while (i.hasNext()) {
		    AlternateIIOPAddressComponent address =
			(AlternateIIOPAddressComponent) i.next();
		    Any any = orb.create_any();
		    AlternateIIOPAddressComponentHelper.insert(any, address);
		    byte[] data = getCodec(orb).encode_value(any);
		    TaggedComponent tc =
			new TaggedComponent(org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value,
					    //AlternateIIOPAddressComponent.TAG_ALTERNATE_IIOP_ADDRESS_ID,
					    data);
		    randomizedAndMarshaledAddressList.add(tc);
		}
	    } else {
		if (! currentAddressList.equals(previousAddressList)) {
		    throw new RuntimeException(
		        "Current address list: "
			+ currentAddressList
			+ " different from previous list: "
			+ previousAddressList);
		}
	    }

	    Iterator i = randomizedAndMarshaledAddressList.iterator();
	    while (i.hasNext()) {
		iorInfo.add_ior_component((TaggedComponent) i.next());
	    }

	    if (_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, baseMsg 
			    + " adding randomized alternate addresses: "
			    + randomizedAddressList);
	    }
	} catch (Throwable e) {
            _logger.log(Level.WARNING, 
			"Problem in " + baseMsg + ".establish_components", 
			e);
	    /*
	    RuntimeException rte = 
		new RuntimeException("Problem in " + baseMsg + ".establish_components");
	    rte.initCause(e);
	    throw rte;
	    */
	} finally {
	    _logger.log(Level.FINE, baseMsg + ".establish_components<-:");
	}
    }

    public void components_established( IORInfo iorInfo )
    {
    }

    public void adapter_manager_state_changed( int managerId, short state )
    {
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates,
				       short state ) 
    {
    }

    static Codec getCodec(ORB orb)
    {
	Codec result = null;
	try {
	    CodecFactory codecFactory = 
		CodecFactoryHelper.narrow(
                    orb.resolve_initial_references("CodecFactory"));
	    result = codecFactory.create_codec(
                new Encoding((short)ENCODING_CDR_ENCAPS.value, 
			     (byte)1, (byte)2));
	} catch (Throwable e) {
            _logger.log(Level.WARNING, 
			"Problem in " + baseMsg + ".getCodec",
			e);
	}
	return result;
    }
}

// End of file.
