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
 

package com.sun.enterprise.management.ext.lb;

import com.sun.appserv.management.config.LBConfig;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.pluggable.PluggableFeatureFactory;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Level;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;

import javax.management.ObjectName;

import com.sun.appserv.management.base.AMX;
import static com.sun.appserv.management.base.AMX.*;
import static com.sun.appserv.management.base.XTypes.*;
import com.sun.appserv.management.ext.lb.LoadBalancer;
import com.sun.appserv.management.config.LoadBalancerConfig;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.enterprise.management.support.AMXImplBase;
import com.sun.enterprise.server.pluggable.LBFeatureFactory;
/**
 * Implmentation class for LoadBalancer AMX MBean
 * @author Harsha R A
 * @since Appserver 9.0
 */
public final class LoadBalancerImpl extends AMXImplBase implements LoadBalancer {
    private static final String COMMA = ",";
    private static final String COLON = ":";
    private static final String EQUAL = "=";

    public static final String	NAME_PROP_VALUE	= "load-balancer";
    public static final String  LAST_APPLIED_PROPERTY = "last-applied";    
    public static final String  LAST_APPLIED_HASH_PROPERTY = "last-applied-message-digest";    
    public static final String  LAST_EXPORTED_PROPERTY = "last-exported";    

    public static final String LOADBALANCER_CONFIG_OBJECT_NAME=
                    JMX_DOMAIN+COLON+J2EE_TYPE_KEY+EQUAL+
                    LOAD_BALANCER_CONFIG+COMMA+NAME_KEY+EQUAL;

    public static final String LBCONFIG_OBJECT_NAME=
                    JMX_DOMAIN+COLON+J2EE_TYPE_KEY+EQUAL+
                    LB_CONFIG+COMMA+NAME_KEY+EQUAL;

    public static final String LOADBALANCER_OBJECT_NAME = 
                    JMX_DOMAIN+COLON+J2EE_TYPE_KEY+EQUAL+
                    LOAD_BALANCER+COMMA+NAME_KEY+EQUAL;
                    
    private static final String XML_COMMENT_START = "<!--";
    private static final String MD5 = "MD5";

    private final LBFeatureFactory lbFactory;
    private final LoadBalancerConfig loadBalancerConfig;
    private final MBeanServerConnection	mServer;

    public LoadBalancerImpl(
        final MBeanServerConnection server,
        final LoadBalancerConfig loadBalancerConfigIn ) {
        super( );
        if ( loadBalancerConfigIn == null ) {
            throw new IllegalArgumentException();
        }
        
        mServer            = server;
        loadBalancerConfig = loadBalancerConfigIn;
        
        final PluggableFeatureFactory featureFactory =
                ApplicationServer.getServerContext().getPluggableFeatureFactory();
        
        lbFactory = featureFactory.getLBFeatureFactory();
        if ( lbFactory == null ) {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Applies changes in the corresponding configuration to this LB
     */
    public void applyLBChanges() {
        final String lbConfigName = loadBalancerConfig.getLbConfigName();

        lbFactory.applyChanges(lbConfigName, getName());
        if(!loadBalancerConfig.existsProperty(LAST_APPLIED_PROPERTY))
            loadBalancerConfig.createProperty(LAST_APPLIED_PROPERTY,String.valueOf(new Date().getTime()));
        else
            loadBalancerConfig.setPropertyValue(LAST_APPLIED_PROPERTY,String.valueOf(new Date().getTime()));
        String digest = getMessageDigest();
        if(!loadBalancerConfig.existsProperty(LAST_APPLIED_HASH_PROPERTY))
            loadBalancerConfig.createProperty(LAST_APPLIED_HASH_PROPERTY,digest);
        else
            loadBalancerConfig.setPropertyValue(LAST_APPLIED_HASH_PROPERTY,digest);
    }        

    /**
     * checks if apply change is required
     * @return true if there are pending changes for this LB
     */
    public boolean isApplyChangeRequired() {
        boolean isRequired = true;
        try{
        String digest = getMessageDigest();
        String lastDigest = getLastAppliedMessageDigest();
        if(lastDigest!=null)
            isRequired = !digest.equals(lastDigest);
        }catch (Throwable t){
            getMBeanLogger().warning(t.getMessage());
            if(getMBeanLogger().isLoggable(Level.FINE))
                t.printStackTrace();
        }
        return isRequired;
                
    }  	

    /**
     * Returns the timestamp of the most recent application of referenced LBConfig
     * @return Date the timestamp when the changes were applied to the load balancer
     */
    public Date getLastApplied() {
        final String lbName = getName();
        if(loadBalancerConfig.existsProperty(LAST_APPLIED_PROPERTY))
            return new Date(Long.valueOf(loadBalancerConfig.getPropertyValue(LAST_APPLIED_PROPERTY)));
        return null;
    }
    
    /**
     * method to return the message digest of the load balaner.xml that was last
     * applied
     * @return String last-applied-digest property of the load balancer from domain.xml
     */
    public String getLastAppliedMessageDigest() {
        final String lbName = getName();
        if(loadBalancerConfig.existsProperty(LAST_APPLIED_HASH_PROPERTY))
            return loadBalancerConfig.getPropertyValue(LAST_APPLIED_HASH_PROPERTY);
        return null;
    }

    /**
     * Returns the timestamp of the most recent export of referenced LBConfig
     * @return Date timestamp
     */
    public Date getLastExported() {
        final String lbName = getName();
        final String lbConfigName = loadBalancerConfig.getLbConfigName();
        LBConfig lbConfig = getLBConfig(lbConfigName);
        if(lbConfig.existsProperty(LAST_EXPORTED_PROPERTY))
            return new Date(Long.valueOf(lbConfig.getPropertyValue(LAST_EXPORTED_PROPERTY)));
        return null;
    }

    /**
     * Exports the corresponding LBConfig information and returns the contents as a string.
     * @see com.sun.appserv.management.config.LBConfig
     * @return String the loadbalancer.xml as a string
     */    
    public String getLoadBalancerXML() {
        return getLoadBalancerXML(true);
    }
    
/*
    private LoadBalancerConfig getLoadBalancerConfig(String lbName){
        ObjectName loadBalancerConfigObjName = null;
        try{
            loadBalancerConfigObjName =
                    new ObjectName(LOADBALANCER_CONFIG_OBJECT_NAME+lbName);
        } catch ( MalformedObjectNameException e ){
            if(getMBeanLogger().isLoggable(Level.FINE))
                e.printStackTrace();
        }
        loadBalancerConfig =
                getProxy(loadBalancerConfigObjName, LoadBalancerConfig.class);        
        return loadBalancerConfig;
    }
    */
    
    private LBConfig getLBConfig(String lbConfigName){
        ObjectName lbConfigObjName = null;
        try{
            lbConfigObjName =
                    new ObjectName(LBCONFIG_OBJECT_NAME+lbConfigName);
        } catch ( MalformedObjectNameException e ){
            e.printStackTrace();
        }
        LBConfig lbConfig = getProxy(lbConfigObjName, LBConfig.class);
        return lbConfig;
    }
    
    private String getLoadBalancerXML(boolean updateTimeStamp) {
        final String lbName = getName();
        final String lbConfigName = loadBalancerConfig.getLbConfigName();
        final LBConfig lbConfig = getLBConfig(lbConfigName);
        if(updateTimeStamp) {
            if(!lbConfig.existsProperty(LAST_EXPORTED_PROPERTY))
                lbConfig.createProperty(LAST_EXPORTED_PROPERTY,String.valueOf(new Date().getTime()));
            else
                lbConfig.setPropertyValue(LAST_EXPORTED_PROPERTY,String.valueOf(new Date().getTime()));
        }

        return lbFactory.getLoadBalancerXML(lbConfigName,lbName);
                
    }
    
    private String getMessageDigest(){
        try {
            String lbxml = getLoadBalancerXML(false).split(XML_COMMENT_START)[0];
            MessageDigest md = MessageDigest.getInstance(MD5);
            md.update(lbxml.getBytes());
            String hash = new BigInteger(md.digest()).toString(16);        
            return hash;
        }catch(NoSuchAlgorithmException e){
            getMBeanLogger().warning(e.getMessage());
            if(getMBeanLogger().isLoggable(Level.FINE))
                e.printStackTrace();            
        }
        return "";
    }


    /**
      Returns the timestamp of the last time the stats on this loadbalancer were reset
     */
    public Date getLastResetTime() {
        return null;
    }            

    /**
      Reset the monitoring stats on this loadbalancer.
     */
    public void resetStats() {
        final String lbName = getName();
        final String lbConfigName = loadBalancerConfig.getLbConfigName();
        lbFactory.resetStats(lbConfigName,lbName);        
    }               

    /**
      Test the LB and Domain Application Server setup 
     */
    public boolean testConnection() {
        final String lbName = getName();
        final String lbConfigName = loadBalancerConfig.getLbConfigName();
        return lbFactory.testConnection(lbConfigName,lbName);
    }
            
    /**
     * Returns the uhealthy/healthy/quiesced status for an insatnce load balanced
     * by this load balancer.
     */
    public String getStatus(String instanceName) {
        return null;
    }     
}









