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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.omg.CORBA.ORB;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;

import com.sun.corba.ee.spi.transport.SocketInfo;
import com.sun.corba.ee.spi.legacy.connection.GetEndPointInfoAgainException;
import com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.impl.legacy.connection.EndPointInfoImpl;

import java.util.logging.*;
import com.sun.logging.*;

import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.Encoding;

import java.util.Hashtable;
import org.omg.CORBA.Any;
import org.omg.IOP.TaggedComponent;

/**
 *
 * 
 */
public class SocketFactory extends com.sun.corba.ee.impl.legacy.connection.DefaultSocketFactory {

    private static Logger _logger=null;
    static {
       _logger=LogDomains.getLogger(LogDomains.CORBA_LOGGER);
    }
    
    private Hashtable endpointTable = new Hashtable();
    private Codec codec;

    /** Creates a new instance of SocketFactory */
    public SocketFactory() {
        super();
    }

    public SocketInfo getEndPointInfo(ORB orb, IOR ior,
                    SocketInfo endPointInfo) {
        try {
            IIOPProfileTemplate temp = (IIOPProfileTemplate)ior.
		                 getProfile().getTaggedProfileTemplate();
            IIOPAddress primary = temp.getPrimaryAddress() ;
            String host = primary.getHost().toLowerCase();
            int port = primary.getPort();
      
	    if(_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE,"ENDPOINT INFO:host=" +host + ", port=" + port);
	    }
            
            // If there is already a cached EndPointImpl for the primary host and
            // port return that.
            if ((endPointInfo = (EndPointInfoImpl)endpointTable.get(host + port)) != null)
                return endPointInfo;
            
            TaggedComponent alternateAddressTaggedComponents[] = 
                       ior.getProfile().getTaggedProfileTemplate().
			getIOPComponents((com.sun.corba.ee.spi.orb.ORB)orb,
                               org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value
	                       //AlternateIIOPAddressComponent.TAG_ALTERNATE_IIOP_ADDRESS_ID
					 );
            if (alternateAddressTaggedComponents.length > 0) {
                getCodec(orb);
                for (int i = 0; i < alternateAddressTaggedComponents.length; i++) {
                    byte[] data = alternateAddressTaggedComponents[i].component_data;
                    Any any = null;
                    try {
                        any = codec.decode_value(data,
                                 AlternateIIOPAddressComponentHelper.type());
                    } catch (org.omg.IOP.CodecPackage.TypeMismatch e) {
                        if (_logger.isLoggable(Level.FINE)){
                            _logger.log(Level.FINE,"Exception codec TypeMismatch",e);
			}
                        throw new RuntimeException(e.toString());
                    } catch (org.omg.IOP.CodecPackage.FormatMismatch e) {
                        if (_logger.isLoggable(Level.FINE) ){
                            _logger.log(Level.FINE,"Exception codec FormatMismatch",e);
			}
                        throw new RuntimeException(e.toString());
                    }
                    AlternateIIOPAddressComponent iiopAddress = 
                        AlternateIIOPAddressComponentHelper.extract(any);
                    // if any host:port has an EndPointImpl in the endpointTable
                    // we should make sure we set the primary host:port with
                    // that EndPointImpl in the endpointTable and return that. 
                    // Subsequently we will not even get here, since for the 
                    // primary host:port the cached value will be returned
                    if ((endPointInfo = (EndPointInfoImpl)endpointTable.
                            get(iiopAddress.host + iiopAddress.port)) != null) {
                                /*
                       if (!type.equals(ORBSocketFactory.IIOP_CLEAR_TEXT)) {
                           // Use this host address to make the connection, since a connection
                           // has already been made using this address, just use the secure port.
                           // It is assumed that the primary host address in the IOR is also 
                           // one of the taggeded alternate addresses, which is a fair assumption. 
                           // Hence the remote machine should be reacheable thru the cached 
                           // host address and new secure port being specified
                           endPointInfo = new EndPointInfoImpl(type, iiopAddress.host, port);
                           endpointTable.put(host + port, endPointInfo);
                           return endPointInfo;
                       } else { */
                           endpointTable.put(host + port, endPointInfo);
                           return endPointInfo;
                       //}
                    }
                }
                // In case nothing is found use primary host:port
                endPointInfo = new EndPointInfoImpl(ORBSocketFactory.IIOP_CLEAR_TEXT, port, host);
                endpointTable.put(host + port, endPointInfo);
           } else {
                
                endPointInfo = new EndPointInfoImpl(ORBSocketFactory.IIOP_CLEAR_TEXT, port, host);
                endpointTable.put(host + port, endPointInfo);
            }
            return endPointInfo;
        } catch ( Exception ex ) {
            if (_logger.isLoggable(Level.FINE)){
                _logger.log(Level.FINE,"Exception getting End point info",ex);
	    }
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    private Codec getCodec(ORB orb)
    {
        if (codec == null) {
            synchronized (this) {
                CodecFactory codecFactory = null;
                try {
                    codecFactory = CodecFactoryHelper.narrow(
                            orb.resolve_initial_references("CodecFactory"));
                } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                    System.out.println("Getting org.omg.CORBA.ORBPackage.InvalidName exception");
                }
                Encoding encoding = new Encoding((short)0, (byte)1, (byte)2);
                try {
                    codec = codecFactory.create_codec(encoding);
                } catch (org.omg.IOP.CodecFactoryPackage.UnknownEncoding e) {
                    System.out.println("Getting org.omg.IOP.CodecFactoryPackage.UnknownEncoding exception");
                }
            }
        }
        return codec;
    }

    private int shortToInt( short value )
    {
        if (value < 0)
            return value + 65536 ;
        return value ;
    }
        
}
