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

import java.lang.reflect.Constructor;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TSIdentification;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.Current;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.Encoding;
import org.omg.IOP.ENCODING_CDR_ENCAPS;

import com.sun.ejb.base.sfsb.util.EJBServerConfigLookup;
import com.sun.enterprise.Switch;
import com.sun.enterprise.log.Log;

import com.sun.enterprise.iiop.security.SecClientRequestInterceptor;
import com.sun.enterprise.iiop.security.SecServerRequestInterceptor;
import com.sun.enterprise.iiop.security.SecurityService;
import com.sun.enterprise.iiop.security.SecurityServiceImpl;
import com.sun.enterprise.iiop.security.Csiv2Manager;

import com.sun.corba.ee.spi.legacy.interceptor.ORBInitInfoExt;
import com.sun.jts.pi.InterceptorImpl;
import java.util.logging.*;
import com.sun.logging.*;

/**
 * This file implements an initializer class for all portable interceptors
 * used in the J2EE RI (currently security and transactions).
 * It registers the IOR, client and server request interceptors.
 *
 * @author Vivek Nagar
 */

public class J2EEInitializer extends org.omg.CORBA.LocalObject
		implements ORBInitializer
{
    private static Logger _logger=null;
    static{
       _logger=LogDomains.getLogger(LogDomains.CORBA_LOGGER);
        }

    private static final String SEC_INTEROP_CLIENTINT_PROP = "interop.secinterceptor.client";
    private static final String SEC_INTEROP_SERVERINT_PROP = "interop.secinterceptor.server";

    public J2EEInitializer() {
	try {
	    System.setProperty(
			com.sun.jts.pi.InterceptorImpl.CLIENT_POLICY_CHECKING, 
			String.valueOf(false));
	} catch ( Exception ex ) {
		_logger.log(Level.WARNING,"iiop.readproperty_exception",ex);
	}
    } 

    /**
     * This method is called during ORB initialization.
     * @param the info object that provides initialization attributes
     *        and operations by which interceptors are registered.
     */
    public void pre_init(org.omg.PortableInterceptor.ORBInitInfo info)
    {
    }

    /**
     * This method is called during ORB initialization.
     * @param the info object that provides initialization attributes
     *        and operations by which interceptors are registered.
     */
    public void post_init(org.omg.PortableInterceptor.ORBInitInfo info) 
    {
        Codec codec = null;

	if(_logger.isLoggable(Level.FINE)){
		_logger.log(Level.FINE,"J2EE Initializer post_init");
		// Create a Codec that can be passed to interceptors.
		_logger.log(Level.FINE,"Creating Codec for CDR encoding");
	}

        CodecFactory cf = info.codec_factory();
  
        byte major_version = 1;
        byte minor_version = 2;
        Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, 
                                         major_version, minor_version);
        try {
            codec = cf.create_codec(encoding);

	    // register CSIv2 interceptors.
 	    ClientConnectionInterceptor cci =
                 new ClientConnectionInterceptor("ClientConnInterceptor",1);
            info.add_client_request_interceptor(cci);

            String clientSecInterceptor = System.getProperty(SEC_INTEROP_CLIENTINT_PROP);
            String serverSecInterceptor = System.getProperty(SEC_INTEROP_SERVERINT_PROP);

            ClientRequestInterceptor creq;
            ServerRequestInterceptor sreq;

            if( clientSecInterceptor == null ) {
                creq = new SecClientRequestInterceptor(
                    "SecClientRequestInterceptor", codec);
            }
            else {
                try {
                    Class cInterceptorClass = 
                        Class.forName( clientSecInterceptor );

                    // Find two-parameter constructor:
                    Class[] paramTypes = new Class[2];
                    paramTypes[0] = java.lang.String.class;
                    paramTypes[1] = org.omg.IOP.Codec.class;

                    Object[] params = new Object[2];
                    params[0] = "SecClientRequestInterceptor";
                    params[1] = codec;

                    Constructor constructor = cInterceptorClass.getConstructor( 
                        paramTypes );

                    creq = (ClientRequestInterceptor)
                        constructor.newInstance( params );
                }
                catch( Exception e ) {
		    if (_logger.isLoggable(Level.FINE)) {
			_logger.log(Level.FINE,"Exception registering security client request receptor",e);
			_logger.log(Level.FINE,"Going to register default security client request interceptor");
		    }
                    creq = new SecClientRequestInterceptor(
                        "SecClientRequestInterceptor", codec);
                }
            }

            if( serverSecInterceptor == null ) {
                sreq = new SecServerRequestInterceptor(
                    "SecServerRequestInterceptor", codec);
            }
            else {
                try {
                    Class sInterceptorClass = 
                        Class.forName( serverSecInterceptor );

                    // Try two-parameter form of constructor:
                    Class[] paramTypes = new Class[2];
                    paramTypes[0] = java.lang.String.class;
                    paramTypes[1] = org.omg.IOP.Codec.class;

                    Object[] params = new Object[2];
                    params[0] = "SecServerRequestInterceptor";
                    params[1] = codec;

                    Constructor constructor = sInterceptorClass.getConstructor( 
                        paramTypes );

                    sreq = (ServerRequestInterceptor)
                        constructor.newInstance( params );
                }
                catch( Exception e ) {
		    if (_logger.isLoggable(Level.FINE)) {
			_logger.log(Level.FINE,"Exception registering security server request receptor",e);
			_logger.log(Level.FINE,"Going to register default security server request interceptor");
		    }
                    sreq = new SecServerRequestInterceptor(
                        "SecServerRequestInterceptor", codec);
                }
            }

            info.add_client_request_interceptor(creq);

            ServerConnectionInterceptor sci =
                new ServerConnectionInterceptor(2);
            info.add_server_request_interceptor(sci);
            info.add_server_request_interceptor(sreq);

	    SecurityService ss = new SecurityServiceImpl();
	    Csiv2Manager.setSecurityService(ss);


	    // register JTS interceptors
	    // first get hold of PICurrent to allocate a slot for JTS service.
	    Current pic = (Current)info.resolve_initial_references("PICurrent");

	    // allocate a PICurrent slotId for the transaction service.
	    int[] slotIds = new int[2];
	    slotIds[0] = info.allocate_slot_id();
	    slotIds[1] = info.allocate_slot_id();

	    InterceptorImpl interceptor =
                new InterceptorImpl(pic, codec, slotIds, null);
            info.add_client_request_interceptor(interceptor);
            info.add_server_request_interceptor(interceptor);

        boolean addSFSBInterceptors = false;
        if (Switch.getSwitch().getContainerType() != Switch.APPCLIENT_CONTAINER) {
            addSFSBInterceptors = EJBServerConfigLookup.needToAddSFSBVersionInterceptors();
        }
           
        _logger.log(Level.FINE, "J2EEInitializer: Checking if interceptors need to "
                + " be added");
        if (addSFSBInterceptors) {
            SFSBClientRequestInterceptor sfsbClientInterceptor =
                new SFSBClientRequestInterceptor(codec);
            SFSBServerRequestInterceptor sfsbServerInterceptor =
                new SFSBServerRequestInterceptor(codec);
            info.add_client_request_interceptor(sfsbClientInterceptor);
            info.add_server_request_interceptor(sfsbServerInterceptor);
            _logger.log(Level.FINE, "J2EEInitializer: Added SFSBInterceptors");
        }
        
	    // Get the ORB instance on which this interceptor is being
	    // initialized
	    com.sun.corba.ee.spi.orb.ORB thisORB = ((ORBInitInfoExt)info).getORB();

	    PEORBConfigurator.setJTSInterceptor(interceptor, thisORB);

	    // add IOR Interceptor for CSIv2 and OTS tagged components
	    TxSecIORInterceptor iorInterceptor = new TxSecIORInterceptor(codec);
	    info.add_ior_interceptor(iorInterceptor);

        if (addSFSBInterceptors) {
            info.add_ior_interceptor(new SFSBVersionIORInterceptor(codec));
        }
        
        } catch (Exception e) {
	    if(_logger.isLoggable(Level.FINE)){
		_logger.log(Level.FINE,"Exception registering JTS interceptors",e);
	    }
	    throw new RuntimeException(e.getMessage());
        }
        
    }
}

