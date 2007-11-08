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
 
 * Appserver70DomainNamesResolver.java
 
 *
 
 * Created on September 11, 2003, 2:32 PM
 
 */



package com.sun.enterprise.tools.upgrade.common;



/**
 *
 *
 *
 * @author  prakash
 *
 */

import java.net.URL;

import java.io.*;

import java.util.*;

import java.util.logging.*;

import java.net.URLClassLoader;

import java.lang.reflect.*;

import java.util.Hashtable;

import com.sun.enterprise.tools.upgrade.logging.*;

import com.sun.enterprise.tools.upgrade.logging.*;

import com.sun.enterprise.util.i18n.StringManager;



public class Appserver70DomainNamesResolver {
    
    
    
    private static Logger _logger = LogService.getLogger(LogService.UPGRADE_LOGGER);
    
    private StringManager sm = StringManager.getManager(LogService.UPGRADE_LOGGER);
    
    private URLClassLoader classLoader;
    
    private Class domainRegistryClass;
    
    private Object domainRegistryObject;
    
    private static final String ASADMINUNIX = "asadmin";
    
    private static final String ASADMINWIN = "asadmin.bat";
    
    private static final String BIN = "bin";
    
    /** Creates a new instance of Appserver70DomainNamesResolver */
    
    public Appserver70DomainNamesResolver(String appserverRoot) {
        
        try{
            
            File jarFile = new File(appserverRoot+File.separator+"lib"+File.separator+"appserv-admin.jar");
            
            //System.setProperty("com.sun.aas.configRoot", "C:\\Softwares\\Sun\\AppServer7\\config");
            
            //System.setProperty("com.sun.aas.configRoot", appserverRoot+File.separator+"config");
            
            System.setProperty("com.sun.aas.configRoot", getConfigDir70(appserverRoot));
            
            URL[] jars = {jarFile.toURL()};
            
            classLoader = new URLClassLoader(jars,this.getClass().getClassLoader());
            
            domainRegistryClass = classLoader.loadClass("com.iplanet.ias.admin.common.domains.registry.DomainRegistry");
            
            Method newInstanceMethod = domainRegistryClass.getMethod( "newInstance", null );
            
            domainRegistryObject = newInstanceMethod.invoke(null, null);
            
        }catch(Exception ex){
            
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),ex);
            
        }
        
    }
    
    //Admin credential changes. Added for CR 6454007
    public Hashtable getDomainNamesPathMapping(CommonInfoModel commonInfoModel){
        Hashtable mapping = new Hashtable();
        String profile = UpgradeUtils.getUpgradeUtils(commonInfoModel).getProfileInfoFromSourceInput();	
        try{
            Method iteratorMethod = domainRegistryClass.getMethod("iterator", null);
            java.util.Iterator domainNameIterator = (java.util.Iterator)iteratorMethod.invoke(domainRegistryObject, null);
            for(;domainNameIterator.hasNext();){
                Object obj = domainNameIterator.next();
                Method dName = obj.getClass().getMethod("getName",null);
                String name = (String)dName.invoke(obj, null);
                Method dPath = obj.getClass().getMethod("getPath",null);
                String path = (String)dPath.invoke(obj, null);
                //dEntry = (com.iplanet.ias.admin.common.domains.registry.DomainEntry)obj;
    
                if(commonInfoModel.getSourceVersion().equals(UpgradeConstants.VERSION_7X)) {
                    DomainInfo dInfo = new DomainInfo(name, path, profile);
                    //Retrieve values from commonInfoModel object and set in domainInfo object
                    String dValues = commonInfoModel.getDomValuesFromPasswordFile(name);
                    if(dValues != null && !("".equals(dValues))) {
                        String dAdminUser = dValues.substring(0, dValues.indexOf(";"));
                        String dAdminPassword = dValues.substring(dValues.indexOf(";")+1, dValues.length());
                        dInfo.setDomainAdminUser(dAdminUser);
                        dInfo.setDomainAdminPassword(dAdminPassword);
                    }
                    mapping.put(name, dInfo);
                }
                //mapping.put(name, new DomainInfo(name,path));
            }
        }catch(Exception ex){
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),ex);
        }
        return mapping;
    }
    //Admin credential changes. - End
    
    public Hashtable getDomainNamesPathMapping(){
        
        Hashtable mapping = new Hashtable();
        
        try{
            
            Method iteratorMethod = domainRegistryClass.getMethod("iterator", null);
            
            java.util.Iterator domainNameIterator = (java.util.Iterator)iteratorMethod.invoke(domainRegistryObject, null);
            
            for(;domainNameIterator.hasNext();){
                
                Object obj = domainNameIterator.next();
                
                Method dName = obj.getClass().getMethod("getName",null);
                
                String name = (String)dName.invoke(obj, null);
                
                Method dPath = obj.getClass().getMethod("getPath",null);
                
                String path = (String)dPath.invoke(obj, null);
                
                //dEntry = (com.iplanet.ias.admin.common.domains.registry.DomainEntry)obj;
                                
                mapping.put(name, new DomainInfo(name,path));
                
            }
            
        }catch(Exception ex){
            
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),ex);
            
        }
        
        return mapping;
        
    }
    
    
    
    public String getConfigDir70(String source) {
        
        String osName = System.getProperty("os.name");
        
        String asenv = null;
        
        String asadmin = null;
        
        if(osName.indexOf("Windows") != -1)
            
            asadmin = source + File.separator + BIN + File.separator + ASADMINWIN;
        
        else
            
            asadmin = source + File.separator + BIN + File.separator + ASADMINUNIX;
        
        try {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(asadmin)));
            
            String readString =reader.readLine();
            
            while(readString != null) {
                
                if(readString.indexOf("asenv") != -1) {
                    
                    StringTokenizer st = new StringTokenizer(readString);
                    
                    //Read String is like . /etc/appserver/asenv.conf
                    
                    st.nextToken();
                    
                    String asenvStr = st.nextToken();
                    
                    int index = asenvStr.indexOf("asenv");
                    
                    asenv = asenvStr.substring(0,index);
                    
                    break;
                    
                }
                
                readString =reader.readLine();
                
            }
            
        }catch (Exception e) {
            
            _logger.log(Level.WARNING,sm.getString("enterprise.tools.upgrade.unknownError"),e);
            
        }
        
        return asenv;
        
    }
    
    
    
    /**
     *
     * @param args the command line arguments
     *
     */
    
    public static void main(String[] args) {
        
        Appserver70DomainNamesResolver as =new Appserver70DomainNamesResolver(args[0]);
        
        as.getDomainNamesPathMapping();
        
    }
    
    
    
}

