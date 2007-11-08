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
 * JxtaUtil.java
 *
 * Created on March 21, 2006, 3:29 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import net.jxta.document.AdvertisementFactory;
import net.jxta.peer.PeerID;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement; 

/**
 *
 * @author Larry White
 */
public class JxtaUtil {
    
    public static final String PROPAGATED = "propagated";   
    
    /** Creates a new instance of JxtaUtil */
    public JxtaUtil() {
    }
    
   /**
    *  Gets the pipeAdvertisement attribute of the NetworkManager class
    *
    * @param  instanceName  instance name value
    * @return           The pipeAdvertisement value
    */
   public static PipeAdvertisement getPipeAdvertisement(String instanceName) {
       PipeAdvertisement advertisement = (PipeAdvertisement)
               AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
       advertisement.setPipeID(getPipeID(instanceName));
       advertisement.setName(instanceName);
       advertisement.setType(PipeService.UnicastType);
       return advertisement;
   }
   
   /**
    *  Returns the getSessionQueryPipeID ID
    *
    * @return           The HealthPipe Pipe ID
    */
   public static PipeID getSessionQueryPipeID() {
       return JxtaStarter.getSessionQueryPipeID();
   }   
   
   /**
    *  Gets the pipeAdvertisement attribute of the NetworkManager class
    *
    * @param  instanceName  instance name value
    * @return           The pipeAdvertisement value
    */
   public static PipeAdvertisement getPropagatedPipeAdvertisement() {
       PipeAdvertisement advertisement = (PipeAdvertisement)
               AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
       advertisement.setPipeID(getSessionQueryPipeID());
       advertisement.setName(PROPAGATED);
       advertisement.setType(PipeService.PropagateType);
       return advertisement;
   } 
   
   /**
    *  Gets the pipeID attribute of the NetworkManager class
    *  FIXME: delegates to JxtaStarter - can likely remove from here later
    *
    * @param  instanceName  Description of the Parameter
    * @return           The pipeID value
    */
   public static PipeID getPipeID(String instanceName) {
       return JxtaStarter.getPipeID(instanceName);
   }    
   
   /**
    *  Gets the peerID attribute of the NetworkManager class
    *  FIXME: delegates to JxtaStarter - can likely remove from here later
    * @param  instanceName  instance name value
    * @return           The peerID value
    */   
   public static PeerID getPeerID(String instanceName) {
       return JxtaStarter.getPeerID(instanceName);
   }    
    
}
