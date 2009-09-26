/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.jk.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.tomcat.util.modeler.Registry;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.ProtocolHandler;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.tcp.RequestInfo;
import com.sun.grizzly.tcp.Constants;
import org.apache.jk.core.JkHandler;
import org.apache.jk.core.Msg;
import org.apache.jk.core.MsgContext;

/** Plugs Jk into Coyote. Must be named "type=JkHandler,name=container"
 *
 * jmx:notification-handler name="org.apache.jk.SEND_PACKET
 * jmx:notification-handler name="com.sun.grizzly.tcp.ACTION_COMMIT
 */
public class JkCoyoteHandler extends JkHandler implements ProtocolHandler {
    protected static final Logger log 
        = Logger.getLogger(JkCoyoteHandler.class.getName());
    // Set debug on this logger to see the container request time

    // ----------------------------------------------------------- DoPrivileged
    private boolean paused = false;
    int epNote;
    Adapter adapter;
    protected JkMain jkMain=null;

    /** Set a property. Name is a "component.property". JMX should
     * be used instead.
     */
    public void setProperty( String name, String value ) {
        if( log.isLoggable(Level.FINEST))
            log.finest("setProperty " + name + " " + value );
        getJkMain().setProperty( name, value );
        properties.put( name, value );
    }

    public String getProperty( String name ) {
        return properties.getProperty(name) ;
    }

    public Iterator getAttributeNames() {
       return properties.keySet().iterator();
    }

    /** Pass config info
     */
    public void setAttribute( String name, Object value ) {
        if( log.isLoggable(Level.FINEST))
            log.finest("setAttribute " + name + " " + value );
        if( value instanceof String )
            this.setProperty( name, (String)value );
    }

    /**
     * Retrieve config info.
     * Primarily for use with the admin webapp.
     */   
    public Object getAttribute( String name ) {
        return getJkMain().getProperty(name);
    }

    /** The adapter, used to call the connector 
     */
    public void setAdapter(Adapter adapter) {
        this.adapter=adapter;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    public JkMain getJkMain() {
        if( jkMain == null ) {
            jkMain=new JkMain();
            jkMain.setWorkerEnv(wEnv);
            
        }
        return jkMain;
    }
    
    boolean started=false;
    
    /** Start the protocol
     */
    public void init() {
        if( started ) return;

        started=true;
        
        if( wEnv==null ) {
            // we are probably not registered - not very good.
            wEnv=getJkMain().getWorkerEnv();
            wEnv.addHandler("container", this );
        }

        try {
            // jkMain.setJkHome() XXX;
            
            getJkMain().init();

        } catch( Exception ex ) {
            log.log(Level.SEVERE, "Error during init",ex);
        }
    }

    public void start() {
        try {
            if( oname != null && getJkMain().getDomain() == null) {
                try {
                    ObjectName jkmainOname = 
                        new ObjectName(oname.getDomain() + ":type=JkMain");
                    Registry.getRegistry(null, null)
                        .registerComponent(getJkMain(), jkmainOname, "JkMain");
                } catch (Exception e) {
                    log.severe( "Error registering jkmain " + e );
                }
            }
            getJkMain().start();
        } catch( Exception ex ) {
            log.log(Level.SEVERE, "Error during startup",ex);
        }
    }

    public void pause() throws Exception {
        if(!paused) {
            paused = true;
            getJkMain().pause();
        }
    }

    public void resume() throws Exception {
        if(paused) {
            paused = false;
            getJkMain().resume();
        }
    }

    public void destroy() {
        if( !started ) return;

        started = false;
        getJkMain().stop();
    }

    
    // -------------------- Jk handler implementation --------------------
    // Jk Handler mehod
    public int invoke( Msg msg, MsgContext ep ) 
        throws IOException {
        if( ep.isLogTimeEnabled() ) 
            ep.setLong( MsgContext.TIMER_PRE_REQUEST, System.currentTimeMillis());
        
        Request req=ep.getRequest();
        Response res=req.getResponse();

        if( log.isLoggable(Level.FINEST) )
            log.finest( "Invoke " + req + " " + res + " " + req.requestURI().toString());
        
        res.setNote( epNote, ep );
        ep.setStatus( MsgContext.JK_STATUS_HEAD );
        RequestInfo rp = req.getRequestProcessor();
        rp.setStage(Constants.STAGE_SERVICE);
        try {
            adapter.service( req, res );
        } catch( Exception ex ) {
            log.log(Level.INFO, "Error servicing request " + req,ex);
        }
        if(ep.getStatus() != MsgContext.JK_STATUS_CLOSED) {
            res.finish();
        }

        req.recycle();
        req.updateCounters();
        res.recycle();
        ep.recycle();
        if( ep.getStatus() == MsgContext.JK_STATUS_ERROR ) {
            return ERROR;
        }
        ep.setStatus( MsgContext.JK_STATUS_NEW );
        rp.setStage(Constants.STAGE_KEEPALIVE);
        return OK;
    }


    public ObjectName preRegister(MBeanServer server,
                                  ObjectName oname) throws Exception
    {
        // override - we must be registered as "container"
        this.name="container";        
        return super.preRegister(server, oname);
    }
}
