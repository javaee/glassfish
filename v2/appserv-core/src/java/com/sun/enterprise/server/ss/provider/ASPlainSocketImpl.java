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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.security.PrivilegedActionException;
import com.sun.enterprise.server.ss.spi.ASSocketFacadeUtils;
import com.sun.enterprise.server.ss.spi.ASSocketServiceFacade;
import com.sun.logging.LogDomains;

/**
 * Wrapper socketimpl above JDK socketimpl implementation.
 * This will be used for any outbound communication.
 */
public class ASPlainSocketImpl {
    
    private SocketImpl si = null;
    private final String SOCKET_IMPL_CLASS = "java.net.SocksSocketImpl";    
    private static final Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    public ASPlainSocketImpl() {
        try {
            setup();
        } catch (PrivilegedActionException ex) {
            logger.log(Level.FINE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Introspect all the class and create all the method objects.
     * The JDK socket impl implementation has all protected methods
     * and the only way to wrap it is by using setAccessible.
     *
     * The method can be either in the SocksSocketImpl or in any 
     * of the parent classes.
     */
    private void setup() 
    throws PrivilegedActionException {
        Object impl = java.security.AccessController.doPrivileged
            (new java.security.PrivilegedExceptionAction() {
            public java.lang.Object run() throws Exception {
               Class tmpClass = Class.forName(SOCKET_IMPL_CLASS);            
               Constructor cons = tmpClass.getDeclaredConstructor(new Class[] {});
               cons.setAccessible(true);            
               Object obj = cons.newInstance( (Class[])null);
               while (tmpClass.getName().
               equalsIgnoreCase("JAVA.NET.SOCKETIMPL") == false) {
                   _setupMethods(tmpClass);      
                   tmpClass = tmpClass.getSuperclass();
               }
               return obj;
            }
        });

        this.si = (SocketImpl) impl;
    }
    
    /**
     * Compare any non-private method. The java.net.socketimpl class
     * has abtsract protected methods. So, its implementation is 
     * has to have atleast protected methods.
     */
    private void _setupMethods(Class siClass) {
        Method[] methods = siClass.getDeclaredMethods();
        for (Method m : methods) {
            if (Modifier.isPrivate(m.getModifiers())) {
                continue;
            }            
            m.setAccessible(true);
            if (m.getName().equalsIgnoreCase("CREATE")) {
                if (createMethod == null)
                    createMethod = m;
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("CONNECT")) {
                if (m.getParameterTypes()[0].equals(String.class)) {
                    if (connectMethod1 == null)
                        connectMethod1 = m;
                } else if (m.getParameterTypes()[0].equals(InetAddress.class)){
                    if (connectMethod2 == null)
                        connectMethod2 = m;
                } else {
                    if (connectMethod3 == null)
                        connectMethod3 = m;
                }
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("BIND")) {
                if (bindMethod == null)
                    bindMethod = m;
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("LISTEN")) {
                if (listenMethod == null)
                    listenMethod = m;
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("ACCEPT")) {
                if (acceptMethod == null)
                    acceptMethod = m;
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("GETINPUTSTREAM")) {
                if (getInputStreamMethod == null)
                    getInputStreamMethod = m;
                continue;
            }            
            
            if (m.getName().equalsIgnoreCase("GETOUTPUTSTREAM")) {
                if (getOutputStreamMethod == null)
                    getOutputStreamMethod = m;
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("AVAILABLE")) {
                if (availableMethod == null)
                    availableMethod = m;
                continue;
            }
            
            
            if (m.getName().equalsIgnoreCase("CLOSE")) {
                if (closeMethod == null)
                    closeMethod = m;
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("SHUTDOWNINPUT")) {
                if (shutdownInputMethod == null)
                    shutdownInputMethod = m;
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("SHUTDOWNOUTPUT")) {
                if (shutdownOutputMethod == null)
                    shutdownOutputMethod = m;
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("SENDURGENTDATA")) {
                if (sendUrgentDataMethod == null)
                    sendUrgentDataMethod = m;
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("GETOPTION")) {
                if (getOptionMethod == null)
                    getOptionMethod = m;
                continue;
            }
                        
            if (m.getName().equalsIgnoreCase("SETOPTION")) {
                if (setOptionMethod == null)
                    setOptionMethod = m;
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("GETPORT")) {
                if (getPortMethod == null)
                    getPortMethod = m;
                continue;
            }

            if (m.getName().equalsIgnoreCase("GETINETADDRESS")) {
                if (getInetAddressMethod == null)
                    getInetAddressMethod = m;
                continue;
            }

            
            if (m.getName().equalsIgnoreCase("GETLOCALPORT")) {
                if (getLocalPortMethod == null)
                    getLocalPortMethod = m;
                continue;
            }
            
            if (m.getName().equalsIgnoreCase("SUPPORTSURGENTDATA")) {
                if (supportsUrgentDataMethod == null)
                    supportsUrgentDataMethod = m;
                continue;
            }
        }
    }
    
    private Method createMethod = null;
    protected void create(boolean stream) throws IOException {
        _invoke(createMethod, new Object[] {new Boolean(stream)});
    }

    private Method connectMethod1 = null;
    protected void connect(String host, int port) throws IOException {
        boolean waitForStartupReqd = ! ASSocketFacadeUtils.getASSocketService().
                                      socketServiceNotified(port);

        _invoke(connectMethod1, new Object[] {host, new Integer(port)});

        if (ASSocketFacadeUtils.getASSocketService().
        isLocalClient(InetAddress.getByName(host)) == false) {
            return;
        }

        if (waitForStartupReqd) {
            ASSocketFacadeUtils.getASSocketService().
            waitOnClientConnection(getPort());
        }


	ASSocketFacadeUtils.getASSocketService().clientSocketConnected(
                                                 getPort(), getLocalPort());
    }
    
    private Method connectMethod2 = null;
    protected void connect(InetAddress address, int port) throws IOException {
        boolean waitForStartupReqd = ! ASSocketFacadeUtils.getASSocketService().
                                      socketServiceNotified(port);
        _invoke(connectMethod2, new Object[] {address, new Integer(port)});

        if (ASSocketFacadeUtils.getASSocketService().
        isLocalClient(address) == false) {
            return;
        }

        if (waitForStartupReqd) {
            ASSocketFacadeUtils.getASSocketService().
            waitOnClientConnection(getPort());
        }
	ASSocketFacadeUtils.getASSocketService().clientSocketConnected(
                                                 getPort(), getLocalPort());
    }
    
    private Method connectMethod3 = null;
    protected void connect(SocketAddress address, int timeout) throws IOException {
        InetSocketAddress isa = (InetSocketAddress) address;
        boolean waitForStartupReqd = ! ASSocketFacadeUtils.getASSocketService().
                                      socketServiceNotified(isa.getPort());
        _invoke(connectMethod3, new Object[] {address, new Integer(timeout)});

        if (ASSocketFacadeUtils.getASSocketService().
        isLocalClient(isa.getAddress()) == false) {
            return;
        }

        if (waitForStartupReqd) {
            ASSocketFacadeUtils.getASSocketService().
            waitOnClientConnection(getPort());
        }
	ASSocketFacadeUtils.getASSocketService().clientSocketConnected(
                                                 getPort(), getLocalPort());
    }
    
    private Method bindMethod = null;
    protected void bind(InetAddress host, int port) throws IOException {
        _invoke(bindMethod, new Object[] {host, new Integer(port)});
    }
    
    private Method listenMethod = null;
    protected void listen(int backlog) throws IOException {
        _invoke(listenMethod, new Object[] {new Integer(backlog)});
    }

    private Method acceptMethod = null;
    protected void accept(SocketImpl s) throws IOException {
        _invoke(acceptMethod, new Object[] {s});
    }

    private Method getInputStreamMethod = null;
    protected InputStream getInputStream() throws IOException {
        return (InputStream) _invoke(getInputStreamMethod, null);
    }

    private Method getOutputStreamMethod = null;
    protected OutputStream getOutputStream() throws IOException {
        return (OutputStream) _invoke(getOutputStreamMethod, null);
    }

    private Method availableMethod = null;
    protected int available() throws IOException {
        return  (Integer) _invoke(availableMethod, null);
    }

    private Method closeMethod = null;
    protected void close() throws IOException {
        _invoke(closeMethod, null);
    }

    private Method shutdownInputMethod = null;
    protected void shutdownInput() throws IOException {
        _invoke(shutdownInputMethod, null);
    }

    private Method shutdownOutputMethod = null;
    protected void shutdownOutput() throws IOException {
        _invoke(shutdownOutputMethod, null);
    }

    private Method sendUrgentDataMethod = null;
    protected void sendUrgentData(int data) throws IOException {
        _invoke(sendUrgentDataMethod, new Object[] {new Integer(data)});
    }

    private Method setOptionMethod = null;
    public void setOption(int optID, Object value) throws SocketException {
        try {
            _invoke(setOptionMethod, new Object[] {new Integer(optID), value});
        } catch (IOException ie) {
            SocketException se = new SocketException(ie.getMessage());
            throw (SocketException) se.initCause(ie);
        }
    }

    private Method getOptionMethod = null;
    public Object getOption(int optID) throws SocketException {
        try {
            return  _invoke(getOptionMethod, new Object[] {new Integer(optID)});        
        } catch (IOException ie) {
            SocketException se = new SocketException(ie.getMessage());
            throw (SocketException) se.initCause(ie);
        }
    }

    private Method getPortMethod = null;
    public int getPort() {
        try {
            return  (Integer) _invoke(getPortMethod, null);        
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Method supportsUrgentDataMethod = null;
    public boolean supportsUrgentData () {
        try {
            return  (Boolean) _invoke(supportsUrgentDataMethod, null);        
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    private Method getInetAddressMethod = null;
    public InetAddress getInetAddress() {
        try {
            return  (InetAddress) _invoke(getInetAddressMethod, null);
        } catch (IOException ex) {
            logger.log(Level.FINER, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }


    private Method getLocalPortMethod = null;
    public int getLocalPort() {
        try {
            return  (Integer) _invoke(getLocalPortMethod, null);        
        } catch (IOException ex) {
            logger.log(Level.FINER, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }
    
    private Object _invoke(final Method m, final Object[] args) 
    throws SocketException, IOException {
        try {
            return java.security.AccessController.doPrivileged
            (new java.security.PrivilegedExceptionAction() {
                 public java.lang.Object run() throws Exception {
                     return m.invoke(si, args);            
                 }
            });
        } catch (PrivilegedActionException ex) {
            logger.log(Level.FINER, ex.getMessage(), ex);
            Throwable tmpEx = ex;
            if ( ex.getCause() != null) {
                 tmpEx = ex.getCause();
            }

            if (tmpEx instanceof InvocationTargetException) {
                 Throwable e = 
                 ((InvocationTargetException) tmpEx).getTargetException();
                 if (e != null) {
                     tmpEx = e;
                 }
            }
                
            logger.log(Level.FINER, tmpEx.getMessage(), tmpEx);
            if (tmpEx instanceof SocketException) {
                throw (SocketException) tmpEx;
            } else if (tmpEx instanceof IOException) {
                throw (IOException) tmpEx;
            } else {
                IOException ie = new IOException(tmpEx.getMessage());
                throw (IOException) ie.initCause(tmpEx);
            }
        } catch (Exception ex) {
            logger.log(Level.FINER, ex.getMessage(), ex);
            IOException ie = new IOException(ex.getMessage());
            throw (IOException) ie.initCause(ex);
        }    
    }      
}
