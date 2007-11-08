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
 * JxtaStarter.java
 *
 * Created on February 8, 2006, 2:06 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.IOException;
import java.util.HashMap;

import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.peer.PeerID;
import net.jxta.pipe.PipeID;
import com.sun.enterprise.jxtamgmt.NetworkManager;
import com.sun.enterprise.jxtamgmt.NetworkManagerProxy;
import com.sun.enterprise.jxtamgmt.JxtaUtil;

import com.sun.enterprise.ee.cms.core.*;
import com.sun.enterprise.web.ServerConfigLookup;

/**
 *
 * @author Larry White
 */
public class JxtaStarter {
    
    private PeerGroup netPeerGroup = null;
    private RendezVousService rendezvous;
    private boolean starting = false;
    private boolean started = false;
    private static final String TIE_BREAK_STRING = "::123";
    
    /**
     * a monitor obj for synchronization
     */    
    private static Object _monitor = new Object();     
    
    /** Creates a new instance of JxtaStarter */
    public JxtaStarter() {
    }
    
    public JxtaStarter(String instanceName, String certpass) {
        String clusterName = this.getClusterName();
        this.instanceName = instanceName;
        //this.networkManager = new NetworkManager("cluster1", instanceName, certpass, new HashMap());
        //this.networkManager = new NetworkManager("cluster1", instanceName, new HashMap());

        //this.networkManager = new NetworkManager(clusterName, instanceName, new HashMap());

        /*
        try {
            networkManager.start();
        } catch (PeerGroupException pge) {
            pge.printStackTrace();
        } catch (IOException ioe) {
            LOG.log(Level.WARN, "Exception occured", ioe);
        }
         */        
    }
    
    /**
     * The singleton instance of JxtaStarter
     */    
    private static JxtaStarter _soleInstance = null;
    
    private static String getInstanceName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getServerName();
    }
    
    /** Return the singleton instance
     *  lazily creates a new instance of JxtaStarter if not created yet
     */
    public static JxtaStarter createInstance() {
        synchronized(_monitor) {
            String instanceName = getInstanceName();
            if (_soleInstance == null) {
                _soleInstance = new JxtaStarter(instanceName, "password");
                //not here for now
                //_soleInstance.startJxta();
            }
            return _soleInstance;
        }
    }
    
    private String getClusterName() {
        if(clusterName == null) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            clusterName = lookup.getClusterName();
            //clusterName = (lookup.getClusterName() + TIE_BREAK_STRING);
        }
        return clusterName;       
    }
    
    //check if GMS is started and has started Jxta
    boolean checkGMS() {
        boolean result = false;
        if (GMSFactory.isGMSEnabled(getClusterName())) {
            //System.out.println("GMS enabled for my cluster: " + getClusterName());
            NetworkManagerProxy nmp = getJxtaFacade(getClusterName());
            if(nmp.isStarted()) {
                //System.out.println("GMS has started Jxta");
                networkManagerProxy = nmp;
                result = true;
            } else {
                //System.out.println("GMS has not started Jxta");
            }
        } else {
            //System.out.println("GMS NOT enabled for the cluster:" + getClusterName());
        }
        return result;
    }
    
    private NetworkManagerProxy getJxtaFacade(String groupName) {
        return com.sun.enterprise.jxtamgmt.JxtaUtil.getNetworkManagerProxy(groupName);
    }
    
    /**
     *  Starts jxta
     */
    void startJxta() {
        
        if(started) {
            return;
        }        
        if(starting) {
            return;
        }
        starting = true;
        synchronized(this) {
            gmsStartedJxta = checkGMS();
            //if GMS has started Jxta we don't need to
            //and we register for GMS events
            if(gmsStartedJxta) {
            //if(checkGMS()) {
                starting = false;
                return;
            }
        }

        try {
            networkManager = getNetworkManager();
            networkManager.start();
        } catch (PeerGroupException pge) {
            pge.printStackTrace();
        } catch (IOException ioe) {
            //FIXME later
            //LOG.log(Level.WARN, "Exception occured", ioe);
        } finally {
            starting = false;
        } 
    }
    
    /**
     *  Starts jxta
     */
    void startJxta(boolean isServer) {
        startJxta();
    }      

    /**
     *  Gets the netPeerGroup object from the network manager
     *
     * @return    The netPeerGroup value
     */    
    public PeerGroup getNetPeerGroupLastGood() {
        //return netPeerGroup;
        if (networkManager != null) {
            return networkManager.getNetPeerGroup();
        } else {
            return null;
        }
    }
    
    /**
     *  Gets the netPeerGroup object from the network manager
     *
     * @return    The netPeerGroup value
     */    
    public PeerGroup getNetPeerGroup() {
       //return netPeerGroup; 
       if(gmsStartedJxta) {
           //delegate to NetworkManagerProxy
           return createInstance().getNetworkManagerProxy().getNetPeerGroup();
       } else {
           //delegate to NetworkManager
           return createInstance().getNetworkManager().getNetPeerGroup();
       }        
    }    
    
    
    /**
     *  Gets the RendezVousService from the netPeerGroup
     *
     * @param  instanceName  instance name value
     * @return The peerID value
     */     
    public synchronized RendezVousService getRendezvous() {
        if(rendezvous == null) {
            if(getNetPeerGroup() != null) {
                rendezvous = getNetPeerGroup().getRendezVousService();
            }
        }
        return rendezvous;
    }
   
    /**
     *  Gets the peerID attribute of the NetworkManager class
     *
     * @param  instanceName  instance name value
     * @return The peerID value
     */    
   public static PeerID getPeerIDLastGood(String instanceName) {
       //delegate to NetworkManager
       NetworkManager mgr = createInstance().getNetworkManager();
       return mgr.getPeerID(instanceName);
   } 
   
    /**
     *  Gets the peerID attribute of the NetworkManager class
     *
     * @param  instanceName  instance name value
     * @return The peerID value
     */    
   public static PeerID getPeerID(String instanceName) {
       if(gmsStartedJxta) {
           //delegate to NetworkManagerProxy
           return createInstance().getNetworkManagerProxy().getPeerID(instanceName);
       } else {
           //delegate to NetworkManager
           return createInstance().getNetworkManager().getPeerID(instanceName);
       }
   }   

   
    /**
     *  Gets the pipeID attribute of the NetworkManager class
     *
     * @param  instanceName  instance name
     * @return The pipeID value
     */   
   public static PipeID getPipeIDLastGood(String instanceName) {
       //delegate to NetworkManager
       NetworkManager mgr = createInstance().getNetworkManager();
       return mgr.getPipeID(instanceName);
   } 
   
    /**
     *  Gets the pipeID attribute of the NetworkManager class
     *
     * @param  instanceName  instance name
     * @return The pipeID value
     */   
   public static PipeID getPipeID(String instanceName) {       
       if(gmsStartedJxta) {
           //delegate to NetworkManagerProxy
           return createInstance().getNetworkManagerProxy().getPipeID(instanceName);
       } else {
           //delegate to NetworkManager
           return createInstance().getNetworkManager().getPipeID(instanceName);
       }       
   }   
   
   /**
    *  Returns the getSessionQueryPipeID ID
    *
    * @return           The HealthPipe Pipe ID
    */
   public static PipeID getSessionQueryPipeIDLastGood() {
       //delegate to NetworkManager
       NetworkManager mgr = createInstance().getNetworkManager();
       return mgr.getSessionQueryPipeID();       
   } 
   
   /**
    *  Returns the getSessionQueryPipeID ID
    *
    * @return           The HealthPipe Pipe ID
    */
   public static PipeID getSessionQueryPipeID() {       
       if(gmsStartedJxta) {
           //delegate to NetworkManagerProxy
           return createInstance().getNetworkManagerProxy().getSessionQueryPipeID();
       } else {
           //delegate to NetworkManager
           return createInstance().getNetworkManager().getSessionQueryPipeID();
       }        
   }    
    
    public NetworkManager getNetworkManagerPrevious() {
        return networkManager;
    }
    
    public synchronized NetworkManager getNetworkManager() {
        if(networkManager == null) {
            this.networkManager = new NetworkManager(getClusterName(), instanceName, new HashMap());
        }
        return networkManager;
    }    
    
    public NetworkManagerProxy getNetworkManagerProxy() {
        return networkManagerProxy;
    }    
    
    NetworkManager networkManager = null;
    NetworkManagerProxy networkManagerProxy = null;
    String instanceName = null;
    String clusterName = null;
    static boolean gmsStartedJxta = false;
    
}
