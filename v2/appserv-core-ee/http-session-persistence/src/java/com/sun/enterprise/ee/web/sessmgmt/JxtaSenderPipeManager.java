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
 * JxtaSenderPipeManager.java
 *
 * Created on February 8, 2006, 12:10 PM
 *
 * later we can have a pool - for now just a single sender pipe
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.ArrayList;
import net.jxta.pipe.OutputPipe;
//import com.sun.enterprise.ee.web.sessmgmt.JxtaServerPipeWrapper.ConnectionHandler;


/**
 *
 * @author Larry White
 */
public class JxtaSenderPipeManager {
    
    /** Creates a new instance of JxtaSenderPipeManager */
    public JxtaSenderPipeManager() {
    }
    
    /**
     * The singleton instance of JxtaSenderPipeManager
     */    
    private static JxtaSenderPipeManager _soleInstance 
        = new JxtaSenderPipeManager(); 
    
    /** 
     *  Return the singleton instanceReturn the singleton instance
     */
    public static JxtaSenderPipeManager createInstance() {
        return _soleInstance;
    } 
    
    /*
    public void setConnectionHandler(JxtaServerPipeWrapper.ConnectionHandler connectionHandler) {
        _connectionHandler = connectionHandler;
    }
    
    public JxtaServerPipeWrapper.ConnectionHandler getConnectionHandler() {
        //FIXME
        //return _connectionHandler;
        return (JxtaServerPipeWrapper.ConnectionHandler) _connectionHandlers.get(0);
    }
    
    public void addConnectionHandler(JxtaServerPipeWrapper.ConnectionHandler connectionHandler) {
        _connectionHandlers.add(connectionHandler);
    }
     */ 
    
    public PipePool getPipePool() {
        return _pipePool;
    }    
    
    public void setPipePool(PipePool pool) {
        _pipePool = pool;
    }
    
    public PipeWrapper getHealthPipeWrapper() {
        return _healthPipeWrapper;
    }    
    
    public void setHealthPipeWrapper(PipeWrapper healthPipeWrapper) {
        _healthPipeWrapper = healthPipeWrapper;
    }
    
    public String getPartnerInstanceName() {
        String result = null;
        PipeWrapper healthPipeWrapper = getHealthPipeWrapper();
        if(healthPipeWrapper == null) {
            return result;
        }
        result = healthPipeWrapper.getPartnerInstanceName();
        return result;
    }
    
    public boolean isOurPartnerInstance(String instanceName) { 
        String partnerInstance = this.getPartnerInstanceName();
        if(partnerInstance == null) {
            return false;
        }
        return partnerInstance.equalsIgnoreCase(instanceName);
    }
    
    OutputPipe getPropagatedOutputPipe() {
        if(this.getJxtaBiDiPipeWrapper() == null) {
            return null;
        }
        synchronized(this) {
            if(_outputPipe == null) {
                _outputPipe 
                    = this.getJxtaBiDiPipeWrapper().createPropagatedOutputPipe();
            }
        }
        return _outputPipe;
    }    
    
    void setPropagatedOutputPipe(OutputPipe outputPipe) {
        _outputPipe = outputPipe;
    }   
    
    public void initPipePool(ArrayList pipeWrappers) {
        _pipePool = new PipePool(pipeWrappers);
        _pooledWrappers = pipeWrappers;
    }
    
    public void closePooledPipes() {
        for(int i=0; i<_pooledWrappers.size(); i++) {
            PipeWrapper nextWrapper = (PipeWrapper)_pooledWrappers.get(i);
            nextWrapper.cleanup();
        }
    }
    
    void closeHealthPipeWrapper() {
        System.out.println("JxtaSenderPipeManager>>closeHealthPipeWrapper");
        PipeWrapper healthPipeWrapper = this.getHealthPipeWrapper();
        if(healthPipeWrapper != null) {
            healthPipeWrapper.cleanup();
        }
        _healthPipeWrapper = null;
    }    
    
    public void closePropagatedOutputPipe() {
        try {
            _outputPipe.close();
        } catch(Exception ex) {
            ;
        }
        _outputPipe = null;
    }
    
    void setJxtaBiDiPipeWrapper(JxtaBiDiPipeWrapper jxtaBiDiPipeWrapper) {
        _jxtaBiDiPipeWrapper = jxtaBiDiPipeWrapper;
    }
    
    JxtaBiDiPipeWrapper getJxtaBiDiPipeWrapper() {
        return _jxtaBiDiPipeWrapper;
    }    
    
    //for now a single sender pipe - later a collection
    //private JxtaServerPipeWrapper.ConnectionHandler _connectionHandler = null;
    //private ArrayList _connectionHandlers = new ArrayList();
    private ArrayList _pooledWrappers = new ArrayList();
    private PipePool _pipePool = null;
    private PipeWrapper _healthPipeWrapper = null;
    private OutputPipe _outputPipe = null;
    private JxtaBiDiPipeWrapper _jxtaBiDiPipeWrapper = null;
    
}
