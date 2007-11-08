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
package com.sun.enterprise.server.ss.provider;

import java.nio.channels.spi.*;
import java.nio.channels.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Application Server's implementation of SelectorProvider, 
 * which wraps the JDK's default SelectorProvider obtained by
 * sun.nio.ch.DefaultSelectorProvider.create().
 * This wrapping is necessary to provide special implementations of
 * ServerSocketChannel and Selector.
 */
public class ASSelectorProvider extends SelectorProvider {

    public static final int PORT_UNBOUND = 0;
    public static final int PORT_BOUND = 1;
    public static final int PORT_CONFLICT = 2;
   
    private SelectorProvider provider = null;

    private HashMap<Integer, Integer> statemap = 
        new HashMap<Integer, Integer>();
    private HashMap<Integer, ServerSocket> socketmap = 
        new HashMap<Integer, ServerSocket>();

    public ASSelectorProvider() {
        provider = getProvider();
    }

    public DatagramChannel openDatagramChannel() throws IOException {
        return provider.openDatagramChannel();
    }

    public Pipe openPipe() throws IOException {
        return provider.openPipe();
    }

    public ServerSocketChannel openServerSocketChannel() throws IOException {
        ServerSocketChannel ssc = provider.openServerSocketChannel();
        return new ASServerSocketChannel(ssc, this);
    }

    public SocketChannel openSocketChannel() throws IOException {
        //SocketChannel sc = provider.openSocketChannel();
        //return new ASSocketChannel(sc, this);
        return provider.openSocketChannel();
    }

    public AbstractSelector openSelector() throws IOException {
        AbstractSelector sel =  provider.openSelector();
        return new ASSelector(sel, this);
    }


    ServerSocket getServerSocket(int port) {
        return socketmap.get(port);
    }

    int getPortState(int port) {
        Integer i = statemap.get(port);
        if (i == null) {
            return PORT_UNBOUND;
        } else {
            return i.intValue();
        }
    }

    public void setPortState(int port, int state) {
        statemap.put(port, state);
    }

    public void clear(int port) {
        statemap.remove(port);
        socketmap.remove(port);
    }


    void setServerSocket(ServerSocket ss, int port) {
        int state = getPortState(port);

	/**
        if  (state < 3)  {
            state++;
            statemap.put(s, new Integer(state));
        }
	**/

        if (state == PORT_UNBOUND) {
            socketmap.put(port, ss);
            setPortState(port, PORT_BOUND);
        }
	else if (state == PORT_BOUND) {
            setPortState(port, PORT_CONFLICT);
	}
    }

    private SelectorProvider getProvider() { 
        SelectorProvider sp = null;
        try {
            Class clazz = Class.forName( "sun.nio.ch.DefaultSelectorProvider" );
            java.lang.reflect.Method createMeth = clazz.getMethod("create", new Class[] {});
            sp = (SelectorProvider) createMeth.invoke( null, new Object[] {} ); 
        } catch( Exception e ) {
	    throw new RuntimeException("Unable to create default SelectorProvider.", e);
	}

	return sp;
    }


}

